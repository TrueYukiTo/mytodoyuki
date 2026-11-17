"""YukiCore 7.4 «Арсенал стихий»: оружие, снаряжение, инструменты, расходники, скакуны, камни, два 3D-комплекта."""
import colorsys
import math

from PIL import Image, ImageDraw

from . import hd
from .recipes import I, shaped, shapeless

# ------------------------------------------------------------------ палитра
ICE, ICE_L, ICE_D, WHITE = (120, 200, 255), (200, 240, 255), (40, 90, 170), (255, 255, 255)
FIRE, FIRE_L, FIRE_D, GOLD = (240, 90, 20), (255, 200, 80), (130, 30, 10), (255, 205, 60)
WOOD, WOOD_D, IRON, IRON_D, DARK = (140, 90, 50), (90, 55, 30), (200, 205, 215), (110, 115, 130), (40, 30, 45)
PURPLE, PURPLE_L, RED, RED_D, LEATHER = (150, 70, 220), (220, 170, 255), (200, 20, 40), (110, 0, 20), (150, 80, 40)


def _c(rgb, a=255):
    return tuple(rgb) if len(rgb) == 4 else tuple(rgb) + (a,)


def _new():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def _line(d, pts, col, w=1):
    d.line(pts, fill=_c(col), width=w)


def _handle(d, x0, y0, x1, y1, col=WOOD):
    _line(d, [x0, y0, x1, y1], col)


