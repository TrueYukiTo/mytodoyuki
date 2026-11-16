package dev.yukito.yukicore.multiblock;

import dev.yukito.yukicore.registry.ModBlockEntities;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Звёздный реактор — контроллер мультиструктуры 3×3×3 (обшивка реактора, ядро — блок звёздного света,
 * верхние углы — целестий). Сжигает топливо из осколков и выдаёт огромную энергию соседям.
 */
public class StarReactorBlockEntity extends BlockEntity {
    public static final CubeStructure STRUCTURE =
            new CubeStructure(ModBlocks.REACTOR_CASING.get(), ModBlocks.STARLIGHT_BLOCK.get(), ModBlocks.CELESTIUM_BLOCK.get());

    /** Топливо: тиков горения и FE за тик. */
    public static int[] fuel(ItemStack s) {
        Item i = s.getItem();
        if (i == ModItems.YUKI_CRYSTAL.get()) return new int[]{400, 400};
        if (i == ModItems.STARLIGHT_SHARD.get()) return new int[]{2400, 2500};
        if (i == ModItems.PYROLITE_INGOT.get()) return new int[]{3000, 3000};
        if (i == ModItems.VOID_SHARD.get()) return new int[]{4000, 5000};
        if (i == ModItems.STELLAR_SHARD.get()) return new int[]{8000, 10000};
        return null;
    }

    public final ItemStackHandler fuelSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return fuel(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public final YukiEnergy energy = new YukiEnergy(20_000_000, 0, 200_000, this::setChanged);

    private int burn, burnRate;
    private boolean formed;

    public StarReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STAR_REACTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StarReactorBlockEntity be) {
        if (level.getGameTime() % 40 == 0) be.formed = STRUCTURE.find(level, pos) != null;
        if (!be.formed) return;
        if (be.burn <= 0 && be.energy.getEnergyStored() < be.energy.getMaxEnergyStored()) {
            ItemStack f = be.fuelSlot.getStackInSlot(0);
            int[] v = f.isEmpty() ? null : fuel(f);
            if (v != null) {
                be.burn = v[0];
                be.burnRate = v[1];
                be.fuelSlot.extractItem(0, 1, false);
            }
        }
        if (be.burn > 0) {
            be.burn--;
            be.energy.add(be.burnRate);
            if (level instanceof ServerLevel s && level.getGameTime() % 10 == 0) {
                s.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 3, 0.3, 0.2, 0.3, 0.03);
            }
        }
        // раздача энергии во все стороны
        for (Direction d : Direction.values()) {
            if (be.energy.getEnergyStored() <= 0) break;
            IEnergyStorage t = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(d), d.getOpposite());
            if (t == null || !t.canReceive()) continue;
            int sent = t.receiveEnergy(Math.min(200_000, be.energy.getEnergyStored()), false);
            be.energy.use(sent);
        }
        if (level.getGameTime() % 40 == 0) be.setChanged();
    }

    public void interact(Player player, ItemStack held) {
        if (player.isShiftKeyDown() || held.isEmpty()) {
            boolean ok = STRUCTURE.find(level, worldPosition) != null;
            player.displayClientMessage(Component.translatable("message.yukicore.reactor.status",
                    ok ? "✔" : "✘", energy.getEnergyStored(), burn > 0 ? burnRate : 0,
                    fuelSlot.getStackInSlot(0).getCount()).withStyle(ok ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED), false);
            return;
        }
        ItemStack rest = fuelSlot.insertItem(0, held.copy(), false);
        held.setCount(rest.getCount());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fuel", fuelSlot.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putInt("burn", burn);
        tag.putInt("rate", burnRate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("fuel")) fuelSlot.deserializeNBT(registries, tag.getCompound("fuel"));
        if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
        burn = tag.getInt("burn");
        burnRate = tag.getInt("rate");
    }
}
