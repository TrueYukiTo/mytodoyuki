package dev.yukito.yukicore.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import java.util.ArrayList;
import java.util.List;

/**
 * Камни стихий и Наковальня стихий: оружие или инструмент в правой руке, камень в левой, ПКМ по наковальне —
 * камень вставлен (до 3 на предмет). Эффекты срабатывают при ударе (см. GearEvents).
 */
public final class Gems {
    private Gems() {}

    public static final int MAX = 3;
    static final String TAG = "YukiGems";

    public enum Type {
        EMBER(ChatFormatting.GOLD), FROST(ChatFormatting.AQUA), BLOOD(ChatFormatting.DARK_RED), STORM(ChatFormatting.YELLOW);

        final ChatFormatting color;

        Type(ChatFormatting color) {
            this.color = color;
        }

        String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static List<Type> of(ItemStack stack) {
        List<Type> out = new ArrayList<>();
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return out;
        CompoundTag t = cd.copyTag();
        ListTag l = t.getList(TAG, Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) {
            try {
                out.add(Type.valueOf(l.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return out;
    }

    static int count(ItemStack stack, Type type) {
        int n = 0;
        for (Type t : of(stack)) if (t == type) n++;
        return n;
    }

    static void add(ItemStack stack, Type type) {
        CompoundTag t = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag l = t.getList(TAG, Tag.TAG_STRING);
        l.add(StringTag.valueOf(type.name()));
        t.put(TAG, l);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** Строки подсказки для оружия/инструмента с камнями. */
    public static void tooltip(ItemStack stack, List<Component> tooltip) {
        List<Type> gems = of(stack);
        if (gems.isEmpty()) return;
        tooltip.add(Component.translatable("gear.yukicore.gems", gems.size(), MAX).withStyle(ChatFormatting.LIGHT_PURPLE));
        for (Type g : gems) tooltip.add(Component.literal(" ◆ ").append(Component.translatable("gear.yukicore.gem." + g.id())).withStyle(g.color));
    }

    public static class GemItem extends Item {
        final Type type;

        public GemItem(Properties props, Type type) {
            super(props);
            this.type = type;
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("gear.yukicore.gem." + type.id()).withStyle(type.color));
            tooltip.add(Component.translatable("gear.yukicore.gem.how").withStyle(ChatFormatting.GRAY));
        }
    }

    static boolean socketable(ItemStack st) {
        return !st.isEmpty() && st.isDamageableItem() && st.getMaxStackSize() == 1;
    }

    public static class AnvilBlock extends Block {
        private static final VoxelShape SHAPE = Block.box(1, 0, 3, 15, 13, 13);

        public AnvilBlock(Properties props) {
            super(props);
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
            return SHAPE;
        }

        @Override
        protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                                  InteractionHand hand, BlockHitResult hit) {
            ItemStack weapon = player.getMainHandItem(), gem = player.getOffhandItem();
            if (!(gem.getItem() instanceof GemItem g) || !socketable(weapon)) {
                if (!level.isClientSide) player.displayClientMessage(Component.translatable("gear.yukicore.anvil.how")
                        .withStyle(ChatFormatting.GRAY), true);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (of(weapon).size() >= MAX) {
                if (!level.isClientSide) player.displayClientMessage(Component.translatable("gear.yukicore.anvil.full")
                        .withStyle(ChatFormatting.RED), true);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                add(weapon, g.type);
                if (!player.getAbilities().instabuild) gem.shrink(1);
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 1.4f);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1f);
                ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        40, 0.4, 0.3, 0.4, 0.5);
                player.displayClientMessage(Component.translatable("gear.yukicore.anvil.done",
                        Component.translatable("gear.yukicore.gem." + g.type.id())).withStyle(g.type.color), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}
