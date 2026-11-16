package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.entity.boss.GlacialArchon;
import dev.yukito.yukicore.entity.boss.InfernalPhoenix;
import dev.yukito.yukicore.magic.net.MagicNet;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Мана, синхронизация, опыт за магию, испытания стихий, ледяной панцирь и возрождение. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class MagicEvents {
    private static final String TAG_SCHOOL = "yukiMagicSchool";
    private static final String TAG_TIME = "yukiMagicTime";

    private MagicEvents() {}

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        MagicData d = Magic.get(p);
        float max = Magic.maxMana(p);
        float before = d.mana;
        d.mana = Math.min(max, d.mana + Magic.regenPerTick(p));
        if (d.dirty) {
            Magic.sync(p);
        } else if (p.tickCount % 10 == 0 && Math.abs(before - d.mana) > 0.001f) {
            PacketDistributor.sendToPlayer(p, new MagicNet.Mana(d.mana));
        }
        if (p.tickCount % 4 == 0 && p.hasEffect(ModMagic.PHOENIX)) {
            p.serverLevel().sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 4, 0.5, 0.4, 0.5, 0.01);
        }
        if (p.tickCount % 6 == 0 && p.hasEffect(ModMagic.FROST_ARMOR)) {
            p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 2, 0.5, 0.6, 0.5, 0.01);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Magic.sync(p);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Magic.sync(p);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Magic.sync(p);
    }

    /** Вызывается из {@link Element#hurt}: опыт за урон заклинаниями и метка для опыта за убийство. */
    static void onSpellDamage(ServerPlayer p, LivingEntity target, Element el, float amount) {
        School s = el == Element.ICE ? School.ICE : el == Element.FIRE ? School.FIRE : null;
        if (s == null) return;
        target.getPersistentData().putInt(TAG_SCHOOL, s.ordinal());
        target.getPersistentData().putInt(TAG_TIME, target.tickCount);
        Magic.addXp(p, s, (int) Math.min(40, amount / 3));
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        MobEffectInstance armor = target.getEffect(ModMagic.FROST_ARMOR);
        if (armor != null && e.getSource().getEntity() instanceof LivingEntity attacker && attacker != target) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            attacker.setTicksFrozen(Math.min(300, attacker.getTicksFrozen() + 60));
            attacker.hurt(target.damageSources().freeze(), 2 + 2 * armor.getAmplifier());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        // Возрождение Синтары
        if (dead instanceof Player p && p.hasEffect(ModMagic.REBIRTH)) {
            e.setCanceled(true);
            p.removeEffect(ModMagic.REBIRTH);
            p.setHealth(p.getMaxHealth() * 0.5f);
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3));
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
            if (p.level() instanceof ServerLevel s) {
                s.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 200, 1, 1.5, 1, 0.2);
                s.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 80, 1, 1.5, 1, 0.5);
                s.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1f, 1.3f);
            }
            p.displayClientMessage(Component.translatable("magic.yukicore.rebirth").withStyle(ChatFormatting.GOLD), true);
            return;
        }
        // Испытания стихий
        if (dead instanceof GlacialArchon || dead instanceof InfernalPhoenix) {
            School s = dead instanceof GlacialArchon ? School.ICE : School.FIRE;
            for (ServerPlayer p : dead.level().getEntitiesOfClass(ServerPlayer.class, dead.getBoundingBox().inflate(64))) {
                MagicData d = Magic.get(p);
                if (!d.trial[s.ordinal()]) {
                    d.trial[s.ordinal()] = true;
                    d.dirty = true;
                    p.sendSystemMessage(Component.translatable("magic.yukicore.trial." + s.id)
                            .withStyle(s == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD, ChatFormatting.BOLD));
                }
                Magic.addXp(p, s, 3000);
            }
        }
        // Опыт за убийство магией
        if (e.getSource().getEntity() instanceof ServerPlayer p && dead.getPersistentData().contains(TAG_SCHOOL)
                && dead.tickCount - dead.getPersistentData().getInt(TAG_TIME) < 100) {
            School s = School.values()[dead.getPersistentData().getInt(TAG_SCHOOL)];
            Magic.addXp(p, s, (int) Math.min(3000, Math.max(3, dead.getMaxHealth() / 3)));
        }
    }
}
