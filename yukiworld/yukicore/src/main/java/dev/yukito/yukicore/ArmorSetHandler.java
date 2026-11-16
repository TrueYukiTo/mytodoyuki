package dev.yukito.yukicore;

import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Бонусы за полный комплект брони YukiCore. Проверка раз в секунду. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class ArmorSetHandler {
    private ArmorSetHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide || p.tickCount % 20 != 0) return;

        if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.CROWN_OF_YUKI.get())) {
            give(p, MobEffects.NIGHT_VISION, 0, 260);
            give(p, MobEffects.FIRE_RESISTANCE, 0, 40);
            give(p, MobEffects.WATER_BREATHING, 0, 40);
            give(p, MobEffects.REGENERATION, 1, 40);
            give(p, MobEffects.DAMAGE_BOOST, 2, 40);
            give(p, MobEffects.DIG_SPEED, 2, 40);
            give(p, MobEffects.MOVEMENT_SPEED, 2, 40);
            give(p, MobEffects.DAMAGE_RESISTANCE, 2, 40);
            give(p, MobEffects.SATURATION, 0, 40);
            p.setTicksFrozen(0);
            return;
        }
        if (starSuit(p)) {
            // звёздный скафандр Юки: ночное зрение, скорость, без урона от падения
            give(p, MobEffects.NIGHT_VISION, 0, 260);
            give(p, MobEffects.MOVEMENT_SPEED, 1, 40);
        }
        if (fullSet(p, ModItems.CELESTIUM_HELMET.get(), ModItems.CELESTIUM_CHESTPLATE.get(),
                ModItems.CELESTIUM_LEGGINGS.get(), ModItems.CELESTIUM_BOOTS.get())) {
            give(p, MobEffects.NIGHT_VISION, 0, 260);
            give(p, MobEffects.FIRE_RESISTANCE, 0, 40);
            give(p, MobEffects.WATER_BREATHING, 0, 40);
            give(p, MobEffects.REGENERATION, 0, 40);
            give(p, MobEffects.DAMAGE_BOOST, 1, 40);
            give(p, MobEffects.DIG_SPEED, 2, 40);
            give(p, MobEffects.MOVEMENT_SPEED, 1, 40);
        } else if (fullSet(p, ModItems.AURORIUM_HELMET.get(), ModItems.AURORIUM_CHESTPLATE.get(),
                ModItems.AURORIUM_LEGGINGS.get(), ModItems.AURORIUM_BOOTS.get())) {
            give(p, MobEffects.DIG_SPEED, 1, 40);
            give(p, MobEffects.DAMAGE_BOOST, 0, 40);
            give(p, MobEffects.FIRE_RESISTANCE, 0, 40);
        } else if (fullSet(p, ModItems.FROST_HELMET.get(), ModItems.FROST_CHESTPLATE.get(),
                ModItems.FROST_LEGGINGS.get(), ModItems.FROST_BOOTS.get())) {
            give(p, MobEffects.MOVEMENT_SPEED, 0, 40);
            p.setTicksFrozen(0);
        }
    }

    @SubscribeEvent
    public static void onFall(net.neoforged.neoforge.event.entity.living.LivingFallEvent e) {
        if (e.getEntity() instanceof Player p && starSuit(p)) e.setCanceled(true);
    }

    private static boolean starSuit(Player p) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(p.getItemBySlot(slot).getItem());
            if (!id.getNamespace().equals(YukiCore.MOD_ID) || !id.getPath().startsWith("star_space_")) return false;
        }
        return true;
    }

    private static boolean fullSet(Player p, Item head, Item chest, Item legs, Item feet) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(head) && p.getItemBySlot(EquipmentSlot.CHEST).is(chest)
                && p.getItemBySlot(EquipmentSlot.LEGS).is(legs) && p.getItemBySlot(EquipmentSlot.FEET).is(feet);
    }

    private static void give(Player p, Holder<MobEffect> effect, int amp, int duration) {
        p.addEffect(new MobEffectInstance(effect, duration, amp, true, false, true));
    }
}
