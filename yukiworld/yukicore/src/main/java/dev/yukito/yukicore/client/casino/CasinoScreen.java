package dev.yukito.yukicore.client.casino;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.casino.Casino;
import dev.yukito.yukicore.casino.CasinoGames;
import dev.yukito.yukicore.client.YukiTheme;
import dev.yukito.yukicore.magic.net.MagicNet;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Казино Синтары 2.0: вкладки «Слоты», «Рулетка», «Выше/Ниже», «21» и «Бонус».
 * Только игровые фишки. Исходы считает сервер — экран лишь крутит анимацию.
 */
public class CasinoScreen extends Screen {
    private static final int W = 300, H = 236;
    private static final String[] TABS = {"slots", "roulette", "hilo", "blackjack", "bonus"};
    private static final int[] STOP = {22, 30, 38};
    private static int tab = 0, betIndex = 0;

    private final BlockPos pos;
    private int x0, y0, ticks;
    private Component status = Component.empty();
    private int statusColor = 0xFFFFFFFF;

    // слоты
    private int[] reels = {5, 5, 5};
    private int spinStart = -1000, slotWin = -1, pendingWin = -1, freeSpins, freeBet;
    // рулетка / колесо: анимация полосы
    private int stripStart = -1000, rouStart = -1000, rouTarget, wheelStart = -1000, wheelTarget;
    private int pendingRoulette = -1;
    // выше/ниже
    private int hiloCard = -1, hiloPrev = -1, hiloShowAt = -1000;
    // 21
    private int bjStatus = -1, bjWin, bjBet;
    private final List<Integer> bjMe = new ArrayList<>(), bjDealer = new ArrayList<>();
    // бонус
    private boolean wheelReady;
    private int streak, wheelSecs, wheelSeg = -1, wheelPrize;
    private long wheelAt;

    public CasinoScreen(BlockPos pos) {
        super(Component.translatable("casino.yukicore.title"));
        this.pos = pos;
    }

    public static void open(BlockPos pos) {
        CasinoScreen s = new CasinoScreen(pos);
        MagicNet.casinoHandler = s::onResult;
        Minecraft.getInstance().setScreen(s);
        s.send(CasinoGames.WHEEL, 0);
        s.send(CasinoGames.BLACKJACK, 4);
    }

    private void send(int game, int action) {
        PacketDistributor.sendToServer(new MagicNet.CasinoReq(pos, game, action, bet()));
    }

    private static int bet() {
        return CasinoGames.BETS[betIndex];
    }

    private static ResourceLocation icon(int i) {
        return YukiCore.id("textures/gui/casino/" + Casino.SYMBOLS[i] + ".png");
    }

