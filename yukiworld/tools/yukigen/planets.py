"""
Система Юки — пять планет для Ad Astra (ракета 4-го уровня).

Для каждой планеты: dimension, dimension_type, noise_settings (рельеф Глацио, свои блоки),
биом (цвета неба, частицы, мобы, руда), placed/configured feature руды, ad_astra planet,
рендер неба. Всё помечено условием neoforge:mod_loaded ad_astra.
"""
import copy
import json
from pathlib import Path

M = "yukicore"
COND = [{"type": "neoforge:mod_loaded", "modid": "ad_astra"}]

PLANETS = {
    # id: русское имя, английское, блок по умолчанию, верх, под верхом, жидкость, эффекты, небо, туман,
    #     частица, гравитация, кислород, температура, солн. энергия, руда, мобы, фичи, ambient
    "yukia": dict(ru="Юкия", en="Yukia", stone="minecraft:packed_ice", top="minecraft:snow_block",
                  under="minecraft:snow_block", fluid="minecraft:water", effects="minecraft:overworld",
                  sky=0xBFE6FF, fog=0xDDF2FF, particle="minecraft:snowflake", gravity=6.2, oxygen=True,
                  temperature=-60, solar=12, ore="permafrost_ore", precipitation=True, temp=-0.8,
                  monsters=[("minecraft:stray", 80, 1, 3)], creatures=[("minecraft:polar_bear", 6, 1, 2)],
                  features={4: ["minecraft:ice_spike", "minecraft:ice_patch"], 10: ["minecraft:freeze_top_layer"]}),
    "aurora": dict(ru="Аврора", en="Aurora", stone="minecraft:amethyst_block", top="minecraft:calcite",
                   under="minecraft:calcite", fluid="minecraft:water", effects="minecraft:overworld",
                   sky=0x7A5CD0, fog=0xC7A8FF, particle="minecraft:end_rod", gravity=7.0, oxygen=False,
                   temperature=-20, solar=16, ore="aurora_ore", precipitation=False, temp=0.2,
                   monsters=[("minecraft:stray", 40, 1, 2), ("minecraft:witch", 10, 1, 1)], creatures=[],
                   features={2: ["minecraft:amethyst_geode"], 9: ["minecraft:glow_lichen"]}),
    "pyrolith": dict(ru="Пиролит", en="Pyrolith", stone="minecraft:blackstone", top="minecraft:basalt",
                     under="minecraft:blackstone", fluid="minecraft:lava", effects="minecraft:the_nether",
                     sky=0x3A0A04, fog=0x6A1A0A, particle="minecraft:white_ash", gravity=11.0, oxygen=False,
                     temperature=280, solar=30, ore="pyrolite_ore", precipitation=False, temp=2.0,
                     monsters=[("minecraft:blaze", 40, 1, 2), ("minecraft:magma_cube", 40, 1, 3)], creatures=[],
                     features={8: ["minecraft:spring_lava"],
                               9: ["minecraft:small_basalt_columns", "minecraft:large_basalt_columns"]}),
    "nyx": dict(ru="Никта", en="Nyx", stone="minecraft:deepslate", top="minecraft:sculk", under="minecraft:tuff",
                fluid="minecraft:water", effects="minecraft:the_end", sky=0x000000, fog=0x120A20,
                particle="minecraft:sculk_soul", gravity=4.0, oxygen=False, temperature=-180, solar=2,
                ore="void_ore", precipitation=False, temp=-0.5,
                monsters=[("minecraft:enderman", 40, 1, 2), ("minecraft:endermite", 20, 1, 3)], creatures=[],
                features={7: ["minecraft:sculk_patch_deep_dark"], 9: ["minecraft:sculk_vein"]}),
    "celestia": dict(ru="Целестия", en="Celestia", stone="minecraft:calcite", top="minecraft:smooth_quartz",
                     under="minecraft:calcite", fluid="minecraft:water", effects="minecraft:overworld",
                     sky=0xFFE6B0, fog=0xFFF4DA, particle="minecraft:end_rod", gravity=9.81, oxygen=True,
                     temperature=24, solar=40, ore="stellar_ore", precipitation=False, temp=0.8,
                     monsters=[("minecraft:enderman", 10, 1, 1), ("minecraft:vex", 6, 1, 2)], creatures=[],
                     features={2: ["minecraft:amethyst_geode"]}),
}


def _cond(d):
    return {"neoforge:conditions": COND, **d}


def surface_rule(top, under):
    return {"type": "minecraft:sequence", "sequence": [
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor",
                     "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:bedrock"}}},
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                     "add_surface_depth": False, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": top}}},
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                     "add_surface_depth": True, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": under}}},
    ]}


