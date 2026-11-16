"""YukiCore 6.0 «Две стихии»: магия Юки и Синтары, испытания, ракеты и скафандры."""
import math

from PIL import Image, ImageDraw

from . import hd
from .art import (AMULET, CRYOSTEEL, ICEBLUE, MEDALLION, PYRO, STAFF, STELLAR, TEAR, VOIDP,
                  _sprite_extra, rainbow, sparkle)
from .pixel import ART, BLADE_ART, orb, sprite
from .recipes import I, altar, mech, shaped, shapeless, smithing, when

FLAME = {"o": 0x3A0A04, "d": 0xB02A0C, "m": 0xFF7A1C, "l": 0xFFC060, "w": 0xFFF4D0}
FROSTP = {"o": 0x0E2448, "d": 0x2C6CB0, "m": 0x6EC6FF, "l": 0xC8F0FF, "w": 0xFFFFFF}
CRYO_CLOTH = {"o": 0x14284A, "d": 0x3A6AB0, "m": 0x8FC8F0, "l": 0xE0F4FF, "w": 0xFFFFFF}
PYRO_CLOTH = {"o": 0x3A0A0A, "d": 0x9A1A10, "m": 0xD8401A, "l": 0xFFB050, "w": 0xFFF0C0}
PARCH = {"o": 0x4A3418, "d": 0xA88850, "m": 0xD8C090, "l": 0xF0E2C0, "w": 0xFFF8E8}
MANA = {"o": 0x14104A, "d": 0x3A3AC8, "m": 0x6E8CFF, "l": 0xB8D0FF, "w": 0xFFFFFF}
GREATER = {"o": 0x2A0A4A, "d": 0x7A2AC8, "m": 0xC070FF, "l": 0xF0C8FF, "w": 0xFFFFFF}
FEATHERP = {"o": 0x3A0804, "d": 0xC02A0A, "m": 0xFF7A1C, "l": 0xFFD060, "w": 0xFFFFF0}

GRIMOIRE = """
................
..ooooooooooo...
..odmmmmmmmmlo..
..odmmmmwmmmlo..
..odmmmwlwmmlo..
..odmmwlllwmlo..
..odmmmwlwmmlo..
..odmmmmwmmmlo..
..odmmmmmmmmlo..
..odmmmmmmmmlo..
..oddddddddddo..
..owwwwwwwwwwo..
..oddddddddddo..
..ooooooooooo...
"""

POTION = """
................
......oooo......
......oHHo......
.......oo.......
......owlo......
.....owllmo.....
....owllmmmo....
...owlmmmmmdo...
...olmmmmmmdo...
...olmmmmmmdo...
...olmmmmmmdo...
....odmmmmddo...
.....odddddo....
......ooooo.....
"""

SCROLL = """
................
..oooooooooooo..
.owwwwwwwwwwwwo.
..oooooooooooo..
..owllllllllo...
..owlmmmmmmlo...
..owllllllllo...
..owlmmmmmlo....
..owllllllllo...
..owlmmmmmmlo...
..owllllllllo...
.oooooooooooo...
owwwwwwwwwwwwo..
.oooooooooooo...
"""

RING = """
................
................
......oooo......
.....owwlo......
.....olmmo......
....ogommogo....
...og......go...
..og........go..
..og........go..
..og........go..
...og......go...
....oggggggo....
.....oooooo.....
"""

FEATHER = """
................
............oo..
...........owo..
..........owlo..
.........owlmo..
........owlmo...
.......owlmo....
......owlmdo....
.....owlmdo.....
....owlmdo......
...owlmdo.......
...olmdo........
..oHdo..........
..oHo...........
.oo.............
"""

ICICLE = """
................
.....oooooo.....
....owwllmmo....
.....owlmmo.....
.....owlmdo.....
......olmo......
......olmo......
......olmo......
.......oo.......
.......oo.......
"""


def _hd(new, base_seed):
    return {n: (hd.hd_item(img, seed=base_seed + i, sparkles=sp), hh) for i, (n, (img, hh, sp)) in enumerate(new.items())}


def item_textures():
    gold = {"g": 0xF2C030}
    silver = {"g": 0xC8D4E8}
    new = {
        "yuki_grimoire": (sprite(GRIMOIRE, FROSTP), False, 2),
        "sintara_tome": (sprite(GRIMOIRE, FLAME), False, 2),
        "yuki_staff": (sprite(STAFF, FROSTP), True, 5),
        "sintara_staff": (sprite(STAFF, FLAME), True, 5),
        "sintara_amulet": (sprite(AMULET, FLAME), False, 4),
        "staff_of_elements": (rainbow(sprite(STAFF, TEAR), 0.5, 0.2), True, 6),
        "mana_potion": (sprite(POTION, MANA), False, 1),
        "greater_mana_potion": (sprite(POTION, GREATER), False, 3),
        "frost_essence": (sprite(ART["yuki_crystal"][0], FROSTP), False, 2),
        "flame_essence": (sprite(ART["yuki_crystal"][0], FLAME), False, 2),
        "mana_crystal": (sparkle(sprite(ART["yuki_crystal"][0], MANA), 0xFFFFFF, 7, 4), False, 4),
        "respec_scroll": (sprite(SCROLL, PARCH), False, 0),
        "frost_ring": (_sprite_extra(RING, FROSTP, silver), False, 2),
        "flame_ring": (_sprite_extra(RING, FLAME, gold), False, 2),
        "ring_of_eternal_winter": (_sprite_extra(RING, {**FROSTP, "m": 0xBFEFFF}, gold), False, 5),
        "ring_of_sintara": (_sprite_extra(RING, {**FLAME, "m": 0xFFE070}, gold), False, 5),
        "archon_heart": (sprite(ART["yuki_crystal"][0], ICEBLUE), False, 5),
        "phoenix_feather": (sprite(FEATHER, FEATHERP), False, 4),
        "sigil_archon": (sprite(MEDALLION, FROSTP), False, 3),
        "sigil_phoenix": (sprite(MEDALLION, FLAME), False, 3),
        # снаряды
        "orb_frost": (orb(FROSTP), False, 0),
        "orb_flame": (orb(FLAME), False, 0),
        "orb_void": (orb(VOIDP), False, 0),
        "orb_star": (orb(TEAR), False, 0),
        "orb_boulder": (orb(ICEBLUE), False, 0),
        "orb_meteor": (orb({"o": 0x1A0804, "d": 0x5A1A0A, "m": 0xC04A10, "l": 0xFF9A3A, "w": 0xFFE070}), False, 0),
        "orb_icicle": (sprite(ICICLE, FROSTP), False, 0),
        "orb_feather": (sprite(FEATHER, FEATHERP), False, 0),
        "orb_blade": (sprite(BLADE_ART, TEAR), True, 0),
    }
    for piece, art in (("hood", "helmet"), ("robe", "chestplate"), ("leggings", "leggings"), ("boots", "boots")):
        new[f"cryomancer_{piece}"] = (sprite(ART[f"frost_{art}"][0], CRYO_CLOTH), False, 1)
        new[f"pyromancer_{piece}"] = (sprite(ART[f"frost_{art}"][0], PYRO_CLOTH), False, 1)
    for mat, pal in (("frost", CRYOSTEEL), ("sintara", PYRO), ("star", STELLAR)):
        for piece, art in (("helmet", "helmet"), ("suit", "chestplate"), ("pants", "leggings"), ("boots", "boots")):
            new[f"{mat}_space_{piece}"] = (sprite(ART[f"frost_{art}"][0], pal), False, 2 if mat == "star" else 1)
    return _hd(new, 600)


