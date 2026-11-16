package dev.yukito.yukicore.multiblock;

import com.mojang.serialization.MapCodec;
import dev.yukito.yukicore.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Контроллер мультиструктуры: forge = Морозная кузня, иначе Звёздный реактор. */
public class ControllerBlock extends BaseEntityBlock {
    private final boolean forge;

    public ControllerBlock(Properties p, boolean forge) {
        super(p);
        this.forge = forge;
    }

    public static ControllerBlock forge(Properties p) {
        return new ControllerBlock(p, true);
    }

    public static ControllerBlock reactor(Properties p) {
        return new ControllerBlock(p, false);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(forge ? ControllerBlock::forge : ControllerBlock::reactor);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return forge ? new FrostForgeBlockEntity(pos, state) : new StarReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return forge ? createTickerHelper(type, ModBlockEntities.FROST_FORGE.get(), FrostForgeBlockEntity::serverTick)
                : createTickerHelper(type, ModBlockEntities.STAR_REACTOR.get(), StarReactorBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FrostForgeBlockEntity f) f.interact(player, stack);
            else if (be instanceof StarReactorBlockEntity r) r.interact(player, stack);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FrostForgeBlockEntity f) f.interact(player, ItemStack.EMPTY);
            else if (be instanceof StarReactorBlockEntity r) r.interact(player, ItemStack.EMPTY);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FrostForgeBlockEntity f) {
                for (int i = 0; i < f.items.getSlots(); i++) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), f.items.getStackInSlot(i));
            } else if (be instanceof StarReactorBlockEntity r) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), r.fuelSlot.getStackInSlot(0));
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
