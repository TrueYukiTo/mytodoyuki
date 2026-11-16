package dev.yukito.yukicore.client.magic;

import dev.yukito.yukicore.client.YukiTheme;
import dev.yukito.yukicore.magic.*;
import dev.yukito.yukicore.magic.net.MagicNet;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Гримуар стихий: дерево навыков двух школ. Слева — лёд Юки, справа — пламя Синтары.
 * ЛКМ по узлу — изучить/улучшить, ПКМ — выбрать заклинание для клавиши «Сотворить».
 */
public class GrimoireScreen extends Screen {
    private static final int W = 400, H = 262, NODE = 28;
    private int x0, y0;

    public GrimoireScreen() {
        super(Component.translatable("screen.yukicore.grimoire"));
    }

    @Override
    protected void init() {
        x0 = (width - W) / 2;
        y0 = (height - H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int colX(School s) {
        return x0 + (s == School.ICE ? W / 4 : W * 3 / 4);
    }

    private int[] spellPos(Spell s) {
        int i = s.ordinal() % 6;
        int cx = colX(s.school);
        return new int[]{cx + (i % 2 == 0 ? -36 : 36) - NODE / 2, y0 + 66 + (i / 2) * 40};
    }

    private int[] passivePos(Passive p) {
        int i = p.ordinal() % 2;
        return new int[]{colX(p.school) + (i == 0 ? -36 : 36) - 11, y0 + 190};
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        Player pl = Minecraft.getInstance().player;
        if (pl == null) return;
        MagicData d = Magic.get(pl);
        float time = (Util.getMillis() % 1_000_000L) / 1000f;

        // фон: лёд слева, пламя справа
        YukiTheme.roundedRect(g, x0 - 2, y0 - 2, W + 4, H + 4, 0xF0060912);
        g.fillGradient(x0, y0, x0 + W / 2, y0 + H, 0xF00C1A36, 0xF0081022);
        g.fillGradient(x0 + W / 2, y0, x0 + W, y0 + H, 0xF0301008, 0xF0180806);
        particles(g, time);
        g.fillGradient(x0 + W / 2, y0 + 20, x0 + W / 2 + 1, y0 + H - 20, 0xFF8FD8FF, 0xFFFFA040);
        YukiTheme.roundedOutline(g, x0 - 2, y0 - 2, W + 4, H + 4, 0x90C8A0FF);

        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire").withStyle(ChatFormatting.BOLD),
                x0 + W / 2, y0 + 6, 0xFFFFE6A0);

        for (School s : School.values()) header(g, d, s);

        // связи между узлами
        for (School s : School.values()) {
            for (int i = 0; i < 5; i++) {
                Spell a = Spell.values()[s.ordinal() * 6 + i], b = Spell.values()[s.ordinal() * 6 + i + 1];
                int[] pa = spellPos(a), pb = spellPos(b);
                boolean lit = d.rank(a) > 0 && d.rank(b) > 0;
                line(g, pa[0] + NODE / 2, pa[1] + NODE / 2, pb[0] + NODE / 2, pb[1] + NODE / 2,
                        lit ? (0xFF000000 | s.color) : 0x60FFFFFF);
            }
        }

        List<FormattedCharSequence> tip = null;
        for (Spell s : Spell.values()) {
            int[] p = spellPos(s);
            boolean hover = mx >= p[0] && mx < p[0] + NODE && my >= p[1] && my < p[1] + NODE;
            node(g, d, s, p[0], p[1], hover, time);
            if (hover) tip = spellTip(d, s);
        }
        for (Passive ps : Passive.values()) {
            int[] p = passivePos(ps);
            boolean hover = mx >= p[0] && mx < p[0] + 22 && my >= p[1] && my < p[1] + 22;
            passive(g, d, ps, p[0], p[1], hover);
            if (hover) tip = passiveTip(d, ps);
        }

        // нижняя строка
        Component foot = Component.translatable("screen.yukicore.grimoire.footer", (int) d.mana, (int) Magic.maxMana(pl),
                d.crystals, Component.literal(d.trial[0] ? "✔" : "✘").withStyle(d.trial[0] ? ChatFormatting.GREEN : ChatFormatting.RED),
                Component.literal(d.trial[1] ? "✔" : "✘").withStyle(d.trial[1] ? ChatFormatting.GREEN : ChatFormatting.RED));
        g.fill(x0 + 8, y0 + H - 30, x0 + W - 8, y0 + H - 29, 0x40FFFFFF);
        g.drawCenteredString(font, foot, x0 + W / 2, y0 + H - 25, 0xFFDDE6F4);
        g.pose().pushPose();
        g.pose().translate(x0 + W / 2f, y0 + H - 12, 0);
        g.pose().scale(0.8f, 0.8f, 1);
        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire.hint"), 0, 0, 0xFF8A98B0);
        g.pose().popPose();

        if (tip != null) g.renderTooltip(font, tip, mx, my);
    }

    private void header(GuiGraphics g, MagicData d, School s) {
        int cx = colX(s);
        boolean ice = s == School.ICE;
        g.drawCenteredString(font, Component.translatable("magic.yukicore.school." + s.id).withStyle(ChatFormatting.BOLD),
                cx, y0 + 22, ice ? 0xFFBFEFFF : 0xFFFFC080);
        int lvl = d.level(s);
        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire.level", lvl, d.points[s.ordinal()]),
                cx, y0 + 34, 0xFFE8EEF8);
        int bw = 140, bx = cx - bw / 2, by = y0 + 46;
        float k = lvl >= MagicData.MAX_LEVEL ? 1 : d.xp[s.ordinal()] / (float) MagicData.xpToNext(lvl);
        g.fill(bx - 1, by - 1, bx + bw + 1, by + 5, 0xFF05080F);
        g.fillGradient(bx, by, bx + (int) (bw * k), by + 4, 0xFF000000 | s.color, 0xFF000000 | s.dark);
    }