SUIT_ITEMS = [f"{m}_space_{p}" for m in ("frost", "sintara", "star") for p in ("helmet", "suit", "pants", "boots")]
ROCKETS = ["frost_rocket", "sintara_rocket", "star_rocket"]
ROBES = [f"{s}_{p}" for s in ("cryomancer", "pyromancer") for p in ("hood", "robe", "leggings", "boots")]
ARMOR_LAYERS = {"cryomancer": CRYO_CLOTH, "pyromancer": PYRO_CLOTH, "frost_suit": CRYOSTEEL, "sintara_suit": PYRO,
                "star_suit": STELLAR}


# ------------------------------------------------------------------ иконки заклинаний (32×32)
def _icon_base(c1, c2):
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for r in range(15, 0, -1):
        t = r / 15
        col = tuple(int(c1[i] * t + c2[i] * (1 - t)) for i in range(3))
        d.ellipse([16 - r, 16 - r, 16 + r, 16 + r], fill=col + (255,))
    d.ellipse([1, 1, 30, 30], outline=(255, 255, 255, 120), width=1)
    return img, d


def _flake(d, cx, cy, r, col, w=2):
    for k in range(6):
        a = k * math.pi / 3
        x, y = cx + math.cos(a) * r, cy + math.sin(a) * r
        d.line([cx, cy, x, y], fill=col, width=w)
        for s in (-1, 1):
            bx, by = cx + math.cos(a) * r * 0.6, cy + math.sin(a) * r * 0.6
            d.line([bx, by, bx + math.cos(a + s * 0.8) * r * 0.3, by + math.sin(a + s * 0.8) * r * 0.3], fill=col, width=1)


def _flame(d, cx, cy, s, outer, inner):
    d.polygon([(cx, cy - s), (cx + s * 0.6, cy), (cx + s * 0.45, cy + s * 0.6), (cx, cy + s * 0.8),
               (cx - s * 0.45, cy + s * 0.6), (cx - s * 0.6, cy)], fill=outer)
    d.polygon([(cx, cy - s * 0.4), (cx + s * 0.3, cy + s * 0.2), (cx, cy + s * 0.6), (cx - s * 0.3, cy + s * 0.2)],
              fill=inner)


def spell_icons():
    W, B = (255, 255, 255, 255), (200, 240, 255, 255)
    ICE1, ICE2 = (40, 90, 170), (8, 20, 50)
    F1, F2 = (200, 70, 20), (40, 8, 4)
    OR, YE = (255, 140, 40, 255), (255, 230, 120, 255)
    icons = {}

    img, d = _icon_base(ICE1, ICE2)
    d.polygon([(6, 26), (22, 6), (26, 10), (10, 28)], fill=B)
    d.polygon([(22, 6), (28, 4), (26, 10)], fill=W)
    icons["frost_bolt"] = img

    img, d = _icon_base(ICE1, ICE2)
    _flake(d, 16, 16, 12, W)
    d.ellipse([12, 12, 20, 20], fill=B)
    icons["frost_nova"] = img

    img, d = _icon_base(ICE1, ICE2)
    d.polygon([(16, 4), (27, 9), (25, 20), (16, 28), (7, 20), (5, 9)], fill=(140, 210, 255, 255), outline=W)
    _flake(d, 16, 15, 6, W, 1)
    icons["glacial_armor"] = img

    img, d = _icon_base(ICE1, ICE2)
    for k, a in enumerate((90, 160, 220)):
        d.arc([5 + k * 2, 5 + k * 2, 27 - k * 2, 27 - k * 2], a, a + 200, fill=(200, 240, 255, 255 - k * 50), width=2)
    d.polygon([(22, 8), (27, 13), (20, 14)], fill=W)
    icons["frost_blink"] = img

    img, d = _icon_base(ICE1, ICE2)
    d.ellipse([5, 6, 19, 16], fill=(220, 235, 250, 255))
    d.ellipse([12, 4, 27, 16], fill=(235, 245, 255, 255))
    for x, y in ((8, 20), (14, 24), (20, 20), (24, 26), (11, 27)):
        _flake(d, x, y, 2.5, W, 1)
    icons["blizzard"] = img

    img, d = _icon_base((90, 170, 255), (10, 10, 40))
    _flake(d, 16, 16, 13, W, 2)
    d.ellipse([3, 3, 29, 29], outline=(255, 230, 140, 255), width=2)
    d.ellipse([13, 13, 19, 19], fill=YE)
    icons["absolute_zero"] = img

    img, d = _icon_base(F1, F2)
    d.polygon([(4, 28), (14, 14), (18, 18)], fill=(255, 120, 30, 180))
    d.ellipse([13, 6, 27, 20], fill=OR)
    d.ellipse([17, 9, 24, 16], fill=YE)
    icons["ember_bolt"] = img

    img, d = _icon_base(F1, F2)
    for k in range(8):
        a = k * math.pi / 4
        _flame(d, 16 + math.cos(a) * 10, 16 + math.sin(a) * 10, 5, OR, YE)
    d.ellipse([12, 12, 20, 20], fill=(255, 200, 80, 255))
    icons["flame_ring"] = img

    img, d = _icon_base(F1, F2)
    for s in (-1, 1):
        for k in range(4):
            d.polygon([(16, 18), (16 + s * (6 + k * 3), 8 + k * 3), (16 + s * (4 + k * 3), 14 + k * 3)],
                      fill=(255, 150 - k * 20, 40, 255))
    _flame(d, 16, 20, 7, OR, YE)
    icons["phoenix_wings"] = img

    img, d = _icon_base(F1, F2)
    for k in range(3):
        x = 6 + k * 7
        d.polygon([(x, 8), (x + 8, 16), (x, 24), (x + 3, 16)], fill=(255, 160 + k * 30, 40 + k * 30, 255))
    icons["flame_dash"] = img

    img, d = _icon_base(F1, F2)
    d.polygon([(4, 4), (16, 14), (12, 18)], fill=(255, 120, 30, 200))
    d.ellipse([12, 12, 27, 27], fill=(120, 50, 30, 255))
    d.ellipse([14, 14, 22, 22], fill=OR)
    d.ellipse([17, 17, 20, 20], fill=YE)
    icons["meteor"] = img

    img, d = _icon_base((255, 160, 40), (60, 10, 4))
    for k in range(12):
        a = k * math.pi / 6
        r = 14 if k % 2 == 0 else 9
        d.line([16, 16, 16 + math.cos(a) * r, 16 + math.sin(a) * r], fill=YE, width=2)
    d.ellipse([10, 10, 22, 22], fill=(255, 250, 220, 255))
    icons["supernova"] = img

    # таланты
    img, d = _icon_base(ICE1, ICE2)
    d.polygon([(16, 27), (5, 15), (8, 7), (16, 11), (24, 7), (27, 15)], fill=(120, 200, 255, 255), outline=W)
    icons["ice_heart"] = img
    img, d = _icon_base(ICE1, ICE2)
    d.polygon([(16, 3), (24, 14), (16, 29), (8, 14)], fill=(170, 230, 255, 255), outline=W)
    d.line([16, 3, 16, 29], fill=W)
    icons["permafrost"] = img
    img, d = _icon_base(F1, F2)
    _flame(d, 16, 16, 12, OR, YE)
    icons["fire_will"] = img
    img, d = _icon_base(F1, F2)
    for k in range(8):
        a = k * math.pi / 4
        d.line([16, 16, 16 + math.cos(a) * 13, 16 + math.sin(a) * 13], fill=OR, width=2)
    d.ellipse([9, 9, 23, 23], fill=YE)
    icons["sintara_heat"] = img
    return icons


