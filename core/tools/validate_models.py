#!/usr/bin/env python3
"""Validates every model/blockstate of the mod before it ever reaches the game.

Checks:
  * parent references resolve to existing model files
  * texture references resolve to existing PNG files
  * OBJ loader references (model / mtl_override) exist, MTL textures exist
  * element coordinates stay inside -16..32
  * UVs are ordered (u1 < u2, v1 < v2)
  * rotations use one axis and the vanilla-allowed angles
  * neoforge_data keys are known
  * blockstates reference existing models

Usage:
  python3 tools/validate_models.py [path/to/latest.log]
"""

import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets"

ALLOWED_ANGLES = {-45.0, -22.5, 0.0, 22.5, 45.0}
FACE_DATA_KEYS = {"block_light", "sky_light", "ambient_occlusion", "tint_index"}


def resolve(namespace: str, path: str, kind: str) -> pathlib.Path:
    if kind == "model":
        return ASSETS / namespace / "models" / f"{path}.json"
    if kind == "texture":
        return ASSETS / namespace / "textures" / f"{path}.png"
    return ASSETS / namespace / path


def split(ref: str):
    if ":" in ref:
        namespace, path = ref.split(":", 1)
    else:
        namespace, path = "minecraft", ref
    return namespace, path


def main() -> int:
    problems: list[str] = []

    for model_path in sorted(ASSETS.glob("*/models/**/*.json")):
        namespace = model_path.parts[len(ASSETS.parts)]
        rel = model_path.relative_to(ASSETS / namespace / "models")
        where = f"{namespace}:{rel.with_suffix('')}"
        try:
            data = json.loads(model_path.read_text(encoding="utf-8"))
        except Exception as exception:
            problems.append(f"{where}: битый JSON ({exception})")
            continue

        parent = data.get("parent")
        if parent and parent != "builtin/entity":
            ns, path = split(parent)
            if not resolve(ns, path, "model").exists() and ns != "minecraft":
                problems.append(f"{where}: parent {parent} не найден")

        for key, value in (data.get("textures") or {}).items():
            if not isinstance(value, str) or value.startswith("#"):
                continue
            ns, path = split(value)
            if ns == "minecraft":
                continue
            if not resolve(ns, path, "texture").exists():
                problems.append(f"{where}: текстура {value} -> {path}.png не найдена")

        if data.get("loader") == "neoforge:obj":
            for field, suffix in (("model", ""), ("mtl_override", "")):
                ref = data.get(field)
                if not ref:
                    continue
                ns, path = split(ref)
                if not (ASSETS / ns / path).exists():
                    problems.append(f"{where}: {field} {ref} -> assets/{ns}/{path} не найден")
            mtl = data.get("mtl_override")
            if mtl:
                ns, path = split(mtl)
                mtl_file = ASSETS / ns / path
                if mtl_file.exists():
                    for line in mtl_file.read_text(encoding="utf-8", errors="replace").splitlines():
                        if line.strip().startswith("map_Kd"):
                            tex = line.split()[-1]
                            tns, tpath = split(tex)
                            if tns != "minecraft" and not resolve(tns, tpath, "texture").exists():
                                problems.append(f"{where}: MTL текстура {tex} не найдена")

        for index, element in enumerate(data.get("elements") or []):
            for corner in ("from", "to"):
                values = element.get(corner)
                if not values:
                    continue
                for value in values:
                    if value < -16 or value > 32:
                        problems.append(f"{where}: элемент {index} {corner} {values} вне -16..32")
            rotation = element.get("rotation")
            if rotation:
                angle = rotation.get("angle", 0.0)
                if angle not in ALLOWED_ANGLES:
                    problems.append(f"{where}: элемент {index} угол {angle} недопустим")
                if not rotation.get("axis"):
                    problems.append(f"{where}: элемент {index} rotation без axis")
            for face, face_data in (element.get("faces") or {}).items():
                uv = face_data.get("uv")
                if uv and (uv[0] > uv[2] or uv[1] > uv[3]):
                    problems.append(f"{where}: элемент {index} {face} UV перевёрнуты {uv}")
                extra = face_data.get("neoforge_data")
                if extra:
                    for key in extra:
                        if key not in FACE_DATA_KEYS:
                            problems.append(f"{where}: элемент {index} {face} неизвестный neoforge_data ключ {key}")

    for state_path in sorted(ASSETS.glob("*/blockstates/*.json")):
        namespace = state_path.parts[len(ASSETS.parts)]
        where = f"blockstates/{state_path.name}"
        try:
            data = json.loads(state_path.read_text(encoding="utf-8"))
        except Exception as exception:
            problems.append(f"{where}: битый JSON ({exception})")
            continue
        for variant in (data.get("variants") or {}).values():
            entries = variant if isinstance(variant, list) else [variant]
            for entry in entries:
                ref = entry.get("model")
                if not ref:
                    continue
                ns, path = split(ref)
                if not resolve(ns, path, "model").exists():
                    problems.append(f"{where}: модель {ref} не найдена")

    if len(sys.argv) > 1:
        log = pathlib.Path(sys.argv[1])
        if log.exists():
            for line in log.read_text(encoding="utf-8", errors="replace").splitlines():
                if re.search(r"(Failed to load model|Unable to load model|Missing textures in model)", line):
                    problems.append(f"лог: {line.strip()[:160]}")

    if problems:
        print(f"Проблем: {len(problems)}")
        for problem in problems:
            print("  ", problem)
        return 1

    print("Модели и blockstate в порядке: проблем нет")
    return 0


if __name__ == "__main__":
    sys.exit(main())
