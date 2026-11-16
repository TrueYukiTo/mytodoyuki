"""YukiCore 7.0 «Вечная зима»: дерево магии (44 навыка), 3D-броня, лучшая броня сборки, AE2, энергия."""
import math

from PIL import Image, ImageDraw

from . import hd
from .art import STELLAR
from .pixel import ART, sprite
from .recipes import I, altar, shapeless, when
from .v6 import CRYO_CLOTH, FLAME, FROSTP, PYRO_CLOTH

REGALIA_ICE = {"o": 0x0E2448, "d": 0x5A86B4, "m": 0xBFDDF4, "l": 0xE8F6FF, "w": 0xFFFFFF}
REGALIA_FIRE = {"o": 0x3A1404, "d": 0xB07A10, "m": 0xF2C030, "l": 0xFFE68A, "w": 0xFFFFFF}
ETERNAL = {"o": 0x1A2A4A, "d": 0x6A8AC0, "m": 0xD8ECFF, "l": 0xFFF6D0, "w": 0xFFFFFF}

PIECES = (("hood", "helmet"), ("robe", "chestplate"), ("leggings", "leggings"), ("boots", "boots"))
NEW_ARMOR = [f"{s}_{p}" for s in ("yuki_regalia", "sintara_regalia") for p, _ in PIECES] + \
            [f"eternal_{p}" for p in ("helmet", "chestplate", "leggings", "boots")]

TOME = """
................
..ooooooooooo...
..owwwwwwwwwlo..
..owmmmmmmmmlo..
..owmlllllmmlo..
..owmlwwwlmmlo..
..owmlwmwlmmlo..
..owmlwwwlmmlo..
..owmlllllmmlo..
..owmmmmmmmmlo..
..oddddddddddo..
..owwwwwwwwwwo..
..oddddddddddo..
..ooooooooooo...
"""


def item_textures():
    new = {}
    for set_, pal in (("yuki_regalia", REGALIA_ICE), ("sintara_regalia", REGALIA_FIRE)):
        for piece, art in PIECES:
            new[f"{set_}_{piece}"] = (sprite(ART[f"frost_{art}"][0], pal), False, 4)
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        new[f"eternal_{piece}"] = (sprite(ART[f"frost_{piece}"][0], ETERNAL), False, 6)
    new["greater_frost_essence"] = (sprite(ART["yuki_crystal"][0], {**FROSTP, "m": 0xBFEFFF}), False, 5)
    new["greater_flame_essence"] = (sprite(ART["yuki_crystal"][0], {**FLAME, "m": 0xFFE070}), False, 5)
    new["tome_of_winter"] = (sprite(TOME, FROSTP), False, 4)
    new["tome_of_flame"] = (sprite(TOME, FLAME), False, 4)
    return {n: (hd.hd_item(img, seed=700 + i, sparkles=sp), hh) for i, (n, (img, hh, sp)) in enumerate(new.items())}


ARMOR_LAYERS = {"eternal": ETERNAL, "yuki_regalia": REGALIA_ICE, "sintara_regalia": REGALIA_FIRE}


# ------------------------------------------------------------------ иконки 44 навыков (32×32)
W_, B_ = (255, 255, 255, 255), (200, 240, 255, 255)
OR, YE = (255, 140, 40, 255), (255, 230, 120, 255)


def _base(ice, special=False):
    c1, c2 = ((40, 90, 170), (8, 20, 50)) if ice else ((200, 70, 20), (40, 8, 4))
    if special:
        c1, c2 = ((120, 200, 255), (20, 10, 60)) if ice else ((255, 180, 60), (80, 10, 4))
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for r in range(15, 0, -1):
        t = r / 15
        d.ellipse([16 - r, 16 - r, 16 + r, 16 + r], fill=tuple(int(c1[i] * t + c2[i] * (1 - t)) for i in range(3)) + (255,))
    d.ellipse([1, 1, 30, 30], outline=(255, 230, 140, 255) if special else (255, 255, 255, 110), width=1)
    return img, d


def _flake(d, cx, cy, r, col, w=2):
    for k in range(6):
        a = k * math.pi / 3
        d.line([cx, cy, cx + math.cos(a) * r, cy + math.sin(a) * r], fill=col, width=w)
        for s in (-1, 1):
            bx, by = cx + math.cos(a) * r * 0.6, cy + math.sin(a) * r * 0.6
            d.line([bx, by, bx + math.cos(a + s * 0.8) * r * 0.3, by + math.sin(a + s * 0.8) * r * 0.3], fill=col, width=1)


def _flame(d, cx, cy, s, outer=OR, inner=YE):
    d.polygon([(cx, cy - s), (cx + s * 0.6, cy), (cx + s * 0.45, cy + s * 0.6), (cx, cy + s * 0.8),
               (cx - s * 0.45, cy + s * 0.6), (cx - s * 0.6, cy)], fill=outer)
    d.polygon([(cx, cy - s * 0.4), (cx + s * 0.3, cy + s * 0.2), (cx, cy + s * 0.6), (cx - s * 0.3, cy + s * 0.2)], fill=inner)


def _shard(d, x0, y0, x1, y1, col, w=3):
    d.polygon([(x0, y0), (x1, y1), (x1 - w, y1 + w * 0.5), (x0 + w * 0.3, y0 + w)], fill=col)