    private void node(GuiGraphics g, MagicData d, Spell s, int x, int y, boolean hover, float time) {
        int rank = d.rank(s);
        boolean can = canUpgrade(d, s);
        int border = rank > 0 ? 0xFFFFD060 : can ? (0xFF000000 | s.school.color) : 0xFF3A4458;
        if (can) {
            float pulse = 0.5f + 0.5f * Mth.sin(time * 4);
            border = YukiTheme.lerpColor(pulse, border, 0xFFFFFFFF);
        }
        if (d.selected == s.ordinal()) g.fill(x - 3, y - 3, x + NODE + 3, y + NODE + 3, 0x70FFFFFF);
        g.fill(x - 1, y - 1, x + NODE + 1, y + NODE + 1, border);
        g.fill(x, y, x + NODE, y + NODE, hover ? 0xFF1A2438 : 0xFF0A0E18);
        g.blit(MagicHud.icon(s.id()), x + 4, y + 4, 0, 0, 20, 20, 20, 20);
        if (rank == 0) g.fill(x, y, x + NODE, y + NODE, 0xA0000000);
        if (s.ultimate()) g.drawString(font, "★", x + NODE - 6, y - 4, 0xFFFFD060, true);
        for (int i = 0; i < Spell.MAX_RANK; i++) {
            int px = x + 2 + i * 5;
            g.fill(px, y + NODE + 3, px + 4, y + NODE + 6, i < rank ? 0xFFFFD060 : 0xFF2A3040);
        }
    }

    private void passive(GuiGraphics g, MagicData d, Passive p, int x, int y, boolean hover) {
        int rank = d.passive(p);
        int border = rank > 0 ? 0xFFFFD060 : d.points[p.school.ordinal()] > 0 ? (0xFF000000 | p.school.color) : 0xFF3A4458;
        g.fill(x - 1, y - 1, x + 23, y + 23, border);
        g.fill(x, y, x + 22, y + 22, hover ? 0xFF1A2438 : 0xFF0A0E18);
        g.blit(MagicHud.icon(p.id()), x + 3, y + 3, 0, 0, 16, 16, 16, 16);
        g.drawCenteredString(font, rank + "/" + Passive.MAX_RANK, x + 11, y + 25, 0xFFAFC4DA);
    }

    private boolean canUpgrade(MagicData d, Spell s) {
        int r = d.rank(s), i = s.school.ordinal();
        return r < Spell.MAX_RANK && d.points[i] >= s.upgradeCost(r) && d.level[i] >= s.requiredLevel + r * 2
                && (!s.ultimate() || d.trial[i]);
    }

