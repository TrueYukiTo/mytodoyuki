package dev.yukito.yukicore.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ледяной Колосс — хранитель планеты Юкия. */
public class FrostColossus extends IronGolem {
    private final BossCore core = new BossCore(this, BossEvent.BossBarColor.BLUE);

    public FrostColossus(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return IronGolem.createAttributes()
                .add(Attributes.MAX_HEALTH, 900).add(Attributes.ATTACK_DAMAGE, 22).add(Attributes.ARMOR, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.SCALE, 2.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16f));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        core.tick();
        LivingEntity t = getTarget();
        if (t == null) return;
        if (core.every(90)) core.fangLine(t, 12);
        if (core.every(160) && distanceTo(t) < 9) {
            core.slam(9, 14, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
        }
        if (core.enraged() && core.every(240)) core.summon(EntityType.STRAY, 3);
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
