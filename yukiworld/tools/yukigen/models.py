"""
3D-модели боссов YukiCore.

Геометрия описана здесь (части, кубоиды, материалы). Из неё генерируются:
  * Java-класс BossLayers с LayerDefinition для каждого босса и путями к частям;
  * HD-текстура (×SCALE) и карта свечения (_glow) — светящиеся части рендерятся отдельным слоем.

Координаты как в Blockbench/Minecraft: ось Y вниз, земля на y=24, вперёд — −Z.
"""
import math
import random

from PIL import Image

SCALE = 4  # плотность текстуры: 4 пикселя на единицу модели (как HD-пак 64×)
ATLAS_W = 256
GLOW = 0.42


def P(name, pivot, rot=(0, 0, 0), cubes=(), children=()):
    return {"name": name, "pivot": pivot, "rot": rot, "cubes": list(cubes), "children": list(children)}


def C(x, y, z, w, h, d, mat, infl=0.0):
    return {"pos": (x, y, z), "size": (w, h, d), "mat": mat, "infl": infl}


# --------------------------------------------------------------------------- материалы
# имя: (основной, тёмный, светлый, узор, светится)
MATS = {
    "ice": (0x7CC8F0, 0x2C6CB0, 0xD6F2FF, "ice", False),
    "ice_dark": (0x2A5A90, 0x14284A, 0x6EA8D8, "stone", False),
    "ice_glow": (0xBFEFFF, 0x5AB8F0, 0xFFFFFF, "ice", True),
    "stone": (0x4A5468, 0x262C3A, 0x7A8498, "stone", False),
    "eye": (0xE8FFFF, 0x9FE0FF, 0xFFFFFF, "plain", True),
    "eye_ice": (0xC8F4FF, 0x6EC6FF, 0xFFFFFF, "plain", True),
    "eye_pink": (0xFF9EDC, 0xE060B0, 0xFFF0FA, "plain", True),
    "eye_void": (0xD080FF, 0x9040E0, 0xFFF0FF, "plain", True),
    "eye_fire": (0xFFE070, 0xFF9A1C, 0xFFFFFF, "plain", True),
    "robe": (0x2A3A8A, 0x141C4A, 0x6A80D8, "cloth", False),
    "robe_dark": (0x18204E, 0x0A0E28, 0x34408A, "cloth", False),
    "silver": (0xC8D4E8, 0x7A869C, 0xFFFFFF, "metal", False),
    "skin": (0xEAF0FA, 0xB8C4DA, 0xFFFFFF, "skin", False),
    "hair": (0xDCEBFF, 0x9AB4DA, 0xFFFFFF, "hair", False),
    "magma": (0x3A2018, 0x1A0C08, 0x5A3428, "magma", False),
    "lava": (0xFF7A1C, 0xC03A08, 0xFFE070, "fire", True),
    "obsidian": (0x221830, 0x0C0812, 0x3E2E52, "stone", False),
    "bone": (0xE0D4B8, 0x9A8C70, 0xFFF6E0, "stone", False),
    "fire": (0xFFA030, 0xE04A10, 0xFFF0A0, "fire", True),
    "void": (0x2A0E4A, 0x12061E, 0x4A2A7A, "void", False),
    "void_dark": (0x14061E, 0x06020A, 0x2A0E4A, "stone", False),
    "void_glow": (0xB070FF, 0x6A2BA8, 0xF0D8FF, "ice", True),
    "membrane": (0x4A1A7A, 0x200A3A, 0x8A50E0, "void", False),
    "shadow": (0x1E0E2A, 0x0A0510, 0x3A1A50, "cloth", False),
    "shadow_dark": (0x120818, 0x05020A, 0x2A1238, "cloth", False),
    "shadow_hair": (0x2A1440, 0x120820, 0x6A2A90, "hair", False),
    "pale": (0xD8C8E8, 0x9A88B0, 0xF4ECFF, "skin", False),
    "pink_glow": (0xFF9EDC, 0xC04A9A, 0xFFE0F4, "ice", True),
    "star_glow": (0xFFF0FA, 0xFFB0E0, 0xFFFFFF, "ice", True),
    "gold": (0xF2C030, 0xA06A10, 0xFFE68A, "metal", False),
    "armor": (0x9AB4D4, 0x4A6A90, 0xE0F0FF, "metal", False),
    "armor_dark": (0x3A4E6A, 0x1A2438, 0x6A84A8, "metal", False),
    "feather": (0xD8401A, 0x8A1A08, 0xFF8A3A, "feather", False),
    "feather_light": (0xFF9A3A, 0xD05A10, 0xFFD08A, "feather", False),
    "ember": (0x5A1A0A, 0x2A0804, 0xA0300C, "magma", False),
}


def mirror(part, suffix_from="_r", suffix_to="_l"):
    """Зеркальная копия части по оси X (для рук, ног, крыльев)."""
    def m(p):
        px, py, pz = p["pivot"]
        rx, ry, rz = p["rot"]
        cubes = [C(-c["pos"][0] - c["size"][0], c["pos"][1], c["pos"][2], *c["size"], c["mat"], c["infl"])
                 for c in p["cubes"]]
        return P(p["name"].replace(suffix_from, suffix_to), (-px, py, pz), (rx, -ry, -rz), cubes,
                 [m(ch) for ch in p["children"]])
    return m(part)


