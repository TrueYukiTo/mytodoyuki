package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
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

    private ModBlocks() {}
}