def skill_icons():
    icons = {}

    def ico(name, ice, fn, special=False):
        img, d = _base(ice, special)
        fn(d)
        icons[name] = img

    # ---------------- ❄ Юки
    ico("frost_bolt", True, lambda d: (d.polygon([(6, 26), (22, 6), (26, 10), (10, 28)], fill=B_),
                                        d.polygon([(22, 6), (28, 4), (26, 10)], fill=W_)))
    ico("ice_shards", True, lambda d: [_shard(d, 6, 24 - i * 3, 18 + i * 2, 8 + i * 2, B_ if i % 2 else W_, 2) for i in range(4)])
    ico("frost_nova", True, lambda d: (_flake(d, 16, 16, 12, W_), d.ellipse([12, 12, 20, 20], fill=B_)))
    ico("cold_mind", True, lambda d: (d.ellipse([9, 7, 23, 21], outline=W_, width=2), _flake(d, 16, 14, 4, B_, 1),
                                       d.rectangle([12, 21, 20, 25], fill=B_)))
    ico("ice_spikes", True, lambda d: [d.polygon([(5 + i * 6, 27), (8 + i * 6, 8 + (i % 2) * 6), (11 + i * 6, 27)],
                                                  fill=W_ if i % 2 else B_) for i in range(4)])
    ico("glacial_armor", True, lambda d: (d.polygon([(16, 4), (27, 9), (25, 20), (16, 28), (7, 20), (5, 9)],
                                                    fill=(140, 210, 255, 255), outline=W_), _flake(d, 16, 15, 6, W_, 1)))
    ico("frostbite", True, lambda d: (d.polygon([(10, 8), (22, 8), (20, 24), (12, 24)], fill=(170, 220, 255, 255)),
                                       [d.line([10 + i * 3, 12, 12 + i * 3, 20], fill=(30, 60, 120, 255)) for i in range(4)]))
    ico("ice_wall", True, lambda d: [d.rectangle([5 + i * 6, 9, 10 + i * 6, 26], fill=W_ if i % 2 else B_,
                                                  outline=(40, 90, 170, 255)) for i in range(4)])
    ico("frost_blink", True, lambda d: ([d.arc([5 + k * 2, 5 + k * 2, 27 - k * 2, 27 - k * 2], 90 + k * 60, 290 + k * 60,
                                                fill=(200, 240, 255, 255 - k * 50), width=2) for k in range(3)],
                                        d.polygon([(22, 8), (27, 13), (20, 14)], fill=W_)))
    ico("permafrost", True, lambda d: (d.polygon([(16, 3), (24, 14), (16, 29), (8, 14)], fill=(170, 230, 255, 255), outline=W_),
                                        d.line([16, 3, 16, 29], fill=W_)))
    ico("glacier_crash", True, lambda d: (d.polygon([(8, 4), (24, 4), (27, 18), (5, 18)], fill=W_, outline=(40, 90, 170, 255)),
                                           [d.line([4 + i * 6, 22, 2 + i * 6, 28], fill=B_, width=2) for i in range(5)]))
    ico("blizzard", True, lambda d: (d.ellipse([5, 6, 19, 16], fill=(220, 235, 250, 255)),
                                      d.ellipse([12, 4, 27, 16], fill=(235, 245, 255, 255)),
                                      [_flake(d, x, y, 2.5, W_, 1) for x, y in ((8, 20), (14, 24), (20, 20), (24, 26), (11, 27))]))
    ico("winter_heart", True, lambda d: (d.polygon([(16, 27), (5, 15), (8, 7), (16, 11), (24, 7), (27, 15)],
                                                   fill=(120, 200, 255, 255), outline=W_), _flake(d, 16, 15, 4, W_, 1)))
    ico("ice_prison", True, lambda d: (d.rectangle([8, 6, 24, 27], fill=(170, 230, 255, 200), outline=W_, width=2),
                                        d.ellipse([12, 9, 20, 17], fill=(40, 60, 100, 255)),
                                        d.rectangle([12, 17, 20, 25], fill=(40, 60, 100, 255))))
    ico("frozen_orb", True, lambda d: (d.ellipse([9, 9, 23, 23], fill=B_, outline=W_),
                                        [d.line([16, 16, 16 + math.cos(k * math.pi / 3) * 14, 16 + math.sin(k * math.pi / 3) * 14],
                                                fill=W_, width=1) for k in range(6)]))
    ico("shatter", True, lambda d: [d.polygon([(16, 16), (16 + math.cos(k) * 13, 16 + math.sin(k) * 13),
                                               (16 + math.cos(k + 0.5) * 9, 16 + math.sin(k + 0.5) * 9)],
                                              fill=W_ if i % 2 else B_) for i, k in enumerate([0, 1.2, 2.4, 3.6, 4.8])])
    ico("ice_golem", True, lambda d: (d.rectangle([11, 5, 21, 13], fill=B_), d.rectangle([8, 13, 24, 23], fill=W_),
                                       d.rectangle([5, 13, 8, 22], fill=B_), d.rectangle([24, 13, 27, 22], fill=B_),
                                       d.rectangle([10, 23, 14, 28], fill=B_), d.rectangle([18, 23, 22, 28], fill=B_),
                                       d.rectangle([13, 8, 15, 10], fill=(40, 90, 170, 255)),
                                       d.rectangle([17, 8, 19, 10], fill=(40, 90, 170, 255))))
    ico("avalanche", True, lambda d: [d.polygon([(4 + i * 2, 26), (14 + i * 4, 10 + i * 4), (28, 26)],
                                                 fill=(255, 255, 255, 255 - i * 60)) for i in range(3)])
    ico("eternal_cold", True, lambda d: (d.ellipse([6, 6, 26, 26], outline=W_, width=2),
                                          d.line([16, 16, 16, 9], fill=W_, width=2), d.line([16, 16, 21, 18], fill=B_, width=2),
                                          _flake(d, 16, 16, 3, B_, 1)))
    ico("polar_storm", True, lambda d: ([d.arc([4 + k * 3, 4 + k * 3, 28 - k * 3, 28 - k * 3], k * 50, k * 50 + 240, fill=W_, width=2)
                                         for k in range(4)], d.polygon([(15, 6), (19, 15), (16, 15), (18, 26), (12, 14), (15, 14)],
                                                                         fill=(255, 255, 160, 255))))
    ico("absolute_zero", True, lambda d: (_flake(d, 16, 16, 13, W_, 2), d.ellipse([3, 3, 29, 29], outline=YE, width=2),
                                           d.ellipse([13, 13, 19, 19], fill=YE)), True)
    ico("yuki_avatar", True, lambda d: ([d.polygon([(16, 16), (16 + s * 14, 6), (16 + s * 12, 14), (16 + s * 14, 20)],
                                                    fill=(220, 245, 255, 230)) for s in (-1, 1)],
                                        d.ellipse([12, 6, 20, 14], fill=W_), d.rectangle([13, 14, 19, 26], fill=B_),
                                        d.polygon([(12, 5), (14, 1), (16, 4), (18, 1), (20, 5)], fill=YE)), True)

    # ---------------- ✹ Синтара
    ico("ember_bolt", False, lambda d: (d.polygon([(4, 28), (14, 14), (18, 18)], fill=(255, 120, 30, 180)),
                                         d.ellipse([13, 6, 27, 20], fill=OR), d.ellipse([17, 9, 24, 16], fill=YE)))
    ico("fire_spray", False, lambda d: (d.polygon([(4, 16), (28, 5), (28, 27)], fill=(255, 110, 30, 220)),
                                         d.polygon([(4, 16), (24, 10), (24, 22)], fill=YE)))
    ico("flame_ring", False, lambda d: ([_flame(d, 16 + math.cos(k * math.pi / 4) * 10, 16 + math.sin(k * math.pi / 4) * 10, 5)
                                         for k in range(8)], d.ellipse([12, 12, 20, 20], fill=(255, 200, 80, 255))))
    ico("burning_will", False, lambda d: (_flame(d, 16, 15, 11), d.ellipse([13, 18, 19, 24], fill=(255, 255, 220, 255))))
    ico("fire_pillars", False, lambda d: [d.rectangle([5 + i * 8, 6 + (i % 2) * 6, 10 + i * 8, 27], fill=OR if i % 2 else YE)
                                          for i in range(3)])
    ico("phoenix_wings", False, lambda d: ([d.polygon([(16, 18), (16 + s * (6 + k * 3), 8 + k * 3), (16 + s * (4 + k * 3), 14 + k * 3)],
                                                       fill=(255, 150 - k * 20, 40, 255)) for s in (-1, 1) for k in range(4)],
                                           _flame(d, 16, 20, 7)))
    ico("ignite", False, lambda d: (d.ellipse([7, 7, 25, 25], outline=YE, width=2), _flame(d, 16, 17, 8)))
    ico("lava_pool", False, lambda d: (d.ellipse([3, 15, 29, 27], fill=(255, 100, 20, 255)), d.ellipse([8, 17, 24, 24], fill=YE),
                                        [d.ellipse([x, y, x + 3, y + 3], fill=OR) for x, y in ((9, 9), (18, 6), (22, 11))]))
    ico("flame_dash", False, lambda d: [d.polygon([(6 + k * 7, 8), (14 + k * 7, 16), (6 + k * 7, 24), (9 + k * 7, 16)],
                                                  fill=(255, 160 + k * 30, 40 + k * 30, 255)) for k in range(3)])
    ico("sintara_heat", False, lambda d: ([d.line([16, 16, 16 + math.cos(k * math.pi / 4) * 13, 16 + math.sin(k * math.pi / 4) * 13],
                                                  fill=OR, width=2) for k in range(8)], d.ellipse([9, 9, 23, 23], fill=YE)))
    ico("fireball_barrage", False, lambda d: [d.ellipse([x, y, x + 7, y + 7], fill=OR, outline=YE)
                                              for x, y in ((4, 18), (11, 10), (19, 4), (18, 18), (10, 22))])
    ico("meteor", False, lambda d: (d.polygon([(4, 4), (16, 14), (12, 18)], fill=(255, 120, 30, 200)),
                                     d.ellipse([12, 12, 27, 27], fill=(120, 50, 30, 255)), d.ellipse([14, 14, 22, 22], fill=OR),
                                     d.ellipse([17, 17, 20, 20], fill=YE)))
    ico("inner_fire", False, lambda d: (d.polygon([(16, 27), (5, 15), (8, 7), (16, 11), (24, 7), (27, 15)], fill=(220, 60, 40, 255)),
                                         _flame(d, 16, 15, 6)))
    ico("dragon_breath", False, lambda d: (d.polygon([(4, 10), (12, 6), (14, 14), (6, 18)], fill=(120, 30, 20, 255)),
                                            d.polygon([(12, 12), (29, 4), (29, 26)], fill=(255, 120, 30, 230)),
                                            d.polygon([(12, 12), (25, 9), (25, 19)], fill=YE)))
    ico("flame_tornado", False, lambda d: [d.ellipse([12 - k * 2, 24 - k * 4, 20 + k * 2, 28 - k * 4], outline=OR if k % 2 else YE,
                                                     width=2) for k in range(6)])
    ico("combustion", False, lambda d: ([d.line([16, 16, 16 + math.cos(k) * 13, 16 + math.sin(k) * 13], fill=YE, width=2)
                                         for k in [0.3 + i * 0.9 for i in range(7)]], d.ellipse([11, 11, 21, 21], fill=OR)))
    ico("fire_spirits", False, lambda d: [(_flame(d, x, y, 6), d.ellipse([x - 2, y - 1, x - 1, y], fill=(40, 10, 0, 255)),
                                           d.ellipse([x + 1, y - 1, x + 2, y], fill=(40, 10, 0, 255)))
                                          for x, y in ((9, 13), (23, 13), (16, 22))])
    ico("meteor_shower", False, lambda d: [(d.line([x - 6, y - 6, x, y], fill=(255, 120, 30, 220), width=2),
                                            d.ellipse([x - 2, y - 2, x + 3, y + 3], fill=YE))
                                           for x, y in ((12, 10), (24, 9), (18, 18), (10, 24), (26, 24))])
    ico("phoenix_soul", False, lambda d: (d.ellipse([6, 6, 26, 26], outline=YE, width=2), d.line([16, 16, 16, 9], fill=YE, width=2),
                                           d.line([16, 16, 21, 18], fill=OR, width=2)))
    ico("sun_strike", False, lambda d: (d.rectangle([12, 2, 20, 26], fill=(255, 250, 200, 255)), d.ellipse([4, 22, 28, 30], fill=OR),
                                         d.rectangle([14, 2, 18, 26], fill=W_)))
    ico("supernova", False, lambda d: ([d.line([16, 16, 16 + math.cos(k * math.pi / 6) * (14 if k % 2 == 0 else 9),
                                                16 + math.sin(k * math.pi / 6) * (14 if k % 2 == 0 else 9)], fill=YE, width=2)
                                        for k in range(12)], d.ellipse([10, 10, 22, 22], fill=(255, 250, 220, 255))), True)
    ico("sintara_avatar", False, lambda d: ([d.polygon([(16, 16), (16 + s * 14, 5), (16 + s * 12, 14), (16 + s * 14, 21)],
                                                       fill=(255, 140, 30, 240)) for s in (-1, 1)],
                                            d.ellipse([12, 6, 20, 14], fill=YE), d.rectangle([13, 14, 19, 26], fill=OR),
                                            _flame(d, 16, 4, 4)), True)
    return icons


