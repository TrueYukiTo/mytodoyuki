#!/usr/bin/env python3
"""
Генератор квестов FTB Quests для YukiWorld («Легенда о Юки», см. yukigen/questbook.py).

Пишет yukiworld/pack/config/ftbquests/quests/ :
  data.snbt, chapter_groups.snbt, chapters/*.snbt, lang/ru_ru.snbt, lang/en_us.snbt

Все квесты идут строго по очереди: каждый зависит от предыдущего, первая квест главы — от
последнего квеста прошлой главы. ID детерминированы — прогресс не сбрасывается при пересборке.

Запуск:  python3 yukiworld/tools/gen_quests.py [items.txt для проверки ID]
"""
import hashlib
import math
import sys
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from yukigen.questbook import CHAPTERS as CH2  # noqa: E402
from yukigen.questbook3 import CHAPTERS3, GROUPS, ORDER  # noqa: E402

from yukigen.questbook4 import CHAPTERS4, GROUP as GROUP4, INSERT_AFTER  # noqa: E402

from yukigen.questbook5 import CHAPTERS5, GROUP5, INSERT_AFTER5  # noqa: E402
from yukigen.questbook6 import CHAPTERS6, GROUP6, INSERT_AFTER6  # noqa: E402
from yukigen.questbook7 import CHAPTERS7, GROUP7, INSERT_AFTER7  # noqa: E402

INSERT_AFTER = {**INSERT_AFTER, **INSERT_AFTER5, **INSERT_AFTER6, **INSERT_AFTER7}
GROUP4 = {**GROUP4, **GROUP5, **GROUP6, **GROUP7}
for _k, _after in INSERT_AFTER.items():
    _i = [k for k, _ in ORDER].index(_after)
    ORDER.insert(_i + 1, (_k, GROUP4[_k]))
_BY_KEY = {c[0]: c for c in CH2 + CHAPTERS3 + CHAPTERS4 + CHAPTERS5 + CHAPTERS6 + CHAPTERS7}
CHAPTERS = [_BY_KEY[k] for k, _ in ORDER]
GROUP_OF = dict(ORDER)
assert len(CHAPTERS) == len(_BY_KEY), set(_BY_KEY) - {k for k, _ in ORDER}

OUT = Path(__file__).resolve().parent.parent / "pack" / "config" / "ftbquests" / "quests"
YUKI_MODELS = Path(__file__).resolve().parent.parent / "yukicore" / "src" / "main" / "resources" / "assets" \
    / "yukicore" / "models" / "item"
PER_ROW = 7


def qid(key: str) -> str:
    """Положительный 64-битный ID (старший разряд 1..7): FTB Quests переназначает «отрицательные» ID,
    и тогда у квеста теряется перевод."""
    h = hashlib.sha1(("yukiworld:" + key).encode()).hexdigest()[:16].upper()
    return "1234567"[int(h[0], 16) % 7] + h[1:]


@dataclass
class Long:
    v: int


def snbt(v, ind=0) -> str:
    pad, pad1 = "\t" * ind, "\t" * (ind + 1)
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, int):
        return str(v)
    if isinstance(v, float):
        return f"{v}d"
    if isinstance(v, Long):
        return f"{v.v}L"
    if isinstance(v, str):
        return '"' + v.replace("\\", "\\\\").replace('"', '\\"') + '"'
    if isinstance(v, list):
        if not v:
            return "[ ]"
        return "[\n" + "\n".join(pad1 + snbt(x, ind + 1) for x in v) + "\n" + pad + "]"
    if isinstance(v, dict):
        if not v:
            return "{ }"
        lines = []
        for k in sorted(v):
            key = k if all(c.isalnum() or c == "_" for c in k) else snbt(k)
            lines.append(f"{pad1}{key}: {snbt(v[k], ind + 1)}")
        return "{\n" + "\n".join(lines) + "\n" + pad + "}"
    raise TypeError(type(v))


