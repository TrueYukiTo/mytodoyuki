package dev.yukito.yukicore.magic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.yukito.yukicore.YukiCore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /yukimagic xp|trial|reset — для администраторов и тестов. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class MagicCommand {
    private MagicCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
        d.register(Commands.literal("yukimagic").requires(s -> s.hasPermission(2))
                .then(Commands.literal("xp").then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("school", StringArgumentType.word()).suggests((c, b) -> b.suggest("yuki").suggest("sintara").buildFuture())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> each(c, p -> Magic.addXp(p, school(c), IntegerArgumentType.getInteger(c, "amount"))))))))
                .then(Commands.literal("trial").then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("school", StringArgumentType.word()).suggests((c, b) -> b.suggest("yuki").suggest("sintara").buildFuture())
                                .executes(c -> each(c, p -> {
                                    MagicData m = Magic.get(p);
                                    m.trial[school(c).ordinal()] = true;
                                    m.dirty = true;
                                })))))
                .then(Commands.literal("mana").then(Commands.argument("targets", EntityArgument.players())
                        .executes(c -> each(c, p -> {
                            Magic.get(p).mana = Magic.maxMana(p);
                            Magic.get(p).dirty = true;
                        }))))
                .then(Commands.literal("planetevent").executes(c -> {
                    boolean ok = dev.yukito.yukicore.PlanetEvents.trigger(c.getSource().getLevel());
                    c.getSource().sendSuccess(() -> Component.literal(ok ? "Planet event started" : "Not a Yuki planet"), true);
                    return ok ? 1 : 0;
                }))
                .then(Commands.literal("reset").then(Commands.argument("targets", EntityArgument.players())
                        .executes(c -> each(c, p -> {
                            Magic.get(p).copyFrom(new MagicData());
                        })))));
    }

    private static School school(CommandContext<CommandSourceStack> c) {
        return StringArgumentType.getString(c, "school").startsWith("s") ? School.FIRE : School.ICE;
    }

    private static int each(CommandContext<CommandSourceStack> c, java.util.function.Consumer<ServerPlayer> f)
            throws CommandSyntaxException {
        int n = 0;
        for (ServerPlayer p : EntityArgument.getPlayers(c, "targets")) {
            f.accept(p);
            n++;
        }
        int count = n;
        c.getSource().sendSuccess(() -> Component.literal("YukiMagic: " + count), true);
        return n;
    }
}
