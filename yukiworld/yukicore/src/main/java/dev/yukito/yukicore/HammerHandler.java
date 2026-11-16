package dev.yukito.yukicore;

import dev.yukito.yukicore.item.HammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Молоты YukiCore ломают площадь 3×3 перпендикулярно грани, по которой ударили. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class HammerHandler {
    private static boolean breaking = false;

    private HammerHandler() {}

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (breaking || !(event.getPlayer() instanceof ServerPlayer player) || player.isShiftKeyDown()) return;
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof HammerItem)) return;
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange() + 1));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        Direction face = hit.getDirection();
        BlockPos origin = event.getPos();
        breaking = true;
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) continue;
                    BlockPos p = switch (face.getAxis()) {
                        case Y -> origin.offset(a, 0, b);
                        case X -> origin.offset(0, a, b);
                        case Z -> origin.offset(a, b, 0);
                    };
                    BlockState st = player.level().getBlockState(p);
                    if (st.isAir() || st.getDestroySpeed(player.level(), p) < 0) continue;
                    if (!tool.isCorrectToolForDrops(st)) continue;
                    if (tool.isEmpty()) break;
                    player.gameMode.destroyBlock(p);
                }
            }
        } finally {
            breaking = false;
        }
    }
}