def effect_icons(icons):
    def small(n):
        return icons[n].resize((18, 18), Image.LANCZOS)
    return {"frozen": small("absolute_zero"), "frost_armor": small("glacial_armor"), "phoenix": small("phoenix_wings"),
            "rebirth": small("supernova"), "frostbite": small("frostbite"), "avatar_ice": small("yuki_avatar"),
            "avatar_fire": small("sintara_avatar")}


# ------------------------------------------------------------------ тексты навыков (ru-имя, en-имя, ru, en)
SKILLS = {
    "frost_bolt": ("Ледяная стрела", "Frost Bolt", "Стрела льда: 25 урона и замедление. Ранг 3 — три стрелы, ранг 5 — пять.",
                   "An ice arrow: 25 damage and slow. Rank 3 — three arrows, rank 5 — five."),
    "ice_shards": ("Ледяные осколки", "Ice Shards", "Залп из 11–19 сосулек веером на близкой дистанции, по 12 урона.",
                   "A short-range fan of 11–19 icicles, 12 damage each."),
    "frost_nova": ("Ледяная нова", "Frost Nova", "Взрыв холода радиусом 7–11: 30 урона, заморозка, ледяные шипы вокруг.",
                   "A cold blast of radius 7–11: 30 damage, freeze, ice spikes around you."),
    "cold_mind": ("Холодный разум", "Cold Mind", "+30 к запасу маны за ранг.", "+30 max mana per rank."),
    "ice_spikes": ("Ледяные пики", "Ice Spikes", "Дорожка из 16–24 ледяных пик вперёд: 45 урона и подброс.",
                   "A trail of 16–24 ice spikes: 45 damage and launch."),
    "glacial_armor": ("Ледяной панцирь", "Glacial Armor", "Поглощение, сопротивление; атакующие мёрзнут и получают урон.",
                      "Absorption and resistance; attackers freeze and take damage."),
    "frostbite": ("Обморожение", "Frostbite", "Ледяные удары вешают Обморожение: враг получает на 6% больше любого урона за ступень.",
                  "Ice hits apply Frostbite: +6% damage taken from everything per stack."),
    "ice_wall": ("Ледяная стена", "Ice Wall", "Стена из льда шириной 10 блоков на 9–13 с: ранит и отбрасывает врагов.",
                 "A 10-block ice wall for 9–13 s that hurts and pushes enemies away."),
    "frost_blink": ("Ледяной шаг", "Frost Blink", "Телепорт на 17–29 блоков по взгляду, волны холода в начале и конце.",
                    "Teleport 17–29 blocks, with cold bursts at both ends."),
    "permafrost": ("Вечная мерзлота", "Permafrost", "+10% к силе ледяной магии за ранг.", "+10% ice power per rank."),
    "glacier_crash": ("Удар ледника", "Glacier Crash", "Гигантская ледяная глыба падает в точку взгляда: 180 урона, тряска земли.",
                      "A giant ice block crashes down: 180 damage and a quake."),
    "blizzard": ("Метель", "Blizzard", "Буря радиусом 9 на 10–18 с: урон, замедление и падающие сосульки.",
                 "A radius-9 storm for 10–18 s: damage, slow and falling icicles."),
    "winter_heart": ("Сердце зимы", "Heart of Winter", "+1 маны в секунду за ранг.", "+1 mana per second per rank."),
    "ice_prison": ("Ледяная тюрьма", "Ice Prison", "Заточает цель в лёд на 3 с, затем лёд взрывается: 250 урона и осколки.",
                   "Encases the target for 3 s, then shatters: 250 damage and shards."),
    "frozen_orb": ("Морозная сфера", "Frozen Orb", "Медленная сфера пускает сосульки во все стороны и взрывается: 90 урона.",
                   "A slow orb spraying icicles in all directions, then exploding for 90."),
    "shatter": ("Раскол", "Shatter", "Замороженные враги получают +15% урона от льда за ранг.",
                "Frozen enemies take +15% ice damage per rank."),
    "ice_golem": ("Ледяной страж", "Ice Guardian", "Призывает 1–2 ледяных голема на 70–110 с. Они бьются за тебя.",
                  "Summons 1–2 ice golems for 70–110 s to fight for you."),
    "avalanche": ("Лавина", "Avalanche", "Стена снега несётся на 28 блоков вперёд: 120 урона, отброс и заморозка.",
                  "A wall of snow rushes 28 blocks ahead: 120 damage, knockback and freeze."),
    "eternal_cold": ("Вечный холод", "Eternal Cold", "−6% к перезарядке ледяной магии за ранг.", "−6% ice cooldowns per rank."),
    "polar_storm": ("Полярная буря", "Polar Storm", "13–17 с вокруг тебя бьют ледяные молнии: до 3 ударов по 60 каждые полсекунды.",
                    "For 13–17 s ice lightning strikes up to 3 enemies for 60 every half second."),
    "absolute_zero": ("Абсолютный ноль", "Absolute Zero", "Ультимейт. 600 урона и заморозка всего в 24 блоках, кольца ледяных пик.",
                      "Ultimate. 600 damage and freeze within 24 blocks, rings of ice spikes."),
    "yuki_avatar": ("Аватар Юки", "Avatar of Yuki", "Ультимейт. 30–50 с: полёт, ледяная аура, +50% силы, −75% маны, −50% перезарядки.",
                    "Ultimate. 30–50 s: flight, freezing aura, +50% power, −75% cost, −50% cooldowns."),
    "ember_bolt": ("Огненный шар Синтары", "Sintara's Ember", "Взрывающийся шар: 25 урона. Ранг 3 — очередь из трёх, ранг 5 — из пяти.",
                   "An exploding fireball: 25 damage. Rank 3 — a burst of three, rank 5 — five."),
    "fire_spray": ("Огненный поток", "Fire Spray", "2–3 с огнемёта: конус 11 блоков, урон каждые 2 тика.",
                   "2–3 s flamethrower: an 11-block cone hitting every 2 ticks."),
    "flame_ring": ("Огненное кольцо", "Flame Ring", "Кольцо пламени радиусом 7–11: 30 урона, поджог, отброс.",
                   "A ring of fire of radius 7–11: 30 damage, burning, knockback."),
    "burning_will": ("Воля пламени", "Will of Flame", "+1 маны в секунду за ранг.", "+1 mana per second per rank."),
    "fire_pillars": ("Огненные столпы", "Fire Pillars", "Из-под 5–9 врагов бьют огненные столпы: 70 урона и подброс.",
                     "Fire pillars erupt under 5–9 enemies: 70 damage and launch."),
    "phoenix_wings": ("Крылья феникса", "Phoenix Wings", "15–27 с полёта, регенерация, скорость и +30% к огню.",
                      "15–27 s of flight, regeneration, speed and +30% fire power."),
    "ignite": ("Жгучее пламя", "Searing Flame", "Горящие враги получают +8% огненного урона за ранг, поджог дольше.",
               "Burning enemies take +8% fire damage per rank; burns last longer."),
    "lava_pool": ("Лавовое озеро", "Lava Pool", "Озеро лавы в точке взгляда на 9–13 с: урон каждые полсекунды.",
                  "A lava pool for 9–13 s that burns every half second."),
    "flame_dash": ("Огненный рывок", "Flame Dash", "Рывок сквозь врагов (50 урона) и взрыв в конце (60).",
                   "Dash through enemies (50 damage) ending in an explosion (60)."),
    "sintara_heat": ("Жар Синтары", "Sintara's Heat", "+10% к силе огненной магии за ранг.", "+10% fire power per rank."),
    "fireball_barrage": ("Шквал огня", "Fireball Barrage", "Шквал из 18–26 огненных шаров по прицелу, по 30 урона.",
                         "A barrage of 18–26 fireballs, 30 damage each."),
    "meteor": ("Метеор", "Meteor", "Огромный метеор: 220 урона в радиусе 5–7. Ранг 5 — три метеора.",
               "A huge meteor: 220 damage in a 5–7 radius. Rank 5 — three meteors."),
    "inner_fire": ("Внутренний огонь", "Inner Fire", "+4 ❤ здоровья и +20 маны за ранг.", "+4 ❤ health and +20 mana per rank."),
    "dragon_breath": ("Дыхание дракона", "Dragon Breath", "4 с луча пламени длиной 22 блока: урон каждые 2 тика.",
                      "4 s of a 22-block flame beam hitting every 2 ticks."),
    "flame_tornado": ("Огненные смерчи", "Flame Tornadoes", "Три смерча идут вперёд, затягивая и сжигая врагов.",
                      "Three tornadoes march forward, pulling in and burning enemies."),
    "combustion": ("Самовозгорание", "Combustion", "Враги, убитые огнём, взрываются и ранят соседей.",
                   "Enemies killed by fire explode, hurting those nearby."),
    "fire_spirits": ("Огненные духи", "Fire Spirits", "Призывает 3–4 огненных духов на 70–110 с.",
                     "Summons 3–4 fire spirits for 70–110 s."),
    "meteor_shower": ("Метеоритный дождь", "Meteor Shower", "18–26 метеоров падают в радиусе 12 от точки взгляда, по 120 урона.",
                      "18–26 meteors rain within 12 blocks of the target, 120 damage each."),
    "phoenix_soul": ("Душа феникса", "Phoenix Soul", "−6% к перезарядке огненной магии за ранг.", "−6% fire cooldowns per rank."),
    "sun_strike": ("Солнечный удар", "Sun Strike", "Столп солнца с неба через 1,5 с: 500 урона в радиусе 8.",
                   "A pillar of sunlight after 1.5 s: 500 damage within 8 blocks."),
    "supernova": ("Сверхновая", "Supernova", "Ультимейт. 700 урона в 24 блоках и минута Возрождения.",
                  "Ultimate. 700 damage within 24 blocks and a minute of Rebirth."),
    "sintara_avatar": ("Аватар Синтары", "Avatar of Sintara",
                       "Ультимейт. 30–50 с формы феникса: полёт, огненная аура, +50% силы, −75% маны, −50% перезарядки.",
                       "Ultimate. 30–50 s phoenix form: flight, fire aura, +50% power, −75% cost, −50% cooldowns."),
}

