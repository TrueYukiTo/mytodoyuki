package dev.yukito.yukicore;

import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Броня YukiCore — сильнейшая в сборке. Каждая часть снижает любой урон на свой процент (после брони),
 * полный комплект — ещё бонус. Итог: Вечная зима — до 75%, регалии — 50%, целестий — 42% (максимум 85%).
 */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class ArmorSetHandler {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String FLIGHT_TAG = "yukiEternalFlight";

    private ArmorSetHandler() {}

    /** Снижение урона от надетой брони YukiCore (0..0.85). */
    public static float protection(LivingEntity e) {
        float total = 0;
        Map<String, Integer> sets = new HashMap<>();
        Map<String, Float> bonus = new HashMap<>();
        for (EquipmentSlot slot : ARMOR) {
            if (e.getItemBySlot(slot).getItem() instanceof YukiArmorItem a) {
                total += a.tier.protection();
                sets.merge(a.tier.set(), 1, Integer::sum);
                bonus.put(a.tier.set(), a.tier.setBonus());
            }
        }
        for (var en : sets.entrySet()) if (en.getValue() == 4) total += bonus.get(en.getKey());
        return Math.min(0.85f, total);
    }

    public static boolean fullSet(LivingEntity e, String set) {
        for (EquipmentSlot slot : ARMOR) {
            if (!(e.getItemBySlot(slot).getItem() instanceof YukiArmorItem a) || !a.tier.set().equals(set)) return false;
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onIncoming(LivingIncomingDamageEvent e) {
        LivingEntity t = e.getEntity();
        if (e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (fullSet(t, "eternal") && (e.getSource().is(DamageTypeTags.IS_FIRE) || e.getSource().is(DamageTypeTags.IS_FREEZING)
                || e.getSource().is(DamageTypeTags.IS_DROWNING) || e.getSource().is(DamageTypes.WITHER)
                || e.getSource().is(DamageTypes.MAGIC) && e.getSource().getEntity() == null)) {
            e.setCanceled(true);
            return;
        }
        float p = protection(t);
        if (p > 0) e.setAmount(e.getAmount() * (1 - p));
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent e) {
        LivingEntity t = e.getEntity();
        if (fullSet(t, "eternal") || starSuit(t)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide || p.tickCount % 20 != 0) return;

        // Вечная зима: полёт, иммунитеты, все усиления
        boolean eternal = fullSet(p, "eternal");
        boolean hadFlight = p.getPersistentData().getBoolean(FLIGHT_TAG);
        if (eternal) {
            if (!p.getAbilities().mayfly) {
                p.getAbilities().mayfly = true;
                p.onUpdateAbilities();
                p.getPersistentData().putBoolean(FLIGHT_TAG, true);
            }
            give(p, MobEffects.NIGHT_VISION, 0, 260);
            give(p, MobEffects.REGENERATION, 2, 40);
            give(p, MobEffects.DAMAGE_BOOST, 3, 40);
            give(p, MobEffects.DIG_SPEED, 3, 40);
            give(p, MobEffects.MOVEMENT_SPEED, 2, 40);
            give(p, MobEffects.SATURATION, 0, 40);
            give(p, MobEffects.WATER_BREATHING, 0, 40);
            p.removeEffect(MobEffects.WITHER);
            p.removeEffect(MobEffects.POISON);
            p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            p.setTicksFrozen(0);
            p.clearFire();
        } else if (hadFlight) {
            p.getPersistentData().remove(FLIGHT_TAG);
            if (!p.isCreative() && !p.isSpectator()) {
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
                p.onUpdateAbilities();
            }
        }

        if (starSuit(p)) {
            give(p, MobEffects.NIGHT_VISION, 0, 260);
            give(p, MobEffects.MOVEMENT_SPEED, 1, 40);
        }
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
        }
        if (eternal) return;
        if (fullSet(p, "celestium")) {
            give(p, MobEffects.NIGHT_VISION, 0, 260);
            give(p, MobEffects.FIRE_RESISTANCE, 0, 40);
            give(p, MobEffects.WATER_BREATHING, 0, 40);
            give(p, MobEffects.REGENERATION, 1, 40);
            give(p, MobEffects.DAMAGE_BOOST, 2, 40);
            give(p, MobEffects.DIG_SPEED, 2, 40);
            give(p, MobEffects.MOVEMENT_SPEED, 1, 40);
        } else if (fullSet(p, "aurorium")) {
            give(p, MobEffects.DIG_SPEED, 1, 40);
            give(p, MobEffects.DAMAGE_BOOST, 1, 40);
            give(p, MobEffects.FIRE_RESISTANCE, 0, 40);
            give(p, MobEffects.REGENERATION, 0, 40);
        } else if (fullSet(p, "frost")) {
            give(p, MobEffects.MOVEMENT_SPEED, 0, 40);
            give(p, MobEffects.DAMAGE_BOOST, 0, 40);
            p.setTicksFrozen(0);
        }
        if (fullSet(p, "yuki_regalia") || fullSet(p, "cryomancer")) p.setTicksFrozen(0);
        if (fullSet(p, "sintara_regalia") || fullSet(p, "pyromancer")) give(p, MobEffects.FIRE_RESISTANCE, 0, 40);
        if (fullSet(p, "yuki_regalia") || fullSet(p, "sintara_regalia")) give(p, MobEffects.REGENERATION, 1, 40);
    }

    private static boolean starSuit(LivingEntity p) {
        for (EquipmentSlot slot : ARMOR) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(p.getItemBySlot(slot).getItem());
            if (!id.getNamespace().equals(YukiCore.MOD_ID) || !id.getPath().startsWith("star_space_")) return false;
        }
        return true;
    }

    private static void give(Player p, Holder<MobEffect> effect, int amp, int duration) {
        p.addEffect(new MobEffectInstance(effect, duration, amp, true, false, true));
    }
}
