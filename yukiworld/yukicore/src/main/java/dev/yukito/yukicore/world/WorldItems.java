package dev.yukito.yukicore.world;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.item.DescBlockItem;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 7.5: Доска Синтары, Осколок чемпиона (с элиты), Трофей Ночи Пустоты. */
public final class WorldItems {
    private WorldItems() {}

    public static void init() {
        // загрузка класса регистрирует поля
    }

    public static final DeferredBlock<Block> SINTARA_BOARD = ModBlocks.BLOCKS.register("sintara_board",
            () -> new BoardBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().lightLevel(s -> 6)));
    public static final DeferredItem<Item> SINTARA_BOARD_ITEM = ModItems.ITEMS.register("sintara_board",
            () -> new DescBlockItem(SINTARA_BOARD.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> CHAMPION_SHARD = ModItems.ITEMS.register("champion_shard",
            () -> new CoreItem(new Item.Properties().rarity(Rarity.RARE), true));
    public static final DeferredItem<Item> VOID_NIGHT_TROPHY = ModItems.ITEMS.register("void_night_trophy",
            () -> new Trophy(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant()));

    /** Трофей: ПКМ — эпическая (60%) или легендарная (40%) награда сундука удачи и 20 фишек. */
    public static class Trophy extends CoreItem {
        public Trophy(Properties props) {
            super(props, true);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack st = player.getItemInHand(hand);
            if (player instanceof ServerPlayer p) {
                ServerLevel lvl = p.serverLevel();
                String rarity = p.getRandom().nextFloat() < 0.4f ? "legendary" : "epic";
                LootTable table = lvl.getServer().reloadableRegistries().getLootTable(ResourceKey.create(
                        net.minecraft.core.registries.Registries.LOOT_TABLE, YukiCore.id("gameplay/fortune_" + rarity)));
                LootParams params = new LootParams.Builder(lvl).withParameter(LootContextParams.ORIGIN, p.position())
                        .withParameter(LootContextParams.THIS_ENTITY, p).withLuck(p.getLuck()).create(LootContextParamSets.GIFT);
                for (ItemStack drop : table.getRandomItems(params)) if (!p.getInventory().add(drop)) p.drop(drop, false);
                ItemStack chips = new ItemStack(ModItems.SINTARA_CHIP.get(), 20);
                if (!p.getInventory().add(chips)) p.drop(chips, false);
                lvl.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 0.8, 0.6, 0.1);
                lvl.playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
                p.displayClientMessage(Component.translatable("casino.yukicore.box." + rarity).withStyle(ChatFormatting.LIGHT_PURPLE,
                        ChatFormatting.BOLD), true);
                if (!p.getAbilities().instabuild) st.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(st, level.isClientSide);
        }
    }
}