# --------------------------------------------------------------------------- боссы
def frost_colossus():
    arm_r = P("arm_r", (-13, -17, 0), cubes=[C(-7, -4, -6, 8, 8, 12, "stone"), C(-6, 4, -4, 6, 12, 8, "ice")],
              children=[P("spike_r", (-4, -4, 0), (0, 0, -0.6), [C(-1.5, -9, -1.5, 3, 9, 3, "ice_glow")]),
                        P("forearm_r", (-3, 16, 0), cubes=[C(-4, 0, -5, 8, 11, 10, "ice"),
                                                           C(-5.5, 10, -6, 11, 9, 12, "ice_glow")])])
    leg_r = P("leg_r", (-5, 8, 0), cubes=[C(-4, 0, -4, 8, 11, 8, "stone"), C(-5, 11, -6, 10, 5, 11, "ice_dark")])
    torso = P("torso", (0, -3, 0), cubes=[
        C(-12, -20, -7, 24, 20, 14, "ice"), C(-9, -17, -8, 18, 12, 1, "ice_dark"),
        C(-3, -14, -9, 6, 6, 2, "ice_glow"), C(-10, -4, -6, 20, 4, 12, "stone")], children=[
        P("head", (0, -20, -2), cubes=[C(-5, -9, -5, 10, 9, 10, "ice"), C(-6, -11, -4, 12, 3, 8, "ice_dark"),
                                       C(-4, -6, -5.5, 3, 2, 1, "eye"), C(1, -6, -5.5, 3, 2, 1, "eye"),
                                       C(-3, -2, -6, 6, 3, 2, "stone")],
          children=[P("crest", (0, -11, 0), (-0.3, 0, 0), [C(-1.5, -8, -1.5, 3, 8, 3, "ice_glow")])]),
        arm_r, mirror(arm_r),
        P("crystals", (0, -14, 7), children=[
            P("crystal_a", (-6, 0, 0), (-0.5, 0, -0.35), [C(-2, -14, -2, 4, 14, 4, "ice_glow")]),
            P("crystal_b", (6, 0, 0), (-0.5, 0, 0.35), [C(-2, -12, -2, 4, 12, 4, "ice_glow")]),
            P("crystal_c", (0, -2, 0), (-0.7, 0, 0), [C(-2.5, -18, -2.5, 5, 18, 5, "ice_glow")])])])
    return P("root", (0, 0, 0), children=[P("hips", (0, 6, 0), cubes=[C(-8, -4, -5, 16, 6, 10, "stone")],
                                            children=[torso]), leg_r, mirror(leg_r)])


def blizzard_queen():
    arm_r = P("arm_r", (-6, -11, 0), cubes=[C(-3, -1, -2, 3, 11, 4, "robe"), C(-3.5, 7, -2.5, 4, 4, 5, "silver"),
                                           C(-2.5, 11, -1.5, 2, 2, 3, "skin")],
              children=[P("staff", (-1.5, 12, -0.5), cubes=[C(-0.5, -16, -0.5, 1, 32, 1, "silver"),
                                                            C(-2, -22, -2, 4, 6, 4, "ice_glow"),
                                                            C(-1.5, -17, -1.5, 3, 2, 3, "silver")])])
    arm_l = P("arm_l", (6, -11, 0), cubes=[C(0, -1, -2, 3, 11, 4, "robe"), C(-0.5, 7, -2.5, 4, 4, 5, "silver"),
                                          C(0.5, 11, -1.5, 2, 2, 3, "skin")])
    crown = P("crown", (0, -8, 0), cubes=[C(-4.5, -2, -4.5, 9, 2, 9, "ice_glow")], children=[
        P("crown_1", (0, -2, -4), (-0.2, 0, 0), [C(-1, -7, -1, 2, 7, 2, "ice_glow")]),
        P("crown_2", (-3, -2, -3.5), (-0.15, 0, -0.3), [C(-1, -5, -1, 2, 5, 2, "ice_glow")]),
        P("crown_3", (3, -2, -3.5), (-0.15, 0, 0.3), [C(-1, -5, -1, 2, 5, 2, "ice_glow")]),
        P("crown_4", (-4, -2, 1), (0, 0, -0.5), [C(-1, -4, -1, 2, 4, 2, "ice_glow")]),
        P("crown_5", (4, -2, 1), (0, 0, 0.5), [C(-1, -4, -1, 2, 4, 2, "ice_glow")])])
    body = P("body", (0, 6, 0), cubes=[C(-5, -13, -3, 10, 13, 6, "robe"), C(-5.5, -6, -3.5, 11, 2, 7, "silver"),
                                       C(-2, -11, -3.5, 4, 3, 1, "ice_glow")], children=[
        P("head", (0, -13, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "skin"), C(-4.5, -8.5, -4.5, 9, 3, 9, "hair"),
                                      C(-4.5, -6, 2.5, 9, 14, 2, "hair"), C(-3, -5, -4.5, 2, 1, 1, "eye_ice"),
                                      C(1, -5, -4.5, 2, 1, 1, "eye_ice")], children=[crown]),
        arm_r, arm_l,
        P("cape", (0, -12, 3), cubes=[C(-6, 0, 0, 12, 26, 1, "robe_dark")]),
        P("skirt", (0, 0, 0), cubes=[C(-6, 0, -4, 12, 6, 8, "robe")], children=[
            P("skirt2", (0, 6, 0), cubes=[C(-7, 0, -5, 14, 6, 10, "robe_dark")], children=[
                P("skirt3", (0, 6, 0), cubes=[C(-8, 0, -6, 16, 4, 12, "robe"),
                                              C(-8.5, 4, -6.5, 17, 2, 13, "ice_glow")])])])])
    shards = [P(f"shard_{i}", (0, 0, 0), cubes=[C(-1.5, -5, -1.5, 3, 10, 3, "ice_glow")]) for i in range(4)]
    return P("root", (0, 0, 0), children=[body] + shards)


