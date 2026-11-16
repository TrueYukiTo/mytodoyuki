package dev.yukito.yukicore.client.magic;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.client.YukiTheme;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.School;
import dev.yukito.yukicore.magic.Spell;
import dev.yukito.yukicore.magic.item.FocusItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Панель магии у хотбара: выбранное заклинание с откатом и полоса маны. */
public final class MagicHud implements LayeredDraw.Layer {
    public static ResourceLocation icon(String id) {
        return YukiCore.id("textures/gui/spells/" + id + ".png");
    }

    @Override
    public void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.level == null || mc.options.hideGui || p.isSpectator()) return;
        MagicData d = Magic.get(p);
        boolean holding = p.getMainHandItem().getItem() instanceof FocusItem || p.getOffhandItem().getItem() instanceof FocusItem;
        if (!d.hasAnySpell() && !holding) return;

        Font font = mc.font;
        int cx = g.guiWidth() / 2;
        int w = 128, h = 30;
        int x = cx - 91 - 34 - w;
        int y = g.guiHeight() - h - 1;
        if (x < 4) {
            x = cx - 91;
            y = g.guiHeight() - 22 - 30 - h - (p.getArmorValue() > 0 ? 10 : 0);
        }

        Spell s = d.selectedSpell();
        int accent = s == null ? 0xFF8FD8FF : (0xFF000000 | s.school.color);
        YukiTheme.roundedRect(g, x, y, w, h, 0xA0101828);
        g.fillGradient(x + 1, y + 2, x + 2, y + h - 2, accent, YukiTheme.SAKURA);

        // иконка заклинания с откатом
        int ix = x + 5, iy = y + 5;
        g.fill(ix - 1, iy - 1, ix + 21, iy + 21, 0xFF05080F);
        if (s != null) {
            g.blit(icon(s.id()), ix, iy, 0, 0, 20, 20, 20, 20);
            long now = mc.level.getGameTime();
            long left = d.cooldownUntil[s.ordinal()] - now;
            if (left > 0) {
                float k = Math.min(1f, left / (float) s.cooldown(Math.max(1, d.rank(s))));
                g.fill(ix, iy + (int) (20 * (1 - k)), ix + 20, iy + 20, 0xB0000000);
                String sec = left >= 20 ? String.valueOf(left / 20) : String.format("%.1f", left / 20f);
                g.drawCenteredString(font, sec, ix + 10, iy + 6, 0xFFFFFFFF);
            }
        } else {
            g.drawCenteredString(font, "?", ix + 10, iy + 6, 0xFF8FA0B8);
        }

        // название и мана
        int tx = ix + 25;
        Component name = s == null ? Component.translatable("magic.yukicore.hud.none")
                : Component.translatable("spell.yukicore." + s.id());
        String nm = font.plainSubstrByWidth(name.getString(), w - 30);
        g.drawString(font, nm, tx, y + 5, s == null ? 0xFFAFC4DA : (0xFF000000 | (s.school == School.ICE ? 0xBFEFFF : 0xFFC080)), true);
        float max = Magic.maxMana(p);
        float k = Math.max(0, Math.min(1, d.mana / max));
        int bx = tx, by = y + 17, bw = w - 30, bh = 7;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF05080F);
        g.fillGradient(bx, by, bx + (int) (bw * k), by + bh, 0xFF6EC6FF, 0xFF3A50D8);
        if (k > 0) g.fill(bx, by, bx + (int) (bw * k), by + 1, 0x80FFFFFF);
        String mana = (int) d.mana + "/" + (int) max;
        g.pose().pushPose();
        g.pose().translate(bx + bw / 2f, by + 0.5f, 0);
        g.pose().scale(0.75f, 0.75f, 1);
        g.drawCenteredString(font, mana, 0, 0, 0xFFFFFFFF);
        g.pose().popPose();
    }
}
