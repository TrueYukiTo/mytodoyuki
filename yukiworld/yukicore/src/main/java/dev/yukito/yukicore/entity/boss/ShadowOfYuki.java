package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Тень Юки — финальный босс Целестии. Владеет льдом, пламенем и звёздными клинками. 30 000 ❤. */
public class ShadowOfYuki extends YukiBoss {
    public ShadowOfYuki(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PINK);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return base(30_000, 36, 26, 0.32);
    }

    @Override
    public Element element() {
        return Element.STAR;
    }

    @Override
    public Element weakness() {
        return Element.VOID;
    }

    @Override
    protected boolean flying() {
        return true;
    }

    @Override
    protected double hoverHeight() {
        return 2.5;
    }

    @Override
    protected double orbitRadius() {
        return 8;
    }

    @Override
    protected void registerAttacks() {
        attack("sword_rain", A_CAST_UP, 50, 100, 1, 0, 32, 4, (t, k) -> {
            if (k >= 6 && k <= 36 && k % 5 == 1) {
                for (Player p : players(32)) {
                    meteor(p.position().add(random.nextGaussian() * 2.5, 0, random.nextGaussian() * 2.5), 20, 26, 1.8f, Kind.BLADE);
                }
            }
        });
        attack("dash_slash", A_DASH, 24, 70, 1, 0, 30, 4, (t, k) -> {
            if (k == 6) {
                Vec3 look = t.getLookAngle().multiply(1, 0, 1).normalize();
                blink(t.position().subtract(look.scale(3)).add(0, 0.5, 0));
            }
            if (k == 12) hurtCone(5, 0.1, 40, 1.8);
        });
        attack("dual", A_CAST, 36, 50, 1, 3, 32, 4, (t, k) -> {
            if (k >= 10 && k <= 28 && k % 6 == 4) {
                shoot(k % 12 == 4 ? Kind.FROST : Kind.FLAME, handPos(), aim(t), 1.3f, 24, 2f);
            }
        });
        attack("clones", A_SPIN, 30, 220, 2, 0, 40, 2, (t, k) -> {
            if (k == 15) summon(EntityType.VEX, 4, Component.translatable("entity.yukicore.minion.shadow"));
        });
        attack("eclipse", A_ROAR, 40, 150, 2, 0, 14, 3, (t, k) -> {
            if (k == 10) fangRing(3, 10, 0);
            if (k == 14) fangRing(5, 16, 0);
            if (k == 18) fangRing(7, 22, 0);
            if (k == 22) {
                fangRing(9, 28, 0);
                for (int i = 0; i < 12; i++) {
                    Vec3 d = new Vec3(Math.cos(i * Math.PI / 6), 0, Math.sin(i * Math.PI / 6));
                    shoot(Kind.VOID, position().add(0, 1.5, 0), position().add(0, 1.5, 0).add(d), 0.8f, 22, 2f);
                }
            }
        });
        attack("collapse", A_CAST_UP, 90, 280, 3, 0, 24, 2, (t, k) -> {
            Vec3 c = position();
            if (k < 70) {
                pull(c, 24, 0.07);
                if (k % 4 == 0) {
                    telegraph(c.add(0, -hoverHeight() + 0.2, 0), 7, ParticleTypes.END_ROD);
                    particles(ParticleTypes.REVERSE_PORTAL, c.add(0, 1, 0), 40, 3, 0.2);
                }
            }
            if (k == 72) {
                hurtArea(c, 7.5, 80, 3.0, null);
                particles(ParticleTypes.EXPLOSION_EMITTER, c, 3, 2, 0);
                particles(ParticleTypes.END_ROD, c, 200, 4, 0.6);
            }
        });
        attack("star_wave", A_SLAM, 40, 140, 3, 0, 20, 3, (t, k) -> {
            if (k == 20) {
                Vec3 ground = position().add(0, -hoverHeight(), 0);
                shockwave(ground, 18, 30, ParticleTypes.END_ROD);
            }
        });
    }

    @Override
    protected void onPhase(int phase) {
        summon(EntityType.VEX, phase == 2 ? 3 : 6, Component.translatable("entity.yukicore.minion.shadow"));
    }
}