def effect_icons(icons):
    def small(name):
        return icons[name].resize((18, 18), Image.LANCZOS)
    return {"frozen": small("absolute_zero"), "frost_armor": small("glacial_armor"),
            "phoenix": small("phoenix_wings"), "rebirth": small("supernova")}


def rocket_texture(src, stops):
    return hd.recolor_entity(src, stops, 2, 0.75)


ROCKET_STOPS = {
    "frost_rocket": [0x0A1E3A, 0x2C6CB0, 0x8FD8FF, 0xF2FBFF],
    "sintara_rocket": [0x2A0804, 0xA02A0C, 0xFF8A2A, 0xFFE6A0],
    "star_rocket": [0x1A0A30, 0x6A3AC8, 0xE0B0FF, 0xFFF6D0],
}


# ------------------------------------------------------------------ рецепты
def recipes():
    r = {}
    # фокусы
    r["yuki_grimoire"] = shaped(["SAS", "LBL", "SAS"], {"S": "minecraft:snow_block", "A": "minecraft:amethyst_shard",
                                                         "L": "minecraft:lapis_lazuli", "B": "minecraft:book"},
                                I("yuki_grimoire"))
    r["sintara_tome"] = shaped(["FAF", "LBL", "FAF"], {"F": "minecraft:blaze_powder", "A": "minecraft:amethyst_shard",
                                                        "L": "minecraft:lapis_lazuli", "B": "minecraft:book"},
                               I("sintara_tome"))
    r["altar/yuki_staff"] = altar(1, I("yuki_crystal_block"), [I("frost_essence"), I("frost_essence"),
                                                               I("cryosteel_ingot"), I("cryosteel_ingot")],
                                  I("yuki_staff"), 1, 200)
    r["altar/sintara_staff"] = altar(1, I("yuki_crystal_block"), [I("flame_essence"), I("flame_essence"),
                                                                  I("cryosteel_ingot"), I("cryosteel_ingot")],
                                     I("sintara_staff"), 1, 200)
    r["altar/staff_of_elements"] = altar(3, I("yuki_heart"), [I("yuki_staff"), I("sintara_staff"), I("archon_heart"),
                                                              I("phoenix_feather"), I("stellar_alloy"),
                                                              I("stellar_alloy"), I("mana_crystal"), I("mana_crystal")],
                                         I("staff_of_elements"), 1, 600)
    # расходники
    r["mana_potion"] = shapeless(["minecraft:glass_bottle", "minecraft:lapis_lazuli", "minecraft:lapis_lazuli",
                                  "minecraft:amethyst_shard", "minecraft:sugar"], I("mana_potion"))
    r["greater_mana_potion"] = shapeless([I("mana_potion"), "minecraft:ghast_tear", I("yuki_crystal"),
                                          "minecraft:glowstone_dust"], I("greater_mana_potion"))
    r["frost_essence"] = shapeless(["minecraft:packed_ice", "minecraft:packed_ice", "minecraft:lapis_lazuli",
                                    "minecraft:amethyst_shard"], I("frost_essence"))
    r["flame_essence"] = shapeless(["minecraft:blaze_powder", "minecraft:magma_cream", "minecraft:lapis_lazuli",
                                    "minecraft:amethyst_shard"], I("flame_essence"))
    r["altar/mana_crystal"] = altar(2, "minecraft:diamond_block", [I("yuki_crystal")] * 4 + [I("frost_essence")] * 2
                                    + [I("flame_essence")] * 2, I("mana_crystal"), 1, 300)
    r["respec_scroll"] = shapeless(["minecraft:paper", I("frost_essence"), I("flame_essence"),
                                    "minecraft:amethyst_shard", "minecraft:ender_pearl"], I("respec_scroll"))
    # кольца
    r["frost_ring"] = shaped(["ECE", "G G", "GGG"], {"E": I("frost_essence"), "C": "minecraft:diamond",
                                                     "G": "minecraft:gold_ingot"}, I("frost_ring"))
    r["flame_ring"] = shaped(["ECE", "G G", "GGG"], {"E": I("flame_essence"), "C": "minecraft:diamond",
                                                     "G": "minecraft:gold_ingot"}, I("flame_ring"))
    r["altar/ring_of_eternal_winter"] = altar(2, I("frost_ring"), [I("archon_heart"), I("aurorium_ingot"),
                                                                  I("cryosteel_ingot"), I("cryosteel_ingot")]
                                              + [I("frost_essence")] * 4, I("ring_of_eternal_winter"), 1, 400)
    r["altar/ring_of_sintara"] = altar(2, I("flame_ring"), [I("phoenix_feather"), I("aurorium_ingot"),
                                                           I("cryosteel_ingot"), I("cryosteel_ingot")]
                                       + [I("flame_essence")] * 4, I("ring_of_sintara"), 1, 400)
    # мантии
    for school, wool, ess in (("cryomancer", "minecraft:light_blue_wool", I("frost_essence")),
                              ("pyromancer", "minecraft:red_wool", I("flame_essence"))):
        key = {"W": wool, "E": ess, "G": "minecraft:gold_ingot"}
        r[f"{school}_hood"] = shaped(["WEW", "W W"], key | {}, I(f"{school}_hood"))
        r[f"{school}_robe"] = shaped(["W W", "WEW", "GWG"], key, I(f"{school}_robe"))
        r[f"{school}_leggings"] = shaped(["WEW", "W W", "W W"], key, I(f"{school}_leggings"))
        r[f"{school}_boots"] = shaped(["W W", "E E"], key, I(f"{school}_boots"))
        for k in ("hood", "leggings", "boots"):
            r[f"{school}_{k}"]["key"].pop("G", None)
    # испытания стихий
    r["altar/sigil_archon"] = altar(2, I("frost_core"), [I("frost_essence")] * 4 + [I("cryosteel_ingot")] * 2
                                    + ["minecraft:blue_ice"] * 2, I("sigil_archon"), 1, 300)
    r["altar/sigil_phoenix"] = altar(2, I("frost_core"), [I("flame_essence")] * 4 + [I("cryosteel_ingot")] * 2
                                     + ["minecraft:blaze_rod"] * 2, I("sigil_phoenix"), 1, 300)
    # ракеты (Create, механический крафтер)
    r["rocket/frost_rocket"] = when(mech(["   N   ", "  PCP  ", "  PTP  ", " FPXPF ", " F E F "], {
        "N": "ad_astra:rocket_nose_cone", "P": I("cryosteel_block"), "C": I("cryo_circuit"),
        "T": "ad_astra:tier_4_rocket", "X": I("frost_core"), "F": "ad_astra:rocket_fin",
        "E": "ad_astra:calorite_engine"}, I("frost_rocket")), "ad_astra")
    r["rocket/sintara_rocket"] = when(mech(["   N   ", "  PSP  ", "  PTP  ", " FPXPF ", " F E F "], {
        "N": "ad_astra:rocket_nose_cone", "P": I("aurora_block"), "S": I("phoenix_feather"),
        "T": I("frost_rocket"), "X": I("blizzard_crown"), "F": "ad_astra:rocket_fin",
        "E": "ad_astra:calorite_engine"}, I("sintara_rocket")), "ad_astra")
    r["rocket/star_rocket"] = when(mech(["   N   ", "  ASA  ", "  ATA  ", " FAXAF ", " FEEEF "], {
        "N": "ad_astra:rocket_nose_cone", "A": I("pyrolite_block"), "S": I("ash_core"),
        "T": I("sintara_rocket"), "X": I("celestium_block"), "F": "ad_astra:rocket_fin",
        "E": "ad_astra:calorite_engine"}, I("star_rocket")), "ad_astra")
    # скафандры (кузнечный стол)
    aa = {"helmet": "space_helmet", "suit": "space_suit", "pants": "space_pants", "boots": "space_boots"}
    for piece, base in aa.items():
        r[f"suit/frost_space_{piece}"] = when(smithing(I("frost_upgrade_template"), f"ad_astra:{base}",
                                                       I("cryosteel_block"), I(f"frost_space_{piece}")), "ad_astra")
        r[f"suit/sintara_space_{piece}"] = when(smithing(I("aurorium_upgrade_template"), f"ad_astra:netherite_{base}",
                                                         I("aurora_block"), I(f"sintara_space_{piece}")), "ad_astra")
        r[f"suit/star_space_{piece}"] = when(smithing(I("celestium_upgrade_template"), I(f"sintara_space_{piece}"),
                                                      I("pyrolite_block"), I(f"star_space_{piece}")), "ad_astra")
    return r


