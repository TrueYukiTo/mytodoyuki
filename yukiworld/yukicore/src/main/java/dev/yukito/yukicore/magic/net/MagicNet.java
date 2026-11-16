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

    public record Select(int spell) implements CustomPacketPayload {
        public static final Type<Select> TYPE = new Type<>(YukiCore.id("select"));
        public static final StreamCodec<ByteBuf, Select> CODEC = ByteBufCodecs.VAR_INT.map(Select::new, Select::spell);

        @Override
        public Type<Select> type() {
            return TYPE;
        }
    }

    /** Улучшить: kind 0 — заклинание, 1 — талант, 2 — сброс (свиток забвения не нужен, только из экрана). */
    public record Upgrade(int kind, int index) implements CustomPacketPayload {
        public static final Type<Upgrade> TYPE = new Type<>(YukiCore.id("upgrade"));
        public static final StreamCodec<ByteBuf, Upgrade> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Upgrade::kind, ByteBufCodecs.VAR_INT, Upgrade::index, Upgrade::new);

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

    public static void register(RegisterPayloadHandlersEvent e) {
        PayloadRegistrar r = e.registrar("1");
        r.playToServer(Cast.TYPE, Cast.CODEC, (p, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) SpellCaster.castFromKey(sp, p.spell());
        });
        r.playToServer(Select.TYPE, Select.CODEC, (p, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) Magic.select(sp, p.spell());
        });
        r.playToServer(Upgrade.TYPE, Upgrade.CODEC, (p, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) Magic.upgrade(sp, p.kind(), p.index());
        });
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
