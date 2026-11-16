package dev.yukito.yukicore.entity.boss;

import dev.yukito.yukicore.magic.Element;
import dev.yukito.yukicore.magic.MagicProjectile;
import dev.yukito.yukicore.magic.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Основа боссов YukiCore: три фазы, очередь атак с замахом и откатом, синхронизация анимаций с клиентом,
 * стихийные слабости, защита от «сыра» (лимит урона за удар, возврат на арену, регенерация без игроков).
 */
public abstract class YukiBoss extends Monster {
    public static final String MINION_TAG = "yukicore_minion";
    private static final EntityDataAccessor<Integer> ANIM = SynchedEntityData.defineId(YukiBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(YukiBoss.class, EntityDataSerializers.INT);

    // общие анимации — модели реализуют их по-своему
    public static final int A_NONE = 0, A_SWING = 1, A_SLAM = 2, A_CAST = 3, A_CAST_UP = 4, A_ROAR = 5, A_DASH = 6,
            A_SPIN = 7, A_GROUND = 8, A_BITE = 9, A_BREATH = 10, A_THROW = 11;

    public static final int DEATH_TICKS = 60;

    protected final ServerBossEvent bar;
    private final List<BossAttack> attacks = new ArrayList<>();
    private final Map<String, Integer> cooldowns = new HashMap<>();
    private BossAttack current;
    private int attackTick;
    private int pause = 40;
    private int roar;
    private int lonely;
    private BlockPos home;
    private final Set<UUID> hitOnce = new HashSet<>();

    // клиент: момент начала текущей анимации
    public int animStart;

    protected YukiBoss(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bar = new ServerBossEvent(getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bar.setDarkenScreen(true);
        this.xpReward = 1500;
        setPersistenceRequired();
        registerAttacks();
    }

    // ------------------------------------------------------------------ описание босса
    protected abstract void registerAttacks();

    /** Стихия босса: свой урон по нему вдвое слабее. */
    public abstract Element element();

    /** Слабость: этот урон в полтора раза сильнее. */
    public abstract Element weakness();

    protected boolean flying() {
        return false;
    }

    protected double hoverHeight() {
        return 4;
    }

    protected double orbitRadius() {
        return 10;
    }

    protected double flySpeed() {
        return 0.35;
    }

    protected double chaseSpeed() {
        return 1.0;
    }

    protected double keepDistance() {
        return 3.5;
    }

    protected void onPhase(int phase) {
    }

    protected final void attack(String name, int anim, int duration, int cooldown, int minPhase, float minRange,
                                float maxRange, int weight, BossAttack.Body body) {
        attacks.add(new BossAttack(name, anim, duration, cooldown, minPhase, minRange, maxRange, weight, body));
    }

    // ------------------------------------------------------------------ данные
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(ANIM, A_NONE);
        b.define(PHASE, 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ANIM.equals(key)) animStart = tickCount;
    }

    public int anim() {
        return entityData.get(ANIM);
    }

    public int phase() {
        return entityData.get(PHASE);
    }

    /** Время текущей анимации в тиках (с долей кадра). */
    public float animTime(float ageInTicks) {
        return ageInTicks - animStart;
    }

    protected void setAnim(int a) {
        entityData.set(ANIM, a);
        animStart = tickCount;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 24f));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason,
                                        @Nullable SpawnGroupData data) {
        home = blockPosition();
        return super.finalizeSpawn(level, diff, reason, data);
    }

    // ------------------------------------------------------------------ ИИ
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (home == null) home = blockPosition();
        bar.setProgress(getHealth() / getMaxHealth());
        updatePhase();
        LivingEntity target = getTarget();
        if (target != null && (!target.isAlive() || target.distanceToSqr(this) > 64 * 64
                || (target instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            setTarget(null);
            target = null;
        }
        guardArena(target);

        if (roar > 0) {
            roar--;
            getNavigation().stop();
            if (level() instanceof ServerLevel s && roar % 5 == 0) {
                s.sendParticles(element().burst(), getX(), getY() + getBbHeight() * 0.6, getZ(), 30,
                        getBbWidth(), getBbHeight() * 0.4, getBbWidth(), 0.15);
            }
            if (roar == 0) setAnim(A_NONE);
            return;
        }

        if (current != null) {
            if (target == null) {
                endAttack();
            } else {
                getLookControl().setLookAt(target, 30f, 30f);
                current.body().tick(target, attackTick);
                if (++attackTick >= current.duration()) endAttack();
            }
        } else if (target != null) {
            move(target);
            if (--pause <= 0) pickAttack(target);
        } else if (flying()) {
            setDeltaMovement(getDeltaMovement().scale(0.8).add(0, Math.sin(tickCount * 0.05) * 0.01, 0));
        }
    }

    private void updatePhase() {
        float f = getHealth() / getMaxHealth();
        int want = f > 0.66f ? 1 : f > 0.33f ? 2 : 3;
        if (want > phase()) {
            entityData.set(PHASE, want);
            current = null;
            hitOnce.clear();
            roar = 50;
            setAnim(A_ROAR);
            bar.setName(Component.translatable("boss.yukicore.phase", getDisplayName(),
                    want == 2 ? "II" : "III").withStyle(want == 2 ? ChatFormatting.GOLD : ChatFormatting.RED));
            playSound(roarSound(), 3f, 0.7f);
            if (level() instanceof ServerLevel s) {
                s.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + getBbHeight() / 2, getZ(), 1, 0, 0, 0, 0);
                for (Player p : players(24)) {
                    Vec3 push = p.position().subtract(position()).normalize().scale(1.6);
                    p.push(push.x, 0.6, push.z);
                    p.hurtMarked = true;
                }
            }
            onPhase(want);
        }
    }

    protected SoundEvent roarSound() {
        return SoundEvents.WITHER_SPAWN;
    }

    private void pickAttack(LivingEntity target) {
        double dist = distanceTo(target);
        int total = 0;
        List<BossAttack> ok = new ArrayList<>();
        for (BossAttack a : attacks) {
            if (a.minPhase() > phase() || dist < a.minRange() || dist > a.maxRange()) continue;
            if (cooldowns.getOrDefault(a.name(), 0) > tickCount) continue;
            ok.add(a);
            total += a.weight();
        }
        if (ok.isEmpty()) {
            pause = 5;
            return;
        }
        int r = random.nextInt(total);
        for (BossAttack a : ok) {
            r -= a.weight();
            if (r < 0) {
                current = a;
                attackTick = 0;
                hitOnce.clear();
                setAnim(a.anim());
                if (!flying()) getNavigation().stop();
                return;
            }
        }
    }

    private void endAttack() {
        if (current != null) {
            float k = phase() == 1 ? 1f : phase() == 2 ? 0.8f : 0.6f;
            cooldowns.put(current.name(), tickCount + (int) (current.cooldown() * k));
        }
        current = null;
        pause = phase() == 3 ? 8 : phase() == 2 ? 16 : 24;
        setAnim(A_NONE);
    }

    private void move(LivingEntity target) {
        getLookControl().setLookAt(target, 30f, 30f);
        if (flying()) {
            double a = tickCount * 0.025 + getId();
            Vec3 want = target.position().add(Math.cos(a) * orbitRadius(), hoverHeight() + Math.sin(tickCount * 0.07) * 1.2,
                    Math.sin(a) * orbitRadius());
            Vec3 d = want.subtract(position());
            Vec3 v = getDeltaMovement().scale(0.85).add(d.normalize().scale(Math.min(d.length(), 4) * flySpeed() * 0.06));
            setDeltaMovement(v);
            Vec3 look = target.position().subtract(position());
            setYRot((float) (Mth.atan2(look.z, look.x) * 180 / Math.PI) - 90f);
            yBodyRot = getYRot();
            yHeadRot = getYRot();
        } else {
            double dist = distanceTo(target);
            if (dist > keepDistance()) {
                if (tickCount % 8 == 0) getNavigation().moveTo(target, chaseSpeed());
                // застрял внизу, а цель на возвышении — прыжок к цели
                if (tickCount % 60 == 0 && getNavigation().isDone() && dist > 8 && onGround()) {
                    Vec3 d = target.position().subtract(position()).normalize();
                    setDeltaMovement(d.x * 1.2, 1.1, d.z * 1.2);
                }
            } else {
                getNavigation().stop();
            }
        }
    }

    private void guardArena(LivingEntity target) {
        if (home == null) return;
        if (distanceToSqr(Vec3.atCenterOf(home)) > 72 * 72) {
            teleportTo(home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5);
        }
        if (players(48).isEmpty()) {
            if (++lonely > 200 && tickCount % 20 == 0) heal(getMaxHealth() * 0.02f);
        } else {
            lonely = 0;
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (flying()) {
            move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91));
            calculateEntityAnimation(true);
        } else {
            super.travel(input);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(flying());
    }

    // ------------------------------------------------------------------ урон и смерть
    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(src, amount);
        if (roar > 0) return false;
        if (src.is(DamageTypes.FALL) || src.is(DamageTypes.IN_WALL) || src.is(DamageTypes.DROWN)
                || src.is(DamageTypes.CRAMMING) || src.is(DamageTypes.CACTUS) || src.is(DamageTypes.SWEET_BERRY_BUSH)) {
            return false;
        }
        Element hit = Element.of(src);
        if (hit != Element.NONE && hit == weakness()) amount *= 1.5f;
        else if (hit != Element.NONE && hit == element()) amount *= 0.5f;
        // лимит урона за удар: ни одно оружие не убьёт босса с пары ударов
        // магия Юки и Синтары пробивает вдвое лучше (5% за удар против 2,5%)
        boolean spell = src.getEntity() instanceof Player && (hit == Element.ICE || hit == Element.FIRE);
        amount = Math.min(amount, getMaxHealth() * (spell ? 0.05f : 0.025f));
        return super.hurt(src, amount);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return false;
    }

    @Override
    public boolean addEffect(MobEffectInstance e, @Nullable Entity source) {
        if (e.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
            // боссы стряхивают контроль вчетверо быстрее
            e = new MobEffectInstance(e.getEffect(), Math.max(10, e.getDuration() / 4), Math.min(1, e.getAmplifier()));
        }
        return super.addEffect(e, source);
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (level() instanceof ServerLevel s) {
            if (deathTime % 4 == 0) {
                s.sendParticles(element().burst(), getX(), getY() + getBbHeight() / 2, getZ(), 20,
                        getBbWidth(), getBbHeight() / 2, getBbWidth(), 0.1);
                s.sendParticles(ParticleTypes.EXPLOSION, getX() + (random.nextDouble() - 0.5) * getBbWidth() * 2,
                        getY() + random.nextDouble() * getBbHeight(), getZ() + (random.nextDouble() - 0.5) * getBbWidth() * 2,
                        1, 0, 0, 0, 0);
            }
            if (deathTime == DEATH_TICKS) {
                s.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 2, 0.5, 0.5, 0.5, 0);
                s.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1, getZ(), 120, 1, 2, 1, 0.6);
                playSound(SoundEvents.GENERIC_EXPLODE.value(), 3f, 0.6f);
            }
        }
        if (deathTime >= DEATH_TICKS && !level().isClientSide && !isRemoved()) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        bar.addPlayer(p);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        bar.removePlayer(p);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("YukiPhase", phase());
        if (home != null) tag.putLong("YukiHome", home.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(PHASE, Math.max(1, tag.getInt("YukiPhase")));
        if (tag.contains("YukiHome")) home = BlockPos.of(tag.getLong("YukiHome"));
        if (hasCustomName()) bar.setName(getDisplayName());
    }

    // ------------------------------------------------------------------ инструменты атак
    public List<Player> players(double r) {
        return level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(r),
                p -> !p.isCreative() && !p.isSpectator() && p.isAlive() && p.distanceTo(this) <= r + getBbWidth());
    }

    protected ServerLevel server() {
        return (ServerLevel) level();
    }

    protected void later(int ticks, Runnable r) {
        if (level() instanceof ServerLevel s) Scheduler.later(s, ticks, () -> {
            if (isAlive()) r.run();
        });
    }

    protected Vec3 handPos() {
        return position().add(0, getBbHeight() * 0.7, 0).add(getLookAngle().scale(getBbWidth() * 0.6));
    }

    protected Vec3 aim(LivingEntity t) {
        return t.position().add(0, t.getBbHeight() * 0.5, 0);
    }

    protected float strength(float base) {
        return base * (phase() == 3 ? 1.25f : phase() == 2 ? 1.1f : 1f);
    }

    /** Урон по врагам в радиусе с отбрасыванием и стихийным эффектом. */
    public void hurtArea(Vec3 c, double r, float dmg, double knock, @Nullable Consumer<LivingEntity> extra) {
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r),
                e -> e.isAlive() && MagicProjectile.isEnemy(this, e) && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (e.position().distanceTo(c) > r + e.getBbWidth() / 2) continue;
            hitOne(e, dmg, knock, c, extra);
        }
    }

    protected void hitOne(LivingEntity e, float dmg, double knock, Vec3 from, @Nullable Consumer<LivingEntity> extra) {
        Element.hurt(e, damageSources().mobAttack(this), strength(dmg), element());
        if (knock > 0) {
            Vec3 push = e.position().subtract(from).multiply(1, 0, 1).normalize().scale(knock);
            e.push(push.x, 0.35 + knock * 0.2, push.z);
            e.hurtMarked = true;
        }
        if (extra != null) extra.accept(e);
        else element().apply(e, phase() * 2);
    }

    /** Удар конусом перед боссом. */
    protected void hurtCone(double len, double cos, float dmg, double knock) {
        Vec3 dir = getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 c = position();
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(len),
                e -> e.isAlive() && MagicProjectile.isEnemy(this, e))) {
            Vec3 to = e.position().subtract(c).multiply(1, 0, 1);
            if (to.length() > len + getBbWidth() / 2) continue;
            if (to.length() > 1.5 && to.normalize().dot(dir) < cos) continue;
            hitOne(e, dmg, knock, c, null);
        }
        if (level() instanceof ServerLevel s) {
            for (int i = -4; i <= 4; i++) {
                double a = Math.atan2(dir.z, dir.x) + i * Math.acos(cos) / 4;
                for (double d = 1.5; d <= len; d += 1.2) {
                    s.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(a) * d, getY() + 1, getZ() + Math.sin(a) * d,
                            1, 0, 0, 0, 0);
                }
            }
        }
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2f, 0.5f);
    }

    /** Один раз за атаку на каждого игрока (для рывков и пике). */
    protected void hitOnceNear(double r, float dmg, double knock) {
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r),
                e -> e.isAlive() && MagicProjectile.isEnemy(this, e))) {
            if (hitOnce.add(e.getUUID())) hitOne(e, dmg, knock, position(), null);
        }
    }

    /** Предупреждающий круг на земле. */
    public void telegraph(Vec3 c, double r, ParticleOptions p) {
        if (!(level() instanceof ServerLevel s)) return;
        int n = (int) Math.max(12, r * 10);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            s.sendParticles(p, c.x + Math.cos(a) * r, c.y + 0.15, c.z + Math.sin(a) * r, 1, 0, 0.02, 0, 0);
        }
    }

    /** Ударная волна: кольцо расширяется, перепрыгни его. */
    protected void shockwave(Vec3 c, double maxR, float dmg, ParticleOptions p) {
        Set<UUID> hit = new HashSet<>();
        int steps = (int) (maxR / 0.8);
        for (int i = 1; i <= steps; i++) {
            double r = i * 0.8;
            later(i, () -> {
                telegraph(c, r, p);
                for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r + 1),
                        e -> e.isAlive() && MagicProjectile.isEnemy(this, e) && e.onGround())) {
                    double d = e.position().multiply(1, 0, 1).distanceTo(c.multiply(1, 0, 1));
                    if (Math.abs(d - r) < 1.0 && Math.abs(e.getY() - c.y) < 2 && hit.add(e.getUUID())) {
                        hitOne(e, dmg, 1.2, c, null);
                    }
                }
            });
        }
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 2f, 0.6f);
    }

    /** Метеор/валун: круг-предупреждение, затем падение сверху. */
    protected void meteor(Vec3 at, int delay, float dmg, float aoe, MagicProjectile.Kind kind) {
        Vec3 ground = at;
        for (int i = 0; i < delay; i += 4) later(i + 1, () -> telegraph(ground, aoe, element().particle()));
        later(Math.max(1, delay - 14), () -> MagicProjectile.create(level(), this, kind, ground.add(0, 16, 0),
                strength(dmg), aoe).launch(new Vec3(0, -1.1, 0)));
    }

    protected MagicProjectile shoot(MagicProjectile.Kind kind, Vec3 from, Vec3 to, float speed, float dmg, float aoe) {
        Vec3 v = to.subtract(from).normalize().scale(speed);
        return MagicProjectile.create(level(), this, kind, from, strength(dmg), aoe).power(phase() * 2).launch(v);
    }

    /** Веер снарядов. */
    protected void fan(MagicProjectile.Kind kind, LivingEntity t, int count, double spread, float speed, float dmg, float aoe) {
        Vec3 from = handPos();
        Vec3 dir = aim(t).subtract(from).normalize();
        for (int i = 0; i < count; i++) {
            double a = (i - (count - 1) / 2.0) * spread;
            Vec3 d = dir.yRot((float) a);
            shoot(kind, from, from.add(d), speed, dmg, aoe);
        }
    }

    protected void fangLine(LivingEntity target, int count, float angle) {
        double dx = target.getX() - getX(), dz = target.getZ() - getZ();
        float yaw = (float) Mth.atan2(dz, dx) + angle;
        for (int i = 1; i <= count; i++) {
            double d = 1.25 * i;
            level().addFreshEntity(new EvokerFangs(level(), getX() + Mth.cos(yaw) * d, getY(),
                    getZ() + Mth.sin(yaw) * d, yaw, i, this));
        }
    }

    protected void fangRing(double radius, int count, int delay) {
        for (int i = 0; i < count; i++) {
            float a = (float) (i * Math.PI * 2 / count);
            level().addFreshEntity(new EvokerFangs(level(), getX() + Mth.cos(a) * radius, getY(),
                    getZ() + Mth.sin(a) * radius, a, delay, this));
        }
    }

    protected <T extends Mob> void summon(EntityType<T> type, int count, @Nullable Component name) {
        if (!(level() instanceof ServerLevel s)) return;
        for (int i = 0; i < count; i++) {
            T m = type.create(s);
            if (m == null) continue;
            m.moveTo(getX() + random.nextGaussian() * 3, getY() + 0.5, getZ() + random.nextGaussian() * 3, getYRot(), 0);
            m.finalizeSpawn(s, s.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            m.addTag(MINION_TAG);
            if (name != null) m.setCustomName(name);
            if (getTarget() != null) m.setTarget(getTarget());
            s.addFreshEntity(m);
            s.sendParticles(ParticleTypes.POOF, m.getX(), m.getY() + 1, m.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        }
    }

    protected void blink(Vec3 dst) {
        if (level() instanceof ServerLevel s) {
            s.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1, getZ(), 40, 0.4, 1, 0.4, 0.05);
        }
        teleportTo(dst.x, dst.y, dst.z);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5f, 0.6f);
    }

    /** Притянуть игроков к точке. */
    protected void pull(Vec3 c, double r, double strength) {
        for (Player p : players(r)) {
            Vec3 d = c.subtract(p.position()).normalize().scale(strength);
            p.push(d.x, d.y * 0.3, d.z);
            p.hurtMarked = true;
        }
    }

    protected void particles(ParticleOptions p, Vec3 c, int n, double spread, double speed) {
        if (level() instanceof ServerLevel s) s.sendParticles(p, c.x, c.y, c.z, n, spread, spread, spread, speed);
    }

    protected void sound(SoundEvent e, float vol, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), e, SoundSource.HOSTILE, vol, pitch);
    }

    @Override
    public void checkDespawn() {
        // боссы не исчезают
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder base(double hp, double dmg,
            double armor, double speed) {
        return Monster.createMonsterAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, hp)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, dmg)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, armor)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS, armor / 2)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, speed)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED, speed)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, 64)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT, 1.5);
    }
}
