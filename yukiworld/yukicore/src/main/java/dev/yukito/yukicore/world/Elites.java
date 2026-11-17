package dev.yukito.yukicore.world;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.entity.boss.YukiBoss;
import dev.yukito.yukicore.gear.GearItems;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Элитные мобы: изредка обычный монстр рождается чемпионом со случайными способностями (огненный, ледяной,
 * вампир, быстрый, гигант, бронированный, взрывной, призыватель, телепортер). Он сильнее и с хорошей добычей.
 */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class Elites {
    private Elites() {}

    public static final String TAG = "yukiElite";
    static final String DATA = "YukiAffixes";

    public enum Affix {
        FIERY(ChatFormatting.GOLD), FROZEN(ChatFormatting.AQUA), VAMPIRIC(ChatFormatting.DARK_RED), SWIFT(ChatFormatting.GREEN),
        GIANT(ChatFormatting.DARK_PURPLE), ARMORED(ChatFormatting.GRAY), EXPLOSIVE(ChatFormatting.RED),
        SUMMONER(ChatFormatting.DARK_GREEN), BLINKING(ChatFormatting.LIGHT_PURPLE);

        final ChatFormatting color;

        Affix(ChatFormatting color) {
            this.color = color;
        }

        String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static boolean isElite(Entity e) {
        return e.getTags().contains(TAG);
    }

    static List<Affix> affixes(Entity e) {
        List<Affix> out = new ArrayList<>();
        for (String s : e.getPersistentData().getString(DATA).split(",")) {
            try {
                if (!s.isEmpty()) out.add(Affix.valueOf(s));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return out;
    }

    static boolean has(Entity e, Affix a) {
        return isElite(e) && e.getPersistentData().getString(DATA).contains(a.name());
    }

    // ------------------------------------------------------------------ появление
    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent e) {
        Mob m = e.getEntity();
        if (!(m instanceof Enemy) || m instanceof YukiBoss || !(m.level() instanceof ServerLevel lvl)) return;
        if (e.getSpawnType() != MobSpawnType.NATURAL && e.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        // шанс растёт с удалением от точки появления мира: 4% у спавна, до 10% вдали
        double dist = Math.sqrt(m.blockPosition().distSqr(lvl.getSharedSpawnPos()));
        float chance = 0.04f + (float) Math.min(0.06, dist / 50_000);
        if (lvl.random.nextFloat() < chance) {
            int n = lvl.random.nextFloat() < 0.25f ? 2 : 1;
            make(m, n, 1f);
        }
    }

    /** Сделать моба элитным с n случайными способностями; power — множитель силы (вожаки вторжения сильнее). */
    public static void make(Mob m, int n, float power) {
        RandomSource r = m.getRandom();
        List<Affix> pool = new ArrayList<>(List.of(Affix.values()));
        List<Affix> picked = new ArrayList<>();
        for (int i = 0; i < n && !pool.isEmpty(); i++) picked.add(pool.remove(r.nextInt(pool.size())));
        m.addTag(TAG);
        StringBuilder sb = new StringBuilder();
        for (Affix a : picked) sb.append(a.name()).append(',');
        m.getPersistentData().putString(DATA, sb.toString());
        m.setPersistenceRequired();

        double hp = 2.5 * power, dmg = 1.4 * power;
        if (picked.contains(Affix.GIANT)) {
            hp *= 2;
            dmg *= 1.4;
            mod(m.getAttribute(Attributes.SCALE), "elite_scale", 0.6, AttributeModifier.Operation.ADD_VALUE);
        }
        mod(m.getAttribute(Attributes.MAX_HEALTH), "elite_health", hp - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        mod(m.getAttribute(Attributes.ATTACK_DAMAGE), "elite_damage", dmg - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        mod(m.getAttribute(Attributes.FOLLOW_RANGE), "elite_follow", 16, AttributeModifier.Operation.ADD_VALUE);
        if (picked.contains(Affix.SWIFT)) mod(m.getAttribute(Attributes.MOVEMENT_SPEED), "elite_speed", 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if (picked.contains(Affix.ARMORED)) {
            mod(m.getAttribute(Attributes.ARMOR), "elite_armor", 12, AttributeModifier.Operation.ADD_VALUE);
            mod(m.getAttribute(Attributes.KNOCKBACK_RESISTANCE), "elite_kb", 0.8, AttributeModifier.Operation.ADD_VALUE);
        }
        if (picked.contains(Affix.FIERY)) m.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
        m.setHealth(m.getMaxHealth());

        MutableComponent name = Component.empty();
        for (Affix a : picked) name.append(Component.translatable("elite.yukicore." + a.id()).withStyle(a.color)).append(" ");
        name.append(m.getType().getDescription().copy().withStyle(ChatFormatting.WHITE));
        m.setCustomName(name);
        m.setCustomNameVisible(true);
    }

    private static void mod(AttributeInstance inst, String id, double v, AttributeModifier.Operation op) {
        if (inst != null && !inst.hasModifier(YukiCore.id(id))) inst.addPermanentModifier(new AttributeModifier(YukiCore.id(id), v, op));
    }

    // ------------------------------------------------------------------ поведение
    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post e) {
        Entity ent = e.getEntity();
        if (ent.tickCount % 10 != 0 || !isElite(ent) || !(ent.level() instanceof ServerLevel lvl) || !(ent instanceof Mob m)) return;
        for (Affix a : affixes(m)) {
            ParticleOptions p = switch (a) {
                case FIERY -> ParticleTypes.FLAME;
                case FROZEN -> ParticleTypes.SNOWFLAKE;
                case VAMPIRIC -> ParticleTypes.DAMAGE_INDICATOR;
                case SWIFT -> ParticleTypes.CLOUD;
                case GIANT, SUMMONER -> ParticleTypes.SOUL;
                case ARMORED -> ParticleTypes.CRIT;
                case EXPLOSIVE -> ParticleTypes.SMOKE;
                case BLINKING -> ParticleTypes.PORTAL;
            };
            lvl.sendParticles(p, m.getX(), m.getY() + m.getBbHeight() * 0.6, m.getZ(), 3, 0.3, 0.4, 0.3, 0.01);
        }
        if (m.tickCount % 20 == 0 && has(m, Affix.FROZEN)) {
            for (Player pl : lvl.getEntitiesOfClass(Player.class, m.getBoundingBox().inflate(4))) {
                if (!pl.isCreative()) pl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        if (target.level().isClientSide) return;
        // элита бьёт
        if (e.getSource().getEntity() instanceof Mob m && isElite(m) && e.getSource().getDirectEntity() == m) {
            if (has(m, Affix.FIERY)) target.igniteForSeconds(4);
            if (has(m, Affix.FROZEN)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                target.setTicksFrozen(Math.min(300, target.getTicksFrozen() + 80));
            }
            if (has(m, Affix.VAMPIRIC)) m.heal(e.getAmount() * 0.4f);
        }
        // элиту бьют
        if (target instanceof Mob m && isElite(m) && target.level() instanceof ServerLevel lvl) {
            if (has(m, Affix.SUMMONER) && !m.getPersistentData().getBoolean("YukiSummoned") && m.getHealth() - e.getAmount() < m.getMaxHealth() / 2) {
                m.getPersistentData().putBoolean("YukiSummoned", true);
                for (int i = 0; i < 2; i++) {
                    Entity c = m.getType().create(lvl);
                    if (c instanceof Mob minion) {
                        minion.moveTo(m.getX() + (i * 2 - 1), m.getY(), m.getZ(), m.getYRot(), 0);
                        minion.finalizeSpawn(lvl, lvl.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.REINFORCEMENT, null);
                        if (m.getTarget() != null) minion.setTarget(m.getTarget());
                        lvl.addFreshEntity(minion);
                    }
                }
                lvl.sendParticles(ParticleTypes.SOUL, m.getX(), m.getY() + 1, m.getZ(), 40, 1, 0.5, 1, 0.05);
                lvl.playSound(null, m.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1f, 1f);
            }
            if (has(m, Affix.BLINKING) && e.getSource().getEntity() instanceof LivingEntity att && m.getRandom().nextFloat() < 0.25f) {
                Vec3 behind = att.position().subtract(att.getLookAngle().multiply(1, 0, 1).normalize().scale(2));
                lvl.sendParticles(ParticleTypes.PORTAL, m.getX(), m.getY() + 1, m.getZ(), 30, 0.4, 0.8, 0.4, 0.3);
                m.teleportTo(behind.x, att.getY(), behind.z);
                lvl.playSound(null, m.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1f, 1f);
            }
        }
    }

    // ------------------------------------------------------------------ смерть и добыча
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (!isElite(dead) || !(dead.level() instanceof ServerLevel lvl)) return;
        List<Affix> list = affixes(dead);
        if (list.contains(Affix.EXPLOSIVE)) {
            lvl.explode(dead, dead.getX(), dead.getY() + 0.5, dead.getZ(), 2.5f, Level.ExplosionInteraction.NONE);
        }
        if (!(e.getSource().getEntity() instanceof ServerPlayer killer)) return;
        RandomSource r = dead.getRandom();
        int n = list.size();
        boolean leader = dead.getPersistentData().getBoolean("YukiLeader");
        dead.spawnAtLocation(new ItemStack(ModItems.SINTARA_CHIP.get(), 2 + r.nextInt(4) + 3 * n + (leader ? 20 : 0)));
        if (r.nextFloat() < 0.12f + 0.06f * n || leader) {
            Item[] gems = {GearItems.EMBER_GEM.get(), GearItems.FROST_GEM.get(), GearItems.BLOOD_GEM.get(), GearItems.STORM_GEM.get()};
            dead.spawnAtLocation(new ItemStack(gems[r.nextInt(gems.length)]));
        }
        if (r.nextFloat() < 0.04f * n || leader) dead.spawnAtLocation(new ItemStack(ModItems.FORTUNE_BOX.get()));
        if (r.nextFloat() < 0.25f || leader) dead.spawnAtLocation(new ItemStack(WorldItems.CHAMPION_SHARD.get(), leader ? 3 : 1));
        ExperienceOrb.award(lvl, dead.position(), 15 + 15 * n + (leader ? 150 : 0));
        Contracts.progress(killer, Contracts.Type.ELITES, 1);
        CompoundTag stats = killer.getPersistentData().getCompound("YukiStats");
        stats.putInt("Elites", stats.getInt("Elites") + 1);
        killer.getPersistentData().put("YukiStats", stats);
    }

    /** Команда для тестов: элитный зомби рядом с игроком. */
    public static void spawnTest(ServerPlayer p, int affixes) {
        Mob z = EntityType.ZOMBIE.create(p.serverLevel());
        if (z == null) return;
        z.moveTo(p.getX() + 3, p.getY(), p.getZ(), 0, 0);
        z.finalizeSpawn(p.serverLevel(), p.serverLevel().getCurrentDifficultyAt(z.blockPosition()), MobSpawnType.COMMAND, null);
        make(z, affixes, 1f);
        p.serverLevel().addFreshEntity(z);
    }
}
