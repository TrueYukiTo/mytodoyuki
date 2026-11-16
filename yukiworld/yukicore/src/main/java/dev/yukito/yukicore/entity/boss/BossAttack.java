package dev.yukito.yukicore.entity.boss;

import net.minecraft.world.entity.LivingEntity;

/** Атака босса: анимация, длительность, откат, фаза открытия, дистанция и вес выбора. */
public record BossAttack(String name, int anim, int duration, int cooldown, int minPhase, float minRange,
                         float maxRange, int weight, Body body) {
    @FunctionalInterface
    public interface Body {
        /** Вызывается каждый тик атаки; k — номер тика от 0 до duration-1. */
        void tick(LivingEntity target, int k);
    }
}