# Оформление квестов по типу
STYLE = {
    "story": {"shape": "hexagon", "size": 1.35, "color": "&b", "subtitle": "📜 Письмо и сюжет"},
    "boss": {"shape": "diamond", "size": 1.45, "color": "&c", "subtitle": "⚔ Босс"},
    "world": {"shape": "rsquare", "size": 1.25, "color": "&a", "subtitle": "🌍 Новый мир"},
    "final": {"shape": "gear", "size": 2.0, "color": "&6&l", "subtitle": "⭐ Финал главы · подарки"},
    "item": {"shape": "", "size": 1.0, "color": "&f", "subtitle": "❄ Задание"},
}
CHAPTER_ART = {  # глава -> баннер (по акту / особые)
    "sintara": "sintara", "newyear": "newyear", "celestia": "finale", "finale": "finale",
    "elements": "sintara", "trials": "sintara", "mastery": "finale", "eternal": "finale", "archmage": "finale",
}


def chapter_images(key, count, icon):
    """Баннер над главой и снежинки/звёзды в промежутках между рядами."""
    import random as _r
    rows = (count + PER_ROW - 1) // PER_ROW
    art = CHAPTER_ART.get(key, "banner")
    width = (PER_ROW - 1) * 1.75
    imgs = [{"x": round(width / 2, 2), "y": -2.6, "width": 9.0, "height": 2.25, "rotation": 0.0,
             "image": f"yukicore:textures/quests/{art}.png", "order": -1}]
    rng = _r.Random(key)
    deco = ["snowflake", "snowflake", "star", "tree"] if key != "sintara" else ["star", "sintara_star"]
    for row in range(rows):
        for _ in range(2):
            side = rng.choice((-1, 1))
            x = (-1.6 if side < 0 else width + 1.6) + rng.uniform(-0.3, 0.3)
            y = row * 2.1 + rng.uniform(-0.6, 0.6)
            sz = rng.uniform(0.7, 1.2)
            imgs.append({"x": round(x, 2), "y": round(y, 2), "width": round(sz, 2), "height": round(sz, 2),
                         "rotation": round(rng.uniform(-25, 25), 1),
                         "image": f"yukicore:textures/quests/{rng.choice(deco)}.png", "order": -1})
    return imgs


def item(stack_id, count=1, components=None):
    d = {"id": stack_id, "count": count}
    if components:
        d["components"] = components
    return d


