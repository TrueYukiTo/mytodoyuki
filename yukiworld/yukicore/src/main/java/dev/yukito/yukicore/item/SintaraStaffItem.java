package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * Посох Синтары — подарок другу. ПКМ: луч северного сияния (пронзает всех на 24 блока, 24 урона).
 * Shift+ПКМ: «Сияние» — лечит игроков рядом, даёт регенерацию и силу, подсвечивает врагов.
 */
public class SintaraStaffItem extends CoreItem {
    private static final Vector3f[] AURORA = {new Vector3f(0.42f, 0.94f, 0.75f), new Vector3f(0.88f, 0.69f, 1f),
            new Vector3f(0.55f, 0.85f, 1f)};

    public SintaraStaffItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        if (player.isShiftKeyDown()) {
            for (Player p : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(10))) {
                p.heal(8);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 30, 1));
            }
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24),
                    e -> e instanceof Enemy)) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 30, 0));
            }
            for (int i = 0; i < 120; i++) {
                double a = i * Math.PI / 30, r = 1.5 + i / 30.0;
                server.sendParticles(new DustParticleOptions(AURORA[i % 3], 1.6f), player.getX() + Math.cos(a) * r,
                        player.getY() + 0.2 + i * 0.02, player.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5f, 1.2f);
            player.getCooldowns().addCooldown(this, 20 * 20);
        } else {
            Vec3 eye = player.getEyePosition(), dir = player.getLookAngle();
            for (int i = 1; i <= 48; i++) {
                Vec3 p = eye.add(dir.scale(i * 0.5));
                server.sendParticles(new DustParticleOptions(AURORA[i % 3], 1.3f), p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0);
            }
            AABB box = player.getBoundingBox().expandTowards(dir.scale(24)).inflate(1.5);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
                double along = to.dot(dir);
                if (along < 0 || along > 24 || to.subtract(dir.scale(along)).length() > 1.6) continue;
                e.hurt(player.damageSources().indirectMagic(player, player), 24f);
                server.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 1, e.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1f, 1.4f);
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yukicore.sintara_staff.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.yukicore.sintara_staff.desc2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.yukicore.for_sintara").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }
}
