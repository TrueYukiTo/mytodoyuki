package dev.yukito.yukicore.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Пепельный Владыка — повелитель вулканического Пиролита. */
public class AshLord extends Blaze {
    private final BossCore core = new BossCore(this, BossEvent.BossBarColor.RED);

    public AshLord(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Blaze.createAttributes()
                .add(Attributes.MAX_HEALTH, 1350).add(Attributes.ATTACK_DAMAGE, 16).add(Attributes.ARMOR, 14)
                .add(Attributes.FOLLOW_RANGE, 64).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.SCALE, 2.5);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        core.tick();
        LivingEntity t = getTarget();
        if (t == null) return;
        if (core.every(120)) fireNova(18);
        if (core.every(300)) core.summon(EntityType.BLAZE, 3);
        if (core.every(180) && distanceTo(t) < 8) {
            core.slam(8, 12, new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
            t.igniteForSeconds(6);
        }
    }

    private void fireNova(int count) {
        for (int i = 0; i < count; i++) {
            float a = (float) (i * Math.PI * 2 / count);
            Vec3 dir = new Vec3(Mth.cos(a), -0.05, Mth.sin(a));
            SmallFireball fb = new SmallFireball(level(), this, dir.normalize());
            fb.setPos(getX(), getY() + 1.5, getZ());
            level().addFreshEntity(fb);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        core.addPlayer(p);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        core.removePlayer(p);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }
}
