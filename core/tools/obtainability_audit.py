#!/usr/bin/env python3
"""Аудит достижимости: что из зарегистрированного контента вообще можно получить.

Идея: собрать все id предметов и блоков из реестров исходников, потом собрать
все id, которые встречаются результатом какого-либо рецепта (shaped/shapeless/
crucible/arcane/infusion/smelting/enchant/fortress). Всё, что есть в реестре,
но не производится ни одним рецептом, — кандидат в «недостижимое».

Вывод НЕ приговор: предмет может падать из лута, выпадать из существ или
выдаваться жезлом. Поэтому кандидаты делятся на категории и печатаются списком
для ручной проверки.
"""
import json
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
JAVA = os.path.join(ROOT, "src", "main", "java", "thaumcraft", "init", "registry")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "thaumcraft")

RECIPE_TYPES = ("shaped", "shapeless", "crucible", "arcane", "infusion",
                "smelting", "enchant", "fortress", "runic_augmentation")


def registered_ids(path, patterns):
    text = open(path, encoding="utf-8").read()
    ids = set()
    for pat in patterns:
        ids |= set(re.findall(pat, text))
    return ids


def main():
    items = registered_ids(os.path.join(JAVA, "ModItems.java"), [
        r'register[A-Za-z]*\(\s*"([a-z0-9_/]+)"',
        r'REGISTER\.register\(\s*"([a-z0-9_/]+)"',
    ])
    items -= set(re.findall(
        r'register(?:Block)?Colors\("([a-z0-9_/]+)"',
        open(os.path.join(JAVA, "ModItems.java"), encoding="utf-8").read()))

    blocks = registered_ids(os.path.join(JAVA, "ModBlocks.java"), [
        r'REGISTER\.register\(\s*"([a-z0-9_/]+)"',
        r'registerBlock\(\s*"([a-z0-9_/]+)"',
    ])
    blocks -= set(re.findall(
        r'register(?:Block)?Colors\("([a-z0-9_/]+)"',
        open(os.path.join(JAVA, "ModBlocks.java"), encoding="utf-8").read()))

    # Всё, что вообще производится рецептом.
    produced = set()
    per_type = {}
    for rtype in RECIPE_TYPES:
        folder = os.path.join(DATA, "recipe", rtype)
        if not os.path.isdir(folder):
            continue
        found = set()
        for name in os.listdir(folder):
            if not name.endswith(".json"):
                continue
            try:
                data = json.load(open(os.path.join(folder, name), encoding="utf-8"))
            except Exception:
                continue
            for key in ("result", "output"):
                res = data.get(key)
                if isinstance(res, dict) and isinstance(res.get("id"), str):
                    found.add(res["id"].split(":")[-1])
                elif isinstance(res, str):
                    found.add(res.split(":")[-1])
        per_type[rtype] = found
        produced |= found

    # Лут: id блоков, у которых есть loot_table.
    looted = set()
    loot_dir = os.path.join(DATA, "loot_table", "blocks")
    if os.path.isdir(loot_dir):
        looted = {n[:-5] for n in os.listdir(loot_dir) if n.endswith(".json")}

    print(f"Реестр: предметов={len(items)} блоков={len(blocks)}")
    print(f"Производится рецептами: {len(produced)} уникальных id")
    for rtype, found in sorted(per_type.items()):
        print(f"  {rtype:20s} {len(found)}")

    def report(title, ids, extra_known=()):
        missing = sorted(i for i in ids
                         if i not in produced and i not in extra_known)
        print(f"\n### {title}: без рецепта — {len(missing)} из {len(ids)}")
        for i in missing:
            tag = " (есть лут-таблица)" if i in looted else ""
            print(f"  {i}{tag}")
        return missing

    # Инфраструктурное: то, что не предмет в игровом смысле.
    skip_items = {"air", "candle"}
    colors = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
              "gray", "light_gray", "cyan", "purple", "blue", "brown", "green",
              "red", "black"]
    skip_items |= {c + "_banner" for c in colors} | {c + "_candle" for c in colors}

    skip_blocks = set()
    for b in ("banner", "wall_banner", "candle"):
        skip_blocks |= {c + "_" + b for c in colors}

    mi = report("ПРЕДМЕТЫ без рецепта", items - skip_items)
    mb = report("БЛОКИ без рецепта", blocks - skip_blocks)

    print("\n--- сводка ---")
    print(f"предметов без рецепта: {len(mi)}, блоков без рецепта: {len(mb)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())