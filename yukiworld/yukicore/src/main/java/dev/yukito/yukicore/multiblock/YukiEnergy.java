package dev.yukito.yukicore.multiblock;

import net.neoforged.neoforge.energy.EnergyStorage;

/** Буфер FE с внутренними операциями (снаружи — обычные лимиты приёма/отдачи). */
public class YukiEnergy extends EnergyStorage {
    private final Runnable onChange;

    public YukiEnergy(int capacity, int maxReceive, int maxExtract, Runnable onChange) {
        super(capacity, maxReceive, maxExtract);
        this.onChange = onChange;
    }

    public void add(int amount) {
        energy = (int) Math.min(capacity, (long) energy + amount);
    }

    public boolean use(int amount) {
        if (energy < amount) return false;
        energy -= amount;
        return true;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        int r = super.receiveEnergy(amount, simulate);
        if (r > 0 && !simulate) onChange.run();
        return r;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        int r = super.extractEnergy(amount, simulate);
        if (r > 0 && !simulate) onChange.run();
        return r;
    }
}
