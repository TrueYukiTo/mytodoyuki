"""
Планеты 7.1: по 3 биома на каждой, кристаллические столбы и шпили, постройки (иглу, руины, бастионы,
древний город, город Края, испытательные залы), лут на планетах и небо с соседними планетами системы.
"""
import copy
import json
import math
import random
import zipfile
import zlib

from PIL import Image, ImageDraw

M = "yukicore"
COND = [{"type": "neoforge:mod_loaded", "modid": "ad_astra"}]
VANILLA = "/root/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar"


def _cond(d):
    return {"neoforge:conditions": COND, **d}


# биом: (суффикс, ru, en, верх, под верхом, небо, туман, частица, фичи {шаг: [..]}, монстры, существа, осадки, t°)
BIOMES = {
    "yukia": [
        ("", "Ледяные равнины Юкии", "Yukian Ice Plains", "minecraft:snow_block", "minecraft:packed_ice", 0xBFE6FF, 0xDDF2FF,
         "minecraft:snowflake", {1: ["minecraft:iceberg_packed", "minecraft:iceberg_blue"], 4: ["minecraft:ice_spike", "minecraft:ice_patch"],
                                 10: ["minecraft:freeze_top_layer"]},
         [("minecraft:stray", 80, 1, 3)], [("minecraft:polar_bear", 8, 1, 2)], True, -0.8),
        ("frost_forest", "Морозный лес", "Frost Forest", "minecraft:snow_block", "minecraft:dirt", 0xA8D8FF, 0xE6F6FF,
         "minecraft:snowflake", {9: ["minecraft:trees_snowy", "minecraft:patch_berry_common"], 10: ["minecraft:freeze_top_layer"]},
         [("minecraft:stray", 60, 1, 2), ("minecraft:wolf", 10, 1, 3)], [("minecraft:fox", 10, 1, 3), ("minecraft:rabbit", 10, 1, 3)],
         True, -0.6),
        ("crystal_glacier", "Кристальный ледник", "Crystal Glacier", "minecraft:blue_ice", "minecraft:packed_ice", 0x8FD8FF, 0xC8F0FF,
         "minecraft:end_rod", {4: ["minecraft:blue_ice", "@crystal_spire"], 10: ["minecraft:freeze_top_layer"]},
         [("minecraft:stray", 50, 1, 2)], [], True, -0.9),
    ],
    "aurora": [
        ("", "Аметистовые поля Авроры", "Auroran Amethyst Fields", "minecraft:calcite", "minecraft:calcite", 0x7A5CD0, 0xC7A8FF,
         "minecraft:end_rod", {2: ["minecraft:amethyst_geode"], 9: ["@amethyst_patch", "minecraft:glow_lichen"]},
         [("minecraft:stray", 40, 1, 2), ("minecraft:witch", 10, 1, 1)], [], False, 0.2),
        ("glow_valley", "Сияющая долина", "Glowing Valley", "minecraft:moss_block", "minecraft:clay", 0x3AD8B0, 0x9AF0D8,
         "minecraft:glow", {9: ["minecraft:lush_caves_vegetation", "minecraft:rooted_azalea_tree", "minecraft:glow_lichen"]},
         [("minecraft:witch", 15, 1, 1)], [("minecraft:allay", 4, 1, 2), ("minecraft:frog", 8, 1, 3)], False, 0.5),
        ("aurora_spires", "Шпили сияния", "Aurora Spires", "minecraft:calcite", "minecraft:tuff", 0x9A5CFF, 0xD8B8FF,
         "minecraft:end_rod", {4: ["@aurora_spire", "minecraft:amethyst_geode"]},
         [("minecraft:stray", 40, 1, 2), ("minecraft:vex", 6, 1, 2)], [], False, 0.1),
    ],
    "pyrolith": [
        ("", "Базальтовые дельты Пиролита", "Pyrolith Basalt Deltas", "minecraft:basalt", "minecraft:blackstone", 0x3A0A04, 0x6A1A0A,
         "minecraft:white_ash", {4: ["minecraft:delta", "minecraft:small_basalt_columns", "minecraft:large_basalt_columns"],
                                 8: ["minecraft:spring_delta", "minecraft:spring_lava"]},
         [("minecraft:blaze", 40, 1, 2), ("minecraft:magma_cube", 40, 1, 3)], [], False, 2.0),
        ("ash_wastes", "Пепельные пустоши", "Ash Wastes", "minecraft:soul_soil", "minecraft:blackstone", 0x2A1410, 0x4A2A20,
         "minecraft:ash", {9: ["minecraft:patch_fire", "minecraft:patch_soul_fire", "minecraft:basalt_pillar"]},
         [("minecraft:wither_skeleton", 20, 1, 2), ("minecraft:blaze", 30, 1, 2)], [], False, 2.0),
        ("magma_fields", "Магмовые поля", "Magma Fields", "minecraft:magma_block", "minecraft:blackstone", 0x5A1404, 0x8A2A08,
         "minecraft:lava", {1: ["minecraft:lake_lava_surface"], 4: ["@magma_spire"], 8: ["minecraft:spring_lava"]},
         [("minecraft:magma_cube", 60, 1, 4), ("minecraft:ghast", 6, 1, 1)], [("minecraft:strider", 10, 1, 2)], False, 2.0),
    ],
    "nyx": [
        ("", "Скалковые равнины Никты", "Nyx Sculk Plains", "minecraft:sculk", "minecraft:tuff", 0x000000, 0x120A20,
         "minecraft:sculk_soul", {7: ["minecraft:sculk_patch_deep_dark"], 9: ["minecraft:sculk_vein"]},
         [("minecraft:enderman", 40, 1, 2), ("minecraft:endermite", 20, 1, 3)], [], False, -0.5),
        ("void_fields", "Поля пустоты", "Void Fields", "minecraft:end_stone", "minecraft:obsidian", 0x05010C, 0x1A0A30,
         "minecraft:reverse_portal", {4: ["@obsidian_spire"], 9: ["minecraft:chorus_plant"]},
         [("minecraft:enderman", 60, 1, 3), ("minecraft:shulker", 4, 1, 1)], [], False, -0.5),
        ("shadow_forest", "Теневой лес", "Shadow Forest", "minecraft:warped_nylium", "minecraft:deepslate", 0x0A1A20, 0x0A2A30,
         "minecraft:warped_spore", {9: ["minecraft:warped_forest_vegetation", "minecraft:warped_fungi", "minecraft:twisting_vines"]},
         [("minecraft:enderman", 30, 1, 2), ("minecraft:phantom", 10, 1, 2)], [], False, -0.5),
    ],
    "celestia": [
        ("", "Кварцевые террасы Целестии", "Celestian Quartz Terraces", "minecraft:smooth_quartz", "minecraft:calcite", 0xFFE6B0,
         0xFFF4DA, "minecraft:end_rod", {2: ["minecraft:amethyst_geode"], 4: ["@quartz_spire"]},
         [("minecraft:enderman", 10, 1, 1), ("minecraft:vex", 6, 1, 2)], [], False, 0.8),
        ("golden_meadows", "Золотые луга", "Golden Meadows", "minecraft:grass_block", "minecraft:dirt", 0xFFD890, 0xFFF0C8,
         "minecraft:cherry_leaves", {9: ["minecraft:trees_cherry", "minecraft:flower_cherry", "minecraft:flower_meadow",
                                         "minecraft:patch_grass_plain"]},
         [("minecraft:vex", 4, 1, 1)], [("minecraft:allay", 6, 1, 2), ("minecraft:rabbit", 10, 1, 3), ("minecraft:horse", 4, 1, 2)],
         False, 0.8),
        ("star_garden", "Звёздный сад", "Star Garden", "minecraft:calcite", "minecraft:calcite", 0xFFC8F0, 0xFFE8FA,
         "minecraft:firework", {4: ["@star_spire", "minecraft:glowstone_extra"], 9: ["@amethyst_patch"]},
         [("minecraft:vex", 8, 1, 2)], [("minecraft:allay", 6, 1, 2)], False, 0.8),
    ],
}

