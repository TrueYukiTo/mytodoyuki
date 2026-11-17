package dev.yukito.yukicore.casino;

import dev.yukito.yukicore.magic.Scheduler;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Казино Синтары 2.0 (только игровые фишки): слоты с бесплатными прокрутками, рулетка, «Выше/Ниже»,
 * «21» против Синтары и ежедневное колесо удачи с бонусом за серию дней.
 * Все исходы считает сервер; клиент только показывает анимацию.
 */
public final class CasinoGames {
    private CasinoGames() {}

    public static final int SLOTS = 0, ROULETTE = 1, HILO = 2, BLACKJACK = 3, WHEEL = 4;
    /** Ставки для всех игр. */
    public static final int[] BETS = {1, 5, 10, 25, 50, 100};
    /** Колесо удачи: призы в фишках (−1 — сундук удачи), веса. */
    public static final int[] WHEEL_PRIZES = {10, 15, 20, 25, 30, 50, 75, 100, 150, 250, -1, 500};
    private static final int[] WHEEL_WEIGHT = {16, 15, 14, 12, 10, 9, 7, 6, 4, 3, 3, 1};
    public static final int FREE_SPINS = 5, WELCOME = 100;

    private static final Map<UUID, Long> BUSY = new HashMap<>();
    private static final Map<UUID, Integer> HILO_CARD = new HashMap<>();
    private static final Map<UUID, Hand> HANDS = new HashMap<>();

    private static final String TAG = "yukicore_casino";

