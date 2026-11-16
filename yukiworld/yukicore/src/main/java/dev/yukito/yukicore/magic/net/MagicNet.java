package dev.yukito.yukicore.magic.net;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.SpellCaster;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Сетевые пакеты магии. */
public final class MagicNet {
    private MagicNet() {}

    /** Сотворить заклинание (spell = -1 — выбранное). */
    public record Cast(int spell) implements CustomPacketPayload {
        public static final Type<Cast> TYPE = new Type<>(YukiCore.id("cast"));
        public static final StreamCodec<ByteBuf, Cast> CODEC = ByteBufCodecs.VAR_INT.map(Cast::new, Cast::spell);

        @Override
        public Type<Cast> type() {
            return TYPE;
        }
    }

    /** Положить заклинание в ячейку быстрого доступа (slot) — или только сменить активную (skill = -1). */
    public record Assign(int slot, int skill) implements CustomPacketPayload {
        public static final Type<Assign> TYPE = new Type<>(YukiCore.id("assign"));
        public static final StreamCodec<ByteBuf, Assign> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Assign::slot, ByteBufCodecs.VAR_INT, Assign::skill, Assign::new);

        @Override
        public Type<Assign> type() {
            return TYPE;
        }
    }

    /** Изучить или улучшить навык дерева. */
    public record Upgrade(int index) implements CustomPacketPayload {
        public static final Type<Upgrade> TYPE = new Type<>(YukiCore.id("upgrade"));
        public static final StreamCodec<ByteBuf, Upgrade> CODEC = ByteBufCodecs.VAR_INT.map(Upgrade::new, Upgrade::index);

        @Override
        public Type<Upgrade> type() {
            return TYPE;
        }
    }

    public record Sync(CompoundTag data) implements CustomPacketPayload {
        public static final Type<Sync> TYPE = new Type<>(YukiCore.id("sync"));
        public static final StreamCodec<ByteBuf, Sync> CODEC = ByteBufCodecs.COMPOUND_TAG.map(Sync::new, Sync::data);

        @Override
        public Type<Sync> type() {
            return TYPE;
        }
    }

    public record Mana(float mana) implements CustomPacketPayload {
        public static final Type<Mana> TYPE = new Type<>(YukiCore.id("mana"));
        public static final StreamCodec<ByteBuf, Mana> CODEC = ByteBufCodecs.FLOAT.map(Mana::new, Mana::mana);

        @Override
        public Type<Mana> type() {
            return TYPE;
        }
    }

    /** Тряска камеры. */
    public record Shake(float strength, int ticks) implements CustomPacketPayload {
        public static final Type<Shake> TYPE = new Type<>(YukiCore.id("shake"));
        public static final StreamCodec<ByteBuf, Shake> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Shake::strength, ByteBufCodecs.VAR_INT, Shake::ticks, Shake::new);

        @Override
        public Type<Shake> type() {
            return TYPE;
        }
    }

    /** Казино: прокрутить автомат на позиции pos со ставкой bet. */
    public record Spin(net.minecraft.core.BlockPos pos, int bet) implements CustomPacketPayload {
        public static final Type<Spin> TYPE = new Type<>(YukiCore.id("spin"));
        public static final StreamCodec<ByteBuf, Spin> CODEC = StreamCodec.composite(
                net.minecraft.core.BlockPos.STREAM_CODEC, Spin::pos, ByteBufCodecs.VAR_INT, Spin::bet, Spin::new);

        @Override
        public Type<Spin> type() {
            return TYPE;
        }
    }

    public record SpinResult(int a, int b, int c, int win) implements CustomPacketPayload {
        public static final Type<SpinResult> TYPE = new Type<>(YukiCore.id("spin_result"));
        public static final StreamCodec<ByteBuf, SpinResult> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SpinResult::a, ByteBufCodecs.VAR_INT, SpinResult::b, ByteBufCodecs.VAR_INT, SpinResult::c,
                ByteBufCodecs.VAR_INT, SpinResult::win, SpinResult::new);

        @Override
        public Type<SpinResult> type() {
            return TYPE;
        }
    }

    /** Клиентский приёмник результата прокрутки. */
    public static java.util.function.Consumer<SpinResult> spinHandler = r -> {
    };

    /** Клиентский приёмник тряски (ставится клиентом, на сервере пустой). */
    public static java.util.function.BiConsumer<Float, Integer> shakeHandler = (a, b) -> {
    };

    public static void register(RegisterPayloadHandlersEvent e) {
        PayloadRegistrar r = e.registrar("1");
        r.playToServer(Cast.TYPE, Cast.CODEC, (p, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) SpellCaster.castFromKey(sp, p.spell());
        });
        r.playToServer(Assign.TYPE, Assign.CODEC, (p, ctx) -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (p.skill() < 0) Magic.setActiveSlot(sp, p.slot());
            else Magic.assign(sp, p.slot(), p.skill());
        });
        r.playToServer(Upgrade.TYPE, Upgrade.CODEC, (p, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) Magic.upgrade(sp, p.index());
        });
        r.playToServer(Spin.TYPE, Spin.CODEC, (p, ctx) -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            int[] res = dev.yukito.yukicore.casino.Casino.spin(sp, p.pos(), p.bet());
            if (res != null) PacketDistributor.sendToPlayer(sp, new SpinResult(res[0], res[1], res[2], res[3]));
        });
        r.playToClient(SpinResult.TYPE, SpinResult.CODEC, (p, ctx) -> spinHandler.accept(p));
        r.playToClient(Shake.TYPE, Shake.CODEC, (p, ctx) -> shakeHandler.accept(p.strength(), p.ticks()));
        r.playToClient(Sync.TYPE, Sync.CODEC, MagicNet::onSync);
        r.playToClient(Mana.TYPE, Mana.CODEC, (p, ctx) -> {
            Player pl = ctx.player();
            if (pl != null) Magic.get(pl).mana = p.mana();
        });
    }

    private static void onSync(Sync p, IPayloadContext ctx) {
        Player pl = ctx.player();
        if (pl == null) return;
        MagicData d = Magic.get(pl);
        d.deserializeNBT(pl.registryAccess(), p.data());
    }
}
