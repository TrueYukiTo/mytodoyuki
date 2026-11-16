package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Морозный магнит: ПКМ — вкл/выкл; включённый притягивает предметы и опыт в радиусе. */
public class FrostMagnetItem extends Item {
    private final double radius;

    public FrostMagnetItem(Properties properties, double radius) {
        super(properties);
        this.radius = radius;
    }

    public static boolean isOn(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("on");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean on = !isOn(stack);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("on", on);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, on ? 1.6f : 0.7f);
        player.displayClientMessage(Component.translatable(on ? "message.yukicore.magnet.on" : "message.yukicore.magnet.off")
                .withStyle(on ? ChatFormatting.AQUA : ChatFormatting.GRAY), true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || player.isSpectator() || !isOn(stack)) return;
        if (player.tickCount % 3 != 0) return;
        AABB box = player.getBoundingBox().inflate(radius);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box,
                e -> e.isAlive() && !e.hasPickUpDelay() && !e.getPersistentData().getBoolean("PreventRemoteMovement"))) {
            item.setPos(player.getX(), player.getY() + 0.5, player.getZ());
            item.setDeltaMovement(0, 0, 0);
        }
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
            orb.setPos(player.getX(), player.getY() + 0.5, player.getZ());
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isOn(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.yukicore.magnet", (int) radius).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(isOn(stack) ? "message.yukicore.magnet.on" : "message.yukicore.magnet.off")
                .withStyle(isOn(stack) ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
    }
}
