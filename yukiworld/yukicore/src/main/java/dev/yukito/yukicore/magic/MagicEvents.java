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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/** Мана, синхронизация, опыт, пассивки, ауры аватаров, магический полёт, союзники, испытания и возрождение. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class MagicEvents {
    private static final String TAG_SCHOOL = "yukiMagicSchool";
    private static final String TAG_TIME = "yukiMagicTime";
    private static final net.minecraft.resources.ResourceLocation INNER_FIRE = YukiCore.id("inner_fire");

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
        ServerLevel lvl = p.serverLevel();
        // магический полёт: крылья феникса и аватары
        boolean wantFlight = p.hasEffect(ModMagic.PHOENIX) || p.hasEffect(ModMagic.AVATAR_ICE) || p.hasEffect(ModMagic.AVATAR_FIRE);
        if (wantFlight && !p.getAbilities().mayfly) {
            p.getAbilities().mayfly = true;
            p.onUpdateAbilities();
            d.magicFlight = true;
        } else if (!wantFlight && d.magicFlight) {
            d.magicFlight = false;
            if (!p.isCreative() && !p.isSpectator()) {
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
                p.onUpdateAbilities();
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
            }
        }
        if (p.tickCount % 4 == 0) {
            if (p.hasEffect(ModMagic.PHOENIX)) wings(lvl, p, ParticleTypes.FLAME);
            if (p.hasEffect(ModMagic.AVATAR_FIRE)) wings(lvl, p, ParticleTypes.FLAME);
            if (p.hasEffect(ModMagic.AVATAR_ICE)) wings(lvl, p, ParticleTypes.SNOWFLAKE);
            if (p.hasEffect(ModMagic.FROST_ARMOR)) {
                lvl.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 2, 0.5, 0.6, 0.5, 0.01);
            }
        }
        // ауры аватаров
        if (p.tickCount % 10 == 0) {
            if (p.hasEffect(ModMagic.AVATAR_ICE)) {
                Fx.area(p, p.position(), 9, 10 * Magic.power(p, Skill.YUKI_AVATAR), Element.ICE,
                        x -> x.addEffect(new MobEffectInstance(ModMagic.FROZEN, 20, 0)));
                Fx.ring(lvl, p.position(), 9, ParticleTypes.SNOWFLAKE);
            }
            if (p.hasEffect(ModMagic.AVATAR_FIRE)) {
                Fx.area(p, p.position(), 9, 12 * Magic.power(p, Skill.SINTARA_AVATAR), Element.FIRE, null);
                Fx.ring(lvl, p.position(), 9, ParticleTypes.FLAME);
            }
        }
        // 7.3: Аура холода / Аура жара
        if (p.tickCount % 20 == 0) {
            int chill = d.rank(Skill.CHILL_AURA), heat = d.rank(Skill.HEAT_AURA);
            if (chill > 0) {
                for (LivingEntity x : Fx.enemies(p, p.position(), 3 + chill)) {
                    x.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, chill >= 4 ? 1 : 0));
                    lvl.sendParticles(ParticleTypes.SNOWFLAKE, x.getX(), x.getY() + 1, x.getZ(), 4, 0.3, 0.4, 0.3, 0.01);
                }
            }
            if (heat > 0 && p.tickCount % 40 == 0) {
                for (LivingEntity x : Fx.enemies(p, p.position(), 3 + heat)) {
                    Fx.hit(p, x, 2 * heat * Magic.power(p, Skill.HEAT_AURA), Element.FIRE);
                    x.igniteForSeconds(2);
                }
            }
        }
        // Внутренний огонь: +4 ❤ за ранг
        if (p.tickCount % 40 == 0) {
            AttributeInstance hp = p.getAttribute(Attributes.MAX_HEALTH);
            int want = 4 * d.rank(Skill.INNER_FIRE);
            AttributeModifier cur = hp == null ? null : hp.getModifier(INNER_FIRE);
            if (hp != null && (cur == null ? want != 0 : cur.amount() != want)) {
                hp.removeModifier(INNER_FIRE);
                if (want > 0) hp.addPermanentModifier(new AttributeModifier(INNER_FIRE, want, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void wings(ServerLevel lvl, Player p, net.minecraft.core.particles.ParticleOptions part) {
        Vec3 back = p.getLookAngle().multiply(1, 0, 1).normalize().scale(-0.4);
        Vec3 side = new Vec3(-back.z, 0, back.x).normalize();
        for (int i = 1; i <= 6; i++) {
            double h = 1.4 + Math.sin(p.tickCount * 0.3) * 0.2 + i * 0.08;
            for (int s = -1; s <= 1; s += 2) {
                Vec3 at = p.position().add(back).add(side.scale(s * i * 0.32)).add(0, h, 0);
                lvl.sendParticles(part, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            Magic.rebuildIfOverspent(p);
            Magic.sync(p);
        }
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
        Magic.addXp(p, s, (int) Math.min(60, amount / 4));
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        MobEffectInstance bite = target.getEffect(ModMagic.FROSTBITE);
        if (bite != null) e.setAmount(e.getAmount() * (1 + 0.06f * (bite.getAmplifier() + 1)));
        MobEffectInstance armor = target.getEffect(ModMagic.FROST_ARMOR);
        if (armor != null && e.getSource().getEntity() instanceof LivingEntity attacker && attacker != target) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            attacker.setTicksFrozen(Math.min(300, attacker.getTicksFrozen() + 60));
            attacker.hurt(target.damageSources().freeze(), 4 + 4 * armor.getAmplifier());
        }
        // союзники не ранят хозяина, хозяин — союзников
        Entity src = e.getSource().getEntity();
        if (src != null && Fx.isAlly(src) && target instanceof Player) e.setCanceled(true);
        if (src instanceof Player && Fx.isAlly(target)) e.setCanceled(true);
    }

    // ------------------------------------------------------------------ союзники
    @SubscribeEvent
    public static void onAllyTarget(LivingChangeTargetEvent e) {
        if (Fx.isAlly(e.getEntity()) && (e.getNewAboutToBeSetTarget() instanceof Player || (e.getNewAboutToBeSetTarget() != null
                && Fx.isAlly(e.getNewAboutToBeSetTarget())))) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAllyTick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof Mob m) || m.level().isClientSide || m.tickCount % 20 != 0 || !Fx.isAlly(m)) return;
        ServerLevel lvl = (ServerLevel) m.level();
        if (lvl.getGameTime() > m.getPersistentData().getLong("yukiExpire")) {
            lvl.sendParticles(ParticleTypes.POOF, m.getX(), m.getY() + 1, m.getZ(), 20, 0.4, 0.6, 0.4, 0.05);
            m.discard();
            return;
        }
        UUID owner = m.getPersistentData().hasUUID("yukiOwner") ? m.getPersistentData().getUUID("yukiOwner") : null;
        Player p = owner == null ? null : lvl.getPlayerByUUID(owner);
        if (p == null) {
            m.discard();
            return;
        }
        if (m.distanceTo(p) > 28) m.teleportTo(p.getX(), p.getY() + 0.5, p.getZ());
        if (m.getTarget() == null || !m.getTarget().isAlive() || m.getTarget() instanceof Player) {
            LivingEntity best = null;
            double bd = 24 * 24;
            for (LivingEntity c : lvl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(20),
                    x -> x instanceof Enemy && x.isAlive() && !Fx.isAlly(x))) {
                double dd = c.distanceToSqr(p);
                if (dd < bd) {
                    bd = dd;
                    best = c;
                }
            }
            m.setTarget(best);
        }
        if (m.getType() == net.minecraft.world.entity.EntityType.VEX) {
            lvl.sendParticles(ParticleTypes.FLAME, m.getX(), m.getY() + 0.5, m.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
        } else {
            lvl.sendParticles(ParticleTypes.SNOWFLAKE, m.getX(), m.getY() + 1.5, m.getZ(), 8, 0.6, 0.8, 0.6, 0.01);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        // Возрождение Синтары
        if (dead instanceof Player p && p.hasEffect(ModMagic.REBIRTH)) {
            e.setCanceled(true);
            p.removeEffect(ModMagic.REBIRTH);
            p.setHealth(p.getMaxHealth() * 0.6f);
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3));
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
            if (p.level() instanceof ServerLevel s) {
                s.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 300, 1, 1.5, 1, 0.3);
                s.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 120, 1, 1.5, 1, 0.5);
                s.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1f, 1.3f);
                Fx.shake(s, p.position(), 16, 1.5f, 15);
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
                Magic.addXp(p, s, 8000);
            }
        }
        // Опыт за убийство магией и Самовозгорание
        if (e.getSource().getEntity() instanceof ServerPlayer p && dead.getPersistentData().contains(TAG_SCHOOL)
                && dead.tickCount - dead.getPersistentData().getInt(TAG_TIME) < 100) {
            School s = School.values()[dead.getPersistentData().getInt(TAG_SCHOOL)];
            Magic.addXp(p, s, (int) Math.min(5000, Math.max(5, dead.getMaxHealth() / 2)));
            int comb = Magic.get(p).rank(Skill.COMBUSTION);
            if (s == School.FIRE && comb > 0 && dead.level() instanceof ServerLevel lvl) {
                Vec3 at = dead.position();
                Scheduler.later(lvl, 2, () -> {
                    Fx.area(p, at, 3 + comb * 0.5, 20 * comb * Magic.power(p, Skill.COMBUSTION), Element.FIRE, null);
                    Fx.burst(lvl, at.add(0, 0.5, 0), ParticleTypes.EXPLOSION, 2, 0.5, 0);
                    Fx.burst(lvl, at.add(0, 0.5, 0), ParticleTypes.FLAME, 40, 1, 0.1);
                    Fx.sound(lvl, at, SoundEvents.GENERIC_EXPLODE.value(), 0.8f, 1.3f);
                });
            }
        }
    }
}
