package dev.yukito.yukicore.magic;

/** Школы магии: лёд Юки и пламя Синтары. */
public enum School {
    ICE("yuki", Element.ICE, 0x8FD8FF, 0x2C6CB0),
    FIRE("sintara", Element.FIRE, 0xFFA040, 0xB03A0C);

    public final String id;
    public final Element element;
    public final int color;
    public final int dark;

    School(String id, Element element, int color, int dark) {
        this.id = id;
        this.element = element;
        this.color = color;
        this.dark = dark;
    }
}
