package dev.yukito.yukicore.entity;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;

/** Пустотный Змей — кошмар тёмной планеты Никта. */
public class VoidWyrm extends Phantom {
    private final BossCore core = new BossCore(this, BossEvent.BossBarColor.PURPLE);

    public VoidWyrm(EntityType<? extends Phantom> type, Level level) {
        super(type, level);
        this.xpReward = 700;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000).add(Attributes.ATTACK_DAMAGE, 18).add(Attributes.ARMOR, 10)
                .add(Attributes.FOLLOW_RANGE, 96).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void tick() {
        if (getPhantomSize() != 12) setPhantomSize(12);
        super.tick();
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        core.tick();
        LivingEntity t = getTarget();
        if (t == null) return;
        if (core.every(80)) {
            for (int i = 0; i < 3; i++) {
                level().addFreshEntity(new ShulkerBullet(level(), this, t, Direction.Axis.values()[i]));
            }
        }
        if (core.every(220)) {
            for (Player p : core.playersAround(16)) {
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                p.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            }
        }
        if (core.enraged() && core.every(300)) core.summon(EntityType.ENDERMITE, 4);
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
