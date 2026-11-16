package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Пламенный Феникс — страж Испытания Синтары. Один раз возрождается из пепла. 10 000 ❤. */
public class InfernalPhoenix extends YukiBoss {
    private boolean reborn;

    public InfernalPhoenix(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(10_000, 28, 14, 0.4);
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
    protected boolean flying() {
        return true;
    }

    @Override
    protected double hoverHeight() {
        return 5;
    }

    @Override
    protected double orbitRadius() {
        return 12;
    }

    @Override
    protected double flySpeed() {
        return 0.5;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void registerAttacks() {
        attack("dive", A_DASH, 40, 70, 1, 0, 40, 4, (t, k) -> {
            if (k < 12) setDeltaMovement(getDeltaMovement().add(0, 0.07, 0));
            if (k == 12) setDeltaMovement(aim(t).subtract(position()).normalize().scale(1.8));
            if (k >= 12 && k <= 30) {
                hitOnceNear(1.5, 32, 1.6);
                particles(ParticleTypes.FLAME, position(), 14, 0.8, 0.02);
            }
        });
        attack("feathers", A_CAST, 24, 34, 1, 3, 32, 5, (t, k) -> {
            if (k == 12) fan(Kind.FEATHER, t, phase() * 2 + 3, 0.14, 1.4f, 18, 0);
        });
        attack("tornado", A_SPIN, 70, 150, 1, 0, 30, 2, (t, k) -> {
            if (k == 10) {
                Vec3 start = position().add(0, -hoverHeight(), 0);
                for (int i = 0; i < 50; i++) {
                    final int step = i;
                    later(i * 2 + 1, () -> {
                        Vec3 want = t.position();
                        Vec3 at = start.add(want.subtract(start).scale(Math.min(1.0, step / 30.0)));
                        for (int h = 0; h < 6; h++) {
                            double a = step * 0.6 + h;
                            particles(ParticleTypes.FLAME, at.add(Math.cos(a) * (0.6 + h * 0.25), h * 0.8,
                                    Math.sin(a) * (0.6 + h * 0.25)), 2, 0.05, 0.01);
                        }
                        if (step % 3 == 0) hurtArea(at, 2.0, 7, 0.4, e -> e.igniteForSeconds(4));
                    });
                }
            }
        });
        attack("gust", A_ROAR, 30, 110, 1, 0, 10, 3, (t, k) -> {
            if (k == 15) {
                for (Player p : players(10)) {
                    hitOne(p, 18, 2.6, position(), e -> e.igniteForSeconds(5));
                }
                particles(ParticleTypes.FLAME, position(), 120, 4, 0.4);
                sound(SoundEvents.ENDER_DRAGON_FLAP, 3f, 0.6f);
            }
        });
        attack("sun_flare", A_CAST_UP, 60, 200, 2, 0, 32, 2, (t, k) -> {
            if (k >= 8 && k <= 44 && k % 9 == 8) {
                for (Player p : players(32)) {
                    meteor(p.position().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3), 24, 30, 3f, Kind.METEOR);
                }
            }
        });
    }

    @Override
    public void die(DamageSource src) {
        if (!reborn && !level().isClientSide) {
            // возрождение из пепла: один раз
            reborn = true;
            setHealth(getMaxHealth() * 0.4f);
            setAnim(A_ROAR);
            particles(ParticleTypes.FLAME, position(), 300, 3, 0.4);
            particles(ParticleTypes.TOTEM_OF_UNDYING, position(), 120, 2, 0.6);
            sound(SoundEvents.TOTEM_USE, 3f, 0.7f);
            bar.setName(Component.translatable("boss.yukicore.reborn", getDisplayName()));
            summon(EntityType.BLAZE, 4, Component.translatable("entity.yukicore.minion.ash"));
            return;
        }
        super.die(src);
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.BLAZE, 3, Component.translatable("entity.yukicore.minion.ash"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Reborn", reborn);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        reborn = tag.getBoolean("Reborn");
    }
}