    private List<FormattedCharSequence> spellTip(MagicData d, Spell s) {
        List<FormattedCharSequence> out = new ArrayList<>();
        int r = d.rank(s), i = s.school.ordinal();
        ChatFormatting c = s.school == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD;
        out.add(Component.translatable("spell.yukicore." + s.id()).withStyle(c, ChatFormatting.BOLD).getVisualOrderText());
        out.addAll(font.split(Component.translatable("spell.yukicore." + s.id() + ".desc").withStyle(ChatFormatting.GRAY), 220));
        out.add(Component.translatable("screen.yukicore.grimoire.rank", r, Spell.MAX_RANK).withStyle(ChatFormatting.YELLOW).getVisualOrderText());
        int shown = Math.max(1, r);
        out.add(Component.translatable("screen.yukicore.grimoire.cost", s.cost(shown), String.format("%.1f", s.cooldown(shown) / 20f))
                .withStyle(ChatFormatting.BLUE).getVisualOrderText());
        if (r < Spell.MAX_RANK) {
            int need = s.requiredLevel + r * 2;
            out.add(Component.translatable("screen.yukicore.grimoire.need_level", need)
                    .withStyle(d.level[i] >= need ? ChatFormatting.GREEN : ChatFormatting.RED).getVisualOrderText());
            if (s.ultimate() && !d.trial[i]) {
                out.add(Component.translatable("screen.yukicore.grimoire.need_trial." + s.school.id)
                        .withStyle(ChatFormatting.RED).getVisualOrderText());
            }
            out.add(Component.translatable("screen.yukicore.grimoire.click_upgrade", s.upgradeCost(r))
                    .withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        }
        if (r > 0) out.add(Component.translatable("screen.yukicore.grimoire.click_select").withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        return out;
    }

    private List<FormattedCharSequence> passiveTip(MagicData d, Passive p) {
        List<FormattedCharSequence> out = new ArrayList<>();
        out.add(Component.translatable("passive.yukicore." + p.id()).withStyle(
                p.school == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD, ChatFormatting.BOLD).getVisualOrderText());
        out.addAll(font.split(Component.translatable("passive.yukicore." + p.id() + ".desc").withStyle(ChatFormatting.GRAY), 220));
        out.add(Component.translatable("screen.yukicore.grimoire.rank", d.passive(p), Passive.MAX_RANK)
                .withStyle(ChatFormatting.YELLOW).getVisualOrderText());
        out.add(Component.translatable("screen.yukicore.grimoire.click_upgrade", 1).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        return out;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (Spell s : Spell.values()) {
            int[] p = spellPos(s);
            if (mx >= p[0] && mx < p[0] + NODE && my >= p[1] && my < p[1] + NODE) {
                if (button == 1) {
                    PacketDistributor.sendToServer(new MagicNet.Select(s.ordinal()));
                    Player pl = Minecraft.getInstance().player;
                    if (pl != null && Magic.get(pl).rank(s) > 0) Magic.get(pl).selected = s.ordinal();
                } else {
                    PacketDistributor.sendToServer(new MagicNet.Upgrade(0, s.ordinal()));
                }
                return true;
            }
        }
        for (Passive ps : Passive.values()) {
            int[] p = passivePos(ps);
            if (mx >= p[0] && mx < p[0] + 22 && my >= p[1] && my < p[1] + 22) {
                PacketDistributor.sendToServer(new MagicNet.Upgrade(1, ps.ordinal()));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ClientMagic.GRIMOIRE.matches(key, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    private void particles(GuiGraphics g, float time) {
        for (int i = 0; i < 46; i++) {
            float h1 = hash(i * 7 + 1), h2 = hash(i * 13 + 5), h3 = hash(i * 3 + 11);
            boolean ice = i % 2 == 0;
            int px = x0 + (ice ? 0 : W / 2) + (int) (h1 * (W / 2 - 4)) + 2;
            float speed = 8 + h3 * 14;
            float off = (time * speed + h2 * H) % H;
            int py = ice ? y0 + (int) off : y0 + H - (int) off;
            int size = h3 > 0.7f ? 2 : 1;
            int col = ice ? 0x90E8F6FF : (h3 > 0.5f ? 0xA0FFB040 : 0x90FF6A20);
            g.fill(px, py, px + size, py + size, col);
        }
    }

    private static float hash(int n) {
        n = (n << 13) ^ n;
        return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / (float) 0x7fffffff;
    }

    private static void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i += 2) {
            int x = x1 + (x2 - x1) * i / Math.max(1, steps), y = y1 + (y2 - y1) * i / Math.max(1, steps);
            g.fill(x, y, x + 1, y + 1, color);
        }
    }
}
