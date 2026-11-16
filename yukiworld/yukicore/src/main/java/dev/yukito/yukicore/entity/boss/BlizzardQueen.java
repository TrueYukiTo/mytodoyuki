package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import dev.yukito.yukicore.registry.ModMagic;
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

/** Королева Метелей — владычица Авроры. Парит, телепортируется, засыпает арену сосульками. 12 500 ❤. */
public class BlizzardQueen extends YukiBoss {
    public BlizzardQueen(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(12_500, 24, 16, 0.3);
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
    protected boolean flying() {
        return true;
    }

    @Override
    protected double hoverHeight() {
        return 3.5;
    }

    @Override
    protected double orbitRadius() {
        return 9;
    }

    /** Позиция одного из четырёх кружащих осколков (совпадает с моделью). */
    public Vec3 shardPos(int i, float age) {
        double a = age * 0.08 + i * Math.PI / 2;
        return position().add(Math.cos(a) * 2.6, getBbHeight() * 0.55, Math.sin(a) * 2.6);
    }

    @Override
    protected void registerAttacks() {
        attack("bolts", A_CAST, 24, 26, 1, 3, 32, 5, (t, k) -> {
            if (k == 12) fan(Kind.FROST, t, phase() * 2 + 1, 0.18, 1.3f, 22, 0);
        });
        attack("nova", A_CAST_UP, 30, 90, 1, 0, 7, 3, (t, k) -> {
            if (k < 16 && k % 4 == 0) telegraph(position().add(0, -hoverHeight() * 0.5, 0), 7, ParticleTypes.SNOWFLAKE);
            if (k == 18) {
                hurtArea(position(), 8, 30, 2.0, null);
                particles(ParticleTypes.SNOWFLAKE, position(), 120, 4, 0.3);
                sound(SoundEvents.GLASS_BREAK, 2f, 0.6f);
            }
        });
        attack("blink", A_SPIN, 16, 110, 1, 0, 6, 3, (t, k) -> {
            if (k == 8) {
                double a = random.nextDouble() * Math.PI * 2;
                blink(t.position().add(Math.cos(a) * 10, 4, Math.sin(a) * 10));
            }
        });
        attack("icicles", A_CAST_UP, 50, 130, 1, 0, 32, 3, (t, k) -> {
            if (k >= 6 && k <= 40 && k % 4 == 0) {
                for (Player p : players(32)) {
                    meteor(p.position().add(p.getDeltaMovement().scale(8)).add(random.nextGaussian() * 2.5, 0,
                            random.nextGaussian() * 2.5), 18, 20, 2f, Kind.ICICLE);
                }
            }
        });
        attack("prison", A_CAST, 30, 200, 2, 0, 24, 2, (t, k) -> {
            if (k == 15) {
                t.addEffect(new MobEffectInstance(ModMagic.FROZEN, 60, 0));
                t.setTicksFrozen(260);
                Vec3 at = t.position();
                for (int i = 0; i < 60; i += 6) later(i + 1, () -> telegraph(at, 1.5, ParticleTypes.SNOWFLAKE));
                later(60, () -> hurtArea(at, 2.2, 34, 1.0, null));
            }
        });
        attack("shard_storm", A_SPIN, 40, 150, 2, 0, 32, 3, (t, k) -> {
            if (k >= 5 && k % 5 == 0) {
                Vec3 from = shardPos(k / 5 % 4, tickCount);
                shoot(Kind.FROST, from, aim(t), 0.9f, 18, 0).homing(t);
            }
        });
        attack("blizzard", A_CAST_UP, 100, 280, 3, 0, 24, 2, (t, k) -> {
            if (k % 10 == 0) {
                for (Player p : players(22)) {
                    hitOne(p, 6, 0, position(), e -> {
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0));
                    });
                    particles(ParticleTypes.SNOWFLAKE, p.position().add(0, 2, 0), 40, 3, 0.2);
                }
            }
            if (k % 20 == 5) {
                for (Player p : players(22)) meteor(p.position(), 16, 18, 2f, Kind.ICICLE);
            }
        });
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.STRAY, 3, Component.translatable("entity.yukicore.minion.frost"));
        if (phase == 3) summon(EntityType.VEX, 3, Component.translatable("entity.yukicore.minion.spirit"));
    }
}
