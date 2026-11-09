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

    public static final DeferredBlock<Block> YUKI_CRYSTAL_ORE = BLOCKS.register("yuki_crystal_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE).lightLevel(s -> 4)));

    public static final DeferredBlock<Block> DEEPSLATE_YUKI_CRYSTAL_ORE = BLOCKS.register("deepslate_yuki_crystal_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE).lightLevel(s -> 4)));

    public static final DeferredBlock<Block> FROST_ORE = BLOCKS.register("frost_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 2),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));

    public static final DeferredBlock<Block> FROST_BLOCK = BLOCKS.register("frost_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> YUKI_CRYSTAL_BLOCK = BLOCKS.register("yuki_crystal_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).lightLevel(s -> 7)));

    public static final DeferredBlock<Block> YUKI_LAMP = BLOCKS.register("yuki_lamp",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).lightLevel(s -> 15)));

    private ModBlocks() {}
}