LANG = {
    "screen.yukicore.grimoire.slots": ("Ячейки: ПКМ по заклинанию — в выбранную ячейку", "Slots: RMB a spell to put it here"),
    "screen.yukicore.grimoire.hint": ("ЛКМ — изучить/улучшить  •  ПКМ — в ячейку  •  R — сотворить  •  Z — след. ячейка",
                                      "LMB — learn/upgrade  •  RMB — to slot  •  R — cast  •  Z — next slot"),
    "screen.yukicore.grimoire.need_parent": ("Сначала изучи: %s", "Learn first: %s"),
    "screen.yukicore.grimoire.kind.active": ("Заклинание", "Spell"),
    "screen.yukicore.grimoire.kind.passive": ("Пассивный талант", "Passive talent"),
    "screen.yukicore.grimoire.kind.ultimate": ("★ Ультимейт", "★ Ultimate"),
    "screen.yukicore.grimoire.click_select": ("ПКМ — положить в выбранную ячейку", "RMB — put into the selected slot"),
    "magic.yukicore.no_target": ("Нет цели", "No target"),
    "magic.yukicore.no_spell": ("Ячейка пуста — открой Гримуар (K)", "Empty slot — open the Grimoire (K)"),
    "message.yukicore.knowledge_max": ("Больше томов знаний не прочесть", "You can't learn from more tomes"),
    "tooltip.yukicore.armor_protection": ("−%s%% любого урона, +%s ❤", "−%s%% all damage, +%s ❤"),
    "tooltip.yukicore.set.frost": ("Комплект: −15% урона, скорость, сила", "Set: −15% damage, speed, strength"),
    "tooltip.yukicore.set.aurorium": ("Комплект: −25% урона, сила II, регенерация", "Set: −25% damage, strength II, regen"),
    "tooltip.yukicore.set.celestium": ("Комплект: −42% урона, сила III, регенерация II, ночное зрение",
                                       "Set: −42% damage, strength III, regen II, night vision"),
    "tooltip.yukicore.set.yuki": ("Венец: −15% урона, +20 ❤ и все благословения", "Crown: −15% damage, +20 ❤ and all blessings"),
    "tooltip.yukicore.set.eternal": ("Комплект: −75% урона, полёт, иммунитет к огню, холоду, иссушению; сила IV",
                                     "Set: −75% damage, flight, immunity to fire, cold, wither; strength IV"),
    "tooltip.yukicore.set.cryomancer": ("Комплект: −12% урона, +15% силы льда", "Set: −12% damage, +15% ice power"),
    "tooltip.yukicore.set.pyromancer": ("Комплект: −12% урона, +15% силы огня", "Set: −12% damage, +15% fire power"),
    "tooltip.yukicore.set.yuki_regalia": ("Комплект: −50% урона, регенерация II, +15% силы льда сверху",
                                          "Set: −50% damage, regen II, extra +15% ice power"),
    "tooltip.yukicore.set.sintara_regalia": ("Комплект: −50% урона, регенерация II, +15% силы огня сверху",
                                             "Set: −50% damage, regen II, extra +15% fire power"),
    "tooltip.yukicore.robe.yuki": ("❄ +%s%% к силе льда, −%s%% маны, +%s к запасу", "❄ +%s%% ice power, −%s%% cost, +%s mana"),
    "tooltip.yukicore.robe.sintara": ("✹ +%s%% к силе огня, −%s%% маны, +%s к запасу", "✹ +%s%% fire power, −%s%% cost, +%s mana"),
    "tooltip.yukicore.robe.both": ("+%s%% к силе, −%s%% маны, +%s к запасу", "+%s%% power, −%s%% cost, +%s mana"),
    "entity.yukicore.ally.ice_golem": ("Ледяной страж Юки", "Yuki's Ice Guardian"),
    "entity.yukicore.ally.fire_spirit": ("Огненный дух Синтары", "Sintara's Fire Spirit"),
    "effect.yukicore.frostbite": ("Обморожение", "Frostbite"),
    "effect.yukicore.avatar_ice": ("Аватар Юки", "Avatar of Yuki"),
    "effect.yukicore.avatar_fire": ("Аватар Синтары", "Avatar of Sintara"),
    "item.yukicore.greater_frost_essence": ("Великая эссенция льда", "Greater Frost Essence"),
    "item.yukicore.greater_frost_essence.desc": ("Использовать: +3000 опыта магии Юки", "Use: +3000 Yuki magic XP"),
    "item.yukicore.greater_flame_essence": ("Великая эссенция пламени", "Greater Flame Essence"),
    "item.yukicore.greater_flame_essence.desc": ("Использовать: +3000 опыта магии Синтары", "Use: +3000 Sintara magic XP"),
    "item.yukicore.tome_of_winter": ("Том зимы", "Tome of Winter"),
    "item.yukicore.tome_of_winter.desc": ("Использовать: +1 очко навыков Юки (до 10)", "Use: +1 Yuki skill point (up to 10)"),
    "item.yukicore.tome_of_flame": ("Том пламени", "Tome of Flame"),
    "item.yukicore.tome_of_flame.desc": ("Использовать: +1 очко навыков Синтары (до 10)", "Use: +1 Sintara skill point (up to 10)"),
    "item.yukicore.eternal_helmet": ("Шлем Вечной зимы", "Eternal Winter Helmet"),
    "item.yukicore.eternal_chestplate": ("Кираса Вечной зимы", "Eternal Winter Chestplate"),
    "item.yukicore.eternal_leggings": ("Поножи Вечной зимы", "Eternal Winter Leggings"),
    "item.yukicore.eternal_boots": ("Сапоги Вечной зимы", "Eternal Winter Boots"),
    "block.yukicore.star_reactor.desc": ("Контроллер мультиструктуры 3×3×3. Сжигает осколки, до 10 млн FE/т",
                                         "3×3×3 multiblock controller. Burns shards, up to 10M FE/t"),
    "menu.yukicore.subtitle": ("Легенда о Юки: Вечная зима", "The Legend of Yuki: Eternal Winter"),
    "event.yukicore.yukia": ("❄ Метель Юкии", "❄ Yukian Blizzard"),
    "event.yukicore.yukia.desc": ("Холод сковывает — но с неба падают кристаллы Юки", "Freezing cold — but Yuki crystals fall from the sky"),
    "event.yukicore.aurora": ("✦ Северное сияние", "✦ Aurora Borealis"),
    "event.yukicore.aurora.desc": ("Сияние лечит, дарит ману и осколки авроры", "The aurora heals, gives mana and aurora shards"),
    "event.yukicore.pyrolith": ("☄ Метеоритный дождь", "☄ Meteor Shower"),
    "event.yukicore.pyrolith.desc": ("Уходи из огненных кругов! После ударов остаётся пиролит", "Leave the fiery circles! Impacts leave pyrolite"),
    "event.yukicore.nyx": ("◉ Пустотный разлом", "◉ Void Rift"),
    "event.yukicore.nyx.desc": ("Тьма и эндермиты — и осколки пустоты с неба", "Darkness and endermites — and void shards from the sky"),
    "event.yukicore.celestia": ("★ Звездопад", "★ Starfall"),
    "event.yukicore.celestia.desc": ("Собирай падающие звёздные осколки", "Collect the falling stellar shards"),
    "event.yukicore.end": ("Явление на планете закончилось", "The planetary event has ended"),
    "screen.yukicore.grimoire.level": ("Уровень %s / 60  •  Очки: %s", "Level %s / 60  •  Points: %s"),
}


