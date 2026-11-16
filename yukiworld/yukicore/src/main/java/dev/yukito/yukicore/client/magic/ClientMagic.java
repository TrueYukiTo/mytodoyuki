package dev.yukito.yukicore.client.magic;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.Spell;
import dev.yukito.yukicore.magic.net.MagicNet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Клавиши магии: R — сотворить, Z — следующее заклинание, K — Гримуар стихий. */
public final class ClientMagic {
    public static final String CATEGORY = "key.categories.yukicore";
    public static final KeyMapping CAST = new KeyMapping("key.yukicore.cast", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping NEXT = new KeyMapping("key.yukicore.next_spell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping GRIMOIRE = new KeyMapping("key.yukicore.grimoire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

    private ClientMagic() {}

    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(CAST);
        e.register(NEXT);
        e.register(GRIMOIRE);
    }

    public static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (CAST.consumeClick()) PacketDistributor.sendToServer(new MagicNet.Cast(-1));
        while (NEXT.consumeClick()) {
            MagicData d = Magic.get(mc.player);
            Spell s = d.nextUnlocked(mc.player.isShiftKeyDown() ? -1 : 1);
            if (s != null) {
                d.selected = s.ordinal();
                PacketDistributor.sendToServer(new MagicNet.Select(s.ordinal()));
                mc.player.displayClientMessage(Component.translatable("spell.yukicore." + s.id())
                        .withStyle(s.school == dev.yukito.yukicore.magic.School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD), true);
            }
        }
        while (GRIMOIRE.consumeClick()) openGrimoire();
    }

    public static void openGrimoire() {
        Minecraft.getInstance().setScreen(new GrimoireScreen());
    }
}