# свои фичи-шпили: (блок, высота мин–макс, частота)
SPIRES = {
    "crystal_spire": ("yukicore:yuki_crystal_block", 3, 9, 3),
    "aurora_spire": ("yukicore:aurora_block", 4, 12, 3),
    "magma_spire": ("minecraft:magma_block", 3, 8, 4),
    "obsidian_spire": ("minecraft:crying_obsidian", 5, 14, 2),
    "quartz_spire": ("minecraft:quartz_pillar", 4, 12, 3),
    "star_spire": ("yukicore:starlight_block", 3, 9, 2),
}

# постройки: планета → [(ванильная постройка, шаг, разрыв, соль)]
STRUCTURES = {
    "yukia": [("igloo", 18, 6), ("ruined_portal_mountain", 30, 10)],
    "aurora": [("trail_ruins", 24, 8), ("trial_chambers", 34, 12)],
    "pyrolith": [("bastion_remnant", 26, 8), ("nether_fossil", 10, 4), ("fortress", 30, 10)],
    "nyx": [("ancient_city", 26, 8)],
    "celestia": [("end_city", 20, 8), ("trial_chambers", 34, 12)],
}

# лут на планетах: дополнительные предметы в сундуках построек
LOOT = {
    "yukia": [("permafrost_crystal", 4, 10), ("frost_essence", 2, 6), ("greater_frost_essence", 1, 2), ("yuki_crystal", 3, 8)],
    "aurora": [("aurora_shard", 4, 10), ("mana_crystal", 1, 1), ("greater_frost_essence", 1, 3), ("mana_potion", 2, 4)],
    "pyrolith": [("raw_pyrolite", 4, 10), ("flame_essence", 3, 8), ("greater_flame_essence", 1, 3), ("star_shard", 1, 1)],
    "nyx": [("void_shard", 3, 8), ("tome_of_winter", 1, 1), ("tome_of_flame", 1, 1), ("star_shard", 1, 2)],
    "celestia": [("stellar_shard", 3, 8), ("stellar_alloy", 1, 2), ("mana_crystal", 1, 2), ("star_shard", 1, 3)],
}
CHEST_TABLES = {
    "igloo": ["minecraft:chests/igloo_chest"], "ruined_portal_mountain": ["minecraft:chests/ruined_portal"],
    "trail_ruins": ["minecraft:archaeology/trail_ruins_rare", "minecraft:archaeology/trail_ruins_common"],
    "trial_chambers": ["minecraft:chests/trial_chambers/reward", "minecraft:chests/trial_chambers/supply"],
    "bastion_remnant": ["minecraft:chests/bastion_treasure", "minecraft:chests/bastion_other"],
    "nether_fossil": [], "fortress": ["minecraft:chests/nether_bridge"],
    "ancient_city": ["minecraft:chests/ancient_city"], "end_city": ["minecraft:chests/end_city_treasure"],
}

