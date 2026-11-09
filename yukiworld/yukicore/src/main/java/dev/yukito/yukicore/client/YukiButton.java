package dev.yukito.yukicore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Полупрозрачная «ледяная» кнопка с плавной подсветкой при наведении. */
public class YukiButton extends Button {
    private float hover = 0f;
    private long lastFrame = System.nanoTime();

    public YukiButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();
        float dt = Math.min((now - lastFrame) / 1_000_000_000f, 0.1f);
        lastFrame = now;
        float target = this.active && this.isHoveredOrFocused() ? 1f : 0f;
        hover = Mth.clamp(hover + (target - hover) * Math.min(1f, dt * 12f), 0f, 1f);

        int x = getX(), y = getY(), w = width, h = height;
        int alpha = (int) (this.alpha * 255);

        int bg = YukiTheme.lerpColor(hover, 0xA0101828, 0xD0203250);
        int border = YukiTheme.lerpColor(hover, YukiTheme.PANEL_BORDER, YukiTheme.ICE);
        YukiTheme.roundedRect(g, x, y, w, h, scaleAlpha(bg, alpha));
        YukiTheme.roundedOutline(g, x, y, w, h, scaleAlpha(border, alpha));

        // Акцентная полоска слева, растёт при наведении
        int accentH = (int) ((h - 6) * (0.35f + 0.65f * hover));
        int accentY = y + (h - accentH) / 2;
        g.fillGradient(x + 3, accentY, x + 5, accentY + accentH,
                scaleAlpha(YukiTheme.ICE, alpha), scaleAlpha(YukiTheme.SAKURA, alpha));

        Font font = Minecraft.getInstance().font;
        int textColor = !this.active ? 0xFF6A7A8C : YukiTheme.lerpColor(hover, YukiTheme.TEXT_DIM, YukiTheme.SNOW);
        int offset = (int) (hover * 3);
        g.drawCenteredString(font, getMessage(), x + w / 2 + offset, y + (h - 8) / 2, scaleAlpha(textColor, alpha));
    }

    private static int scaleAlpha(int color, int alpha) {
        int a = ((color >>> 24) * alpha) / 255;
        return YukiTheme.withAlpha(color, a);
    }
}
