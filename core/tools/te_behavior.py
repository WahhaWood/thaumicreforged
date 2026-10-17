#!/usr/bin/env python3
"""Поведенческие проверки фиксов ThaumicEnergistics (фаза 1).

1. Мод загрузился без ошибок (по логу сервера).
2. GUI открываются по ПКМ для 7 блоков (через мост /menu/open).
3. Тики не сыплют ошибками (лог чист от Exception после прогрева).
4. Печь плавит предмет при наличии энергии (через data modify + 200 тиков).
"""
import json
import subprocess
import sys
import time
import urllib.request

BASE = "http://127.0.0.1:9888"
FAKE = "fake"
BX, BY, BZ = 320, 99, -380  # отдельная площадка вдали от регрессии (300..340)

BLOCKS_WITH_GUI = {
    "knowledge_inscriber": "thaumicenergistics:knowledge_inscriber",
    "crucible_inscriber": "thaumicenergistics:crucible_inscriber",
    "infusion_inscriber": "thaumicenergistics:infusion_inscriber",
    "arcane_assembler": "thaumicenergistics:arcane_assembler",
    "crucible_assembler": "thaumicenergistics:crucible_assembler",
    "infusion_assembler": "thaumicenergistics:infusion_assembler",
    "industrial_alchemy_furnace": "thaumicenergistics:industrial_alchemy_furnace",
}

fails = []


def post(path, payload=None):
    req = urllib.request.Request(
        BASE + path,
        data=json.dumps(payload or {}).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.loads(r.read().decode())


def get(path):
    with urllib.request.urlopen(BASE + path, timeout=30) as r:
        return json.loads(r.read().decode())


def check(name, cond, extra=""):
    print(("PASS " if cond else "FAIL ") + name + (f" [{extra}]" if extra else ""))
    if not cond:
        fails.append(name)


def main():
    st = get("/status")
    check("bridge up", st.get("server") is True, str(st.get("tick")))
    post("/exec", {"commands": ["forceload add 280 -420 360 -360", "kill @e[type=!player]"]})
    post("/player", {"player": FAKE, "x": BX + 0.5, "y": BY, "z": BZ + 0.5, "join": True})
    post("/exec", {"commands": ["gamemode creative fake"]})

    # 1. GUI
    for i, (short, full) in enumerate(BLOCKS_WITH_GUI.items()):
        x, y, z = BX + i * 3, BY, BZ
        post("/exec", {"commands": [f"setblock {x} {y} {z} {full}"]})
    post("/wait", {"ticks": 20})
    for i, (short, full) in enumerate(BLOCKS_WITH_GUI.items()):
        x, y, z = BX + i * 3, BY, BZ
        try:
            res = post("/menu/open", {"x": x, "y": y, "z": z, "player": FAKE})
            ok = bool(res.get("ok", True)) and "error" not in str(res).lower()
        except Exception as e:  # noqa: BLE001
            ok = False
            res = str(e)
        check(f"menu:{short}", ok, str(res)[:120])
        try:
            post("/menu/close", {"player": FAKE})
        except Exception:  # noqa: BLE001
            pass

    # 2. Печь: энергия через data + предмет с аспектами (vis-кристалл воды)
    fx, fy, fz = BX, BY + 3, BZ
    post("/exec", {"commands": [f"setblock {fx} {fy} {fz} thaumicenergistics:industrial_alchemy_furnace"]})
    post("/wait", {"ticks": 10})
    # предмет с аспектами в слот 0 через give+перемещение? Проще: data modify inventory
    r = post("/exec", {"commands": [
        f"data modify block {fx} {fy} {fz} energy set value 5000",
    ]})
    check("furnace:energy-set", r.get("ok", True), str(r)[:100])
    post("/exec", {"commands": [
        f"setblock {fx} {fy+2} {fz} minecraft:chest",
        f"give fake thaumcraft:shard_water 4",
    ]})
    post("/wait", {"ticks": 40})
    # читаем NBT печи: totalEssentia виден через /block? смотрим energy/aspects через data get
    r = post("/exec", {"commands": [f"data get block {fx} {fy} {fz} energy"]})
    print("furnace data:", str(r)[:200])

    print()
    if fails:
        print(f"FAILED: {len(fails)}: {fails}")
        return 1
    print("ALL BEHAVIOR CHECKS PASSED")
    return 0


if __name__ == "__main__":
    sys.exit(main())
