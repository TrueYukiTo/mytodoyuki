package dev.yukito.yukicore.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** Королева Метелей — хозяйка планеты Аврора. Стреляет, замораживает, зовёт свиту. */
public class BlizzardQueen extends Stray {
    private final BossCore core = new BossCore(this, BossEvent.BossBarColor.WHITE);

    public BlizzardQueen(EntityType<? extends Stray> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Stray.createAttributes()
                .add(Attributes.MAX_HEALTH, 1100).add(Attributes.ATTACK_DAMAGE, 14).add(Attributes.ARMOR, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8).add(Attributes.SCALE, 1.6);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        setDropChance(EquipmentSlot.MAINHAND, 0);
        setDropChance(EquipmentSlot.HEAD, 0);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        core.tick();
        if (tickCount % 20 == 0) {
            for (Player p : core.playersAround(14)) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                p.setTicksFrozen(Math.min(p.getTicksFrozen() + 50, 300));
            }
        }
        LivingEntity t = getTarget();
        if (t == null) return;
        if (core.every(140)) core.blinkBehind(t);
        if (core.every(200)) core.summon(EntityType.STRAY, 3);
        if (core.every(120)) core.fangRing(4, 12);
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