# ------------------------------------------------------------------ лут
BOSS_LOOT = {
    "glacial_archon": [("archon_heart", 1, 1), ("frost_essence", 8, 16), ("mana_crystal", 1, 1), ("star_shard", 1, 2)],
    "infernal_phoenix": [("phoenix_feather", 1, 1), ("flame_essence", 8, 16), ("mana_crystal", 1, 1),
                         ("star_shard", 1, 2)],
}
BOSS_EXTRA = {  # добавка к старым боссам
    "frost_colossus": [("frost_essence", 6, 12), ("greater_mana_potion", 2, 3)],
    "blizzard_queen": [("frost_essence", 8, 14), ("greater_mana_potion", 2, 3), ("mana_crystal", 1, 1)],
    "ash_lord": [("flame_essence", 8, 14), ("greater_mana_potion", 2, 3), ("mana_crystal", 1, 1)],
    "void_wyrm": [("frost_essence", 6, 10), ("flame_essence", 6, 10), ("mana_crystal", 1, 2)],
    "shadow_of_yuki": [("frost_essence", 10, 16), ("flame_essence", 10, 16), ("mana_crystal", 2, 3)],
}
ESSENCE_DROPS = [("frost_essence_stray", "minecraft:entities/stray", "frost_essence", 0.3),
                 ("frost_essence_snow_golem", "minecraft:entities/snow_golem", "frost_essence", 0.15),
                 ("flame_essence_blaze", "minecraft:entities/blaze", "flame_essence", 0.3),
                 ("flame_essence_magma_cube", "minecraft:entities/magma_cube", "flame_essence", 0.1),
                 ("mana_potion_witch", "minecraft:entities/witch", "mana_potion", 0.25)]


# ------------------------------------------------------------------ книга
CATEGORY = {"magic": ("Две стихии", "Магия Юки и Синтары: лёд и пламя.", "yukicore:yuki_grimoire", 10),
            "space": ("Ракеты и скафандры", "Как долететь до дальних планет Системы Юки.", "yukicore:frost_space_suit", 11)}

