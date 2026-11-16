package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.altar.AltarBlockEntity;
import dev.yukito.yukicore.altar.PedestalBlockEntity;
import dev.yukito.yukicore.machine.ChronoBlockEntity;
import dev.yukito.yukicore.machine.CryoGeneratorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, YukiCore.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR =
            BLOCK_ENTITIES.register("altar", () -> BlockEntityType.Builder.of(AltarBlockEntity::new,
                    ModBlocks.YUKI_ALTAR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL =
            BLOCK_ENTITIES.register("pedestal", () -> BlockEntityType.Builder.of(PedestalBlockEntity::new,
                    ModBlocks.YUKI_PEDESTAL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChronoBlockEntity>> CHRONO =
            BLOCK_ENTITIES.register("chrono", () -> BlockEntityType.Builder.of(ChronoBlockEntity::new,
                    ModBlocks.YUKI_CHRONO.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CryoGeneratorBlockEntity>> CRYO_GENERATOR =
            BLOCK_ENTITIES.register("cryo_generator", () -> BlockEntityType.Builder.of(CryoGeneratorBlockEntity::new,
                    ModBlocks.CRYO_GENERATOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<dev.yukito.yukicore.multiblock.FrostForgeBlockEntity>> FROST_FORGE =
            BLOCK_ENTITIES.register("frost_forge", () -> BlockEntityType.Builder.of(
                    dev.yukito.yukicore.multiblock.FrostForgeBlockEntity::new, ModBlocks.FROST_FORGE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<dev.yukito.yukicore.multiblock.StarReactorBlockEntity>> STAR_REACTOR =
            BLOCK_ENTITIES.register("star_reactor", () -> BlockEntityType.Builder.of(
                    dev.yukito.yukicore.multiblock.StarReactorBlockEntity::new, ModBlocks.STAR_REACTOR.get()).build(null));

    /** Энергия криогенератора доступна трубам и кабелям других модов. */
    public static void capabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                CRYO_GENERATOR.get(), (be, side) -> be.energy);
        e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                FROST_FORGE.get(), (be, side) -> be.energy);
        e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                FROST_FORGE.get(), (be, side) -> be.automation);
        e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                STAR_REACTOR.get(), (be, side) -> be.energy);
        e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                STAR_REACTOR.get(), (be, side) -> be.fuelSlot);
    }

    private ModBlockEntities() {}
}
