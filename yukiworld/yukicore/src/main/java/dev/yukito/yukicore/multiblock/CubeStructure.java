package dev.yukito.yukicore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Мультиструктура «куб 3×3×3»: контроллер стоит в центре одной из боковых граней куба,
 * в самом центре куба — ядро, по четырём верхним углам — «венцы», остальное — корпус.
 * Проверяются все четыре стороны, поэтому строить можно в любом направлении.
 */
public record CubeStructure(Block casing, Block core, Block crown) {

    /** @return направление от контроллера к центру куба или null, если структура не собрана. */
    public Direction find(Level level, BlockPos controller) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (matches(level, controller, controller.relative(d))) return d;
        }
        return null;
    }

    private boolean matches(Level level, BlockPos controller, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = center.offset(x, y, z);
                    if (p.equals(controller)) continue;
                    Block expected;
                    if (x == 0 && y == 0 && z == 0) expected = core;
                    else if (y == 1 && x != 0 && z != 0) expected = crown;
                    else expected = casing;
                    if (!level.getBlockState(p).is(expected)) return false;
                }
            }
        }
        return true;
    }
}
