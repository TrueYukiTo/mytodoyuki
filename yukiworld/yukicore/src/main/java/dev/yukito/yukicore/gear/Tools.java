package dev.yukito.yukicore.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Инструменты 7.4: бур Юки (3×3 с переплавкой), посох строителя, сфера телепорта. */
public final class Tools {
    private Tools() {}

    // ------------------------------------------------------------------ бур
    public static class Drill extends PickaxeItem {
        public Drill(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public float getDestroySpeed(ItemStack stack, BlockState state) {
            // копает и как лопата
            if (state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL)) return getTier().getSpeed();
            return super.getDestroySpeed(stack, state);
        }

        @Override
        public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
            return super.isCorrectToolForDrops(stack, state) || state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip("yuki_drill", tooltip);
        }
    }

    private static boolean drilling;

    /** Сломать соседние блоки 3×3 в плоскости взгляда (вызывается из события ломания). */
    static void drill(ServerPlayer p, BlockPos center, BlockState state) {
        if (drilling || p.isShiftKeyDown()) return;
        HitResult hr = p.pick(6, 0, false);
        Direction face = hr instanceof BlockHitResult b ? b.getDirection() : Direction.UP;
        float hardness = state.getDestroySpeed(p.level(), center);
        drilling = true;
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) continue;
                    BlockPos q = switch (face.getAxis()) {
                        case X -> center.offset(0, a, b);
                        case Y -> center.offset(a, 0, b);
                        case Z -> center.offset(a, b, 0);
                    };
                    BlockState s = p.level().getBlockState(q);
                    float h = s.getDestroySpeed(p.level(), q);
                    if (s.isAir() || h < 0 || h > hardness + 3 || !p.getMainHandItem().isCorrectToolForDrops(s)) continue;
                    p.gameMode.destroyBlock(q);
                }
            }
        } finally {
            drilling = false;
        }
    }

    /** Переплавка добычи бура в печи «на лету». */
    static ItemStack smelt(ServerLevel lvl, ItemStack in) {
        Optional<ItemStack> out = lvl.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(in), lvl)
                .map(r -> r.value().getResultItem(lvl.registryAccess()).copy());
        if (out.isEmpty() || out.get().isEmpty()) return in;
        ItemStack r = out.get();
        r.setCount(Math.min(r.getMaxStackSize(), r.getCount() * in.getCount()));
        return r;
    }

    // ------------------------------------------------------------------ посох строителя
    public static class BuilderWand extends Item {
        public BuilderWand(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            Level level = ctx.getLevel();
            Player p = ctx.getPlayer();
            if (p == null) return InteractionResult.PASS;
            BlockPos origin = ctx.getClickedPos();
            Direction face = ctx.getClickedFace();
            BlockState state = level.getBlockState(origin);
            Item blockItem = state.getBlock().asItem();
            if (!(blockItem instanceof BlockItem)) return InteractionResult.PASS;
            if (level.isClientSide) return InteractionResult.SUCCESS;
            int radius = p.isShiftKeyDown() ? 0 : 2; // Shift — только ряд из 1, иначе квадрат 5×5
            List<BlockPos> targets = new ArrayList<>();
            for (int a = -radius; a <= radius; a++) {
                for (int b = -radius; b <= radius; b++) {
                    BlockPos src = switch (face.getAxis()) {
                        case X -> origin.offset(0, a, b);
                        case Y -> origin.offset(a, 0, b);
                        case Z -> origin.offset(a, b, 0);
                    };
                    BlockPos dst = src.relative(face);
                    if (level.getBlockState(src).is(state.getBlock()) && level.getBlockState(dst).canBeReplaced()
                            && level.isUnobstructed(state, dst, net.minecraft.world.phys.shapes.CollisionContext.empty())) targets.add(dst);
                }
            }
            int placed = 0;
            for (BlockPos dst : targets) {
                if (!p.getAbilities().instabuild && !take(p, blockItem)) break;
                level.setBlockAndUpdate(dst, state);
                placed++;
            }
            if (placed > 0) {
                level.playSound(null, origin, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1f, 0.9f);
                ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5,
                        placed * 2, 1.2, 1.2, 1.2, 0.02);
            }
            return InteractionResult.CONSUME;
        }

        private static boolean take(Player p, Item item) {
            for (ItemStack s : p.getInventory().items) {
                if (s.is(item)) {
                    s.shrink(1);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip("builder_wand", tooltip);
        }
    }

    // ------------------------------------------------------------------ сфера телепорта
    public static class TeleportOrb extends Item {
        public TeleportOrb(Properties props) {
            super(props);
        }

        private static CompoundTag data(ItemStack st) {
            return st.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        }

        private static void save(ItemStack st, CompoundTag t) {
            st.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            CompoundTag t = data(stack);
            return t.contains("Dim") || t.hasUUID("Friend");
        }

        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
            if (!(target instanceof Player friend)) return InteractionResult.PASS;
            if (!player.level().isClientSide) {
                ItemStack held = player.getItemInHand(hand);
                CompoundTag t = data(held);
                t.remove("Dim");
                t.putUUID("Friend", friend.getUUID());
                t.putString("FriendName", friend.getGameProfile().getName());
                save(held, t);
                player.displayClientMessage(Component.translatable("gear.yukicore.orb.friend", friend.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack st = player.getItemInHand(hand);
            if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(st);
            CompoundTag t = data(st);
            if (player.isShiftKeyDown()) {
                t.remove("Friend");
                t.putString("Dim", level.dimension().location().toString());
                t.putDouble("X", player.getX());
                t.putDouble("Y", player.getY());
                t.putDouble("Z", player.getZ());
                save(st, t);
                sp.displayClientMessage(Component.translatable("gear.yukicore.orb.saved").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                return InteractionResultHolder.success(st);
            }
            ServerLevel dest;
            double x, y, z;
            if (t.hasUUID("Friend")) {
                ServerPlayer f = sp.server.getPlayerList().getPlayer(t.getUUID("Friend"));
                if (f == null) {
                    sp.displayClientMessage(Component.translatable("gear.yukicore.orb.offline").withStyle(ChatFormatting.RED), true);
                    return InteractionResultHolder.fail(st);
                }
                dest = f.serverLevel();
                x = f.getX();
                y = f.getY();
                z = f.getZ();
            } else if (t.contains("Dim")) {
                dest = sp.server.getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                        ResourceLocation.parse(t.getString("Dim"))));
                x = t.getDouble("X");
                y = t.getDouble("Y");
                z = t.getDouble("Z");
            } else {
                sp.displayClientMessage(Component.translatable("gear.yukicore.orb.empty").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(st);
            }
            if (dest == null) return InteractionResultHolder.fail(st);
            sp.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, sp.getX(), sp.getY() + 1, sp.getZ(), 60, 0.4, 0.8, 0.4, 0.1);
            sp.teleportTo(dest, x, y, z, sp.getYRot(), sp.getXRot());
            sp.resetFallDistance();
            dest.sendParticles(ParticleTypes.PORTAL, x, y + 1, z, 80, 0.4, 0.8, 0.4, 0.4);
            dest.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1.2f);
            player.getCooldowns().addCooldown(this, 20 * 30);
            return InteractionResultHolder.success(st);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip("teleport_orb", tooltip);
            CompoundTag t = data(stack);
            if (t.hasUUID("Friend")) {
                tooltip.add(Component.translatable("gear.yukicore.orb.to_friend", t.getString("FriendName")).withStyle(ChatFormatting.LIGHT_PURPLE));
            } else if (t.contains("Dim")) {
                tooltip.add(Component.translatable("gear.yukicore.orb.to_point", (int) t.getDouble("X"), (int) t.getDouble("Y"),
                        (int) t.getDouble("Z"), t.getString("Dim")).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }
}
