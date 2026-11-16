package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

/** Печать призыва: вызывает босса, но только на «его» планете. */
public class SigilItem extends CoreItem {
    private final Supplier<? extends EntityType<? extends Mob>> boss;
    private final ResourceKey<Level> dimension;

    public SigilItem(Properties properties, Supplier<? extends EntityType<? extends Mob>> boss, ResourceLocation dimension) {
        super(properties, true);
        this.boss = boss;
        this.dimension = dimension == null ? null : ResourceKey.create(Registries.DIMENSION, dimension);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        if (dimension != null && !level.dimension().equals(dimension)) {
            player.displayClientMessage(Component.translatable("message.yukicore.sigil.wrong_world",
                    Component.translatable("planet." + dimension.location().getNamespace() + "."
                            + dimension.location().getPath())).withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        Mob mob = boss.get().create(server);
        if (mob == null) return InteractionResultHolder.fail(stack);
        Vec3 at = player.position().add(player.getLookAngle().multiply(1, 0, 1).normalize().scale(6));
        mob.moveTo(at.x, player.getY() + 1, at.z, player.getYRot() + 180, 0);
        mob.finalizeSpawn(server, server.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null);
        mob.setPersistenceRequired();
        mob.setTarget(player);
        server.addFreshEntity(mob);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt != null) {
            bolt.moveTo(at);
            bolt.setVisualOnly(true);
            server.addFreshEntity(bolt);
        }
        server.playSound(null, mob.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 0.8f);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (dimension == null) {
            tooltip.add(Component.translatable("tooltip.yukicore.sigil_trial").withStyle(ChatFormatting.DARK_PURPLE));
            return;
        }
        tooltip.add(Component.translatable("tooltip.yukicore.sigil", Component.translatable(
                "planet." + dimension.location().getNamespace() + "." + dimension.location().getPath()))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
