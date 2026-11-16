package dev.yukito.yukicore.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.yukito.yukicore.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Ритуал Алтаря Юки.
 * <pre>{"type":"yukicore:altar","tier":2,"center":{...},"pedestals":[...до 8...],"result":{...},"duration":200}</pre>
 */
public record AltarRecipe(int tier, Ingredient center, List<Ingredient> pedestals, ItemStack result, int duration)
        implements Recipe<AltarInput> {

    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(1, 3).optionalFieldOf("tier", 1).forGetter(AltarRecipe::tier),
            Ingredient.CODEC_NONEMPTY.fieldOf("center").forGetter(AltarRecipe::center),
            Ingredient.CODEC_NONEMPTY.listOf(0, 8).fieldOf("pedestals").forGetter(AltarRecipe::pedestals),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AltarRecipe::result),
            Codec.intRange(20, 2400).optionalFieldOf("duration", 100).forGetter(AltarRecipe::duration)
    ).apply(i, AltarRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AltarRecipe::tier,
            Ingredient.CONTENTS_STREAM_CODEC, AltarRecipe::center,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::pedestals,
            ItemStack.STREAM_CODEC, AltarRecipe::result,
            ByteBufCodecs.VAR_INT, AltarRecipe::duration,
            AltarRecipe::new);

    @Override
    public boolean matches(AltarInput input, Level level) {
        if (input.tier() < tier || !center.test(input.center())) return false;
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack s : input.pedestals()) if (!s.isEmpty()) items.add(s);
        if (items.size() != pedestals.size()) return false;
        return assign(0, items, new boolean[items.size()]);
    }

    /** Подбор без учёта порядка (пьедесталов не больше 8 — перебор мгновенный). */
    private boolean assign(int idx, List<ItemStack> items, boolean[] used) {
        if (idx == pedestals.size()) return true;
        for (int i = 0; i < items.size(); i++) {
            if (!used[i] && pedestals.get(idx).test(items.get(i))) {
                used[i] = true;
                if (assign(idx + 1, items, used)) return true;
                used[i] = false;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(AltarInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALTAR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALTAR_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<AltarRecipe> {
        @Override
        public MapCodec<AltarRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
