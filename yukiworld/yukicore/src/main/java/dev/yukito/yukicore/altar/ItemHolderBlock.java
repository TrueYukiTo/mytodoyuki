package dev.yukito.yukicore.altar;

import com.mojang.serialization.MapCodec;
import dev.yukito.yukicore.registry.ModBlockEntities;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Алтарь (altar = true) или пьедестал: ПКМ предметом — положить, пустой рукой — забрать. */
public class ItemHolderBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(4, 3, 4, 12, 11, 12),
            Block.box(2, 11, 2, 14, 14, 14));

    private final boolean altar;

    public ItemHolderBlock(Properties properties, boolean altar) {
        super(properties);
        this.altar = altar;
    }

    public static ItemHolderBlock altar(Properties p) {
        return new ItemHolderBlock(p, true);
    }

    public static ItemHolderBlock pedestal(Properties p) {
        return new ItemHolderBlock(p, false);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(altar ? ItemHolderBlock::altar : ItemHolderBlock::pedestal);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return altar ? new AltarBlockEntity(pos, state) : new PedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!altar || level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.ALTAR.get(), AltarBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ItemHolderBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(ModItems.FROST_WAND.get())) {
            if (altar && be instanceof AltarBlockEntity a) {
                if (!level.isClientSide) a.tryStart(player);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (be.isEmpty() && !(be instanceof AltarBlockEntity a && a.isActive())) {
            if (!level.isClientSide) {
                be.setItem(stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ItemHolderBlockEntity be && !be.isEmpty()) {
            if (be instanceof AltarBlockEntity a && a.isActive()) return InteractionResult.PASS;
            if (!level.isClientSide) {
                ItemStack out = be.getItem();
                be.setItem(ItemStack.EMPTY);
                if (!player.addItem(out)) player.drop(out, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ItemHolderBlockEntity be) {
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), be.getItem());
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
