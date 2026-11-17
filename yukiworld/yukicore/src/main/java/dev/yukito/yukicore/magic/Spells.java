package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.magic.MagicProjectile.Kind;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.yukito.yukicore.magic.Element.FIRE;
import static dev.yukito.yukicore.magic.Element.ICE;

/** Все 44 активных заклинания дерева: 22 ледяных (Юки) и 22 огненных (Синтара). */
final class Spells {
    private Spells() {}

    private static final BlockState PACKED = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState BLUE = Blocks.BLUE_ICE.defaultBlockState();
    private static final BlockState MAGMA = Blocks.MAGMA_BLOCK.defaultBlockState();
    private static final BlockState LAVA = Blocks.SHROOMLIGHT.defaultBlockState();

    static boolean cast(ServerPlayer p, Skill s, int r, float pw) {
        ServerLevel lvl = p.serverLevel();
        Vec3 eye = p.getEyePosition(), look = p.getLookAngle(), pos = p.position();
        switch (s) {
            // ================================================================ ❄ ЮКИ
            case FROST_BOLT -> {
                int n = r >= 5 ? 5 : r >= 3 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    Vec3 dir = look.yRot((float) ((i - (n - 1) / 2.0) * 0.1));
                    MagicProjectile.create(lvl, p, Kind.FROST, eye.add(look.scale(0.6)), 25 * pw, 0).power(r).launch(dir.scale(2.0));
                }
                Fx.sound(lvl, pos, SoundEvents.SNOW_GOLEM_SHOOT, 1f, 1.2f);
            }
            case ICE_SHARDS -> {
                for (int i = 0; i < 9 + r * 2; i++) {
                    Vec3 dir = look.add(rnd(lvl, 0.22), rnd(lvl, 0.12), rnd(lvl, 0.22)).normalize();
                    MagicProjectile.create(lvl, p, Kind.ICICLE, eye.add(look.scale(0.6)), 12 * pw, 0).power(r)
                            .size(0.7f).life(25).launch(dir.scale(2.4));
                }
                Fx.shake(lvl, pos, 6, 0.6f, 6);
                Fx.sound(lvl, pos, SoundEvents.GLASS_BREAK, 1.2f, 1.4f);
            }
            case FROST_NOVA -> {
                double rad = 6 + r;
                Fx.area(p, pos, rad, 30 * pw, ICE, e -> {
                    e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 50 + 10 * r, 0));
                    Fx.push(pos, e, 1.2, 0.4);
                });
                for (int i = 0; i < 16; i++) {
                    double a = i * Math.PI / 8;
                    Vec3 at = Fx.ground(lvl, pos.add(Math.cos(a) * rad * 0.7, 0, Math.sin(a) * rad * 0.7));
                    Fx.block(lvl, at, PACKED, 0.8f, 1.6f + lvl.random.nextFloat(), 0.8f, 3, 30, (float) Math.sin(a) * 0.4f, (float) -Math.cos(a) * 0.4f);
                }
                Fx.ring(lvl, pos, rad, ParticleTypes.SNOWFLAKE);
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.ITEM_SNOWBALL, 80, rad / 2, 0.1);
                Fx.shake(lvl, pos, 12, 1.2f, 10);
                Fx.sound(lvl, pos, SoundEvents.GLASS_BREAK, 1.5f, 0.6f);
            }
            case ICE_SPIKES -> {
                Vec3 dir = look.multiply(1, 0, 1).normalize();
                Set<UUID> hit = new HashSet<>();
                for (int i = 1; i <= 14 + r * 2; i++) {
                    final int k = i;
                    Scheduler.later(lvl, i, () -> {
                        Vec3 at = Fx.ground(lvl, pos.add(dir.scale(k * 1.3)));
                        float h = 2.2f + k * 0.12f + lvl.random.nextFloat();
                        Fx.block(lvl, at, k % 3 == 0 ? BLUE : PACKED, 1.1f, h, 1.1f, 3, 40,
                                (lvl.random.nextFloat() - 0.5f) * 0.5f, (lvl.random.nextFloat() - 0.5f) * 0.5f);
                        for (LivingEntity e : Fx.enemies(p, at, 1.8)) {
                            if (hit.add(e.getUUID()) && Fx.hit(p, e, 45 * pw, ICE)) {
                                e.push(0, 1.1, 0);
                                e.hurtMarked = true;
                            }
                        }
                        Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.SNOWFLAKE, 10, 0.4, 0.05);
                        if (k % 4 == 0) Fx.sound(lvl, at, SoundEvents.GLASS_BREAK, 0.8f, 0.8f);
                    });
                }
                Fx.shake(lvl, pos, 10, 0.8f, 20);
            }
            case GLACIAL_ARMOR -> {
                int dur = 20 * (30 + 5 * r);
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, dur, 2 + r));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, r >= 4 ? 2 : 1));
                p.addEffect(new MobEffectInstance(ModMagic.FROST_ARMOR, dur, r - 1));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    Fx.block(lvl, pos.add(Math.cos(a) * 1.4, 0, Math.sin(a) * 1.4), BLUE, 0.4f, 2.4f, 0.4f, 4, 24, 0, 0);
                }
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.SNOWFLAKE, 100, 0.8, 0.05);
                Fx.sound(lvl, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.4f, 0.8f);
            }
            case ICE_WALL -> {
                Vec3 dir = look.multiply(1, 0, 1).normalize();
                Vec3 side = new Vec3(-dir.z, 0, dir.x);
                Vec3 c = Fx.ground(lvl, pos.add(dir.scale(4)));
                int life = 20 * (8 + r);
                for (int i = -4; i <= 4; i++) {
                    Fx.block(lvl, Fx.ground(lvl, c.add(side.scale(i * 1.1))), i % 2 == 0 ? BLUE : PACKED, 1.15f, 3.2f, 0.9f, 6, life, 0, 0);
                }
                for (int t = 0; t < life; t += 10) {
                    Scheduler.later(lvl, t + 1, () -> {
                        for (int i = -4; i <= 4; i++) {
                            Vec3 at = c.add(side.scale(i * 1.1));
                            for (LivingEntity e : Fx.enemies(p, at.add(0, 1, 0), 1.6)) {
                                Fx.hit(p, e, 15 * pw, ICE);
                                Fx.push(pos, e, 1.4, 0.3);
                            }
                        }
                    });
                }
                Fx.shake(lvl, pos, 8, 0.7f, 10);
                Fx.sound(lvl, c, SoundEvents.GLASS_PLACE, 2f, 0.5f);
            }
            case FROST_BLINK -> {
                Vec3 to = Fx.target(p, 14 + 3 * r);
                Fx.area(p, pos, 4, 25 * pw, ICE, e -> e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 40, 0)));
                Fx.ring(lvl, pos, 3, ParticleTypes.SNOWFLAKE);
                for (int i = 0; i < 20; i++) Fx.burst(lvl, pos.lerp(to, i / 20.0).add(0, 1, 0), ParticleTypes.SNOWFLAKE, 3, 0.2, 0.01);
                p.teleportTo(to.x, to.y + 0.1, to.z);
                p.resetFallDistance();
                Fx.area(p, to, 4, 25 * pw, ICE, e -> e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 40, 0)));
                Fx.ring(lvl, to, 3, ParticleTypes.SNOWFLAKE);
                Fx.sound(lvl, to, SoundEvents.ENDERMAN_TELEPORT, 1f, 1.6f);
            }
            case GLACIER_CRASH -> {
                Vec3 at = Fx.target(p, 32);
                float size = 4 + r * 0.5f;
                for (int i = 0; i < 20; i += 4) Scheduler.later(lvl, i + 1, () -> Fx.ring(lvl, at, size + 1, ParticleTypes.SNOWFLAKE));
                Fx.fallingBlock(lvl, at, BLUE, size, 18, 60);
                Scheduler.later(lvl, 20, () -> {
                    Fx.area(p, at, size + 2, 180 * pw, ICE, e -> {
                        e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 60, 0));
                        Fx.push(at, e, 1.6, 0.8);
                    });
                    Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.EXPLOSION_EMITTER, 2, 1, 0);
                    Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.ITEM_SNOWBALL, 160, size, 0.3);
                    for (int i = 0; i < 10; i++) {
                        double a = i * Math.PI / 5;
                        Fx.block(lvl, Fx.ground(lvl, at.add(Math.cos(a) * size, 0, Math.sin(a) * size)), PACKED, 0.9f, 2.5f, 0.9f, 3, 50,
                                (float) Math.sin(a) * 0.6f, (float) -Math.cos(a) * 0.6f);
                    }
                    Fx.shake(lvl, at, 32, 2.5f, 20);
                    Fx.sound(lvl, at, SoundEvents.GENERIC_EXPLODE.value(), 2f, 0.5f);
                    Fx.sound(lvl, at, SoundEvents.GLASS_BREAK, 2f, 0.4f);
                });
            }
            case BLIZZARD -> {
                Vec3 at = Fx.target(p, 32);
                int waves = (8 + r * 2) * 2;
                for (int i = 0; i < waves; i++) {
                    final int k = i;
                    Scheduler.later(lvl, i * 10 + 1, () -> {
                        Fx.area(p, at, 9, 20 * pw, ICE, e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3)));
                        Fx.column(lvl, at, 8, 9, ParticleTypes.SNOWFLAKE, 120);
                        Fx.burst(lvl, at.add(0, 6, 0), ParticleTypes.CLOUD, 30, 6, 0.02);
                        if (k % 4 == 0) {
                            Vec3 ic = at.add(rnd(lvl, 7), 0, rnd(lvl, 7));
                            MagicProjectile.create(lvl, p, Kind.ICICLE, ic.add(0, 14, 0), 30 * pw, 2.5f).launch(new Vec3(0, -1.3, 0));
                        }
                    });
                }
                Fx.sound(lvl, at, SoundEvents.POWDER_SNOW_BREAK, 2f, 0.5f);
            }
            case ICE_PRISON -> {
                LivingEntity t = Fx.aimed(p, 32);
                if (t == null) return false;
                float w = t.getBbWidth() + 1.2f, h = t.getBbHeight() + 1;
                Fx.block(lvl, t.position().add(0, -0.3, 0), PACKED, w, h, w, 4, 64, 0, 0);
                t.addEffect(new MobEffectInstance(ModMagic.FROZEN, 60, 0));
                t.setTicksFrozen(300);
                Fx.sound(lvl, t.position(), SoundEvents.GLASS_PLACE, 2f, 0.6f);
                Scheduler.later(lvl, 60, () -> {
                    if (!t.isAlive()) return;
                    Fx.hit(p, t, 250 * pw, ICE);
                    Fx.area(p, t.position(), 5, 60 * pw, ICE, null);
                    for (int i = 0; i < 12; i++) {
                        Vec3 dir = new Vec3(rnd(lvl, 1), Math.abs(rnd(lvl, 0.6)), rnd(lvl, 1)).normalize();
                        MagicProjectile.create(lvl, p, Kind.ICICLE, t.position().add(0, 1, 0), 20 * pw, 0).launch(dir.scale(1.4));
                    }
                    Fx.burst(lvl, t.position().add(0, 1, 0), ParticleTypes.ITEM_SNOWBALL, 120, 1.2, 0.3);
                    Fx.shake(lvl, t.position(), 16, 1.2f, 10);
                    Fx.sound(lvl, t.position(), SoundEvents.GLASS_BREAK, 2.5f, 0.5f);
                });
            }
            case FROZEN_ORB -> {
                MagicProjectile orb = MagicProjectile.create(lvl, p, Kind.FROST, eye.add(look.scale(1)), 90 * pw, 5f).power(r)
                        .size(2.4f).life(70).launch(look.scale(0.45));
                for (int t = 3; t < 66; t += 3) {
                    final int k = t;
                    Scheduler.later(lvl, t, () -> {
                        if (orb.isRemoved()) return;
                        for (int i = 0; i < 6; i++) {
                            double a = i * Math.PI / 3 + k * 0.3;
                            MagicProjectile.create(lvl, p, Kind.ICICLE, orb.position(), 15 * pw, 0).size(0.6f).life(20)
                                    .launch(new Vec3(Math.cos(a), 0.05, Math.sin(a)).scale(1.3));
                        }
                    });
                }
                Fx.sound(lvl, pos, SoundEvents.BEACON_ACTIVATE, 1.5f, 1.6f);
            }
            case ICE_GOLEM -> {
                Fx.summonAllies(p, EntityType.IRON_GOLEM, r >= 4 ? 2 : 1, 60 + 10 * r, "entity.yukicore.ally.ice_golem",
                        200 + 60 * r, 25 + 8 * r * pw, 1.3f);
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.SNOWFLAKE, 120, 2, 0.1);
                Fx.sound(lvl, pos, SoundEvents.IRON_GOLEM_REPAIR, 2f, 0.6f);
            }
            case AVALANCHE -> {
                Vec3 dir = look.multiply(1, 0, 1).normalize();
                Vec3 side = new Vec3(-dir.z, 0, dir.x);
                Set<UUID> hit = new HashSet<>();
                for (int step = 1; step <= 14; step++) {
                    final int k = step;
                    Scheduler.later(lvl, k * 2, () -> {
                        Vec3 c = pos.add(dir.scale(k * 2));
                        for (int i = -4; i <= 4; i += 2) {
                            Vec3 at = Fx.ground(lvl, c.add(side.scale(i)));
                            Fx.block(lvl, at, Blocks.SNOW_BLOCK.defaultBlockState(), 1.8f, 1.4f + lvl.random.nextFloat(), 1.8f, 2, 16,
                                    0.3f, 0);
                            Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.ITEM_SNOWBALL, 12, 0.8, 0.1);
                        }
                        for (LivingEntity e : Fx.enemies(p, c, 6)) {
                            if (hit.add(e.getUUID()) && Fx.hit(p, e, 120 * pw, ICE)) {
                                e.push(dir.x * 2, 0.7, dir.z * 2);
                                e.hurtMarked = true;
                                e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 50, 0));
                            }
                        }
                        Fx.sound(lvl, c, SoundEvents.SNOW_BREAK, 2f, 0.5f);
                    });
                }
                Fx.shake(lvl, pos, 30, 1.8f, 30);
            }
            case POLAR_STORM -> {
                int ticks = 20 * (12 + r);
                for (int t = 0; t < ticks; t += 10) {
                    Scheduler.later(lvl, t + 1, () -> {
                        if (!p.isAlive()) return;
                        Fx.column(lvl, p.position(), 10, 16, ParticleTypes.SNOWFLAKE, 100);
                        List<LivingEntity> en = Fx.enemies(p, p.position(), 18);
                        for (int i = 0; i < Math.min(3, en.size()); i++) {
                            LivingEntity e = en.get(lvl.random.nextInt(en.size()));
                            Fx.hit(p, e, 60 * pw, ICE);
                            e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 20, 0));
                            Fx.block(lvl, e.position(), BLUE, 0.6f, 3f, 0.6f, 2, 12, 0, 0);
                            for (double y = 0; y < 12; y += 0.6) {
                                lvl.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + y, e.getZ(), 1, 0.05, 0, 0.05, 0);
                            }
                            Fx.sound(lvl, e.position(), SoundEvents.GLASS_BREAK, 1.2f, 1.5f);
                        }
                    });
                }
                Fx.shake(lvl, pos, 20, 0.8f, 20);
                Fx.sound(lvl, pos, SoundEvents.ELYTRA_FLYING, 1.5f, 0.6f);
            }
            case ABSOLUTE_ZERO -> {
                Fx.area(p, pos, 24, 600 * pw, ICE, e -> {
                    e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 160, 0));
                    e.setTicksFrozen(400);
                });
                for (int ring = 1; ring <= 4; ring++) {
                    final int rr = ring;
                    Scheduler.later(lvl, ring * 3, () -> {
                        double rad = rr * 5.5;
                        int n = 10 + rr * 6;
                        for (int i = 0; i < n; i++) {
                            double a = i * Math.PI * 2 / n;
                            Vec3 at = Fx.ground(lvl, pos.add(Math.cos(a) * rad, 0, Math.sin(a) * rad));
                            Fx.block(lvl, at, rr % 2 == 0 ? BLUE : PACKED, 1.2f, 3f + rr, 1.2f, 3, 80,
                                    (float) Math.sin(a) * 0.35f, (float) -Math.cos(a) * 0.35f);
                        }
                        Fx.ring(lvl, pos, rad, ParticleTypes.SNOWFLAKE);
                    });
                }
                Fx.burst(lvl, pos.add(0, 2, 0), ParticleTypes.END_ROD, 300, 8, 0.2);
                Fx.shake(lvl, pos, 48, 3f, 30);
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3));
                Fx.sound(lvl, pos, SoundEvents.WITHER_BREAK_BLOCK, 2f, 0.5f);
                Fx.sound(lvl, pos, SoundEvents.GLASS_BREAK, 2f, 0.4f);
            }
            case YUKI_AVATAR -> {
                p.addEffect(new MobEffectInstance(ModMagic.AVATAR_ICE, 20 * (25 + 5 * r), r - 1));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * (25 + 5 * r), 2));
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.SNOWFLAKE, 400, 3, 0.4);
                Fx.shake(lvl, pos, 32, 2f, 20);
                Fx.sound(lvl, pos, SoundEvents.BEACON_POWER_SELECT, 2f, 0.6f);
            }

            // ================================================================ ✹ СИНТАРА
            case EMBER_BOLT -> {
                int n = r >= 5 ? 5 : r >= 3 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    Scheduler.later(lvl, i * 3 + 1, () -> {
                        Vec3 e2 = p.getEyePosition(), l2 = p.getLookAngle();
                        MagicProjectile.create(lvl, p, Kind.FLAME, e2.add(l2.scale(0.6)), 25 * pw, 1.8f).power(r).launch(l2.scale(1.9));
                        Fx.sound(lvl, e2, SoundEvents.BLAZE_SHOOT, 0.8f, 1.3f);
                    });
                }
            }
            case FIRE_SPRAY -> {
                for (int t = 0; t < 40 + r * 4; t += 2) {
                    Scheduler.later(lvl, t + 1, () -> {
                        if (!p.isAlive()) return;
                        Vec3 e2 = p.getEyePosition().add(0, -0.3, 0), l2 = p.getLookAngle();
                        for (double d = 1; d < 11; d += 1) {
                            lvl.sendParticles(ParticleTypes.FLAME, e2.x + l2.x * d, e2.y + l2.y * d, e2.z + l2.z * d,
                                    3, d * 0.08, d * 0.08, d * 0.08, 0.02);
                        }
                        cone(p, e2, l2, 11, 0.85, 6 * pw);
                    });
                }
                Fx.sound(lvl, pos, SoundEvents.FIRECHARGE_USE, 1.5f, 0.6f);
            }
            case FLAME_RING -> {
                double rad = 6 + r;
                Fx.area(p, pos, rad, 30 * pw, FIRE, e -> Fx.push(pos, e, 1.6, 0.5));
                for (int k = 1; k <= 3; k++) Fx.ring(lvl, pos, rad * k / 3, ParticleTypes.FLAME);
                Fx.burst(lvl, pos.add(0, 0.5, 0), ParticleTypes.LAVA, 40, rad / 2, 0.2);
                Fx.shake(lvl, pos, 12, 1.2f, 10);
                Fx.sound(lvl, pos, SoundEvents.FIRECHARGE_USE, 1.5f, 0.6f);
            }
            case FIRE_PILLARS -> {
                List<LivingEntity> en = Fx.enemies(p, pos, 22);
                if (en.isEmpty()) en = List.of();
                int n = Math.min(4 + r, en.size());
                for (int i = 0; i < Math.max(1, n); i++) {
                    Vec3 at = i < en.size() ? en.get(i).position() : Fx.target(p, 20);
                    for (int t = 0; t < 10; t += 3) Scheduler.later(lvl, t + 1, () -> Fx.ring(lvl, at, 1.6, ParticleTypes.FLAME));
                    Scheduler.later(lvl, 12, () -> {
                        Fx.area(p, at, 2.4, 70 * pw, FIRE, e -> {
                            e.push(0, 1.3, 0);
                            e.hurtMarked = true;
                        });
                        Fx.block(lvl, at, MAGMA, 1.4f, 5f, 1.4f, 3, 18, 0, 0);
                        Fx.column(lvl, at, 7, 1.2, ParticleTypes.FLAME, 80);
                        Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.LAVA, 20, 0.6, 0.3);
                        Fx.sound(lvl, at, SoundEvents.BLAZE_SHOOT, 1.5f, 0.5f);
                    });
                }
                Fx.shake(lvl, pos, 16, 1f, 14);
            }
            case PHOENIX_WINGS -> {
                int dur = 20 * (12 + 3 * r);
                p.addEffect(new MobEffectInstance(ModMagic.PHOENIX, dur, r - 1));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, r >= 4 ? 2 : 1));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, 1));
                p.push(0, 1.0, 0);
                p.hurtMarked = true;
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.FLAME, 160, 1.6, 0.08);
                Fx.sound(lvl, pos, SoundEvents.ENDER_DRAGON_FLAP, 1.5f, 1.4f);
            }
            case LAVA_POOL -> {
                Vec3 at = Fx.target(p, 28);
                int life = 20 * (8 + r);
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) + Math.abs(z) > 3) continue;
                        Fx.block(lvl, Fx.ground(lvl, at.add(x * 1.3, 0, z * 1.3)).add(0, -0.05, 0),
                                (x + z) % 2 == 0 ? MAGMA : LAVA, 1.3f, 0.25f, 1.3f, 4, life, 0, 0);
                    }
                }
                for (int t = 0; t < life; t += 10) {
                    Scheduler.later(lvl, t + 1, () -> {
                        Fx.area(p, at, 3.6, 15 * pw, FIRE, null);
                        Fx.column(lvl, at, 1.5, 3.4, ParticleTypes.FLAME, 30);
                        lvl.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 6, 2, 0.1, 2, 0.1);
                    });
                }
                Fx.sound(lvl, at, SoundEvents.LAVA_AMBIENT, 2f, 0.8f);
            }
            case FLAME_DASH -> {
                Vec3 d = look.multiply(1, 0.3, 1).normalize().scale(2.2 + 0.2 * r);
                p.setDeltaMovement(d.x, Math.max(0.25, d.y), d.z);
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 16, 3));
                Set<UUID> hit = new HashSet<>();
                for (int i = 0; i < 12; i++) {
                    final int k = i;
                    Scheduler.later(lvl, i + 1, () -> {
                        Fx.burst(lvl, p.position().add(0, 0.6, 0), ParticleTypes.FLAME, 20, 0.5, 0.03);
                        for (LivingEntity e : Fx.enemies(p, p.position(), 2.4)) {
                            if (hit.add(e.getUUID())) Fx.hit(p, e, 50 * pw, FIRE);
                        }
                        if (k == 11) {
                            Fx.area(p, p.position(), 4, 60 * pw, FIRE, e -> Fx.push(p.position(), e, 1.2, 0.4));
                            Fx.burst(lvl, p.position(), ParticleTypes.EXPLOSION, 3, 1, 0);
                            Fx.shake(lvl, p.position(), 10, 1f, 8);
                        }
                    });
                }
                p.resetFallDistance();
                Fx.sound(lvl, pos, SoundEvents.BLAZE_SHOOT, 1f, 0.7f);
            }
            case FIREBALL_BARRAGE -> {
                for (int i = 0; i < 16 + r * 2; i++) {
                    Scheduler.later(lvl, i * 2 + 1, () -> {
                        Vec3 e2 = p.getEyePosition(), l2 = p.getLookAngle();
                        Vec3 dir = l2.add(rnd(lvl, 0.08), rnd(lvl, 0.05), rnd(lvl, 0.08)).normalize();
                        MagicProjectile.create(lvl, p, Kind.FLAME, e2.add(l2.scale(0.8)).add(rnd(lvl, 0.6), rnd(lvl, 0.4), rnd(lvl, 0.6)),
                                30 * pw, 2.2f).size(0.9f).launch(dir.scale(1.8));
                        Fx.sound(lvl, e2, SoundEvents.BLAZE_SHOOT, 0.6f, 1.2f + lvl.random.nextFloat() * 0.4f);
                    });
                }
                Fx.shake(lvl, pos, 6, 0.4f, 34);
            }
            case METEOR -> {
                Vec3 at = Fx.target(p, 40);
                int n = r >= 5 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    Vec3 spot = at.add(i == 0 ? 0 : (i == 1 ? 4 : -4), 0, i == 0 ? 0 : 3);
                    for (int k = 0; k < 20; k += 4) Scheduler.later(lvl, k + 1, () -> Fx.ring(lvl, spot, 5, ParticleTypes.FLAME));
                    Scheduler.later(lvl, 4 + i * 6, () -> MagicProjectile.create(lvl, p, Kind.METEOR, spot.add(3, 22, 3),
                            220 * pw, 5f + 0.4f * r).power(r).size(3.2f).launch(new Vec3(-0.15, -1.2, -0.15)));
                    Scheduler.later(lvl, 22 + i * 6, () -> Fx.shake(lvl, spot, 28, 2f, 16));
                }
                Fx.sound(lvl, pos, SoundEvents.BLAZE_AMBIENT, 1f, 0.5f);
            }
            case DRAGON_BREATH -> {
                for (int t = 0; t < 80; t += 2) {
                    Scheduler.later(lvl, t + 1, () -> {
                        if (!p.isAlive()) return;
                        Vec3 e2 = p.getEyePosition().add(0, -0.2, 0), l2 = p.getLookAngle();
                        for (double d = 1; d < 22; d += 1.2) {
                            lvl.sendParticles(d % 2.4 < 1.2 ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME,
                                    e2.x + l2.x * d, e2.y + l2.y * d, e2.z + l2.z * d, 3, d * 0.07, d * 0.07, d * 0.07, 0.01);
                        }
                        cone(p, e2, l2, 22, 0.88, 10 * pw);
                    });
                }
                Fx.shake(lvl, pos, 8, 0.5f, 80);
                Fx.sound(lvl, pos, SoundEvents.ENDER_DRAGON_GROWL, 1.2f, 1.3f);
            }
            case FLAME_TORNADO -> {
                Vec3 dir = look.multiply(1, 0, 1).normalize();
                for (int tor = -1; tor <= 1; tor++) {
                    Vec3 start = pos.add(new Vec3(-dir.z, 0, dir.x).scale(tor * 3));
                    for (int t = 0; t < 60; t += 2) {
                        final int k = t;
                        Scheduler.later(lvl, t + 1, () -> {
                            Vec3 c = Fx.ground(lvl, start.add(dir.scale(2 + k * 0.45)));
                            for (int h = 0; h < 10; h++) {
                                double a = k * 0.7 + h * 0.6, rr = 0.5 + h * 0.25;
                                lvl.sendParticles(ParticleTypes.FLAME, c.x + Math.cos(a) * rr, c.y + h * 0.7,
                                        c.z + Math.sin(a) * rr, 2, 0.05, 0.05, 0.05, 0.01);
                            }
                            for (LivingEntity e : Fx.enemies(p, c, 3)) {
                                Fx.hit(p, e, 12 * pw, FIRE);
                                Vec3 pull = c.subtract(e.position()).normalize().scale(0.3);
                                e.push(pull.x, 0.25, pull.z);
                                e.hurtMarked = true;
                            }
                        });
                    }
                }
                Fx.sound(lvl, pos, SoundEvents.FIRECHARGE_USE, 2f, 0.4f);
            }
            case FIRE_SPIRITS -> {
                Fx.summonAllies(p, EntityType.VEX, 3 + (r >= 4 ? 1 : 0), 60 + 10 * r, "entity.yukicore.ally.fire_spirit",
                        60 + 20 * r, 14 + 5 * r * pw, 1.4f);
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.FLAME, 160, 2, 0.1);
                Fx.sound(lvl, pos, SoundEvents.BLAZE_AMBIENT, 2f, 1.3f);
            }
            case METEOR_SHOWER -> {
                Vec3 at = Fx.target(p, 40);
                int n = 16 + r * 2;
                for (int i = 0; i < n; i++) {
                    Scheduler.later(lvl, i * 5 + 1, () -> {
                        Vec3 spot = Fx.ground(lvl, at.add(rnd(lvl, 12), 0, rnd(lvl, 12)));
                        Fx.ring(lvl, spot, 3, ParticleTypes.FLAME);
                        MagicProjectile.create(lvl, p, Kind.METEOR, spot.add(4, 26, 4), 120 * pw, 4f).size(2.4f)
                                .launch(new Vec3(-0.16, -1.25, -0.16));
                    });
                }
                for (int t = 20; t < n * 5 + 20; t += 10) Scheduler.later(lvl, t, () -> Fx.shake(lvl, at, 32, 1.2f, 10));
                Fx.sound(lvl, pos, SoundEvents.WITHER_SHOOT, 1.5f, 0.5f);
            }
            case SUN_STRIKE -> {
                Vec3 at = Fx.target(p, 48);
                for (int t = 0; t < 30; t += 3) Scheduler.later(lvl, t + 1, () -> {
                    Fx.ring(lvl, at, 7, ParticleTypes.END_ROD);
                    Fx.column(lvl, at, 30, 0.5, ParticleTypes.END_ROD, 20);
                });
                Scheduler.later(lvl, 30, () -> {
                    Fx.area(p, at, 8, 500 * pw, FIRE, e -> {
                        e.igniteForSeconds(10);
                        Fx.push(at, e, 1.8, 1.0);
                    });
                    Fx.block(lvl, at, Blocks.GLOWSTONE.defaultBlockState(), 4f, 40f, 4f, 2, 24, 0, 0);
                    Fx.column(lvl, at, 40, 4, ParticleTypes.FLAME, 300);
                    Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.EXPLOSION_EMITTER, 3, 2, 0);
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lvl);
                    if (bolt != null) {
                        bolt.moveTo(at);
                        bolt.setVisualOnly(true);
                        lvl.addFreshEntity(bolt);
                    }
                    Fx.shake(lvl, at, 48, 3f, 24);
                    Fx.sound(lvl, at, SoundEvents.GENERIC_EXPLODE.value(), 3f, 0.4f);
                });
                Fx.sound(lvl, at, SoundEvents.BEACON_ACTIVATE, 2f, 0.5f);
            }
            case SUPERNOVA -> {
                Fx.area(p, pos, 24, 700 * pw, FIRE, e -> {
                    e.igniteForSeconds(12);
                    Fx.push(pos, e, 2.5, 0.8);
                });
                for (int ring = 1; ring <= 5; ring++) {
                    final int rr = ring;
                    Scheduler.later(lvl, ring * 2, () -> {
                        Fx.ring(lvl, pos, rr * 4.8, ParticleTypes.FLAME);
                        Fx.ring(lvl, pos.add(0, 1, 0), rr * 4.8, ParticleTypes.LAVA);
                    });
                }
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.EXPLOSION_EMITTER, 4, 2, 0);
                Fx.shake(lvl, pos, 48, 3f, 30);
                p.addEffect(new MobEffectInstance(ModMagic.REBIRTH, 20 * 60, 0));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0));
                Fx.sound(lvl, pos, SoundEvents.GENERIC_EXPLODE.value(), 3f, 0.5f);
            }
            case SINTARA_AVATAR -> {
                p.addEffect(new MobEffectInstance(ModMagic.AVATAR_FIRE, 20 * (25 + 5 * r), r - 1));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * (25 + 5 * r), 0));
                Fx.burst(lvl, pos.add(0, 1, 0), ParticleTypes.FLAME, 400, 3, 0.4);
                Fx.shake(lvl, pos, 32, 2f, 20);
                Fx.sound(lvl, pos, SoundEvents.ENDER_DRAGON_GROWL, 2f, 1.2f);
            }
            // ================================================================ 7.3: Путь стужи
            case ICE_LANCE -> {
                MagicProjectile.create(lvl, p, Kind.ICICLE, eye.add(look.scale(0.8)), 70 * pw, r >= 3 ? 2.5f : 0).power(r)
                        .size(1.5f + 0.1f * r).life(40).launch(look.scale(3.2));
                Fx.burst(lvl, eye.add(look), ParticleTypes.SNOWFLAKE, 20, 0.3, 0.05);
                Fx.sound(lvl, pos, SoundEvents.TRIDENT_THROW.value(), 1.2f, 1.5f);
            }
            case FROST_TRAP -> trap(p, lvl, Fx.target(p, 24), r, pw, true);
            case HAILSTORM -> rain(p, lvl, Fx.target(p, 32), r, pw, true);
            case ARCTIC_GALE -> gale(p, lvl, r, pw, true);
            case FROST_COMET -> comet(p, lvl, Fx.target(p, 48), r, pw, true);
            // ================================================================ 7.3: Путь жара
            case FLAME_LANCE -> {
                MagicProjectile.create(lvl, p, Kind.FLAME, eye.add(look.scale(0.8)), 70 * pw, r >= 3 ? 2.5f : 0).power(r)
                        .size(1.4f + 0.1f * r).life(40).launch(look.scale(3.2));
                Fx.burst(lvl, eye.add(look), ParticleTypes.FLAME, 20, 0.3, 0.05);
                Fx.sound(lvl, pos, SoundEvents.BLAZE_SHOOT, 1.2f, 0.8f);
            }
            case FIRE_MINE -> trap(p, lvl, Fx.target(p, 24), r, pw, false);
            case ASH_RAIN -> rain(p, lvl, Fx.target(p, 32), r, pw, false);
            case HEAT_WAVE -> gale(p, lvl, r, pw, false);
            case SOLAR_FLARE -> comet(p, lvl, Fx.target(p, 48), r, pw, false);
            default -> {
                return false;
            }
        }
        return true;
    }

    /** Ледяная ловушка / огненная мина: руна на земле, через секунду — взрыв. */
    private static void trap(ServerPlayer p, ServerLevel lvl, Vec3 at, int r, float pw, boolean ice) {
        var part = ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME;
        double rad = 4 + 0.5 * r;
        for (int t = 0; t < 20; t += 4) Scheduler.later(lvl, t + 1, () -> {
            Fx.ring(lvl, at, rad, part);
            Fx.ring(lvl, at, rad * 0.5, ice ? ParticleTypes.END_ROD : ParticleTypes.SMALL_FLAME);
        });
        Scheduler.later(lvl, 22, () -> {
            Fx.area(p, at, rad, 55 * pw, ice ? ICE : FIRE, e -> {
                if (ice) e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 50 + 10 * r, 0));
                else e.igniteForSeconds(4 + r);
                e.push(0, 0.9, 0);
                e.hurtMarked = true;
            });
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI / 5;
                Fx.block(lvl, Fx.ground(lvl, at.add(Math.cos(a) * rad * 0.6, 0, Math.sin(a) * rad * 0.6)), ice ? BLUE : MAGMA,
                        0.7f, 1.8f + lvl.random.nextFloat(), 0.7f, 3, 26, (float) Math.sin(a) * 0.4f, (float) -Math.cos(a) * 0.4f);
            }
            Fx.burst(lvl, at.add(0, 1, 0), ice ? ParticleTypes.ITEM_SNOWBALL : ParticleTypes.LAVA, 60, rad / 2, 0.1);
            Fx.shake(lvl, at, 14, 1f, 10);
            Fx.sound(lvl, at, ice ? SoundEvents.GLASS_BREAK : SoundEvents.GENERIC_EXPLODE.value(), 1.6f, ice ? 0.7f : 1.2f);
        });
        Fx.sound(lvl, at, SoundEvents.AMETHYST_BLOCK_CHIME, 1f, ice ? 1.6f : 0.6f);
    }

    /** Град / пепельный дождь: снаряды падают с неба по области 3 секунды. */
    private static void rain(ServerPlayer p, ServerLevel lvl, Vec3 at, int r, float pw, boolean ice) {
        int n = 26 + 4 * r;
        for (int i = 0; i < n; i++) {
            Scheduler.later(lvl, i * 2 + 1, () -> {
                Vec3 spot = Fx.ground(lvl, at.add(rnd(lvl, 8), 0, rnd(lvl, 8)));
                MagicProjectile.create(lvl, p, ice ? Kind.ICICLE : Kind.METEOR, spot.add(rnd(lvl, 1), 18, rnd(lvl, 1)),
                        22 * pw, ice ? 1.5f : 2f).size(ice ? 0.9f : 1.0f).launch(new Vec3(0, -1.4, 0));
            });
        }
        for (int t = 0; t < n * 2; t += 8) Scheduler.later(lvl, t + 1, () -> {
            Fx.ring(lvl, at, 8, ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.ASH);
            Fx.shake(lvl, at, 20, 0.5f, 8);
        });
        Fx.sound(lvl, at, ice ? SoundEvents.POWDER_SNOW_STEP : SoundEvents.FIRECHARGE_USE, 2f, 0.5f);
    }

    /** Арктический шквал / тепловая волна: конус перед магом — урон и отбрасывание. */
    private static void gale(ServerPlayer p, ServerLevel lvl, int r, float pw, boolean ice) {
        Vec3 from = p.getEyePosition().add(0, -0.3, 0), dir = p.getLookAngle();
        double len = 12 + r;
        for (int t = 0; t < 10; t += 2) Scheduler.later(lvl, t + 1, () -> {
            for (double d = 1; d < len; d += 0.8) {
                lvl.sendParticles(ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, from.x + dir.x * d, from.y + dir.y * d,
                        from.z + dir.z * d, 4, d * 0.12, d * 0.08, d * 0.12, 0.05);
            }
        });
        for (LivingEntity e : Fx.enemies(p, from, len)) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            if (to.length() > len || to.normalize().dot(dir) < 0.7) continue;
            if (Fx.hit(p, e, 35 * pw, ice ? ICE : FIRE)) {
                Fx.push(from, e, 2.2, 0.5);
                if (ice) e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                else e.igniteForSeconds(4 + r);
            }
        }
        Fx.shake(lvl, from, 10, 0.8f, 8);
        Fx.sound(lvl, from, ice ? SoundEvents.ELYTRA_FLYING : SoundEvents.BLAZE_SHOOT, 1.5f, ice ? 1.6f : 0.5f);
    }

    /** Ледяная комета / солнечная вспышка: огромная глыба падает с неба через 1,5 секунды. */
    private static void comet(ServerPlayer p, ServerLevel lvl, Vec3 at, int r, float pw, boolean ice) {
        double rad = 9 + r * 0.5;
        Fx.fallingBlock(lvl, at, ice ? BLUE : MAGMA, 5f + 0.4f * r, 30, 46);
        for (int t = 0; t < 30; t += 3) Scheduler.later(lvl, t + 1, () -> {
            Fx.ring(lvl, at, rad, ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME);
            Fx.column(lvl, at, 24, 0.6, ice ? ParticleTypes.END_ROD : ParticleTypes.LAVA, 10);
        });
        Scheduler.later(lvl, 31, () -> {
            Fx.area(p, at, rad, 420 * pw, ice ? ICE : FIRE, e -> {
                if (ice) e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 100, 0));
                else e.igniteForSeconds(10);
                Fx.push(at, e, 2.0, 1.0);
            });
            for (int i = 0; i < 18; i++) {
                double a = i * Math.PI / 9;
                Fx.block(lvl, Fx.ground(lvl, at.add(Math.cos(a) * rad * 0.7, 0, Math.sin(a) * rad * 0.7)), ice ? PACKED : LAVA,
                        1.2f, 2.5f + lvl.random.nextFloat() * 2, 1.2f, 3, 50, (float) Math.sin(a) * 0.5f, (float) -Math.cos(a) * 0.5f);
            }
            Fx.burst(lvl, at.add(0, 1, 0), ParticleTypes.EXPLOSION_EMITTER, 3, 3, 0);
            Fx.burst(lvl, at.add(0, 2, 0), ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, 300, rad / 2, 0.3);
            Fx.shake(lvl, at, 48, 2.5f, 25);
            Fx.sound(lvl, at, SoundEvents.GENERIC_EXPLODE.value(), 3f, ice ? 0.8f : 0.5f);
        });
        Fx.sound(lvl, at, ice ? SoundEvents.ELDER_GUARDIAN_CURSE : SoundEvents.WITHER_SPAWN, 1.5f, 1.5f);
    }

    private static void cone(ServerPlayer p, Vec3 from, Vec3 dir, double len, double cos, float dmg) {
        for (LivingEntity e : Fx.enemies(p, from, len)) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            if (to.length() <= len && to.normalize().dot(dir) > cos) Fx.hit(p, e, dmg, FIRE);
        }
    }

    private static double rnd(ServerLevel lvl, double k) {
        return (lvl.random.nextDouble() * 2 - 1) * k;
    }
}
