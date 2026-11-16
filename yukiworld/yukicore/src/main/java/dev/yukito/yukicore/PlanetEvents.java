package dev.yukito.yukicore;

import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.MagicProjectile;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * События планет Системы Юки: раз в несколько минут на планете начинается своё явление
 * (метель, сияние, метеоритный дождь, пустотный разлом, звездопад) — с опасностями и наградами с неба.
 */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class PlanetEvents {
    private static final Map<ServerLevel, int[]> STATE = new WeakHashMap<>(); // [осталось тиков события, до следующего]

    private PlanetEvents() {}

    private static String planet(ServerLevel lvl) {
        var id = lvl.dimension().location();
        if (!id.getNamespace().equals(YukiCore.MOD_ID) || id.getPath().endsWith("_orbit")) return null;
        return switch (id.getPath()) {
            case "yukia", "aurora", "pyrolith", "nyx", "celestia" -> id.getPath();
            default -> null;
        };
    }

    /** Запустить явление сейчас (команда /yukimagic planetevent). */
    public static boolean trigger(ServerLevel lvl) {
        String pid = planet(lvl);
        if (pid == null) return false;
        int[] st = STATE.computeIfAbsent(lvl, l -> new int[]{0, 0});
        st[0] = 600;
        st[1] = 3600;
        announce(lvl, pid, true);
        return true;
    }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel lvl)) return;
        String pid = planet(lvl);
        if (pid == null || lvl.players().isEmpty()) return;
        int[] st = STATE.computeIfAbsent(lvl, l -> new int[]{0, 2400 + l.random.nextInt(2400)});
        if (st[0] > 0) {
            st[0]--;
            run(lvl, pid, st[0]);
            if (st[0] == 0) announce(lvl, pid, false);
            return;
        }
        if (--st[1] <= 0) {
            st[0] = 600 + lvl.random.nextInt(400);
            st[1] = 3600 + lvl.random.nextInt(3600);
            announce(lvl, pid, true);
        }
    }

    private static void announce(ServerLevel lvl, String pid, boolean start) {
        ChatFormatting c = switch (pid) {
            case "yukia" -> ChatFormatting.AQUA;
            case "aurora" -> ChatFormatting.LIGHT_PURPLE;
            case "pyrolith" -> ChatFormatting.GOLD;
            case "nyx" -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.YELLOW;
        };
        for (ServerPlayer p : lvl.players()) {
            if (start) {
                p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
                p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("event.yukicore." + pid).withStyle(c)));
                p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("event.yukicore." + pid + ".desc")));
                lvl.playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.AMBIENT, 0.5f, 1.4f);
            } else {
                p.displayClientMessage(Component.translatable("event.yukicore.end").withStyle(ChatFormatting.GRAY), true);
            }
        }
    }

    private static boolean coldProof(ServerPlayer p) {
        for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            Item it = p.getItemBySlot(s).getItem();
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(it).getPath();
            if (it instanceof YukiArmorItem a && (a.tier.set().equals("frost") || a.tier.set().equals("eternal")
                    || a.tier.set().equals("cryomancer") || a.tier.set().equals("yuki_regalia"))) continue;
            if (id.startsWith("frost_space_") || id.startsWith("star_space_")) continue;
            return false;
        }
        return true;
    }

    private static void gift(ServerLevel lvl, ServerPlayer p, Item item, int n, double r) {
        double x = p.getX() + (lvl.random.nextDouble() - 0.5) * r * 2, z = p.getZ() + (lvl.random.nextDouble() - 0.5) * r * 2;
        double y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z) + 12;
        ItemEntity ie = new ItemEntity(lvl, x, y, z, new ItemStack(item, n));
        ie.setDeltaMovement(0, -0.6, 0);
        ie.setGlowingTag(true);
        lvl.addFreshEntity(ie);
        for (int i = 0; i < 12; i++) lvl.sendParticles(ParticleTypes.END_ROD, x, y + i, z, 1, 0.05, 0.1, 0.05, 0);
    }

    private static void run(ServerLevel lvl, String pid, int left) {
        for (ServerPlayer p : lvl.players()) {
            if (p.isSpectator()) continue;
            Vec3 at = p.position();
            switch (pid) {
                case "yukia" -> {
                    if (left % 5 == 0) lvl.sendParticles(p, ParticleTypes.SNOWFLAKE, true, at.x, at.y + 4, at.z, 60, 8, 3, 8, 0.15);
                    if (left % 40 == 0 && !coldProof(p) && !p.isCreative()) {
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                        p.setTicksFrozen(Math.min(300, p.getTicksFrozen() + 60));
                    }
                    if (left % 120 == 0) gift(lvl, p, ModItems.YUKI_CRYSTAL.get(), 1 + lvl.random.nextInt(2), 10);
                }
                case "aurora" -> {
                    if (left % 4 == 0) {
                        for (int i = 0; i < 8; i++) {
                            double a = (left * 0.05 + i * 0.8);
                            lvl.sendParticles(p, i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.GLOW, true,
                                    at.x + Math.cos(a) * 20, at.y + 25 + Math.sin(a * 2) * 3, at.z + Math.sin(a) * 20, 3, 2, 0.5, 2, 0.01);
                        }
                    }
                    if (left % 40 == 0) {
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
                        p.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1));
                        MagicData d = Magic.get(p);
                        d.mana = Math.min(Magic.maxMana(p), d.mana + 10);
                    }
                    if (left % 100 == 0) gift(lvl, p, ModItems.AURORA_SHARD.get(), 1 + lvl.random.nextInt(2), 10);
                }
                case "pyrolith" -> {
                    if (left % 30 == 0) {
                        double x = at.x + (lvl.random.nextDouble() - 0.5) * 40, z = at.z + (lvl.random.nextDouble() - 0.5) * 40;
                        double y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                        Vec3 spot = new Vec3(x, y, z);
                        for (int i = 0; i < 24; i++) {
                            double a = i * Math.PI / 12;
                            lvl.sendParticles(ParticleTypes.FLAME, x + Math.cos(a) * 2.5, y + 0.2, z + Math.sin(a) * 2.5, 1, 0, 0, 0, 0);
                        }
                        MagicProjectile.create(lvl, null, MagicProjectile.Kind.METEOR, spot.add(4, 30, 4), 10, 2.5f).size(2.2f)
                                .launch(new Vec3(-0.13, -1.0, -0.13));
                        if (lvl.random.nextInt(3) == 0) {
                            ItemEntity ie = new ItemEntity(lvl, x, y + 1, z, new ItemStack(ModItems.RAW_PYROLITE.get(), 1 + lvl.random.nextInt(3)));
                            lvl.addFreshEntity(ie);
                        }
                    }
                    if (left % 10 == 0) lvl.sendParticles(p, ParticleTypes.ASH, true, at.x, at.y + 6, at.z, 80, 10, 4, 10, 0.02);
                }
                case "nyx" -> {
                    if (left % 100 == 0 && !p.isCreative()) p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
                    if (left % 8 == 0) lvl.sendParticles(p, ParticleTypes.REVERSE_PORTAL, true, at.x, at.y + 2, at.z, 40, 8, 3, 8, 0.05);
                    if (left % 200 == 0) {
                        var m = EntityType.ENDERMITE.create(lvl);
                        if (m != null) {
                            m.moveTo(at.x + lvl.random.nextGaussian() * 6, at.y + 1, at.z + lvl.random.nextGaussian() * 6);
                            m.finalizeSpawn(lvl, lvl.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.EVENT, null);
                            lvl.addFreshEntity(m);
                        }
                    }
                    if (left % 120 == 0) gift(lvl, p, ModItems.VOID_SHARD.get(), 1, 10);
                }
                default -> { // celestia — звездопад
                    if (left % 3 == 0) {
                        double x = at.x + (lvl.random.nextDouble() - 0.5) * 60, z = at.z + (lvl.random.nextDouble() - 0.5) * 60;
                        for (int i = 0; i < 6; i++) {
                            lvl.sendParticles(p, ParticleTypes.END_ROD, true, x + i * 0.6, at.y + 40 - i * 0.6, z + i * 0.6, 1, 0, 0, 0, 0);
                        }
                    }
                    if (left % 40 == 0) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
                    if (left % 80 == 0) gift(lvl, p, ModItems.STELLAR_SHARD.get(), 1, 12);
                }
            }
        }
    }
}
