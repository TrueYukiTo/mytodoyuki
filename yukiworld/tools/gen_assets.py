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

from yukigen import art, book, lang, recipes  # noqa: E402
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
          "starlight_block", "aurorium_block", "celestium_block", "yuki_lamp"]
ORES = {"frost_ore": ("raw_frost", 1, 2), "yuki_crystal_ore": ("yuki_crystal", 1, 3),
        "starlight_ore": ("starlight_shard", 1, 1)}


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
    items = art.item_textures()
    for name, (img, handheld) in items.items():
        write(ASSETS / "textures" / "item" / f"{name}.png", img)
        write(ASSETS / "models" / "item" / f"{name}.json",
              {"parent": "minecraft:item/handheld" if handheld else "minecraft:item/generated",
               "textures": {"layer0": f"{MOD}:item/{name}"}})

    # ---------------- блоки
    for name, img in art.block_textures(rng).items():
        write(ASSETS / "textures" / "block" / f"{name}.png", img)
        write(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
        write(ASSETS / "models" / "block" / f"{name}.json",
              {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{name}"}})
        write(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"{MOD}:block/{name}"})

    for i, (tier, pal) in enumerate(art.TIERS.items()):
        for layer in (1, 2):
            write(ASSETS / "textures" / "models" / "armor" / f"{tier}_layer_{layer}.png",
                  art.armor_layer(layer, pal, i * 10))

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
    write(ASSETS / "lang" / "ru_ru.json", lang.RU)
    write(ASSETS / "lang" / "en_us.json", lang.EN)
    missing_names = [n for n in list(items) + BLOCKS
                     if f"item.{MOD}.{n}" not in lang.RU and f"block.{MOD}.{n}" not in lang.RU]
    assert not missing_names, missing_names

    # ---------------- лут блоков
    lt = DATA / MOD / "loot_table" / "blocks"
    for b in BLOCKS:
        write(lt / f"{b}.json", ore_loot(b, *ORES[b]) if b in ORES else self_loot(b))

    # ---------------- рецепты
    rec = recipes.build()
    for name, r in rec.items():
        write(DATA / MOD / "recipe" / f"{name}.json", r)
    if known is not None:
        ours = {I(n) for n in list(items) + BLOCKS}
        bad = sorted(i for i in recipes.ingredient_ids(rec)
                     if not i.startswith("minecraft:") and i not in known and i not in ours)
        assert not bad, f"unknown items in recipes: {bad}"

    # ---------------- теги
    tags = DATA / "minecraft" / "tags"
    write(tags / "block" / "mineable" / "pickaxe.json", {"values": [I(b) for b in BLOCKS]})
    write(tags / "block" / "needs_diamond_tool.json",
          {"values": [I(b) for b in ("frost_ore", "yuki_crystal_ore", "starlight_ore", "aurorium_block",
                                     "celestium_block", "starlight_block")]})
    write(tags / "block" / "needs_iron_tool.json", {"values": [I("frost_block"), I("yuki_crystal_block")]})
    write(tags / "block" / "beacon_base_blocks.json",
          {"values": [I(b) for b in ("frost_block", "aurorium_block", "celestium_block")]})
    tool_tags = {"sword": "swords", "pickaxe": "pickaxes", "axe": "axes", "shovel": "shovels", "hoe": "hoes",
                 "helmet": "head_armor", "chestplate": "chest_armor", "leggings": "leg_armor", "boots": "foot_armor"}
    for piece, tag in tool_tags.items():
        vals = [I(f"{t}_{piece}") for t in art.TIERS]
        if piece == "sword":
            vals.append(I("yuki_blade"))
        write(tags / "item" / f"{tag}.json", {"values": vals})
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
    write(DATA / "neoforge" / "loot_modifiers" / "global_loot_modifiers.json", {"replace": False, "entries": entries})

    # ---------------- книга Patchouli
    for rel, content in book.files().items():
        write(ROOT / rel, content)

    write(ROOT / "pack.mcmeta", {"pack": {"description": "YukiCore resources", "pack_format": 34}})
    print(f"{len(items)} items, {len(BLOCKS)} blocks, {len(rec)} recipes -> {ROOT}")


if __name__ == "__main__":
    main()
