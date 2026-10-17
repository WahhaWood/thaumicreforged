import json, pathlib, zipfile

# Тигель по принципу ванильного котла: тело на ножках, зазор снизу, полые стенки 2px,
# открытый верх с бортом и видимая внутренность. Текстуры TC4 (crucible1..6).
W = 2       # толщина стенок
BODY = 3    # низ тела (высота ножек), как у ванильного котла
INNER = 4   # внутреннее дно (0.25 блока)

def face(tex, uv, cull=None, light=False):
    f = {"texture": tex, "uv": uv}
    if cull:
        f["cullface"] = cull
    if light:
        f["neoforge_data"] = {"sky_light": 15, "block_light": 0, "ambient_occlusion": False}
    return f

elements = []

# --- стенки тела (y=BODY..16) ---
elements.append({"from": [0, BODY, 0], "to": [16, 16, W], "faces": {
    "north": face("#outer", [0, 0, 16, 16], "north"),
    "south": face("#inner", [0, 0, 16, 16], light=True),
    "up": face("#top", [0, 0, 16, 2]),
    "down": face("#bottom", [0, 14, 16, 16]),
    "west": face("#outer", [0, 0, 2, 16]),
    "east": face("#outer", [14, 0, 16, 16]),
}})
elements.append({"from": [0, BODY, 16 - W], "to": [16, 16, 16], "faces": {
    "south": face("#outer", [0, 0, 16, 16], "south"),
    "north": face("#inner", [0, 0, 16, 16], light=True),
    "up": face("#top", [0, 0, 16, 2]),
    "down": face("#bottom", [0, 14, 16, 16]),
    "west": face("#outer", [0, 0, 2, 16]),
    "east": face("#outer", [14, 0, 16, 16]),
}})
elements.append({"from": [0, BODY, W], "to": [W, 16, 16 - W], "faces": {
    "west": face("#outer", [0, 0, 16, 16], "west"),
    "east": face("#inner", [0, 0, 16, 16], light=True),
    "up": face("#top", [0, 0, 2, 16]),
    "down": face("#bottom", [0, 14, 16, 16]),
}})
elements.append({"from": [16 - W, BODY, W], "to": [16, 16, 16 - W], "faces": {
    "east": face("#outer", [0, 0, 16, 16], "east"),
    "west": face("#inner", [0, 0, 16, 16], light=True),
    "up": face("#top", [0, 0, 2, 16]),
    "down": face("#bottom", [0, 14, 16, 16]),
}})

# --- внутреннее дно ---
elements.append({"from": [W, BODY, W], "to": [16 - W, INNER, 16 - W], "faces": {
    "up": face("#inner_bottom", [2, 2, 14, 14], light=True),
    "down": face("#inner_bottom", [2, 2, 14, 14]),
}})

# --- ножки (y=0..BODY), как у ванильного котла ---
legs = [
    ([0, 0, 0], [4, BODY, 2], ("north", "east", "south", "west")),
    ([0, 0, 2], [2, BODY, 4], ("east", "south", "west")),
    ([12, 0, 0], [16, BODY, 2], ("north", "east", "south", "west")),
    ([14, 0, 2], [16, BODY, 4], ("east", "south", "west")),
    ([0, 0, 14], [4, BODY, 16], ("north", "east", "south", "west")),
    ([0, 0, 12], [2, BODY, 14], ("north", "east", "west")),
    ([12, 0, 14], [16, BODY, 16], ("north", "east", "south", "west")),
    ([14, 0, 12], [16, BODY, 14], ("north", "east", "west")),
]
for frm, to, sides in legs:
    w = to[0] - frm[0]
    d = to[2] - frm[2]
    faces = {}
    for s in sides:
        if s in ("north", "south"):
            faces[s] = face("#outer", [0, 13, w, 16])
        elif s in ("west", "east"):
            faces[s] = face("#outer", [0, 13, d, 16])
        else:
            faces[s] = face("#inner", [0, 0, w, d])
    faces["down"] = face("#bottom", [0, 13, w, 16])
    elements.append({"from": frm, "to": to, "faces": faces})

model = {
    "ambientocclusion": False,
    "render_type": "minecraft:cutout",
    "textures": {
        "particle": "thaumcraft:block/crucible3",
        "top": "thaumcraft:block/crucible1",
        "bottom": "thaumcraft:block/crucible2",
        "outer": "thaumcraft:block/crucible3",
        "inner": "thaumcraft:block/crucible5",
        "inner_bottom": "thaumcraft:block/crucible6",
    },
    "elements": elements,
}
with zipfile.ZipFile("build/moddev/artifacts/neoforge-21.1.252-client-extra-aka-minecraft-resources.jar") as jar:
    model["display"] = json.loads(jar.read("assets/minecraft/models/block/block.json"))["display"]

pathlib.Path("src/main/resources/assets/thaumcraft/models/block/crucible.json").write_text(
    json.dumps(model, indent=2) + "\n", encoding="utf-8")
print("crucible.json: тело на ножках (как ванильный котёл), элементов:", len(elements))

# --- таумометр: вменяемые позы (ванильные блочные, масштаб под размер модели 2.8 блока) ---
p = pathlib.Path("src/main/resources/assets/thaumcraft/models/item/thaumometer.json")
d = json.loads(p.read_text(encoding="utf-8"))
d["display"] = {
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.13, 0.13, 0.13]},
    "thirdperson_lefthand": {"rotation": [75, -45, 0], "translation": [0, 2.5, 0], "scale": [0.13, 0.13, 0.13]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.18, 0.18, 0.18]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [-1.13, 3.2, 1.13], "scale": [0.18, 0.18, 0.18]},
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.25, 0.25, 0.25]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.06, 0.06, 0.06]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.12, 0.12, 0.12]},
}
p.write_text(json.dumps(d, indent=2) + "\n", encoding="utf-8")
print("thaumometer.json: позы как у блока, масштаб под модель")
