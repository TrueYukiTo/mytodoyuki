package dev.yukito.yukicore.world;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Ночь Пустоты: иногда (примерно раз в 5 ночей) на обычный мир нападают волны монстров, часть из них — элита.
 * В полночь приходит Вожак. Кто убил хотя бы 5 захватчиков и дожил до рассвета — получает трофей.
 */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class Invasions {
    private Invasions() {}

    static final String INVADER = "yukiInvader";
    private static boolean active, leaderSpawned;
    private static long lastNight = -1;
    private static ServerBossEvent bar;

    private static final List<EntityType<? extends Mob>> TYPES = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
            EntityType.HUSK, EntityType.STRAY, EntityType.WITCH, EntityType.VINDICATOR, EntityType.PILLAGER);

    public static boolean active() {
        return active;
    }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel lvl) || lvl.dimension() != Level.OVERWORLD || lvl.getGameTime() % 20 != 0) return;
        long time = lvl.getDayTime() % 24000, day = lvl.getDayTime() / 24000;
        if (!active && time >= 13000 && time < 13100 && day != lastNight) {
            lastNight = day;
            if (!lvl.players().isEmpty() && lvl.random.nextFloat() < 0.2f) start(lvl);
            return;
        }
        if (!active) return;
        if (time < 13000 || time >= 23000) {
            end(lvl);
            return;
        }
        if (bar != null) {
            bar.setProgress(1f - (time - 13000) / 10000f);
            for (ServerPlayer p : lvl.players()) if (!bar.getPlayers().contains(p)) bar.addPlayer(p);
        }
        if (lvl.getGameTime() % 600 == 0) waves(lvl);
        if (!leaderSpawned && time >= 18000) {
            leaderSpawned = true;
            for (ServerPlayer p : lvl.players()) {
                if (p.isSpectator() || p.isCreative()) continue;
                Mob m = spawnNear(lvl, p, EntityType.ZOMBIE, true);
                if (m != null) {
                    Elites.make(m, 3, 2.5f);
                    m.getPersistentData().putBoolean("YukiLeader", true);
                    m.setCustomName(Component.translatable("invasion.yukicore.leader").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
                }
                title(p, "invasion.yukicore.leader_title", ChatFormatting.DARK_PURPLE);
                lvl.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.7f, 0.8f);
            }
        }
    }

    /** Запустить Ночь Пустоты (также командой /yukimagic invasion). Время ставится на начало ночи. */
    public static void start(ServerLevel lvl) {
        if (active) return;
        long time = lvl.getDayTime() % 24000;
        if (time < 13000 || time >= 23000) lvl.setDayTime(lvl.getDayTime() - time + 13050 + (time >= 23000 ? 24000 : 0));
        active = true;
        leaderSpawned = false;
        bar = new ServerBossEvent(Component.translatable("invasion.yukicore.bar").withStyle(ChatFormatting.DARK_PURPLE),
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
        bar.setDarkenScreen(true);
        for (ServerPlayer p : lvl.players()) {
            p.getPersistentData().putInt("YukiInvasionKills", 0);
            bar.addPlayer(p);
            title(p, "invasion.yukicore.start", ChatFormatting.DARK_PURPLE);
            lvl.playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.AMBIENT, 1f, 0.6f);
        }
        waves(lvl);
    }

    private static void end(ServerLevel lvl) {
        active = false;
        if (bar != null) {
            bar.removeAllPlayers();
            bar = null;
        }
        for (ServerPlayer p : lvl.players()) {
            int kills = p.getPersistentData().getInt("YukiInvasionKills");
            if (kills >= 5 && p.isAlive()) {
                give(p, new ItemStack(WorldItems.VOID_NIGHT_TROPHY.get()));
                give(p, new ItemStack(ModItems.SINTARA_CHIP.get(), Math.min(64, 10 + kills * 2)));
                title(p, "invasion.yukicore.victory", ChatFormatting.GOLD);
                p.sendSystemMessage(Component.translatable("invasion.yukicore.reward", kills).withStyle(ChatFormatting.GOLD));
            } else {
                p.displayClientMessage(Component.translatable("invasion.yukicore.over").withStyle(ChatFormatting.GRAY), true);
            }
            p.getPersistentData().remove("YukiInvasionKills");
        }
        // оставшиеся захватчики рассыпаются с рассветом
        for (var ent : lvl.getAllEntities()) {
            if (ent instanceof Mob m && m.getTags().contains(INVADER) && !m.getPersistentData().getBoolean("YukiLeader")) {
                lvl.sendParticles(ParticleTypes.REVERSE_PORTAL, m.getX(), m.getY() + 1, m.getZ(), 20, 0.3, 0.6, 0.3, 0.05);
                m.discard();
            }
        }
    }

    private static void waves(ServerLevel lvl) {
        for (ServerPlayer p : lvl.players()) {
            if (p.isSpectator() || p.isCreative()) continue;
            int n = 4 + lvl.random.nextInt(3) + Math.min(4, lvl.players().size());
            for (int i = 0; i < n; i++) {
                Mob m = spawnNear(lvl, p, TYPES.get(lvl.random.nextInt(TYPES.size())), false);
                if (m != null && lvl.random.nextFloat() < 0.3f) Elites.make(m, 1 + lvl.random.nextInt(2), 1f);
            }
            p.displayClientMessage(Component.translatable("invasion.yukicore.wave").withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }

    private static Mob spawnNear(ServerLevel lvl, ServerPlayer p, EntityType<? extends Mob> type, boolean close) {
        for (int tries = 0; tries < 10; tries++) {
            double a = lvl.random.nextDouble() * Math.PI * 2, r = close ? 10 : 16 + lvl.random.nextDouble() * 10;
            int x = (int) (p.getX() + Math.cos(a) * r), z = (int) (p.getZ() + Math.sin(a) * r);
            BlockPos pos = lvl.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (Math.abs(pos.getY() - p.getY()) > 12 || !lvl.getFluidState(pos.below()).isEmpty()) continue;
            Mob m = type.create(lvl);
            if (m == null) return null;
            m.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, lvl.random.nextFloat() * 360, 0);
            m.finalizeSpawn(lvl, lvl.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            m.addTag(INVADER);
            m.setPersistenceRequired();
            m.setTarget(p);
            lvl.addFreshEntity(m);
            lvl.sendParticles(ParticleTypes.REVERSE_PORTAL, m.getX(), m.getY() + 1, m.getZ(), 30, 0.3, 0.8, 0.3, 0.05);
            return m;
        }
        return null;
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (!dead.getTags().contains(INVADER) || !(e.getSource().getEntity() instanceof ServerPlayer p)) return;
        p.getPersistentData().putInt("YukiInvasionKills", p.getPersistentData().getInt("YukiInvasionKills") + 1);
        if (dead.getPersistentData().getBoolean("YukiLeader")) {
            for (ServerPlayer o : p.serverLevel().players()) {
                o.sendSystemMessage(Component.translatable("invasion.yukicore.leader_dead", p.getDisplayName()).withStyle(ChatFormatting.GOLD));
            }
        }
    }

    private static void title(ServerPlayer p, String key, ChatFormatting c) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key).withStyle(c, ChatFormatting.BOLD)));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(key + ".sub")));
    }

    private static void give(ServerPlayer p, ItemStack st) {
        if (!p.getInventory().add(st)) p.drop(st, false);
    }
}
