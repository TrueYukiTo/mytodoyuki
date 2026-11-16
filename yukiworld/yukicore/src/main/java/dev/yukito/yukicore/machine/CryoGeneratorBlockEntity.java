package dev.yukito.yukicore.machine;

import dev.yukito.yukicore.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Криогенератор: вырабатывает FE от льда и снега вокруг (6 соседних блоков) и сам раздаёт энергию соседям.
 * Снег 5, лёд 10, плотный лёд 20, синий лёд 40 FE/т на каждую сторону — до 240 FE/т.
 */
public class CryoGeneratorBlockEntity extends BlockEntity {
    public final Buffer energy = new Buffer();

    /** Буфер: снаружи можно только забирать энергию, генерация — изнутри. */
    public static final class Buffer extends EnergyStorage {
        Buffer() {
            super(200_000, 0, 2_000);
        }

        void generate(int amount) {
            energy = Math.min(capacity, energy + amount);
        }
    }

    public CryoGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYO_GENERATOR.get(), pos, state);
    }

    public static int rate(BlockState s) {
        if (s.is(Blocks.BLUE_ICE)) return 40;
        if (s.is(Blocks.PACKED_ICE)) return 20;
        if (s.is(Blocks.ICE)) return 10;
        if (s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.POWDER_SNOW)) return 5;
        return 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CryoGeneratorBlockEntity be) {
        int gen = 0;
        for (Direction d : Direction.values()) gen += rate(level.getBlockState(pos.relative(d)));
        if (gen > 0) be.energy.generate(gen);
        if (be.energy.getEnergyStored() > 0) {
            for (Direction d : Direction.values()) {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(d), d.getOpposite());
                if (target == null || !target.canReceive()) continue;
                int sent = target.receiveEnergy(be.energy.extractEnergy(2_000, true), false);
                be.energy.extractEnergy(sent, false);
                if (be.energy.getEnergyStored() <= 0) break;
            }
        }
        if (level.getGameTime() % 40 == 0) be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energy.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
    }
}
