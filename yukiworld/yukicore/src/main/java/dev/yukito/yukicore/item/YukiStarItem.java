package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Звезда Юки: ПКМ даёт ночное зрение, скорость, прыгучесть и регенерацию
 * на 45 секунд, с перезарядкой 2 минуты.
 */
public class YukiStarItem extends Item {
    private static final int DURATION = 20 * 45;
    private static final int COOLDOWN = 20 * 120;

    public YukiStarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION + 200, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, DURATION, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1, false, false, true));

            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SNOWFLAKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        60, 0.6, 0.8, 0.6, 0.05);
                server.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        20, 0.4, 0.6, 0.4, 0.02);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.4f);
            player.getCooldowns().addCooldown(this, COOLDOWN);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yukicore.yuki_star.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.yukicore.yuki_star.desc2").withStyle(ChatFormatting.GRAY));
    }
}
