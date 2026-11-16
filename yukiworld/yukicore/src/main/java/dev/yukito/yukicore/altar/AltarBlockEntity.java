package dev.yukito.yukicore.altar;

import dev.yukito.yukicore.registry.ModBlockEntities;
import dev.yukito.yukicore.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Центр Алтаря Юки: проверяет структуру, ищет рецепт и проводит ритуал. */
public class AltarBlockEntity extends ItemHolderBlockEntity {
    private ResourceLocation activeRecipe;
    private int progress;
    private int duration;
    private int tier;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR.get(), pos, state);
    }

    public boolean isActive() {
        return activeRecipe != null;
    }

    /** Нажатие Морозным жезлом. */
    public void tryStart(Player player) {
        if (level == null || level.isClientSide || isActive()) return;
        int t = AltarStructure.detectTier(level, worldPosition);
        if (t == 0) {
            player.displayClientMessage(Component.translatable("message.yukicore.altar.no_structure")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        Optional<RecipeHolder<AltarRecipe>> recipe = findRecipe(t);
        if (recipe.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.yukicore.altar.no_recipe", t)
                    .withStyle(ChatFormatting.GOLD), true);
            return;
        }
        activeRecipe = recipe.get().id();
        duration = recipe.get().value().duration();
        tier = t;
        progress = 0;
        setChanged();
        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.3f);
        player.displayClientMessage(Component.translatable("message.yukicore.altar.started")
                .withStyle(ChatFormatting.AQUA), true);
    }

    private AltarInput input(int t) {
        List<ItemStack> peds = new ArrayList<>();
        for (BlockPos p : AltarStructure.pedestals(worldPosition, t)) {
            if (level.getBlockEntity(p) instanceof PedestalBlockEntity be) peds.add(be.getItem());
        }
        return new AltarInput(getItem(), peds, t);
    }

    private Optional<RecipeHolder<AltarRecipe>> findRecipe(int t) {
        return level.getRecipeManager().getRecipeFor(ModRecipes.ALTAR_TYPE.get(), input(t), level);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity be) {
        if (!be.isActive() || !(level instanceof ServerLevel server)) return;
        be.progress++;

        // искры от пьедесталов к центру
        if (be.progress % 4 == 0) {
            Vec3 c = Vec3.atCenterOf(pos).add(0, 0.9, 0);
            for (BlockPos p : AltarStructure.pedestals(pos, be.tier)) {
                Vec3 from = Vec3.atCenterOf(p).add(0, 0.9, 0);
                double f = (be.progress % 20) / 20.0;
                Vec3 at = from.lerp(c, f);
                server.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.SNOWFLAKE, from.x, from.y, from.z, 1, 0.1, 0.1, 0.1, 0.01);
            }
            server.sendParticles(ParticleTypes.ENCHANT, c.x, c.y + 0.5, c.z, 6, 0.4, 0.4, 0.4, 0.5);
        }

        // каждую секунду убеждаемся, что ничего не убрали
        if (be.progress % 20 == 0) {
            Optional<RecipeHolder<AltarRecipe>> r = be.findRecipe(be.tier);
            if (r.isEmpty() || !r.get().id().equals(be.activeRecipe)
                    || AltarStructure.detectTier(level, pos) < r.get().value().tier()) {
                be.cancel();
                return;
            }
        }

        if (be.progress >= be.duration) {
            Optional<RecipeHolder<AltarRecipe>> r = be.findRecipe(be.tier);
            if (r.isPresent() && r.get().id().equals(be.activeRecipe)) {
                ItemStack out = r.get().value().assemble(be.input(be.tier), level.registryAccess());
                for (BlockPos p : AltarStructure.pedestals(pos, be.tier)) {
                    if (level.getBlockEntity(p) instanceof PedestalBlockEntity ped && !ped.isEmpty()) {
                        ped.setItem(ItemStack.EMPTY);
                    }
                }
                be.setItem(out);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
                server.sendParticles(ParticleTypes.FLASH, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5f, 1f);
            }
            be.activeRecipe = null;
            be.progress = 0;
            be.setChanged();
        }
    }

    private void cancel() {
        activeRecipe = null;
        progress = 0;
        setChanged();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1f, 1f);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (activeRecipe != null) {
            tag.putString("recipe", activeRecipe.toString());
            tag.putInt("progress", progress);
            tag.putInt("duration", duration);
            tag.putInt("tier", tier);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        activeRecipe = tag.contains("recipe") ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        progress = tag.getInt("progress");
        duration = tag.getInt("duration");
        tier = tag.getInt("tier");
    }
}
