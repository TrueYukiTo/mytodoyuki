package dev.yukito.yukicore.multiblock;

import dev.yukito.yukicore.registry.ModBlockEntities;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Морозная кузня — контроллер мультиструктуры 3×3×3 (корпус из морозной обшивки, ядро — синий лёд,
 * верхние углы — лампы Юки). 4 входных слота, 1 выходной, буфер 2 000 000 FE.
 * Работает с трубами/воронками (ItemHandler) и кабелями (EnergyStorage).
 */
public class FrostForgeBlockEntity extends BlockEntity {
    public static final CubeStructure STRUCTURE =
            new CubeStructure(ModBlocks.FROST_CASING.get(), Blocks.BLUE_ICE, ModBlocks.YUKI_LAMP.get());
    public static final int INPUTS = 4;

    public final ItemStackHandler items = new ItemStackHandler(INPUTS + 1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public final YukiEnergy energy = new YukiEnergy(2_000_000, 100_000, 0, this::setChanged);
    /** Для автоматизации: входы только принимают, выход только отдаёт. */
    public final IItemHandler automation = new IItemHandler() {
        public int getSlots() { return INPUTS + 1; }
        public ItemStack getStackInSlot(int s) { return items.getStackInSlot(s); }
        public ItemStack insertItem(int s, ItemStack st, boolean sim) { return s < INPUTS ? items.insertItem(s, st, sim) : st; }
        public ItemStack extractItem(int s, int n, boolean sim) { return s == INPUTS ? items.extractItem(s, n, sim) : ItemStack.EMPTY; }
        public int getSlotLimit(int s) { return 64; }
        public boolean isItemValid(int s, ItemStack st) { return s < INPUTS; }
    };

    private int progress;
    private boolean formed;

    public FrostForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FROST_FORGE.get(), pos, state);
    }

    private List<ItemStack> inputs() {
        List<ItemStack> l = new ArrayList<>();
        for (int i = 0; i < INPUTS; i++) l.add(items.getStackInSlot(i));
        return l;
    }

    private Optional<RecipeHolder<ForgeRecipe>> recipe() {
        return level.getRecipeManager().getRecipeFor(ModRecipes.FORGE_TYPE.get(), new ForgeRecipe.Input(inputs()), level);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FrostForgeBlockEntity be) {
        if (level.getGameTime() % 40 == 0) be.formed = STRUCTURE.find(level, pos) != null;
        if (!be.formed) {
            be.progress = 0;
            return;
        }
        Optional<RecipeHolder<ForgeRecipe>> r = be.recipe();
        if (r.isEmpty()) {
            be.progress = 0;
            return;
        }
        ForgeRecipe rec = r.get().value();
        ItemStack out = be.items.getStackInSlot(INPUTS);
        ItemStack result = rec.result();
        if (!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out, result)
                || out.getCount() + result.getCount() > out.getMaxStackSize())) return;
        int perTick = Math.max(1, rec.energy() / rec.time());
        if (!be.energy.use(perTick)) return;
        be.progress++;
        if (level instanceof ServerLevel s && be.progress % 8 == 0) {
            s.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4, 0.3, 0.1, 0.3, 0.02);
        }
        if (be.progress >= rec.time()) {
            List<ItemStack> in = be.inputs();
            rec.consume(in);
            if (out.isEmpty()) be.items.setStackInSlot(INPUTS, result.copy());
            else out.grow(result.getCount());
            be.progress = 0;
            be.setChanged();
            if (level instanceof ServerLevel s) {
                s.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    /** ПКМ предметом — положить во вход, пустой рукой — забрать результат, Shift — состояние. */
    public void interact(Player player, ItemStack held) {
        if (player.isShiftKeyDown()) {
            Direction d = STRUCTURE.find(level, worldPosition);
            String rec = recipe().map(r -> r.value().result().getHoverName().getString()).orElse("—");
            player.displayClientMessage(Component.translatable("message.yukicore.forge.status",
                    d != null ? "✔" : "✘", energy.getEnergyStored(), rec, progress)
                    .withStyle(d != null ? ChatFormatting.AQUA : ChatFormatting.RED), false);
            return;
        }
        if (!held.isEmpty()) {
            ItemStack rest = held.copy();
            for (int i = 0; i < INPUTS && !rest.isEmpty(); i++) rest = items.insertItem(i, rest, false);
            held.setCount(rest.getCount());
            return;
        }
        ItemStack out = items.extractItem(INPUTS, 64, false);
        if (out.isEmpty()) {
            for (int i = INPUTS - 1; i >= 0 && out.isEmpty(); i--) out = items.extractItem(i, 64, false);
        }
        if (!out.isEmpty() && !player.addItem(out)) player.drop(out, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("items", items.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("items")) items.deserializeNBT(registries, tag.getCompound("items"));
        if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
        progress = tag.getInt("progress");
    }
}
