#!/usr/bin/env python3
"""Самотест dev-моста ThaumicReforged.

Прогоняет все ключевые эндпоинты и механики на живом сервере (или клиенте в мире):
базовые команды, блоки, меню (fake-игрок), знания, узлы ауры, транспорт эссенции
(алембик → трубки → банка), мироген через /place feature, 404.

Использование:
  tools/tcbridge_selftest.py --base http://127.0.0.1:9888
  tools/tcbridge_selftest.py --base http://127.0.0.1:9887   # клиент в мире
"""

import argparse
import json
import sys
import urllib.error
import urllib.request

PASS = 0
FAIL = 0
FAILURES = []


def check(name, condition, info=""):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  PASS  {name}")
    else:
        FAIL += 1
        FAILURES.append(name)
        print(f"  FAIL  {name}  {info}")


class Bridge:
    def __init__(self, base):
        self.base = base

    def call(self, path, payload=None, timeout=120):
        url = self.base + path
        if payload is None:
            request = urllib.request.Request(url, headers={"Accept": "application/json"})
        else:
            request = urllib.request.Request(
                url,
                data=json.dumps(payload).encode("utf-8"),
                headers={"Content-Type": "application/json", "Accept": "application/json"},
            )
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                return response.status, json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            body = error.read().decode("utf-8", "replace")
            try:
                return error.code, json.loads(body)
            except json.JSONDecodeError:
                return error.code, {"raw": body}
        except urllib.error.URLError as error:
            print(f"мост недоступен: {error}", file=sys.stderr)
            sys.exit(2)

    def get(self, path, **query):
        if query:
            path += "?" + "&".join(f"{k}={v}" for k, v in query.items())
        return self.call(path)

    def exec(self, command):
        status, data = self.call("/exec", {"command": command})
        return status, data

    def wait(self, ticks):
        _status, data = self.call("/wait", {"ticks": ticks}, timeout=300)
        return data


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://127.0.0.1:9888")
    parser.add_argument("--area", default="300,-400", help="x,z центра тестовой площадки")
    args = parser.parse_args()

    ax, az = (int(v) for v in args.area.split(","))
    y = 100
    fake = {"player": "fake"}
    bridge = Bridge(args.base)

    print("== статус ==")
    status, data = bridge.call("/status")
    check("GET /status", status == 200 and "dist" in data, str(data)[:120])
    if data.get("server") is not True:
        print("!! сервер ещё не готов (server != true), подожди и повтори")
        sys.exit(2)

    print("== площадка ==")
    bridge.exec(f"forceload add {ax - 4} {az - 4} {ax + 28} {az + 12}")
    bridge.wait(20)

    print("== базовое ==")
    status, data = bridge.exec("list")
    check("exec list", status == 200 and data.get("ok", True) and data.get("output"), str(data)[:120])
    status, data = bridge.exec("totally_not_a_command_xyz")
    check("exec ошибка", not data.get("ok", True), str(data)[:120])
    status, data = bridge.call("/wait", {"ticks": 5})
    check("wait", status == 200, str(data)[:120])

    bx, bz = ax, az
    bridge.exec(f"setblock {bx} {y} {bz} minecraft:stone")
    status, data = bridge.get(f"/block", x=bx, y=y, z=bz)
    check("block", "stone" in json.dumps(data), str(data)[:120])

    print("== меню (fake-игрок, сундук) ==")
    bridge.exec(f"setblock {bx} {y} {bz - 2} minecraft:air")
    bridge.exec(f"setblock {bx} {y} {bz - 2} minecraft:chest")
    bridge.exec(f"item replace block {bx} {y} {bz - 2} container.0 with minecraft:stone 5")
    status, data = bridge.call("/menu/open", {**fake, "x": bx, "y": y, "z": bz - 2})
    check("menu/open", status == 200, str(data)[:140])
    status, data = bridge.call("/menu", fake)
    check("menu", status == 200 and "slots" in json.dumps(data), str(data)[:140])
    status, data = bridge.call("/menu/click", {**fake, "slot": 0, "type": "QUICK_MOVE"})
    check("menu/click", status == 200, str(data)[:140])
    bridge.call("/menu/close", fake)

    print("== знания ==")
    status, data = bridge.call("/knowledge/selftest", fake)
    check("knowledge/selftest", data.get("roundTripOk") is True, str(data)[:140])
    status, data = bridge.call("/research", fake)
    check("research", "completed" in data, str(data)[:120])

    print("== узлы ауры ==")
    nx, nz = ax + 2, az
    bridge.exec(f"setblock {nx} {y} {nz} thaumcraft:aura_node")
    status, data = bridge.get("/node", x=nx, y=y, z=nz)
    check("node найден", data.get("found") is True, str(data)[:140])
    status, data = bridge.call(
        "/node/config",
        {"x": nx, "y": y, "z": nz, "aspects": {"aer": 100, "ignis": 50}, "type": "DARK", "modifier": "BRIGHT"},
    )
    check("node/config", data.get("type") == "DARK" and data.get("modifier") == "BRIGHT", str(data)[:140])
    status, data = bridge.call("/node/config", {"x": nx, "y": y, "z": nz, "drain": {"aer": 40}})
    check("node/drain", data.get("aspects", {}).get("aer") == 60, str(data)[:140])
    bridge.wait(210)
    status, data = bridge.get("/node", x=nx, y=y, z=nz)
    check("node регенерация (BRIGHT=200т)", data.get("aspects", {}).get("aer", 0) >= 61, str(data)[:140])
    status, data = bridge.get("/node/drops", x=nx, y=y, z=nz)
    check("node/drops (эссенция виспов)", any(d.get("item") == "thaumcraft:wisp_essence" for d in data.get("drops", [])), str(data)[:140])

    print("== мироген /place feature ==")
    fx, fz = ax + 3, az + 3
    bridge.exec(f"setblock {fx} {y} {fz} minecraft:air")
    bridge.exec(f"setblock {fx} {y + 1} {fz} minecraft:air")
    status, data = bridge.exec(f"place feature thaumcraft:aura_node {fx} {y} {fz}")
    check("place feature", "Placed" in json.dumps(data), str(data)[:140])
    status, data = bridge.get("/node", x=fx, y=y, z=fz)
    check("узел из фичи", data.get("found") is True and data.get("aspects"), str(data)[:140])

    print("== эссенция: алембик → трубки → банка ==")
    s = ax - 2
    bridge.exec(f"setblock {s} {y} {az} minecraft:air")
    bridge.exec(f"setblock {s} {y} {az + 2} minecraft:air")
    bridge.exec(f"setblock {s} {y} {az} thaumcraft:alembic")
    bridge.exec(f"setblock {s} {y + 1} {az} thaumcraft:essence_tube")
    bridge.exec(f"setblock {s} {y + 1} {az + 1} thaumcraft:essence_tube")
    bridge.exec(f"setblock {s} {y + 1} {az + 2} thaumcraft:essence_tube")
    bridge.exec(f"setblock {s} {y} {az + 2} thaumcraft:essentia_jar")
    status, data = bridge.call(
        "/essentia/insert",
        {"x": s, "y": y, "z": az, "aspect": "aer", "amount": 10},
    )
    check("essentia/insert", data.get("inserted") == 10, str(data)[:140])
    status, data = bridge.get("/essentia", x=s, y=y + 1, z=az + 1)
    check("essentia транспорт (сакция)", data.get("sides"), str(data)[:140])
    bridge.wait(250)
    status, data = bridge.get("/essentia", x=s, y=y, z=az + 2)
    check("эссенция доехала в банку", data.get("aspects", {}).get("aer", 0) >= 8, str(data)[:140])
    status, data = bridge.get("/essentia", x=s, y=y, z=az)
    check("алембик опустел", data.get("aspects", {}).get("aer", 0) <= 2, str(data)[:140])

    print("== трубки: направленная и буфер ==")
    ox, oz = ax + 12, az
    bridge.exec(f"setblock {ox} {y} {oz} thaumcraft:alembic")
    bridge.exec(f"setblock {ox + 1} {y} {oz} thaumcraft:directional_essence_tube[facing=east]")
    bridge.exec(f"setblock {ox + 2} {y} {oz} thaumcraft:essence_tube")
    bridge.exec(f"setblock {ox + 2} {y - 1} {oz} minecraft:air")
    bridge.exec(f"setblock {ox + 2} {y - 1} {oz} thaumcraft:essentia_jar")
    bridge.call("/essentia/insert", {"x": ox, "y": y, "z": oz, "aspect": "aer", "amount": 10})
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=ox + 2, y=y - 1, z=oz)
    check("oneway: поток по направлению", data.get("aspects", {}).get("aer", 0) >= 8, str(data)[:160])
    ox2, oz2 = ax + 12, az + 3
    bridge.exec(f"setblock {ox2} {y} {oz2} thaumcraft:alembic")
    bridge.exec(f"setblock {ox2 + 1} {y} {oz2} thaumcraft:directional_essence_tube[facing=west]")
    bridge.exec(f"setblock {ox2 + 2} {y} {oz2} thaumcraft:essence_tube")
    bridge.exec(f"setblock {ox2 + 2} {y - 1} {oz2} minecraft:air")
    bridge.exec(f"setblock {ox2 + 2} {y - 1} {oz2} thaumcraft:essentia_jar")
    bridge.call("/essentia/insert", {"x": ox2, "y": y, "z": oz2, "aspect": "aer", "amount": 10})
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=ox2 + 2, y=y - 1, z=oz2)
    check("oneway: обратный поток запрещён", data.get("aspects", {}).get("aer", 0) == 0, str(data)[:160])
    bx2, bz2 = ax + 16, az
    bridge.exec(f"setblock {bx2 + 2} {y} {bz2} minecraft:air")
    bridge.exec(f"setblock {bx2 + 2} {y - 1} {bz2} minecraft:air")
    bridge.exec(f"setblock {bx2} {y} {bz2} thaumcraft:alembic")
    bridge.exec(f"setblock {bx2 + 1} {y} {bz2} thaumcraft:essence_buffer")
    bridge.call("/essentia/insert", {"x": bx2, "y": y, "z": bz2, "aspect": "aer", "amount": 10})
    bridge.wait(100)
    status, data = bridge.get("/essentia", x=bx2 + 1, y=y, z=bz2)
    check("buffer: набрал из алембика", data.get("aspects", {}).get("aer", 0) >= 5, str(data)[:160])
    bridge.exec(f"setblock {bx2 + 2} {y} {bz2} thaumcraft:essence_tube")
    bridge.exec(f"setblock {bx2 + 2} {y - 1} {bz2} minecraft:air")
    bridge.exec(f"setblock {bx2 + 2} {y - 1} {bz2} thaumcraft:essentia_jar")
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=bx2 + 2, y=y - 1, z=bz2)
    check("buffer: отдал в банку", data.get("aspects", {}).get("aer", 0) >= 4, str(data)[:160])

    print("== трубки: клапан, ограничитель, фильтр ==")
    vx, vz = ax + 20, az
    bridge.exec(f"setblock {vx} {y} {vz} thaumcraft:alembic")
    bridge.exec(f"setblock {vx + 1} {y} {vz} thaumcraft:essence_valve")
    bridge.exec(f"setblock {vx + 2} {y} {vz} thaumcraft:essence_tube")
    bridge.exec(f"setblock {vx + 2} {y - 1} {vz} minecraft:air")
    bridge.exec(f"setblock {vx + 2} {y - 1} {vz} thaumcraft:essentia_jar")
    bridge.call("/essentia/insert", {"x": vx, "y": y, "z": vz, "aspect": "aer", "amount": 10})
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=vx + 2, y=y - 1, z=vz)
    check("клапан: открыт — течёт", data.get("aspects", {}).get("aer", 0) >= 8, str(data)[:160])
    bridge.call("/tube/valve", {"x": vx + 1, "y": y, "z": vz, "open": False})
    bridge.call("/essentia/insert", {"x": vx, "y": y, "z": vz, "aspect": "aer", "amount": 5})
    bridge.wait(150)
    status, data = bridge.get("/essentia", x=vx, y=y, z=vz)
    check("клапан: закрыт — не течёт", data.get("aspects", {}).get("aer", 0) >= 4, str(data)[:160])
    bridge.call("/tube/valve", {"x": vx + 1, "y": y, "z": vz, "open": True})
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=vx, y=y, z=vz)
    check("клапан: открыли — дотекло", data.get("aspects", {}).get("aer", 0) <= 1, str(data)[:160])

    rx, rz = ax + 20, az + 3
    bridge.exec(f"setblock {rx} {y} {rz} thaumcraft:essence_tube")
    bridge.exec(f"setblock {rx} {y - 1} {rz} minecraft:air")
    bridge.exec(f"setblock {rx} {y - 1} {rz} thaumcraft:essentia_jar")
    bridge.exec(f"setblock {rx + 1} {y} {rz} thaumcraft:restricted_essence_tube")
    bridge.exec(f"setblock {rx + 2} {y} {rz} thaumcraft:alembic")
    bridge.wait(40)
    status, data = bridge.get("/essentia", x=rx + 1, y=y, z=rz)
    suctions = [side.get("suction", 0) for side in data.get("sides", {}).values()]
    check("ограничитель: сакция вдвое", suctions and 14 <= max(suctions) <= 16, str(data)[:200])

    fx2, fz2 = ax + 20, az + 6
    bridge.exec(f"setblock {fx2} {y} {fz2} thaumcraft:alembic")
    bridge.exec(f"setblock {fx2 + 1} {y} {fz2} thaumcraft:filtered_essence_tube")
    bridge.exec(f"setblock {fx2 + 2} {y} {fz2} thaumcraft:essence_tube")
    bridge.exec(f"setblock {fx2 + 2} {y - 1} {fz2} minecraft:air")
    bridge.exec(f"setblock {fx2 + 2} {y - 1} {fz2} thaumcraft:essentia_jar")
    bridge.call("/tube/filter", {"x": fx2 + 1, "y": y, "z": fz2, "aspect": "aer"})
    bridge.call("/essentia/insert", {"x": fx2, "y": y, "z": fz2, "aspect": "ignis", "amount": 8})
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=fx2 + 2, y=y - 1, z=fz2)
    check("фильтр: чужой аспект не прошёл", data.get("aspects", {}).get("ignis", 0) == 0, str(data)[:160])
    bridge.call("/tube/filter", {"x": fx2 + 1, "y": y, "z": fz2, "aspect": "ignis"})
    bridge.wait(200)
    status, data = bridge.get("/essentia", x=fx2 + 2, y=y - 1, z=fz2)
    check("фильтр: свой аспект прошёл", data.get("aspects", {}).get("ignis", 0) >= 6, str(data)[:160])

    print("== инфузия ==")
    ix, iz = ax + 6, az + 6
    bridge.exec(f"setblock {ix} {y} {iz} thaumcraft:runic_matrix")
    bridge.exec(f"setblock {ix} {y - 2} {iz} thaumcraft:arcane_pedestal")
    for ddx, ddz in ((1, 1), (1, -1), (-1, -1), (-1, 1)):
        bridge.exec(f"setblock {ix + ddx} {y - 2} {iz + ddz} thaumcraft:infusion_pillar")
    bridge.exec(f"setblock {ix + 2} {y} {iz} thaumcraft:arcane_pedestal")
    bridge.exec(f"setblock {ix - 2} {y} {iz} thaumcraft:arcane_pedestal")
    bridge.exec(f"setblock {ix + 3} {y} {iz} thaumcraft:essentia_jar")
    bridge.exec(f"setblock {ix + 3} {y} {iz + 1} thaumcraft:essentia_jar")
    bridge.call("/essentia/insert", {"x": ix + 3, "y": y, "z": iz, "aspect": "aer", "amount": 5})
    bridge.call("/essentia/insert", {"x": ix + 3, "y": y, "z": iz + 1, "aspect": "ignis", "amount": 3})
    bridge.wait(30)
    status, data = bridge.get("/infusion", x=ix, y=y, z=iz)
    check("инфузия: структура", data.get("structureValid") is True and data.get("pedestals") == 2, str(data)[:180])
    bridge.call("/pedestal/set", {"x": ix, "y": y - 2, "z": iz, "item": "thaumcraft:tallow"})
    bridge.call("/pedestal/set", {"x": ix + 2, "y": y, "z": iz, "item": "minecraft:redstone"})
    bridge.call("/pedestal/set", {"x": ix - 2, "y": y, "z": iz, "item": "minecraft:glowstone_dust"})
    bridge.wait(5)
    status, data = bridge.get("/infusion", x=ix, y=y, z=iz)
    check("инфузия: рецепт найден", "matchedRecipe" in data or data.get("crafting") is True, str(data)[:220])
    status, data = bridge.call("/infusion/start", {"x": ix, "y": y, "z": iz})
    check("инфузия: старт", data.get("started") is True or data.get("crafting") is True, str(data)[:180])
    bridge.wait(160)
    status, data = bridge.get("/infusion", x=ix, y=y, z=iz)
    altar = data.get("altar", {}).get("item", {})
    check(
        "инфузия: крафт завершён",
        data.get("crafting") is False
        and altar.get("item") == "thaumcraft:alumentum"
        and altar.get("count") == 2,
        str(data)[:220],
    )

    print("== жезл и виз ==")
    status, data = bridge.call("/wand/give", {**fake, "rod": 0, "cap": 0})
    check("жезл выдан", data.get("maxVis") == 50 and data.get("rod") == 0, str(data)[:160])
    status, data = bridge.call("/wand/vis", {**fake, "aspect": "aer", "amount": 5, "set": True})
    check("виз установлен", data.get("vis", {}).get("aer") == 5, str(data)[:160])
    wdx, wdz = ax - 4, az - 4
    bridge.exec(f"setblock {wdx} {y} {wdz} thaumcraft:aura_node")
    bridge.call("/node/config", {"x": wdx, "y": y, "z": wdz, "aspects": {"aer": 100}})
    status, data = bridge.call("/wand/drain", {**fake, "x": wdx, "y": y, "z": wdz, "amount": 20})
    check(
        "высасывание из узла",
        data.get("drained") == 20
        and data.get("vis", {}).get("aer") == 25
        and data.get("nodeAspects", {}).get("aer") == 80,
        str(data)[:200],
    )

    print("== виз-крафт (арканный верстак) ==")
    wx, wz = ax + 2, az + 2
    bridge.exec(f"setblock {wx} {y} {wz} minecraft:air")
    bridge.exec(f"setblock {wx} {y} {wz} thaumcraft:arcane_workbench")
    bridge.call("/wand/vis", {**fake, "aspect": "aer", "amount": 5, "set": True})
    bridge.call("/menu/open", {**fake, "x": wx, "y": y, "z": wz})
    bridge.call("/items", {"player": "fake", "slot": 1, "item": "minecraft:glowstone_dust", "count": 1})
    bridge.call("/items", {"player": "fake", "slot": 2, "item": "thaumcraft:tallow", "count": 1})
    bridge.call("/menu/click", {**fake, "slot": 27, "type": "PICKUP"})
    bridge.call("/menu/click", {**fake, "slot": 46, "type": "PICKUP"})
    bridge.call("/menu/click", {**fake, "slot": 28, "type": "PICKUP"})
    bridge.call("/menu/click", {**fake, "slot": 36, "type": "PICKUP"})
    bridge.call("/menu/click", {**fake, "slot": 29, "type": "PICKUP"})
    bridge.call("/menu/click", {**fake, "slot": 37, "type": "PICKUP"})
    status, menu = bridge.call("/menu", fake)
    result_slot = next((s for s in menu.get("slots", []) if s.get("slot") == 45), {})
    check("виз-крафт: превью", result_slot.get("item") == "thaumcraft:alumentum", str(result_slot)[:160])
    bridge.call("/menu/click", {**fake, "slot": 45, "type": "PICKUP"})
    bridge.call("/menu/click", {**fake, "slot": 3, "type": "PICKUP"})
    status, data = bridge.call("/wand/item", {"x": wx, "y": y, "z": wz, "slot": 10})
    check("виз-крафт: вис списан", data.get("vis", {}).get("aer", 0) == 3, str(data)[:160])
    bridge.call("/menu/close", fake)

    print("== фокусы и каст ==")
    bridge.call("/wand/give", {**fake, "rod": 0, "cap": 0})
    for aspect in ("ignis", "aer", "aqua", "ordo", "perditio"):
        bridge.call("/wand/vis", {**fake, "aspect": aspect, "amount": 10, "set": True})
    status, data = bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_fire"})
    check("фокус заряжен", data.get("focus") == "thaumcraft:focus_fire", str(data)[:160])
    fx, fz = ax + 8, az - 4
    bridge.exec(f"setblock {fx} {y} {fz} minecraft:oak_planks")
    status, data = bridge.call("/wand/cast", {**fake, "mode": "block", "x": fx, "y": y, "z": fz})
    check("каст: огонь (vis -2)", data.get("cast") is True and data.get("vis", {}).get("ignis") == 8, str(data)[:200])
    status, data = bridge.get("/block", x=fx, y=y + 1, z=fz)
    check("каст: огонь на блоке", "minecraft:fire" in json.dumps(data), str(data)[:200])
    bridge.exec(f"setblock {fx} {y} {fz + 1} minecraft:water")
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_frost"})
    bridge.call("/wand/cast", {**fake, "mode": "block", "x": fx, "y": y, "z": fz + 1})
    status, data = bridge.get("/block", x=fx, y=y, z=fz + 1)
    check("каст: вода → лёд", "minecraft:ice" in json.dumps(data), str(data)[:200])
    bridge.exec("time set night")
    bridge.exec(f"kill @e[type=minecraft:zombie,x={fx},y={y},z={fz},distance=..8]")
    bridge.wait(40)
    bridge.exec(f"setblock {fx} {y - 1} {fz - 2} minecraft:stone")
    bridge.exec(f"summon minecraft:zombie {fx} {y} {fz - 2}")
    bridge.wait(5)
    status, ent = bridge.get("/entities", x=fx, y=y, z=fz - 2, r=3)
    zombie = next((e for e in ent.get("entities", []) if e.get("type") == "minecraft:zombie"), {})
    health_before = zombie.get("health", 20)
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_shock"})
    status, data = bridge.call("/wand/cast", {**fake, "mode": "entity", "x": fx, "y": y, "z": fz - 2})
    bridge.wait(2)
    status, ent2 = bridge.get("/entities", x=fx, y=y, z=fz - 2, r=3)
    zombie2 = next((e for e in ent2.get("entities", []) if e.get("type") == "minecraft:zombie"), {})
    check(
        "каст: урон по зомби",
        data.get("cast") is True and zombie2.get("health", health_before) < health_before,
        f"before={health_before} after={zombie2.get('health')}",
    )

    # доп. фокусы: xp_drain / heal — каждый на свежем зомби (без кадров неуязвимости)
    dzx, dzz = fx + 10, fz - 2
    bridge.exec(f"kill @e[type=minecraft:zombie,x={dzx},y={y},z={dzz},distance=..12]")
    bridge.exec(f"setblock {dzx} {y - 1} {dzz} minecraft:stone")
    bridge.exec(f"summon minecraft:zombie {dzx} {y} {dzz}")
    bridge.wait(10)
    bridge.call("/wand/vis", {**fake, "aspect": "perditio", "amount": 10, "set": True})
    bridge.call("/wand/vis", {**fake, "aspect": "cognitio", "amount": 10, "set": True})
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_xp_drain"})
    status, data = bridge.call("/wand/cast", {**fake, "mode": "entity", "x": dzx, "y": y, "z": dzz})
    bridge.wait(2)
    status, ent3 = bridge.get("/entities", x=dzx, y=y, z=dzz, r=3)
    zombie3 = next((e for e in ent3.get("entities", []) if e.get("type") == "minecraft:zombie"), {})
    health_drained = zombie3.get("health", 20)
    check("xp_drain: урон 1", data.get("cast") is True and health_drained <= 19.0, f"20 -> {health_drained}")

    hzx, hzz = fx + 10, fz + 2
    bridge.exec(f"kill @e[type=minecraft:zombie,x={hzx},y={y},z={hzz},distance=..12]")
    bridge.exec(f"setblock {hzx} {y - 1} {hzz} minecraft:stone")
    bridge.exec(f"summon minecraft:zombie {hzx} {y} {hzz}")
    bridge.wait(10)
    bridge.call("/wand/vis", {**fake, "aspect": "sano", "amount": 10, "set": True})
    bridge.call("/wand/vis", {**fake, "aspect": "victus", "amount": 10, "set": True})
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_heal"})
    status, data = bridge.call("/wand/cast", {**fake, "mode": "entity", "x": hzx, "y": y, "z": hzz})
    bridge.wait(2)
    status, ent4 = bridge.get("/entities", x=hzx, y=y, z=hzz, r=3)
    zombie4 = next((e for e in ent4.get("entities", []) if e.get("type") == "minecraft:zombie"), {})
    check(
        "heal: нежить получает урон",
        data.get("cast") is True and zombie4.get("health", 20) <= 18.0,
        f"cast={data.get('cast')} 20 -> {zombie4.get('health')}",
    )

    print("== доп. фокусы ==")
    ex, ez = ax + 24, az + 6
    for ddx in (-1, 0, 1):
        for ddz in (-1, 0, 1):
            bridge.exec(f"setblock {ex + ddx} {y} {ez + ddz} minecraft:stone")
    bridge.call("/wand/vis", {**fake, "aspect": "terra", "amount": 10, "set": True})
    bridge.call("/wand/vis", {**fake, "aspect": "perditio", "amount": 10, "set": True})
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_excavation"})
    status, data = bridge.call("/wand/cast", {**fake, "mode": "block", "x": ex, "y": y, "z": ez})
    bridge.wait(2)
    broken = 0
    for ddx in (-1, 0, 1):
        for ddz in (-1, 0, 1):
            status, blk = bridge.get("/block", x=ex + ddx, y=y, z=ez + ddz)
            if blk.get("block") == "minecraft:air":
                broken += 1
    check("excavation: слой 3x3 сломан", data.get("cast") is True and broken == 9, f"broken={broken}")

    tx2, tz2 = ax + 24, az + 9
    bridge.call("/player", {**fake, "x": tx2, "y": y, "z": tz2 - 2})
    bridge.exec(f"kill @e[type=minecraft:item,x={tx2},y={y},z={tz2},distance=..6]")
    bridge.exec(f'summon minecraft:item {tx2} {y} {tz2 - 1} {{Item:{{id:"minecraft:diamond",count:1}}}}')
    bridge.wait(5)
    status, ent = bridge.get("/entities", x=tx2, y=y, z=tz2 - 1, r=3)
    near_item = any(e.get("item") == "minecraft:diamond" for e in ent.get("entities", []))
    check("telekinesis: предмет рядом", near_item, str(ent)[:180])
    bridge.call("/wand/vis", {**fake, "aspect": "aer", "amount": 10, "set": True})
    bridge.call("/wand/vis", {**fake, "aspect": "ordo", "amount": 10, "set": True})
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_telekinesis"})
    status, data = bridge.call("/wand/cast", {**fake})
    bridge.wait(10)
    status, items = bridge.call("/items", {**fake, "player": "fake"})
    has_diamond = any(s.get("item") == "minecraft:diamond" for s in items.get("slots", []))
    check("telekinesis: предмет у игрока", data.get("cast") is True and has_diamond, f"cast={data.get('cast')} diamond={has_diamond}")

    bridge.call("/menu/close", fake)
    bridge.call("/wand/vis", {**fake, "aspect": "vacuos", "amount": 10, "set": True})
    bridge.call("/wand/vis", {**fake, "aspect": "motus", "amount": 10, "set": True})
    bridge.call("/wand/focus", {**fake, "item": "thaumcraft:focus_ender_chest"})
    status, data = bridge.call("/wand/cast", {**fake})
    bridge.wait(5)
    status, menu = bridge.call("/menu", fake)
    check(
        "ender chest: меню открылось",
        data.get("cast") is True and len(menu.get("slots", [])) == 63,
        f"cast={data.get('cast')} slots={len(menu.get('slots', []))}",
    )
    bridge.call("/menu/close", fake)

    print("== големы ==")
    gx, gz = ax + 20, az + 4
    for ddx in (-1, 0, 1):
        for ddz in (-1, 0, 1):
            bridge.exec(f"setblock {gx + ddx} {y - 1} {gz + ddz} minecraft:stone")
    # прошлые прогоны оставляют големов — чистим (мгновенно) и убеждаемся, что зона пуста
    bridge.call("/golem/clear", {"x": gx, "y": y, "z": gz, "r": 24.0})
    # ... и контейнеры рядом: иначе голем отнесёт предмет в старый сундук до проверки
    bridge.exec(f"setblock {gx + 3} {y} {gz} minecraft:air")
    bridge.exec(f"setblock {gx + 3} {y - 1} {gz} minecraft:air")
    for ddx in (-1, 0, 1):
        for ddz in (-1, 0, 1):
            bridge.exec(f"setblock {gx + ddx} {y - 1} {gz + ddz} minecraft:stone")
    bridge.exec(f"setblock {gx} {y} {gz} minecraft:air")
    bridge.exec(f"setblock {gx} {y + 1} {gz} minecraft:air")
    bridge.wait(5)
    status, ent0 = bridge.get("/entities", x=gx, y=y, z=gz, r=8)
    stale = [e for e in ent0.get("entities", []) if e.get("type") == "thaumcraft:golem"]
    check("голем: зона очищена", not stale, str(stale)[:200])
    status, data = bridge.call("/golem/spawn", {**fake, "x": gx + 0.5, "y": y, "z": gz + 0.5, "material": "iron"})
    golem_uuid = data.get("uuid", "")
    check(
        "голем: заспавнен",
        data.get("ok") is True and data.get("material") == "iron" and bool(golem_uuid) and data.get("health", 0) > 0,
        str(data)[:180],
    )
    status, data = bridge.call("/golem/task", {"x": gx, "y": y, "z": gz, "task": "gather", "uuid": golem_uuid})
    check(
        "голем: задача gather",
        data.get("task") == "gather" and data.get("uuid") == golem_uuid,
        str(data)[:180],
    )
    bridge.exec(f"kill @e[type=minecraft:item,x={gx},y={y},z={gz},distance=..6]")
    bridge.exec(f'summon minecraft:item {gx + 1} {y} {gz} {{Item:{{id:"minecraft:diamond",count:1}}}}')
    bridge.wait(40)
    status, data = bridge.call("/golem", {"x": gx, "y": y, "z": gz, "uuid": golem_uuid})
    check(
        "голем: подобрал предмет",
        data.get("carried") == "minecraft:diamond" and data.get("health", 0) > 0,
        str(data)[:200],
    )
    # доставка: рядом ставим сундук, голем должен отнести туда ношу
    bridge.exec(f"setblock {gx + 3} {y} {gz} minecraft:air")
    bridge.exec(f"setblock {gx + 3} {y - 1} {gz} minecraft:stone")
    bridge.exec(f"setblock {gx + 3} {y} {gz} minecraft:chest")
    bridge.wait(120)
    status, chest = bridge.get("/items", x=gx + 3, y=y, z=gz)
    delivered = any(s.get("item") == "minecraft:diamond" for s in chest.get("slots", []))
    check("голем: доставка в сундук", delivered, str(chest)[:200])
    status, data = bridge.call("/golem", {"x": gx, "y": y, "z": gz, "uuid": golem_uuid})
    check("голем: руки пусты после доставки", data.get("carried") == "", str(data)[:200])

    print("== колокол големанта ==")
    bx2, bz2 = ax + 26, az + 4
    bridge.call("/golem/clear", {"x": bx2, "y": y, "z": bz2, "r": 8.0})
    bridge.exec(f"setblock {bx2} {y - 1} {bz2} minecraft:stone")
    bridge.exec(f"setblock {bx2} {y} {bz2} minecraft:iron_block")
    bridge.exec(f"setblock {bx2} {y + 1} {bz2} minecraft:air")
    bridge.wait(2)
    bridge.call("/hand", {**fake, "item": "thaumcraft:golem_bell"})
    status, data = bridge.call("/use", {**fake, "x": bx2, "y": y, "z": bz2, "side": "up"})
    bridge.wait(5)
    status, blk = bridge.get("/block", x=bx2, y=y, z=bz2)
    status, ent = bridge.get("/entities", x=bx2, y=y, z=bz2, r=6)
    bell_golem = next((e for e in ent.get("entities", []) if e.get("type") == "thaumcraft:golem"), {})
    check(
        "колокол: голем из блока",
        data.get("ok") is True and blk.get("block") == "minecraft:air" and bool(bell_golem),
        f"use={data} block={blk.get('block')} golem={bell_golem}",
    )
    bridge.call("/hand", {**fake, "item": "none"})

    print("== ядра големов ==")
    cx, cz = bx2, bz2
    bridge.call("/golem/clear", {"x": cx, "y": y, "z": cz, "r": 8.0})
    status, data = bridge.call("/golem/spawn", {**fake, "x": cx + 0.5, "y": y, "z": cz + 0.5, "material": "wood"})
    core_uuid = data.get("uuid", "")
    bridge.call("/hand", {**fake, "item": "thaumcraft:golem_core_gather"})
    status, data = bridge.call("/use/entity", {**fake, "x": cx, "y": y, "z": cz, "r": 4})
    status, golem = bridge.call("/golem", {"uuid": core_uuid, "x": cx, "y": y, "z": cz})
    check(
        "ядро: gather ставит задачу",
        data.get("ok") is True and golem.get("task") == "gather",
        f"use={data} golem={golem}",
    )
    bridge.call("/hand", {**fake, "item": "thaumcraft:golem_core_empty"})
    status, data = bridge.call("/use/entity", {**fake, "x": cx, "y": y, "z": cz, "r": 4})
    status, golem = bridge.call("/golem", {"uuid": core_uuid, "x": cx, "y": y, "z": cz})
    check(
        "ядро: empty снимает задачу",
        data.get("ok") is True and golem.get("task") == "none",
        f"use={data} golem={golem}",
    )
    bridge.call("/hand", {**fake, "item": "none"})

    print("== 404 ==")
    status, _data = bridge.call("/definitely_unknown_endpoint")
    check("404 неизвестный эндпоинт", status == 404)
    print()
    print(f"ИТОГО: {PASS} PASS, {FAIL} FAIL")
    if FAILURES:
        print("Провалы: " + ", ".join(FAILURES))
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
