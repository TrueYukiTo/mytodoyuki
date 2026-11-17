"""YukiCore 7.3: баланс магии (4-й ряд дерева), Казино Синтары 2.0."""
import math

from . import v7

W_, B_ = v7.W_, v7.B_
F1, F2 = (255, 210, 90, 255), (255, 120, 30, 255)

SKILLS_ICE73 = {
    "chill_aura": ("Аура холода", "Chill Aura", "Пассивно: враги в радиусе 4–8 блоков замедляются, на ранге 4+ — сильнее.",
                   "Passive: enemies within 4–8 blocks are slowed, stronger at rank 4+."),
    "ice_lance": ("Ледяное копьё", "Ice Lance", "Огромная сосулька: 70 урона одной цели. С ранга 3 — взрыв осколков.",
                  "A huge icicle: 70 single-target damage. Rank 3+ — shard explosion."),
    "frost_trap": ("Морозная ловушка", "Frost Trap", "Руна на земле: через секунду взрыв 55 урона, заморозка и подброс.",
                   "A rune on the ground: after a second — 55 damage, freeze and knock-up."),
    "frozen_flow": ("Ледяной поток", "Frozen Flow", "Пассивно: ледяные заклинания дешевле на 4% за ранг.",
                    "Passive: ice spells cost 4% less mana per rank."),
    "hailstorm": ("Град", "Hailstorm", "3 секунды с неба падают 30–46 сосулек по области 8 блоков, по 22 урона.",
                  "For 3 seconds 30–46 icicles fall on an 8-block area, 22 damage each."),
    "arctic_gale": ("Арктический шквал", "Arctic Gale", "Ледяной ветер конусом 13–17 блоков: 35 урона, отброс и сильное замедление.",
                    "An icy cone wind 13–17 blocks long: 35 damage, knockback and heavy slow."),
    "cryo_mastery": ("Мастер стужи", "Cryo Mastery", "Пассивно: +6% силы ледяной магии за ранг.",
                     "Passive: +6% ice spell power per rank."),
    "frost_comet": ("Ледяная комета", "Frost Comet", "Глыба льда падает с неба: 420 урона в радиусе 9–11 и долгая заморозка.",
                    "A block of ice falls from the sky: 420 damage in a 9–11 radius and a long freeze."),
}
SKILLS_FIRE73 = {
    "heat_aura": ("Аура жара", "Heat Aura", "Пассивно: раз в 2 секунды поджигает врагов в радиусе 4–8 блоков и наносит урон.",
                  "Passive: every 2 seconds burns and damages enemies within 4–8 blocks."),
    "flame_lance": ("Огненное копьё", "Flame Lance", "Пылающее копьё: 70 урона одной цели. С ранга 3 — взрыв пламени.",
                    "A blazing lance: 70 single-target damage. Rank 3+ — fiery explosion."),
    "fire_mine": ("Огненная мина", "Fire Mine", "Руна на земле: через секунду взрыв 55 урона, поджог и подброс.",
                  "A rune on the ground: after a second — 55 damage, burning and knock-up."),
    "kindling": ("Растопка", "Kindling", "Пассивно: огненные заклинания дешевле на 4% за ранг.",
                 "Passive: fire spells cost 4% less mana per rank."),
    "ash_rain": ("Пепельный дождь", "Ash Rain", "3 секунды с неба падают 30–46 огненных глыб по области 8 блоков, по 22 урона.",
                 "For 3 seconds 30–46 fire rocks fall on an 8-block area, 22 damage each."),
    "heat_wave": ("Тепловая волна", "Heat Wave", "Волна жара конусом 13–17 блоков: 35 урона, отброс и поджог.",
                  "A cone of heat 13–17 blocks long: 35 damage, knockback and burning."),
    "pyro_mastery": ("Мастер пламени", "Pyro Mastery", "Пассивно: +6% силы огненной магии за ранг.",
                     "Passive: +6% fire spell power per rank."),
    "solar_flare": ("Солнечная вспышка", "Solar Flare", "Пылающая глыба падает с неба: 420 урона в радиусе 9–11 и поджог.",
                    "A blazing rock falls from the sky: 420 damage in a 9–11 radius and burning."),
}


