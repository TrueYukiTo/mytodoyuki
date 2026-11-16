package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ледяной Колосс — хранитель планеты Юкия. 10 000 ❤, 7 атак. */
public class FrostColossus extends YukiBoss {
    public FrostColossus(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(10_000, 30, 24, 0.27);
    }

    @Override
    public Element element() {
        return Element.ICE;
    }

    @Override
    public Element weakness() {
        return Element.FIRE;
    }

    @Override
    protected double keepDistance() {
        return 4;
    }

    @Override
    protected void registerAttacks() {
        attack("swipe", A_SWING, 22, 26, 1, 0, 6, 5, (t, k) -> {
            if (k == 11) hurtCone(6.5, 0.2, 32, 1.6);
        });
        attack("slam", A_SLAM, 40, 90, 1, 0, 9, 3, (t, k) -> {
            if (k < 20 && k % 4 == 0) telegraph(position(), 7, ParticleTypes.SNOWFLAKE);
            if (k == 22) {
                hurtArea(position(), 7, 40, 1.5, e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3)));
                fangRing(4, 10, 2);
                fangRing(6.5, 16, 6);
                particles(ParticleTypes.EXPLOSION_EMITTER, position(), 1, 0, 0);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 2f, 0.6f);
            }
        });
        attack("spikes", A_GROUND, 44, 100, 1, 3, 24, 3, (t, k) -> {
            if (k == 14) fangLine(t, 16, 0);
            if (k == 20 && phase() >= 2) {
                fangLine(t, 14, 0.4f);
                fangLine(t, 14, -0.4f);
            }
            if (k == 26 && phase() >= 3) fangRing(3, 12, 2);
        });
        attack("boulder", A_THROW, 30, 70, 1, 7, 32, 3, (t, k) -> {
            if (k == 18) {
                Vec3 from = position().add(0, getBbHeight() + 0.5, 0);
                Vec3 to = aim(t).add(t.getDeltaMovement().scale(10));
                double dist = from.distanceTo(to);
                shoot(Kind.BOULDER, from, to.add(0, dist * 0.18, 0), 1.5f, 30, 3.5f);
                if (phase() >= 2) shoot(Kind.BOULDER, from, to.add(3, dist * 0.18, 3), 1.4f, 26, 3f);
            }
        });
        attack("quake", A_SLAM, 50, 150, 2, 0, 20, 2, (t, k) -> {
            if (k == 8) setDeltaMovement(0, 1.1, 0);
            if (k == 28) shockwave(position(), 16, 28, ParticleTypes.SNOWFLAKE);
        });
        attack("avalanche", A_CAST_UP, 60, 190, 3, 0, 30, 2, (t, k) -> {
            if (k % 6 == 0 && k >= 6 && k <= 42) {
                for (Player p : players(30)) {
                    meteor(p.position().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3), 24, 30, 3f, Kind.BOULDER);
                }
            }
        });
        attack("frost_roar", A_ROAR, 60, 240, 3, 0, 14, 2, (t, k) -> {
            if (k % 5 == 0) {
                for (Player p : players(12)) {
                    hitOne(p, 4, 0, position(), e -> e.setTicksFrozen(Math.min(300, e.getTicksFrozen() + 30)));
                }
                particles(ParticleTypes.SNOWFLAKE, position().add(0, 2, 0), 60, 6, 0.05);
            }
        });
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.STRAY, phase == 2 ? 3 : 5, Component.translatable("entity.yukicore.minion.frost"));
        fangRing(5, 18, 4);
    }
}
