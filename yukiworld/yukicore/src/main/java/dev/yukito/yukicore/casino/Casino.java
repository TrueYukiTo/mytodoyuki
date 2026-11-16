package dev.yukito.yukicore.casino;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Казино Синтары — только на игровые фишки. Огненный автомат (3 барабана, возврат ~95%, джекпот 777),
 * Кости Синтары и Огненный сундук удачи.
 */
public final class Casino {
    private Casino() {}

    /** Символы: мандарин, снежинка, пламя, кристалл, звезда, семёрка. */
    public static final String[] SYMBOLS = {"tangerine", "snowflake", "flame", "crystal", "star", "seven"};
    private static final int[] WEIGHT = {30, 24, 18, 12, 8, 5};
    private static final int[] TRIPLE = {5, 8, 12, 25, 50, 200};
    private static final int[] DOUBLE = {0, 1, 1, 2, 3, 10};
    public static final int[] BETS = {1, 5, 10};
    private static final Map<UUID, Long> NEXT_SPIN = new HashMap<>();

    public static int chips(ServerPlayer p) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) if (s.is(ModItems.SINTARA_CHIP.get())) n += s.getCount();
        return n;
    }

    public static void take(ServerPlayer p, int n) {
        for (ItemStack s : p.getInventory().items) {
            if (n <= 0) break;
            if (!s.is(ModItems.SINTARA_CHIP.get())) continue;
            int t = Math.min(n, s.getCount());
            s.shrink(t);
            n -= t;
        }
    }

    public static void give(ServerPlayer p, ItemStack st) {
        if (!p.getInventory().add(st)) p.drop(st, false);
    }

    private static int roll(RandomSource r) {
        int total = 0;
        for (int w : WEIGHT) total += w;
        int x = r.nextInt(total);
        for (int i = 0; i < WEIGHT.length; i++) {
            x -= WEIGHT[i];
            if (x < 0) return i;
        }
        return 0;
    }

    public static int multiplier(int a, int b, int c) {
        if (a == b && b == c) return TRIPLE[a];
        if (a == b || a == c) return DOUBLE[a];
        if (b == c) return DOUBLE[b];
        return 0;
    }

    /** Прокрутка на сервере. Возвращает {a, b, c, выигрыш} или null, если ставка невозможна. */
    public static int[] spin(ServerPlayer p, BlockPos pos, int bet) {
        if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64
                || !p.level().getBlockState(pos).is(ModBlocks.SLOT_MACHINE.get())) return null;
        boolean ok = false;
        for (int b : BETS) ok |= b == bet;
        long now = p.level().getGameTime();
        if (!ok || NEXT_SPIN.getOrDefault(p.getUUID(), 0L) > now) return null;
        if (chips(p) < bet && !p.getAbilities().instabuild) {
            p.displayClientMessage(Component.translatable("casino.yukicore.no_chips").withStyle(ChatFormatting.RED), true);
            return null;
        }
        NEXT_SPIN.put(p.getUUID(), now + 40);
        if (!p.getAbilities().instabuild) take(p, bet);
        RandomSource r = p.getRandom();
        int a = roll(r), b = roll(r), c = roll(r);
        int win = multiplier(a, b, c) * bet;
        ServerLevel lvl = p.serverLevel();
        lvl.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1f, 1.2f);
        // выигрыш выдаётся, когда барабаны остановятся
        final int fa = a, fb = b, fc = c, fw = win;
        dev.yukito.yukicore.magic.Scheduler.later(lvl, 38, () -> payout(p, pos, fa, fb, fc, fw, bet));
        return new int[]{a, b, c, win};
    }

    private static void payout(ServerPlayer p, BlockPos pos, int a, int b, int c, int win, int bet) {
        ServerLevel lvl = p.serverLevel();
        if (win <= 0) {
            lvl.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.4f);
            return;
        }
        int left = win;
        while (left > 0) {
            int n = Math.min(64, left);
            give(p, new ItemStack(ModItems.SINTARA_CHIP.get(), n));
            left -= n;
        }
        boolean jackpot = a == 5 && b == 5 && c == 5;
        boolean big = a == b && b == c && a >= 3;
        lvl.playSound(null, pos, big ? SoundEvents.PLAYER_LEVELUP : SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1f,
                big ? 0.8f : 1.4f);
        lvl.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5, 20 + win, 0.4, 0.4, 0.4, 0.05);
        if (a == 4 && b == 4 && c == 4) give(p, new ItemStack(ModItems.FORTUNE_BOX.get()));
        if (jackpot) {
            give(p, new ItemStack(ModItems.LUCKY_SEVEN.get()));
            for (int i = 0; i < 6; i++) firework(lvl, pos.getX() + 0.5 + (i - 3), pos.getY() + 1.5, pos.getZ() + 0.5);
            lvl.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("casino.yukicore.jackpot_all",
                    p.getDisplayName(), win).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        } else if (big) {
            firework(lvl, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
        }
    }

    private static void firework(ServerLevel lvl, double x, double y, double z) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(net.minecraft.core.component.DataComponents.FIREWORKS, new net.minecraft.world.item.component.Fireworks(1,
                java.util.List.of(new net.minecraft.world.item.component.FireworkExplosion(
                        net.minecraft.world.item.component.FireworkExplosion.Shape.LARGE_BALL,
                        it.unimi.dsi.fastutil.ints.IntList.of(0xFF7A1C, 0xFFE070), it.unimi.dsi.fastutil.ints.IntList.of(0xFFFFFF),
                        true, true))));
        lvl.addFreshEntity(new FireworkRocketEntity(lvl, x, y, z, rocket));
    }

    // ------------------------------------------------------------------ кости
    public static void dice(ServerPlayer p) {
        if (chips(p) < 1 && !p.getAbilities().instabuild) {
            p.displayClientMessage(Component.translatable("casino.yukicore.no_chips").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (!p.getAbilities().instabuild) take(p, 1);
        int d1 = 1 + p.getRandom().nextInt(6), d2 = 1 + p.getRandom().nextInt(6), s = d1 + d2;
        int win = s == 12 ? 10 : (s == 7 || s == 11) ? 2 : d1 == d2 ? 1 : 0;
        if (win > 0) give(p, new ItemStack(ModItems.SINTARA_CHIP.get(), win));
        ChatFormatting col = win >= 10 ? ChatFormatting.GOLD : win > 1 ? ChatFormatting.GREEN : win == 1 ? ChatFormatting.YELLOW
                : ChatFormatting.GRAY;
        p.displayClientMessage(Component.translatable("casino.yukicore.dice", d1, d2, win).withStyle(col), true);
        p.serverLevel().playSound(null, p.blockPosition(), win > 0 ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.WOOD_HIT,
                SoundSource.PLAYERS, 1f, 1.2f);
        if (win >= 10) firework(p.serverLevel(), p.getX(), p.getY() + 1, p.getZ());
    }

    // ------------------------------------------------------------------ сундук удачи
    public static void openBox(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        int x = p.getRandom().nextInt(100);
        String rarity = x < 3 ? "legendary" : x < 15 ? "epic" : x < 40 ? "rare" : "common";
        ResourceKey<LootTable> key = ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                YukiCore.id("gameplay/fortune_" + rarity));
        LootTable table = lvl.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(lvl).withParameter(LootContextParams.ORIGIN, p.position())
                .withParameter(LootContextParams.THIS_ENTITY, p).withLuck(p.getLuck()).create(LootContextParamSets.GIFT);
        for (ItemStack st : table.getRandomItems(params)) give(p, st);
        ChatFormatting c = switch (rarity) {
            case "legendary" -> ChatFormatting.GOLD;
            case "epic" -> ChatFormatting.LIGHT_PURPLE;
            case "rare" -> ChatFormatting.AQUA;
            default -> ChatFormatting.WHITE;
        };
        p.displayClientMessage(Component.translatable("casino.yukicore.box." + rarity).withStyle(c, ChatFormatting.BOLD), true);
        lvl.sendParticles(rarity.equals("legendary") ? ParticleTypes.TOTEM_OF_UNDYING : ParticleTypes.FLAME,
                p.getX(), p.getY() + 1, p.getZ(), 60, 0.6, 0.8, 0.6, 0.2);
        lvl.playSound(null, p.blockPosition(), rarity.equals("legendary") ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE
                : SoundEvents.CHEST_OPEN, SoundSource.PLAYERS, 1f, 1.2f);
        if (rarity.equals("legendary")) firework(lvl, p.getX(), p.getY() + 1, p.getZ());
    }
}
