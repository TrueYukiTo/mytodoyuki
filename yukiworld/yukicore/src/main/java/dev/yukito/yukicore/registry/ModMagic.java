package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.MagicProjectile;
import dev.yukito.yukicore.magic.net.MagicNet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Регистрация магии: данные игрока, эффекты, снаряд, сетевые пакеты. */
public final class ModMagic {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, YukiCore.MOD_ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, YukiCore.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, YukiCore.MOD_ID);

    public static final Supplier<AttachmentType<MagicData>> DATA = ATTACHMENTS.register("magic",
            () -> AttachmentType.serializable(MagicData::new).copyOnDeath().build());

    public static final DeferredHolder<MobEffect, MobEffect> FROZEN = EFFECTS.register("frozen",
            () -> new Effect(MobEffectCategory.HARMFUL, 0x9FE0FF)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, YukiCore.id("frozen"), -0.9,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH, YukiCore.id("frozen_jump"), -0.8,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<MobEffect, MobEffect> FROST_ARMOR = EFFECTS.register("frost_armor",
            () -> new Effect(MobEffectCategory.BENEFICIAL, 0x8FD8FF));
    public static final DeferredHolder<MobEffect, MobEffect> PHOENIX = EFFECTS.register("phoenix",
            () -> new Effect(MobEffectCategory.BENEFICIAL, 0xFF8A2A));
    public static final DeferredHolder<MobEffect, MobEffect> REBIRTH = EFFECTS.register("rebirth",
            () -> new Effect(MobEffectCategory.BENEFICIAL, 0xFFD060));

    public static final DeferredHolder<EntityType<?>, EntityType<MagicProjectile>> PROJECTILE = ENTITIES.register(
            "magic_projectile", () -> EntityType.Builder.<MagicProjectile>of(MagicProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(8).updateInterval(4).build("magic_projectile"));

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        EFFECTS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(MagicNet::register);
    }

    static class Effect extends MobEffect {
        Effect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    private ModMagic() {}
}
