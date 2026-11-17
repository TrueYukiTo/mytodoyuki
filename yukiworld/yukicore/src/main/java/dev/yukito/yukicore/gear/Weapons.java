package dev.yukito.yukicore.gear;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.Fx;
import dev.yukito.yukicore.magic.MagicProjectile;
import dev.yukito.yukicore.magic.Scheduler;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Оружие 7.4: коса Синтары, клинки «Лёд и Пламя», молот-метеор, боевой веер, ледяной лук Юки. */
public final class Weapons {
    private Weapons() {}

    public enum Kind { SCYTHE, TWIN, HAMMER, FAN }

    static final String ARROW_TAG = "yukiFrostArrow";

    static void tip(String id, List<Component> tooltip) {
        for (int i = 1; i <= 3; i++) {
            String key = "item.yukicore." + id + ".desc" + (i == 1 ? "" : i);
            if (net.minecraft.locale.Language.getInstance().has(key)) {
                tooltip.add(Component.translatable(key).withStyle(i == 1 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
            }
        }
    }

    private static int combo(Player p, String key) {
        int n = p.getPersistentData().getInt(key) + 1;
        p.getPersistentData().putInt(key, n);
        return n;
    }

    public static class Melee extends SwordItem {
        final Kind kind;

        public Melee(Tier tier, Kind kind, Properties props) {
            super(tier, props);
            this.kind = kind;
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker instanceof ServerPlayer p) onHit(p, target);
            return super.hurtEnemy(stack, target, attacker);
        }

        private void onHit(ServerPlayer p, LivingEntity t) {
            ServerLevel lvl = p.serverLevel();
            Vec3 at = t.position();
            float base = (float) p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            switch (kind) {
                case SCYTHE -> {
                    t.igniteForSeconds(4);
                    // круговой взмах: все вокруг цели горят и получают треть урона
                    Fx.area(p, at, 3, base * 0.35f, Element.FIRE, e -> e.igniteForSeconds(3));
                    Fx.ring(lvl, at.add(0, 0.5, 0), 3, ParticleTypes.FLAME);
                    if (combo(p, "yukiScythe") % 5 == 0) {
                        Vec3 c = p.position();
                        for (int r = 1; r <= 3; r++) {
                            final int rr = r;
                            Scheduler.later(lvl, r * 2, () -> Fx.ring(lvl, c.add(0, 0.3, 0), rr * 2.2, ParticleTypes.FLAME));
                        }
                        Fx.area(p, c, 7, base * 1.5f, Element.FIRE, e -> {
                            e.igniteForSeconds(6);
                            Fx.push(c, e, 1.4, 0.4);
                        });
                        Fx.sound(lvl, c, SoundEvents.BLAZE_SHOOT, 1.4f, 0.5f);
                        Fx.shake(lvl, c, 12, 0.8f, 8);
                    }
                }
                case TWIN -> {
                    int n = combo(p, "yukiTwin");
                    if (n % 2 == 0) {
                        t.igniteForSeconds(3);
                        lvl.sendParticles(ParticleTypes.FLAME, t.getX(), t.getY() + 1, t.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                    } else {
                        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
                        lvl.sendParticles(ParticleTypes.SNOWFLAKE, t.getX(), t.getY() + 1, t.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                    }
                    if (n % 3 == 0) {
                        // пар: лёд + пламя — взрыв, ослепление и урон по области
                        Fx.area(p, at, 3.5, base * 0.9f, Element.NONE, e -> e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0)));
                        lvl.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 1, at.z, 40, 1.2, 0.6, 1.2, 0.05);
                        Fx.sound(lvl, at, SoundEvents.FIRE_EXTINGUISH, 1.4f, 0.8f);
                    }
                }
                case HAMMER -> {
                    if (p.fallDistance > 1.5f) slam(p, at, base * 2f);
                }
                case FAN -> Fx.push(p.position(), t, 1.6, 0.4);
            }
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack st = player.getItemInHand(hand);
            if (kind == Kind.HAMMER && player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer sp) {
                    Vec3 look = sp.getLookAngle().multiply(1, 0, 1).normalize();
                    sp.setDeltaMovement(look.x * 1.6, 0.75, look.z * 1.6);
                    sp.hurtMarked = true;
                    float dmg = (float) sp.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 1.8f;
                    Scheduler.later(sp.serverLevel(), 16, () -> slam(sp, sp.position(), dmg));
                    Fx.sound(sp.serverLevel(), sp.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 0.6f);
                }
                player.getCooldowns().addCooldown(this, 60);
                return InteractionResultHolder.sidedSuccess(st, level.isClientSide);
            }
            if (kind == Kind.FAN) {
                if (player instanceof ServerPlayer sp) gust(sp);
                player.getCooldowns().addCooldown(this, 25);
                return InteractionResultHolder.sidedSuccess(st, level.isClientSide);
            }
            return super.use(level, player, hand);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            // боевой веер в руке: парение вниз вместо падения (движение игрока считает клиент)
            if (kind == Kind.FAN && selected && entity instanceof Player p && !p.onGround() && !p.isShiftKeyDown()
                    && !p.getAbilities().flying && p.getDeltaMovement().y < -0.12) {
                p.setDeltaMovement(p.getDeltaMovement().x, -0.12, p.getDeltaMovement().z);
                p.resetFallDistance();
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tip(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath(), tooltip);
        }
    }

