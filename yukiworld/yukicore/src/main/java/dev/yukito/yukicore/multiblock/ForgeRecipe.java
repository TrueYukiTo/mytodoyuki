package dev.yukito.yukicore.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.yukito.yukicore.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/**
 * Рецепт Морозной кузни.
 * <pre>{"type":"yukicore:frost_forging","ingredients":[{"item":"...","count":2},...до 4],
 *  "result":{...},"energy":40000,"time":200}</pre>
 */
public record ForgeRecipe(List<SizedIngredient> ingredients, ItemStack result, int energy, int time)
        implements Recipe<ForgeRecipe.Input> {

    public record Input(List<ItemStack> items) implements RecipeInput {
        @Override
        public ItemStack getItem(int i) {
            return items.get(i);
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    public static final MapCodec<ForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SizedIngredient.FLAT_CODEC.listOf(1, 4).fieldOf("ingredients").forGetter(ForgeRecipe::ingredients),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ForgeRecipe::result),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("energy").forGetter(ForgeRecipe::energy),
            Codec.intRange(1, 72000).optionalFieldOf("time", 200).forGetter(ForgeRecipe::time)
    ).apply(i, ForgeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), ForgeRecipe::ingredients,
            ItemStack.STREAM_CODEC, ForgeRecipe::result,
            ByteBufCodecs.VAR_INT, ForgeRecipe::energy,
            ByteBufCodecs.VAR_INT, ForgeRecipe::time,
            ForgeRecipe::new);

    /** Сколько предметов, подходящих под ингредиент, лежит во входных слотах. */
    private static int available(SizedIngredient ing, List<ItemStack> items) {
        int n = 0;
        for (ItemStack s : items) if (!s.isEmpty() && ing.ingredient().test(s)) n += s.getCount();
        return n;
    }

    @Override
    public boolean matches(Input input, Level level) {
        // во входах не должно быть ничего лишнего — иначе «слиток → пластины» перехватит сплав
        for (ItemStack s : input.items()) {
            if (s.isEmpty()) continue;
            boolean used = false;
            for (SizedIngredient ing : ingredients) used |= ing.ingredient().test(s);
            if (!used) return false;
        }
        for (SizedIngredient ing : ingredients) {
            if (available(ing, input.items()) < ing.count()) return false;
        }
        return true;
    }

    /** Списывает ингредиенты из входных слотов. */
    public void consume(List<ItemStack> items) {
        for (SizedIngredient ing : ingredients) {
            int need = ing.count();
            for (ItemStack s : items) {
                if (need <= 0) break;
                if (!s.isEmpty() && ing.ingredient().test(s)) {
                    int take = Math.min(need, s.getCount());
                    s.shrink(take);
                    need -= take;
                }
            }
        }
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
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
        return ModRecipes.FORGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORGE_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<ForgeRecipe> {
        @Override
        public MapCodec<ForgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