ENTRIES = [
    ("magic", "two_elements", "Две стихии", "yukicore:yuki_grimoire", [
        "$(o)Юки — дух зимы. Синтара — огненная волшебница, её лучшая подруга. Когда Тень расколола Сердце, "
        "они спрятали свою магию в двух книгах.$()",
        "$(l)Лёд Юки$() — контроль: замедление, заморозка, щиты, метель.$(br)$(l)Пламя Синтары$() — разрушение: "
        "огненные шары, рывки, метеоры и возрождение из пепла.",
        "Каждая школа качается отдельно: творишь заклинания и побеждаешь врагов — растёт $(l)уровень$() (до 60) "
        "и копятся $(l)очки навыков$(). В каждой ветке — дерево из 22 навыков.",
        ("spot", "yukicore:yuki_grimoire", "$(l)Гримуар Юки$() — открывает ледяную школу. Держи в инвентаре."),
        ("spot", "yukicore:sintara_tome", "$(l)Фолиант Синтары$() — открывает огненную школу."),
    ]),
    ("magic", "controls", "Управление", "minecraft:writable_book", [
        "$(li)$(l)K$() или Shift+ПКМ фокусом — Гримуар стихий (дерево навыков)$(li)$(l)ЛКМ$() по узлу — изучить или "
        "улучшить$(li)$(l)ПКМ$() по узлу — выбрать$(li)$(l)R$() — сотворить выбранное$(li)$(l)Z$() — следующее "
        "заклинание (Shift+Z — предыдущее)",
        "Внизу экрана, слева от хотбара, — $(l)мана$() и выбранное заклинание с перезарядкой. Клавиши можно "
        "сменить в настройках управления.",
    ]),
    ("magic", "yuki_spells", "Заклинания Юки", "yukicore:yuki_staff", [
        "$(li)$(#8fd8ff)Ледяная стрела$() (ур. 1) — снаряд, замедляет. С 3 ранга — веер из трёх.$(li)$(#8fd8ff)Ледяная "
        "нова$() (3) — заморозка вокруг.$(li)$(#8fd8ff)Ледяной панцирь$() (6) — поглощение, сопротивление и холод "
        "для атакующих.",
        "$(li)$(#8fd8ff)Ледяной шаг$() (10) — телепорт по взгляду с волнами холода.$(li)$(#8fd8ff)Метель$() (15) — "
        "буря в точке на 6–10 секунд.$(li)$(#ffd060)★ Абсолютный ноль$() (20) — заморозить всё в 12 блоках. Нужно "
        "пройти $(l)Испытание Юки$().",
    ]),
    ("magic", "sintara_spells", "Заклинания Синтары", "yukicore:sintara_staff", [
        "$(li)$(#ffa040)Огненный шар$() (ур. 1) — взрывается, поджигает. С 3 ранга — очередь из трёх.$(li)$(#ffa040)"
        "Огненное кольцо$() (3) — отбрасывает и жжёт.$(li)$(#ffa040)Крылья феникса$() (6) — подъём, плавное падение, "
        "регенерация, +30% к огню.",
        "$(li)$(#ffa040)Огненный рывок$() (10) — рывок сквозь врагов.$(li)$(#ffa040)Метеор$() (15) — падает с неба в "
        "точку взгляда; на 5 ранге — три.$(li)$(#ffd060)★ Сверхновая$() (20) — взрыв в 10 блоках и минута "
        "$(l)Возрождения$(): один раз не умрёшь. Нужно $(l)Испытание Синтары$().",
    ]),
    ("magic", "gear", "Снаряжение мага", "yukicore:cryomancer_robe", [
        ("spot", "yukicore:yuki_staff", "$(l)Посохи$() в руке усиливают свою школу на 25%. $(l)Посох Двух Стихий$() — "
                                        "обе школы, +40%."),
        ("spot", "yukicore:cryomancer_robe", "$(l)Мантии криоманта и пироманта$(): каждая часть +6% силы, "
                                             "−5% маны, +15 к запасу; комплект ещё +15% силы."),
        ("spot", "yukicore:frost_ring", "$(l)Кольца$() работают из инвентаря: +10%, легендарные — +20%."),
        ("spot", "yukicore:mana_crystal", "$(l)Кристалл маны$(): навсегда +10 к запасу (до 10 раз). Зелья маны "
                                          "восполняют 60 и 220."),
        ("spot", "yukicore:respec_scroll", "$(l)Свиток забвения$() возвращает все очки навыков."),
    ]),
    ("magic", "elements", "Слабости стихий", "yukicore:frost_essence", [
        "Лёд бьёт огненных врагов в $(l)полтора раза сильнее$(), огонь — ледяных. Свою стихию босс получает вдвое "
        "слабее.$(br2)Колосс, Королева и Архонт боятся $(#ffa040)пламени Синтары$(). Повелитель Пепла и Феникс — "
        "$(#8fd8ff)льда Юки$().",
        ("spot", "yukicore:frost_essence", "$(l)Эссенции$() дают 250 опыта своей школы. Падают со зимогоров и "
                                           "ифритов, крафтятся."),
    ]),
    ("magic", "trials", "Испытания стихий", "yukicore:sigil_archon", [
        "Чтобы открыть ультимативное заклинание, победи стража своей стихии. Печати работают в любом мире.",
        ("spot", "yukicore:sigil_archon", "$(l)Ледяной Архонт$() — рыцарь с двуручным мечом и щитом. 10 000 ❤. "
                                          "Ледяные копья из-под земли и ледяная тюрьма."),
        ("spot", "yukicore:sigil_phoenix", "$(l)Пламенный Феникс$() — огненная птица. 10 000 ❤ и $(l)одно "
                                           "возрождение$(). Пикирует, крутит огненный смерч, зовёт метеоры."),
        "Победа засчитывается всем игрокам в радиусе 64 блоков и даёт 3000 опыта школы.",
    ]),
    ("stars", "bosses_v6", "Боссы стали сильнее", "yukicore:sigil_shadow", [
        "Стражи планет получили 3D-тела, по 6–7 атак и $(l)три фазы$(). На 66% и 33% здоровья они ревут, "
        "отбрасывают и зовут помощь.",
        "$(li)Колосс — 10 000 ❤$(li)Королева — 12 500 ❤$(li)Повелитель Пепла — 15 000 ❤$(li)Змей — 18 000 ❤"
        "$(li)Тень Юки — 30 000 ❤",
        "Круги на земле — $(l)предупреждение$(): уйди, пока не ударило. Через ударные волны можно $(l)перепрыгнуть$(). "
        "Один удар снимает не больше 2,5% здоровья босса.",
    ]),
    ("space", "rockets", "Ракеты Юки", "yukicore:frost_rocket", [
        "Планеты Системы Юки требуют ракет YukiCore (механический крафтер Create): каждая следующая ракета строится "
        "из трофеев предыдущей планеты.",
        ("spot", "yukicore:frost_rocket", "$(l)Морозная ракета$() — 5 уровень: Юкия и Аврора."),
        ("spot", "yukicore:sintara_rocket", "$(l)Ракета Синтары$() — 6 уровень: Пиролит. Нужны блоки авроры и Корона Метели."),
        ("spot", "yukicore:star_rocket", "$(l)Звёздная ракета$() — 7 уровень: Никта и Целестия. Нужны блоки пиролита и Пепельное ядро."),
    ]),
    ("space", "suits", "Скафандры", "yukicore:frost_space_suit", [
        ("spot", "yukicore:frost_space_suit", "$(l)Морозный скафандр$(): бак кислорода 2 000 мБ, защита от холода. "
                                              "Кузнечный стол: скафандр Ad Astra + морозный шаблон + блок криостали."),
        ("spot", "yukicore:sintara_space_suit", "$(l)Скафандр Синтары$(): 4 000 мБ, жар и холод. Из незеритового "
                                                "скафандра + ауриевый шаблон + блок авроры."),
        ("spot", "yukicore:star_space_suit", "$(l)Звёздный скафандр$(): 8 000 мБ, всё сразу. Полный комплект: ночное "
                                             "зрение, скорость, нет урона от падения."),
    ]),
]