def ash_lord():
    horn_r = P("horn_r", (-4, -8, -1), (0.3, 0, -0.6), [C(-1.5, -7, -1.5, 3, 7, 3, "bone")],
               [P("horn_r2", (0, -7, 0), (0.5, 0, 0.4), [C(-1, -6, -1, 2, 6, 2, "bone")])])
    arm_r = P("arm_r", (-10, -15, 0), cubes=[C(-6, -3, -4, 7, 7, 8, "obsidian"), C(-5, 3, -3, 6, 12, 6, "magma")],
              children=[P("forearm_r", (-2, 15, 0), cubes=[C(-3.5, 0, -3.5, 7, 10, 7, "magma"),
                                                           C(-4.5, 9, -4.5, 9, 7, 9, "lava")])])
    leg_r = P("leg_r", (-5, 6, 0), cubes=[C(-4, 0, -4, 7, 12, 8, "obsidian"), C(-4.5, 12, -5, 8, 6, 9, "magma")])
    body = P("body", (0, 4, 0), cubes=[C(-9, -18, -6, 18, 18, 12, "magma"), C(-6, -14, -6.5, 12, 9, 1, "lava"),
                                       C(-10, -2, -7, 20, 4, 14, "obsidian")], children=[
        P("head", (0, -18, -2), cubes=[C(-5, -9, -6, 10, 9, 10, "magma"), C(-4, -6, -6.5, 3, 2, 1, "fire"),
                                       C(1, -6, -6.5, 3, 2, 1, "fire"), C(-4, -2, -7, 8, 3, 3, "obsidian")],
          children=[horn_r, mirror(horn_r),
                    P("flame_crown", (0, -9, 0), cubes=[C(-4, -5, -3, 8, 5, 6, "fire")])]),
        arm_r, mirror(arm_r),
        P("tail", (0, -2, 6), (0.9, 0, 0), [C(-2, 0, 0, 4, 4, 10, "magma")], [
            P("tail2", (0, 0, 10), (0.3, 0, 0), [C(-1.5, 0.5, 0, 3, 3, 10, "magma"), C(-2.5, 0, 9, 5, 4, 4, "fire")])])])
    rocks = [P(f"rock_{i}", (0, 0, 0), cubes=[C(-2, -2, -2, 4, 4, 4, "magma" if i % 2 else "lava")]) for i in range(6)]
    return P("root", (0, 0, 0), children=[body, leg_r, mirror(leg_r)] + rocks)


def void_wyrm():
    wing_r = P("wing_r", (-5, -3, 3), cubes=[C(-22, -0.5, -3, 22, 1, 12, "membrane")],
               children=[P("wing_r2", (-22, 0, 0), cubes=[C(-20, -0.5, -2, 20, 1, 10, "membrane")])])

    def seg(i, size):
        h = size / 2
        cubes = [C(-h, -h, 0, size, size, 10, "void"), C(-0.5, -h - 4, 2, 1, 4, 6, "void_glow")]
        return P(f"seg{i}", (0, 0, 10), cubes=cubes)
    chain = P("tail", (0, 0, 10), cubes=[C(-0.5, -4, 0, 1, 8, 10, "void_glow"), C(-2, -2, 0, 4, 4, 6, "void")])
    for i, size in reversed(list(enumerate([9, 8, 7, 6, 5, 4], start=2))):
        s = seg(i, size)
        s["children"].append(chain)
        chain = s
    seg1 = P("seg1", (0, 12, -12), cubes=[C(-5, -5, 0, 10, 10, 10, "void"), C(-0.5, -9, 2, 1, 4, 6, "void_glow")],
             children=[wing_r, mirror(wing_r), chain])
    head = P("head", (0, 12, -12), cubes=[C(-5, -5, -10, 10, 8, 10, "void"), C(-4, -3, -18, 8, 4, 8, "void"),
                                          C(-5.5, -4, -8, 1, 2, 3, "eye_void"), C(4.5, -4, -8, 1, 2, 3, "eye_void")],
             children=[P("jaw", (0, 2, -10), cubes=[C(-4, 0, -8, 8, 2, 8, "void_dark"),
                                                    C(-3.5, -1, -7.5, 7, 1, 7, "bone")]),
                       P("horn_r", (-4, -5, -3), (0.7, 0, -0.3), [C(-1, -1, 0, 2, 2, 10, "bone")]),
                       P("horn_l", (4, -5, -3), (0.7, 0, 0.3), [C(-1, -1, 0, 2, 2, 10, "bone")])])
    return P("root", (0, 0, 0), children=[head, seg1])


def shadow_of_yuki():
    body = P("body", (0, 4, 0), cubes=[C(-4, -12, -2, 8, 12, 4, "shadow"), C(-4.5, -12, -2.5, 9, 3, 5, "gold"),
                                       C(-1, -9, -2.5, 2, 2, 1, "pink_glow")], children=[
        P("head", (0, -12, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "pale"), C(-4.5, -8.5, -4.5, 9, 4, 9, "shadow_hair"),
                                      C(-4.5, -5, 2.5, 9, 15, 2, "shadow_hair"), C(-3, -4, -4.5, 2, 1, 1, "eye_pink"),
                                      C(1, -4, -4.5, 2, 1, 1, "eye_pink")], children=[
            P("halo", (0, -6, 5), cubes=[C(-6, -7, 0, 12, 1, 1, "star_glow"), C(-6, 6, 0, 12, 1, 1, "star_glow"),
                                         C(-7, -7, 0, 1, 14, 1, "star_glow"), C(6, -7, 0, 1, 14, 1, "star_glow")])]),
        P("arm_r", (-5, -11, 0), cubes=[C(-3, -1, -1.5, 3, 12, 3, "shadow"), C(-3.5, 8, -2, 4, 2, 4, "gold")]),
        P("arm_l", (5, -11, 0), cubes=[C(0, -1, -1.5, 3, 12, 3, "shadow"), C(-0.5, 8, -2, 4, 2, 4, "gold")]),
        P("cloak", (0, -12, 2.5), cubes=[C(-6, 0, 0, 12, 24, 1, "shadow_dark")]),
        P("robe", (0, 0, 0), cubes=[C(-5, 0, -3, 10, 10, 6, "shadow")], children=[
            P("robe2", (0, 10, 0), cubes=[C(-6, 0, -4, 12, 6, 8, "shadow_dark"),
                                          C(-6.5, 6, -4.5, 13, 2, 9, "pink_glow")])])])
    swords = [P(f"sword_{i}", (0, 0, 0), cubes=[C(-0.5, -12, -1.5, 1, 16, 3, "star_glow"),
                                                C(-2.5, 4, -1, 5, 1, 2, "gold"), C(-0.5, 5, -0.5, 1, 4, 1, "shadow_dark")])
              for i in range(4)]
    return P("root", (0, 0, 0), children=[body] + swords)


