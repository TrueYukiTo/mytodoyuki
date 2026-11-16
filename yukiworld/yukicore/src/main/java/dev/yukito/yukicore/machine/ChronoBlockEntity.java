package dev.yukito.yukicore.machine;

import dev.yukito.yukicore.registry.ModBlockEntities;
import dev.yukito.yukicore.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Часы Юки — ускоритель времени (как Torcherino в ATM).
 * Каждый тик даёт блокам-машинам в кубе 3×3×3 ещё {@link #EXTRA} тиков, а растениям — шанс случайного тика.
 * Работает всегда; сигнал редстоуна выключает часы.
 */
public class ChronoBlockEntity extends BlockEntity {
    public static final int EXTRA = 3;

    public ChronoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHRONO.get(), pos, state);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void serverTick(Level level, BlockPos pos, BlockState state, ChronoBlockEntity be) {
        if (!(level instanceof ServerLevel server) || level.hasNeighborSignal(pos)) return;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (p.equals(pos)) continue;
            BlockState st = level.getBlockState(p);
            if (st.isAir() || st.is(ModBlocks.YUKI_CHRONO.get())) continue;
            BlockEntity target = level.getBlockEntity(p);
            if (target != null && !target.isRemoved()) {
                BlockEntityTicker ticker = st.getTicker(level, (BlockEntityType) target.getType());
                if (ticker != null) {
                    BlockPos imm = p.immutable();
                    for (int i = 0; i < EXTRA; i++) ticker.tick(level, imm, st, target);
                }
            } else if (st.isRandomlyTicking() && server.random.nextInt(12) == 0) {
                st.randomTick(server, p.immutable(), server.random);
            }
        }
        if (level.getGameTime() % 20 == 0) {
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.3, 0.1, 0.3, 0.01);
        }
    }
}
