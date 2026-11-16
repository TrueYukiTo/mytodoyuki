package dev.yukito.yukicore.magic;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Стихии: лёд Юки, пламя Синтары, пустота и звёзды (у боссов). */
public enum Element {
    NONE, ICE, FIRE, VOID, STAR;

    private static final ThreadLocal<Element> CURRENT = new ThreadLocal<>();

    /** Нанести урон с пометкой стихии: боссы учитывают слабости и сопротивления. */
    public static boolean hurt(LivingEntity target, DamageSource src, float amount, Element element) {
        Element prev = CURRENT.get();
        CURRENT.set(element);
        try {
            boolean ok = target.hurt(src, amount);
            if (ok && src.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp && sp != target) {
                MagicEvents.onSpellDamage(sp, target, element, amount);
            }
            return ok;
        } finally {
            CURRENT.set(prev);
        }
    }

    public static Element of(DamageSource src) {
        Element e = CURRENT.get();
        if (e != null) return e;
        if (src.is(DamageTypeTags.IS_FIRE)) return FIRE;
        if (src.is(DamageTypeTags.IS_FREEZING)) return ICE;
        return NONE;
    }

    /** Побочный эффект стихии на цели. */
    public void apply(LivingEntity e, int power) {
        switch (this) {
            case ICE -> {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + power * 10, Math.min(3, 1 + power / 3)));
                e.setTicksFrozen(Math.min(300, e.getTicksFrozen() + 60 + power * 10));
            }
            case FIRE -> e.igniteForSeconds(3 + power);
            case VOID -> e.addEffect(new MobEffectInstance(MobEffects.WITHER, 60 + power * 10, 1));
            case STAR -> e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            default -> {
            }
        }
    }

    public ParticleOptions particle() {
        return switch (this) {
            case ICE -> ParticleTypes.SNOWFLAKE;
            case FIRE -> ParticleTypes.FLAME;
            case VOID -> ParticleTypes.REVERSE_PORTAL;
            case STAR -> ParticleTypes.END_ROD;
            default -> ParticleTypes.CRIT;
        };
    }

    public ParticleOptions burst() {
        return switch (this) {
            case ICE -> ParticleTypes.ITEM_SNOWBALL;
            case FIRE -> ParticleTypes.LAVA;
            case VOID -> ParticleTypes.DRAGON_BREATH;
            case STAR -> ParticleTypes.FIREWORK;
            default -> ParticleTypes.POOF;
        };
    }
}