def biome(p, pid):
    def spawns(lst):
        return [{"type": t, "weight": w, "minCount": a, "maxCount": b} for t, w, a, b in lst]
    features = [[] for _ in range(11)]
    for step, fs in p["features"].items():
        features[step] += fs
    features[6].append(f"{M}:ore_{pid}")
    return {
        "has_precipitation": p["precipitation"], "temperature": p["temp"], "downfall": 0.4,
        "effects": {"sky_color": p["sky"], "fog_color": p["fog"], "water_color": 0x3F76E4,
                    "water_fog_color": 0x050533,
                    "particle": {"options": {"type": p["particle"]}, "probability": 0.012},
                    "mood_sound": {"block_search_extent": 8, "offset": 2.0, "sound": "minecraft:ambient.cave",
                                   "tick_delay": 6000}},
        "spawners": {"monster": spawns(p["monsters"]), "creature": spawns(p["creatures"]), "ambient": [],
                     "axolotls": [], "misc": [], "underground_water_creature": [], "water_ambient": [],
                     "water_creature": []},
        "spawn_costs": {},
        "carvers": {"air": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"]},
        "features": features,
    }


def files(adastra_data: Path):
    """adastra_data — распакованные data/ad_astra (для шаблона рельефа и типа измерения)."""
    tmpl_noise = json.loads((adastra_data / "worldgen/noise_settings/glacio.json").read_text())
    tmpl_type = json.loads((adastra_data / "dimension_type/glacio.json").read_text())
    orbit_type = json.loads((adastra_data / "dimension_type/glacio_orbit.json").read_text())
    out = {}
    for pid, p in PLANETS.items():
        dtype = copy.deepcopy(tmpl_type)
        dtype["effects"] = p["effects"]
        dtype["ultrawarm"] = p["temperature"] > 100
        noise = copy.deepcopy(tmpl_noise)
        noise["default_block"] = {"Name": p["stone"]}
        noise["default_fluid"] = {"Name": p["fluid"], "Properties": {"level": "0"}}
        noise["surface_rule"] = surface_rule(p["top"], p["under"])
        out[f"data/{M}/dimension_type/{pid}.json"] = _cond(dtype)
        out[f"data/{M}/worldgen/noise_settings/{pid}.json"] = _cond(noise)
        out[f"data/{M}/worldgen/biome/{pid}.json"] = _cond(biome(p, pid))
        out[f"data/{M}/dimension/{pid}.json"] = _cond({
            "type": f"{M}:{pid}",
            "generator": {"type": "minecraft:noise", "settings": f"{M}:{pid}",
                          "biome_source": {"type": "minecraft:fixed", "biome": f"{M}:{pid}"}}})
        out[f"data/{M}/planets/{pid}.json"] = _cond({
            "dimension": f"{M}:{pid}", "gravity": p["gravity"], "orbit": f"{M}:{pid}_orbit", "oxygen": p["oxygen"],
            "solar_power": p["solar"], "solar_system": f"{M}:yuki_system", "temperature": p["temperature"],
            "tier": 4})
        # орбита: без неё Ad Astra считает планету «космосом» и не показывает её в меню ракеты
        out[f"data/{M}/dimension_type/{pid}_orbit.json"] = _cond(copy.deepcopy(orbit_type))
        out[f"data/{M}/dimension/{pid}_orbit.json"] = _cond({
            "type": f"{M}:{pid}_orbit",
            "generator": {"type": "minecraft:noise", "settings": "ad_astra:orbit",
                          "biome_source": {"type": "minecraft:fixed", "biome": "ad_astra:orbit"}}})
        out[f"data/{M}/planets/{pid}_orbit.json"] = _cond({
            "dimension": f"{M}:{pid}_orbit", "gravity": 0.0, "oxygen": False, "solar_power": p["solar"] * 2,
            "solar_system": f"{M}:yuki_system", "temperature": -270, "tier": 4})
        # руда планеты — богатые жилы
        out[f"data/{M}/worldgen/configured_feature/ore_{pid}.json"] = {"type": "minecraft:ore", "config": {
            "size": 7, "discard_chance_on_air_exposure": 0.0,
            "targets": [{"target": {"predicate_type": "minecraft:block_match", "block": p["stone"]},
                         "state": {"Name": f"{M}:{p['ore']}"}}]}}
        out[f"data/{M}/worldgen/placed_feature/ore_{pid}.json"] = {"feature": f"{M}:ore_{pid}", "placement": [
            {"type": "minecraft:count", "count": 10}, {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid",
                                                          "min_inclusive": {"absolute": -48},
                                                          "max_inclusive": {"absolute": 96}}},
            {"type": "minecraft:biome"}]}
        # небо: солнце + соседние планеты системы
        out[f"assets/{M}/planet_renderers/{pid}.json"] = {
            "custom_clouds": False, "custom_sky": True, "custom_weather": False, "dimension": f"{M}:{pid}",
            "has_fog": True, "has_thick_fog": False, "render_in_rain": False,
            "sky_renderables": [
                {"back_light_color": -39, "back_light_scale": 27.0, "blend": True,
                 "global_rotation": [0.0, 0.0, 0.0], "local_rotation": [0.0, 0.0, 0.0],
                 "movement_type": "TIME_OF_DAY", "scale": 12.0, "texture": "ad_astra:textures/environment/blue_sun.png"},
                {"back_light_color": -6861640, "back_light_scale": 120.0, "blend": False,
                 "global_rotation": [50.0, 0.0, 10.0], "local_rotation": [0.0, 0.0, -5.0],
                 "movement_type": "STATIC", "scale": 40.0, "texture": "ad_astra:textures/environment/glacio.png"}],
            "star_colors": [{"data": -1, "weight": 3}, {"data": -22118, "weight": 1}],
            "stars": 2500, "sunrise_angle": 0, "sunrise_color": 14180147}
    return out


def lang():
    ru = {f"planet.{M}.{k}": v["ru"] for k, v in PLANETS.items()}
    en = {f"planet.{M}.{k}": v["en"] for k, v in PLANETS.items()}
    ru.update({f"planet.{M}.{k}_orbit": f"Орбита: {v['ru']}" for k, v in PLANETS.items()})
    en.update({f"planet.{M}.{k}_orbit": f"{v['en']} Orbit" for k, v in PLANETS.items()})
    ru.update({f"biome.{M}.{k}": v["ru"] for k, v in PLANETS.items()})
    en.update({f"biome.{M}.{k}": v["en"] for k, v in PLANETS.items()})
    ru[f"solar_system.{M}.yuki_system"] = "Система Юки"
    en[f"solar_system.{M}.yuki_system"] = "Yuki System"
    return ru, en