GLM_ENTRIES = []


def biome_id(pid, suf):
    return f"{M}:{pid}" if not suf else f"{M}:{pid}_{suf}"


def _spire_files(out):
    for name, (block, lo, hi, count) in SPIRES.items():
        out[f"data/{M}/worldgen/configured_feature/{name}.json"] = {
            "type": "minecraft:block_column", "config": {
                "direction": "up", "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
                "prioritize_tip": False,
                "layers": [{"height": {"type": "minecraft:uniform", "min_inclusive": lo, "max_inclusive": hi},
                            "provider": {"type": "minecraft:simple_state_provider", "state": {"Name": block}}}]}}
        out[f"data/{M}/worldgen/placed_feature/{name}.json"] = {"feature": f"{M}:{name}", "placement": [
            {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]}
    out[f"data/{M}/worldgen/configured_feature/amethyst_patch.json"] = {
        "type": "minecraft:random_patch", "config": {"tries": 24, "xz_spread": 6, "y_spread": 2, "feature": {
            "feature": {"type": "minecraft:simple_block", "config": {"to_place": {
                "type": "minecraft:weighted_state_provider", "entries": [
                    {"weight": 3, "data": {"Name": "minecraft:amethyst_cluster", "Properties": {"facing": "up", "waterlogged": "false"}}},
                    {"weight": 2, "data": {"Name": "minecraft:large_amethyst_bud", "Properties": {"facing": "up", "waterlogged": "false"}}}]}}},
            "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks",
                                                                                    "blocks": "minecraft:air"}}]}}}
    out[f"data/{M}/worldgen/placed_feature/amethyst_patch.json"] = {"feature": f"{M}:amethyst_patch", "placement": [
        {"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]}


def _biome_json(pid, b, ore):
    suf, ru, en, top, under, sky, fog, particle, feats, mons, crea, precip, temp = b

    def spawns(lst):
        return [{"type": t, "weight": w, "minCount": a, "maxCount": c} for t, w, a, c in lst]
    features = [[] for _ in range(11)]
    for step, fs in feats.items():
        features[step] += [f"{M}:{f[1:]}" if f.startswith("@") else f for f in fs]
    features[6].append(f"{M}:ore_{pid}")
    return _cond({
        "has_precipitation": precip, "temperature": temp, "downfall": 0.4,
        "effects": {"sky_color": sky, "fog_color": fog, "water_color": 0x3F76E4, "water_fog_color": 0x050533,
                    "grass_color": 0xFFD86A if pid == "celestia" else 0x6AC8A0, "foliage_color": 0xFFB0D8 if pid == "celestia" else 0x58D8B0,
                    "particle": {"options": {"type": particle}, "probability": 0.015},
                    "mood_sound": {"block_search_extent": 8, "offset": 2.0, "sound": "minecraft:ambient.cave", "tick_delay": 6000}},
        "spawners": {"monster": spawns(mons), "creature": spawns(crea), "ambient": [], "axolotls": [], "misc": [],
                     "underground_water_creature": [], "water_ambient": [], "water_creature": []},
        "spawn_costs": {},
        "carvers": {"air": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"]},
        "features": features})


def surface_rule(pid):
    seq = [{"type": "minecraft:condition",
            "if_true": {"type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor",
                        "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}},
            "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:bedrock"}}}]
    for b in BIOMES[pid]:
        top, under = b[3], b[4]
        seq.append({"type": "minecraft:condition", "if_true": {"type": "minecraft:biome", "biome_is": [biome_id(pid, b[0])]},
                    "then_run": {"type": "minecraft:sequence", "sequence": [
                        {"type": "minecraft:condition",
                         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                                     "add_surface_depth": False, "secondary_depth_range": 0},
                         "then_run": {"type": "minecraft:block", "result_state": {"Name": top}}},
                        {"type": "minecraft:condition",
                         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                                     "add_surface_depth": True, "secondary_depth_range": 0},
                         "then_run": {"type": "minecraft:block", "result_state": {"Name": under}}}]}})
    return {"type": "minecraft:sequence", "sequence": seq}


def biome_source(pid):
    bs = BIOMES[pid]
    ranges = [[-1.0, -0.15], [-0.15, 0.15], [0.15, 1.0]]
    return {"type": "minecraft:multi_noise", "biomes": [
        {"biome": biome_id(pid, b[0]), "parameters": {
            "temperature": ranges[i], "humidity": [-1.0, 1.0], "continentalness": [-1.0, 1.0], "erosion": [-1.0, 1.0],
            "weirdness": [-1.0, 1.0], "depth": 0.0, "offset": 0.0}} for i, b in enumerate(bs)]}


def structures(out):
    z = zipfile.ZipFile(VANILLA)
    for pid, lst in STRUCTURES.items():
        for name, spacing, sep in lst:
            sid = f"{pid}_{name}"
            src = json.loads(z.read(f"data/minecraft/worldgen/structure/{name}.json"))
            src["biomes"] = f"#{M}:has_structure/{sid}"
            out[f"data/{M}/worldgen/structure/{sid}.json"] = _cond(src)
            out[f"data/{M}/tags/worldgen/biome/has_structure/{sid}.json"] = {
                "values": [biome_id(pid, b[0]) for b in BIOMES[pid]]}
            out[f"data/{M}/worldgen/structure_set/{sid}.json"] = _cond({
                "structures": [{"structure": f"{M}:{sid}", "weight": 1}],
                "placement": {"type": "minecraft:random_spread", "spacing": spacing, "separation": sep,
                              "salt": zlib.crc32(sid.encode()) % 100_000_000}})
            # лут планеты в сундуках постройки
            for table in CHEST_TABLES.get(name, []):
                key = f"planet_{pid}_{table.split('/')[-1]}"
                out[f"data/{M}/loot_modifiers/{key}.json"] = {
                    "type": "neoforge:add_table",
                    "conditions": [{"condition": "neoforge:loot_table_id", "loot_table_id": table},
                                   {"condition": "minecraft:location_check",
                                    "predicate": {"dimension": f"{M}:{pid}"}}],
                    "table": f"{M}:chests/planet_{pid}"}
                GLM_ENTRIES.append(f"{M}:{key}")
        out[f"data/{M}/loot_table/chests/planet_{pid}.json"] = {"type": "minecraft:chest", "pools": [
            {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4}, "bonus_rolls": 0, "entries": [
                {"type": "minecraft:item", "name": f"{M}:{it}", "weight": 10 if lo > 1 else 3, "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}
                for it, lo, hi in LOOT[pid]]}]}


# ------------------------------------------------------------------ небо: соседние планеты
PLANET_COLORS = {"yukia": [0xEAF6FF, 0x8FD8FF, 0x3A7BD5], "aurora": [0xE0C8FF, 0x9A5CFF, 0x3AD8B0],
                 "pyrolith": [0xFFD060, 0xE0501A, 0x3A0A04], "nyx": [0x6A3AB0, 0x2A0E4A, 0x05010C],
                 "celestia": [0xFFFFFF, 0xFFE070, 0xC08A20]}


def planet_texture(pid, seed):
    s = 64
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    rng = random.Random(seed)
    c = [((h >> 16) & 255, (h >> 8) & 255, h & 255) for h in PLANET_COLORS[pid]]
    px = img.load()
    for y in range(s):
        for x in range(s):
            dx, dy = (x - s / 2 + 0.5) / (s / 2), (y - s / 2 + 0.5) / (s / 2)
            r = dx * dx + dy * dy
            if r > 1:
                continue
            band = 0.5 + 0.5 * math.sin(dy * 9 + math.sin(dx * 3 + seed) * 1.5)
            light = max(0.25, 1 - ((dx + 0.4) ** 2 + (dy + 0.4) ** 2) * 0.55)
            a, b = (c[0], c[1]) if band > 0.5 else (c[1], c[2])
            t = abs(band - 0.5) * 2
            col = tuple(int((a[i] * t + b[i] * (1 - t)) * light) for i in range(3))
            px[x, y] = (*col, 255)
    d = ImageDraw.Draw(img)
    for _ in range(6):
        x, y = rng.randrange(10, 54), rng.randrange(10, 54)
        if (x - 32) ** 2 + (y - 32) ** 2 < 600:
            d.ellipse([x - 2, y - 1, x + 2, y + 1], fill=(*c[2], 180))
    return img


def sky(pid):
    others = [o for o in PLANET_COLORS if o != pid]
    sun = {"pyrolith": "ad_astra:textures/environment/red_sun.png", "nyx": "ad_astra:textures/environment/blue_sun.png"}.get(
        pid, "ad_astra:textures/environment/sun.png")
    sun_scale = {"pyrolith": 30.0, "nyx": 4.0, "celestia": 28.0}.get(pid, 14.0)
    rend = [{"back_light_color": -39, "back_light_scale": sun_scale * 2.2, "blend": True, "global_rotation": [0.0, 0.0, 0.0],
             "local_rotation": [0.0, 0.0, 0.0], "movement_type": "TIME_OF_DAY", "scale": sun_scale, "texture": sun}]
    for i, o in enumerate(others[:3]):
        rend.append({"back_light_color": -6861640, "back_light_scale": 40.0 + i * 10, "blend": False,
                     "global_rotation": [40.0 + i * 35, i * 70.0, 10.0 + i * 15], "local_rotation": [0.0, 0.0, -5.0 + i * 8],
                     "movement_type": "STATIC", "scale": 26.0 - i * 6, "texture": f"{M}:textures/environment/{o}.png"})
    stars = {"nyx": 9000, "celestia": 4500}.get(pid, 3000)
    return {"custom_clouds": False, "custom_sky": True, "custom_weather": False, "dimension": f"{M}:{pid}",
            "has_fog": True, "has_thick_fog": pid == "nyx", "render_in_rain": False, "sky_renderables": rend,
            "star_colors": [{"data": -1, "weight": 3}, {"data": -22118, "weight": 1}, {"data": -6861640, "weight": 1}],
            "stars": stars, "sunrise_angle": 0, "sunrise_color": 14180147}


def apply(out, planets_mod):
    """Достраивает файлы планет из planets.files(): биомы, поверхность, постройки, небо."""
    GLM_ENTRIES.clear()
    _spire_files(out)
    for pid in BIOMES:
        ore = planets_mod.PLANETS[pid]["ore"]
        for b in BIOMES[pid]:
            bid = biome_id(pid, b[0]).split(":")[1]
            out[f"data/{M}/worldgen/biome/{bid}.json"] = _biome_json(pid, b, ore)
        out[f"data/{M}/worldgen/noise_settings/{pid}.json"]["surface_rule"] = surface_rule(pid)
        out[f"data/{M}/dimension/{pid}.json"]["generator"]["biome_source"] = biome_source(pid)
        out[f"assets/{M}/planet_renderers/{pid}.json"] = sky(pid)
        out[f"assets/{M}/textures/environment/{pid}.png"] = planet_texture(pid, len(pid) * 7)
    structures(out)
    return out


def lang():
    ru, en = {}, {}
    for pid, bs in BIOMES.items():
        for b in bs:
            k = "biome." + biome_id(pid, b[0]).replace(":", ".")
            ru[k], en[k] = b[1], b[2]
    return ru, en
