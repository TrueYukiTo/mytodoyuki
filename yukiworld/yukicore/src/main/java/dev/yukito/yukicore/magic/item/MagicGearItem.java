package dev.yukito.yukicore.magic.item;

import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.magic.School;

/** Кольцо стихии: пока лежит в инвентаре, усиливает заклинания своей школы. */
public class MagicGearItem extends CoreItem {
    public final School school;
    public final float power;

    public MagicGearItem(Properties props, School school, float power) {
        super(props, true);
        this.school = school;
        this.power = power;
    }
}
