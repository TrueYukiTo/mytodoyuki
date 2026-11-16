package dev.yukito.yukicore.client.magic;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.client.YukiTheme;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.School;
import dev.yukito.yukicore.magic.Skill;
import dev.yukito.yukicore.magic.item.FocusItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Панель магии у хотбара: 6 ячеек заклинаний с перезарядкой, название активного и полоса маны. */
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
        int cell = 18, w = MagicData.SLOTS * (cell + 2) + 6, h = 40;
        int x = g.guiWidth() / 2 - 91 - 34 - w;
        int y = g.guiHeight() - h - 1;
        if (x < 4) {
            x = g.guiWidth() / 2 - 91;
            y = g.guiHeight() - 22 - 30 - h - (p.getArmorValue() > 0 ? 10 : 0);
        }
        YukiTheme.roundedRect(g, x, y, w, h, 0xA0101828);
        long now = mc.level.getGameTime();
        for (int i = 0; i < MagicData.SLOTS; i++) {
            int cx = x + 4 + i * (cell + 2), cy = y + 4;
            Skill s = d.slotSkill(i);
            boolean active = i == d.activeSlot;
            int border = active ? (s == null ? 0xFFFFFFFF : 0xFF000000 | s.school.color) : 0xFF2A3448;
            g.fill(cx - 1, cy - 1, cx + cell + 1, cy + cell + 1, border);
            g.fill(cx, cy, cx + cell, cy + cell, 0xFF05080F);
            if (s == null) continue;
            g.blit(icon(s.id()), cx + 1, cy + 1, 0, 0, cell - 2, cell - 2, cell - 2, cell - 2);
            long left = d.cooldownUntil[s.ordinal()] - now;
            if (left > 0) {
                float k = Math.min(1f, left / (float) Math.max(1, Magic.cooldown(p, s)));
                g.fill(cx, cy + (int) (cell * (1 - k)), cx + cell, cy + cell, 0xB0000000);
                g.pose().pushPose();
                g.pose().translate(cx + cell / 2f, cy + 6, 200);
                g.pose().scale(0.7f, 0.7f, 1);
                g.drawCenteredString(font, left >= 20 ? String.valueOf(left / 20) : "<1", 0, 0, 0xFFFFFFFF);
                g.pose().popPose();
            }
        }
        Skill s = d.activeSkill();
        Component name = s == null ? Component.translatable("magic.yukicore.hud.none")
                : Component.translatable("spell.yukicore." + s.id());
        float max = Magic.maxMana(p);
        String mana = (int) d.mana + "/" + (int) max;
        int manaW = (int) (font.width(mana) * 0.6f) + 3;
        g.pose().pushPose();
        g.pose().translate(x + 4, y + cell + 7, 0);
        g.pose().scale(0.75f, 0.75f, 1);
        g.drawString(font, font.plainSubstrByWidth(name.getString(), (int) ((w - 8 - manaW) / 0.75f)), 0, 0,
                s == null ? 0xFFAFC4DA : (s.school == School.ICE ? 0xFFBFEFFF : 0xFFFFC080), true);
        g.pose().popPose();
        float k = Math.max(0, Math.min(1, d.mana / max));
        int bx = x + 4, by = y + h - 9, bw = w - 8, bh = 5;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF05080F);
        g.fillGradient(bx, by, bx + (int) (bw * k), by + bh, 0xFF6EC6FF, 0xFF3A50D8);
        g.pose().pushPose();
        g.pose().translate(bx + bw - font.width(mana) * 0.6f, y + cell + 7.5f, 0);
        g.pose().scale(0.6f, 0.6f, 1);
        g.drawString(font, mana, 0, 0, 0xFFB8D0FF, true);
        g.pose().popPose();
    }
}