def main():
    known = set(Path(sys.argv[1]).read_text().split()) if len(sys.argv) > 1 else None
    entities = None
    if len(sys.argv) > 2:
        entities = {line.split("\t")[0] for line in Path(sys.argv[2]).read_text().splitlines()}
        entities |= {"yukicore:" + b for b in ("frost_colossus", "blizzard_queen", "ash_lord", "void_wyrm",
                                               "shadow_of_yuki")}
    dims = {"minecraft:the_nether", "minecraft:the_end", "twilightforest:twilight_forest", "aether:the_aether",
            "the_bumblezone:the_bumblezone", "undergarden:undergarden", "eternal_starlight:starlight",
            "ad_astra:moon", "ad_astra:mars", "ad_astra:venus", "ad_astra:mercury", "ad_astra:glacio",
            "yukicore:yukia", "yukicore:aurora", "yukicore:pyrolith", "yukicore:nyx", "yukicore:celestia"}
    yuki = {p.stem for p in YUKI_MODELS.glob("*.json")}
    missing = []

    def check(i):
        ns, path = i.split(":", 1)
        if ns == "minecraft":
            return
        if ns == "yukicore":
            if path not in yuki:
                missing.append(i)
        elif known is not None and i not in known:
            missing.append(i)

    lang = {}
    (OUT / "chapters").mkdir(parents=True, exist_ok=True)
    for old in (OUT / "chapters").glob("*.snbt"):
        old.unlink()

    prev = None
    total = 0
    for order, (key, title, subtitle, icon, base_xp, steps) in enumerate(CHAPTERS):
        cid = qid("chapter/" + key)
        lang[f"chapter.{cid}.title"] = title
        lang[f"chapter.{cid}.chapter_subtitle"] = [subtitle]
        check(icon)
        quests = []
        for n, (task, count, name, desc, reward) in enumerate(steps):
            qkey = f"{key}/{n:02d}"
            id_ = qid(qkey)
            tid = qid(qkey + "/task")
            if isinstance(task, tuple) and task[0] == "check":
                t = {"id": tid, "type": "checkmark"}
                q_icon = task[1]
                shape = "hexagon"
            elif isinstance(task, tuple) and task[0] == "kill":
                if entities is not None and task[1] not in entities and not task[1].startswith("minecraft:"):
                    missing.append("entity " + task[1])
                t = {"id": tid, "type": "kill", "entity": task[1], "value": Long(count)}
                q_icon = "minecraft:netherite_sword"
                shape = "diamond"
            elif isinstance(task, tuple) and task[0] == "dim":
                if task[1] not in dims:
                    missing.append("dimension " + task[1])
                t = {"id": tid, "type": "dimension", "dimension": task[1]}
                q_icon = "minecraft:ender_eye"
                shape = "rsquare"
            else:
                check(task)
                t = {"id": tid, "type": "item", "item": item(task), "count": Long(count)}
                q_icon = task
                shape = ""
            check(q_icon)

            rewards = [{"id": qid(qkey + "/xp"), "type": "xp", "xp": base_xp + n * 5}]
            if n == len(steps) - 1:  # финал главы — новогодние подарки
                rewards.append({"id": qid(qkey + "/gifts"), "type": "item",
                                "item": item("yukicore:gift"), "count": 3})
            if reward:
                stack = item(reward[0], 1, reward[2] if len(reward) > 2 else None)
                check(reward[0])
                rewards.append({"id": qid(qkey + "/item"), "type": "item", "item": stack, "count": reward[1]})

            last = n == len(steps) - 1
            kind = ("final" if last else "story" if shape == "hexagon" else "boss" if shape == "diamond"
                    else "world" if shape == "rsquare" else "item")
            row, col = divmod(n, PER_ROW)
            x = col if row % 2 == 0 else PER_ROW - 1 - col  # змейка
            wave = 0.45 * math.sin(x * 0.9 + row * 1.3)          # мягкая волна вместо ровной сетки
            quest = {"id": id_, "x": round(x * 1.75, 3), "y": round(row * 2.1 + wave, 3), "tasks": [t],
                     "rewards": rewards, "dependencies": [prev] if prev else [], "icon": item(q_icon)}
            style = STYLE[kind]
            if style["shape"]:
                quest["shape"] = style["shape"]
            if style["size"] != 1.0:
                quest["size"] = style["size"]
            quests.append(quest)
            lang[f"quest.{id_}.title"] = style["color"] + name
            lang[f"quest.{id_}.quest_subtitle"] = style["subtitle"]
            if desc:
                lang[f"quest.{id_}.quest_desc"] = desc.split("\n")
            prev = id_
            total += 1

        chapter = {"id": cid, "filename": key, "group": qid("group/" + GROUP_OF[key]), "order_index": order, "icon": item(icon),
                   "default_quest_shape": "", "default_hide_dependency_lines": False,
                   "quest_links": [], "images": chapter_images(key, len(steps), icon), "quests": quests}
        (OUT / "chapters" / f"{key}.snbt").write_text(snbt(chapter) + "\n", encoding="utf-8")

    data = {"version": 13, "default_reward_team": False, "default_consume_items": False,
            "default_autoclaim_rewards": "disabled", "default_quest_shape": "circle",
            "default_quest_disable_jei": False, "emergency_items_cooldown": 300, "drop_loot_crates": False,
            "disable_gui": False, "grid_scale": 0.5, "pause_game": False, "lock_message": "",
            "progression_mode": "linear", "detection_delay": 20, "fallback_locale": "ru_ru",
            "icon": item("yukicore:yuki_infinity_star")}
    (OUT / "data.snbt").write_text(snbt(data) + "\n", encoding="utf-8")
    groups = []
    for gk, title in GROUPS.items():
        gid = qid("group/" + gk)
        groups.append({"id": gid})
        lang[f"chapter_group.{gid}.title"] = title
    (OUT / "chapter_groups.snbt").write_text(snbt({"chapter_groups": groups}) + "\n", encoding="utf-8")
    (OUT / "lang").mkdir(exist_ok=True)
    for old in (OUT / "lang").glob("*.snbt"):
        old.unlink()
    # Сюжет написан на русском; en_us — та же таблица, чтобы у английского клиента не было пустых квестов
    for loc in ("ru_ru", "en_us"):
        (OUT / "lang" / f"{loc}.snbt").write_text(snbt(lang) + "\n", encoding="utf-8")

    print(f"{len(CHAPTERS)} chapters, {total} quests -> {OUT}")
    if missing:
        print("UNKNOWN ITEMS:", sorted(set(missing)))
        sys.exit(1)


if __name__ == "__main__":
    main()
