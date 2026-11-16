package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.magic.item.FocusItem;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Сотворение заклинаний на сервере. */
public final class SpellCaster {
    private SpellCaster() {}

    /** Клавиша «Сотворить»: выбранное заклинание. */
    public static void castFromKey(ServerPlayer p, int spellId) {
        MagicData d = Magic.get(p);
        Spell s = spellId >= 0 ? Spell.byId(spellId) : d.selectedSpell();
        if (s == null) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_spell").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        cast(p, s);
    }

    /** ПКМ фокусом: выбранное заклинание, если школа подходит, иначе первое открытое своей школы. */
    public static void castFromItem(ServerPlayer p, FocusItem focus) {
        MagicData d = Magic.get(p);
        Spell s = d.selectedSpell();
        if (s == null || !focus.supports(s.school)) {
            s = null;
            for (Spell c : Spell.values()) {
                if (d.rank(c) > 0 && focus.supports(c.school)) {
                    s = c;
                    break;
                }
            }
        }
        if (s == null) {
            p.displayClientMessage(Component.translatable("magic.yukicore.learn_first").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        cast(p, s);
    }

    public static boolean cast(ServerPlayer p, Spell s) {
        MagicData d = Magic.get(p);
        int rank = d.rank(s);
        if (rank <= 0) return false;
        if (!Magic.hasFocus(p, s.school)) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_focus." + s.school.id).withStyle(ChatFormatting.RED), true);
            return false;
        }
        long now = p.level().getGameTime();
        if (d.cooldownUntil[s.ordinal()] > now) {
            p.displayClientMessage(Component.translatable("magic.yukicore.cooldown",
                    Component.translatable("spell.yukicore." + s.id()),
                    String.format("%.1f", (d.cooldownUntil[s.ordinal()] - now) / 20f)).withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        int cost = Magic.cost(p, s);
        boolean free = p.getAbilities().instabuild;
        if (!free && d.mana < cost) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_mana").withStyle(ChatFormatting.BLUE), true);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.8f);
            return false;
        }
        float pw = Magic.power(p, s);
        if (!perform(p, s, rank, pw)) return false;
        if (!free) d.mana -= cost;
        d.cooldownUntil[s.ordinal()] = now + s.cooldown(rank);
        d.dirty = true;
        Magic.addXp(p, s.school, Math.max(2, cost / 3));
        p.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }

    // ------------------------------------------------------------------ сами заклинания
    private static boolean perform(ServerPlayer p, Spell s, int r, float pw) {
        ServerLevel lvl = p.serverLevel();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        switch (s) {
            case FROST_BOLT -> {
                int n = r >= 3 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    Vec3 dir = look.yRot((float) ((i - (n - 1) / 2.0) * 0.12));
                    MagicProjectile.create(lvl, p, MagicProjectile.Kind.FROST, eye.add(look.scale(0.6)), 12 * pw, 0)
                            .power(r).launch(dir.scale(1.7));
                }
                sound(p, SoundEvents.SNOW_GOLEM_SHOOT, 1.2f);
            }
            case FROST_NOVA -> {
                double rad = 4 + 0.5 * r;
                area(p, p.position(), rad, 14 * pw, Element.ICE, e -> {
                    e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 40 + 10 * r, 0));
                    push(p.position(), e, 0.8);
                });
                ring(lvl, p.position(), rad, ParticleTypes.SNOWFLAKE);
                lvl.sendParticles(ParticleTypes.ITEM_SNOWBALL, p.getX(), p.getY() + 1, p.getZ(), 60, rad / 2, 0.5, rad / 2, 0.1);
                sound(p, SoundEvents.GLASS_BREAK, 0.7f);
            }
            case GLACIAL_ARMOR -> {
                int dur = 20 * (25 + 5 * r);
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, dur, r));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, r >= 4 ? 1 : 0));
                p.addEffect(new MobEffectInstance(ModMagic.FROST_ARMOR, dur, r - 1));
                lvl.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1, 0.6, 0.05);
                sound(p, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8f);
            }
            case FROST_BLINK -> {
                double dist = 8 + 2 * r;
                HitResult hit = lvl.clip(new ClipContext(eye, eye.add(look.scale(dist)), ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, p));
                Vec3 to = hit.getLocation().subtract(look.scale(0.8));
                Vec3 from = p.position();
                ring(lvl, from, 3, ParticleTypes.SNOWFLAKE);
                area(p, from, 3, 8 * pw, Element.ICE, null);
                p.teleportTo(to.x, to.y - p.getEyeHeight() + 0.2, to.z);
                p.resetFallDistance();
                area(p, p.position(), 3, 8 * pw, Element.ICE, null);
                ring(lvl, p.position(), 3, ParticleTypes.SNOWFLAKE);
                sound(p, SoundEvents.ENDERMAN_TELEPORT, 1.6f);
            }
            case BLIZZARD -> {
                Vec3 at = target(p, 24);
                int waves = (5 + r) * 2;
                for (int i = 0; i < waves; i++) {
                    Scheduler.later(lvl, i * 10 + 1, () -> {
                        area(p, at, 5, 6 * pw, Element.ICE, e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2)));
                        lvl.sendParticles(ParticleTypes.SNOWFLAKE, at.x, at.y + 3, at.z, 80, 4, 2, 4, 0.05);
                        lvl.sendParticles(ParticleTypes.ITEM_SNOWBALL, at.x, at.y + 1, at.z, 20, 4, 1, 4, 0.1);
                    });
                }
                sound(p, SoundEvents.POWDER_SNOW_BREAK, 0.6f);
            }
            case ABSOLUTE_ZERO -> {
                area(p, p.position(), 12, 80 * pw, Element.ICE, e -> {
                    e.addEffect(new MobEffectInstance(ModMagic.FROZEN, 160, 0));
                    e.setTicksFrozen(300);
                });
                for (int k = 1; k <= 12; k += 2) ring(lvl, p.position(), k, ParticleTypes.SNOWFLAKE);
                lvl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 2, p.getZ(), 200, 6, 2, 6, 0.1);
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
                sound(p, SoundEvents.WITHER_BREAK_BLOCK, 0.5f);
                sound(p, SoundEvents.GLASS_BREAK, 0.5f);
            }
            case EMBER_BOLT -> {
                int n = r >= 3 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    Scheduler.later(lvl, i * 4 + 1, () -> {
                        Vec3 e2 = p.getEyePosition(), l2 = p.getLookAngle();
                        MagicProjectile.create(lvl, p, MagicProjectile.Kind.FLAME, e2.add(l2.scale(0.6)), 12 * pw, 1.2f)
                                .power(r).launch(l2.scale(1.6));
                        sound(p, SoundEvents.BLAZE_SHOOT, 1.3f);
                    });
                }
            }
            case FLAME_RING -> {
                double rad = 4 + 0.5 * r;
                area(p, p.position(), rad, 14 * pw, Element.FIRE, e -> {
                    e.igniteForSeconds(4 + r);
                    push(p.position(), e, 1.2);
                });
                ring(lvl, p.position(), rad, ParticleTypes.FLAME);
                ring(lvl, p.position(), rad * 0.6, ParticleTypes.FLAME);
                sound(p, SoundEvents.FIRECHARGE_USE, 0.8f);
            }
            case PHOENIX_WINGS -> {
                int dur = 20 * (10 + 2 * r);
                p.addEffect(new MobEffectInstance(ModMagic.PHOENIX, dur, r - 1));
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, r >= 4 ? 1 : 0));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, 1));
                p.push(0, 0.9, 0);
                p.hurtMarked = true;
                lvl.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 120, 1.2, 0.6, 1.2, 0.05);
                sound(p, SoundEvents.ENDER_DRAGON_FLAP, 1.4f);
            }
            case FLAME_DASH -> {
                Vec3 d = look.multiply(1, 0.3, 1).normalize().scale(1.6 + 0.15 * r);
                p.setDeltaMovement(d.x, Math.max(0.2, d.y), d.z);
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12, 2));
                Set<UUID> hit = new HashSet<>();
                for (int i = 0; i < 10; i++) {
                    Scheduler.later(lvl, i + 1, () -> {
                        lvl.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 0.5, p.getZ(), 12, 0.4, 0.4, 0.4, 0.02);
                        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(1.8),
                                e -> e != p && e.isAlive() && MagicProjectile.isEnemy(p, e))) {
                            if (hit.add(e.getUUID())) {
                                Element.hurt(e, src(p), 14 * pw, Element.FIRE);
                                e.igniteForSeconds(4);
                            }
                        }
                    });
                }
                p.resetFallDistance();
                sound(p, SoundEvents.BLAZE_SHOOT, 0.8f);
            }
            case METEOR -> {
                Vec3 at = target(p, 32);
                int n = r >= 5 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    Vec3 spot = at.add(i == 0 ? 0 : (i == 1 ? 3 : -3), 0, i == 0 ? 0 : 2);
                    for (int k = 0; k < 20; k += 4) Scheduler.later(lvl, k + 1, () -> ring(lvl, spot, 3, ParticleTypes.FLAME));
                    Scheduler.later(lvl, 6 + i * 6, () -> MagicProjectile.create(lvl, p, MagicProjectile.Kind.METEOR,
                            spot.add(2, 18, 2), 45 * pw, 3.5f + 0.3f * r).power(r).launch(new Vec3(-0.12, -1.1, -0.12)));
                }
                sound(p, SoundEvents.BLAZE_AMBIENT, 0.5f);
            }
            case SUPERNOVA -> {
                area(p, p.position(), 10, 90 * pw, Element.FIRE, e -> {
                    e.igniteForSeconds(8);
                    push(p.position(), e, 2.0);
                });
                for (int k = 1; k <= 10; k += 2) ring(lvl, p.position(), k, ParticleTypes.FLAME);
                lvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY() + 1, p.getZ(), 2, 1, 0.5, 1, 0);
                lvl.sendParticles(ParticleTypes.LAVA, p.getX(), p.getY() + 1, p.getZ(), 80, 4, 1, 4, 0.2);
                p.addEffect(new MobEffectInstance(ModMagic.REBIRTH, 20 * 60, 0));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0));
                sound(p, SoundEvents.GENERIC_EXPLODE.value(), 0.7f);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ помощники
    static DamageSource src(ServerPlayer p) {
        return p.damageSources().indirectMagic(p, p);
    }

    private static void area(ServerPlayer p, Vec3 c, double r, float dmg, Element el,
                             java.util.function.Consumer<LivingEntity> extra) {
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r),
                e -> e != p && e.isAlive() && MagicProjectile.isEnemy(p, e))) {
            if (e.position().distanceTo(c) > r + e.getBbWidth() / 2) continue;
            Element.hurt(e, src(p), dmg, el);
            if (extra != null) extra.accept(e);
            else el.apply(e, 2);
        }
    }

    private static void push(Vec3 from, LivingEntity e, double k) {
        Vec3 d = e.position().subtract(from).multiply(1, 0, 1).normalize().scale(k);
        e.push(d.x, 0.3, d.z);
        e.hurtMarked = true;
    }

    private static Vec3 target(ServerPlayer p, double dist) {
        Vec3 eye = p.getEyePosition();
        HitResult hit = p.level().clip(new ClipContext(eye, eye.add(p.getLookAngle().scale(dist)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, p));
        return hit.getLocation();
    }

    static void ring(ServerLevel lvl, Vec3 c, double r, ParticleOptions particle) {
        int n = (int) Math.max(16, r * 10);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            lvl.sendParticles(particle, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
        }
    }

    private static void sound(ServerPlayer p, SoundEvent e, float pitch) {
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), e, SoundSource.PLAYERS, 1f, pitch);
    }
}