def lang():
    ru, en = {}, {}
    for k, (r, e) in LANG.items():
        ru[k], en[k] = r, e
    for sid, (rn, enn, rd, ed) in SKILLS.items():
        ru[f"spell.yukicore.{sid}"], en[f"spell.yukicore.{sid}"] = rn, enn
        ru[f"spell.yukicore.{sid}.desc"], en[f"spell.yukicore.{sid}.desc"] = rd, ed
    names = {"yuki_regalia": ("Юки", "Yuki's"), "sintara_regalia": ("Синтары", "Sintara's")}
    pieces = {"hood": ("Венец", "Diadem"), "robe": ("Регалия", "Regalia"), "leggings": ("Поножи", "Leggings"),
              "boots": ("Сапоги", "Boots")}
    for s, (rs, es) in names.items():
        for p, (rp, ep) in pieces.items():
            ru[f"item.yukicore.{s}_{p}"] = f"{rp} {rs}"
            en[f"item.yukicore.{s}_{p}"] = f"{es} {ep}"
    return ru, en


# ------------------------------------------------------------------ рецепты
def recipes():
    r = {}
    trophies = [I("colossus_heart"), I("blizzard_crown"), I("ash_core"), I("void_scale")]
    for i, piece in enumerate(("helmet", "chestplate", "leggings", "boots")):
        r[f"altar/eternal_{piece}"] = altar(3, I(f"celestium_{piece}"), [I("yuki_tear"), trophies[i], I("stellar_alloy"),
                                                                         I("stellar_alloy"), I("starlight_block"),
                                                                         I("cryosteel_block"), I("star_shard"),
                                                                         I("star_shard")], I(f"eternal_{piece}"), 1, 600)
    for set_, robe, trophy, ess in (("yuki_regalia", "cryomancer", I("archon_heart"), I("greater_frost_essence")),
                                    ("sintara_regalia", "pyromancer", I("phoenix_feather"), I("greater_flame_essence"))):
        for piece, _ in PIECES:
            r[f"altar/{set_}_{piece}"] = altar(3, I(f"{robe}_{piece}"), [trophy, ess, ess, I("mana_crystal"),
                                                                       I("celestium_ingot"), I("celestium_ingot"),
                                                                       I("aurorium_ingot"), I("aurorium_ingot")],
                                               I(f"{set_}_{piece}"), 1, 500)
    r["altar/greater_frost_essence"] = altar(1, I("frost_essence"), [I("frost_essence")] * 4, I("greater_frost_essence"), 2, 120)
    r["altar/greater_flame_essence"] = altar(1, I("flame_essence"), [I("flame_essence")] * 4, I("greater_flame_essence"), 2, 120)
    r["altar/tome_of_winter"] = altar(2, "minecraft:book", [I("greater_frost_essence")] * 4 + [I("mana_crystal"),
                                                                                              I("yuki_crystal"), I("yuki_crystal"),
                                                                                              "minecraft:blue_ice"],
                                      I("tome_of_winter"), 1, 300)
    r["altar/tome_of_flame"] = altar(2, "minecraft:book", [I("greater_flame_essence")] * 4 + [I("mana_crystal"),
                                                                                             I("yuki_crystal"), I("yuki_crystal"),
                                                                                             "minecraft:blaze_rod"],
                                     I("tome_of_flame"), 1, 300)
    # AE2 в Морозной кузне: дешевле, но за энергию
    from .recipes import forge
    r["forge/ae2_fluix"] = when(forge([("ae2:certus_quartz_crystal", 2), ("minecraft:redstone", 1), (I("yuki_crystal"), 1)],
                                      "ae2:fluix_crystal", 6, 400_000, 100), "ae2")
    r["forge/ae2_engineering"] = when(forge([("minecraft:diamond", 1), ("ae2:silicon", 1), ("minecraft:redstone", 2),
                                             (I("cryo_circuit"), 1)], "ae2:engineering_processor", 6, 1_000_000, 120), "ae2")
    r["forge/ae2_logic"] = when(forge([("minecraft:gold_ingot", 1), ("ae2:silicon", 1), ("minecraft:redstone", 2),
                                       (I("cryo_circuit"), 1)], "ae2:logic_processor", 6, 1_000_000, 120), "ae2")
    r["forge/ae2_calculation"] = when(forge([("ae2:certus_quartz_crystal", 1), ("ae2:silicon", 1), ("minecraft:redstone", 2),
                                             (I("cryo_circuit"), 1)], "ae2:calculation_processor", 6, 1_000_000, 120), "ae2")
    r["forge/ae2_singularity"] = when(forge([("ae2:matter_ball", 16), (I("stellar_shard"), 1)], "ae2:singularity", 2,
                                            20_000_000, 200), "ae2")
    # Create 6 убрал рецепты «наследия» — возвращаем их, иначе квесты и Сердце Юки не собрать
    r["create_legacy/chromatic_compound"] = when({
        "type": "create:mixing", "heat_requirement": "superheated",
        "ingredients": [{"item": "create:powdered_obsidian"}] * 3 + [{"item": "minecraft:glowstone_dust"}] * 3
                       + [{"item": "create:polished_rose_quartz"}],
        "results": [{"id": "create:chromatic_compound"}]}, "create")
    r["forge/refined_radiance"] = when(forge([("create:chromatic_compound", 1), (I("yuki_crystal"), 1),
                                              ("minecraft:glowstone_dust", 4)], "create:refined_radiance", 1,
                                             2_000_000, 160), "create")
    r["forge/shadow_steel"] = when(forge([("create:chromatic_compound", 1), ("minecraft:obsidian", 1),
                                          ("minecraft:ender_pearl", 1)], "create:shadow_steel", 1, 2_000_000, 160), "create")
    r["respec_scroll"] = shapeless(["minecraft:paper", I("frost_essence"), I("flame_essence"), "minecraft:amethyst_shard",
                                    "minecraft:ender_pearl"], I("respec_scroll"))
    return r


