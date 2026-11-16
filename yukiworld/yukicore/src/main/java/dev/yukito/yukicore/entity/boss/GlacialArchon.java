package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ледяной Архонт — страж Испытания Юки. Двуручный меч, щит и ледяные копья. 10 000 ❤. */
public class GlacialArchon extends YukiBoss {
    public GlacialArchon(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(10_000, 30, 26, 0.3);
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
        return 3;
    }

    @Override
    protected void registerAttacks() {
        attack("slash", A_SWING, 20, 22, 1, 0, 6, 5, (t, k) -> {
            if (k == 10) hurtCone(6.5, 0.15, 34, 1.4);
        });
        attack("frost_wave", A_SLAM, 34, 70, 1, 3, 24, 3, (t, k) -> {
            if (k == 16) {
                fangLine(t, 16, 0);
                Vec3 dir = t.position().subtract(position()).multiply(1, 0, 1).normalize();
                for (int i = 1; i <= 12; i++) {
                    Vec3 at = position().add(dir.scale(i * 1.5));
                    later(i, () -> {
                        hurtArea(at, 1.5, 26, 0.6, null);
                        particles(ParticleTypes.SNOWFLAKE, at.add(0, 0.5, 0), 12, 0.4, 0.05);
                    });
                }
                if (phase() >= 2) {
                    fangLine(t, 14, 0.5f);
                    fangLine(t, 14, -0.5f);
                }
            }
        });
        attack("shield_bash", A_DASH, 26, 100, 1, 4, 18, 2, (t, k) -> {
            if (k == 6) {
                Vec3 d = t.position().subtract(position()).multiply(1, 0, 1).normalize();
                setDeltaMovement(d.x * 1.5, 0.15, d.z * 1.5);
            }
            if (k >= 6 && k <= 18) hitOnceNear(1.2, 24, 2.5);
        });
        attack("spears", A_GROUND, 40, 110, 1, 0, 26, 3, (t, k) -> {
            if (k == 10 || (k == 22 && phase() >= 2) || (k == 32 && phase() >= 3)) {
                for (Player p : players(26)) {
                    Vec3 at = p.position();
                    for (int i = 0; i < 16; i += 4) later(i + 1, () -> telegraph(at, 1.6, ParticleTypes.SNOWFLAKE));
                    later(16, () -> {
                        hurtArea(at, 1.8, 32, 0, e -> {
                            e.push(0, 0.9, 0);
                            e.hurtMarked = true;
                            e.setTicksFrozen(200);
                        });
                        particles(ParticleTypes.ITEM_SNOWBALL, at.add(0, 1, 0), 30, 0.3, 0.3);
                        particles(ParticleTypes.SNOWFLAKE, at.add(0, 1.5, 0), 30, 0.2, 0.2);
                    });
                }
            }
        });
        attack("prison", A_CAST, 30, 190, 2, 0, 24, 2, (t, k) -> {
            if (k == 15) {
                t.addEffect(new MobEffectInstance(ModMagic.FROZEN, 50, 0));
                Vec3 at = t.position();
                later(50, () -> hurtArea(at, 2.2, 32, 1.0, null));
            }
        });
        attack("avalanche", A_CAST_UP, 60, 210, 3, 0, 30, 2, (t, k) -> {
            if (k >= 8 && k <= 44 && k % 6 == 2) {
                for (Player p : players(30)) {
                    meteor(p.position().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3), 22, 28, 3f, Kind.BOULDER);
                }
            }
        });
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.STRAY, 4, Component.translatable("entity.yukicore.minion.frost"));
    }
}