def skill_icons():
    icons = {}

    def ico(name, ice, fn, special=False):
        img, d = v7._base(ice, special)
        fn(d)
        icons[name] = img

    ico("chill_aura", True, lambda d: ([d.ellipse([4 + k * 4, 4 + k * 4, 28 - k * 4, 28 - k * 4], outline=W_ if k % 2 else B_, width=1)
                                        for k in range(3)], v7._flake(d, 16, 16, 4, W_, 1)))
    ico("ice_lance", True, lambda d: (d.polygon([(16, 2), (21, 12), (18, 12), (18, 29), (14, 29), (14, 12), (11, 12)], fill=B_, outline=W_),
                                      d.line([9, 16, 23, 16], fill=W_, width=3)))
    ico("frost_trap", True, lambda d: (d.ellipse([5, 14, 27, 26], outline=W_, width=2), d.ellipse([11, 17, 21, 23], outline=B_),
                                       v7._flake(d, 16, 10, 5, W_, 1)))
    ico("frozen_flow", True, lambda d: [d.arc([6, 6 + k * 5, 26, 18 + k * 5], 200, 340, fill=W_ if k % 2 else B_, width=2) for k in range(3)])
    ico("hailstorm", True, lambda d: (d.ellipse([5, 4, 27, 14], fill=(220, 235, 250, 255)),
                                      [d.polygon([(7 + i * 5, 17 + (i % 2) * 3), (9 + i * 5, 17 + (i % 2) * 3), (8 + i * 5, 25 + (i % 2) * 3)],
                                                 fill=B_) for i in range(4)]))
    ico("arctic_gale", True, lambda d: [d.line([4, 10 + k * 6, 26 - k * 2, 8 + k * 7], fill=W_ if k % 2 else B_, width=2) for k in range(3)])
    ico("cryo_mastery", True, lambda d: (v7._flake(d, 16, 16, 12, W_), d.polygon([(16, 9), (19, 16), (16, 23), (13, 16)], fill=(120, 200, 255, 255))),
        special=True)
    ico("frost_comet", True, lambda d: (d.ellipse([14, 14, 28, 28], fill=B_, outline=W_),
                                        [d.line([16 - k * 3, 16 - k * 3, 6 - k * 2, 6 - k * 2], fill=W_, width=2) for k in range(3)]),
        special=True)
    ico("heat_aura", False, lambda d: ([d.ellipse([4 + k * 4, 4 + k * 4, 28 - k * 4, 28 - k * 4], outline=F1 if k % 2 else F2, width=1)
                                        for k in range(3)], d.polygon([(16, 9), (20, 18), (16, 23), (12, 18)], fill=F1)))
    ico("flame_lance", False, lambda d: (d.polygon([(16, 2), (21, 12), (18, 12), (18, 29), (14, 29), (14, 12), (11, 12)], fill=F2, outline=F1),
                                         d.line([9, 16, 23, 16], fill=F1, width=3)))
    ico("fire_mine", False, lambda d: (d.ellipse([5, 14, 27, 26], outline=F1, width=2), d.ellipse([11, 17, 21, 23], outline=F2),
                                       d.polygon([(16, 3), (20, 11), (16, 14), (12, 11)], fill=F1)))
    ico("kindling", False, lambda d: ([d.line([8 + k * 5, 26, 14 + k * 3, 18], fill=(120, 70, 30, 255), width=3) for k in range(3)],
                                      d.polygon([(16, 4), (22, 14), (16, 18), (10, 14)], fill=F2)))
    ico("ash_rain", False, lambda d: (d.ellipse([5, 4, 27, 14], fill=(90, 80, 80, 255)),
                                      [d.ellipse([6 + i * 5, 17 + (i % 2) * 4, 10 + i * 5, 21 + (i % 2) * 4], fill=F2) for i in range(4)]))
    ico("heat_wave", False, lambda d: [d.arc([4 + k * 3, 6, 30 - k * 3, 26], 300, 60, fill=F1 if k % 2 else F2, width=2) for k in range(4)])
    ico("pyro_mastery", False, lambda d: (d.polygon([(16, 3), (25, 16), (22, 26), (10, 26), (7, 16)], fill=F2),
                                          d.polygon([(16, 11), (20, 20), (16, 25), (12, 20)], fill=F1)), special=True)
    ico("solar_flare", False, lambda d: (d.ellipse([10, 10, 22, 22], fill=F1),
                                         [d.line([16 + math.cos(k * math.pi / 4) * 8, 16 + math.sin(k * math.pi / 4) * 8,
                                                  16 + math.cos(k * math.pi / 4) * 14, 16 + math.sin(k * math.pi / 4) * 14], fill=F2, width=2)
                                          for k in range(8)]), special=True)
    return icons


