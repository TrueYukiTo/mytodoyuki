package dev.yukito.yukicore.client;

import dev.yukito.yukicore.Config;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ModListScreen;

/** Главное меню YukiWorld: панорама, градиент, снег, переливающийся логотип. */
public class YukiTitleScreen extends Screen {
    private static final String LOGO = "YukiWorld";
    private static final String PACK_VERSION = "1.1.0";

    private final Snowfall snow = new Snowfall();
    private long openedAt;

    public YukiTitleScreen() {
        super(Component.translatable("narrator.screen.title"));
    }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        snow.resize(width, height);

        int bw = 200, bh = 22, gap = 6;
        int x = width / 2 - bw / 2;
        int y = height / 2 - 12;

        addRenderableWidget(new YukiButton(x, y, bw, bh, Component.translatable("menu.singleplayer"),
                b -> minecraft.setScreen(new SelectWorldScreen(this))));
        y += bh + gap;
        addRenderableWidget(new YukiButton(x, y, bw, bh, Component.translatable("menu.multiplayer"),
                b -> minecraft.setScreen(minecraft.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(this) : new SafetyScreen(this))));
        y += bh + gap;
        int half = (bw - gap) / 2;
        addRenderableWidget(new YukiButton(x, y, half, bh, Component.translatable("fml.menu.mods"),
                b -> minecraft.setScreen(new ModListScreen(this))));
        addRenderableWidget(new YukiButton(x + half + gap, y, half, bh, Component.translatable("menu.options"),
                b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
        y += bh + gap;
        addRenderableWidget(new YukiButton(x, y, bw, bh, Component.translatable("menu.quit"),
                b -> minecraft.stop()));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderPanorama(g, partialTick);
        // Ночной градиент поверх панорамы
        g.fillGradient(0, 0, width, height, 0x70101830, 0xE00B1020);
        if (Config.SNOWFALL.get()) {
            snow.render(g);
        }
        // Подложка под кнопки (рисуется до виджетов)
        float fade = Mth.clamp((System.currentTimeMillis() - openedAt) / 700f, 0f, 1f);
        int pw = 220, ph = 4 * 28 + 12;
        int px = width / 2 - pw / 2, py = height / 2 - 22;
        YukiTheme.roundedRect(g, px, py, pw, ph, YukiTheme.withAlpha(0x0B1020, (int) (110 * fade)));
        YukiTheme.roundedOutline(g, px, py, pw, ph, YukiTheme.withAlpha(0x90C8FF, (int) (60 * fade)));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        float fade = Mth.clamp((System.currentTimeMillis() - openedAt) / 700f, 0f, 1f);
        float t = (System.currentTimeMillis() % 100_000L) / 1000f;

        renderLogo(g, t, fade);

        int dim = YukiTheme.withAlpha(0xAFC4DA, (int) (255 * fade));
        String left = "YukiWorld " + PACK_VERSION + "  |  Minecraft "
                + SharedConstants.getCurrentVersion().getName() + "  |  NeoForge";
        g.drawString(font, left, 4, height - 20, dim, true);
        g.drawString(font, Component.translatable("menu.yukicore.mods_loaded", ModList.get().size()),
                4, height - 10, dim, true);

        String copy = "Copyright Mojang AB. Do not distribute!";
        g.drawString(font, copy, width - font.width(copy) - 4, height - 10, dim, true);
    }

    private void renderLogo(GuiGraphics g, float t, float fade) {
        float scale = Math.min(4.0f, width / 160f);
        float bob = Mth.sin(t * 1.6f) * 2.0f;
        int logoW = font.width(LOGO);
        float lx = width / 2f - logoW * scale / 2f;
        float ly = height / 2f - 70 - 8 * scale / 2f + bob;

        g.pose().pushPose();
        g.pose().translate(lx, ly, 0);
        g.pose().scale(scale, scale, 1);
        float cx = 0;
        for (int i = 0; i < LOGO.length(); i++) {
            String ch = String.valueOf(LOGO.charAt(i));
            float wave = (Mth.sin(t * 2.2f - i * 0.55f) + 1f) / 2f;
            int color = YukiTheme.lerpColor(wave, YukiTheme.ICE, YukiTheme.SAKURA);
            g.drawString(font, ch, cx, 0, YukiTheme.withAlpha(color, (int) (255 * fade)), true);
            cx += font.width(ch);
        }
        g.pose().popPose();

        Component sub = Component.translatable("menu.yukicore.subtitle");
        int sw = font.width(sub);
        g.drawString(font, sub, width / 2 - sw / 2, (int) (ly + 8 * scale + 6),
                YukiTheme.withAlpha(0xF4FAFF, (int) (200 * fade)), true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
