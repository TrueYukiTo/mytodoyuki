package dev.yukito.yukicore.client;

import dev.yukito.yukicore.magic.net.MagicNet;
import dev.yukito.yukicore.world.Contracts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Доска Синтары: задания дня и Лавка за фишки. */
public class BoardScreen extends Screen {
    private static final int W = 300, H = 236, PER_PAGE = 10;
    private static int tab, page;
    private final BlockPos pos;
    private int x0, y0;
    private int[] state;

    public BoardScreen(BlockPos pos) {
        super(Component.translatable("board.yukicore.title"));
        this.pos = pos;
    }

    public static void open(BlockPos pos) {
        BoardScreen s = new BoardScreen(pos);
        MagicNet.boardHandler = s::onState;
        Minecraft.getInstance().setScreen(s);
        s.send(0, 0);
    }

    private void send(int action, int arg) {
        PacketDistributor.sendToServer(new MagicNet.BoardReq(pos, action, arg));
    }

    private void onState(MagicNet.BoardRes r) {
        List<Integer> d = r.data();
        state = d.stream().mapToInt(Integer::intValue).toArray();
        rebuildWidgets();
    }

    @Override
    protected void init() {
        x0 = (width - W) / 2;
        y0 = (height - H) / 2;
        String[] tabs = {"contracts", "shop"};
        for (int i = 0; i < 2; i++) {
            final int t = i;
            Button b = Button.builder(Component.translatable("board.yukicore.tab." + tabs[i]), btn -> {
                tab = t;
                rebuildWidgets();
            }).bounds(x0 + W / 2 - 104 + i * 106, y0 + 22, 102, 16).build();
            b.active = i != tab;
            addRenderableWidget(b);
        }
        if (state == null) return;
        if (tab == 0) {
            for (int i = 0; i < Contracts.COUNT; i++) {
                final int idx = i;
                int need = state[3 + i * 5], have = state[4 + i * 5];
                boolean claimed = state[6 + i * 5] == 1, done = have >= need;
                int y = y0 + 46 + i * 56;
                Button claim = Button.builder(Component.translatable("board.yukicore.claim"), b -> send(1, idx))
                        .bounds(x0 + W - 92, y + 6, 80, 18).build();
                claim.active = done && !claimed;
                addRenderableWidget(claim);
                Button reroll = Button.builder(Component.translatable("board.yukicore.reroll", Contracts.REROLL_COST), b -> send(2, idx))
                        .bounds(x0 + W - 92, y + 26, 80, 16).build();
                reroll.active = !done && !claimed;
                addRenderableWidget(reroll);
            }
        } else {
            int pages = (Contracts.SHOP.size() + PER_PAGE - 1) / PER_PAGE;
            page = Math.min(page, pages - 1);
            for (int k = 0; k < PER_PAGE; k++) {
                int i = page * PER_PAGE + k;
                if (i >= Contracts.SHOP.size()) break;
                int cx = x0 + 10 + (k % 2) * 142, cy = y0 + 46 + (k / 2) * 30;
                final int idx = i;
                Button buy = Button.builder(Component.translatable("board.yukicore.buy", Contracts.SHOP.get(i).price()), b -> send(3, idx))
                        .bounds(cx + 82, cy + 4, 56, 18).build();
                buy.active = state[1] >= Contracts.SHOP.get(i).price() || (minecraft != null && minecraft.player != null
                        && minecraft.player.getAbilities().instabuild);
                addRenderableWidget(buy);
            }
            if (pages > 1) {
                addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                    page = (page + pages - 1) % pages;
                    rebuildWidgets();
                }).bounds(x0 + W / 2 - 40, y0 + H - 32, 20, 16).build());
                addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                    page = (page + 1) % pages;
                    rebuildWidgets();
                }).bounds(x0 + W / 2 + 20, y0 + H - 32, 20, 16).build());
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderTransparentBackground(g);
        YukiTheme.roundedRect(g, x0 - 3, y0 - 3, W + 6, H + 6, 0xFF2A1A08);
        g.fillGradient(x0, y0, x0 + W, y0 + H, 0xFF6A4420, 0xFF3A2410);
        g.drawCenteredString(font, title.copy().withStyle(ChatFormatting.BOLD), x0 + W / 2, y0 + 8, 0xFFFFE6A0);
        if (state == null) {
            super.render(g, mx, my, pt);
            return;
        }
        if (tab == 0) {
            for (int i = 0; i < Contracts.COUNT; i++) {
                int type = state[2 + i * 5], need = state[3 + i * 5], have = state[4 + i * 5], reward = state[5 + i * 5];
                boolean claimed = state[6 + i * 5] == 1;
                int y = y0 + 46 + i * 56;
                g.fill(x0 + 8, y, x0 + W - 8, y + 50, claimed ? 0x60204020 : 0x70000000);
                String id = Contracts.Type.values()[type].name().toLowerCase(java.util.Locale.ROOT);
                g.drawString(font, Component.translatable("board.yukicore.type." + id).withStyle(ChatFormatting.BOLD), x0 + 14, y + 5, 0xFFFFE6A0, true);
                g.drawString(font, Component.translatable("board.yukicore.goal." + id, need), x0 + 14, y + 17, 0xFFE0D0B0, false);
                int bw = W - 118, bx = x0 + 14, by = y + 31;
                g.fill(bx, by, bx + bw, by + 6, 0xFF201008);
                g.fill(bx, by, bx + bw * Math.min(have, need) / Math.max(1, need), by + 6, have >= need ? 0xFF60D060 : 0xFFE0A030);
                g.drawString(font, have + "/" + need, bx, by + 9, 0xFFFFFFFF, true);
                g.drawString(font, Component.translatable("board.yukicore.reward", reward), bx + 60, by + 9,
                        claimed ? 0xFF80C080 : 0xFFFFD060, true);
            }
            Component bonus = state[0] == 1 ? Component.translatable("board.yukicore.bonus_got").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("board.yukicore.bonus", Contracts.ALL_BONUS).withStyle(ChatFormatting.GOLD);
            g.drawCenteredString(font, bonus, x0 + W / 2, y0 + H - 22, 0xFFFFFFFF);
        } else {
            for (int k = 0; k < PER_PAGE; k++) {
                int i = page * PER_PAGE + k;
                if (i >= Contracts.SHOP.size()) break;
                Contracts.Offer o = Contracts.SHOP.get(i);
                int cx = x0 + 10 + (k % 2) * 142, cy = y0 + 46 + (k / 2) * 30;
                g.fill(cx, cy, cx + 140, cy + 26, 0x70000000);
                ItemStack st = new ItemStack(o.item().get(), o.count());
                g.renderItem(st, cx + 4, cy + 5);
                g.renderItemDecorations(font, st, cx + 4, cy + 5);
                g.pose().pushPose();
                g.pose().translate(cx + 24, cy + 9, 0);
                g.pose().scale(0.7f, 0.7f, 1);
                String name = st.getHoverName().getString();
                if (font.width(name) > 76) name = font.plainSubstrByWidth(name, 72) + "…";
                g.drawString(font, name, 0, 0, 0xFFFFFFFF, false);
                g.pose().popPose();
                if (mx >= cx && mx < cx + 80 && my >= cy && my < cy + 26) g.renderTooltip(font, st, mx, my);
            }
        }
        g.drawString(font, Component.translatable("casino.yukicore.chips", state[1]), x0 + 10, y0 + H - 12, 0xFFFFE6A0, true);
        super.render(g, mx, my, pt);
    }
}
