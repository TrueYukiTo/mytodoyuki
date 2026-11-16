#!/usr/bin/env python3
"""
Генератор ресурсов YukiCore: текстуры, модели, blockstates, лут, рецепты, теги,
генерация руд в Энде, осколки звезды с боссов, книга Patchouli и локализация.

Запуск:  python3 yukiworld/tools/gen_assets.py [items.txt — список ID для проверки рецептов]
Нужен Pillow. Всё сгенерированное в yukicore/src/main/resources перезаписывается.
"""
import json
import random
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from yukigen import art, book, hd, lang, models, planets, recipes, v6, v7  # noqa: E402
from yukigen.art import CRYSTAL, SAKURA, TIERS  # noqa: E402
from yukigen.pixel import gui_button, gui_handle, logo, nine_slice  # noqa: E402

MOD = "yukicore"
ROOT = Path(__file__).resolve().parent.parent / "yukicore" / "src" / "main" / "resources"
ASSETS = ROOT / "assets" / MOD
DATA = ROOT / "data"


def I(n):  # noqa: E743
    return f"{MOD}:{n}"


def write(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    if hasattr(data, "save"):
        data.save(path)
    else:
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


BLOCKS = ["frost_ore", "yuki_crystal_ore", "starlight_ore", "frost_block", "yuki_crystal_block",
          "starlight_block", "aurorium_block", "celestium_block", "yuki_lamp",
          "permafrost_ore", "aurora_ore", "pyrolite_ore", "void_ore", "stellar_ore",
          "permafrost_block", "aurora_block", "pyrolite_block", "void_block", "stellar_block"]
V5_BLOCKS = ["frost_casing", "cryosteel_block", "reactor_casing", "frost_forge", "star_reactor", "aurora_lamp"]
SPECIAL_BLOCKS = V5_BLOCKS + ["yuki_altar", "yuki_pedestal", "yuki_chrono", "cryo_generator", "festive_garland",
                  "christmas_ornament", "yuki_tree", "snow_globe"]
ORES = {"frost_ore": ("raw_frost", 1, 2), "yuki_crystal_ore": ("yuki_crystal", 1, 3),
        "starlight_ore": ("starlight_shard", 1, 1), "permafrost_ore": ("permafrost_crystal", 1, 3),
        "aurora_ore": ("aurora_shard", 1, 2), "pyrolite_ore": ("raw_pyrolite", 1, 2),
        "void_ore": ("void_shard", 1, 1), "stellar_ore": ("stellar_shard", 1, 1)}

BOSS_LOOT = {  # босс: [(предмет, min, max)]
    "frost_colossus": [("colossus_heart", 1, 1), ("star_shard", 1, 2), ("permafrost_crystal", 4, 8)],
    "blizzard_queen": [("blizzard_crown", 1, 1), ("star_shard", 1, 2), ("aurora_shard", 4, 8)],
    "ash_lord": [("ash_core", 1, 1), ("star_shard", 2, 2), ("pyrolite_ingot", 4, 8)],
    "void_wyrm": [("void_scale", 1, 1), ("star_shard", 2, 3), ("void_shard", 4, 8)],
    "shadow_of_yuki": [("yuki_tear", 1, 1), ("star_shard", 4, 4), ("stellar_shard", 6, 10)],
}
VANILLA_CLIENT = Path("/root/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar")
ADASTRA_JAR = Path("/tmp/claude-0/-home-user-mytodoyuki/7717ab37-fa34-58cc-9896-ef064fd55632/scratchpad/jars/adastra-1.21.1-1.16.26-neoforge.jar")
JAVA_BOSS = Path(__file__).resolve().parent.parent / "yukicore" / "src" / "main" / "java" / "dev" / "yukito" / "yukicore" / "client" / "boss" / "BossLayers.java"
ADASTRA_DATA = Path("/tmp/claude-0/-home-user-mytodoyuki/7717ab37-fa34-58cc-9896-ef064fd55632/scratchpad/aadata/data/ad_astra")


def ore_loot(block, drop, lo, hi):
    fns = []
    if (lo, hi) != (1, 1):
        fns.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    fns += [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
            {"function": "minecraft:explosion_decay"}]
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    return {"type": "minecraft:block", "random_sequence": f"{MOD}:blocks/{block}", "pools": [{
        "rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": I(block), "conditions": [silk]},
            {"type": "minecraft:item", "name": I(drop), "functions": fns}]}]}]}


