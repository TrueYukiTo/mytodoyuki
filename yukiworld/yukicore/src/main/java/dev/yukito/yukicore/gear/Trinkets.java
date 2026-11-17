package dev.yukito.yukicore.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

/** Снаряжение 7.4: работает, просто лёжа в инвентаре (кольца, амулет, плащ) или надетым (ботинки). */
public final class Trinkets {
    private Trinkets() {}

    public enum Kind { DOUBLE_JUMP, SECOND_CHANCE, PHOENIX_CLOAK, MINER }

    public static boolean has(Player p, Kind kind) {
        for (ItemStack s : p.getInventory().items) if (s.getItem() instanceof Trinket t && t.kind == kind) return true;
        for (ItemStack s : p.getInventory().offhand) if (s.getItem() instanceof Trinket t && t.kind == kind) return true;
        return false;
    }

    public static ItemStack find(Player p, Kind kind) {
        for (ItemStack s : p.getInventory().items) if (s.getItem() instanceof Trinket t && t.kind == kind) return s;
        for (ItemStack s : p.getInventory().offhand) if (s.getItem() instanceof Trinket t && t.kind == kind) return s;
        return ItemStack.EMPTY;
    }

    public static boolean wearing(Player p, boolean skates) {
        return p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Boots b && b.skates == skates;
    }

    public static class Trinket extends Item {
        final Kind kind;

        public Trinket(Properties props, Kind kind) {
            super(props);
            this.kind = kind;
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return kind == Kind.SECOND_CHANCE || kind == Kind.PHOENIX_CLOAK;
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            if (!(entity instanceof Player p)) return;
            if (kind == Kind.PHOENIX_CLOAK && !p.onGround() && !p.isShiftKeyDown() && !p.getAbilities().flying && !p.isFallFlying()
                    && p.getDeltaMovement().y < -0.25 && !p.isInWater()) {
                // планирование: плавный спуск вперёд по взгляду, огненный след
                var look = p.getLookAngle();
                p.setDeltaMovement(p.getDeltaMovement().x * 0.9 + look.x * 0.05, -0.18, p.getDeltaMovement().z * 0.9 + look.z * 0.05);
                p.resetFallDistance();
                if (level instanceof ServerLevel s && p.tickCount % 2 == 0) {
                    s.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 0.8, p.getZ(), 3, 0.3, 0.2, 0.3, 0.01);
                }
            }
            if (level.isClientSide || p.tickCount % 40 != 0) return;
            if (kind == Kind.MINER) {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 1, true, false, true));
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath(), tooltip);
        }
    }

    /** Ботинки рывка и ледяные коньки. */
    public static class Boots extends ArmorItem {
        final boolean skates;

        public Boots(Holder<ArmorMaterial> material, Properties props, boolean skates) {
            super(material, Type.BOOTS, props);
            this.skates = skates;
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            if (!skates || !(entity instanceof Player p) || p.getItemBySlot(EquipmentSlot.FEET) != stack) return;
            BlockState under = level.getBlockState(p.blockPosition().below());
            if (under.is(net.minecraft.tags.BlockTags.ICE) && p.isSprinting()) {
                // скольжение: разгон на льду
                p.setDeltaMovement(p.getDeltaMovement().multiply(1.06, 1, 1.06));
            }
            if (level.isClientSide || !p.onGround()) return;
            BlockPos c = p.blockPosition().below();
            for (BlockPos q : BlockPos.betweenClosed(c.offset(-2, 0, -2), c.offset(2, 0, 2))) {
                BlockState s = level.getBlockState(q);
                if (s.getBlock() instanceof LiquidBlock && s.getFluidState().is(Fluids.WATER) && s.getFluidState().isSource()
                        && level.getBlockState(q.above()).isAir()) {
                    level.setBlockAndUpdate(q, Blocks.FROSTED_ICE.defaultBlockState());
                    level.scheduleTick(q, Blocks.FROSTED_ICE, 60 + p.getRandom().nextInt(40));
                }
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Weapons.tip(skates ? "ice_skates" : "dash_boots", tooltip);
        }
    }
}
