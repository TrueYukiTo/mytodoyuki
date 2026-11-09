package dev.yukito.yukicore.client;

import dev.yukito.yukicore.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Компактная панель: FPS, координаты, биом, игровой день. */
public final class YukiHud implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.SHOW_HUD.get() || mc.options.hideGui || mc.player == null || mc.level == null
                || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }

        Font font = mc.font;
        BlockPos pos = mc.player.blockPosition();
        List<Component> lines = new ArrayList<>();

        int fps = mc.getFps();
        int fpsColor = fps >= 60 ? 0x7CFFB2 : fps >= 30 ? 0xFFE27C : 0xFF7C8A;
        lines.add(Component.literal("FPS ").withColor(0x8FD8FF)
                .append(Component.literal(String.valueOf(fps)).withColor(fpsColor)));
        lines.add(Component.literal("XYZ ").withColor(0x8FD8FF)
                .append(Component.literal(pos.getX() + " " + pos.getY() + " " + pos.getZ()).withColor(0xF4FAFF)));

        if (Config.HUD_SHOW_BIOME.get()) {
            mc.level.getBiome(pos).unwrapKey().ifPresent(key -> {
                ResourceLocation id = key.location();
                lines.add(Component.translatable("hud.yukicore.biome").withColor(0x8FD8FF)
                        .append(Component.literal(" "))
                        .append(Component.translatable("biome." + id.getNamespace() + "." + id.getPath())
                                .withColor(0xFFB3E6)));
            });
        }

        long day = mc.level.getDayTime() / 24000L + 1;
        lines.add(Component.translatable("hud.yukicore.day", day).withColor(0xAFC4DA));

        int w = 0;
        for (Component c : lines) w = Math.max(w, font.width(c));
        // Правый верхний угол (слева сидит миникарта Xaero), ниже иконок эффектов
        boolean good = false, bad = false;
        for (var e : mc.player.getActiveEffects()) {
            if (!e.showIcon()) continue;
            if (e.getEffect().value().isBeneficial()) good = true; else bad = true;
        }
        int pad = 4, lh = 10;
        int panelW = w + pad * 2 + 3;
        int x = g.guiWidth() - panelW - 4;
        int y = 4 + (good ? 26 : 0) + (bad ? 26 : 0);
        int h = lines.size() * lh + pad * 2 - 2;

        YukiTheme.roundedRect(g, x, y, panelW, h, 0x90101828);
        g.fillGradient(x + 2, y + 3, x + 3, y + h - 3, YukiTheme.ICE, YukiTheme.SAKURA);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), x + pad + 3, y + pad + i * lh, 0xFFFFFFFF, true);
        }
    }
}
