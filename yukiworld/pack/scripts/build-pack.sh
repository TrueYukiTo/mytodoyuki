#!/usr/bin/env bash
# Собирает YukiWorld: подтягивает моды из mods.txt через packwiz (Modrinth; строки cf: — CurseForge),
# добавляет свой мод YukiCore и экспортирует .mrpack (Prism / Modrinth App / ATLauncher).
#
#   bash yukiworld/pack/scripts/build-pack.sh [путь/к/yukicore.jar]
#
# Нужен packwiz:  go install github.com/packwiz/packwiz@latest
set -uo pipefail

PACK_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO_DIR="$(cd "$PACK_DIR/../.." && pwd)"
OUT="$PACK_DIR/build"
WORK="$OUT/YukiWorld"
JAR="${1:-$(ls -t "$REPO_DIR"/yukiworld/yukicore/build/libs/yukicore-*.jar 2>/dev/null | grep -v sources | head -n1)}"

command -v packwiz >/dev/null || { echo "❌ packwiz не найден (go install github.com/packwiz/packwiz@latest)"; exit 1; }

rm -rf "$OUT" && mkdir -p "$WORK/mods"
cp "$PACK_DIR/pack.toml" "$PACK_DIR/index.toml" "$WORK/"
cp -r "$PACK_DIR/config" "$WORK/"
cd "$WORK"
packwiz refresh

ok=0; failed=()
while IFS= read -r line; do
  slug="${line%%#*}"; slug="$(echo "$slug" | xargs)"
  [[ -z "$slug" ]] && continue
  src=modrinth
  if [[ "$slug" == cf:* ]]; then src=curseforge; slug="${slug#cf:}"; fi
  if packwiz -y "$src" add "$slug" >"$OUT/add-$slug.log" 2>&1; then
    echo "✅ $slug"; ok=$((ok+1))
  else
    echo "⚠️  $slug — не найден для 1.21.1/NeoForge, пропущен"; failed+=("$slug")
  fi
done < "$PACK_DIR/mods.txt"

# CurseForge-моды могут притянуть зависимость, которая уже взята с Modrinth (например AE2) —
# оставляем копию с Modrinth, чтобы в сборке не было двух одинаковых jar.
for cf in mods/*.pw.toml; do
  grep -q 'mode = "metadata:curseforge"' "$cf" || continue
  fname="$(grep '^filename' "$cf")"
  for mr in mods/*.pw.toml; do
    [[ "$mr" == "$cf" ]] && continue
    if [[ "$(grep '^filename' "$mr")" == "$fname" ]] && ! grep -q 'metadata:curseforge' "$mr"; then
      echo "🧹 дубликат: $(basename "$cf") (уже есть $(basename "$mr"))"; rm "$cf"; break
    fi
  done
done

if [[ -n "$JAR" && -f "$JAR" ]]; then
  cp "$JAR" mods/ && echo "✅ YukiCore: $(basename "$JAR")"
else
  echo "⚠️  YukiCore jar не найден — собери его: (cd yukiworld/yukicore && ./gradlew build)"
fi

packwiz refresh
VERSION="$(grep '^version' pack.toml | cut -d'"' -f2)"
packwiz modrinth export -o "$OUT/YukiWorld-$VERSION.mrpack"

{
  echo "YukiWorld $VERSION — отчёт сборки"
  echo "Добавлено из списка: $ok"
  echo "Всего модов в сборке (с зависимостями): $(ls mods | wc -l)"
  echo; echo "Пропущены (нет версии под 1.21.1 NeoForge):"
  for f in "${failed[@]:-}"; do [[ -n "$f" ]] && echo "  - $f"; done
  echo; echo "Состав:"; ls mods | sed 's/\.pw\.toml$//' | sort | sed 's/^/  • /'
} > "$OUT/mods-report.txt"

cat "$OUT/mods-report.txt" | head -n 8
echo "📦 Готово: $OUT/YukiWorld-$VERSION.mrpack"
