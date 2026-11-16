package dev.yukito.yukicore.client.magic;

import dev.yukito.yukicore.client.YukiTheme;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.School;
import dev.yukito.yukicore.magic.Skill;
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
 * Гримуар стихий: дерево навыков школы (вкладки Юки / Синтара), 9 ступеней.
 * Навык открывается только после предков. ЛКМ — изучить/улучшить, ПКМ — в активную ячейку, клик по ячейке — выбрать её.
 */
public class GrimoireScreen extends Screen {
    private static final int W = 444, H = 286, NODE = 26;
    private static School tab = School.ICE;
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

    private int[] pos(Skill s) {
        return new int[]{x0 + 18 + s.column * 47, y0 + 74 + s.row * 50};
    }

    private int[] slotPos(int i) {
        int total = MagicData.SLOTS * 26;
        return new int[]{x0 + (W - total) / 2 + i * 26, y0 + H - 58};
    }

    private int[] tabPos(School s) {
        return new int[]{x0 + W / 2 + (s == School.ICE ? -112 : 8), y0 + 22, 104, 16};
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        Player pl = Minecraft.getInstance().player;
        if (pl == null) return;
        MagicData d = Magic.get(pl);
        float time = (Util.getMillis() % 1_000_000L) / 1000f;
        boolean ice = tab == School.ICE;

        YukiTheme.roundedRect(g, x0 - 2, y0 - 2, W + 4, H + 4, 0xF0060912);
        if (ice) g.fillGradient(x0, y0, x0 + W, y0 + H, 0xF00C1A36, 0xF0050A18);
        else g.fillGradient(x0, y0, x0 + W, y0 + H, 0xF0341008, 0xF0140604);
        particles(g, time, ice);
        YukiTheme.roundedOutline(g, x0 - 2, y0 - 2, W + 4, H + 4, ice ? 0x908FD8FF : 0x90FFA040);
        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire").withStyle(ChatFormatting.BOLD),
                x0 + W / 2, y0 + 7, 0xFFFFE6A0);

        for (School s : School.values()) {
            int[] t = tabPos(s);
            boolean on = s == tab, hov = in(mx, my, t[0], t[1], t[2], t[3]);
            g.fill(t[0], t[1], t[0] + t[2], t[1] + t[3], on ? (0xC0000000 | s.dark) : hov ? 0x60FFFFFF : 0x40000000);
            g.fill(t[0], t[1] + t[3] - 2, t[0] + t[2], t[1] + t[3], on ? (0xFF000000 | s.color) : 0x40FFFFFF);
            g.drawCenteredString(font, Component.translatable("magic.yukicore.school." + s.id), t[0] + t[2] / 2, t[1] + 4,
                    on ? 0xFFFFFFFF : 0xFFAFC4DA);
        }

