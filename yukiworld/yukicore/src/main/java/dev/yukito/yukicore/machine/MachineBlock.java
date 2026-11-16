package dev.yukito.yukicore.machine;

import com.mojang.serialization.MapCodec;
import dev.yukito.yukicore.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Блок-машина YukiCore: chrono = Часы Юки (ускоритель), иначе Криогенератор. */
public class MachineBlock extends BaseEntityBlock {
    private final boolean chrono;

    public MachineBlock(Properties p, boolean chrono) {
        super(p);
        this.chrono = chrono;
    }

    public static MachineBlock chrono(Properties p) {
        return new MachineBlock(p, true);
    }

    public static MachineBlock generator(Properties p) {
        return new MachineBlock(p, false);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(chrono ? MachineBlock::chrono : MachineBlock::generator);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return chrono ? new ChronoBlockEntity(pos, state) : new CryoGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return chrono ? createTickerHelper(type, ModBlockEntities.CHRONO.get(), ChronoBlockEntity::serverTick)
                : createTickerHelper(type, ModBlockEntities.CRYO_GENERATOR.get(), CryoGeneratorBlockEntity::serverTick);
    }
}