# ------------------------------------------------------------------ локализация (ru, en)
SPELL_TEXT = {
    "frost_bolt": ("Ледяная стрела", "Frost Bolt", "Снаряд льда: урон и замедление. С 3 ранга — веер из трёх стрел.",
                   "An ice projectile that slows. From rank 3 — a fan of three."),
    "frost_nova": ("Ледяная нова", "Frost Nova", "Волна холода вокруг: урон и заморозка врагов на 2–4 с.",
                   "A wave of cold: damage and freeze enemies for 2–4 s."),
    "glacial_armor": ("Ледяной панцирь", "Glacial Armor",
                      "Поглощение и сопротивление на 30–50 с. Атакующие замерзают и получают урон.",
                      "Absorption and resistance for 30–50 s. Attackers freeze and take damage."),
    "frost_blink": ("Ледяной шаг", "Frost Blink", "Телепорт на 10–18 блоков по взгляду. Холодные волны в начале и конце.",
                    "Teleport 10–18 blocks where you look, with cold bursts at both ends."),
    "blizzard": ("Метель", "Blizzard", "Буря в точке взгляда на 6–10 с: урон каждые полсекунды и замедление.",
                 "A storm at the target for 6–10 s: damage every half second and slowness."),
    "absolute_zero": ("Абсолютный ноль", "Absolute Zero",
                      "Ультимативное. Огромный урон и полная заморозка всего в 12 блоках на 8 с.",
                      "Ultimate. Huge damage and a full freeze of everything within 12 blocks for 8 s."),
    "ember_bolt": ("Огненный шар Синтары", "Sintara's Ember", "Взрывающийся огненный шар. С 3 ранга — очередь из трёх.",
                   "An exploding fireball. From rank 3 — a volley of three."),
    "flame_ring": ("Огненное кольцо", "Flame Ring", "Кольцо пламени вокруг: урон, поджог и отбрасывание.",
                   "A ring of flame: damage, burning and knockback."),
    "phoenix_wings": ("Крылья феникса", "Phoenix Wings",
                      "Взлёт, плавное падение, регенерация, скорость и +30% к огненной магии на 12–20 с.",
                      "Lift-off, slow falling, regeneration, speed and +30% fire power for 12–20 s."),
    "flame_dash": ("Огненный рывок", "Flame Dash", "Рывок вперёд сквозь врагов: урон и поджог всех на пути.",
                   "Dash forward through enemies, burning everything on the way."),
    "meteor": ("Метеор", "Meteor", "Метеор падает в точку взгляда. На 5 ранге — три метеора.",
               "A meteor falls where you look. At rank 5 — three meteors."),
    "supernova": ("Сверхновая", "Supernova",
                  "Ультимативное. Взрыв пламени в 10 блоках и минута Возрождения: один раз не умрёшь.",
                  "Ultimate. A blast of fire within 10 blocks and a minute of Rebirth: you cheat death once."),
}
PASSIVE_TEXT = {
    "ice_heart": ("Сердце льда", "Heart of Ice", "+20 к запасу маны за ранг.", "+20 max mana per rank."),
    "permafrost": ("Вечная мерзлота", "Permafrost", "+8% к силе ледяной магии за ранг.", "+8% ice power per rank."),
    "fire_will": ("Воля пламени", "Will of Flame", "+0,6 маны в секунду за ранг.", "+0.6 mana per second per rank."),
    "sintara_heat": ("Жар Синтары", "Sintara's Heat", "+8% к силе огненной магии за ранг.", "+8% fire power per rank."),
}