LANG = {
    "magic.yukicore.rebuilt.yuki": ("❄ Магия 7.3: дерево Юки перестроено — очки (%s) возвращены, изучи навыки заново в гримуаре (K).",
                                   "❄ Magic 7.3: Yuki's tree was rebuilt — points (%s) refunded, relearn skills in the grimoire (K)."),
    "magic.yukicore.rebuilt.sintara": ("✹ Магия 7.3: дерево Синтары перестроено — очки (%s) возвращены, изучи навыки заново в гримуаре (K).",
                                    "✹ Magic 7.3: Sintara's tree was rebuilt — points (%s) refunded, relearn skills in the grimoire (K)."),
    "screen.yukicore.grimoire.power": ("Сила: %s%% (уровень школы даёт %s%%)", "Power: %s%% (school level gives %s%%)"),
    "casino.yukicore.title": ("Казино Синтары", "Sintara's Casino"),
    "casino.yukicore.tab.slots": ("Слоты", "Slots"),
    "casino.yukicore.tab.roulette": ("Рулетка", "Roulette"),
    "casino.yukicore.tab.hilo": ("Угадайка", "Hi/Lo"),
    "casino.yukicore.tab.blackjack": ("21", "21"),
    "casino.yukicore.tab.bonus": ("✦ Бонус", "✦ Bonus"),
    "casino.yukicore.bet": ("Ставка: %s", "Bet: %s"),
    "casino.yukicore.push": ("Ничья — ставка возвращена", "Push — bet returned"),
    "casino.yukicore.table3": ("Три кристалла — ещё и 5 бесплатных прокруток с двойным выигрышем!",
                               "Three crystals — also 5 free spins with double wins!"),
    "casino.yukicore.free_won": ("✦ БОНУС: %s бесплатных прокруток с двойным выигрышем!", "✦ BONUS: %s free spins with double wins!"),
    "casino.yukicore.free_left": ("Бесплатных прокруток: %s (ставка %s, выигрыш ×2)", "Free spins: %s (bet %s, wins ×2)"),
    "casino.yukicore.roulette.red": ("Красное", "Red"),
    "casino.yukicore.roulette.black": ("Чёрное", "Black"),
    "casino.yukicore.roulette.green": ("Зеро", "Zero"),
    "casino.yukicore.roulette.rules": ("15 ячеек: 1–7 красные, 8–14 чёрные, 0 — зелёное зеро",
                                       "15 cells: 1–7 red, 8–14 black, 0 — green zero"),
    "casino.yukicore.roulette.rules2": ("Цвет — ×2, зеро — ×14", "Colour — ×2, zero — ×14"),
    "casino.yukicore.hilo.higher": ("Выше ▲", "Higher ▲"),
    "casino.yukicore.hilo.lower": ("Ниже ▼", "Lower ▼"),
    "casino.yukicore.hilo.new": ("Новая карта (бесплатно)", "New card (free)"),
    "casino.yukicore.hilo.odds": ("Выше: %s • Ниже: %s", "Higher: %s • Lower: %s"),
    "casino.yukicore.hilo.rules": ("Следующая карта старше или младше? Такая же — возврат",
                                   "Guess if the next card is higher. Same card — bet returned."),
    "casino.yukicore.bj.deal": ("Раздать", "Deal"),
    "casino.yukicore.bj.hit": ("Ещё", "Hit"),
    "casino.yukicore.bj.stand": ("Хватит", "Stand"),
    "casino.yukicore.bj.double": ("Удвоить", "Double"),
    "casino.yukicore.bj.dealer": ("Синтара: %s", "Sintara: %s"),
    "casino.yukicore.bj.you": ("Ты: %s", "You: %s"),
    "casino.yukicore.bj.in_game": ("В игре: %s фишек", "In play: %s chips"),
    "casino.yukicore.bj.rules": ("Ближе к 21, чем Синтара. Блэкджек ×2,5, победа ×2",
                                 "Get closer to 21 than Sintara. She draws to 17. Blackjack ×2.5, win ×2."),
    "casino.yukicore.bj.status1": ("Победа! +%s фишек", "You win! +%s chips"),
    "casino.yukicore.bj.status2": ("Синтара выиграла. «Ещё партию?»", "Sintara wins. \"Another round?\""),
    "casino.yukicore.bj.status3": ("Ничья — %s фишек возвращено", "Push — %s chips returned"),
    "casino.yukicore.bj.status4": ("БЛЭКДЖЕК! +%s фишек", "BLACKJACK! +%s chips"),
    "casino.yukicore.bj.quip0": ("«Ну что, рискнёшь?» — улыбается Синтара", "\"Feeling lucky?\" Sintara smiles"),
    "casino.yukicore.bj.quip1": ("«Огонь любит смелых»", "\"Fire loves the brave\""),
    "casino.yukicore.bj.quip2": ("«Я вижу твои карты насквозь… шучу!»", "\"I can see your cards… just kidding!\""),
    "casino.yukicore.bj.quip3": ("«Юки всегда говорит „хватит“ слишком рано»", "\"Yuki always stands too early\""),
    "casino.yukicore.wheel.spin": ("✦ Крутить колесо", "✦ Spin the wheel"),
    "casino.yukicore.wheel.ready": ("Колесо удачи готово — бесплатная прокрутка!", "Lucky wheel ready — free spin!"),
    "casino.yukicore.wheel.wait": ("Следующая прокрутка через %s ч %s мин", "Next spin in %sh %sm"),
    "casino.yukicore.wheel.streak": ("Серия дней: %s (+%s%% к призу)", "Day streak: %s (+%s%% prize)"),
    "casino.yukicore.wheel.rules": ("Раз в сутки — бесплатно: от 10 до 500 фишек или сундук удачи.",
                                    "Once a day, free: 10 to 500 chips or a fortune box."),
    "casino.yukicore.wheel.rules2": ("Заходи каждый день: +10% к призу за каждый день серии (до +100%).",
                                     "Come daily: +10% prize per streak day (up to +100%)."),
    "casino.yukicore.wheel.won": ("Колесо удачи: +%s фишек!", "Lucky wheel: +%s chips!"),
    "casino.yukicore.wheel.box": ("Колесо удачи: Огненный сундук удачи!", "Lucky wheel: Fiery Fortune Box!"),
    "casino.yukicore.welcome": ("✦ Синтара: «Добро пожаловать в моё казино! Держи %s фишек на удачу!»",
                                "✦ Sintara: \"Welcome to my casino! Here are %s chips for luck!\""),
}


def lang():
    ru, en = {}, {}
    for k, (r, e) in LANG.items():
        ru[k], en[k] = r, e
    return ru, en


# Починка чужих таблиц добычи (старые условия Forge не работают на NeoForge 1.21.1)
LOOT_FIXES = {
    ("hexerei", "blocks/willow_vines"): {
        "type": "minecraft:block",
        "pools": [{"bonus_rolls": 0.0, "rolls": 1.0,
                   "conditions": [{"condition": "neoforge:can_item_perform_ability", "ability": "shears_dig"}],
                   "entries": [{"type": "minecraft:item", "name": "hexerei:willow_vines"}]}],
        "random_sequence": "hexerei:blocks/willow_vines",
    },
}