    /** Удар метеора: взрыв по области, подброс, кратер из частиц. */
    static void slam(ServerPlayer p, Vec3 at, float dmg) {
        ServerLevel lvl = p.serverLevel();
        Fx.area(p, at, 4.5, dmg, Element.FIRE, e -> {
            e.igniteForSeconds(4);
            e.push(0, 0.8, 0);
            e.hurtMarked = true;
        });
        p.resetFallDistance();
        Fx.burst(lvl, at.add(0, 0.3, 0), ParticleTypes.EXPLOSION, 3, 1.5, 0);
        Fx.ring(lvl, at.add(0, 0.2, 0), 4.5, ParticleTypes.LAVA);
        Fx.ring(lvl, at.add(0, 0.2, 0), 3, ParticleTypes.FLAME);
        Fx.shake(lvl, at, 16, 1.2f, 12);
        Fx.sound(lvl, at, SoundEvents.GENERIC_EXPLODE.value(), 1.2f, 0.8f);
    }

    static void gust(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        Vec3 from = p.getEyePosition(), dir = p.getLookAngle();
        for (double d = 1; d < 9; d += 0.7) {
            lvl.sendParticles(ParticleTypes.CLOUD, from.x + dir.x * d, from.y + dir.y * d - 0.3, from.z + dir.z * d, 3,
                    d * 0.1, d * 0.06, d * 0.1, 0.02);
        }
        for (LivingEntity e : Fx.enemies(p, from, 9)) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            if (to.length() > 9 || to.normalize().dot(dir) < 0.6) continue;
            Fx.hit(p, e, 4, Element.NONE);
            e.setDeltaMovement(dir.x * 2.4, 0.6, dir.z * 2.4);
            e.hurtMarked = true;
        }
        Fx.sound(lvl, from, SoundEvents.ELYTRA_FLYING, 1f, 1.8f);
    }

    /** Ледяной лук Юки: стрелы замораживают и раскалываются на осколки. */
    public static class FrostBow extends BowItem {
        public FrostBow(Properties props) {
            super(props);
        }

        @Override
        protected void shootProjectile(LivingEntity shooter, net.minecraft.world.entity.projectile.Projectile projectile, int index,
                                       float velocity, float inaccuracy, float angle, @javax.annotation.Nullable LivingEntity target) {
            projectile.getPersistentData().putBoolean(ARROW_TAG, true);
            if (projectile instanceof AbstractArrow a) a.setBaseDamage(a.getBaseDamage() + 3);
            super.shootProjectile(shooter, projectile, index, velocity * 1.15f, inaccuracy * 0.5f, angle, target);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tip("yuki_bow", tooltip);
        }
    }

    /** Попадание ледяной стрелы: заморозка и осколки во все стороны. */
    static void frostArrowHit(LivingEntity target, Entity arrow) {
        if (!(target.level() instanceof ServerLevel lvl)) return;
        target.addEffect(new MobEffectInstance(ModMagic.FROZEN, 50, 0));
        target.setTicksFrozen(Math.min(300, target.getTicksFrozen() + 100));
        LivingEntity owner = ((AbstractArrow) arrow).getOwner() instanceof LivingEntity o ? o : null;
        Vec3 c = target.getBoundingBox().getCenter();
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3;
            MagicProjectile.create(lvl, owner, MagicProjectile.Kind.ICICLE, c.add(Math.cos(a) * 0.9, 0.3, Math.sin(a) * 0.9), 4, 0)
                    .size(0.5f).life(12).launch(new Vec3(Math.cos(a) * 0.9, 0.15, Math.sin(a) * 0.9));
        }
        lvl.sendParticles(ParticleTypes.SNOWFLAKE, c.x, c.y, c.z, 30, 0.5, 0.5, 0.5, 0.05);
        lvl.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1.5f);
    }
}
