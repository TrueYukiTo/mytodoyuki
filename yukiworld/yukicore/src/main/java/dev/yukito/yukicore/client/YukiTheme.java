package dev.yukito.yukicore.client;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** Общая палитра интерфейса YukiWorld. */
public final class YukiTheme {
    public static final int ICE = 0xFF8FD8FF;
    public static final int SAKURA = 0xFFFFB3E6;
    public static final int SNOW = 0xFFF4FAFF;
    public static final int NIGHT = 0xFF0B1020;
    public static final int PANEL = 0xB0101828;
    public static final int PANEL_BORDER = 0x6090C8FF;
    public static final int TEXT_DIM = 0xFFAFC4DA;

    private YukiTheme() {}

    public static int lerpColor(float t, int a, int b) {
        t = Mth.clamp(t, 0f, 1f);
        return FastColor.ARGB32.color(
                (int) Mth.lerp(t, FastColor.ARGB32.alpha(a), FastColor.ARGB32.alpha(b)),
                (int) Mth.lerp(t, FastColor.ARGB32.red(a), FastColor.ARGB32.red(b)),
                (int) Mth.lerp(t, FastColor.ARGB32.green(a), FastColor.ARGB32.green(b)),
                (int) Mth.lerp(t, FastColor.ARGB32.blue(a), FastColor.ARGB32.blue(b)));
    }

    public static int withAlpha(int color, int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    /** Прямоугольник со скруглёнными (срезанными) углами. */
    public static void roundedRect(net.minecraft.client.gui.GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** Рамка со скруглёнными углами толщиной 1px. */
    public static void roundedOutline(net.minecraft.client.gui.GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + 1, color);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
