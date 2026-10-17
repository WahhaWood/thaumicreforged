#!/usr/bin/env python3
"""End-to-end tests for the research / arcane workbench / crucible slice.

Usage: python3 tools/tests_progress.py
"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..", "_shared", "devbridge"))
import bridgekit  # noqa: E402

BASE = "http://127.0.0.1:9888"
bridgekit.configure(BASE)
FAILS = bridgekit.FAILS


def call(path, payload=None, timeout=30):
    return bridgekit.call(path, payload, timeout)


def exec_cmd(command):
    return call("/exec", {"command": command})


def slot_item(menu, slot):
    for entry in menu.get("slots", []):
        if entry.get("slot") == slot:
            return entry
    return None


check = bridgekit.check


def main():
    # ---- setup -------------------------------------------------------
    exec_cmd("forceload add -20 -20 40 40")
    exec_cmd("setblock 10 100 10 thaumcraft:arcane_workbench replace")
    exec_cmd("setblock 11 100 10 thaumcraft:crucible replace")
    call("/wait", {"ticks": 20})

    # ---- research ----------------------------------------------------
    result = call("/autoresearch", {"player": "fake"})
    auto = set(result.get("completed", []))
    expected = {"ASPECTS", "RESEARCH", "THAUMONOMICON", "ARCTABLE", "THAUMOMETER", "CRUCIBLE"}
    check("авто-исследования выданы", expected <= auto, sorted(auto))

    call("/pool", {"player": "fake", "aspect": "metallum", "amount": 10})
    call("/pool", {"player": "fake", "aspect": "praecantatio", "amount": 10})
    result = call("/unlockresearch", {"player": "fake", "key": "THAUMIUM"})
    check("THAUMIUM куплен", result.get("unlocked") is True, result)
    check("THAUMIUM в списке", "THAUMIUM" in result.get("completed", []), result)

    knowledge = call("/knowledge", {"player": "fake"})
    pool = {a["aspect"]: a["pool"] for a in knowledge.get("aspects", [])}
    check("пул списан (metallum 10->7)", pool.get("metallum") == 7, pool)
    check("пул списан (praecantatio 10->7)", pool.get("praecantatio") == 7, pool)

    # ---- arcane workbench --------------------------------------------
    call("/items", {"player": "fake", "slot": 9, "item": "minecraft:iron_ingot", "count": 7})
    call("/items", {"player": "fake", "slot": 10, "item": "minecraft:cauldron", "count": 1})
    call("/menu/open", {"player": "fake", "x": 10, "y": 100, "z": 10})
    call("/menu/click", {"slot": 0, "button": 0, "type": "PICKUP", "player": "fake"})
    for slot in [36, 38, 39, 41, 42, 43, 44]:
        call("/menu/click", {"slot": slot, "button": 1, "type": "PICKUP", "player": "fake"})
    call("/menu/click", {"slot": 1, "button": 0, "type": "PICKUP", "player": "fake"})
    call("/menu/click", {"slot": 40, "button": 0, "type": "PICKUP", "player": "fake"})

    menu = call("/menu", {"player": "fake"})
    result_slot = slot_item(menu, 45)
    check("верстак: результат = тигель", result_slot and result_slot.get("item") == "thaumcraft:crucible", result_slot)

    click = call("/menu/click", {"slot": 45, "button": 0, "type": "PICKUP", "player": "fake"})
    check("верстак: тигель забран", str(click.get("carried", "")).startswith("thaumcraft:crucible"), click.get("carried"))
    call("/menu/close", {"player": "fake"})

    # ---- crucible ----------------------------------------------------
    exec_cmd('data merge block 11 100 10 {Fluid:{id:"minecraft:water",amount:1000},aspects:{ignis:4,perditio:4}}')
    info = call("/crucible/info", {"player": "fake", "x": 11, "y": 100, "z": 10})
    check("тигель: вода 1000", info.get("water") == 1000, info)

    result = call("/crucible/insert", {"player": "fake", "x": 11, "y": 100, "z": 10, "item": "minecraft:gunpowder"})
    check("тигель: рецепт пороха", result.get("handled") is True, result)
    check("тигель: аспекты списаны", not result.get("aspects"), result.get("aspects"))
    check("тигель: вода 1000->950", result.get("water") == 950, result.get("water"))

    result = call("/crucible/insert", {"player": "fake", "x": 11, "y": 100, "z": 10, "item": "minecraft:coal"})
    check("тигель: уголь растворился", result.get("handled") is True, result)
    aspects = {a["aspect"]: a["amount"] for a in result.get("aspects", [])}
    check("тигель: аспекты угля (potentia 2, ignis 1)",
          aspects.get("potentia") == 2 and aspects.get("ignis") == 1, aspects)

    print()
    print("ИТОГ:", "все тесты пройдены" if not FAILS else f"провалено: {FAILS}")
    return 1 if FAILS else 0


if __name__ == "__main__":
    sys.exit(main())
