package dev.yukito.yukicore.magic;

import com.mojang.math.Transformation;
import dev.yukito.yukicore.magic.net.MagicNet;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Зрелищность магии: блоки-дисплеи (пики, стены, глыбы, лава), тряска камеры, круги частиц, урон и союзники. */
public final class Fx {
    public static final String ALLY_TAG = "yukicore_ally";

    private Fx() {}

    // ------------------------------------------------------------------ блоки-дисплеи
    /**
     * Временный «блок» (без коллизии): вырастает за grow тиков, живёт life тиков и тает.
     * pos — центр основания; sx/sy/sz — размер в блоках; tilt — наклон в радианах (для пик).
     */
    public static Display.BlockDisplay block(ServerLevel lvl, Vec3 pos, BlockState state, float sx, float sy, float sz,
                                             int grow, int life, float tiltX, float tiltZ) {
        Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(lvl);
        if (d == null) return null;
        d.moveTo(pos.x, pos.y, pos.z, 0, 0);
        d.setBlockState(state);
        d.setBrightnessOverride(new Brightness(15, 15));
        d.setViewRange(2f);
        Quaternionf rot = new Quaternionf().rotateXYZ(tiltX, 0, tiltZ);
        d.setTransformation(new Transformation(new Vector3f(-sx / 2, 0, -sz / 2), rot, new Vector3f(sx, 0.01f, sz), null));
        lvl.addFreshEntity(d);
        Scheduler.later(lvl, 2, () -> {
            d.setTransformationInterpolationDelay(0);
            d.setTransformationInterpolationDuration(Math.max(1, grow));
            d.setTransformation(new Transformation(new Vector3f(-sx / 2, 0, -sz / 2), rot, new Vector3f(sx, sy, sz), null));
        });
        Scheduler.later(lvl, Math.max(4, life - 6), () -> {
            d.setTransformationInterpolationDelay(0);
            d.setTransformationInterpolationDuration(6);
            d.setTransformation(new Transformation(new Vector3f(-sx / 2, 0, -sz / 2), rot, new Vector3f(sx, 0.01f, sz), null));
        });
        Scheduler.later(lvl, life, d::discard);
        return d;
    }

    /** Падающая глыба: появляется высоко и обрушивается вниз за fall тиков. */
    public static void fallingBlock(ServerLevel lvl, Vec3 ground, BlockState state, float size, int fall, int life) {
        Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(lvl);
        if (d == null) return;
        d.moveTo(ground.x, ground.y, ground.z, 0, 0);
        d.setBlockState(state);
        d.setBrightnessOverride(new Brightness(15, 15));
        d.setViewRange(3f);
        Quaternionf rot = new Quaternionf().rotateXYZ(0.3f, 0.5f, 0.2f);
        d.setTransformation(new Transformation(new Vector3f(-size / 2, 24, -size / 2), rot, new Vector3f(size, size, size), null));
        lvl.addFreshEntity(d);
        Scheduler.later(lvl, 2, () -> {
            d.setTransformationInterpolationDelay(0);
            d.setTransformationInterpolationDuration(fall);
            d.setTransformation(new Transformation(new Vector3f(-size / 2, -size * 0.3f, -size / 2), rot,
                    new Vector3f(size, size, size), null));
        });
        Scheduler.later(lvl, life, d::discard);
    }

    // ------------------------------------------------------------------ частицы, звук, тряска
    public static void ring(ServerLevel lvl, Vec3 c, double r, ParticleOptions p) {
        int n = (int) Math.max(16, r * 10);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            lvl.sendParticles(p, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
        }
    }

    public static void column(ServerLevel lvl, Vec3 c, double h, double r, ParticleOptions p, int n) {
        for (int i = 0; i < n; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2, rr = lvl.random.nextDouble() * r;
            lvl.sendParticles(p, c.x + Math.cos(a) * rr, c.y + lvl.random.nextDouble() * h, c.z + Math.sin(a) * rr,
                    1, 0, 0.05, 0, 0.02);
        }
    }

    public static void burst(ServerLevel lvl, Vec3 c, ParticleOptions p, int n, double spread, double speed) {
        lvl.sendParticles(p, c.x, c.y, c.z, n, spread, spread, spread, speed);
    }

    public static void sound(ServerLevel lvl, Vec3 c, SoundEvent e, float vol, float pitch) {
        lvl.playSound(null, c.x, c.y, c.z, e, SoundSource.PLAYERS, vol, pitch);
    }

    /** Тряска камеры у всех игроков в радиусе. */
    public static void shake(ServerLevel lvl, Vec3 c, double radius, float strength, int ticks) {
        for (ServerPlayer p : lvl.players()) {
            double d = p.position().distanceTo(c);
            if (d > radius) continue;
            float k = (float) (strength * (1 - d / radius * 0.6));
            PacketDistributor.sendToPlayer(p, new MagicNet.Shake(k, ticks));
        }
    }