def draw_items():
    """{имя: (16×16 спрайт, в руке как инструмент, искры)}."""
    out = {}

    def add(name, fn, handheld=False, sparkles=0):
        img, d = _new()
        fn(d)
        out[name] = (img, handheld, sparkles)

    # --- оружие
    add("sintara_scythe", lambda d: (_handle(d, 3, 14, 11, 4, WOOD_D),
                                     d.polygon([(10, 2), (15, 4), (14, 7), (11, 5), (7, 4)], fill=_c(FIRE)),
                                     _line(d, [8, 3, 14, 5], FIRE_L)), True, 3)
    add("twin_blades", lambda d: (_line(d, [2, 13, 9, 3], ICE_L, 1), _line(d, [3, 13, 10, 3], ICE),
                                  _line(d, [7, 13, 14, 3], FIRE_L, 1), _line(d, [8, 13, 15, 3], FIRE),
                                  _line(d, [1, 12, 4, 15], WOOD_D), _line(d, [6, 12, 9, 15], WOOD_D)), True, 2)
    add("meteor_hammer", lambda d: (_handle(d, 3, 14, 9, 6, WOOD_D),
                                    d.rectangle([7, 1, 14, 7], fill=_c(DARK), outline=_c(FIRE)),
                                    d.rectangle([9, 3, 12, 5], fill=_c(FIRE_L))), True, 2)
    add("battle_fan", lambda d: (d.pieslice([1, 1, 15, 15], 200, 340, fill=_c(ICE_L), outline=_c(ICE_D)),
                                 [_line(d, [8, 8, 8 + math.cos(math.radians(a)) * 7, 8 + math.sin(math.radians(a)) * 7], ICE_D)
                                  for a in range(210, 340, 25)],
                                 _line(d, [8, 8, 8, 15], WOOD_D)), True, 1)

    def bow(d, pull=0):
        d.arc([2 - pull, 1, 14 - pull, 15], 270, 90, fill=_c(ICE), width=2)
        _line(d, [8 - pull * 2, 1, 8 - pull * 2, 15], WHITE)
        if pull:
            _line(d, [4 - pull, 8, 13, 8], IRON)
            d.polygon([(13, 7), (15, 8), (13, 9)], fill=_c(ICE_L))
    add("yuki_bow", lambda d: bow(d), True, 2)
    for i in range(3):
        add(f"yuki_bow_pulling_{i}", lambda d, i=i: bow(d, i + 1), True, 2)

    # --- снаряжение
    add("cloud_ring", lambda d: (d.ellipse([3, 5, 13, 15], outline=_c(GOLD), width=2),
                                 d.ellipse([4, 0, 12, 6], fill=_c(WHITE))), False, 2)
    add("second_chance_amulet", lambda d: (_line(d, [3, 1, 8, 7], GOLD), _line(d, [13, 1, 8, 7], GOLD),
                                           d.polygon([(8, 6), (13, 10), (8, 15), (3, 10)], fill=_c((80, 220, 120)), outline=_c(GOLD)),
                                           d.point((8, 10), fill=_c(WHITE))), False, 3)
    add("phoenix_cloak", lambda d: (d.polygon([(4, 1), (12, 1), (15, 15), (1, 15)], fill=_c(FIRE), outline=_c(FIRE_D)),
                                    d.polygon([(6, 1), (10, 1), (11, 10), (5, 10)], fill=_c(FIRE_L)),
                                    _line(d, [1, 15, 15, 15], GOLD)), False, 3)
    add("miner_ring", lambda d: (d.ellipse([3, 5, 13, 15], outline=_c(IRON), width=2),
                                 d.polygon([(8, 0), (12, 4), (8, 7), (4, 4)], fill=_c((80, 230, 230)))), False, 2)
    add("dash_boots", lambda d: (d.polygon([(3, 2), (8, 2), (8, 11), (14, 11), (14, 15), (3, 15)], fill=_c(ICE), outline=_c(ICE_D)),
                                 _line(d, [0, 8, 3, 8], WHITE), _line(d, [0, 11, 3, 11], WHITE)), False, 1)
    add("ice_skates", lambda d: (d.polygon([(3, 1), (8, 1), (8, 9), (14, 9), (14, 12), (3, 12)], fill=_c(LEATHER), outline=_c(WOOD_D)),
                                 _line(d, [2, 14, 15, 14], IRON), _line(d, [4, 12, 4, 14], IRON_D), _line(d, [12, 12, 12, 14], IRON_D)), False, 1)

    # --- инструменты
    add("yuki_drill", lambda d: (d.rectangle([1, 6, 7, 11], fill=_c(DARK), outline=_c(ICE)),
                                 d.polygon([(7, 5), (15, 8), (15, 9), (7, 12)], fill=_c(ICE_L), outline=_c(ICE_D)),
                                 [_line(d, [9 + k * 2, 6, 10 + k * 2, 11], ICE_D) for k in range(3)],
                                 _line(d, [3, 11, 3, 15], WOOD_D)), True, 2)
    add("builder_wand", lambda d: (_handle(d, 2, 14, 11, 5, WOOD),
                                   d.rectangle([10, 1, 14, 5], fill=_c((90, 200, 110)), outline=_c((30, 90, 40)))), True, 2)
    add("teleport_orb", lambda d: (d.ellipse([2, 2, 14, 14], fill=_c(PURPLE), outline=_c((60, 20, 100))),
                                   d.ellipse([5, 4, 9, 8], fill=_c(PURPLE_L)), d.point((10, 10), fill=_c(WHITE))), False, 3)
    add("sintara_backpack", lambda d: (d.rounded_rectangle([2, 3, 14, 15], 2, fill=_c(FIRE_D), outline=_c(DARK)),
                                       d.rectangle([4, 7, 12, 12], fill=_c(FIRE)), d.rectangle([7, 8, 9, 9], fill=_c(GOLD)),
                                       d.arc([5, 0, 11, 6], 180, 360, fill=_c(LEATHER), width=1)), False, 1)

    # --- расходники
    def bomb(d, body, spark):
        d.ellipse([3, 4, 13, 14], fill=_c(body), outline=_c(DARK))
        d.rectangle([7, 2, 9, 4], fill=_c(IRON_D))
        _line(d, [9, 2, 11, 0], spark)
        d.point((5, 7), fill=_c(WHITE))
    add("frost_bomb", lambda d: bomb(d, ICE, ICE_L), False, 1)
    add("fire_bomb", lambda d: bomb(d, FIRE, FIRE_L), False, 1)
    add("snare_bomb", lambda d: (bomb(d, (110, 160, 60), WHITE), _line(d, [4, 9, 12, 9], WHITE), _line(d, [8, 5, 8, 13], WHITE)), False, 1)

    def potion(d, col):
        d.rectangle([6, 1, 9, 4], fill=_c(IRON))
        d.ellipse([3, 4, 13, 15], fill=_c((230, 240, 255, 180)), outline=_c(WHITE))
        d.ellipse([4, 7, 12, 14], fill=_c(col))
        d.point((6, 9), fill=_c(WHITE))
    add("ice_skin_potion", lambda d: potion(d, ICE), False, 1)
    add("fire_blood_potion", lambda d: potion(d, RED), False, 1)
    add("luck_potion", lambda d: potion(d, GOLD), False, 2)

    def scroll(d, seal):
        d.rectangle([3, 3, 13, 13], fill=_c((240, 225, 180)), outline=_c((150, 110, 60)))
        d.rectangle([2, 2, 14, 4], fill=_c((200, 170, 110)))
        d.rectangle([2, 12, 14, 14], fill=_c((200, 170, 110)))
        d.ellipse([6, 6, 10, 10], fill=_c(seal))
    add("recall_scroll", lambda d: scroll(d, PURPLE), False, 1)
    add("golem_scroll", lambda d: scroll(d, ICE), False, 1)
    add("fire_rain_scroll", lambda d: scroll(d, FIRE), False, 1)

    # --- скакуны
    def whistle(d, col, lt):
        d.rounded_rectangle([1, 6, 12, 11], 2, fill=_c(col), outline=_c(DARK))
        d.ellipse([9, 4, 15, 12], fill=_c(lt), outline=_c(DARK))
        d.point((4, 8), fill=_c(DARK))
    add("frost_steed_whistle", lambda d: whistle(d, ICE, ICE_L), False, 2)
    add("flame_steed_whistle", lambda d: whistle(d, FIRE, FIRE_L), False, 2)

    # --- камни
    def gem(d, col, lt):
        d.polygon([(8, 1), (14, 6), (8, 15), (2, 6)], fill=_c(col), outline=_c(DARK))
        d.polygon([(8, 1), (11, 6), (8, 15)], fill=_c(lt))
        _line(d, [2, 6, 14, 6], lt)
    add("ember_gem", lambda d: gem(d, FIRE, FIRE_L), False, 2)
    add("frost_gem", lambda d: gem(d, ICE, ICE_L), False, 2)
    add("blood_gem", lambda d: gem(d, RED_D, RED), False, 2)
    add("storm_gem", lambda d: gem(d, (220, 190, 30), (255, 250, 150)), False, 2)

    # --- броня (иконки)
    def helm(d, a, b):
        d.rectangle([3, 3, 12, 11], fill=_c(a), outline=_c(b))
        d.rectangle([5, 7, 10, 9], fill=_c(DARK))

    def chest(d, a, b):
        d.polygon([(2, 2), (6, 2), (8, 4), (10, 2), (14, 2), (14, 7), (12, 7), (12, 15), (4, 15), (4, 7), (2, 7)], fill=_c(a), outline=_c(b))

    def legs(d, a, b):
        d.polygon([(3, 1), (13, 1), (13, 15), (9, 15), (8, 6), (7, 15), (3, 15)], fill=_c(a), outline=_c(b))

    def boots(d, a, b):
        d.rectangle([2, 7, 6, 14], fill=_c(a), outline=_c(b))
        d.rectangle([9, 7, 13, 14], fill=_c(a), outline=_c(b))
    for set_, a, b, crown in (("frost_queen", ICE_L, ICE_D, ICE), ("fire_demon", FIRE_D, FIRE, FIRE_L)):
        add(f"{set_}_helmet", lambda d, a=a, b=b, c=crown: (helm(d, a, b), [d.polygon([(3 + k * 3, 3), (4 + k * 3, 0), (5 + k * 3, 3)],
                                                                                    fill=_c(c)) for k in range(4)]), False, 2)
        add(f"{set_}_chestplate", lambda d, a=a, b=b, c=crown: (chest(d, a, b), d.rectangle([7, 6, 9, 9], fill=_c(c))), False, 2)
        add(f"{set_}_leggings", lambda d, a=a, b=b: legs(d, a, b), False, 2)
        add(f"{set_}_boots", lambda d, a=a, b=b: boots(d, a, b), False, 2)
    return out


