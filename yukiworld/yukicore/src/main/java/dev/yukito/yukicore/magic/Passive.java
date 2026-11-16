package dev.yukito.yukicore.magic;

/** Пассивные таланты: по два на школу, до 5 рангов. */
public enum Passive {
    ICE_HEART(School.ICE),      // +20 к максимуму маны за ранг
    PERMAFROST(School.ICE),     // +8% к силе льда за ранг
    FIRE_WILL(School.FIRE),     // +0,6 маны/сек за ранг
    SINTARA_HEAT(School.FIRE);  // +8% к силе огня за ранг

    public static final int MAX_RANK = 5;
    public final School school;

    Passive(School school) {
        this.school = school;
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
