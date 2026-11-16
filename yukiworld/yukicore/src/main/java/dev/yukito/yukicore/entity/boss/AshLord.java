package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Повелитель Пепла — огненный демон Пиролита. Метеоры, извержения, рывки. 15 000 ❤. */
public class AshLord extends YukiBoss {
    public AshLord(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(15_000, 34, 22, 0.28);
    }

    @Override
    public Element element() {
        return Element.FIRE;
    }

    @Override
    public Element weakness() {
        return Element.ICE;
    }

    @Override
    protected void registerAttacks() {
        attack("cleave", A_SWING, 22, 26, 1, 0, 6, 5, (t, k) -> {
            if (k == 11) hurtCone(6.5, 0.25, 38, 1.4);
        });
        attack("fireballs", A_CAST, 30, 60, 1, 5, 32, 3, (t, k) -> {
            if (k == 12 || k == 17 || k == 22) shoot(Kind.FLAME, handPos(), aim(t), 1.2f, 24, 2.5f);
        });
        attack("meteors", A_CAST_UP, 60, 140, 1, 0, 36, 3, (t, k) -> {
            if (k >= 8 && k <= 44 && k % 9 == 8) {
                for (Player p : players(36)) {
                    meteor(p.position().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3), 26, 34, 3.5f, Kind.METEOR);
                }
            }
        });
        attack("flame_ring", A_SLAM, 40, 110, 1, 0, 12, 3, (t, k) -> {
            if (k < 20 && k % 5 == 0) telegraph(position(), 3, ParticleTypes.FLAME);
            if (k == 22) shockwave(position(), 14, 26, ParticleTypes.FLAME);
        });
        attack("charge", A_DASH, 30, 130, 1, 6, 24, 2, (t, k) -> {
            if (k == 8) {
                Vec3 d = t.position().subtract(position()).multiply(1, 0, 1).normalize();
                setDeltaMovement(d.x * 1.7, 0.2, d.z * 1.7);
            }
            if (k >= 8 && k <= 22) {
                hitOnceNear(1.5, 34, 2.0);
                particles(ParticleTypes.FLAME, position(), 10, 0.6, 0.02);
            }
        });
        attack("eruption", A_GROUND, 50, 170, 2, 0, 30, 3, (t, k) -> {
            if (k == 10 || k == 20 || k == 30) {
                for (Player p : players(30)) {
                    Vec3 at = p.position();
                    for (int i = 0; i < 20; i += 4) later(i + 1, () -> telegraph(at, 1.8, ParticleTypes.FLAME));
                    later(22, () -> {
                        hurtArea(at, 2.0, 30, 0, e -> {
                            e.igniteForSeconds(5);
                            e.push(0, 1.2, 0);
                            e.hurtMarked = true;
                        });
                        particles(ParticleTypes.LAVA, at.add(0, 1, 0), 30, 0.6, 0.4);
                        particles(ParticleTypes.FLAME, at.add(0, 2, 0), 60, 0.4, 0.2);
                        level().playSound(null, at.x, at.y, at.z, SoundEvents.BLAZE_SHOOT,
                                net.minecraft.sounds.SoundSource.HOSTILE, 1.5f, 0.6f);
                    });
                }
            }
        });
        attack("inferno", A_ROAR, 80, 280, 3, 0, 16, 2, (t, k) -> {
            if (k % 10 == 0) hurtArea(position(), 8, 6, 0.3, e -> e.igniteForSeconds(4));
            if (k % 20 == 0) {
                for (int i = 0; i < 8; i++) {
                    Vec3 d = new Vec3(Math.cos(i * Math.PI / 4 + k), 0, Math.sin(i * Math.PI / 4 + k));
                    shoot(Kind.FLAME, position().add(0, 2, 0), position().add(0, 2, 0).add(d), 0.9f, 20, 2f);
                }
            }
            particles(ParticleTypes.FLAME, position().add(0, 1, 0), 20, 4, 0.02);
        });
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.BLAZE, phase == 2 ? 3 : 5, Component.translatable("entity.yukicore.minion.ash"));
        if (phase == 3) summon(EntityType.MAGMA_CUBE, 3, null);
    }
}