def item_textures():
    return {n: (hd.hd_item(img, seed=900 + i, sparkles=sp), hh) for i, (n, (img, hh, sp)) in enumerate(draw_items().items())}


def bow_models():
    """Модели лука с анимацией натяжения."""
    m = {"yuki_bow": {"parent": "minecraft:item/bow", "textures": {"layer0": "yukicore:item/yuki_bow"},
                      "overrides": [{"predicate": {"pulling": 1}, "model": "yukicore:item/yuki_bow_pulling_0"},
                                    {"predicate": {"pulling": 1, "pull": 0.65}, "model": "yukicore:item/yuki_bow_pulling_1"},
                                    {"predicate": {"pulling": 1, "pull": 0.9}, "model": "yukicore:item/yuki_bow_pulling_2"}]}}
    for i in range(3):
        m[f"yuki_bow_pulling_{i}"] = {"parent": "yukicore:item/yuki_bow", "textures": {"layer0": f"yukicore:item/yuki_bow_pulling_{i}"}}
    return m


# ------------------------------------------------------------------ наковальня стихий
def anvil_textures():
    S = 64
    top = Image.new("RGBA", (S, S), (60, 55, 70, 255))
    d = ImageDraw.Draw(top)
    d.rectangle([0, 0, S - 1, S - 1], outline=(30, 25, 35, 255), width=3)
    d.rectangle([4, 4, 29, S - 5], fill=(90, 160, 220, 255))
    d.rectangle([34, 4, S - 5, S - 5], fill=(220, 90, 40, 255))
    d.ellipse([22, 22, 41, 41], fill=(240, 220, 120, 255), outline=(255, 255, 255, 255))
    side = Image.new("RGBA", (S, S), (70, 65, 80, 255))
    d = ImageDraw.Draw(side)
    for y in range(0, S, 8):
        d.line([0, y, S, y], fill=(55, 50, 62, 255), width=1)
    d.rectangle([0, 0, S - 1, 10], fill=(110, 170, 220, 255))
    d.rectangle([0, 0, 31, 10], fill=(220, 110, 60, 255))
    return {"elemental_anvil_top": top, "elemental_anvil_side": side}


def anvil_model():
    B = "yukicore:block/"
    f = lambda t: {d: {"texture": t} for d in ("north", "south", "east", "west", "down")}
    return {"parent": "minecraft:block/block", "textures": {"top": B + "elemental_anvil_top", "side": B + "elemental_anvil_side",
                                                             "particle": B + "elemental_anvil_side"},
            "elements": [
                {"from": [2, 0, 2], "to": [14, 4, 14], "faces": {**f("#side"), "up": {"texture": "#side"}}},
                {"from": [4, 4, 5], "to": [12, 9, 11], "faces": {**f("#side"), "up": {"texture": "#side"}}},
                {"from": [1, 9, 3], "to": [15, 13, 13], "faces": {**f("#side"), "up": {"texture": "#top"}}}]}