LANG = {
    # фокусы и предметы
    "item.yukicore.yuki_grimoire": ("Гримуар Юки", "Yuki's Grimoire"),
    "item.yukicore.yuki_grimoire.desc": ("Открывает ледяную магию", "Unlocks ice magic"),
    "item.yukicore.sintara_tome": ("Фолиант Синтары", "Sintara's Tome"),
    "item.yukicore.sintara_tome.desc": ("Открывает огненную магию", "Unlocks fire magic"),
    "item.yukicore.yuki_staff": ("Посох Юки", "Yuki's Staff"),
    "item.yukicore.sintara_staff": ("Посох Синтары", "Sintara's Staff"),
    "item.yukicore.sintara_staff.desc": ("♥ Подарок для Sintara — огненной волшебницы", "♥ A gift for Sintara, the fire mage"),
    "item.yukicore.staff_of_elements": ("Посох Двух Стихий", "Staff of Two Elements"),
    "item.yukicore.staff_of_elements.desc": ("Лёд Юки и пламя Синтары в одном посохе", "Yuki's ice and Sintara's flame in one staff"),
    "item.yukicore.mana_potion": ("Зелье маны", "Mana Potion"),
    "item.yukicore.mana_potion.desc": ("+60 маны", "+60 mana"),
    "item.yukicore.greater_mana_potion": ("Великое зелье маны", "Greater Mana Potion"),
    "item.yukicore.greater_mana_potion.desc": ("+220 маны", "+220 mana"),
    "item.yukicore.frost_essence": ("Эссенция льда", "Frost Essence"),
    "item.yukicore.frost_essence.desc": ("Использовать: +250 опыта магии Юки", "Use: +250 Yuki magic XP"),
    "item.yukicore.flame_essence": ("Эссенция пламени", "Flame Essence"),
    "item.yukicore.flame_essence.desc": ("Использовать: +250 опыта магии Синтары", "Use: +250 Sintara magic XP"),
    "item.yukicore.mana_crystal": ("Кристалл маны", "Mana Crystal"),
    "item.yukicore.mana_crystal.desc": ("Использовать: навсегда +10 к запасу маны (до 10 раз)",
                                        "Use: permanently +10 max mana (up to 10 times)"),
    "item.yukicore.respec_scroll": ("Свиток забвения", "Scroll of Oblivion"),
    "item.yukicore.respec_scroll.desc": ("Возвращает все очки навыков магии", "Refunds all magic skill points"),
    "item.yukicore.frost_ring": ("Кольцо инея", "Frost Ring"),
    "item.yukicore.frost_ring.desc": ("В инвентаре: +10% к ледяной магии", "In inventory: +10% ice power"),
    "item.yukicore.flame_ring": ("Кольцо пламени", "Flame Ring"),
    "item.yukicore.flame_ring.desc": ("В инвентаре: +10% к огненной магии", "In inventory: +10% fire power"),
    "item.yukicore.ring_of_eternal_winter": ("Кольцо вечной зимы", "Ring of Eternal Winter"),
    "item.yukicore.ring_of_eternal_winter.desc": ("В инвентаре: +20% к ледяной магии", "In inventory: +20% ice power"),
    "item.yukicore.ring_of_sintara": ("Кольцо Синтары", "Ring of Sintara"),
    "item.yukicore.ring_of_sintara.desc": ("В инвентаре: +20% к огненной магии", "In inventory: +20% fire power"),
    "item.yukicore.archon_heart": ("Сердце Архонта", "Archon's Heart"),
    "item.yukicore.archon_heart.desc": ("Трофей Испытания Юки", "Trophy of Yuki's Trial"),
    "item.yukicore.phoenix_feather": ("Перо Феникса", "Phoenix Feather"),
    "item.yukicore.phoenix_feather.desc": ("Трофей Испытания Синтары", "Trophy of Sintara's Trial"),
    "item.yukicore.sigil_archon": ("Печать Ледяного Архонта", "Sigil of the Glacial Archon"),
    "item.yukicore.sigil_phoenix": ("Печать Пламенного Феникса", "Sigil of the Infernal Phoenix"),
    "item.yukicore.frost_rocket": ("Морозная ракета", "Frost Rocket"),
    "item.yukicore.sintara_rocket": ("Ракета Синтары", "Sintara's Rocket"),
    "item.yukicore.star_rocket": ("Звёздная ракета Юки", "Yuki's Star Rocket"),
    "entity.yukicore.frost_rocket": ("Морозная ракета", "Frost Rocket"),
    "entity.yukicore.sintara_rocket": ("Ракета Синтары", "Sintara's Rocket"),
    "entity.yukicore.star_rocket": ("Звёздная ракета Юки", "Yuki's Star Rocket"),
    "entity.yukicore.magic_projectile": ("Магический снаряд", "Magic Projectile"),
    "entity.yukicore.glacial_archon": ("Ледяной Архонт", "Glacial Archon"),
    "entity.yukicore.infernal_phoenix": ("Пламенный Феникс", "Infernal Phoenix"),
    "entity.yukicore.minion.frost": ("Ледяной страж", "Frost Guard"),
    "entity.yukicore.minion.spirit": ("Дух метели", "Blizzard Spirit"),
    "entity.yukicore.minion.ash": ("Пепельный слуга", "Ash Servant"),
    "entity.yukicore.minion.shadow": ("Отражение Тени", "Shadow Reflection"),
    "effect.yukicore.frozen": ("Заморозка", "Frozen"),
    "effect.yukicore.frost_armor": ("Ледяной панцирь", "Glacial Armor"),
    "effect.yukicore.phoenix": ("Крылья феникса", "Phoenix Wings"),
    "effect.yukicore.rebirth": ("Возрождение", "Rebirth"),
    "boss.yukicore.phase": ("%s — фаза %s", "%s — phase %s"),
    "boss.yukicore.reborn": ("%s возрождается из пепла!", "%s rises from the ashes!"),
    "tooltip.yukicore.sigil_trial": ("Испытание стихий: работает в любом мире", "Elemental Trial: works in any world"),
    "tooltip.yukicore.focus.yuki": ("❄ Фокус магии Юки", "❄ Yuki magic focus"),
    "tooltip.yukicore.focus.sintara": ("✹ Фокус магии Синтары", "✹ Sintara magic focus"),
    "tooltip.yukicore.focus.both": ("❄✹ Фокус обеих стихий", "❄✹ Focus of both elements"),
    "tooltip.yukicore.focus.bonus": ("В руке: +%s%% к силе заклинаний", "In hand: +%s%% spell power"),
    "tooltip.yukicore.focus.use": ("ПКМ — сотворить, Shift+ПКМ — Гримуар стихий", "Use — cast, Sneak+Use — Grimoire"),
    "tooltip.yukicore.robe.yuki": ("Мантия криоманта", "Cryomancer's garb"),
    "tooltip.yukicore.robe.sintara": ("Мантия пироманта", "Pyromancer's garb"),
    "tooltip.yukicore.robe.set": ("Комплект: +25% силы, −20% маны, +40 к запасу", "Set: +25% power, −20% cost, +40 mana"),
    "tooltip.yukicore.rocket_tier": ("Уровень ракеты: %s", "Rocket tier: %s"),
    "tooltip.yukicore.space_suit.frost": ("Защита от холода, кислород 2 000 мБ", "Cold protection, 2,000 mB oxygen"),
    "tooltip.yukicore.space_suit.sintara": ("Защита от жара и холода, кислород 4 000 мБ",
                                            "Heat and cold protection, 4,000 mB oxygen"),
    "tooltip.yukicore.space_suit.star": ("Всё сразу, кислород 8 000 мБ; комплект: ночное зрение и скорость",
                                         "Everything, 8,000 mB oxygen; set: night vision and speed"),
    # магия
    "magic.yukicore.school.yuki": ("❄ Магия Юки", "❄ Yuki's Magic"),
    "magic.yukicore.school.sintara": ("✹ Магия Синтары", "✹ Sintara's Magic"),
    "magic.yukicore.levelup.yuki": ("Магия Юки: уровень %s", "Yuki's magic: level %s"),
    "magic.yukicore.levelup.sintara": ("Магия Синтары: уровень %s", "Sintara's magic: level %s"),
    "magic.yukicore.levelup.points": ("Очков навыков: %s — открой Гримуар (K)", "Skill points: %s — open the Grimoire (K)"),
    "magic.yukicore.no_spell": ("Заклинание не выбрано — открой Гримуар (K)", "No spell selected — open the Grimoire (K)"),
    "magic.yukicore.learn_first": ("Сначала изучи заклинание в Гримуаре (Shift+ПКМ)",
                                   "Learn a spell in the Grimoire first (Sneak+Use)"),
    "magic.yukicore.no_focus.yuki": ("Нужен Гримуар Юки или ледяной посох", "You need Yuki's Grimoire or an ice staff"),
    "magic.yukicore.no_focus.sintara": ("Нужен Фолиант Синтары или огненный посох", "You need Sintara's Tome or a fire staff"),
    "magic.yukicore.cooldown": ("%s: перезарядка %s с", "%s: cooldown %s s"),
    "magic.yukicore.no_mana": ("Не хватает маны", "Not enough mana"),
    "magic.yukicore.rebirth": ("✹ Пламя Синтары не дало тебе погибнуть!", "✹ Sintara's flame kept you alive!"),
    "magic.yukicore.trial.yuki": ("❄ Испытание Юки пройдено! Абсолютный ноль доступен.",
                                  "❄ Yuki's Trial complete! Absolute Zero unlocked."),
    "magic.yukicore.trial.sintara": ("✹ Испытание Синтары пройдено! Сверхновая доступна.",
                                     "✹ Sintara's Trial complete! Supernova unlocked."),
    "magic.yukicore.hud.none": ("Нет заклинания", "No spell"),
    "message.yukicore.crystal_max": ("Больше кристаллов маны не впитать", "You can't absorb more mana crystals"),
    "screen.yukicore.grimoire": ("Гримуар стихий", "Grimoire of Elements"),
    "screen.yukicore.grimoire.level": ("Уровень %s  •  Очки: %s", "Level %s  •  Points: %s"),
    "screen.yukicore.grimoire.footer": ("Мана %s/%s  •  Кристаллы %s/10  •  Испытания: ❄ %s  ✹ %s",
                                        "Mana %s/%s  •  Crystals %s/10  •  Trials: ❄ %s  ✹ %s"),
    "screen.yukicore.grimoire.hint": ("ЛКМ — изучить/улучшить  •  ПКМ — выбрать  •  R — сотворить  •  Z — следующее",
                                      "LMB — learn/upgrade  •  RMB — select  •  R — cast  •  Z — next"),
    "screen.yukicore.grimoire.rank": ("Ранг %s/%s", "Rank %s/%s"),
    "screen.yukicore.grimoire.cost": ("Мана: %s  •  Перезарядка: %s с", "Mana: %s  •  Cooldown: %s s"),
    "screen.yukicore.grimoire.need_level": ("Нужен уровень школы %s", "Requires school level %s"),
    "screen.yukicore.grimoire.need_trial.yuki": ("Нужно пройти Испытание Юки (Ледяной Архонт)",
                                                 "Requires Yuki's Trial (Glacial Archon)"),
    "screen.yukicore.grimoire.need_trial.sintara": ("Нужно пройти Испытание Синтары (Пламенный Феникс)",
                                                    "Requires Sintara's Trial (Infernal Phoenix)"),
    "screen.yukicore.grimoire.click_upgrade": ("ЛКМ — улучшить (%s оч.)", "LMB — upgrade (%s pt.)"),
    "screen.yukicore.grimoire.click_select": ("ПКМ — выбрать для клавиши R", "RMB — select for the R key"),
    "key.categories.yukicore": ("YukiWorld: магия", "YukiWorld: Magic"),
    "key.yukicore.cast": ("Сотворить заклинание", "Cast spell"),
    "key.yukicore.next_spell": ("Следующее заклинание", "Next spell"),
    "key.yukicore.grimoire": ("Гримуар стихий", "Grimoire of Elements"),
    "menu.yukicore.subtitle": ("Легенда о Юки: Две стихии", "The Legend of Yuki: Two Elements"),
}


