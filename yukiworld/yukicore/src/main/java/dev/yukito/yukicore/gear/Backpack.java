package dev.yukito.yukicore.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Рюкзак Синтары: 54 слота. ПКМ — открыть, Shift+ПКМ — переплавить руду внутри (уголь из рюкзака как топливо).
 * Подобранные предметы, которые уже лежат в рюкзаке, сразу уходят в него.
 */
public class Backpack extends Item {
    public static final int SIZE = 54;

    public Backpack(Properties props) {
        super(props);
    }

    static NonNullList<ItemStack> load(ItemStack bag) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        bag.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }

    static void save(ItemStack bag, List<ItemStack> items) {
        bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(bag);
        if (player.isShiftKeyDown()) {
            smeltAll(sp, bag);
            return InteractionResultHolder.success(bag);
        }
        int slot = hand == InteractionHand.MAIN_HAND ? sp.getInventory().selected : Inventory.SLOT_OFFHAND;
        BagContainer c = new BagContainer(bag);
        sp.openMenu(new SimpleMenuProvider((id, inv, pl) -> new Menu(id, inv, c, slot), bag.getHoverName()));
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1f, 1.1f);
        return InteractionResultHolder.success(bag);
    }

    private static void smeltAll(ServerPlayer p, ItemStack bag) {
        NonNullList<ItemStack> items = load(bag);
        ServerLevel lvl = p.serverLevel();
        int fuel = 0, smelted = 0;
        for (ItemStack s : items) if (s.is(Items.COAL) || s.is(Items.CHARCOAL)) fuel += s.getCount() * 8;
        if (fuel == 0) {
            p.displayClientMessage(Component.translatable("gear.yukicore.backpack.no_fuel").withStyle(ChatFormatting.RED), true);
            return;
        }
        int usedOps = 0;
        for (int i = 0; i < items.size() && usedOps < fuel; i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty() || s.is(Items.COAL) || s.is(Items.CHARCOAL)) continue;
            ItemStack one = s.copyWithCount(1);
            ItemStack out = Tools.smelt(lvl, one);
            if (out == one) continue;
            int n = Math.min(s.getCount(), fuel - usedOps);
            ItemStack res = out.copyWithCount(Math.min(out.getMaxStackSize(), out.getCount() * n));
            s.shrink(n);
            usedOps += n;
            smelted += n;
            if (s.isEmpty()) items.set(i, res);
            else if (!p.getInventory().add(res)) p.drop(res, false);
        }
        int coal = (usedOps + 7) / 8;
        for (ItemStack s : items) {
            if (coal <= 0) break;
            if (s.is(Items.COAL) || s.is(Items.CHARCOAL)) {
                int t = Math.min(coal, s.getCount());
                s.shrink(t);
                coal -= t;
            }
        }
        save(bag, items);
        p.displayClientMessage(Component.translatable("gear.yukicore.backpack.smelted", smelted).withStyle(ChatFormatting.GOLD), true);
        lvl.playSound(null, p.blockPosition(), SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.PLAYERS, 1f, 1f);
    }

    /** Автоподбор: если такой предмет уже есть в рюкзаке — положить туда. Возвращает остаток. */
    static ItemStack absorb(Player p, ItemStack incoming) {
        for (ItemStack bag : p.getInventory().items) {
            if (!(bag.getItem() instanceof Backpack) || incoming.isEmpty()) continue;
            NonNullList<ItemStack> items = load(bag);
            boolean known = false;
            for (ItemStack s : items) if (ItemStack.isSameItemSameComponents(s, incoming)) known = true;
            if (!known) continue;
            for (ItemStack s : items) {
                if (incoming.isEmpty()) break;
                if (ItemStack.isSameItemSameComponents(s, incoming) && s.getCount() < s.getMaxStackSize()) {
                    int t = Math.min(incoming.getCount(), s.getMaxStackSize() - s.getCount());
                    s.grow(t);
                    incoming.shrink(t);
                }
            }
            for (int i = 0; i < items.size() && !incoming.isEmpty(); i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, incoming.copy());
                    incoming.setCount(0);
                }
            }
            save(bag, items);
        }
        return incoming;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Weapons.tip("sintara_backpack", tooltip);
        int used = 0;
        for (ItemStack s : stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems()) used++;
        tooltip.add(Component.translatable("gear.yukicore.backpack.used", used, SIZE).withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Содержимое рюкзака: при любом изменении сразу записывается в предмет. */
    static class BagContainer extends SimpleContainer {
        final ItemStack bag;

        BagContainer(ItemStack bag) {
            super(SIZE);
            this.bag = bag;
            NonNullList<ItemStack> items = load(bag);
            for (int i = 0; i < SIZE; i++) setItem(i, items.get(i));
            addListener(c -> save(bag, getItems()));
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return !(stack.getItem() instanceof Backpack);
        }
    }

    /** Сундук 9×6, в котором нельзя трогать сам открытый рюкзак (защита от дюпа). */
    static class Menu extends ChestMenu {
        private final int bagSlot;
        private final BagContainer c;

        Menu(int id, Inventory inv, BagContainer c, int bagInvSlot) {
            super(MenuType.GENERIC_9x6, id, inv, c, 6);
            this.c = c;
            // слоты игрока в меню: 54..80 — инвентарь (9..35), 81..89 — хотбар (0..8)
            this.bagSlot = bagInvSlot < 9 ? 81 + bagInvSlot : -1;
        }

        @Override
        public void clicked(int slot, int button, ClickType type, Player player) {
            if (slot == bagSlot || (type == ClickType.SWAP && button == bagSlot - 81)) return;
            if (slot >= 0 && slot < slots.size() && getSlot(slot).getItem().getItem() instanceof Backpack && slot < SIZE) return;
            super.clicked(slot, button, type, player);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            if (index == bagSlot) return ItemStack.EMPTY;
            ItemStack moving = index >= 0 && index < slots.size() ? getSlot(index).getItem() : ItemStack.EMPTY;
            if (index >= SIZE && moving.getItem() instanceof Backpack) return ItemStack.EMPTY;
            return super.quickMoveStack(player, index);
        }

        @Override
        public boolean stillValid(Player player) {
            return player.getInventory().contains(c.bag) || player.getOffhandItem() == c.bag;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            save(c.bag, c.getItems());
        }
    }
}