def glacial_archon():
    leg_r = P("leg_r", (-3.5, 6, 0), cubes=[C(-3, 0, -3, 6, 12, 6, "armor_dark"), C(-3.5, 12, -4, 7, 6, 8, "armor")])
    horn = P("ahorn_r", (-4.5, -8, 0), (0, 0, -0.9), [C(-1, -8, -1, 2, 8, 2, "ice_glow")])
    body = P("body", (0, 6, 0), cubes=[C(-7, -18, -4, 14, 18, 8, "armor"), C(-5, -15, -4.5, 10, 8, 1, "armor_dark"),
                                       C(-2, -13, -5, 4, 4, 1, "ice_glow"), C(-7.5, -3, -4.5, 15, 3, 9, "armor_dark")],
             children=[
        P("head", (0, -18, 0), cubes=[C(-4.5, -10, -4.5, 9, 10, 9, "armor"), C(-3.5, -6, -5, 7, 1, 1, "eye_ice")],
          children=[P("plume", (0, -10, 0), cubes=[C(-0.5, -4, -4, 1, 4, 10, "ice_glow")]), horn, mirror(horn)]),
        P("arm_r", (-9, -16, 0), cubes=[C(-5, -4, -4.5, 8, 7, 9, "armor"), C(-4, 3, -2.5, 5, 14, 5, "armor_dark")],
          children=[P("sword", (-1.5, 16, 0), cubes=[C(-1, -1, -3, 2, 2, 6, "armor_dark"),
                                                      C(-4, -2, -5, 8, 4, 2, "gold"),
                                                      C(-1.5, -1, -33, 3, 2, 28, "ice_glow")])]),
        P("arm_l", (9, -16, 0), cubes=[C(-3, -4, -4.5, 8, 7, 9, "armor"), C(-1, 3, -2.5, 5, 14, 5, "armor_dark")],
          children=[P("shield", (5, 10, 0), cubes=[C(0, -8, -8, 2, 18, 16, "armor"),
                                                    C(1.5, -5, -3, 1, 10, 6, "ice_glow")])]),
        P("cape", (0, -17, 4), cubes=[C(-7, 0, 0, 14, 26, 1, "robe")])])
    return P("root", (0, 0, 0), children=[body, leg_r, mirror(leg_r)])


def infernal_phoenix():
    wing_r = P("wing_r", (-5, -3, -3), cubes=[C(-18, -1, -3, 18, 2, 14, "feather")],
               children=[P("wing_r2", (-18, 0, 0), cubes=[C(-18, -0.5, -2, 18, 1, 16, "fire")])])
    body = P("body", (0, 10, 0), cubes=[C(-5, -5, -8, 10, 10, 16, "feather"), C(-4, -3, -9, 8, 8, 6, "feather_light")],
             children=[
        P("neck", (0, -3, -8), (0.4, 0, 0), [C(-2.5, -9, -2.5, 5, 9, 5, "feather")], [
            P("head", (0, -9, 0), (-0.4, 0, 0), [C(-3.5, -6, -5, 7, 6, 8, "feather"), C(-1.5, -4, -10, 3, 3, 5, "gold"),
                                                 C(-4, -4, -3, 1, 1, 2, "eye_fire"), C(3, -4, -3, 1, 1, 2, "eye_fire")],
              [P("crest", (0, -6, 0), (-0.6, 0, 0), [C(-0.5, -2, 0, 1, 4, 12, "fire")]),
               P("crest2", (0, -5, 0), (-0.3, 0.3, 0), [C(-0.5, -2, 0, 1, 3, 9, "fire")]),
               P("crest3", (0, -5, 0), (-0.3, -0.3, 0), [C(-0.5, -2, 0, 1, 3, 9, "fire")])])]),
        wing_r, mirror(wing_r),
        P("tail", (0, -1, 8), (-0.3, 0, 0), [C(-1.5, 0, 0, 3, 1, 20, "fire")], [
            P("tail_r", (0, 0, 0), (0, -0.35, 0), [C(-1.5, 0, 0, 3, 1, 18, "fire")]),
            P("tail_l", (0, 0, 0), (0, 0.35, 0), [C(-1.5, 0, 0, 3, 1, 18, "fire")])]),
        P("leg_r", (-2.5, 5, 0), cubes=[C(-1, 0, -1, 2, 6, 2, "gold")]),
        P("leg_l", (2.5, 5, 0), cubes=[C(-1, 0, -1, 2, 6, 2, "gold")])])
    return P("root", (0, 0, 0), children=[body])


BOSSES = {
    "frost_colossus": frost_colossus,
    "blizzard_queen": blizzard_queen,
    "ash_lord": ash_lord,
    "void_wyrm": void_wyrm,
    "shadow_of_yuki": shadow_of_yuki,
    "glacial_archon": glacial_archon,
    "infernal_phoenix": infernal_phoenix,
}


# --------------------------------------------------------------------------- упаковка UV
def _walk(part, path=()):
    yield part, path
    for ch in part["children"]:
        yield from _walk(ch, path + (ch["name"],))


def footprint(c):
    w, h, d = (math.ceil(v) for v in c["size"])
    return 2 * (w + d), d + h


def pack(root):
    cubes = [c for p, _ in _walk(root) for c in p["cubes"]]
    order = sorted(cubes, key=lambda c: -footprint(c)[1])
    x = y = row_h = 0
    for c in order:
        fw, fh = footprint(c)
        if x + fw > ATLAS_W:
            x, y, row_h = 0, y + row_h, 0
        c["uv"] = (x, y)
        x += fw
        row_h = max(row_h, fh)
    h = y + row_h
    height = 16
    while height < h:
        height *= 2
    return ATLAS_W, height


# --------------------------------------------------------------------------- текстура
def _rgb(h):
    return (h >> 16) & 255, (h >> 8) & 255, h & 255


def _mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def _noise(rng, w, h, cell):
    gw, gh = w // cell + 2, h // cell + 2
    g = [[rng.random() for _ in range(gw)] for _ in range(gh)]

    def at(x, y):
        fx, fy = x / cell, y / cell
        ix, iy = int(fx), int(fy)
        tx, ty = fx - ix, fy - iy
        tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
        a = g[iy][ix] + (g[iy][ix + 1] - g[iy][ix]) * tx
        b = g[iy + 1][ix] + (g[iy + 1][ix + 1] - g[iy + 1][ix]) * tx
        return a + (b - a) * ty
    return at


