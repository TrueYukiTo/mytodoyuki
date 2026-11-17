package dev.yukito.yukicore.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


/** Доска Синтары: ПКМ — задания дня и Лавка за фишки. */
public class BoardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BoardBlock> CODEC = simpleCodec(BoardBlock::new);
    private static final VoxelShape[] SHAPES = {Block.box(0, 0, 12, 16, 16, 16), Block.box(0, 0, 0, 4, 16, 16),
            Block.box(0, 0, 0, 16, 16, 4), Block.box(12, 0, 0, 16, 16, 16)};

    public BoardBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return switch (s.getValue(FACING)) {
            case NORTH -> SHAPES[0];
            case EAST -> SHAPES[1];
            case SOUTH -> SHAPES[2];
            default -> SHAPES[3];
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) dev.yukito.yukicore.client.BoardScreen.open(pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