    // ------------------------------------------------------------------ урон
    /** Урон заклинанием: без кадров неуязвимости (многоударные чары работают), с учётом пассивок. */
    public static boolean hit(ServerPlayer p, LivingEntity e, float dmg, Element el) {
        if (e == p || !e.isAlive() || !MagicProjectile.isEnemy(p, e)) return false;
        MagicData d = Magic.get(p);
        if (el == Element.ICE && e.hasEffect(ModMagic.FROZEN)) dmg *= 1 + 0.15f * d.rank(Skill.SHATTER);
        if (el == Element.FIRE && e.isOnFire()) dmg *= 1 + 0.08f * d.rank(Skill.IGNITE);
        e.invulnerableTime = 0;
        boolean ok = Element.hurt(e, p.damageSources().indirectMagic(p, p), dmg, el);
        if (ok && el == Element.ICE && d.rank(Skill.FROSTBITE) > 0) {
            MobEffectInstance cur = e.getEffect(ModMagic.FROSTBITE);
            int amp = Math.min(d.rank(Skill.FROSTBITE) + 1, cur == null ? 0 : cur.getAmplifier() + 1);
            e.addEffect(new MobEffectInstance(ModMagic.FROSTBITE, 120, amp));
        }
        if (ok && el == Element.FIRE) e.igniteForSeconds(4 + d.rank(Skill.IGNITE));
        return ok;
    }

    /** Урон по площади (шар). */
    public static List<LivingEntity> area(ServerPlayer p, Vec3 c, double r, float dmg, Element el, Consumer<LivingEntity> extra) {
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r),
                e -> e != p && e.isAlive() && MagicProjectile.isEnemy(p, e))) {
            if (e.getBoundingBox().getCenter().distanceTo(c) > r + e.getBbWidth() / 2 + 0.5) continue;
            if (hit(p, e, dmg, el)) {
                out.add(e);
                if (extra != null) extra.accept(e);
            }
        }
        return out;
    }

    public static void push(Vec3 from, LivingEntity e, double k, double up) {
        Vec3 d = e.position().subtract(from).multiply(1, 0, 1);
        d = d.lengthSqr() < 1e-4 ? new Vec3(0, 0, 0) : d.normalize().scale(k);
        e.push(d.x, up, d.z);
        e.hurtMarked = true;
    }

    /** Точка взгляда (блок или максимум дальности), опущенная на землю. */
    public static Vec3 target(ServerPlayer p, double dist) {
        Vec3 eye = p.getEyePosition();
        HitResult hit = p.level().clip(new ClipContext(eye, eye.add(p.getLookAngle().scale(dist)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, p));
        return ground(p.serverLevel(), hit.getLocation());
    }

    public static Vec3 ground(ServerLevel lvl, Vec3 at) {
        HitResult down = lvl.clip(new ClipContext(at.add(0, 1, 0), at.add(0, -24, 0), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return down.getType() == HitResult.Type.MISS ? at : down.getLocation();
    }

    /** Ближайший враг под прицелом (луч с допуском). */
    public static LivingEntity aimed(ServerPlayer p, double dist) {
        Vec3 eye = p.getEyePosition(), dir = p.getLookAngle();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().expandTowards(dir.scale(dist)).inflate(3),
                e -> e != p && e.isAlive() && MagicProjectile.isEnemy(p, e))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(dir);
            if (along < 0 || along > dist) continue;
            double off = to.subtract(dir.scale(along)).length();
            if (off > 2.5 + e.getBbWidth()) continue;
            double score = off * 4 + along * 0.1;
            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return best;
    }

    /** Враги рядом с точкой, ближние первыми. */
    public static List<LivingEntity> enemies(ServerPlayer p, Vec3 c, double r) {
        List<LivingEntity> l = new ArrayList<>(p.level().getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r),
                e -> e != p && e.isAlive() && MagicProjectile.isEnemy(p, e) && !(e instanceof net.minecraft.world.entity.player.Player)));
        l.sort((a, b) -> Double.compare(a.distanceToSqr(c), b.distanceToSqr(c)));
        return l;
    }

    // ------------------------------------------------------------------ союзники
    public static <T extends Mob> void summonAllies(ServerPlayer p, EntityType<T> type, int count, int seconds, String nameKey,
                                                     double health, double damage, float scale) {
        ServerLevel lvl = p.serverLevel();
        for (int i = 0; i < count; i++) {
            T m = type.create(lvl);
            if (m == null) continue;
            double a = i * Math.PI * 2 / count;
            m.moveTo(p.getX() + Math.cos(a) * 2, p.getY() + 0.2, p.getZ() + Math.sin(a) * 2, p.getYRot(), 0);
            m.finalizeSpawn(lvl, lvl.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            m.addTag(ALLY_TAG);
            m.getPersistentData().putUUID("yukiOwner", p.getUUID());
            m.getPersistentData().putLong("yukiExpire", lvl.getGameTime() + seconds * 20L);
            m.setCustomName(Component.translatable(nameKey));
            m.setPersistenceRequired();
            if (m.getAttribute(Attributes.MAX_HEALTH) != null) {
                m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
                m.setHealth((float) health);
            }
            if (m.getAttribute(Attributes.ATTACK_DAMAGE) != null) m.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
            if (m.getAttribute(Attributes.SCALE) != null) m.getAttribute(Attributes.SCALE).setBaseValue(scale);
            lvl.addFreshEntity(m);
            burst(lvl, m.position().add(0, 1, 0), net.minecraft.core.particles.ParticleTypes.POOF, 20, 0.5, 0.05);
        }
    }

    public static boolean isAlly(net.minecraft.world.entity.Entity e) {
        return e.getTags().contains(ALLY_TAG);
    }
}
