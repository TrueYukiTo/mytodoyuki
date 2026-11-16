package dev.yukito.yukicore.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.level.Level;

/** Тень Юки — проклятие дракона, последний враг YukiWorld (планета Целестия). */
public class ShadowOfYuki extends Illusioner {
    private final BossCore core = new BossCore(this, BossEvent.BossBarColor.PINK);

    public ShadowOfYuki(EntityType<? extends Illusioner> type, Level level) {
        super(type, level);
        this.xpReward = 2000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Illusioner.createAttributes()
                .add(Attributes.MAX_HEALTH, 1600).add(Attributes.ATTACK_DAMAGE, 20).add(Attributes.ARMOR, 18)
                .add(Attributes.ARMOR_TOUGHNESS, 8).add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.FOLLOW_RANGE, 64).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.SCALE, 1.5);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        core.tick();
        LivingEntity t = getTarget();
        if (t == null) return;
        if (core.every(110)) core.blinkBehind(t);
        if (core.every(150)) core.fangRing(3.5, 16);
        if (core.every(90)) core.fangLine(t, 14);
        if (core.every(400)) core.summon(EntityType.VEX, 4);
        if (core.enraged() && core.every(100) && level() instanceof ServerLevel s) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(s);
            if (bolt != null) {
                bolt.moveTo(t.position());
                s.addFreshEntity(bolt);
            }
            t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
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
