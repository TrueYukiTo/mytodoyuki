package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.altar.ItemHolderBlock;
import dev.yukito.yukicore.machine.DecorBlock;
import dev.yukito.yukicore.machine.MachineBlock;
import dev.yukito.yukicore.multiblock.ControllerBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(YukiCore.MOD_ID);

    // --- Руды Энда (только внешние острова) ---
    public static final DeferredBlock<Block> FROST_ORE = BLOCKS.register("frost_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).strength(4.5f, 9f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> YUKI_CRYSTAL_ORE = BLOCKS.register("yuki_crystal_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 8),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).strength(5.5f, 9f)
                            .requiresCorrectToolForDrops().lightLevel(s -> 5)));
    public static final DeferredBlock<Block> STARLIGHT_ORE = BLOCKS.register("starlight_ore",
            () -> new DropExperienceBlock(UniformInt.of(8, 14),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).strength(8f, 1200f)
                            .requiresCorrectToolForDrops().lightLevel(s -> 9)));

    // --- Блоки материалов ---
    public static final DeferredBlock<Block> FROST_BLOCK = BLOCKS.register("frost_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)));
    public static final DeferredBlock<Block> YUKI_CRYSTAL_BLOCK = BLOCKS.register("yuki_crystal_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).lightLevel(s -> 7)));
    public static final DeferredBlock<Block> STARLIGHT_BLOCK = BLOCKS.register("starlight_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).strength(6f, 1200f).lightLevel(s -> 12)));
    public static final DeferredBlock<Block> AURORIUM_BLOCK = BLOCKS.register("aurorium_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).lightLevel(s -> 4)));
    public static final DeferredBlock<Block> CELESTIUM_BLOCK = BLOCKS.register("celestium_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).lightLevel(s -> 10)));
    public static final DeferredBlock<Block> YUKI_LAMP = BLOCKS.register("yuki_lamp",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).lightLevel(s -> 15)));

    // --- Мультиструктура «Алтарь Юки» ---
    public static final DeferredBlock<Block> YUKI_ALTAR = BLOCKS.register("yuki_altar",
            () -> ItemHolderBlock.altar(BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).strength(5f, 1200f)
                    .lightLevel(s -> 10).noOcclusion()));
    public static final DeferredBlock<Block> YUKI_PEDESTAL = BLOCKS.register("yuki_pedestal",
            () -> ItemHolderBlock.pedestal(BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).strength(4f, 1200f)
                    .lightLevel(s -> 6).noOcclusion()));

    // --- Руды планет Системы Юки ---
    public static final DeferredBlock<Block> PERMAFROST_ORE = planetOre("permafrost_ore", Blocks.PACKED_ICE, 4);
    public static final DeferredBlock<Block> AURORA_ORE = planetOre("aurora_ore", Blocks.AMETHYST_BLOCK, 8);
    public static final DeferredBlock<Block> PYROLITE_ORE = planetOre("pyrolite_ore", Blocks.BLACKSTONE, 6);
    public static final DeferredBlock<Block> VOID_ORE = planetOre("void_ore", Blocks.DEEPSLATE, 3);
    public static final DeferredBlock<Block> STELLAR_ORE = planetOre("stellar_ore", Blocks.CALCITE, 12);

    // --- Блоки планетарных материалов ---
    public static final DeferredBlock<Block> PERMAFROST_BLOCK = storage("permafrost_block", 6);
    public static final DeferredBlock<Block> AURORA_BLOCK = storage("aurora_block", 10);
    public static final DeferredBlock<Block> PYROLITE_BLOCK = storage("pyrolite_block", 8);
    public static final DeferredBlock<Block> VOID_BLOCK = storage("void_block", 2);
    public static final DeferredBlock<Block> STELLAR_BLOCK = storage("stellar_block", 15);

    // --- Машины ---
    public static final DeferredBlock<Block> YUKI_CHRONO = BLOCKS.register("yuki_chrono",
            () -> MachineBlock.chrono(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).lightLevel(s -> 9)));
    public static final DeferredBlock<Block> CRYO_GENERATOR = BLOCKS.register("cryo_generator",
            () -> MachineBlock.generator(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).lightLevel(s -> 5)));

    // --- Мультиструктуры 5.0 ---
    public static final DeferredBlock<Block> FROST_CASING = BLOCKS.register("frost_casing",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(6f, 1200f)));
    public static final DeferredBlock<Block> REACTOR_CASING = BLOCKS.register("reactor_casing",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).lightLevel(s -> 4)));
    public static final DeferredBlock<Block> CRYOSTEEL_BLOCK = BLOCKS.register("cryosteel_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)));
    public static final DeferredBlock<Block> FROST_FORGE = BLOCKS.register("frost_forge",
            () -> ControllerBlock.forge(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(6f, 1200f).lightLevel(s -> 8)));
    public static final DeferredBlock<Block> STAR_REACTOR = BLOCKS.register("star_reactor",
            () -> ControllerBlock.reactor(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).lightLevel(s -> 12)));
    public static final DeferredBlock<Block> AURORA_LAMP = BLOCKS.register("aurora_lamp",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).lightLevel(s -> 15)));

    // --- Новогодние украшения ---
    public static final DeferredBlock<Block> FESTIVE_GARLAND = BLOCKS.register("festive_garland",
            () -> new DecorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CARPET).lightLevel(s -> 12)
                    .noOcclusion(), Block.box(0, 0, 0, 16, 1, 16)));
    public static final DeferredBlock<Block> CHRISTMAS_ORNAMENT = BLOCKS.register("christmas_ornament",
            () -> new DecorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).lightLevel(s -> 9)
                    .noOcclusion(), Block.box(4, 2, 4, 12, 10, 12)));
    public static final DeferredBlock<Block> YUKI_TREE = BLOCKS.register("yuki_tree",
            () -> new DecorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES).lightLevel(s -> 10)
                    .noOcclusion().noCollission(), Block.box(2, 0, 2, 14, 16, 14)));
    public static final DeferredBlock<Block> SLOT_MACHINE = BLOCKS.register("slot_machine",
            () -> new dev.yukito.yukicore.casino.SlotMachineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .lightLevel(st -> 10).noOcclusion()));
    public static final DeferredBlock<Block> SNOW_GLOBE = BLOCKS.register("snow_globe",
            () -> new DecorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).lightLevel(s -> 6)
                    .noOcclusion(), Block.box(3, 0, 3, 13, 11, 13)));

    private static DeferredBlock<Block> planetOre(String name, Block base, int light) {
        return BLOCKS.register(name, () -> new DropExperienceBlock(UniformInt.of(6, 12),
                BlockBehaviour.Properties.ofFullCopy(base).strength(6f, 1200f).requiresCorrectToolForDrops()
                        .lightLevel(s -> light)));
    }

    private static DeferredBlock<Block> storage(String name, int light) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
                .lightLevel(s -> light)));
    }

    private ModBlocks() {}
}
