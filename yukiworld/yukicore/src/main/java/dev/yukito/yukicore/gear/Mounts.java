package dev.yukito.yukicore.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Ездовые скакуны стихий: Ледяной и Огненный. Призываются свистком, умеют летать (смотри вверх — набор высоты,
 * прямо — планирование), не получают урона от падения и растут в уровне от пройденного пути (до 10-го).
 */
public final class Mounts {
    private Mounts() {}

    static final String TAG_ICE = "yukiSteedIce", TAG_FIRE = "yukiSteedFire";
    public static final int MAX_LEVEL = 10;

    static String key(boolean ice) {
        return ice ? "YukiSteedIce" : "YukiSteedFire";
    }

    /** Данные скакуна хранятся у игрока: уровень, опыт и UUID призванного. */
    static CompoundTag data(Player p, boolean ice) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(key(ice))) root.put(key(ice), new CompoundTag());
        return root.getCompound(key(ice));
    }

    static int level(CompoundTag d) {
        return Math.max(1, d.getInt("Level"));
    }

    static int need(int level) {
        return 400 + level * 300; // блоков пути до следующего уровня
    }

    /** Теги сущностей клиенту не передаются, поэтому скакуна узнаём по имени (оно синхронизируется). */
    public static boolean isSteed(Entity e) {
        return steedKey(e) != null;
    }

    static boolean isIce(Entity e) {
        return "gear.yukicore.steed.ice".equals(steedKey(e));
    }

    private static String steedKey(Entity e) {
        if (!(e instanceof AbstractHorse) || e.getCustomName() == null) return null;
        if (e.getCustomName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
                && tc.getKey().startsWith("gear.yukicore.steed.")) return tc.getKey();
        return null;
    }

    static void applyStats(AbstractHorse h, int level) {
        h.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.28 + 0.015 * level);
        h.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30 + 4 * level);
        h.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(0.8 + 0.03 * level);
        h.setHealth(h.getMaxHealth());
    }

    public static class Whistle extends Item {
        final boolean ice;

        public Whistle(Properties props, boolean ice) {
            super(props);
            this.ice = ice;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack st = player.getItemInHand(hand);
            if (!(player instanceof ServerPlayer p)) return InteractionResultHolder.success(st);
            ServerLevel lvl = p.serverLevel();
            CompoundTag d = data(p, ice);
            if (d.hasUUID("Id") && lvl.getEntity(d.getUUID("Id")) instanceof AbstractHorse old && old.isAlive()) {
                // повторный свист — отпустить скакуна
                lvl.sendParticles(ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, old.getX(), old.getY() + 1, old.getZ(), 50, 0.6, 0.8, 0.6, 0.05);
                old.discard();
                d.remove("Id");
                p.displayClientMessage(Component.translatable("gear.yukicore.steed.dismiss"), true);
                return InteractionResultHolder.success(st);
            }
            Horse h = EntityType.HORSE.create(lvl);
            if (h == null) return InteractionResultHolder.fail(st);
            h.moveTo(p.getX(), p.getY(), p.getZ(), p.getYRot(), 0);
            h.setVariant(ice ? Variant.WHITE : Variant.CHESTNUT);
            h.tameWithName(p);
            h.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.NEUTRAL);
            h.addTag(ice ? TAG_ICE : TAG_FIRE);
            h.setPersistenceRequired();
            int lvlN = level(d);
            applyStats(h, lvlN);
            h.setCustomName(Component.translatable("gear.yukicore.steed." + (ice ? "ice" : "fire"), lvlN)
                    .withStyle(ice ? ChatFormatting.AQUA : ChatFormatting.GOLD));
            lvl.addFreshEntity(h);
            d.putUUID("Id", h.getUUID());
            lvl.sendParticles(ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, h.getX(), h.getY() + 1, h.getZ(), 80, 0.6, 1, 0.6, 0.08);
            lvl.playSound(null, p.blockPosition(), SoundEvents.HORSE_AMBIENT, SoundSource.PLAYERS, 1.2f, ice ? 1.3f : 0.8f);
            p.getCooldowns().addCooldown(this, 40);
            return InteractionResultHolder.success(st);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip(ice ? "frost_steed_whistle" : "flame_steed_whistle", tooltip);
        }
    }

    /** Тик скакуна: полёт (на обеих сторонах — скакуном под игроком управляет клиент), следы, опыт за путь. */
    static void tick(AbstractHorse h) {
        boolean ice = isIce(h);
        if (h.getControllingPassenger() instanceof Player r) {
            Vec3 v = h.getDeltaMovement();
            float pitch = r.getXRot();
            if (pitch < -25 && v.horizontalDistanceSqr() > 0.0004) {
                h.setDeltaMovement(v.x, Math.min(0.45, v.y + 0.12), v.z);       // смотри вверх — набор высоты
            } else if (!h.onGround() && v.y < -0.1) {
                h.setDeltaMovement(v.x * 1.02, -0.1, v.z * 1.02);              // в воздухе — планирование
            }
            h.resetFallDistance();
            r.resetFallDistance();
        }
        if (!(h.level() instanceof ServerLevel lvl)) return;
        if (!(h.getControllingPassenger() instanceof ServerPlayer rider)) {
            if (h.tickCount % 40 == 0 && h.getOwnerUUID() != null && lvl.getPlayerByUUID(h.getOwnerUUID()) == null) h.discard();
            return;
        }
        if (h.tickCount % 3 == 0) {
            lvl.sendParticles(ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, h.getX(), h.getY() + 0.3, h.getZ(), 2, 0.3, 0.1, 0.3, 0.01);
        }
        if (ice && h.onGround()) {
            BlockPos c = h.blockPosition().below();
            for (BlockPos q : BlockPos.betweenClosed(c.offset(-2, 0, -2), c.offset(2, 0, 2))) {
                var s = lvl.getBlockState(q);
                if (s.getBlock() instanceof LiquidBlock && s.getFluidState().is(Fluids.WATER) && s.getFluidState().isSource()
                        && lvl.getBlockState(q.above()).isAir()) {
                    lvl.setBlockAndUpdate(q, Blocks.FROSTED_ICE.defaultBlockState());
                    lvl.scheduleTick(q, Blocks.FROSTED_ICE, 60 + h.getRandom().nextInt(40));
                }
            }
        }
        if (!ice && h.tickCount % 20 == 0) {
            h.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            rider.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
        }
        // опыт: пройденный путь
        CompoundTag d = data(rider, ice);
        // путь по реальным координатам: скорость скакуна под игроком на сервере не видна
        CompoundTag hp = h.getPersistentData();
        double moved = hp.contains("LX") ? Math.min(3, Math.hypot(h.getX() - hp.getDouble("LX"), h.getZ() - hp.getDouble("LZ"))) : 0;
        hp.putDouble("LX", h.getX());
        hp.putDouble("LZ", h.getZ());
        if (moved > 0.05) {
            double dist = d.getDouble("Dist") + moved;
            int level = level(d);
            if (level < MAX_LEVEL && dist >= need(level)) {
                dist -= need(level);
                level++;
                d.putInt("Level", level);
                applyStats(h, level);
                h.setCustomName(Component.translatable("gear.yukicore.steed." + (ice ? "ice" : "fire"), level)
                        .withStyle(ice ? ChatFormatting.AQUA : ChatFormatting.GOLD));
                rider.displayClientMessage(Component.translatable("gear.yukicore.steed.levelup", level)
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
                lvl.playSound(null, h.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1f, 1.2f);
            }
            d.putDouble("Dist", dist);
        }
    }
}
