package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Амулет Синтары: в инвентаре — удача II, ночное зрение, подсветка врагов в 16 блоках. */
public class SintaraAmuletItem extends CoreItem {
    public SintaraAmuletItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player p) || p.tickCount % 40 != 0) return;
        p.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1, true, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(16), e -> e instanceof Enemy)) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yukicore.sintara_amulet.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.yukicore.for_sintara").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }
}
