package dev.yukito.yukicore.world;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.casino.Casino;
import dev.yukito.yukicore.gear.GearItems;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Доска Синтары: 3 ежедневных задания на игрока (сброс в полночь по реальному времени) и Лавка за фишки.
 * Все расчёты на сервере; экран получает состояние списком чисел.
 */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class Contracts {
    private Contracts() {}

    public enum Type {
        MONSTERS(20, 40, 18), ELITES(1, 3, 30), ORES(16, 48, 18), SPELLS(30, 60, 20), FISH(3, 8, 16);

        final int min, max, reward;

        Type(int min, int max, int reward) {
            this.min = min;
            this.max = max;
            this.reward = reward;
        }
    }

    public static final int COUNT = 3, REROLL_COST = 5, ALL_BONUS = 30;
    private static final String KEY = "YukiContracts";
    private static final TagKey<Block> ORES = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "ores"));

    // ------------------------------------------------------------------ лавка
    public record Offer(Supplier<Item> item, int count, int price) {}

    public static final List<Offer> SHOP = List.of(
            new Offer(() -> ModItems.MANA_POTION.get(), 2, 8),
            new Offer(() -> Items.GOLDEN_APPLE, 1, 20),
            new Offer(() -> Items.EXPERIENCE_BOTTLE, 8, 20),
            new Offer(() -> Items.DIAMOND, 2, 40),
            new Offer(() -> GearItems.FROST_BOMB.get(), 4, 24),
            new Offer(() -> GearItems.RECALL_SCROLL.get(), 2, 30),
            new Offer(() -> ModItems.FORTUNE_BOX.get(), 1, 60),
            new Offer(() -> GearItems.LUCK_POTION.get(), 1, 70),
            new Offer(() -> GearItems.EMBER_GEM.get(), 1, 120),
            new Offer(() -> GearItems.FROST_GEM.get(), 1, 120),
            new Offer(() -> GearItems.BLOOD_GEM.get(), 1, 160),
            new Offer(() -> GearItems.STORM_GEM.get(), 1, 160),
            new Offer(() -> Items.NETHERITE_SCRAP, 1, 150),
            new Offer(() -> ModItems.TOME_OF_WINTER.get(), 1, 300),
            new Offer(() -> ModItems.TOME_OF_FLAME.get(), 1, 300),
            new Offer(() -> Items.ENCHANTED_GOLDEN_APPLE, 1, 400),
            new Offer(() -> GearItems.FROST_STEED_WHISTLE.get(), 1, 500),
            new Offer(() -> GearItems.SECOND_CHANCE_AMULET.get(), 1, 800),
            new Offer(() -> Items.ELYTRA, 1, 1500),
            new Offer(() -> ModItems.LUCKY_SEVEN.get(), 1, 2000));

    // ------------------------------------------------------------------ данные
    private static long today() {
        return System.currentTimeMillis() / 86_400_000L;
    }

    static CompoundTag data(ServerPlayer p) {
        CompoundTag root = p.getPersistentData();
        CompoundTag d = root.getCompound(KEY);
        if (d.getLong("Day") != today() || d.getList("List", Tag.TAG_COMPOUND).size() != COUNT) {
            d = new CompoundTag();
            d.putLong("Day", today());
            ListTag list = new ListTag();
            List<Type> pool = new java.util.ArrayList<>(List.of(Type.values()));
            for (int i = 0; i < COUNT; i++) list.add(roll(p.getRandom(), pool.remove(p.getRandom().nextInt(pool.size()))));
            d.put("List", list);
        }
        root.put(KEY, d);
        return d;
    }

    private static CompoundTag roll(RandomSource r, Type t) {
        CompoundTag c = new CompoundTag();
        int need = t.min + r.nextInt(t.max - t.min + 1);
        c.putString("Type", t.name());
        c.putInt("Need", need);
        c.putInt("Have", 0);
        c.putInt("Reward", t.reward + need * t.reward / t.max);
        c.putBoolean("Claimed", false);
        return c;
    }

    /** Прогресс задания типа t (вызывается из событий). */
    public static void progress(ServerPlayer p, Type t, int n) {
        CompoundTag d = data(p);
        ListTag list = d.getList("List", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            if (!c.getString("Type").equals(t.name()) || c.getBoolean("Claimed")) continue;
            int need = c.getInt("Need"), had = c.getInt("Have");
            if (had >= need) continue;
            c.putInt("Have", Math.min(need, had + n));
            if (had + n >= need) {
                p.displayClientMessage(Component.translatable("board.yukicore.done", Component.translatable("board.yukicore.type." + t.name().toLowerCase()))
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.6f);
            }
        }
    }

    /** Состояние для экрана: {бонус получен, фишки игрока, [тип, нужно, есть, награда, забрано] × 3}. */
    public static int[] state(ServerPlayer p) {
        CompoundTag d = data(p);
        ListTag list = d.getList("List", Tag.TAG_COMPOUND);
        int[] out = new int[2 + 5 * COUNT];
        out[0] = d.getBoolean("Bonus") ? 1 : 0;
        out[1] = Casino.chips(p);
        for (int i = 0; i < COUNT; i++) {
            CompoundTag c = list.getCompound(i);
            out[2 + i * 5] = Type.valueOf(c.getString("Type")).ordinal();
            out[3 + i * 5] = c.getInt("Need");
            out[4 + i * 5] = c.getInt("Have");
            out[5 + i * 5] = c.getInt("Reward");
            out[6 + i * 5] = c.getBoolean("Claimed") ? 1 : 0;
        }
        return out;
    }

    /** Действие с доски: 0 — обновить, 1 — забрать награду, 2 — сменить задание, 3 — купить в лавке. */
    public static int[] handle(ServerPlayer p, BlockPos pos, int action, int arg) {
        if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64
                || !p.level().getBlockState(pos).is(WorldItems.SINTARA_BOARD.get())) return null;
        CompoundTag d = data(p);
        ListTag list = d.getList("List", Tag.TAG_COMPOUND);
        switch (action) {
            case 1 -> {
                if (arg < 0 || arg >= COUNT) break;
                CompoundTag c = list.getCompound(arg);
                if (c.getBoolean("Claimed") || c.getInt("Have") < c.getInt("Need")) break;
                c.putBoolean("Claimed", true);
                giveChips(p, c.getInt("Reward"));
                p.giveExperiencePoints(40);
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1.2f);
                boolean all = true;
                for (int i = 0; i < COUNT; i++) all &= list.getCompound(i).getBoolean("Claimed");
                if (all && !d.getBoolean("Bonus")) {
                    d.putBoolean("Bonus", true);
                    giveChips(p, ALL_BONUS);
                    give(p, new ItemStack(ModItems.FORTUNE_BOX.get()));
                    p.displayClientMessage(Component.translatable("board.yukicore.all_done").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                    p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
                }
            }
            case 2 -> {
                if (arg < 0 || arg >= COUNT) break;
                CompoundTag c = list.getCompound(arg);
                if (c.getBoolean("Claimed") || c.getInt("Have") >= c.getInt("Need")) break;
                if (!p.getAbilities().instabuild) {
                    if (Casino.chips(p) < REROLL_COST) break;
                    Casino.take(p, REROLL_COST);
                }
                List<Type> pool = new java.util.ArrayList<>(List.of(Type.values()));
                for (int i = 0; i < COUNT; i++) pool.remove(Type.valueOf(list.getCompound(i).getString("Type")));
                if (pool.isEmpty()) pool.add(Type.valueOf(c.getString("Type")));
                list.set(arg, roll(p.getRandom(), pool.get(p.getRandom().nextInt(pool.size()))));
            }
            case 3 -> {
                if (arg < 0 || arg >= SHOP.size()) break;
                Offer o = SHOP.get(arg);
                if (!p.getAbilities().instabuild) {
                    if (Casino.chips(p) < o.price()) {
                        p.displayClientMessage(Component.translatable("casino.yukicore.no_chips").withStyle(ChatFormatting.RED), true);
                        break;
                    }
                    Casino.take(p, o.price());
                }
                give(p, new ItemStack(o.item().get(), o.count()));
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 0.8f, 1.3f);
            }
            default -> {
            }
        }
        return state(p);
    }

    private static void giveChips(ServerPlayer p, int n) {
        while (n > 0) {
            int k = Math.min(64, n);
            give(p, new ItemStack(ModItems.SINTARA_CHIP.get(), k));
            n -= k;
        }
    }

    private static void give(ServerPlayer p, ItemStack st) {
        if (!p.getInventory().add(st)) p.drop(st, false);
    }

    // ------------------------------------------------------------------ прогресс из событий
    @SubscribeEvent
    public static void onKill(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Enemy && e.getSource().getEntity() instanceof ServerPlayer p) progress(p, Type.MONSTERS, 1);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer() instanceof ServerPlayer p && e.getState().is(ORES)) progress(p, Type.ORES, 1);
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) progress(p, Type.FISH, 1);
    }

    /** Вызывается при успешном заклинании. */
    public static void onCast(ServerPlayer p) {
        progress(p, Type.SPELLS, 1);
    }
}
