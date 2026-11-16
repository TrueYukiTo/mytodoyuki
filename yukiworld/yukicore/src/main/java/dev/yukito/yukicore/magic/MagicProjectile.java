package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.entity.boss.YukiBoss;
import dev.yukito.yukicore.registry.ModItems;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * Магический снаряд: ледяные стрелы Юки, огненные шары Синтары, валуны, метеоры и сферы боссов.
 * Один тип сущности — внешний вид задаёт предмет-«сфера», размер синхронизируется с клиентом.
 */
public class MagicProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(MagicProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HEAVY = SynchedEntityData.defineId(MagicProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> ELEMENT = SynchedEntityData.defineId(MagicProjectile.class, EntityDataSerializers.BYTE);

    public enum Kind {
        FROST(Element.ICE, () -> ModItems.ORB_FROST.get(), 0.6f, false),
        FLAME(Element.FIRE, () -> ModItems.ORB_FLAME.get(), 0.6f, false),
        VOID(Element.VOID, () -> ModItems.ORB_VOID.get(), 0.7f, false),
        STAR(Element.STAR, () -> ModItems.ORB_STAR.get(), 0.6f, false),
        BOULDER(Element.ICE, () -> ModItems.ORB_BOULDER.get(), 1.6f, true),
        METEOR(Element.FIRE, () -> ModItems.ORB_METEOR.get(), 1.8f, true),
        ICICLE(Element.ICE, () -> ModItems.ORB_ICICLE.get(), 0.9f, true),
        FEATHER(Element.FIRE, () -> ModItems.ORB_FEATHER.get(), 0.6f, false),
        BLADE(Element.STAR, () -> ModItems.ORB_BLADE.get(), 1.2f, true);

        final Element element;
        final Supplier<Item> item;
        final float size;
        final boolean heavy;

        Kind(Element element, Supplier<Item> item, float size, boolean heavy) {
            this.element = element;
            this.item = item;
            this.size = size;
            this.heavy = heavy;
        }
    }

    private float damage = 6;
    private float aoe;
    private int power = 1;
    private int life = 120;
    private Entity homing;

    public MagicProjectile(EntityType<? extends MagicProjectile> type, Level level) {
        super(type, level);
    }

    public static MagicProjectile create(Level level, LivingEntity owner, Kind kind, Vec3 from, float damage, float aoe) {
        MagicProjectile p = new MagicProjectile(ModMagic.PROJECTILE.get(), level);
        p.setOwner(owner);
        p.setPos(from);
        p.setItem(new ItemStack(kind.item.get()));
        p.entityData.set(SIZE, kind.size);
        p.entityData.set(HEAVY, kind.heavy);
        p.entityData.set(ELEMENT, (byte) kind.element.ordinal());
        p.damage = damage;
        p.aoe = aoe;
        return p;
    }

    public MagicProjectile power(int power) {
        this.power = power;
        return this;
    }

    public MagicProjectile size(float size) {
        entityData.set(SIZE, size);
        return this;
    }

    public MagicProjectile homing(Entity target) {
        this.homing = target;
        return this;
    }

    public MagicProjectile life(int ticks) {
        this.life = ticks;
        return this;
    }

    public MagicProjectile launch(Vec3 velocity) {
        setDeltaMovement(velocity);
        double h = velocity.horizontalDistance();
        setYRot((float) (Math.atan2(velocity.x, velocity.z) * 180 / Math.PI));
        setXRot((float) (Math.atan2(velocity.y, h) * 180 / Math.PI));
        level().addFreshEntity(this);
        return this;
    }

    public float size() {
        return entityData.get(SIZE);
    }

    public Element element() {
        return Element.values()[entityData.get(ELEMENT)];
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(SIZE, 0.6f);
        b.define(HEAVY, false);
        b.define(ELEMENT, (byte) 0);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ORB_FROST.get();
    }

    @Override
    protected double getDefaultGravity() {
        return entityData.get(HEAVY) ? 0.04 : 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Element el = element();
            int n = Math.max(1, (int) (size() * 2));
            for (int i = 0; i < n; i++) {
                level().addParticle(el.particle(), getX() + (random.nextDouble() - 0.5) * size(),
                        getY() + (random.nextDouble() - 0.5) * size(), getZ() + (random.nextDouble() - 0.5) * size(), 0, 0, 0);
            }
            if (el == Element.FIRE && size() > 1) {
                level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 0, 0.02, 0);
            }
            return;
        }
        if (homing != null && homing.isAlive()) {
            Vec3 v = getDeltaMovement();
            Vec3 want = homing.getBoundingBox().getCenter().subtract(position()).normalize().scale(Math.max(0.6, v.length()));
            setDeltaMovement(v.lerp(want, 0.09));
        }
        if (tickCount > life || getY() < level().getMinBuildHeight() - 16) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        if (!super.canHitEntity(e)) return false;
        return isEnemy(getOwner(), e);
    }

    /** Свой/чужой: игроки не ранят игроков и своих питомцев, боссы — себя и приспешников. */
    public static boolean isEnemy(Entity owner, Entity e) {
        if (owner == null) return true;
        if (e == owner || e instanceof MagicProjectile) return false;
        if (owner instanceof Player p) {
            if (e instanceof Player || Fx.isAlly(e)) return false;
            if (e instanceof TamableAnimal t && t.isOwnedBy(p)) return false;
            return true;
        }
        if (owner instanceof YukiBoss) return !(e instanceof YukiBoss) && !e.getTags().contains(YukiBoss.MINION_TAG);
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        super.onHitEntity(r);
        if (level().isClientSide || aoe > 0) return;
        if (r.getEntity() instanceof LivingEntity le) {
            if (getOwner() instanceof net.minecraft.server.level.ServerPlayer sp) {
                Fx.hit(sp, le, damage, element());
            } else {
                Element.hurt(le, source(), damage, element());
            }
            element().apply(le, power);
        }
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide || isRemoved()) return;
        if (aoe > 0) {
            if (getOwner() instanceof net.minecraft.server.level.ServerPlayer sp) {
                Fx.area(sp, position(), aoe, damage, element(), e -> element().apply(e, power));
                if (aoe >= 3.5) Fx.shake((net.minecraft.server.level.ServerLevel) level(), position(), aoe * 4, Math.min(2f, aoe * 0.3f), 10);
            } else {
                explode(level(), getOwner(), position(), aoe, damage, element(), power, source());
            }
        }
        if (level() instanceof ServerLevel s) {
            Element el = element();
            s.sendParticles(el.burst(), getX(), getY(), getZ(), 12 + (int) (aoe * 8), aoe * 0.4 + 0.2, 0.3, aoe * 0.4 + 0.2, 0.05);
            s.sendParticles(el.particle(), getX(), getY(), getZ(), 16 + (int) (aoe * 10), aoe * 0.5 + 0.2, 0.4, aoe * 0.5 + 0.2, 0.08);
            if (aoe >= 2.5) s.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            SoundEvent snd = switch (el) {
                case ICE -> SoundEvents.GLASS_BREAK;
                case FIRE -> SoundEvents.FIRECHARGE_USE;
                case VOID -> SoundEvents.SHULKER_BULLET_HIT;
                default -> SoundEvents.AMETHYST_BLOCK_BREAK;
            };
            s.playSound(null, getX(), getY(), getZ(), snd, SoundSource.HOSTILE, 1f + aoe * 0.2f, 0.8f + random.nextFloat() * 0.4f);
        }
        discard();
    }

    private DamageSource source() {
        Entity o = getOwner();
        return o instanceof LivingEntity le ? damageSources().indirectMagic(this, le) : damageSources().magic();
    }

    /** Взрыв по площади со стихийным эффектом. */
    public static void explode(Level level, Entity owner, Vec3 at, double radius, float damage, Element element, int power,
                               DamageSource src) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(at, at).inflate(radius), e -> e.isAlive() && isEnemy(owner, e))) {
            double d = e.position().distanceTo(at);
            if (d > radius + e.getBbWidth()) continue;
            float k = (float) (1.0 - 0.4 * Math.min(1, d / Math.max(0.1, radius)));
            Element.hurt(e, src, damage * k, element);
            element.apply(e, power);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        tag.putFloat("Aoe", aoe);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("Damage");
        aoe = tag.getFloat("Aoe");
    }
}
