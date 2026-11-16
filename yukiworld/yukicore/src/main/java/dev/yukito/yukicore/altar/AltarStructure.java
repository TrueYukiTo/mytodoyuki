package dev.yukito.yukicore.altar;

import dev.yukito.yukicore.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Мультиструктура «Алтарь Юки» — три уровня.
 *
 * <pre>
 * Уровень I  «Морозный алтарь»:   алтарь + 4 пьедестала (по сторонам, r=3)
 *                                  + 4 колонны по диагонали (±2,±2): морозный блок ×2 + лампа Юки
 * Уровень II «Звёздный алтарь»:    + 4 пьедестала по диагонали (±3,±3); колонны: целестий ×2 + звёздный свет
 * Уровень III «Алтарь Бесконечности»: уровень II + 4 внешних обелиска (±6) по сторонам:
 *                                  целестий ×3 + лампа Юки
 * </pre>
 */
public final class AltarStructure {
    public static final int[][] CARDINAL = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}};
    public static final int[][] DIAGONAL_PEDESTALS = {{3, 3}, {-3, 3}, {3, -3}, {-3, -3}};
    public static final int[][] PILLARS = {{2, 2}, {-2, 2}, {2, -2}, {-2, -2}};
    public static final int[][] OBELISKS = {{6, 0}, {-6, 0}, {0, 6}, {0, -6}};

    private AltarStructure() {}

    /** @return уровень структуры (0 — структура не собрана). */
    public static int detectTier(Level level, BlockPos center) {
        if (!all(level, center, CARDINAL, 0, ModBlocks.YUKI_PEDESTAL.get())) return 0;

        boolean frostPillars = pillars(level, center, ModBlocks.FROST_BLOCK.get(), ModBlocks.YUKI_LAMP.get(), 2);
        boolean starPillars = pillars(level, center, ModBlocks.CELESTIUM_BLOCK.get(), ModBlocks.STARLIGHT_BLOCK.get(), 2);
        boolean diag = all(level, center, DIAGONAL_PEDESTALS, 0, ModBlocks.YUKI_PEDESTAL.get());

        if (starPillars && diag) {
            boolean obelisks = true;
            for (int[] o : OBELISKS) {
                for (int y = 0; y < 3; y++) {
                    obelisks &= level.getBlockState(center.offset(o[0], y, o[1])).is(ModBlocks.CELESTIUM_BLOCK.get());
                }
                obelisks &= level.getBlockState(center.offset(o[0], 3, o[1])).is(ModBlocks.YUKI_LAMP.get());
            }
            return obelisks ? 3 : 2;
        }
        return frostPillars ? 1 : 0;
    }

    /** Пьедесталы, которые участвуют в ритуале на данном уровне. */
    public static List<BlockPos> pedestals(BlockPos center, int tier) {
        List<BlockPos> out = new ArrayList<>();
        for (int[] c : CARDINAL) out.add(center.offset(c[0], 0, c[1]));
        if (tier >= 2) for (int[] c : DIAGONAL_PEDESTALS) out.add(center.offset(c[0], 0, c[1]));
        return out;
    }

    private static boolean pillars(Level level, BlockPos center, Block body, Block top, int height) {
        for (int[] p : PILLARS) {
            for (int y = 0; y < height; y++) {
                if (!level.getBlockState(center.offset(p[0], y, p[1])).is(body)) return false;
            }
            if (!level.getBlockState(center.offset(p[0], height, p[1])).is(top)) return false;
        }
        return true;
    }

    private static boolean all(Level level, BlockPos center, int[][] offsets, int y, Block block) {
        for (int[] o : offsets) {
            if (!level.getBlockState(center.offset(o[0], y, o[1])).is(block)) return false;
        }
        return true;
    }
}
