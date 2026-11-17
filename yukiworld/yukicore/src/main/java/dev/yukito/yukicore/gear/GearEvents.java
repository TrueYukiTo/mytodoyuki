package dev.yukito.yukicore.gear;

import dev.yukito.yukicore.ArmorSetHandler;
import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.Fx;
import dev.yukito.yukicore.registry.ModItems;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** События 7.4: камни, ледяные стрелы, амулет второго шанса, бур, рюкзак, скакуны, бонусы новых комплектов. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class GearEvents {
    private GearEvents() {}

    // ------------------------------------------------------------------ удары
    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        if (target.level().isClientSide) return;
        Entity direct = e.getSource().getDirectEntity();
        Entity src = e.getSource().getEntity();

        if (direct instanceof AbstractArrow arrow && arrow.getPersistentData().getBoolean(Weapons.ARROW_TAG)) {
            Weapons.frostArrowHit(target, arrow);
        }
        // камни стихий: удар рукой или стрелой из оружия с камнями
        if (src instanceof Player p && src != target && (direct == p || direct instanceof AbstractArrow)) {
            ItemStack weapon = p.getMainHandItem();
            float bonus = 0;
            int ember = Gems.count(weapon, Gems.Type.EMBER), frost = Gems.count(weapon, Gems.Type.FROST);
            int blood = Gems.count(weapon, Gems.Type.BLOOD), storm = Gems.count(weapon, Gems.Type.STORM);
            if (ember > 0) {
                target.igniteForSeconds(3 + ember);
                bonus += 0.12f * ember;
            }
            if (frost > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, frost - 1));
                if (p.getRandom().nextFloat() < 0.12f * frost) target.addEffect(new MobEffectInstance(ModMagic.FROZEN, 30, 0));
            }
            if (storm > 0 && p.getRandom().nextFloat() < 0.12f * storm) {
                bonus += 0.8f;
                if (target.level() instanceof ServerLevel lvl) {
                    lvl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1, target.getZ(), 30, 0.4, 0.6, 0.4, 0.3);
                    lvl.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.5f, 1.6f);
                }
            }
            if (bonus > 0) e.setAmount(e.getAmount() * (1 + bonus));
            if (blood > 0) p.heal(e.getAmount() * 0.06f * blood);
        }
        // Огненная кровь и комплекты: атакующий горит / мёрзнет
        if (src instanceof LivingEntity attacker && attacker != target) {
            if (target.hasEffect(GearItems.FIRE_BLOOD) || ArmorSetHandler.fullSet(target, "fire_demon")) {
                attacker.igniteForSeconds(4);
                if (target instanceof ServerPlayer sp) Fx.hit(sp, attacker, 3, Element.FIRE);
            }
            if (ArmorSetHandler.fullSet(target, "frost_queen")) {
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                attacker.setTicksFrozen(Math.min(300, attacker.getTicksFrozen() + 60));
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent e) {
        LivingEntity t = e.getEntity();
        if (Mounts.isSteed(t) || t.getVehicle() != null && Mounts.isSteed(t.getVehicle())) e.setCanceled(true);
        else if (t instanceof Player p && (Trinkets.has(p, Trinkets.Kind.PHOENIX_CLOAK) || Trinkets.wearing(p, false))) {
            e.setDamageMultiplier(e.getDamageMultiplier() * 0.3f);
        }
    }

    // ------------------------------------------------------------------ смерть: амулет и удача
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof ServerPlayer p) {
            ItemStack amulet = Trinkets.find(p, Trinkets.Kind.SECOND_CHANCE);
            if (!amulet.isEmpty() && !p.getCooldowns().isOnCooldown(amulet.getItem())) {
                e.setCanceled(true);
                p.setHealth(p.getMaxHealth() * 0.5f);
                p.removeAllEffects();
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                p.getCooldowns().addCooldown(amulet.getItem(), 20 * 60 * 10);
                p.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 120, 0.6, 1, 0.6, 0.5);
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1f, 1.2f);
                p.displayClientMessage(Component.translatable("gear.yukicore.second_chance").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                return;
            }
        }
        if (e.getSource().getEntity() instanceof ServerPlayer killer && killer.hasEffect(GearItems.SINTARA_LUCK)
                && !(dead instanceof Player) && killer.getRandom().nextFloat() < 0.3f) {
            dead.spawnAtLocation(new ItemStack(ModItems.SINTARA_CHIP.get(), 1 + killer.getRandom().nextInt(3)));
        }
    }

    // ------------------------------------------------------------------ бур и рюкзак
    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer() instanceof ServerPlayer p && p.getMainHandItem().getItem() instanceof Tools.Drill) {
            Tools.drill(p, e.getPos(), e.getState());
        }
    }

    @SubscribeEvent
    public static void onDrops(BlockDropsEvent e) {
        if (!(e.getTool().getItem() instanceof Tools.Drill) || !(e.getBreaker() instanceof Player p) || p.isShiftKeyDown()) return;
        for (ItemEntity ie : e.getDrops()) {
            ItemStack out = Tools.smelt(e.getLevel(), ie.getItem());
            if (out != ie.getItem()) ie.setItem(out);
        }
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre e) {
        Player p = e.getPlayer();
        if (p.level().isClientSide) return;
        ItemEntity ie = e.getItemEntity();
        if (ie.hasPickUpDelay()) return;
        ItemStack st = ie.getItem();
        int before = st.getCount();
        ItemStack rest = Backpack.absorb(p, st.copy());
        if (rest.getCount() != before) {
            p.take(ie, before - rest.getCount());
            if (rest.isEmpty()) ie.discard();
            else ie.setItem(rest);
            p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1.6f);
            e.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
        }
    }

    // ------------------------------------------------------------------ скакуны
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post e) {
        if (e.getEntity() instanceof AbstractHorse h && Mounts.isSteed(h)) Mounts.tick(h);
    }

    // ------------------------------------------------------------------ бонусы комплектов
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 0) return;
        ServerLevel lvl = p.serverLevel();
        if (ArmorSetHandler.fullSet(p, "frost_queen")) {
            p.setTicksFrozen(0);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false, true));
            Fx.area(p, p.position(), 6, 4, Element.ICE, x -> x.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1)));
            Fx.ring(lvl, p.position().add(0, 0.2, 0), 6, ParticleTypes.SNOWFLAKE);
        }
        if (ArmorSetHandler.fullSet(p, "fire_demon")) {
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, true, false, true));
            if (p.tickCount % 40 == 0) {
                Fx.area(p, p.position(), 6, 6, Element.FIRE, x -> x.igniteForSeconds(2));
                Fx.ring(lvl, p.position().add(0, 0.2, 0), 6, ParticleTypes.FLAME);
            }
        }
    }

    // ------------------------------------------------------------------ подсказки камней на любом предмете
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent e) {
        Gems.tooltip(e.getItemStack(), e.getToolTip());
    }

    // ------------------------------------------------------------------ способности снаряжения (пакет с клиента)
    /** id 0 — двойной прыжок, 1 — рывок ботинок. Движение уже сделал клиент; сервер проверяет и рисует эффекты. */
    public static void ability(ServerPlayer p, int id) {
        ServerLevel lvl = p.serverLevel();
        if (id == 0 && Trinkets.has(p, Trinkets.Kind.DOUBLE_JUMP)) {
            p.resetFallDistance();
            lvl.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 15, 0.3, 0.05, 0.3, 0.02);
            lvl.playSound(null, p.blockPosition(), SoundEvents.WOOL_STEP, SoundSource.PLAYERS, 1f, 1.6f);
        } else if (id == 1 && Trinkets.wearing(p, false)) {
            p.resetFallDistance();
            lvl.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.5, p.getZ(), 20, 0.4, 0.3, 0.4, 0.05);
            lvl.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.6f);
        }
    }
}
