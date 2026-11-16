package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.entity.boss.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Боссы YukiWorld: пять хранителей планет и два стража Испытаний стихий. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, YukiCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FrostColossus>> FROST_COLOSSUS = ENTITIES.register(
            "frost_colossus", () -> EntityType.Builder.of(FrostColossus::new, MobCategory.MONSTER)
                    .sized(2.4f, 4.5f).immuneTo(Blocks.POWDER_SNOW).clientTrackingRange(12).build("frost_colossus"));
    public static final DeferredHolder<EntityType<?>, EntityType<BlizzardQueen>> BLIZZARD_QUEEN = ENTITIES.register(
            "blizzard_queen", () -> EntityType.Builder.of(BlizzardQueen::new, MobCategory.MONSTER)
                    .sized(1.4f, 3.8f).immuneTo(Blocks.POWDER_SNOW).clientTrackingRange(12).build("blizzard_queen"));
    public static final DeferredHolder<EntityType<?>, EntityType<AshLord>> ASH_LORD = ENTITIES.register(
            "ash_lord", () -> EntityType.Builder.of(AshLord::new, MobCategory.MONSTER)
                    .sized(2.4f, 4.6f).fireImmune().clientTrackingRange(12).build("ash_lord"));
    public static final DeferredHolder<EntityType<?>, EntityType<VoidWyrm>> VOID_WYRM = ENTITIES.register(
            "void_wyrm", () -> EntityType.Builder.of(VoidWyrm::new, MobCategory.MONSTER)
                    .sized(2.6f, 1.8f).clientTrackingRange(16).build("void_wyrm"));
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowOfYuki>> SHADOW_OF_YUKI = ENTITIES.register(
            "shadow_of_yuki", () -> EntityType.Builder.of(ShadowOfYuki::new, MobCategory.MONSTER)
                    .sized(1.2f, 3.3f).fireImmune().clientTrackingRange(12).build("shadow_of_yuki"));
    public static final DeferredHolder<EntityType<?>, EntityType<GlacialArchon>> GLACIAL_ARCHON = ENTITIES.register(
            "glacial_archon", () -> EntityType.Builder.of(GlacialArchon::new, MobCategory.MONSTER)
                    .sized(1.8f, 3.9f).immuneTo(Blocks.POWDER_SNOW).clientTrackingRange(12).build("glacial_archon"));
    public static final DeferredHolder<EntityType<?>, EntityType<InfernalPhoenix>> INFERNAL_PHOENIX = ENTITIES.register(
            "infernal_phoenix", () -> EntityType.Builder.of(InfernalPhoenix::new, MobCategory.MONSTER)
                    .sized(2.2f, 1.8f).fireImmune().clientTrackingRange(16).build("infernal_phoenix"));

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(ModEntities::attributes);
    }

    private static void attributes(EntityAttributeCreationEvent e) {
        e.put(FROST_COLOSSUS.get(), FrostColossus.createAttributes().build());
        e.put(BLIZZARD_QUEEN.get(), BlizzardQueen.createAttributes().build());
        e.put(ASH_LORD.get(), AshLord.createAttributes().build());
        e.put(VOID_WYRM.get(), VoidWyrm.createAttributes().build());
        e.put(SHADOW_OF_YUKI.get(), ShadowOfYuki.createAttributes().build());
        e.put(GLACIAL_ARCHON.get(), GlacialArchon.createAttributes().build());
        e.put(INFERNAL_PHOENIX.get(), InfernalPhoenix.createAttributes().build());
    }

    private ModEntities() {}
}
