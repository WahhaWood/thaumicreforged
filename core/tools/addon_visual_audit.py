#!/usr/bin/env python3
"""Целостность визуалов аддона Thaumic Energistics.

Проверяет по файлам, без запуска игры:
  * на каждый зарегистрированный блок есть blockstate;
  * каждый blockstate ссылается на существующую модель;
  * каждая модель ссылается на существующую текстуру;
  * каждая текстура, на которую ссылаются модели, реально лежит на диске.

Это ловит ровно тот класс поломок, который не видно на сервере: блок
регистрируется, ставится, а рисуется как фиолетовый куб или вообще ничем.
"""
import json
import os
import re
import sys

ADDON = "thaumicenergistics"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..",
                    "addons", ADDON)
RES = os.path.join(ROOT, "src", "main", "resources", "assets", ADDON)
JAVA = os.path.join(ROOT, "src", "main", "java", ADDON)


def read_json(path):
    # some resource files start with a BOM — json.load без utf-8-sig на них падает
    with open(path, encoding="utf-8-sig") as fh:
        return json.load(fh)


def registered_blocks():
    text = open(os.path.join(JAVA, "init", "ModBlocks.java"), encoding="utf-8").read()
    return set(re.findall(r'register\("([a-z0-9_]+)"', text))


def collect_refs(node, out_models, out_textures, mode):
    """Собирает ссылки на модели и текстуры аддона с учётом КОНТЕКСТА.

    Одна и та же строка `thaumicenergistics:block/foo` означает разное в разных
    файлах: в blockstate это `models/block/foo.json`, а внутри модели (ключ
    "textures") это `textures/block/foo.png`. Поэтому режим передаётся явно:
      mode "blockstate" — ссылки трактуются как модели;
      mode "model"      — ключ "parent" даёт модель, всё остальное — текстура.
    """
    if isinstance(node, str):
        if not node.startswith(f"{ADDON}:"):
            return
        path = node.split(":", 1)[1]
        (out_models if mode == "blockstate" else out_textures).add(path)
    elif isinstance(node, dict):
        for key, value in node.items():
            if node.get("loader") == "neoforge:obj" and key == "model":
                out_models.add("obj:" + str(value).split(":", 1)[-1])
                continue
            child = "model" if (mode == "model" and key == "parent") else mode
            if mode == "model" and key != "parent":
                child = "texture"
            if child == "model":
                out_models.add(value) if isinstance(value, str) and \
                    value.startswith(f"{ADDON}:") else None
            elif child == "texture" and isinstance(value, str) and \
                    value.startswith(f"{ADDON}:"):
                out_textures.add(value.split(":", 1)[1])
            else:
                collect_refs(value, out_models, out_textures, child)
    elif isinstance(node, list):
        for value in node:
            collect_refs(value, out_models, out_textures, mode)


def main():
    print(f"Аддон: {ROOT}")
    blocks = registered_blocks()
    print(f"Зарегистрировано блоков в ModBlocks: {len(blocks)}")

    problems = []

    # 1. blockstate на каждый блок
    bs_dir = os.path.join(RES, "blockstates")
    have_bs = {n[:-5] for n in os.listdir(bs_dir) if n.endswith(".json")}
    missing_bs = sorted(blocks - have_bs)
    orphan_bs = sorted(have_bs - blocks)
    print(f"\nblockstate: {len(have_bs)} файлов")
    if missing_bs:
        problems.append(f"блоки без blockstate: {missing_bs}")
    if orphan_bs:
        problems.append(f"blockstate без блока: {orphan_bs}")

    # 2+3. модели и текстуры
    models_dir = os.path.join(RES, "models")
    models = set()
    for root, _, files in os.walk(models_dir):
        for name in files:
            if name.endswith(".json"):
                rel = os.path.relpath(os.path.join(root, name), models_dir)
                models.add(rel[:-5])
    print(f"моделей: {len(models)}")

    tex_dir = os.path.join(RES, "textures")
    textures = set()
    for root, _, files in os.walk(tex_dir):
        for name in files:
            if name.endswith(".png"):
                rel = os.path.relpath(os.path.join(root, name), tex_dir)
                textures.add(rel[:-4])
    print(f"текстур: {len(textures)}")

    def strip_ns(pairs):  # noqa: E306
        # ссылки из разных ветвей приходят то с неймспейсом, то без —
        # приводим к одному виду
        out = set()
        for value in pairs:
            value = value.split(":", 1)[-1] if value.startswith(f"{ADDON}:") else value
            out.add(value)
        return out

    used_models, used_textures = set(), set()
    for name in sorted(have_bs):
        collect_refs(read_json(os.path.join(bs_dir, name + ".json")),
                     used_models, used_textures, "blockstate")
    for name in sorted(models):
        collect_refs(read_json(os.path.join(models_dir, name + ".json")),
                     used_models, used_textures, "model")

    used_models = strip_ns(used_models)
    used_textures = strip_ns(used_textures)

    obj_dir = os.path.join(models_dir, "obj")
    have_obj = {n[:-4] for n in os.listdir(obj_dir) if n.endswith(".obj")} \
        if os.path.isdir(obj_dir) else set()

    # Ссылка на OBJ приходит двумя способами: путём с суффиксом .obj либо
    # через loader neoforge:obj. Приводим оба к виду "obj/<имя>".
    def as_obj(model):
        """Имя OBJ без пути и расширения, либо None если это обычная модель."""
        path = model[4:] if model.startswith("obj:") else model
        if ".obj" in path:
            return os.path.basename(path)[:-4]
        if model.startswith("obj:"):
            return os.path.basename(model[4:])
        return None

    missing_models, missing_obj = [], []
    for model in sorted(used_models):
        obj = as_obj(model)
        if obj is not None:
            if obj not in have_obj:
                missing_obj.append(model)
        elif model not in models:
            missing_models.append(model)

    if missing_models:
        problems.append(f"ссылки на несуществующие модели ({len(missing_models)}): "
                        f"{missing_models[:12]}")
    if missing_obj:
        problems.append(f"ссылки на несуществующие OBJ ({len(missing_obj)}): "
                        f"{missing_obj[:12]}")
    print(f"OBJ-моделей: {len(have_obj)}")

    missing_tex = sorted(t for t in used_textures if t not in textures)
    if missing_tex:
        problems.append(f"ссылки на несуществующие текстуры ({len(missing_tex)}): "
                        f"{missing_tex[:12]}")

    unused_models = sorted(models - {m for m in used_models
                                     if as_obj(m) is None})
    print(f"\nиспользуется моделей: {len(used_models)}, текстур: {len(used_textures)}")
    if unused_models:
        print(f"модели без ссылок (лишние или для ручной расстановки): "
              f"{len(unused_models)} — {unused_models[:6]}")

    # 4. lang
    lang_dir = os.path.join(RES, "lang")
    langs = sorted(os.listdir(lang_dir)) if os.path.isdir(lang_dir) else []
    print(f"lang: {langs}")

    print("\n--- итог ---")
    if problems:
        for problem in problems:
            print("ПРОБЛЕМА: " + problem)
        return 1
    print("визуалы целостны: каждый блок имеет blockstate, "
          "все модели и текстуры на месте")
    return 0


if __name__ == "__main__":
    sys.exit(main())