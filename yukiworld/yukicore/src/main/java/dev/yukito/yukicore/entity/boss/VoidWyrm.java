package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Пустотный Змей — кошмар тёмной планеты Никта. Пикирует, дышит пустотой, притягивает. 18 000 ❤. */
public class VoidWyrm extends YukiBoss {
    private Vec3 breathDir = Vec3.ZERO;

    public VoidWyrm(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(18_000, 30, 18, 0.35);
    }

    @Override
    public Element element() {
        return Element.VOID;
    }

    @Override
    public Element weakness() {
        return Element.STAR;
    }

    @Override
    protected boolean flying() {
        return true;
    }

    @Override
    protected double hoverHeight() {
        return 7;
    }

    @Override
    protected double orbitRadius() {
        return 14;
    }

    @Override
    protected double flySpeed() {
        return 0.55;
    }

    @Override
    protected void registerAttacks() {
        attack("dive", A_BITE, 40, 60, 1, 0, 40, 5, (t, k) -> {
            if (k < 12) setDeltaMovement(getDeltaMovement().add(0, 0.08, 0));
            if (k == 12) setDeltaMovement(aim(t).subtract(position()).normalize().scale(1.9));
            if (k >= 12 && k <= 30) hitOnceNear(1.5, 36, 1.5);
        });
        attack("breath", A_BREATH, 50, 120, 1, 0, 22, 3, (t, k) -> {
            if (k == 14) breathDir = aim(t).subtract(getEyePosition()).normalize();
            if (k >= 15 && k <= 45) {
                setDeltaMovement(getDeltaMovement().scale(0.5));
                Vec3 eye = getEyePosition();
                for (double d = 1; d < 18; d += 1.5) {
                    particles(ParticleTypes.DRAGON_BREATH, eye.add(breathDir.scale(d)), 2, d * 0.08, 0.01);
                }
                if (k % 3 == 0) {
                    for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(18),
                            e -> e.isAlive() && dev.yukito.yukicore.magic.MagicProjectile.isEnemy(this, e))) {
                        Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
                        if (to.length() < 18 && to.normalize().dot(breathDir) > 0.86) {
                            hitOne(e, 5, 0, eye, x -> x.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1)));
                        }
                    }
                }
            }
        });
        attack("orbs", A_CAST, 30, 80, 1, 0, 40, 3, (t, k) -> {
            if (k == 12) {
                int n = phase() >= 2 ? 8 : 5;
                for (int i = 0; i < n; i++) {
                    level().addFreshEntity(new ShulkerBullet(level(), this, t, Direction.Axis.values()[i % 3]));
                }
                shoot(Kind.VOID, getEyePosition(), aim(t), 0.8f, 26, 3f).homing(t);
            }
        });
        attack("pulse", A_ROAR, 40, 170, 2, 0, 24, 2, (t, k) -> {
            if (k == 20) {
                for (Player p : players(22)) {
                    hitOne(p, 20, 0, position(), e -> {
                        e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                        e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                    });
                }
                particles(ParticleTypes.REVERSE_PORTAL, position(), 200, 6, 0.4);
            }
        });
        attack("tail", A_SPIN, 30, 90, 2, 0, 9, 3, (t, k) -> {
            if (k == 15) hurtArea(position(), 9, 34, 2.4, null);
        });
        attack("rift", A_CAST_UP, 80, 250, 3, 0, 26, 2, (t, k) -> {
            Vec3 c = position();
            if (k == 10) summon(EntityType.ENDERMITE, 4, null);
            if (k >= 10 && k < 70) {
                pull(c, 26, 0.09);
                if (k % 4 == 0) telegraph(c.add(0, -hoverHeight() * 0.8, 0), 6, ParticleTypes.REVERSE_PORTAL);
            }
            if (k == 72) {
                hurtArea(c, 7, 60, 2.5, null);
                particles(ParticleTypes.EXPLOSION_EMITTER, c, 1, 0, 0);
            }
        });
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.PHANTOM, phase == 2 ? 3 : 5, null);
    }
}