        int lvl = d.level(tab);
        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire.level", lvl, d.points(tab)),
                x0 + W / 2, y0 + 44, 0xFFE8EEF8);
        int bw = 220, bx = x0 + (W - bw) / 2, by = y0 + 56;
        float k = lvl >= MagicData.MAX_LEVEL ? 1 : d.xp[tab.ordinal()] / (float) MagicData.xpToNext(lvl);
        g.fill(bx - 1, by - 1, bx + bw + 1, by + 5, 0xFF05080F);
        g.fillGradient(bx, by, bx + (int) (bw * k), by + 4, 0xFF000000 | tab.color, 0xFF000000 | tab.dark);

        // связи
        for (Skill s : Skill.values()) {
            if (s.school != tab) continue;
            int[] c = pos(s);
            for (Skill par : s.parents()) {
                int[] a = pos(par);
                boolean lit = d.rank(par) > 0;
                line(g, a[0] + NODE / 2, a[1] + NODE / 2, c[0] + NODE / 2, c[1] + NODE / 2,
                        lit ? (0xFF000000 | tab.color) : 0x50FFFFFF, lit);
            }
        }
        List<FormattedCharSequence> tip = null;
        for (Skill s : Skill.values()) {
            if (s.school != tab) continue;
            int[] c = pos(s);
            boolean hover = in(mx, my, c[0], c[1], NODE, NODE);
            node(g, d, s, c[0], c[1], hover, time);
            if (hover) tip = tip(d, s);
        }

        // ячейки
        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire.slots"), x0 + W / 2, y0 + H - 70, 0xFF8FA0B8);
        for (int i = 0; i < MagicData.SLOTS; i++) {
            int[] c = slotPos(i);
            Skill s = d.slotSkill(i);
            boolean active = i == d.activeSlot;
            g.fill(c[0] - 1, c[1] - 1, c[0] + 23, c[1] + 23, active ? 0xFFFFE6A0 : 0xFF3A4458);
            g.fill(c[0], c[1], c[0] + 22, c[1] + 22, 0xFF0A0E18);
            if (s != null) g.blit(MagicHud.icon(s.id()), c[0] + 3, c[1] + 3, 0, 0, 16, 16, 16, 16);
            g.drawString(font, String.valueOf(i + 1), c[0] + 1, c[1] + 1, 0x80FFFFFF, false);
            if (s != null && in(mx, my, c[0], c[1], 22, 22)) tip = tip(d, s);
        }

        Component foot = Component.translatable("screen.yukicore.grimoire.footer", (int) d.mana, (int) Magic.maxMana(pl),
                d.crystals, Component.literal(d.trial[0] ? "✔" : "✘").withStyle(d.trial[0] ? ChatFormatting.GREEN : ChatFormatting.RED),
                Component.literal(d.trial[1] ? "✔" : "✘").withStyle(d.trial[1] ? ChatFormatting.GREEN : ChatFormatting.RED));
        g.drawCenteredString(font, foot, x0 + W / 2, y0 + H - 26, 0xFFDDE6F4);
        g.pose().pushPose();
        g.pose().translate(x0 + W / 2f, y0 + H - 13, 0);
        g.pose().scale(0.8f, 0.8f, 1);
        g.drawCenteredString(font, Component.translatable("screen.yukicore.grimoire.hint"), 0, 0, 0xFF8A98B0);
        g.pose().popPose();

        if (tip != null) g.renderTooltip(font, tip, mx, my);
    }

    private void node(GuiGraphics g, MagicData d, Skill s, int x, int y, boolean hover, float time) {
        int rank = d.rank(s);
        boolean can = d.canUpgrade(s);
        boolean open = d.unlocked(s);
        int col = 0xFF000000 | s.school.color;
        int border = rank > 0 ? 0xFFFFD060 : can ? col : open ? 0xFF5A6478 : 0xFF2A3040;
        if (can) border = YukiTheme.lerpColor(0.5f + 0.5f * Mth.sin(time * 4), border, 0xFFFFFFFF);
        boolean inSlot = false;
        for (int i : d.slots) if (i == s.ordinal()) inSlot = true;
        if (inSlot) g.fill(x - 3, y - 3, x + NODE + 3, y + NODE + 3, 0x50FFFFFF);
        if (s.passive()) {
            // пассивки — «ромбики» поменьше
            g.fill(x + 1, y + 1, x + NODE - 1, y + NODE - 1, border);
            g.fill(x + 2, y + 2, x + NODE - 2, y + NODE - 2, hover ? 0xFF1A2438 : 0xFF0A0E18);
        } else {
            g.fill(x - 1, y - 1, x + NODE + 1, y + NODE + 1, border);
            g.fill(x, y, x + NODE, y + NODE, hover ? 0xFF1A2438 : 0xFF0A0E18);
        }
        g.blit(MagicHud.icon(s.id()), x + 3, y + 3, 0, 0, 20, 20, 20, 20);
        if (rank == 0) g.fill(x, y, x + NODE, y + NODE, open ? 0x90000000 : 0xC8000000);
        if (s.ultimate()) g.drawString(font, "★", x + NODE - 5, y - 4, 0xFFFFD060, true);
        for (int i = 0; i < Skill.MAX_RANK; i++) {
            int px = x + 1 + i * 5;
            g.fill(px, y + NODE + 3, px + 4, y + NODE + 5, i < rank ? 0xFFFFD060 : 0xFF2A3040);
        }
    }

    private List<FormattedCharSequence> tip(MagicData d, Skill s) {
        List<FormattedCharSequence> out = new ArrayList<>();
        int r = d.rank(s), i = s.school.ordinal();
        ChatFormatting c = s.school == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD;
        out.add(Component.translatable("spell.yukicore." + s.id()).withStyle(c, ChatFormatting.BOLD).getVisualOrderText());
        out.add(Component.translatable("screen.yukicore.grimoire.kind." + s.kind.name().toLowerCase(java.util.Locale.ROOT))
                .withStyle(ChatFormatting.DARK_PURPLE).getVisualOrderText());
        out.addAll(font.split(Component.translatable("spell.yukicore." + s.id() + ".desc").withStyle(ChatFormatting.GRAY), 230));
        out.add(Component.translatable("screen.yukicore.grimoire.rank", r, Skill.MAX_RANK).withStyle(ChatFormatting.YELLOW).getVisualOrderText());
        Player pl = Minecraft.getInstance().player;
        if (!s.passive() && pl != null) {
            out.add(Component.translatable("screen.yukicore.grimoire.cost", Magic.cost(pl, s),
                    String.format("%.1f", Magic.cooldown(pl, s) / 20f)).withStyle(ChatFormatting.BLUE).getVisualOrderText());
        }
        if (r < Skill.MAX_RANK) {
            for (Skill p : s.parents()) {
                if (d.rank(p) <= 0) {
                    out.add(Component.translatable("screen.yukicore.grimoire.need_parent",
                            Component.translatable("spell.yukicore." + p.id())).withStyle(ChatFormatting.RED).getVisualOrderText());
                }
            }
            int need = s.requiredLevel(r);
            out.add(Component.translatable("screen.yukicore.grimoire.need_level", need)
                    .withStyle(d.level[i] >= need ? ChatFormatting.GREEN : ChatFormatting.RED).getVisualOrderText());
            if (s.ultimate() && !d.trial[i]) {
                out.add(Component.translatable("screen.yukicore.grimoire.need_trial." + s.school.id)
                        .withStyle(ChatFormatting.RED).getVisualOrderText());
            }
            out.add(Component.translatable("screen.yukicore.grimoire.click_upgrade", s.upgradeCost(r))
                    .withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        }
        if (r > 0 && !s.passive()) {
            out.add(Component.translatable("screen.yukicore.grimoire.click_select").withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        }
        return out;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (School s : School.values()) {
            int[] t = tabPos(s);
            if (in(mx, my, t[0], t[1], t[2], t[3])) {
                tab = s;
                return true;
            }
        }
        Player pl = Minecraft.getInstance().player;
        for (Skill s : Skill.values()) {
            if (s.school != tab) continue;
            int[] c = pos(s);
            if (!in(mx, my, c[0], c[1], NODE, NODE)) continue;
            if (button == 1 && !s.passive()) {
                if (pl != null) {
                    MagicData d = Magic.get(pl);
                    if (d.rank(s) > 0) PacketDistributor.sendToServer(new MagicNet.Assign(d.activeSlot, s.ordinal()));
                }
            } else if (button == 0) {
                PacketDistributor.sendToServer(new MagicNet.Upgrade(s.ordinal()));
            }
            return true;
        }
        for (int i = 0; i < MagicData.SLOTS; i++) {
            int[] c = slotPos(i);
            if (in(mx, my, c[0], c[1], 22, 22)) {
                if (pl != null) Magic.get(pl).activeSlot = i;
                PacketDistributor.sendToServer(new MagicNet.Assign(i, -1));
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

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void particles(GuiGraphics g, float time, boolean ice) {
        for (int i = 0; i < 60; i++) {
            float h1 = hash(i * 7 + 1), h2 = hash(i * 13 + 5), h3 = hash(i * 3 + 11);
            int px = x0 + 2 + (int) (h1 * (W - 4));
            float off = (time * (8 + h3 * 14) + h2 * H) % H;
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

    private static void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color, boolean thick) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i += thick ? 1 : 2) {
            int x = x1 + (x2 - x1) * i / Math.max(1, steps), y = y1 + (y2 - y1) * i / Math.max(1, steps);
            g.fill(x, y, x + 1, y + (thick ? 2 : 1), color);
        }
    }
}
