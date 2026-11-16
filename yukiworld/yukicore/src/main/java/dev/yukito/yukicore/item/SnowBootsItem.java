package dev.yukito.yukicore.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.tags.BlockTags;

/** Снежные ботинки: ходьба по рыхлому снегу, скорость на снегу/льду, замораживают воду под ногами. */
public class SnowBootsItem extends YukiArmorItem {
    public SnowBootsItem(Holder<ArmorMaterial> material, Properties props) {
        super(material, Type.BOOTS, props, "snow_boots");
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player p) || p.getItemBySlot(EquipmentSlot.FEET) != stack) return;
        BlockPos below = p.blockPosition().below();
        BlockState ground = level.getBlockState(below);
        if (p.tickCount % 10 == 0 && (ground.is(BlockTags.SNOW) || ground.is(BlockTags.ICE))) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false, true));
        }
        if (p.onGround()) {
            for (BlockPos pos : BlockPos.betweenClosed(below.offset(-2, 0, -2), below.offset(2, 0, 2))) {
                if (level.getFluidState(pos).is(Fluids.WATER) && level.getBlockState(pos).is(Blocks.WATER)
                        && level.getBlockState(pos.above()).isAir()) {
                    level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                    level.scheduleTick(pos, Blocks.FROSTED_ICE, 60 + p.getRandom().nextInt(60));
                }
            }
        }
    }
}