BOSS_EXTRA = {
    "glacial_archon": [("tome_of_winter", 1, 1), ("greater_frost_essence", 3, 6)],
    "infernal_phoenix": [("tome_of_flame", 1, 1), ("greater_flame_essence", 3, 6)],
    "frost_colossus": [("greater_frost_essence", 2, 4)],
    "blizzard_queen": [("greater_frost_essence", 3, 5)],
    "ash_lord": [("greater_flame_essence", 3, 5)],
    "void_wyrm": [("greater_frost_essence", 2, 4), ("greater_flame_essence", 2, 4)],
    "shadow_of_yuki": [("tome_of_winter", 1, 1), ("tome_of_flame", 1, 1)],
}


# ------------------------------------------------------------------ книга
ENTRIES = [
    ("magic", "skill_tree", "Дерево навыков", "yukicore:tome_of_winter", [
        "У каждой школы теперь $(l)22 навыка$(): 16 заклинаний и 6 пассивных талантов, 9 ступеней. Навык открывается, "
        "только когда изучены все его предки, — качай дерево по очереди.",
        "Уровни школ — до $(l)60$(). Очки: 1 за уровень и ещё 1 за каждый 5-й. Тома зимы и пламени дают +1 очко "
        "(до 10). Всё дерево на 5 ранги не прокачать — выбирай путь.",
        "$(l)Ячейки$(): 6 быстрых ячеек внизу экрана. ПКМ по заклинанию в Гримуаре кладёт его в выбранную ячейку, "
        "$(l)R$() — сотворить, $(l)Z$() — следующая ячейка.",
        "Ультимейты: $(#8fd8ff)Абсолютный ноль$() и $(#ffa040)Сверхновая$() (после Испытания), затем на 50 уровне — "
        "$(l)Аватар Юки$() и $(l)Аватар Синтары$(): полёт, аура и −75% маны на 30–50 секунд.",
    ]),
    ("forge", "best_armor", "Сильнейшая броня", "yukicore:eternal_chestplate", [
        "Броня YukiCore снижает $(l)любой урон$() на процент — сверх обычной защиты, поэтому она сильнее брони других "
        "модов:$(li)Мороз — 15%$(li)Аврорий — 25%$(li)Целестий — 42%$(li)Регалии магов — 50%$(li)$(l)Вечная зима — 75%$()",
        ("spot", "yukicore:eternal_chestplate", "$(l)Вечная зима$(): Алтарь III из целестиевой брони, Слезы Юки, трофеев "
                                                "боссов и звёздного сплава. Полёт, иммунитет к огню, холоду и иссушению."),
        ("spot", "yukicore:yuki_regalia_robe", "$(l)Регалии Юки и Синтары$() — 3D-броня архимагов: +15% силы за часть, "
                                               "−8% маны, +60 маны."),
    ]),
    ("industry", "reactor_v7", "Звёздный реактор 7.0", "yukicore:star_reactor", [
        "Реактор стал эндгеймовым: до $(l)10 млн FE/т$() на осколках звезды (нитро-реактор Powah — около 2 млн). "
        "Буфер 2 млрд FE, отдаёт до 50 млн FE/т в каждую сторону.",
        "Кузня принимает до 20 млн FE/т; звёздный сплав стоит 400 млн FE. Кузня делает и детали AE2: флюикс, "
        "процессоры, сингулярности.",
    ]),
]