def lang():
    ru, en = {}, {}
    for k, (r, e) in LANG.items():
        ru[k], en[k] = r, e
    for sid, (rn, enn, rd, ed) in SPELL_TEXT.items():
        ru[f"spell.yukicore.{sid}"], en[f"spell.yukicore.{sid}"] = rn, enn
        ru[f"spell.yukicore.{sid}.desc"], en[f"spell.yukicore.{sid}.desc"] = rd, ed
    for pid, (rn, enn, rd, ed) in PASSIVE_TEXT.items():
        ru[f"passive.yukicore.{pid}"], en[f"passive.yukicore.{pid}"] = rn, enn
        ru[f"passive.yukicore.{pid}.desc"], en[f"passive.yukicore.{pid}.desc"] = rd, ed
    names = {"cryomancer": ("криоманта", "Cryomancer"), "pyromancer": ("пироманта", "Pyromancer")}
    pieces = {"hood": ("Капюшон", "Hood"), "robe": ("Мантия", "Robe"), "leggings": ("Поножи", "Leggings"),
              "boots": ("Сапоги", "Boots")}
    for s, (rs, es) in names.items():
        for p, (rp, ep) in pieces.items():
            ru[f"item.yukicore.{s}_{p}"] = f"{rp} {rs}"
            en[f"item.yukicore.{s}_{p}"] = f"{es} {ep}"
    suit_ru = {
        "frost": ("Морозный шлем", "Морозный скафандр", "Морозные штаны скафандра", "Морозные сапоги скафандра"),
        "sintara": ("Шлем Синтары", "Скафандр Синтары", "Штаны скафандра Синтары", "Сапоги скафандра Синтары"),
        "star": ("Звёздный шлем", "Звёздный скафандр", "Звёздные штаны скафандра", "Звёздные сапоги скафандра")}
    suit_en = {"frost": "Frost", "sintara": "Sintara's", "star": "Star"}
    for m in suit_ru:
        for i, (p, ep) in enumerate((("helmet", "Space Helmet"), ("suit", "Space Suit"), ("pants", "Space Pants"),
                                     ("boots", "Space Boots"))):
            ru[f"item.yukicore.{m}_space_{p}"] = suit_ru[m][i]
            en[f"item.yukicore.{m}_space_{p}"] = f"{suit_en[m]} {ep}"
    for o in ("frost", "flame", "void", "star", "boulder", "meteor", "icicle", "feather", "blade"):
        ru[f"item.yukicore.orb_{o}"] = en[f"item.yukicore.orb_{o}"] = o
    return ru, en