    private static void click(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    // ------------------------------------------------------------------ виджеты
    @Override
    protected void init() {
        x0 = (width - W) / 2;
        y0 = (height - H) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int t = i;
            Button b = Button.builder(Component.translatable("casino.yukicore.tab." + TABS[i]), btn -> {
                tab = t;
                status = Component.empty();
                rebuildWidgets();
            }).bounds(x0 + 6 + i * 58, y0 + 22, 56, 16).build();
            b.active = i != tab;
            addRenderableWidget(b);
        }
        if (tab != 4) {
            addRenderableWidget(Button.builder(Component.literal("−"), b -> {
                betIndex = Math.max(0, betIndex - 1);
                click(0.9f);
            }).bounds(x0 + 10, y0 + 186, 16, 16).build());
            addRenderableWidget(Button.builder(Component.literal("+"), b -> {
                betIndex = Math.min(CasinoGames.BETS.length - 1, betIndex + 1);
                click(1.1f);
            }).bounds(x0 + 98, y0 + 186, 16, 16).build());
        }
        int by = y0 + 206;
        switch (tab) {
            case 0 -> addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.spin").withStyle(ChatFormatting.BOLD),
                    b -> spin()).bounds(x0 + W - 112, y0 + 184, 102, 20).build());
            case 1 -> {
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.roulette.red"), b -> roulette(0))
                        .bounds(x0 + 120, by - 22, 58, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.roulette.black"), b -> roulette(1))
                        .bounds(x0 + 180, by - 22, 58, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.roulette.green"), b -> roulette(2))
                        .bounds(x0 + 240, by - 22, 54, 18).build());
            }
            case 2 -> {
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.hilo.higher"), b -> hilo(1))
                        .bounds(x0 + 120, by - 22, 86, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.hilo.lower"), b -> hilo(2))
                        .bounds(x0 + 208, by - 22, 86, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.hilo.new"), b -> {
                    send(CasinoGames.HILO, 0);
                    click(1.2f);
                }).bounds(x0 + 120, by, 174, 16).build());
                if (hiloCard < 0) send(CasinoGames.HILO, 0);
            }
            case 3 -> {
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.bj.deal"), b -> bj(0))
                        .bounds(x0 + 120, by - 22, 86, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.bj.double"), b -> bj(3))
                        .bounds(x0 + 208, by - 22, 86, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.bj.hit"), b -> bj(1))
                        .bounds(x0 + 120, by, 86, 16).build());
                addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.bj.stand"), b -> bj(2))
                        .bounds(x0 + 208, by, 86, 16).build());
            }
            default -> addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.wheel.spin").withStyle(ChatFormatting.BOLD),
                    b -> wheel()).bounds(x0 + W / 2 - 60, y0 + 184, 120, 20).build());
        }
    }

    // ------------------------------------------------------------------ действия
    private boolean animating() {
        return ticks - spinStart < STOP[2] + 2 || ticks - stripStart < 58;
    }

    private void spin() {
        if (animating()) return;
        slotWin = -1;
        pendingWin = -1;
        spinStart = ticks;
        send(CasinoGames.SLOTS, 0);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME, 1.4f));
    }

    private void roulette(int choice) {
        if (animating()) return;
        status = Component.empty();
        send(CasinoGames.ROULETTE, choice);
    }

    private void hilo(int action) {
        if (hiloCard < 0 || ticks - hiloShowAt < 14) return;
        send(CasinoGames.HILO, action);
    }

    private void bj(int action) {
        send(CasinoGames.BLACKJACK, action);
    }

    private void wheel() {
        if (animating() || !wheelReady) return;
        send(CasinoGames.WHEEL, 1);
    }

    private void say(Component c, int color) {
        status = c;
        statusColor = color;
    }

    private void onResult(MagicNet.CasinoRes r) {
        List<Integer> d = r.data();
        switch (r.game()) {
            case CasinoGames.SLOTS -> {
                reels = new int[]{d.get(0), d.get(1), d.get(2)};
                pendingWin = d.get(3);
                freeSpins = d.get(4);
                freeBet = d.get(5);
            }
            case CasinoGames.ROULETTE -> {
                rouTarget = d.get(0);
                rouStart = stripStart = ticks;
                pendingRoulette = d.get(1);
            }
            case CasinoGames.HILO -> {
                if (d.size() == 1) {
                    hiloPrev = -1;
                    hiloCard = d.get(0);
                    status = Component.empty();
                } else {
                    hiloPrev = d.get(0);
                    hiloCard = d.get(1);
                    hiloShowAt = ticks;
                    int win = d.get(2);
                    if (win > bet()) say(Component.translatable("casino.yukicore.win", win), 0xFF7CFF7C);
                    else if (win == bet()) say(Component.translatable("casino.yukicore.push"), 0xFFFFE070);
                    else say(Component.translatable("casino.yukicore.lose"), 0xFFAAAAAA);
                }
            }
            case CasinoGames.BLACKJACK -> {
                if (d.get(0) < 0) return;
                bjStatus = d.get(0);
                bjWin = d.get(1);
                bjBet = d.get(2);
                bjMe.clear();
                bjDealer.clear();
                int n = d.get(3), i = 4;
                for (int k = 0; k < n; k++) bjMe.add(d.get(i++));
                int m = d.get(i++);
                for (int k = 0; k < m; k++) bjDealer.add(d.get(i++));
                if (bjStatus > 0) {
                    int col = bjStatus == 2 ? 0xFFAAAAAA : bjStatus == 3 ? 0xFFFFE070 : 0xFF7CFF7C;
                    say(Component.translatable("casino.yukicore.bj.status" + bjStatus, bjWin), col);
                } else say(Component.translatable("casino.yukicore.bj.quip" + (ticks / 40 % 4)), 0xFFFFC890);
            }
            case CasinoGames.WHEEL -> {
                wheelReady = d.get(0) == 1;
                streak = d.get(1);
                wheelSecs = d.get(2);
                wheelAt = System.currentTimeMillis();
                if (d.size() > 6) {
                    freeSpins = d.get(5);
                    freeBet = d.get(6);
                }
                if (d.get(3) >= 0) {
                    wheelSeg = d.get(3);
                    wheelPrize = d.get(4);
                    wheelTarget = wheelSeg;
                    wheelStart = stripStart = ticks;
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void tick() {
        ticks++;
        if (tab == 4) {
            for (var w : children()) {
                if (w instanceof Button b && b.getMessage().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
                        && tc.getKey().equals("casino.yukicore.wheel.spin")) b.active = wheelReady && !animating();
            }
        }
        if (tab == 0) {
            for (int s : STOP) {
                if (ticks - spinStart == s) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 1f));
                }
            }
            if (ticks - spinStart == STOP[2] + 2) {
                if (pendingWin >= 0) slotWin = pendingWin;
                else spinStart = -1000; // сервер отказал (нет фишек)
                pendingWin = -1;
            }
        }
        int st = ticks - stripStart;
        if (st > 0 && st < 56 && st % Math.max(1, st / 8 + 1) == 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.6f));
        }
        if (st == 57) {
            if (pendingRoulette >= 0) {
                int w = pendingRoulette;
                say(w > 0 ? Component.translatable("casino.yukicore.win", w) : Component.translatable("casino.yukicore.lose"),
                        w > 0 ? 0xFF7CFF7C : 0xFFAAAAAA);
                pendingRoulette = -1;
            } else if (wheelSeg >= 0) {
                say(CasinoGames.WHEEL_PRIZES[wheelSeg] < 0 ? Component.translatable("casino.yukicore.wheel.box")
                        : Component.translatable("casino.yukicore.wheel.won", wheelPrize), 0xFFFFD060);
                wheelSeg = -1;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // фон рисуется в render(), иначе super.render() затёр бы панель
    }

    // ------------------------------------------------------------------ отрисовка
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderTransparentBackground(g);
        YukiTheme.roundedRect(g, x0 - 3, y0 - 3, W + 6, H + 6, 0xFF2A0804);
        g.fillGradient(x0, y0, x0 + W, y0 + H, 0xFF5A1404, 0xFF1A0402);
        for (int i = 0; i < 29; i++) {
            boolean on = ((ticks / 4) + i) % 2 == 0;
            int c = on ? 0xFFFFE070 : 0xFF8A4A10;
            g.fill(x0 + 6 + i * 10, y0 + 3, x0 + 9 + i * 10, y0 + 6, c);
            g.fill(x0 + 6 + i * 10, y0 + H - 6, x0 + 9 + i * 10, y0 + H - 3, c);
        }
        g.drawCenteredString(font, title.copy().withStyle(ChatFormatting.BOLD), x0 + W / 2, y0 + 10, 0xFFFFD060);
        float t = ticks + pt;
        switch (tab) {
            case 0 -> renderSlots(g, t);
            case 1 -> renderRoulette(g, t);
            case 2 -> renderHilo(g);
            case 3 -> renderBlackjack(g);
            default -> renderBonus(g, t);
        }
        // ставка и фишки
        if (tab != 4) {
            g.fill(x0 + 28, y0 + 186, x0 + 96, y0 + 202, 0xA0000000);
            g.drawCenteredString(font, Component.translatable("casino.yukicore.bet", bet()), x0 + 62, y0 + 190, 0xFFFFE6A0);
        }
        int chips = 0;
        if (minecraft != null && minecraft.player != null) {
            for (ItemStack s : minecraft.player.getInventory().items) if (s.is(ModItems.SINTARA_CHIP.get())) chips += s.getCount();
        }
        g.drawString(font, Component.translatable("casino.yukicore.chips", chips), x0 + 10, y0 + 208, 0xFFFFE6A0, true);
        if (!status.getString().isEmpty()) g.drawCenteredString(font, status, x0 + W / 2, y0 + 168, statusColor);
        g.pose().pushPose();
        g.pose().translate(x0 + W / 2f, y0 + H - 14, 0);
        g.pose().scale(0.7f, 0.7f, 1);
        g.drawCenteredString(font, Component.translatable("casino.yukicore.fair"), 0, 0, 0xFFB08060);
        g.pose().popPose();
        super.render(g, mx, my, pt);
    }

    private void small(GuiGraphics g, Component c, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x0 + W / 2f, y, 0);
        g.pose().scale(0.75f, 0.75f, 1);
        g.drawCenteredString(font, c, 0, 0, color);
        g.pose().popPose();
    }

    private void renderSlots(GuiGraphics g, float t0) {
        float t = t0 - spinStart;
        for (int r = 0; r < 3; r++) {
            int rx = x0 + 58 + r * 64, ry = y0 + 46;
            g.fill(rx - 3, ry - 3, rx + 55, ry + 69, 0xFFFFD060);
            g.fill(rx, ry, rx + 52, ry + 66, 0xFFF4ECDC);
            g.enableScissor(rx, ry, rx + 52, ry + 66);
            if (t < STOP[r]) {
                float roll = t * 0.9f + r * 2;
                int base = (int) Math.floor(roll);
                float frac = roll - base;
                for (int k = -1; k <= 1; k++) {
                    int sym = Math.floorMod(base + k + r * 2, 6);
                    int sy = ry + 17 + (int) ((k + frac) * 34) - 34;
                    g.blit(icon(sym), rx + 10, sy + 17, 0, 0, 32, 32, 32, 32);
                }
            } else {
                g.blit(icon(reels[r]), rx + 10, ry + 17, 0, 0, 32, 32, 32, 32);
                g.blit(icon(Math.floorMod(reels[r] - 1, 6)), rx + 14, ry - 14, 0, 0, 24, 24, 24, 24);
                g.blit(icon(Math.floorMod(reels[r] + 1, 6)), rx + 14, ry + 56, 0, 0, 24, 24, 24, 24);
            }
            g.disableScissor();
            g.fill(rx, ry + 32, rx + 52, ry + 33, 0x60FF2000);
        }
        if (freeSpins > 0) {
            g.drawCenteredString(font, Component.translatable("casino.yukicore.free_left", freeSpins, freeBet)
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), x0 + W / 2, y0 + 120, 0xFFFFFFFF);
        }
        if (slotWin >= 0 && ticks - spinStart >= STOP[2] + 2) {
            Component msg = slotWin == 0 ? Component.translatable("casino.yukicore.lose").withStyle(ChatFormatting.GRAY)
                    : reels[0] == 5 && reels[1] == 5 && reels[2] == 5
                    ? Component.translatable("casino.yukicore.jackpot", slotWin).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    : Component.translatable("casino.yukicore.win", slotWin).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
            g.drawCenteredString(font, msg, x0 + W / 2, y0 + 168, 0xFFFFFFFF);
        }
        small(g, Component.translatable("casino.yukicore.table1"), y0 + 132, 0xFFFFD8A0);
        small(g, Component.translatable("casino.yukicore.table1b"), y0 + 141, 0xFFFFD8A0);
        small(g, Component.translatable("casino.yukicore.table2"), y0 + 150, 0xFFFFD8A0);
        small(g, Component.translatable("casino.yukicore.table3"), y0 + 159, 0xFF8FE8FF);
    }

    /** Горизонтальная полоса с ячейками, которая прокручивается и останавливается на target под стрелкой. */
    private void strip(GuiGraphics g, float t0, int y, int n, int target, int start, java.util.function.IntFunction<int[]> cell,
                       java.util.function.IntFunction<String> label) {
        int cw = 34, cx = x0 + W / 2;
        float t = Math.min(56, Math.max(0, t0 - start));
        float k = t / 56f;
        float travel = (1 - (1 - k) * (1 - k) * (1 - k)) * (n * 3 + 0f);
        float posCells = start < -500 ? target : target - n * 3 + travel;
        g.fill(x0 + 10, y - 2, x0 + W - 10, y + 40, 0xFF120302);
        g.enableScissor(x0 + 12, y, x0 + W - 12, y + 38);
        int first = (int) Math.floor(posCells) - 6;
        for (int i = first; i <= first + 13; i++) {
            int idx = Math.floorMod(i, n);
            int px = (int) (cx + (i - posCells) * cw - cw / 2f);
            int[] col = cell.apply(idx);
            g.fill(px + 1, y + 1, px + cw - 1, y + 37, col[0]);
            g.drawCenteredString(font, label.apply(idx), px + cw / 2, y + 15, col[1]);
        }
        g.disableScissor();
        g.fill(cx - 1, y - 6, cx + 1, y, 0xFFFFE070);
        g.fill(cx - 1, y + 38, cx + 1, y + 44, 0xFFFFE070);
        g.renderOutline(cx - cw / 2, y, cw, 38, 0xFFFFE070);
        g.drawCenteredString(font, "▼", cx, y - 11, 0xFFFFE070);
    }

    private void renderRoulette(GuiGraphics g, float t) {
        strip(g, t, y0 + 66, 15, rouTarget, rouStart, i -> new int[]{switch (CasinoGames.rouletteColor(i)) {
            case 0 -> 0xFFB01818;
            case 1 -> 0xFF1A1A1A;
            default -> 0xFF168A2A;
        }, 0xFFFFFFFF}, String::valueOf);
        small(g, Component.translatable("casino.yukicore.roulette.rules"), y0 + 120, 0xFFFFD8A0);
        small(g, Component.translatable("casino.yukicore.roulette.rules2"), y0 + 130, 0xFFFFD8A0);
    }

    private static String rankName(int rank) {
        return switch (rank) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> String.valueOf(rank);
        };
    }

    /** Карта: rank 1–13, suit 0–3; rank < 0 — рубашкой вверх. */
    private void card(GuiGraphics g, int x, int y, int rank, int suit, float scale) {
        int w = (int) (28 * scale), h = (int) (40 * scale);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF3A1A04);
        if (rank < 0) {
            g.fillGradient(x, y, x + w, y + h, 0xFFB0301A, 0xFF5A0E06);
            g.drawCenteredString(font, "✦", x + w / 2, y + h / 2 - 4, 0xFFFFD060);
            return;
        }
        g.fill(x, y, x + w, y + h, 0xFFFFF8EC);
        String[] suits = {"♥", "♦", "♣", "♠"};
        int col = suit < 2 ? 0xFFD01818 : 0xFF181818;
        g.drawString(font, rankName(rank), x + 3, y + 3, col, false);
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y + h / 2f - 4 * scale, 0);
        g.pose().scale(scale * 1.5f, scale * 1.5f, 1);
        g.drawCenteredString(font, suits[suit], 0, 0, col);
        g.pose().popPose();
    }

    private void renderHilo(GuiGraphics g) {
        int cx = x0 + W / 2;
        if (hiloPrev > 0) {
            card(g, cx - 80, y0 + 56, hiloPrev, hiloPrev % 4, 1.4f);
            g.drawCenteredString(font, "→", cx - 22, y0 + 80, 0xFFFFE070);
        }
        if (hiloCard > 0) card(g, hiloPrev > 0 ? cx : cx - 20, y0 + 56, hiloCard, (hiloCard * 7) % 4, 1.4f);
        if (hiloCard > 0) {
            int hp = CasinoGames.hiloPercent(hiloCard, true), lp = CasinoGames.hiloPercent(hiloCard, false);
            small(g, Component.translatable("casino.yukicore.hilo.odds", hp == 0 ? "—" : String.format("×%.2f", hp / 100f),
                    lp == 0 ? "—" : String.format("×%.2f", lp / 100f)), y0 + 124, 0xFFFFD8A0);
        }
        small(g, Component.translatable("casino.yukicore.hilo.rules"), y0 + 134, 0xFFFFD8A0);
    }

    private void renderBlackjack(GuiGraphics g) {
        int lx = x0 + 14;
        g.drawString(font, Component.translatable("casino.yukicore.bj.dealer", bjDealerScore()), lx, y0 + 46, 0xFFFFC890, true);
        for (int i = 0; i < bjDealer.size(); i++) {
            int c = bjDealer.get(i);
            card(g, lx + 70 + i * 32, y0 + 44, c < 0 ? -1 : c / 4 + 1, c < 0 ? 0 : c % 4, 1f);
        }
        g.drawString(font, Component.translatable("casino.yukicore.bj.you", bjMe.isEmpty() ? "—" : String.valueOf(CasinoGames.score(bjMe))),
                lx, y0 + 98, 0xFFFFE6A0, true);
        for (int i = 0; i < bjMe.size(); i++) {
            int c = bjMe.get(i);
            card(g, lx + 70 + i * 32, y0 + 96, c / 4 + 1, c % 4, 1f);
        }
        if (bjStatus == 0) small(g, Component.translatable("casino.yukicore.bj.in_game", bjBet), y0 + 144, 0xFFFFE070);
        small(g, Component.translatable("casino.yukicore.bj.rules"), y0 + 154, 0xFFFFD8A0);
    }

    private String bjDealerScore() {
        if (bjDealer.isEmpty()) return "—";
        List<Integer> open = new ArrayList<>();
        for (int c : bjDealer) if (c >= 0) open.add(c);
        return String.valueOf(CasinoGames.score(open));
    }

    private void renderBonus(GuiGraphics g, float t) {
        strip(g, t, y0 + 60, CasinoGames.WHEEL_PRIZES.length, wheelTarget, wheelStart, i -> {
            int p = CasinoGames.WHEEL_PRIZES[i];
            int bg = p < 0 ? 0xFF7A2AB0 : p >= 250 ? 0xFFC89010 : i % 2 == 0 ? 0xFFB0301A : 0xFF6A1A0A;
            return new int[]{bg, 0xFFFFFFFF};
        }, i -> CasinoGames.WHEEL_PRIZES[i] < 0 ? "✦" : String.valueOf(CasinoGames.WHEEL_PRIZES[i]));
        long left = Math.max(0, wheelSecs - (System.currentTimeMillis() - wheelAt) / 1000);
        Component state = wheelReady ? Component.translatable("casino.yukicore.wheel.ready").withStyle(ChatFormatting.GREEN)
                : Component.translatable("casino.yukicore.wheel.wait", left / 3600, (left / 60) % 60).withStyle(ChatFormatting.GRAY);
        g.drawCenteredString(font, state, x0 + W / 2, y0 + 112, 0xFFFFFFFF);
        g.drawCenteredString(font, Component.translatable("casino.yukicore.wheel.streak", streak, streak * 10), x0 + W / 2, y0 + 126, 0xFFFFD060);
        small(g, Component.translatable("casino.yukicore.wheel.rules"), y0 + 142, 0xFFFFD8A0);
        small(g, Component.translatable("casino.yukicore.wheel.rules2"), y0 + 152, 0xFFFFD8A0);
        if (freeSpins > 0) {
            g.drawCenteredString(font, Component.translatable("casino.yukicore.free_left", freeSpins, freeBet)
                    .withStyle(ChatFormatting.AQUA), x0 + W / 2, y0 + 210, 0xFFFFFFFF);
        }
    }
}