def self_loot(block):
    return {"type": "minecraft:block", "random_sequence": f"{MOD}:blocks/{block}", "pools": [{
        "rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": I(block)}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


END_BIOMES = ["minecraft:end_highlands", "minecraft:end_midlands", "minecraft:small_end_islands",
              "minecraft:end_barrens"]


def end_ore(feature, block, size, count, lo, hi, air_discard=0.0):
    configured = {"type": "minecraft:ore", "config": {
        "size": size, "discard_chance_on_air_exposure": air_discard,
        "targets": [{"target": {"predicate_type": "minecraft:block_match", "block": "minecraft:end_stone"},
                     "state": {"Name": I(block)}}]}}
    placed = {"feature": I(feature), "placement": [
        {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                                                      "min_inclusive": {"absolute": lo},
                                                      "max_inclusive": {"absolute": hi}}},
        {"type": "minecraft:biome"}]}
    modifier = {"type": "neoforge:add_features", "biomes": END_BIOMES, "features": I(feature),
                "step": "underground_ores"}
    return configured, placed, modifier


BOSS_SHARDS = {  # loot table -> (min, max)
    "minecraft:entities/wither": (1, 2),
    "minecraft:entities/warden": (1, 1),
    "minecraft:entities/elder_guardian": (1, 1),
}


def main():
    known = set(Path(sys.argv[1]).read_text().split()) if len(sys.argv) > 1 else None
    rng = random.Random(1337)

    # чистим старое сгенерированное (META-INF не трогаем)
    for p in [ROOT / "assets", ROOT / "data"]:
        shutil.rmtree(p, ignore_errors=True)

    # ---------------- предметы
    items = art.hd_textures()
    items.update(art.hd_textures_v4())
    items.update(art.hd_textures_v5())
    items.update(v6.item_textures())
    items.update(v7.item_textures())
    for name, (img, handheld) in items.items():
        write(ASSETS / "textures" / "item" / f"{name}.png", img)
        write(ASSETS / "models" / "item" / f"{name}.json",
              {"parent": "minecraft:item/handheld" if handheld else "minecraft:item/generated",
               "textures": {"layer0": f"{MOD}:item/{name}"}})

    # ---------------- блоки
    for name, img in art.hd_blocks().items():
        write(ASSETS / "textures" / "block" / f"{name}.png", img)
        write(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
        write(ASSETS / "models" / "block" / f"{name}.json",
              {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{name}"}})
        write(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"{MOD}:block/{name}"})

    for i, (tier, pal) in enumerate(list(TIERS.items()) + [("yuki", art.GOLDPINK)] + list(v6.ARMOR_LAYERS.items()) + list(v7.ARMOR_LAYERS.items())):
        for layer in (1, 2):
            write(ASSETS / "textures" / "models" / "armor" / f"{tier}_layer_{layer}.png",
                  hd.armor_layer_hd(layer, pal, i * 10))

    # алтарь и пьедестал: модель из трёх кубоидов
    top, side, bottom = hd.altar_textures(CRYSTAL, CRYSTAL)
    ptop, pside, _ = hd.altar_textures(CRYSTAL, SAKURA)
    for name, (t, sd, b) in {"yuki_altar": (top, side, bottom), "yuki_pedestal": (ptop, pside, bottom)}.items():
        for part, img in (("top", t), ("side", sd), ("bottom", b)):
            write(ASSETS / "textures" / "block" / f"{name}_{part}.png", img)
        tex = {"top": f"{MOD}:block/{name}_top", "side": f"{MOD}:block/{name}_side",
               "bottom": f"{MOD}:block/{name}_bottom", "particle": f"{MOD}:block/{name}_side"}

        def cube(f, t_):
            faces = {d: {"texture": "#side", "uv": [f[0], 16 - t_[1], t_[0], 16 - f[1]]}
                     for d in ("north", "south", "east", "west")}
            faces["up"] = {"texture": "#top", "uv": [f[0], f[2], t_[0], t_[2]]}
            faces["down"] = {"texture": "#bottom", "uv": [f[0], f[2], t_[0], t_[2]]}
            return {"from": f, "to": t_, "faces": faces}
        write(ASSETS / "models" / "block" / f"{name}.json", {"parent": "minecraft:block/block", "textures": tex,
              "elements": [cube([1, 0, 1], [15, 3, 15]), cube([4, 3, 4], [12, 11, 12]),
                           cube([2, 11, 2], [14, 14, 14])]})
        write(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
        write(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"{MOD}:block/{name}"})

    # 4.0: машины и новогодние украшения
    v4 = art.hd_blocks_v4()
    for name, img in v4.items():
        write(ASSETS / "textures" / "block" / f"{name}.png", img)
    B = f"{MOD}:block/"
    v4models = {
        "yuki_chrono": {"parent": "minecraft:block/cube_all", "textures": {"all": B + "yuki_chrono"}},
        "cryo_generator": {"parent": "minecraft:block/cube_all", "textures": {"all": B + "cryo_generator"}},
        "festive_garland": {"parent": "minecraft:block/carpet", "render_type": "minecraft:cutout",
                            "textures": {"wool": B + "festive_garland", "particle": B + "festive_garland"}},
        "christmas_ornament": {"parent": "minecraft:block/block", "textures": {"all": B + "christmas_ornament",
                                                                               "particle": B + "christmas_ornament"},
                               "elements": [{"from": [4, 2, 4], "to": [12, 10, 12], "faces": {
                                   d: {"texture": "#all"} for d in ("north", "south", "east", "west", "up", "down")}},
                                   {"from": [7, 10, 7], "to": [9, 12, 9], "faces": {
                                       d: {"texture": "#all", "uv": [0, 7, 2, 9]}
                                       for d in ("north", "south", "east", "west", "up")}}]},
        "yuki_tree": {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout",
                      "textures": {"cross": B + "yuki_tree"}},
        "snow_globe": {"parent": "minecraft:block/block", "render_type": "minecraft:translucent",
                       "textures": {"glass": B + "snow_globe", "base": B + "snow_globe_base", "particle": B + "snow_globe"},
                       "elements": [{"from": [3, 0, 3], "to": [13, 2, 13], "faces": {
                           d: {"texture": "#base"} for d in ("north", "south", "east", "west", "up", "down")}},
                           {"from": [4, 2, 4], "to": [12, 11, 12], "faces": {
                               d: {"texture": "#glass"} for d in ("north", "south", "east", "west", "up")}}]},
    }
    for name, model in v4models.items():
        write(ASSETS / "models" / "block" / f"{name}.json", model)
        write(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
        item_model = ({"parent": "minecraft:item/generated", "textures": {"layer0": B + name}}
                      if name in ("yuki_tree", "festive_garland") else {"parent": B + name})
        write(ASSETS / "models" / "item" / f"{name}.json", item_model)

    # 5.0: мультиструктуры
    for name, img in art.hd_blocks_v5().items():
        write(ASSETS / "textures" / "block" / f"{name}.png", img)
        write(ASSETS / "models" / "block" / f"{name}.json",
              {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{name}"}})
        write(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
        write(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"{MOD}:block/{name}"})

    # 5.0: картинки для книги квестов
    q = ASSETS / "textures" / "quests"
    write(q / "banner.png", hd.quest_banner(0x0B1A3A, 0x2A4A80, 0x8FD8FF, 1))
    write(q / "newyear.png", hd.quest_banner(0x3A0808, 0x0E3A1A, 0xFFD24A, 2, lights=True))
    write(q / "finale.png", hd.quest_banner(0x2A1406, 0x5A3A9A, 0xFFE070, 3))
    write(q / "sintara.png", hd.quest_banner(0x120A30, 0x2A1460, 0xB080FF, 4, aurora=True))
    write(q / "snowflake.png", hd.quest_snowflake())
    write(q / "star.png", hd.quest_star())
    write(q / "sintara_star.png", hd.quest_star(0xB080FF, 0x8FF0D0))
    write(q / "tree.png", hd.quest_tree())

    # 6.0: 3D-модели боссов — Java-слои + HD-текстуры со светящимся слоем
    java_src, boss_tex = models.build()
    JAVA_BOSS.write_text(java_src, encoding="utf-8")
    armor_src, armor_tex = models.build_armor()
    (JAVA_BOSS.parent.parent / "armor" / "ArmorLayers.java").write_text(armor_src, encoding="utf-8")
    for name, (tex, glow) in armor_tex.items():
        write(ASSETS / "textures" / "models" / "armor3d" / f"{name}.png", tex)
        write(ASSETS / "textures" / "models" / "armor3d" / f"{name}_glow.png", glow)
    for boss, (tex, glow) in boss_tex.items():
        write(ASSETS / "textures" / "entity" / "boss" / f"{boss}.png", tex)
        write(ASSETS / "textures" / "entity" / "boss" / f"{boss}_glow.png", glow)

    # 6.0: ракеты (модель Ad Astra 4-го уровня, свои текстуры) и иконки магии
    import zipfile
    from PIL import Image
    with zipfile.ZipFile(ADASTRA_JAR) as z:
        with z.open("assets/ad_astra/textures/entity/rocket/tier_4_rocket.png") as f:
            rocket_src = Image.open(f).convert("RGBA").copy()
        rocket_model = json.loads(z.read("assets/ad_astra/models/item/tier_4_rocket.json"))
    for name, stops in v6.ROCKET_STOPS.items():
        write(ASSETS / "textures" / "entity" / "rocket" / f"{name}.png", v6.rocket_texture(rocket_src, stops))
        write(ASSETS / "models" / "item" / f"{name}.json", rocket_model)
    icons = v7.skill_icons()
    for name, img in icons.items():
        write(ASSETS / "textures" / "gui" / "spells" / f"{name}.png", img)
    for name, img in v7.effect_icons(icons).items():
        write(ASSETS / "textures" / "mob_effect" / f"{name}.png", img)

    # ---------------- GUI всей игры
    gui = ROOT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "widget"
    for fname, img, meta in [
        ("button", gui_button(200, 20, 0x111A2E, 0x4F7FB8, 0x8FD8FF), nine_slice(200, 20, 3)),
        ("button_highlighted", gui_button(200, 20, 0x22385E, 0xBFEFFF, 0xFFB3E6, 235), nine_slice(200, 20, 3)),
        ("button_disabled", gui_button(200, 20, 0x15181F, 0x30343C, 0x30343C, 180), nine_slice(200, 20, 3)),
        ("slider", gui_button(200, 20, 0x0C1222, 0x3A5A86, 0x3A5A86), nine_slice(200, 20, 3)),
        ("slider_highlighted", gui_button(200, 20, 0x14203A, 0x8FD8FF, 0x8FD8FF), nine_slice(200, 20, 3)),
        ("slider_handle", gui_handle(8, 20, 0x8FD8FF, 0x3A7BD5), nine_slice(8, 20, 2)),
        ("slider_handle_highlighted", gui_handle(8, 20, 0xFFE6F7, 0xFF9EDC), nine_slice(8, 20, 2)),
        ("text_field", gui_button(200, 20, 0x05080F, 0x3A5A86, 0x05080F, 235), nine_slice(200, 20, 1)),
        ("text_field_highlighted", gui_button(200, 20, 0x05080F, 0x8FD8FF, 0x05080F, 235), nine_slice(200, 20, 1)),
    ]:
        write(gui / f"{fname}.png", img)
        write(gui / f"{fname}.png.mcmeta", meta)
    write(ROOT / "yukicore_logo.png", logo())

    # ---------------- локализация
    pru, pen = planets.lang()
    lang.RU.update(pru)
    lang.EN.update(pen)
    vru, ven = v6.lang()
    lang.RU.update(vru)
    lang.EN.update(ven)
    vru, ven = v7.lang()
    lang.RU.update(vru)
    lang.EN.update(ven)
    write(ASSETS / "lang" / "ru_ru.json", lang.RU)
    write(ASSETS / "lang" / "en_us.json", lang.EN)
    missing_names = [n for n in list(items) + BLOCKS + SPECIAL_BLOCKS
                     if f"item.{MOD}.{n}" not in lang.RU and f"block.{MOD}.{n}" not in lang.RU]
    assert not missing_names, missing_names

    # ---------------- лут блоков
    lt = DATA / MOD / "loot_table" / "blocks"
    for b in BLOCKS + SPECIAL_BLOCKS:
        write(lt / f"{b}.json", ore_loot(b, *ORES[b]) if b in ORES else self_loot(b))
    for boss, drops in {**BOSS_LOOT, **v6.BOSS_LOOT}.items():
        drops = drops + v6.BOSS_EXTRA.get(boss, []) + v7.BOSS_EXTRA.get(boss, [])
        write(DATA / MOD / "loot_table" / "entities" / f"{boss}.json", {"type": "minecraft:entity", "pools": [
            {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": I(item), "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}]}
            for item, lo, hi in drops]})

    # ---------------- рецепты
    rec = recipes.build()
    rec.update(v6.recipes())
    rec.update(v7.recipes())
    for name, r in rec.items():
        write(DATA / MOD / "recipe" / f"{name}.json", r)
    for rel, r in recipes.overrides().items():
        write(DATA / rel, r)
    if known is not None:
        ours = {I(n) for n in list(items) + BLOCKS + SPECIAL_BLOCKS + v6.ROCKETS}
        bad = sorted(i for i in recipes.ingredient_ids(rec)
                     if not i.startswith("minecraft:") and i not in known and i not in ours)
        assert not bad, f"unknown items in recipes: {bad}"

    # ---------------- теги
    tags = DATA / "minecraft" / "tags"
    write(tags / "block" / "mineable" / "pickaxe.json", {"values": [I(b) for b in BLOCKS + SPECIAL_BLOCKS]})
    write(tags / "block" / "needs_diamond_tool.json",
          {"values": [I(b) for b in ("frost_ore", "yuki_crystal_ore", "starlight_ore", "aurorium_block",
                                     "celestium_block", "starlight_block", "permafrost_ore", "aurora_ore",
                                     "pyrolite_ore", "void_ore", "stellar_ore")]})
    write(tags / "block" / "needs_iron_tool.json", {"values": [I("frost_block"), I("yuki_crystal_block")]})
    write(tags / "block" / "beacon_base_blocks.json",
          {"values": [I(b) for b in ("frost_block", "aurorium_block", "celestium_block")]})
    tool_tags = {"sword": "swords", "pickaxe": "pickaxes", "axe": "axes", "shovel": "shovels", "hoe": "hoes",
                 "helmet": "head_armor", "chestplate": "chest_armor", "leggings": "leg_armor", "boots": "foot_armor"}
    for piece, tag in tool_tags.items():
        vals = [I(f"{t}_{piece}") for t in TIERS]
        if piece == "sword":
            vals += [I("yuki_blade"), I("eternal_winter_blade")]
        if piece == "helmet":
            vals.append(I("crown_of_yuki"))
        idx = ["helmet", "chestplate", "leggings", "boots"].index(piece) if piece in ("helmet", "chestplate", "leggings", "boots") else -1
        if idx >= 0:
            vals += [I(v6.ROBES[idx]), I(v6.ROBES[4 + idx]), I(v6.SUIT_ITEMS[idx]), I(v6.SUIT_ITEMS[4 + idx]),
                     I(v6.SUIT_ITEMS[8 + idx]), I(v7.NEW_ARMOR[idx]), I(v7.NEW_ARMOR[4 + idx]), I(v7.NEW_ARMOR[8 + idx])]
        write(tags / "item" / f"{tag}.json", {"values": vals})
    aa_tags = DATA / "ad_astra" / "tags" / "item"
    write(aa_tags / "space_suit_items.json", {"replace": False, "values": [I(n) for n in v6.SUIT_ITEMS]})
    write(aa_tags / "heat_resistant_armor.json", {"replace": False,
                                                  "values": [I(n) for n in v6.SUIT_ITEMS if not n.startswith("frost")]})
    write(tags / "item" / "beacon_payment_items.json",
          {"values": [I("frost_ingot"), I("aurorium_ingot"), I("celestium_ingot")]})

    common = DATA / "c" / "tags"
    for metal in ("frost", "aurorium", "celestium"):
        write(common / "item" / "ingots" / f"{metal}.json", {"values": [I(f"{metal}_ingot")]})
        write(common / "item" / "storage_blocks" / f"{metal}.json", {"values": [I(f"{metal}_block")]})
        write(common / "block" / "storage_blocks" / f"{metal}.json", {"values": [I(f"{metal}_block")]})
    write(common / "item" / "ingots.json", {"values": [I(f"{m}_ingot") for m in ("frost", "aurorium", "celestium")]})
    write(common / "item" / "gems.json", {"values": [I("yuki_crystal"), I("starlight_shard")]})
    write(common / "item" / "plates" / "frost.json", {"values": [I("frost_plate")]})
    write(common / "item" / "raw_materials" / "frost.json", {"values": [I("raw_frost")]})
    ores = [I(o) for o in ORES]
    write(common / "block" / "ores.json", {"values": ores})
    write(common / "item" / "ores.json", {"values": ores})
    write(common / "block" / "ores_in_ground" / "end_stone.json", {"values": ores})

    # ---------------- руды в Энде (только внешние острова)
    wg = DATA / MOD / "worldgen"
    bm = DATA / MOD / "neoforge" / "biome_modifier"
    for feature, block, size, count, lo, hi, air in [
            ("ore_frost_end", "frost_ore", 9, 14, 0, 100, 0.0),
            ("ore_yuki_crystal_end", "yuki_crystal_ore", 6, 9, 0, 100, 0.1),
            ("ore_starlight_end", "starlight_ore", 5, 7, 0, 90, 0.15)]:
        c, p, m = end_ore(feature, block, size, count, lo, hi, air)
        write(wg / "configured_feature" / f"{feature}.json", c)
        write(wg / "placed_feature" / f"{feature}.json", p)
        write(bm / f"{feature}.json", m)

    # ---------------- осколки звезды с боссов (Global Loot Modifiers)
    entries = []
    for table, (lo, hi) in BOSS_SHARDS.items():
        boss = table.split("/")[-1]
        write(DATA / MOD / "loot_table" / "bonus" / f"star_shard_{boss}.json", {
            "type": "minecraft:entity", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{
                "type": "minecraft:item", "name": I("star_shard"),
                "functions": [{"function": "minecraft:set_count",
                               "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}],
                "conditions": [{"condition": "minecraft:killed_by_player"}]}]})
        write(DATA / MOD / "loot_modifiers" / f"star_shard_{boss}.json", {
            "type": "neoforge:add_table",
            "conditions": [{"condition": "neoforge:loot_table_id", "loot_table_id": table}],
            "table": f"{MOD}:bonus/star_shard_{boss}"})
        entries.append(f"{MOD}:star_shard_{boss}")
    # 4.0: подарки и мандарины
    def chance_table(item, chance, lo=1, hi=1):
        return {"type": "minecraft:generic", "pools": [{"rolls": 1, "bonus_rolls": 0,
                "conditions": [{"condition": "minecraft:random_chance", "chance": chance}],
                "entries": [{"type": "minecraft:item", "name": item, "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}]}]}
    for key, table, item, chance in ([("gift_stray", "minecraft:entities/stray", I("gift"), 0.05),
                                      ("gift_snow_golem", "minecraft:entities/snow_golem", I("gift"), 0.08),
                                      ("tangerine_spruce", "minecraft:blocks/spruce_leaves", I("tangerine"), 0.03)]
                                     + [(k, t, I(it), c) for k, t, it, c in v6.ESSENCE_DROPS]):
        write(DATA / MOD / "loot_table" / "bonus" / f"{key}.json", chance_table(item, chance))
        write(DATA / MOD / "loot_modifiers" / f"{key}.json", {
            "type": "neoforge:add_table",
            "conditions": [{"condition": "neoforge:loot_table_id", "loot_table_id": table}],
            "table": f"{MOD}:bonus/{key}"})
        entries.append(f"{MOD}:{key}")
    write(DATA / "neoforge" / "loot_modifiers" / "global_loot_modifiers.json", {"replace": False, "entries": entries})

    def gift_table(rolls, pool):
        return {"type": "minecraft:gift", "pools": [{"rolls": rolls, "bonus_rolls": 0, "entries": [
            {"type": "minecraft:item", "name": it, "weight": w, "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": a, "max": b}}]}
            for it, w, a, b in pool]}]}
    write(DATA / MOD / "loot_table" / "gameplay" / "gift.json", gift_table(3, [
        (I("tangerine"), 30, 3, 8), (I("candy_cane"), 25, 2, 5), (I("gingerbread"), 20, 2, 4),
        (I("hot_cocoa"), 12, 1, 2), ("minecraft:iron_ingot", 20, 4, 12), ("minecraft:gold_ingot", 12, 3, 8),
        ("minecraft:diamond", 6, 1, 3), ("minecraft:emerald", 8, 2, 6), ("minecraft:experience_bottle", 10, 4, 12),
        (I("festive_garland"), 10, 4, 8), (I("christmas_ornament"), 8, 2, 4), (I("yuki_crystal"), 4, 1, 3),
        ("minecraft:golden_apple", 3, 1, 1)]))
    write(DATA / MOD / "loot_table" / "gameplay" / "grand_gift.json", gift_table(5, [
        ("minecraft:diamond", 20, 3, 8), ("minecraft:netherite_scrap", 8, 1, 2), ("minecraft:emerald_block", 10, 1, 3),
        (I("frost_ingot"), 12, 2, 6), (I("yuki_crystal"), 12, 3, 8), (I("yuki_essence"), 8, 1, 3),
        (I("starlight_shard"), 4, 1, 2), (I("star_shard"), 2, 1, 1), ("minecraft:enchanted_golden_apple", 2, 1, 1),
        (I("olivier"), 15, 1, 3), (I("yuki_tree"), 10, 1, 1), (I("snow_globe"), 10, 1, 1),
        ("minecraft:totem_of_undying", 3, 1, 1), (I("frost_magnet"), 3, 1, 1)]))

    # ---------------- планеты Системы Юки (Ad Astra)
    for rel, content in planets.files(ADASTRA_DATA).items():
        write(ROOT / rel, content)

    # ---------------- книга Patchouli
    for rel, content in book.files().items():
        write(ROOT / rel, content)

    write(ROOT / "pack.mcmeta", {"pack": {"description": "YukiCore resources", "pack_format": 34}})
    print(f"{len(items)} items, {len(BLOCKS)} blocks, {len(rec)} recipes -> {ROOT}")


if __name__ == "__main__":
    main()
