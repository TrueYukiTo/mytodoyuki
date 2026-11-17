package dev.yukito.yukicore.gear;

import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Эффекты зелий стихий. */
final class GearEffects {
    private GearEffects() {}

    static class Plain extends MobEffect {
        Plain(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    /** Ледяная кожа: +8 брони, иммунитет к замедлению и заморозке. */
    static class IceSkin extends MobEffect {
        IceSkin() {
            super(MobEffectCategory.BENEFICIAL, 0x9FE0FF);
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 10 == 0;
        }

        @Override
        public boolean applyEffectTick(LivingEntity e, int amplifier) {
            e.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            e.removeEffect(ModMagic.FROZEN);
            e.setTicksFrozen(0);
            return true;
        }
    }
}