    private static CompoundTag data(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(TAG)) root.put(TAG, new CompoundTag());
        return root.getCompound(TAG);
    }

    private static boolean validBet(int bet) {
        for (int b : BETS) if (b == bet) return true;
        return false;
    }

    private static boolean near(ServerPlayer p, BlockPos pos) {
        return p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64
                && p.level().getBlockState(pos).is(ModBlocks.SLOT_MACHINE.get());
    }

    private static boolean busy(ServerPlayer p, int ticks) {
        long now = p.level().getGameTime();
        if (BUSY.getOrDefault(p.getUUID(), 0L) > now) return true;
        BUSY.put(p.getUUID(), now + ticks);
        return false;
    }

    private static boolean pay(ServerPlayer p, int bet) {
        if (p.getAbilities().instabuild) return true;
        if (Casino.chips(p) < bet) {
            p.displayClientMessage(Component.translatable("casino.yukicore.no_chips").withStyle(ChatFormatting.RED), true);
            return false;
        }
        Casino.take(p, bet);
        return true;
    }

    static void giveChips(ServerPlayer p, int n) {
        while (n > 0) {
            int k = Math.min(64, n);
            Casino.give(p, new ItemStack(ModItems.SINTARA_CHIP.get(), k));
            n -= k;
        }
    }

    private static void winFx(ServerPlayer p, BlockPos pos, int win, int bet) {
        ServerLevel lvl = p.serverLevel();
        if (win <= 0) {
            lvl.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.4f);
            return;
        }
        boolean big = win >= bet * 10;
        lvl.playSound(null, pos, big ? SoundEvents.PLAYER_LEVELUP : SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1f, big ? 0.8f : 1.4f);
        lvl.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5, Math.min(80, 15 + win / 4), 0.4, 0.4, 0.4, 0.05);
    }

    /** Обработать запрос клиента. Возвращает данные для ответа или null. */
    public static int[] handle(ServerPlayer p, BlockPos pos, int game, int action, int bet) {
        if (!near(p, pos)) return null;
        return switch (game) {
            case SLOTS -> slots(p, pos, bet);
            case ROULETTE -> roulette(p, pos, action, bet);
            case HILO -> hilo(p, pos, action, bet);
            case BLACKJACK -> blackjack(p, pos, action, bet);
            case WHEEL -> {
                int[] w = wheel(p, pos, action);
                if (w == null) yield null;
                int[] f = freeSpins(p);
                int[] out = java.util.Arrays.copyOf(w, w.length + 2);
                out[w.length] = f[0];
                out[w.length + 1] = f[1];
                yield out;
            }
            default -> null;
        };
    }

    // ------------------------------------------------------------------ слоты + бесплатные прокрутки
    /** {a, b, c, выигрыш, осталось бесплатных, ставка бесплатных}. */
    private static int[] slots(ServerPlayer p, BlockPos pos, int bet) {
        CompoundTag d = data(p);
        int free = d.getInt("FreeSpins");
        if (free > 0) bet = Math.max(1, d.getInt("FreeBet"));
        else if (!validBet(bet)) return null;
        if (busy(p, 40)) return null;
        if (free > 0) d.putInt("FreeSpins", free - 1);
        else if (!pay(p, bet)) {
            BUSY.remove(p.getUUID());
            return null;
        }
        int[] r = Casino.rollReels(p.getRandom());
        int win = Casino.multiplier(r[0], r[1], r[2]) * bet * (free > 0 ? 2 : 1);
        boolean bonus = r[0] == 3 && r[1] == 3 && r[2] == 3;
        if (bonus) {
            d.putInt("FreeSpins", d.getInt("FreeSpins") + FREE_SPINS);
            d.putInt("FreeBet", bet);
        }
        final int fb = bet, fw = win;
        Scheduler.later(p.serverLevel(), 38, () -> {
            Casino.payout(p, pos, r[0], r[1], r[2], fw, fb);
            if (bonus) p.displayClientMessage(Component.translatable("casino.yukicore.free_won", FREE_SPINS)
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
        });
        return new int[]{r[0], r[1], r[2], win, d.getInt("FreeSpins"), d.getInt("FreeBet")};
    }

    /** Состояние бесплатных прокруток для экрана: {осталось, ставка}. */
    public static int[] freeSpins(Player p) {
        CompoundTag d = data(p);
        return new int[]{d.getInt("FreeSpins"), d.getInt("FreeBet")};
    }

    // ------------------------------------------------------------------ рулетка: 0 — зеро, 1–7 красные, 8–14 чёрные
    public static int rouletteColor(int n) {
        return n == 0 ? 2 : n <= 7 ? 0 : 1;
    }

    /** action: 0 красное (×2), 1 чёрное (×2), 2 зеро (×14). Ответ {число, выигрыш}. */
    private static int[] roulette(ServerPlayer p, BlockPos pos, int choice, int bet) {
        if (choice < 0 || choice > 2 || !validBet(bet) || busy(p, 60)) return null;
        if (!pay(p, bet)) {
            BUSY.remove(p.getUUID());
            return null;
        }
        int n = p.getRandom().nextInt(15);
        int win = rouletteColor(n) == choice ? bet * (choice == 2 ? 14 : 2) : 0;
        Scheduler.later(p.serverLevel(), 56, () -> {
            giveChips(p, win);
            winFx(p, pos, win, bet);
        });
        return new int[]{n, win};
    }

    // ------------------------------------------------------------------ выше / ниже
    /**
     * Множитель «выше/ниже» в процентах для карты c (1–13). Равная карта возвращает ставку, поэтому
     * выплата считается с учётом ничьей: возврат ≈ 96%. Слишком «верные» ставки (×1,04 и меньше) закрыты.
     */
    public static int hiloPercent(int card, boolean higher) {
        int wins = higher ? 13 - card : card - 1;
        if (wins <= 0) return 0;
        int pct = 1148 / wins;
        return pct < 105 ? 0 : pct;
    }

    /** action 0 — сдать карту {карта}; 1 выше, 2 ниже — {карта, следующая, выигрыш}. Равная карта — возврат ставки. */
    private static int[] hilo(ServerPlayer p, BlockPos pos, int action, int bet) {
        RandomSource r = p.getRandom();
        if (action == 0) {
            int c = 1 + r.nextInt(13);
            HILO_CARD.put(p.getUUID(), c);
            return new int[]{c};
        }
        Integer cur = HILO_CARD.get(p.getUUID());
        if (cur == null || (action != 1 && action != 2) || !validBet(bet)) return null;
        int pct = hiloPercent(cur, action == 1);
        if (pct == 0 || busy(p, 16)) return null;
        if (!pay(p, bet)) {
            BUSY.remove(p.getUUID());
            return null;
        }
        int next = 1 + r.nextInt(13);
        boolean ok = action == 1 ? next > cur : next < cur;
        int win = next == cur ? bet : ok ? bet * pct / 100 : 0;
        HILO_CARD.put(p.getUUID(), next);
        Scheduler.later(p.serverLevel(), 14, () -> {
            giveChips(p, win);
            winFx(p, pos, win - bet, bet);
        });
        return new int[]{cur, next, win};
    }

    // ------------------------------------------------------------------ «21» против Синтары
    private static final class Hand {
        final List<Integer> me = new ArrayList<>(), dealer = new ArrayList<>();
        int bet;
        int status; // 0 игра, 1 победа, 2 проигрыш, 3 ничья, 4 блэкджек
        int win;
    }

    /** Карта 0..51: rank = c / 4 + 1 (1 — туз, 11–13 — картинки), масть = c % 4. */
    private static int draw(RandomSource r) {
        return r.nextInt(52);
    }

    public static int score(List<Integer> cards) {
        int s = 0, aces = 0;
        for (int c : cards) {
            int rank = c / 4 + 1;
            if (rank == 1) {
                aces++;
                s += 11;
            } else s += Math.min(10, rank);
        }
        while (s > 21 && aces-- > 0) s -= 10;
        return s;
    }

    /** action 0 сдать (ставка), 1 ещё, 2 хватит, 3 удвоить, 4 показать текущую. */
    private static int[] blackjack(ServerPlayer p, BlockPos pos, int action, int bet) {
        RandomSource r = p.getRandom();
        Hand h = HANDS.get(p.getUUID());
        if (action == 4) return h == null ? new int[]{-1} : encode(h);
        if (action == 0) {
            if (h != null && h.status == 0) return encode(h);
            if (!validBet(bet) || busy(p, 10) || !pay(p, bet)) return null;
            h = new Hand();
            h.bet = bet;
            h.me.add(draw(r));
            h.dealer.add(draw(r));
            h.me.add(draw(r));
            h.dealer.add(draw(r));
            HANDS.put(p.getUUID(), h);
            if (score(h.me) == 21) finish(p, pos, h, r);
            return encode(h);
        }
        if (h == null || h.status != 0) return null;
        if (action == 3) {
            if (h.me.size() != 2 || !pay(p, h.bet)) return encode(h);
            h.bet *= 2;
            h.me.add(draw(r));
            finish(p, pos, h, r);
        } else if (action == 1) {
            h.me.add(draw(r));
            if (score(h.me) >= 21) finish(p, pos, h, r);
        } else if (action == 2) {
            finish(p, pos, h, r);
        }
        p.serverLevel().playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 1.2f);
        return encode(h);
    }

    private static void finish(ServerPlayer p, BlockPos pos, Hand h, RandomSource r) {
        int me = score(h.me);
        boolean natural = me == 21 && h.me.size() == 2;
        if (me <= 21 && !natural) while (score(h.dealer) < 17) h.dealer.add(draw(r));
        int dl = score(h.dealer);
        boolean dealerNatural = dl == 21 && h.dealer.size() == 2;
        if (me > 21) h.status = 2;
        else if (natural && !dealerNatural) h.status = 4;
        else if (dl > 21 || me > dl) h.status = 1;
        else if (me == dl) h.status = 3;
        else h.status = 2;
        h.win = switch (h.status) {
            case 4 -> h.bet * 5 / 2;
            case 1 -> h.bet * 2;
            case 3 -> h.bet;
            default -> 0;
        };
        int win = h.win, bet = h.bet;
        Scheduler.later(p.serverLevel(), 10, () -> {
            giveChips(p, win);
            winFx(p, pos, win - bet, bet);
        });
    }

    /** {статус, выигрыш, ставка, n, мои карты…, m, карты дилера…}; пока игра идёт, вторая карта Синтары скрыта (−1). */
    private static int[] encode(Hand h) {
        int[] out = new int[5 + h.me.size() + h.dealer.size()];
        int i = 0;
        out[i++] = h.status;
        out[i++] = h.win;
        out[i++] = h.bet;
        out[i++] = h.me.size();
        for (int c : h.me) out[i++] = c;
        out[i++] = h.dealer.size();
        for (int k = 0; k < h.dealer.size(); k++) out[i++] = h.status == 0 && k == 1 ? -1 : h.dealer.get(k);
        return out;
    }

    // ------------------------------------------------------------------ колесо удачи (раз в сутки) + приветственный бонус
    private static long today() {
        return System.currentTimeMillis() / 86_400_000L;
    }

    /** action 0 — состояние {можно крутить, серия дней, секунд до следующего}; 1 — крутить {сектор, приз, серия}. */
    private static int[] wheel(ServerPlayer p, BlockPos pos, int action) {
        CompoundTag d = data(p);
        if (!d.getBoolean("Welcome")) {
            d.putBoolean("Welcome", true);
            giveChips(p, WELCOME);
            p.displayClientMessage(Component.translatable("casino.yukicore.welcome", WELCOME).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        }
        long day = today(), last = d.getLong("WheelDay");
        boolean can = last < day;
        int streak = d.getInt("Streak");
        if (last < day - 1) streak = 0;
        int secs = (int) ((day + 1) * 86_400L - System.currentTimeMillis() / 1000L);
        if (action == 0 || !can) return new int[]{can ? 1 : 0, streak, secs, -1, 0};
        if (busy(p, 60)) return null;
        streak = Math.min(10, streak + 1);
        d.putLong("WheelDay", day);
        d.putInt("Streak", streak);
        int total = 0;
        for (int w : WHEEL_WEIGHT) total += w;
        int x = p.getRandom().nextInt(total), seg = 0;
        for (int i = 0; i < WHEEL_WEIGHT.length; i++) {
            x -= WHEEL_WEIGHT[i];
            if (x < 0) {
                seg = i;
                break;
            }
        }
        int prize = WHEEL_PRIZES[seg];
        int chips = prize < 0 ? 0 : prize * (10 + streak) / 10; // +10% за каждый день серии
        final int fs = seg, fc = chips;
        Scheduler.later(p.serverLevel(), 56, () -> {
            if (WHEEL_PRIZES[fs] < 0) Casino.give(p, new ItemStack(ModItems.FORTUNE_BOX.get()));
            giveChips(p, fc);
            winFx(p, pos, Math.max(fc, 50), 1);
        });
        return new int[]{0, streak, (int) ((day + 1) * 86_400L - System.currentTimeMillis() / 1000L), seg, chips};
    }
}
