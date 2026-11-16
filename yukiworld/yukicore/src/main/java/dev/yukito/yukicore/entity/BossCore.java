package dev.yukito.yukicore.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Общая логика боссов YukiCore: полоса здоровья, фазы, способности. */
public final class BossCore {
    private final Mob mob;
    private final ServerBossEvent bar;
    private boolean enraged;

    public BossCore(Mob mob, BossEvent.BossBarColor color) {
        this.mob = mob;
        this.bar = new ServerBossEvent(mob.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bar.setDarkenScreen(true);
    }

    public void tick() {
        bar.setProgress(mob.getHealth() / mob.getMaxHealth());
        if (!enraged && mob.getHealth() < mob.getMaxHealth() / 2) {
            enraged = true;
            bar.setName(Component.translatable("boss.yukicore.enraged", mob.getDisplayName()));
            if (mob.level() instanceof ServerLevel s) {
                s.sendParticles(ParticleTypes.SONIC_BOOM, mob.getX(), mob.getY() + 1, mob.getZ(), 1, 0, 0, 0, 0);
                s.playSound(null, mob.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1f, 1.4f);
            }
        }
    }

    /** Во второй фазе способности срабатывают вдвое чаще. */
    public boolean every(int ticks) {
        int t = enraged ? Math.max(10, ticks / 2) : ticks;
        return mob.tickCount % t == 0;
    }

    public boolean enraged() {
        return enraged;
    }

    public void addPlayer(ServerPlayer p) {
        bar.addPlayer(p);
    }

    public void removePlayer(ServerPlayer p) {
        bar.removePlayer(p);
    }

    public List<Player> playersAround(double r) {
        return mob.level().getEntitiesOfClass(Player.class, new AABB(mob.blockPosition()).inflate(r),
                p -> !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Линия ледяных клыков к цели (как у Вызывателя). */
    public void fangLine(LivingEntity target, int count) {
        double dx = target.getX() - mob.getX(), dz = target.getZ() - mob.getZ();
        float yaw = (float) Mth.atan2(dz, dx);
        for (int i = 1; i <= count; i++) {
            double d = 1.25 * i;
            mob.level().addFreshEntity(new EvokerFangs(mob.level(), mob.getX() + Mth.cos(yaw) * d, mob.getY(),
                    mob.getZ() + Mth.sin(yaw) * d, yaw, i, mob));
        }
    }

    /** Кольцо клыков вокруг босса. */
    public void fangRing(double radius, int count) {
        for (int i = 0; i < count; i++) {
            float a = (float) (i * Math.PI * 2 / count);
            mob.level().addFreshEntity(new EvokerFangs(mob.level(), mob.getX() + Mth.cos(a) * radius, mob.getY(),
                    mob.getZ() + Mth.sin(a) * radius, a, 3, mob));
        }
    }

    /** Удар по земле: урон, отбрасывание и эффект всем рядом. */
    public void slam(double radius, float damage, MobEffectInstance effect) {
        for (Player p : playersAround(radius)) {
            p.hurt(mob.damageSources().mobAttack(mob), damage);
            Vec3 push = p.position().subtract(mob.position()).normalize().scale(1.4);
            p.push(push.x, 0.7, push.z);
            p.hurtMarked = true;
            if (effect != null) p.addEffect(new MobEffectInstance(effect));
        }
        if (mob.level() instanceof ServerLevel s) {
            s.sendParticles(ParticleTypes.EXPLOSION_EMITTER, mob.getX(), mob.getY(), mob.getZ(), 1, 0, 0, 0, 0);
            s.sendParticles(ParticleTypes.SNOWFLAKE, mob.getX(), mob.getY() + 0.5, mob.getZ(), 80, radius / 2, 0.3, radius / 2, 0.1);
        }
        mob.level().playSound(null, mob.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2f, 0.7f);
    }

    /** Призыв приспешников. */
    public <T extends Mob> void summon(EntityType<T> type, int count) {
        if (!(mob.level() instanceof ServerLevel s)) return;
        for (int i = 0; i < count; i++) {
            T m = type.create(s);
            if (m == null) continue;
            m.moveTo(mob.getX() + mob.getRandom().nextGaussian() * 3, mob.getY() + 0.5,
                    mob.getZ() + mob.getRandom().nextGaussian() * 3, mob.getYRot(), 0);
            m.finalizeSpawn(s, s.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            if (mob.getTarget() != null) m.setTarget(mob.getTarget());
            s.addFreshEntity(m);
            s.sendParticles(ParticleTypes.POOF, m.getX(), m.getY() + 1, m.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        }
    }

    /** Телепорт за спину цели. */
    public void blinkBehind(LivingEntity target) {
        Vec3 look = target.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 dst = target.position().subtract(look.scale(3));
        if (mob.level() instanceof ServerLevel s) {
            s.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + 1, mob.getZ(), 40, 0.4, 1, 0.4, 0.05);
        }
        mob.teleportTo(dst.x, target.getY(), dst.z);
        mob.level().playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1f, 0.6f);
    }
}