def paint_face(img, glow, x0, y0, w, h, mat, rng, face):
    base, dark, light, pattern, emissive = MATS[mat]
    base, dark, light = _rgb(base), _rgb(dark), _rgb(light)
    if w <= 0 or h <= 0:
        return
    n1 = _noise(rng, w, h, max(3, SCALE * 2))
    n2 = _noise(rng, w, h, max(2, SCALE))
    px = img.load()
    gx = glow.load()
    cracks = set()
    if pattern in ("magma", "stone") and w > 6 and h > 6:
        for _ in range(max(1, (w * h) // (SCALE * SCALE * 40))):
            cx, cy = rng.randrange(w), rng.randrange(h)
            for _ in range(rng.randint(SCALE * 3, SCALE * 8)):
                cracks.add((cx, cy))
                cx = min(w - 1, max(0, cx + rng.choice((-1, 0, 1))))
                cy = min(h - 1, max(0, cy + rng.choice((-1, 0, 1, 1))))
    stars = set()
    if pattern == "void":
        for _ in range(max(1, (w * h) // (SCALE * SCALE * 6))):
            stars.add((rng.randrange(w), rng.randrange(h)))
    for yy in range(h):
        for xx in range(w):
            v = n1(xx, yy) * 0.65 + n2(xx, yy) * 0.35
            fy = yy / max(1, h - 1)
            if pattern == "ice":
                c = _mix(light, base, min(1, fy * 0.9 + v * 0.35))
                if (xx + yy * 2) % (SCALE * 7) < SCALE // 2 + 1 and v > 0.5:
                    c = _mix(c, (255, 255, 255), 0.45)
            elif pattern == "stone":
                c = _mix(dark, base, v * 1.2) if v < 0.8 else _mix(base, light, (v - 0.8) * 3)
            elif pattern == "cloth":
                fold = 0.5 + 0.5 * math.sin(xx / (SCALE * 1.6) + v * 2)
                c = _mix(dark, base, 0.35 + fold * 0.55)
                if face == "side" and yy > h - SCALE * 1.5:
                    c = _mix(c, light, 0.7)
            elif pattern == "hair":
                strand = 0.5 + 0.5 * math.sin(xx / (SCALE * 0.7) + v * 3)
                c = _mix(dark, light, 0.25 + strand * 0.6)
            elif pattern == "metal":
                edge = min(xx, yy) / max(1, min(w, h))
                c = _mix(base, light, max(0, 0.45 - edge * 2)) if xx + yy < (w + h) / 2 else _mix(base, dark, 0.35 * fy)
                c = _mix(c, dark, 0.15 * v)
                for rx, ry in ((SCALE, SCALE), (w - SCALE - 1, SCALE), (SCALE, h - SCALE - 1), (w - SCALE - 1, h - SCALE - 1)):
                    if w > SCALE * 4 and h > SCALE * 4 and abs(xx - rx) < SCALE * 0.6 and abs(yy - ry) < SCALE * 0.6:
                        c = light
            elif pattern == "skin":
                c = _mix(base, light, 0.25 + v * 0.3)
            elif pattern == "fire":
                flick = v + 0.25 * math.sin(xx / SCALE + yy / (SCALE * 2))
                c = _mix(light, base, min(1, fy * 0.8 + flick * 0.4))
                if flick > 0.85:
                    c = _mix(c, dark, 0.5)
            elif pattern == "magma":
                c = _mix(dark, base, v * 1.3)
            elif pattern == "void":
                c = _mix(dark, base, v * 1.2)
                if v > 0.72:
                    c = _mix(c, light, (v - 0.72) * 2.5)
            elif pattern == "feather":
                row = (yy % (SCALE * 3)) / (SCALE * 3)
                c = _mix(light, base, row * 0.9 + v * 0.2)
                if (yy % (SCALE * 3)) < max(1, SCALE // 2):
                    c = _mix(c, dark, 0.6)
            else:
                c = _mix(base, light, v * 0.5)
            # контур грани
            if xx < max(1, SCALE // 2) or yy < max(1, SCALE // 2) or xx >= w - max(1, SCALE // 2) or yy >= h - max(1, SCALE // 2):
                c = _mix(c, dark, 0.45)
            a = 255
            g = None
            if emissive:
                g = c
            if (xx, yy) in cracks:
                if pattern == "magma":
                    c = (255, 140 + rng.randrange(80), 40)
                    g = c
                else:
                    c = _mix(c, dark, 0.6)
            if (xx, yy) in stars:
                c = (240, 220, 255)
                g = c
            px[x0 + xx, y0 + yy] = (*c, a)
            if g is not None:
                # слой свечения аддитивный: приглушаем, чтобы цвет не выгорал в белый
                gx[x0 + xx, y0 + yy] = (*(int(v * GLOW) for v in g), 255)


def texture(root, size, seed):
    tw, th = size
    img = Image.new("RGBA", (tw * SCALE, th * SCALE), (0, 0, 0, 0))
    glow = Image.new("RGBA", (tw * SCALE, th * SCALE), (0, 0, 0, 0))
    rng = random.Random(seed)
    for p, _ in _walk(root):
        for c in p["cubes"]:
            u, v = c["uv"]
            w, h, d = (math.ceil(s) for s in c["size"])
            S = SCALE
            faces = [((u + d) * S, v * S, w * S, d * S, "top"), ((u + d + w) * S, v * S, w * S, d * S, "bottom"),
                     (u * S, (v + d) * S, d * S, h * S, "side"), ((u + d) * S, (v + d) * S, w * S, h * S, "side"),
                     ((u + d + w) * S, (v + d) * S, d * S, h * S, "side"),
                     ((u + 2 * d + w) * S, (v + d) * S, w * S, h * S, "side")]
            for fx, fy, fw, fh, kind in faces:
                paint_face(img, glow, fx, fy, fw, fh, c["mat"], rng, kind)
    return img, glow


# --------------------------------------------------------------------------- Java
def _f(v):
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    return (s if s not in ("-0", "") else "0") + "F"


def java(models):
    out = ["package dev.yukito.yukicore.client.boss;", "",
           "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;", "",
           "import java.util.HashMap;", "import java.util.Map;", "",
           "/** СГЕНЕРИРОВАНО tools/yukigen/models.py — не редактировать вручную. */",
           "public final class BossLayers {", "    private BossLayers() {}", ""]
    for name, (root, (tw, th)) in models.items():
        camel = "".join(x.capitalize() for x in name.split("_"))
        out.append(f"    public static LayerDefinition {camel[0].lower() + camel[1:]}() {{")
        out.append("        MeshDefinition mesh = new MeshDefinition();")
        out.append("        PartDefinition p0 = mesh.getRoot();")
        counter = [0]

        def emit(part, parent_var):
            counter[0] += 1
            var = f"p{counter[0]}"
            cubes = "CubeListBuilder.create()"
            for c in part["cubes"]:
                x, y, z = c["pos"]
                w, h, d = c["size"]
                cubes += (f".texOffs({c['uv'][0]}, {c['uv'][1]}).addBox({_f(x)}, {_f(y)}, {_f(z)}, {_f(w)}, {_f(h)}, "
                          f"{_f(d)}, new CubeDeformation({_f(c['infl'])}))")
            px, py, pz = part["pivot"]
            rx, ry, rz = part["rot"]
            out.append(f"        PartDefinition {var} = {parent_var}.addOrReplaceChild(\"{part['name']}\", {cubes}, "
                       f"PartPose.offsetAndRotation({_f(px)}, {_f(py)}, {_f(pz)}, {_f(rx)}, {_f(ry)}, {_f(rz)}));")
            for ch in part["children"]:
                emit(ch, var)
        for ch in root["children"]:
            emit(ch, "p0")
        out.append(f"        return LayerDefinition.create(mesh, {tw}, {th});")
        out.append("    }")
        out.append("")
    out.append("    /** Пути к частям моделей: имя части → цепочка имён от корня. */")
    out.append("    public static final Map<String, Map<String, String[]>> PATHS = new HashMap<>();")
    out.append("")
    out.append("    static {")
    for name, (root, _) in models.items():
        out.append(f"        Map<String, String[]> {name} = new HashMap<>();")
        for p, path in _walk(root):
            if path:
                arr = ", ".join(f'"{s}"' for s in path)
                out.append(f"        {name}.put(\"{p['name']}\", new String[]{{{arr}}});")
        out.append(f"        PATHS.put(\"{name}\", {name});")
    out.append("    }")
    out.append("}")
    return "\n".join(out) + "\n"


def build():
    """→ (java_source, {boss: (texture, glow)})"""
    models, textures = {}, {}
    for i, (name, fn) in enumerate(BOSSES.items()):
        root = fn()
        names = [p["name"] for p, _ in _walk(root)]
        assert len(names) == len(set(names)), f"{name}: duplicate part names"
        size = pack(root)
        models[name] = (root, size)
        textures[name] = texture(root, size, 900 + i)
    return java(models), textures


# =========================================================================== 3D-броня
MATS.update({
    "cloth_ice": (0x8FC8F0, 0x3A6AB0, 0xE0F4FF, "cloth", False),
    "cloth_ice_dark": (0x2C5A96, 0x14284A, 0x6EA8D8, "cloth", False),
    "fur": (0xF4F8FF, 0xB8C8DC, 0xFFFFFF, "hair", False),
    "cloth_fire": (0xC8301A, 0x6A1008, 0xFF8A4A, "cloth", False),
    "cloth_fire_dark": (0x5A120A, 0x2A0604, 0xA0301A, "cloth", False),
    "plate_ice": (0xBFDDF4, 0x5A86B4, 0xFFFFFF, "metal", False),
    "plate_gold": (0xF2C030, 0x9A6A10, 0xFFF0A0, "metal", False),
    "plate_star": (0xE8E0FF, 0x7A70C0, 0xFFFFFF, "metal", False),
})

HUMAN = {"head": (0, 0, 0), "hat": (0, 0, 0), "body": (0, 0, 0), "right_arm": (-5, 2, 0), "left_arm": (5, 2, 0),
         "right_leg": (-1.9, 12, 0), "left_leg": (1.9, 12, 0)}


def _armor(parts):
    """parts: {базовая часть: [дочерние части]}; дочерние имена начинаются с h_/c_/l_/b_ (шлем/нагрудник/поножи/ботинки)."""
    return P("root", (0, 0, 0), children=[P(n, HUMAN[n], children=parts.get(n, [])) for n in HUMAN])


def _limbs(prefix, mat_arm, mat_leg, extra_arm=None, extra_leg=None, boot=None, boot_trim=None):
    """Рукава (c_), штанины (l_) и сапоги (b_) для обеих сторон."""
    out = {}
    for side, sx in (("r", -1), ("l", 1)):
        arm = [C(-3 if sx < 0 else -1, -2, -2, 4, 12, 4, mat_arm, 0.6)]
        if extra_arm:
            arm += extra_arm(sx)
        out["right_arm" if sx < 0 else "left_arm"] = [P(f"c_{prefix}arm_{side}", (0, 0, 0), cubes=arm)]
        leg = [C(-2, 0, -2, 4, 12, 4, mat_leg, 0.45)]
        if extra_leg:
            leg += extra_leg(sx)
        legs = [P(f"l_{prefix}leg_{side}", (0, 0, 0), cubes=leg)]
        if boot:
            bc = [C(-2, 7, -2, 4, 5, 4, boot, 0.75)]
            if boot_trim:
                bc.append(C(-2.5, 6, -2.5, 5, 2, 5, boot_trim, 0.3))
            legs.append(P(f"b_{prefix}boot_{side}", (0, 0, 0), cubes=bc))
        out["right_leg" if sx < 0 else "left_leg"] = legs
    return out


def armor_cryomancer():
    parts = _limbs("", "cloth_ice", "cloth_ice_dark",
                   extra_arm=lambda sx: [C(-3.5 if sx < 0 else -1.5, 6, -2.5, 5, 5, 5, "fur", 0.1),
                                         C(-3.5 if sx < 0 else -1.5, -3, -2.5, 5, 3, 5, "plate_ice", 0.2)],
                   boot="cloth_ice_dark", boot_trim="fur")
    parts["head"] = [P("h_hood", (0, 0, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "cloth_ice", 1.0),
                                                   C(-2, -10, 2, 4, 3, 4, "cloth_ice", 0.2)],
                       children=[P("h_gem", (0, -8, -4.6), (0.3, 0, 0), [C(-1, -3, -0.5, 2, 3, 1, "ice_glow")])])]
    parts["body"] = [P("c_robe", (0, 0, 0), cubes=[C(-4, 0, -2, 8, 12, 4, "cloth_ice", 0.9),
                                                    C(-5, -0.5, -3, 10, 3, 6, "fur", 0.2),
                                                    C(-1, 3, -3.2, 2, 6, 1, "ice_glow")]),
                     P("c_cape", (0, 0, 2.8), (0.12, 0, 0), [C(-4.5, 0, 0, 9, 20, 1, "cloth_ice_dark")]),
                     P("l_skirt", (0, 0, 0), cubes=[C(-4, 9, -2, 8, 5, 4, "cloth_ice_dark", 1.0)])]
    return _armor(parts)


def armor_pyromancer():
    parts = _limbs("", "cloth_fire", "cloth_fire_dark",
                   extra_arm=lambda sx: [C(-3.5 if sx < 0 else -1.5, -3, -2.5, 5, 3, 5, "plate_gold", 0.2),
                                         C(-3.5 if sx < 0 else -1.5, 7, -2.5, 5, 3, 5, "plate_gold", 0.1)],
                   boot="cloth_fire_dark", boot_trim="plate_gold")
    parts["head"] = [P("h_hood", (0, 0, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "cloth_fire", 1.0),
                                                   C(-4.5, -1, -4.6, 9, 1, 1, "plate_gold")],
                       children=[P("h_flame", (0, -9, 0), (-0.25, 0, 0), [C(-1, -4, -1, 2, 4, 2, "fire"),
                                                                          C(-2, -2, -2, 4, 2, 4, "fire")])])]
    parts["body"] = [P("c_robe", (0, 0, 0), cubes=[C(-4, 0, -2, 8, 12, 4, "cloth_fire", 0.9),
                                                    C(-4.5, 8, -3, 9, 2, 6, "plate_gold", 0.2),
                                                    C(-1, 2, -3.2, 2, 5, 1, "lava")]),
                     P("c_cape", (0, 0, 2.8), (0.12, 0, 0), [C(-4.5, 0, 0, 9, 20, 1, "cloth_fire_dark")]),
                     P("l_skirt", (0, 0, 0), cubes=[C(-4, 9, -2, 8, 5, 4, "cloth_fire_dark", 1.0)])]
    return _armor(parts)


def _wings(prefix, mat, tip, glow):
    out = []
    for side, sx in (("r", -1), ("l", 1)):
        out.append(P(f"c_{prefix}wing_{side}", (sx * 2, 2, 3), (0.2, sx * 0.55, sx * 0.35),
                     [C(-12 if sx < 0 else 0, -6, 0, 12, 14, 1, mat)],
                     [P(f"c_{prefix}wing2_{side}", (sx * 12, -4, 0), (0, sx * 0.25, sx * 0.3),
                        [C(-10 if sx < 0 else 0, -4, 0, 10, 12, 1, tip)])]))
    return out


def armor_yuki_regalia():
    def crystals(sx):
        x = -3.5 if sx < 0 else -1.5
        return [C(x, -3.5, -2.5, 5, 4, 5, "plate_ice", 0.3)]
    parts = _limbs("", "cloth_ice", "cloth_ice_dark", extra_arm=crystals, boot="plate_ice", boot_trim="ice_glow")
    for side, sx in (("r", -1), ("l", 1)):
        parts["right_arm" if sx < 0 else "left_arm"].append(
            P(f"c_spike_{side}", (sx * 1.5, -3, 0), (0, 0, sx * 0.5), [C(-1, -6, -1, 2, 6, 2, "ice_glow")]))
    parts["head"] = [P("h_circlet", (0, 0, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "fur", 0.6),
                                                      C(-4.5, -8.6, -4.5, 9, 2, 9, "plate_ice", 0.3)],
                       children=[P("h_c1", (0, -9, -3.5), (-0.15, 0, 0), [C(-1, -7, -1, 2, 7, 2, "ice_glow")]),
                                 P("h_c2", (-3, -9, -3), (-0.1, 0, -0.35), [C(-1, -5, -1, 2, 5, 2, "ice_glow")]),
                                 P("h_c3", (3, -9, -3), (-0.1, 0, 0.35), [C(-1, -5, -1, 2, 5, 2, "ice_glow")]),
                                 P("h_c4", (-4, -9, 1), (0, 0, -0.6), [C(-1, -4, -1, 2, 4, 2, "ice_glow")]),
                                 P("h_c5", (4, -9, 1), (0, 0, 0.6), [C(-1, -4, -1, 2, 4, 2, "ice_glow")])])]
    parts["body"] = [P("c_robe", (0, 0, 0), cubes=[C(-4, 0, -2, 8, 12, 4, "cloth_ice", 0.9),
                                                    C(-4.5, 0, -3, 9, 7, 6, "plate_ice", 0.4),
                                                    C(-1.5, 2, -3.6, 3, 3, 1, "ice_glow")])] \
        + _wings("", "ice_glow", "star_glow", True) + [
        P("l_skirt", (0, 0, 0), cubes=[C(-4, 9, -2, 8, 5, 4, "cloth_ice_dark", 1.0)])]
    return _armor(parts)


def armor_sintara_regalia():
    def plates(sx):
        x = -3.5 if sx < 0 else -1.5
        return [C(x, -3.5, -2.5, 5, 4, 5, "plate_gold", 0.3), C(x, 7, -2.5, 5, 3, 5, "cloth_fire_dark", 0.2)]
    parts = _limbs("", "cloth_fire", "cloth_fire_dark", extra_arm=plates, boot="plate_gold", boot_trim="lava")
    for side, sx in (("r", -1), ("l", 1)):
        parts["right_arm" if sx < 0 else "left_arm"].append(
            P(f"c_flame_{side}", (sx * 1.5, -3.5, 0), (0, 0, sx * 0.35), [C(-1.5, -5, -1.5, 3, 5, 3, "fire")]))
    feathers = [P(f"h_f{i}", (0, -8.5, 1), (-0.5 - i * 0.12, (i - 2) * 0.35, 0), [C(-0.5, -8, 0, 1, 8, 2, "fire")])
                for i in range(5)]
    parts["head"] = [P("h_crown", (0, 0, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "cloth_fire", 0.6),
                                                    C(-4.5, -9, -4.5, 9, 2, 9, "plate_gold", 0.3),
                                                    C(-1, -10.5, -4.8, 2, 2, 1, "lava")], children=feathers)]
    parts["body"] = [P("c_robe", (0, 0, 0), cubes=[C(-4, 0, -2, 8, 12, 4, "cloth_fire", 0.9),
                                                    C(-4.5, 0, -3, 9, 7, 6, "plate_gold", 0.4),
                                                    C(-1.5, 2, -3.6, 3, 3, 1, "lava")])] \
        + _wings("", "feather", "fire", True) + [
        P("l_skirt", (0, 0, 0), cubes=[C(-4, 9, -2, 8, 5, 4, "cloth_fire_dark", 1.0)])]
    return _armor(parts)


def armor_eternal():
    def pauldron(sx):
        x = -4 if sx < 0 else -2
        return [C(x, -4, -3, 6, 5, 6, "plate_ice", 0.3), C(x, 6, -2.5, 6, 4, 5, "plate_gold", 0.1)]
    parts = _limbs("", "plate_ice", "plate_star", extra_arm=pauldron, boot="plate_ice", boot_trim="plate_gold")
    for side, sx in (("r", -1), ("l", 1)):
        parts["right_arm" if sx < 0 else "left_arm"].append(
            P(f"c_horn_{side}", (sx * 2, -4, 0), (0, 0, sx * 0.7), [C(-1, -7, -1, 2, 7, 2, "ice_glow")]))
        parts["right_leg" if sx < 0 else "left_leg"].append(
            P(f"l_knee_{side}", (0, 4, -2.5), cubes=[C(-2, -1, -1, 4, 3, 1, "plate_gold", 0.2)]))
    parts["head"] = [P("h_helm", (0, 0, 0), cubes=[C(-4, -8, -4, 8, 8, 8, "plate_ice", 1.0),
                                                   C(-3, -5, -5.2, 6, 1, 1, "eye_ice"),
                                                   C(-0.5, -10, -4.5, 1, 3, 8, "plate_gold")],
                       children=[P("h_horn_r", (-4.5, -7, 0), (0, 0, -0.9), [C(-1, -7, -1, 2, 7, 2, "ice_glow")],
                                   [P("h_horn_r2", (0, -7, 0), (0, 0, 0.6), [C(-0.5, -4, -0.5, 1, 4, 1, "star_glow")])]),
                                 P("h_horn_l", (4.5, -7, 0), (0, 0, 0.9), [C(-1, -7, -1, 2, 7, 2, "ice_glow")],
                                   [P("h_horn_l2", (0, -7, 0), (0, 0, -0.6), [C(-0.5, -4, -0.5, 1, 4, 1, "star_glow")])])])]
    parts["body"] = [P("c_plate", (0, 0, 0), cubes=[C(-4, 0, -2, 8, 12, 4, "plate_ice", 1.0),
                                                     C(-3, 1, -3.4, 6, 6, 1, "plate_gold"),
                                                     C(-1.5, 2.5, -3.8, 3, 3, 1, "ice_glow")]),
                     P("c_crystals", (0, 2, 3), children=[
                         P("c_cr1", (-2.5, 0, 0), (-0.4, 0, -0.35), [C(-1, -9, -1, 2, 9, 2, "ice_glow")]),
                         P("c_cr2", (2.5, 0, 0), (-0.4, 0, 0.35), [C(-1, -9, -1, 2, 9, 2, "ice_glow")]),
                         P("c_cr3", (0, 1, 0), (-0.5, 0, 0), [C(-1.5, -12, -1.5, 3, 12, 3, "star_glow")])]),
                     P("c_cape", (0, 0, 2.8), (0.12, 0, 0), [C(-4.5, 0, 0, 9, 19, 1, "cloth_ice_dark")]),
                     P("l_belt", (0, 0, 0), cubes=[C(-4, 9, -2, 8, 4, 4, "plate_gold", 1.1)])]
    return _armor(parts)


ARMORS = {
    "cryomancer": armor_cryomancer,
    "pyromancer": armor_pyromancer,
    "yuki_regalia": armor_yuki_regalia,
    "sintara_regalia": armor_sintara_regalia,
    "eternal": armor_eternal,
}


def build_armor():
    """→ (java_source, {set: (texture, glow)})"""
    global ATLAS_W
    models, textures = {}, {}
    saved = ATLAS_W
    ATLAS_W = 128
    try:
        for i, (name, fn) in enumerate(ARMORS.items()):
            root = fn()
            names = [p["name"] for p, _ in _walk(root)]
            assert len(names) == len(set(names)), f"{name}: duplicate part names"
            size = pack(root)
            models[name] = (root, size)
            textures[name] = texture(root, size, 1700 + i)
    finally:
        ATLAS_W = saved
    src = java(models).replace("public final class BossLayers", "public final class ArmorLayers") \
        .replace("private BossLayers()", "private ArmorLayers()") \
        .replace("package dev.yukito.yukicore.client.boss;", "package dev.yukito.yukicore.client.armor;")
    return src, textures
