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
import sys
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from yukigen.questbook import CHAPTERS  # noqa: E402

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


def item(stack_id, count=1, components=None):
    d = {"id": stack_id, "count": count}
    if components:
        d["components"] = components
    return d


def main():
    known = set(Path(sys.argv[1]).read_text().split()) if len(sys.argv) > 1 else None
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
                t = {"id": tid, "type": "kill", "entity": task[1], "value": Long(count)}
                q_icon = "minecraft:netherite_sword"
                shape = "diamond"
            elif isinstance(task, tuple) and task[0] == "dim":
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
            if reward:
                stack = item(reward[0], 1, reward[2] if len(reward) > 2 else None)
                check(reward[0])
                rewards.append({"id": qid(qkey + "/item"), "type": "item", "item": stack, "count": reward[1]})

            row, col = divmod(n, PER_ROW)
            x = col if row % 2 == 0 else PER_ROW - 1 - col  # змейка
            quest = {"id": id_, "x": float(x * 1.5), "y": float(row * 1.5), "tasks": [t], "rewards": rewards,
                     "dependencies": [prev] if prev else [], "icon": item(q_icon)}
            last = n == len(steps) - 1
            if shape or last:
                quest["shape"] = "gear" if last else shape
            if last or n == 0:
                quest["size"] = 1.5
            quests.append(quest)
            lang[f"quest.{id_}.title"] = name
            if desc:
                lang[f"quest.{id_}.quest_desc"] = desc.split("\n")
            prev = id_
            total += 1

        chapter = {"id": cid, "filename": key, "group": "", "order_index": order, "icon": item(icon),
                   "default_quest_shape": "", "default_hide_dependency_lines": False,
                   "quest_links": [], "images": [], "quests": quests}
        (OUT / "chapters" / f"{key}.snbt").write_text(snbt(chapter) + "\n", encoding="utf-8")

    data = {"version": 13, "default_reward_team": False, "default_consume_items": False,
            "default_autoclaim_rewards": "disabled", "default_quest_shape": "circle",
            "default_quest_disable_jei": False, "emergency_items_cooldown": 300, "drop_loot_crates": False,
            "disable_gui": False, "grid_scale": 0.5, "pause_game": False, "lock_message": "",
            "progression_mode": "linear", "detection_delay": 20, "fallback_locale": "ru_ru",
            "icon": item("yukicore:yuki_infinity_star")}
    (OUT / "data.snbt").write_text(snbt(data) + "\n", encoding="utf-8")
    (OUT / "chapter_groups.snbt").write_text(snbt({"chapter_groups": []}) + "\n", encoding="utf-8")
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