# ------------------------------------------------------------------ 3D-броня: перекраска существующих текстур
def _recolor(img, hue_shift=None, sat=1.0, val=1.0, target_hue=None, min_sat=0.0):
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if target_hue is not None:
                h = target_hue + (h - 0.55) * 0.15
            elif hue_shift is not None:
                h = (h + hue_shift) % 1
            s, v = min(1, max(min_sat, s * sat)), min(1, v * val)
            r2, g2, b2 = colorsys.hsv_to_rgb(h % 1, s, v)
            px[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    return img


def armor3d_textures(armor_dir):
    """Ледяная королева — регалии Юки в бело-голубом; Огненный демон — Вечная зима в красно-чёрном."""
    out = {}
    yr = Image.open(armor_dir / "yuki_regalia.png")
    out["frost_queen"] = _recolor(yr, target_hue=0.53, sat=0.7, val=1.15)
    out["frost_queen_glow"] = _recolor(Image.open(armor_dir / "yuki_regalia_glow.png"), target_hue=0.5, sat=0.5, val=1.2)
    et = Image.open(armor_dir / "eternal.png")
    out["fire_demon"] = _recolor(et, target_hue=0.005, sat=1.3, val=0.62, min_sat=0.8)
    out["fire_demon_glow"] = _recolor(Image.open(armor_dir / "eternal_glow.png"), target_hue=0.07, sat=1.4, val=1.1, min_sat=0.85)
    return out


def effect_icons(item_tex):
    small = lambda n: item_tex[n][0].resize((18, 18), Image.LANCZOS)
    return {"ice_skin": small("ice_skin_potion"), "fire_blood": small("fire_blood_potion"), "sintara_luck": small("luck_potion")}


# ------------------------------------------------------------------ рецепты
def recipes():
    A, FE, FL, GFE, GFL = I("aurorium_ingot"), I("frost_essence"), I("flame_essence"), I("greater_frost_essence"), I("greater_flame_essence")
    PY, FI, YC = I("pyrolite_ingot"), I("frost_ingot"), I("yuki_crystal")
    r = {
        "sintara_scythe": shaped(["PPF", " B ", "B  "], {"P": PY, "F": FL, "B": "minecraft:blaze_rod"}, I("sintara_scythe")),
        "twin_blades": shapeless([A, A, FE, FL, "minecraft:stick", "minecraft:stick"], I("twin_blades")),
        "meteor_hammer": shaped(["PMP", " B ", " B "], {"P": PY, "M": "minecraft:magma_block", "B": "minecraft:blaze_rod"}, I("meteor_hammer")),
        "battle_fan": shaped(["FFF", "IFI", " S "], {"F": "minecraft:feather", "I": FI, "S": "minecraft:stick"}, I("battle_fan")),
        "yuki_bow": shaped([" IS", "C S", " IS"], {"I": FI, "C": YC, "S": "minecraft:string"}, I("yuki_bow")),
        "cloud_ring": shaped([" P ", "PGP", " F "], {"P": "minecraft:phantom_membrane", "G": "minecraft:gold_ingot",
                                                    "F": "minecraft:feather"}, I("cloud_ring")),
        "second_chance_amulet": shaped([" S ", "GTG", " D "], {"S": "minecraft:string", "G": "minecraft:gold_ingot",
                                                             "T": "minecraft:totem_of_undying", "D": "minecraft:diamond"}, I("second_chance_amulet")),
        "phoenix_cloak": shaped(["P P", "LPL", "LLL"], {"P": I("phoenix_feather"), "L": "minecraft:leather"}, I("phoenix_cloak")),
        "miner_ring": shaped(["RDR", "G G", " G "], {"R": "minecraft:redstone", "D": "minecraft:diamond", "G": "minecraft:gold_ingot"},
                             I("miner_ring")),
        "dash_boots": shaped(["F F", "IBI"], {"F": "minecraft:feather", "I": FI, "B": "minecraft:leather_boots"}, I("dash_boots")),
        "ice_skates": shaped(["IBI", "S S"], {"I": FI, "B": "minecraft:leather_boots", "S": "minecraft:iron_ingot"}, I("ice_skates")),
        "yuki_drill": shaped(["AAA", "APA", " S "], {"A": A, "P": "minecraft:diamond_pickaxe", "S": "minecraft:stick"}, I("yuki_drill")),
        "builder_wand": shaped(["  E", " S ", "S  "], {"E": "minecraft:emerald_block", "S": "minecraft:stick"}, I("builder_wand")),
        "teleport_orb": shaped([" E ", "EVE", " E "], {"E": "minecraft:ender_pearl", "V": I("void_shard")}, I("teleport_orb")),
        "sintara_backpack": shaped(["LLL", "LCL", "LPL"], {"L": "minecraft:leather", "C": "minecraft:chest", "P": PY}, I("sintara_backpack")),
        "frost_bomb": shapeless(["minecraft:gunpowder", FE, "minecraft:snowball", "minecraft:snowball"], I("frost_bomb"), 4),
        "fire_bomb": shapeless(["minecraft:gunpowder", FL, "minecraft:fire_charge"], I("fire_bomb"), 4),
        "snare_bomb": shapeless(["minecraft:gunpowder", "minecraft:string", "minecraft:string", "minecraft:slime_ball"], I("snare_bomb"), 4),
        "ice_skin_potion": shapeless(["minecraft:glass_bottle", FE, YC, "minecraft:packed_ice"], I("ice_skin_potion")),
        "fire_blood_potion": shapeless(["minecraft:glass_bottle", FL, "minecraft:blaze_powder", "minecraft:magma_cream"], I("fire_blood_potion")),
        "luck_potion": shapeless(["minecraft:glass_bottle", I("sintara_chip"), I("sintara_chip"), I("sintara_chip"),
                                  "minecraft:rabbit_foot"], I("luck_potion")),
        "recall_scroll": shapeless(["minecraft:paper", "minecraft:ender_pearl", "minecraft:compass"], I("recall_scroll"), 2),
        "golem_scroll": shapeless(["minecraft:paper", "minecraft:snow_block", "minecraft:snow_block", "minecraft:carved_pumpkin", FE],
                                  I("golem_scroll")),
        "fire_rain_scroll": shapeless(["minecraft:paper", FL, FL, "minecraft:fire_charge", "minecraft:fire_charge"], I("fire_rain_scroll")),
        "frost_steed_whistle": shapeless(["minecraft:gold_ingot", "minecraft:saddle", FE, YC, A], I("frost_steed_whistle")),
        "flame_steed_whistle": shapeless(["minecraft:gold_ingot", "minecraft:saddle", FL, PY, A], I("flame_steed_whistle")),
        "elemental_anvil": shaped(["FAF", " N ", "III"], {"F": FE, "A": FL, "N": "minecraft:anvil", "I": "minecraft:iron_block"},
                                  I("elemental_anvil")),
        "ember_gem": shaped([" F ", "FDF", " F "], {"F": FL, "D": "minecraft:diamond"}, I("ember_gem")),
        "frost_gem": shaped([" F ", "FDF", " F "], {"F": FE, "D": "minecraft:diamond"}, I("frost_gem")),
        "blood_gem": shaped([" R ", "RDR", " R "], {"R": "minecraft:redstone_block", "D": "minecraft:diamond"}, I("blood_gem")),
        "storm_gem": shaped([" G ", "GDG", " G "], {"G": "minecraft:glowstone", "D": "minecraft:diamond"}, I("storm_gem")),
    }
    for set_, x in (("frost_queen", GFE), ("fire_demon", GFL)):
        k = {"A": A, "X": x}
        r[f"{set_}_helmet"] = shaped(["AAA", "AXA"], k, I(f"{set_}_helmet"))
        r[f"{set_}_chestplate"] = shaped(["AXA", "AAA", "AAA"], k, I(f"{set_}_chestplate"))
        r[f"{set_}_leggings"] = shaped(["AAA", "AXA", "A A"], k, I(f"{set_}_leggings"))
        r[f"{set_}_boots"] = shaped(["AXA", "A A"], k, I(f"{set_}_boots"))
    return r


# Камни и свитки падают с боссов
BOSS_EXTRA = {
    "frost_colossus": [("frost_gem", 1, 2), ("golem_scroll", 1, 2)],
    "blizzard_queen": [("frost_gem", 1, 2), ("storm_gem", 0, 1)],
    "ash_lord": [("ember_gem", 1, 2), ("fire_rain_scroll", 1, 2)],
    "infernal_phoenix": [("ember_gem", 1, 2), ("blood_gem", 0, 1)],
    "void_wyrm": [("blood_gem", 1, 2), ("storm_gem", 1, 1)],
    "shadow_of_yuki": [("storm_gem", 1, 2), ("blood_gem", 1, 1)],
    "glacial_archon": [("frost_gem", 1, 2), ("storm_gem", 1, 1)],
}


# ------------------------------------------------------------------ тексты
ITEMS = {
    # имя: (ru, en, ru-описание (золотом), en, ru-подробно, en)
    "sintara_scythe": ("Коса Синтары", "Sintara's Scythe", "Поджигает и бьёт всех вокруг цели", "Burns and sweeps around the target",
                       "Каждый 5-й удар — огненная волна радиусом 7", "Every 5th hit — a fire wave of radius 7"),
    "twin_blades": ("Клинки «Лёд и Пламя»", "Twin Blades of Ice and Flame", "Удары чередуют лёд и огонь",
                    "Hits alternate ice and fire", "Каждый 3-й удар — взрыв пара: урон по области и ослепление",
                    "Every 3rd hit — a steam blast: area damage and blindness"),
    "meteor_hammer": ("Молот-метеор", "Meteor Hammer", "Удар в падении обрушивает метеор", "A falling hit brings down a meteor",
                      "Shift+ПКМ — прыжок-рывок с ударом о землю (3 с перезарядка)", "Shift+Use — leap and slam (3 s cooldown)"),
    "battle_fan": ("Боевой веер", "Battle Fan", "ПКМ — порыв ветра отбрасывает врагов", "Use — a gust blows enemies away",
                   "В руке: плавное парение вместо падения", "In hand: glide instead of falling"),
    "yuki_bow": ("Ледяной лук Юки", "Yuki's Frost Bow", "Стрелы замораживают цель", "Arrows freeze the target",
                 "Попадание раскалывает стрелу на 6 ледяных осколков", "On hit the arrow shatters into 6 ice shards"),
    "cloud_ring": ("Кольцо облаков", "Cloud Ring", "В инвентаре: двойной прыжок", "In inventory: double jump",
                   "Прыгни ещё раз в воздухе", "Jump again in mid-air"),
    "second_chance_amulet": ("Амулет второго шанса", "Amulet of Second Chance", "Спасает от смерти: 50% здоровья и защита",
                             "Saves you from death: 50% health and protection", "Перезарядка — 10 минут. Достаточно носить в инвентаре",
                             "10 minute cooldown. Just keep it in your inventory"),
    "phoenix_cloak": ("Плащ Феникса", "Phoenix Cloak", "В инвентаре: планирование при падении", "In inventory: glide while falling",
                      "Огненный след, падение почти без урона. Shift — падать быстро", "Fiery trail, little fall damage. Sneak to drop fast"),
    "miner_ring": ("Кольцо шахтёра", "Miner's Ring", "В инвентаре: ночное зрение и спешка II", "In inventory: night vision and Haste II",
                   "Для шахт и пещер", "For mines and caves"),
    "dash_boots": ("Ботинки рывка", "Dash Boots", "Двойное нажатие направления — рывок", "Double-tap a direction to dash",
                   "W, A, S или D дважды. Перезарядка 1 с, падение мягче", "W, A, S or D twice. 1 s cooldown, softer falls"),
    "ice_skates": ("Ледяные коньки", "Ice Skates", "Вода под ногами замерзает", "Water freezes under your feet",
                   "На льду с бегом — разгон", "Sprinting on ice speeds you up"),
    "yuki_drill": ("Бур Юки", "Yuki's Drill", "Копает 3×3 и сразу переплавляет руду", "Mines 3×3 and smelts ore on the spot",
                   "Кирка и лопата в одном. Shift — один блок без переплавки", "Pickaxe and shovel in one. Sneak — single block, no smelting"),
    "builder_wand": ("Посох строителя", "Builder's Wand", "ПКМ по блоку — достраивает слой 5×5", "Use on a block — extends a 5×5 layer",
                     "Берёт блоки из инвентаря. Shift — только один блок", "Takes blocks from your inventory. Sneak — one block"),
    "teleport_orb": ("Сфера телепорта", "Teleport Orb", "Shift+ПКМ — запомнить точку, ПКМ — вернуться",
                     "Sneak+Use — save a point, Use — return", "ПКМ по другу — привязка к нему: телепорт к другу. Перезарядка 30 с",
                     "Use on a friend to bind — teleport to them. 30 s cooldown"),
    "sintara_backpack": ("Рюкзак Синтары", "Sintara's Backpack", "54 слота. Shift+ПКМ — переплавить руду внутри",
                         "54 slots. Sneak+Use — smelt ores inside", "Уголь в рюкзаке — топливо. Подобранные знакомые предметы уходят в рюкзак",
                         "Coal inside is fuel. Picked-up items it already holds go into it"),
    "frost_bomb": ("Ледяная бомба", "Frost Bomb", "Бросок: 14 урона по области и заморозка", "Throw: 14 area damage and freeze", "", ""),
    "fire_bomb": ("Огненная бомба", "Fire Bomb", "Бросок: 18 урона по области и поджог", "Throw: 18 area damage and burning", "", ""),
    "snare_bomb": ("Бомба-ловушка", "Snare Bomb", "Бросок: враги вокруг почти не могут двигаться 9 с",
                   "Throw: enemies around barely move for 9 s", "", ""),
    "ice_skin_potion": ("Зелье «Ледяная кожа»", "Ice Skin Potion", "4 мин: +8 брони, иммунитет к замедлению и холоду",
                        "4 min: +8 armor, immune to slowness and cold", "", ""),
    "fire_blood_potion": ("Зелье «Огненная кровь»", "Fire Blood Potion", "4 мин: тот, кто тебя бьёт, загорается",
                          "4 min: whoever hits you catches fire", "", ""),
    "luck_potion": ("Зелье «Удача Синтары»", "Sintara's Luck Potion", "4 мин: +3 удачи, мобы роняют фишки (30%)",
                    "4 min: +3 luck, mobs drop chips (30%)", "", ""),
    "recall_scroll": ("Свиток возврата", "Recall Scroll", "Телепорт к кровати или точке возрождения", "Teleport to your bed or spawn", "", ""),
    "golem_scroll": ("Свиток снежных стражей", "Scroll of Snow Guards", "Призывает 3 больших снежных голема на 90 с",
                     "Summons 3 big snow golems for 90 s", "", ""),
    "fire_rain_scroll": ("Свиток огненного дождя", "Scroll of Fire Rain", "30 огненных глыб падают в точку взгляда",
                         "30 fire rocks fall where you look", "", ""),
    "frost_steed_whistle": ("Свисток ледяного скакуна", "Frost Steed Whistle", "Призвать / отпустить ледяного скакуна",
                            "Summon / dismiss the frost steed", "Летает: смотри вверх — набор высоты. Замораживает воду. Растёт до 10 ур.",
                            "Flies: look up to climb. Freezes water. Levels up to 10"),
    "flame_steed_whistle": ("Свисток огненного скакуна", "Flame Steed Whistle", "Призвать / отпустить огненного скакуна",
                            "Summon / dismiss the flame steed", "Летает: смотри вверх — набор высоты. Огнестойкий. Растёт до 10 ур.",
                            "Flies: look up to climb. Fireproof. Levels up to 10"),
    "ember_gem": ("Камень жара", "Ember Gem", "", "", "", ""),
    "frost_gem": ("Камень стужи", "Frost Gem", "", "", "", ""),
    "blood_gem": ("Кровавый камень", "Blood Gem", "", "", "", ""),
    "storm_gem": ("Камень бури", "Storm Gem", "", "", "", ""),
    "frost_queen_helmet": ("Корона Ледяной королевы", "Frost Queen's Crown", "", "", "", ""),
    "frost_queen_chestplate": ("Мантия Ледяной королевы", "Frost Queen's Mantle", "", "", "", ""),
    "frost_queen_leggings": ("Поножи Ледяной королевы", "Frost Queen's Greaves", "", "", "", ""),
    "frost_queen_boots": ("Сапоги Ледяной королевы", "Frost Queen's Boots", "", "", "", ""),
    "fire_demon_helmet": ("Рогатый шлем Огненного демона", "Fire Demon's Horned Helm", "", "", "", ""),
    "fire_demon_chestplate": ("Панцирь Огненного демона", "Fire Demon's Carapace", "", "", "", ""),
    "fire_demon_leggings": ("Поножи Огненного демона", "Fire Demon's Greaves", "", "", "", ""),
    "fire_demon_boots": ("Копыта Огненного демона", "Fire Demon's Hooves", "", "", "", ""),
}

LANG = {
    "block.yukicore.elemental_anvil": ("Наковальня стихий", "Elemental Anvil"),
    "block.yukicore.elemental_anvil.desc": ("Оружие в правой руке, камень в левой — ПКМ: вставить камень (до 3)",
                                            "Weapon in main hand, gem in off hand — Use to socket (up to 3)"),
    "effect.yukicore.ice_skin": ("Ледяная кожа", "Ice Skin"),
    "effect.yukicore.fire_blood": ("Огненная кровь", "Fire Blood"),
    "effect.yukicore.sintara_luck": ("Удача Синтары", "Sintara's Luck"),
    "tooltip.yukicore.set.frost_queen": ("Комплект: аура мороза (замедляет и ранит врагов в радиусе 6), скорость, атакующие мёрзнут",
                                         "Set: frost aura (slows and hurts enemies within 6), speed, attackers freeze"),
    "tooltip.yukicore.set.fire_demon": ("Комплект: огненная аура, сила II, огнестойкость, атакующие горят",
                                        "Set: fire aura, Strength II, fire resistance, attackers burn"),
    "gear.yukicore.gems": ("Камни: %s/%s", "Gems: %s/%s"),
    "gear.yukicore.gem.ember": ("Жар: поджог, +12% урона", "Ember: burning, +12% damage"),
    "gear.yukicore.gem.frost": ("Стужа: замедление, шанс заморозки 12%", "Frost: slow, 12% freeze chance"),
    "gear.yukicore.gem.blood": ("Кровь: лечит 6% нанесённого урона", "Blood: heals 6% of damage dealt"),
    "gear.yukicore.gem.storm": ("Буря: шанс 12% на удар молнии ×1,8", "Storm: 12% chance of a ×1.8 lightning hit"),
    "gear.yukicore.gem.how": ("Вставляется в оружие или инструмент на Наковальне стихий. Эффекты складываются",
                              "Socket into a weapon or tool at the Elemental Anvil. Effects stack"),
    "gear.yukicore.anvil.how": ("Оружие — в правую руку, камень стихий — в левую, затем ПКМ по наковальне",
                                "Weapon in main hand, elemental gem in off hand, then use the anvil"),
    "gear.yukicore.anvil.full": ("В этом предмете уже 3 камня", "This item already has 3 gems"),
    "gear.yukicore.anvil.done": ("Камень вставлен: %s", "Gem socketed: %s"),
    "gear.yukicore.orb.saved": ("Точка запомнена", "Point saved"),
    "gear.yukicore.orb.friend": ("Сфера привязана к %s", "Orb bound to %s"),
    "gear.yukicore.orb.offline": ("Друг не в сети", "Friend is offline"),
    "gear.yukicore.orb.empty": ("Сначала Shift+ПКМ — запомнить точку, или ПКМ по другу", "First Sneak+Use to save a point, or use on a friend"),
    "gear.yukicore.orb.to_friend": ("Телепорт к: %s", "Teleports to: %s"),
    "gear.yukicore.orb.to_point": ("Точка: %s %s %s (%s)", "Point: %s %s %s (%s)"),
    "gear.yukicore.backpack.no_fuel": ("Положи в рюкзак уголь — это топливо для переплавки", "Put coal in the backpack — it's the fuel"),
    "gear.yukicore.backpack.smelted": ("Переплавлено: %s", "Smelted: %s"),
    "gear.yukicore.backpack.used": ("Занято слотов: %s/%s", "Slots used: %s/%s"),
    "gear.yukicore.golem_name": ("Снежный страж", "Snow Guard"),
    "gear.yukicore.recall": ("Возврат домой", "Recalled home"),
    "gear.yukicore.steed.ice": ("Ледяной скакун • ур. %s", "Frost Steed • lv %s"),
    "gear.yukicore.steed.fire": ("Огненный скакун • ур. %s", "Flame Steed • lv %s"),
    "gear.yukicore.steed.dismiss": ("Скакун отпущен", "Steed dismissed"),
    "gear.yukicore.steed.levelup": ("Скакун вырос: уровень %s!", "Your steed grew: level %s!"),
    "gear.yukicore.second_chance": ("✦ Амулет второго шанса спас тебя! ✦", "✦ The Amulet of Second Chance saved you! ✦"),
}


def lang():
    ru, en = {}, {}
    for n, (rn, enn, rd, ed, rd2, ed2) in ITEMS.items():
        ru[f"item.yukicore.{n}"], en[f"item.yukicore.{n}"] = rn, enn
        if rd:
            ru[f"item.yukicore.{n}.desc"], en[f"item.yukicore.{n}.desc"] = rd, ed
        if rd2:
            ru[f"item.yukicore.{n}.desc2"], en[f"item.yukicore.{n}.desc2"] = rd2, ed2
    for i in range(3):
        ru[f"item.yukicore.yuki_bow_pulling_{i}"] = en[f"item.yukicore.yuki_bow_pulling_{i}"] = "-"
    for k, (r, e) in LANG.items():
        ru[k], en[k] = r, e
    return ru, en


# ------------------------------------------------------------------ книга
ENTRIES = [
    ("sintara", "arsenal", "Арсенал стихий", "yukicore:sintara_scythe", [
        "$(l)7.4:$() новое оружие, снаряжение и инструменты. Всё крафтится в верстаке, а камни стихий вставляются на "
        "$(l)Наковальне стихий$().",
        ("spot", "yukicore:sintara_scythe", "$(l)Коса Синтары$() поджигает всех вокруг цели, каждый 5-й удар — огненная волна."),
        ("spot", "yukicore:twin_blades", "$(l)Клинки «Лёд и Пламя»$(): удары чередуют стихии, каждый 3-й — взрыв пара."),
        ("spot", "yukicore:meteor_hammer", "$(l)Молот-метеор$(): удар в падении — метеор. Shift+ПКМ — прыжок с ударом о землю."),
        ("spot", "yukicore:yuki_bow", "$(l)Ледяной лук$(): стрелы замораживают и разлетаются на осколки."),
        ("spot", "yukicore:battle_fan", "$(l)Боевой веер$(): порыв ветра, а в руке — парение."),
    ]),
    ("sintara", "gear74", "Снаряжение и инструменты", "yukicore:cloud_ring", [
        "Кольца, амулет и плащ работают, просто $(l)лёжа в инвентаре$(). Ботинки и коньки надеваются.",
        ("spot", "yukicore:cloud_ring", "$(l)Кольцо облаков$() — двойной прыжок."),
        ("spot", "yukicore:second_chance_amulet", "$(l)Амулет второго шанса$() спасает от смерти раз в 10 минут."),
        ("spot", "yukicore:dash_boots", "$(l)Ботинки рывка$(): дважды нажми W, A, S или D."),
        ("spot", "yukicore:yuki_drill", "$(l)Бур Юки$(): 3×3 и переплавка руды. Shift — один блок."),
        ("spot", "yukicore:sintara_backpack", "$(l)Рюкзак Синтары$(): 54 слота, Shift+ПКМ переплавляет руду внутри на угле."),
        ("spot", "yukicore:teleport_orb", "$(l)Сфера телепорта$(): своя точка или телепорт к другу."),
    ]),
    ("sintara", "steeds", "Скакуны и камни стихий", "yukicore:frost_steed_whistle", [
        "$(l)Скакуны$(): свисток призывает ледяного или огненного коня. Смотри вверх — он взлетает, в воздухе — планирует. "
        "С каждым пройденным километром растёт уровень (до 10): быстрее, выносливее, выше прыжок.",
        ("spot", "yukicore:elemental_anvil", "$(l)Наковальня стихий$(): оружие в правой руке, камень в левой — ПКМ. До 3 камней."),
        "$(li)$(#ff8a2a)Жар$() — поджог и +12% урона$(li)$(#8fd8ff)Стужа$() — замедление и заморозка$(li)$(#c02030)Кровь$() — "
        "вампиризм$(li)$(#ffe070)Буря$() — шанс удара ×1,8. Камни падают с боссов.",
        "$(l)Комплекты$(): $(#8fd8ff)Ледяная королева$() — аура мороза; $(#ff8a2a)Огненный демон$() — огненная аура и сила.",
    ]),
]
