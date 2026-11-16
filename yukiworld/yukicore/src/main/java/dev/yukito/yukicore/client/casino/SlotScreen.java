package dev.yukito.yukicore.client.casino;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.casino.Casino;
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

/** Огненный автомат Синтары: три барабана, ставки 1/5/10 фишек. Результат считает сервер. */
public class SlotScreen extends Screen {
    private static final int W = 248, H = 196;
    private static final int[] STOP = {22, 30, 38};
    private final BlockPos pos;
    private int x0, y0, bet = 1;
    private int[] result = {5, 5, 5};
    private int win = -1;
    private int spinStart = -1000, ticks;
    private final int[] seed = {0, 2, 4};
    private Button spinButton;

    public SlotScreen(BlockPos pos) {
        super(Component.translatable("casino.yukicore.title"));
        this.pos = pos;
    }

    public static void open(BlockPos pos) {
        SlotScreen s = new SlotScreen(pos);
        MagicNet.spinHandler = s::onResult;
        Minecraft.getInstance().setScreen(s);
    }

    private static ResourceLocation icon(int i) {
        return YukiCore.id("textures/gui/casino/" + Casino.SYMBOLS[i] + ".png");
    }

    @Override
    protected void init() {
        x0 = (width - W) / 2;
        y0 = (height - H) / 2;
        for (int i = 0; i < Casino.BETS.length; i++) {
            int b = Casino.BETS[i];
            addRenderableWidget(Button.builder(Component.literal("×" + b), btn -> bet = b)
                    .bounds(x0 + 18 + i * 38, y0 + 128, 34, 18).build());
        }
        spinButton = addRenderableWidget(Button.builder(Component.translatable("casino.yukicore.spin").withStyle(ChatFormatting.BOLD),
                btn -> spin()).bounds(x0 + W - 112, y0 + 124, 94, 24).build());
    }

    private boolean spinning() {
        return ticks - spinStart < STOP[2] + 2;
    }

    private void spin() {
        if (spinning()) return;
        win = -1;
        spinStart = ticks;
        PacketDistributor.sendToServer(new MagicNet.Spin(pos, bet));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME, 1.4f));
    }

    private void onResult(MagicNet.SpinResult r) {
        result = new int[]{r.a(), r.b(), r.c()};
        pendingWin = r.win();
    }

    private int pendingWin = -1;

    @Override
    public void tick() {
        ticks++;
        for (int i = 0; i < 3; i++) {
            if (ticks - spinStart == STOP[i]) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 1f));
            }
        }
        if (ticks - spinStart == STOP[2] + 2 && pendingWin >= 0) {
            win = pendingWin;
            pendingWin = -1;
        }
        // сервер отказал (нет фишек) — останавливаем анимацию
        if (spinning() && ticks - spinStart > 10 && pendingWin < 0 && win < 0 && ticks - spinStart > STOP[2]) spinStart = -1000;
        spinButton.active = !spinning();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderTransparentBackground(g);
        YukiTheme.roundedRect(g, x0 - 3, y0 - 3, W + 6, H + 6, 0xFF2A0804);
        g.fillGradient(x0, y0, x0 + W, y0 + H, 0xFF5A1404, 0xFF1A0402);
        // гирлянда огоньков по краю
        for (int i = 0; i < 24; i++) {
            boolean on = ((ticks / 4) + i) % 2 == 0;
            int c = on ? 0xFFFFE070 : 0xFF8A4A10;
            g.fill(x0 + 6 + i * 10, y0 + 3, x0 + 9 + i * 10, y0 + 6, c);
            g.fill(x0 + 6 + i * 10, y0 + H - 6, x0 + 9 + i * 10, y0 + H - 3, c);
        }
        g.drawCenteredString(font, title.copy().withStyle(ChatFormatting.BOLD), x0 + W / 2, y0 + 12, 0xFFFFD060);

        // барабаны
        float t = ticks + pt - spinStart;
        for (int r = 0; r < 3; r++) {
            int rx = x0 + 34 + r * 64, ry = y0 + 32;
            g.fill(rx - 3, ry - 3, rx + 55, ry + 69, 0xFFFFD060);
            g.fill(rx, ry, rx + 52, ry + 66, 0xFFF4ECDC);
            g.enableScissor(rx, ry, rx + 52, ry + 66);
            if (t < STOP[r]) {
                float roll = t * 0.9f + seed[r];
                int base = (int) Math.floor(roll);
                float frac = roll - base;
                for (int k = -1; k <= 1; k++) {
                    int sym = Math.floorMod(base + k + r * 2, 6);
                    int sy = ry + 17 + (int) ((k + frac) * 34) - 34;
                    g.blit(icon(sym), rx + 10, sy + 17, 0, 0, 32, 32, 32, 32);
                }
            } else {
                g.blit(icon(result[r]), rx + 10, ry + 17, 0, 0, 32, 32, 32, 32);
                g.blit(icon(Math.floorMod(result[r] - 1, 6)), rx + 14, ry - 14, 0, 0, 24, 24, 24, 24);
                g.blit(icon(Math.floorMod(result[r] + 1, 6)), rx + 14, ry + 56, 0, 0, 24, 24, 24, 24);
            }
            g.disableScissor();
            g.fill(rx, ry + 32, rx + 52, ry + 33, 0x60FF2000);
        }

        // ставка и фишки
        int chips = 0;
        if (minecraft != null && minecraft.player != null) {
            for (ItemStack s : minecraft.player.getInventory().items) if (s.is(ModItems.SINTARA_CHIP.get())) chips += s.getCount();
        }
        g.drawString(font, Component.translatable("casino.yukicore.bet", bet), x0 + 18, y0 + 116, 0xFFFFE6A0, true);
        g.drawString(font, Component.translatable("casino.yukicore.chips", chips), x0 + W - 112, y0 + 113, 0xFFFFE6A0, true);

        if (win >= 0) {
            Component msg = win == 0 ? Component.translatable("casino.yukicore.lose").withStyle(ChatFormatting.GRAY)
                    : result[0] == 5 && result[1] == 5 && result[2] == 5
                    ? Component.translatable("casino.yukicore.jackpot", win).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    : Component.translatable("casino.yukicore.win", win).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
            g.drawCenteredString(font, msg, x0 + W / 2, y0 + 104, 0xFFFFFFFF);
        }

        g.pose().pushPose();
        g.pose().translate(x0 + W / 2f, y0 + 152, 0);
        g.pose().scale(0.75f, 0.75f, 1);
        g.drawCenteredString(font, Component.translatable("casino.yukicore.table1"), 0, 0, 0xFFFFD8A0);
        g.drawCenteredString(font, Component.translatable("casino.yukicore.table1b"), 0, 10, 0xFFFFD8A0);
        g.drawCenteredString(font, Component.translatable("casino.yukicore.table2"), 0, 20, 0xFFFFD8A0);
        g.drawCenteredString(font, Component.translatable("casino.yukicore.fair"), 0, 32, 0xFFB08060);
        g.pose().popPose();
        super.render(g, mx, my, pt);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // фон рисуется в render(), иначе super.render() затёр бы панель
    }
}