def _tree_pages(ids, color):
    pages, cur = [], ""
    for sid in ids:
        rn, _, rd, _ = SKILLS[sid]
        line = f"$(li)$({color}){rn}$() — {rd}"
        if len(cur) + len(line) > 520:
            pages.append(cur)
            cur = ""
        cur += line
    if cur:
        pages.append(cur)
    return pages


_ICE = list(SKILLS)[:22]
_FIRE = list(SKILLS)[22:]
ENTRIES += [
    ("magic", "tree_yuki", "Дерево Юки: 22 навыка", "yukicore:yuki_staff", _tree_pages(_ICE, "#8fd8ff")),
    ("magic", "tree_sintara", "Дерево Синтары: 22 навыка", "yukicore:sintara_staff", _tree_pages(_FIRE, "#ffa040")),
]


ENTRIES.append(("stars", "planets_v71", "Чудеса планет", "yukicore:aurora_shard", [
    "У каждой планеты Системы Юки теперь $(l)три биома$(), свои постройки и небо, где видны соседние планеты.",
    "$(li)$(#8fd8ff)Юкия$() — ледяные равнины, морозный лес, кристальный ледник; иглу и порталы$(li)$(#c7a8ff)Аврора$() — "
    "аметистовые поля, сияющая долина, шпили; руины тропы и испытательные залы$(li)$(#ff8a2a)Пиролит$() — дельты, "
    "пепельные пустоши, магмовые поля; бастионы и крепости",
    "$(li)$(#a070f0)Никта$() — скалк, поля пустоты, теневой лес; $(l)Древний город$() с Хранителем$(li)$(#ffe070)Целестия$() — "
    "кварцевые террасы, золотые луга, звёздный сад; $(l)города Края$()",
    "Раз в 3–6 минут на планете случается $(l)явление$(): метель, северное сияние, метеоритный дождь, пустотный "
    "разлом или звездопад. С неба при этом падают ресурсы планеты — ищи светящиеся предметы.",
    "В сундуках построек на планетах лежит особый лут: эссенции, кристаллы маны, тома знаний, осколки звезды.",
]))
