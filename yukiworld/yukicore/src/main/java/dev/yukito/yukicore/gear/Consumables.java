package dev.yukito.yukicore.gear;

import dev.yukito.yukicore.magic.Fx;
import dev.yukito.yukicore.magic.MagicProjectile;
import dev.yukito.yukicore.magic.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Расходники 7.4: бомбы, зелья стихий, свитки. */
public final class Consumables {
    private Consumables() {}

    // ------------------------------------------------------------------ бомбы
    public static class Bomb extends Item {
        final int type; // 0 ледяная, 1 огненная, 2 ловушка

        public Bomb(Properties props, int type) {
            super(props);
            this.type = type;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack st = player.getItemInHand(hand);
            if (level instanceof ServerLevel lvl) {
                MagicProjectile.Kind kind = switch (type) {
                    case 0 -> MagicProjectile.Kind.FROST_BOMB;
                    case 1 -> MagicProjectile.Kind.FIRE_BOMB;
                    default -> MagicProjectile.Kind.SNARE_BOMB;
                };
                float dmg = type == 0 ? 14 : type == 1 ? 18 : 1;
                int power = type == 2 ? 12 : 4;
                MagicProjectile.create(lvl, player, kind, player.getEyePosition().add(player.getLookAngle().scale(0.6)), dmg, 4.5f)
                        .power(power).life(100).launch(player.getLookAngle().scale(1.3).add(0, 0.15, 0));
                lvl.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1f, 0.6f);
            }
            player.getCooldowns().addCooldown(this, 12);
            if (!player.getAbilities().instabuild) st.shrink(1);
            return InteractionResultHolder.sidedSuccess(st, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath(), tooltip);
        }
    }

    // ------------------------------------------------------------------ зелья стихий
    public static class ElementPotion extends Item {
        final int type; // 0 ледяная кожа, 1 огненная кровь, 2 удача Синтары

        public ElementPotion(Properties props, int type) {
            super(props);
            this.type = type;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity) {
            return 32;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide) {
                var effect = switch (type) {
                    case 0 -> GearItems.ICE_SKIN;
                    case 1 -> GearItems.FIRE_BLOOD;
                    default -> GearItems.SINTARA_LUCK;
                };
                entity.addEffect(new MobEffectInstance(effect, 20 * 60 * 4, 0));
            }
            if (entity instanceof Player p && p.getAbilities().instabuild) return stack;
            stack.shrink(1);
            if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            if (entity instanceof Player p && !p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                p.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
            return stack;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath(), tooltip);
        }
    }

    // ------------------------------------------------------------------ свитки
    public static class Scroll extends Item {
        final int type; // 0 возврат домой, 1 снежные големы, 2 огненный дождь

        public Scroll(Properties props, int type) {
            super(props);
            this.type = type;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack st = player.getItemInHand(hand);
            if (!(player instanceof ServerPlayer p)) return InteractionResultHolder.success(st);
            boolean ok = switch (type) {
                case 0 -> recall(p);
                case 1 -> {
                    Fx.summonAllies(p, EntityType.SNOW_GOLEM, 3, 90, "gear.yukicore.golem_name", 40, 4, 1.4f);
                    Fx.sound(p.serverLevel(), p.position(), SoundEvents.SNOW_GOLEM_AMBIENT, 1.5f, 0.8f);
                    yield true;
                }
                default -> {
                    fireRain(p);
                    yield true;
                }
            };
            if (ok) {
                if (!player.getAbilities().instabuild) st.shrink(1);
                player.getCooldowns().addCooldown(this, 40);
            }
            return InteractionResultHolder.success(st);
        }

        private static boolean recall(ServerPlayer p) {
            BlockPos at = p.getRespawnPosition();
            ServerLevel dest = p.server.getLevel(p.getRespawnDimension());
            if (at == null || dest == null) {
                dest = p.server.overworld();
                at = dest.getSharedSpawnPos();
            }
            p.serverLevel().sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 60, 0.4, 1, 0.4, 0.05);
            p.teleportTo(dest, at.getX() + 0.5, at.getY() + 0.6, at.getZ() + 0.5, p.getYRot(), p.getXRot());
            p.resetFallDistance();
            dest.playSound(null, at, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
            p.displayClientMessage(Component.translatable("gear.yukicore.recall").withStyle(ChatFormatting.AQUA), true);
            return true;
        }

        private static void fireRain(ServerPlayer p) {
            ServerLevel lvl = p.serverLevel();
            Vec3 at = Fx.target(p, 32);
            for (int i = 0; i < 30; i++) {
                Scheduler.later(lvl, i * 2 + 1, () -> {
                    Vec3 spot = Fx.ground(lvl, at.add((lvl.random.nextDouble() * 2 - 1) * 7, 0, (lvl.random.nextDouble() * 2 - 1) * 7));
                    MagicProjectile.create(lvl, p, MagicProjectile.Kind.METEOR, spot.add(0, 18, 0), 16, 2f).size(1f)
                            .launch(new Vec3(0, -1.4, 0));
                });
            }
            for (int t = 0; t < 60; t += 8) Scheduler.later(lvl, t + 1, () -> Fx.ring(lvl, at, 7, ParticleTypes.FLAME));
            Fx.sound(lvl, at, SoundEvents.BLAZE_SHOOT, 2f, 0.5f);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath(), tooltip);
        }
    }
}
