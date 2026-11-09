package dev.yukito.yukicore;

import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Даёт бонусы владельцу Бесконечной звезды Юки. Проверка раз в секунду — почти бесплатно для TPS. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class InfinityStarHandler {
    private static final String FLIGHT_TAG = "yukicore_star_flight";

    private InfinityStarHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % 20 != 0) return;

        boolean hasStar = player.getInventory().contains(s -> s.is(ModItems.YUKI_INFINITY_STAR.get()));
        CompoundTag data = player.getPersistentData();
        boolean granted = data.getBoolean(FLIGHT_TAG);
        boolean survival = !player.isCreative() && !player.isSpectator();

        if (hasStar) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 15, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false, true));
            if (survival && !player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
                data.putBoolean(FLIGHT_TAG, true);
            }
        } else if (granted) {
            data.remove(FLIGHT_TAG);
            if (survival) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
