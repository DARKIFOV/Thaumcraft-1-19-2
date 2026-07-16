#!/usr/bin/env python3
"""Reject v11.62.70 GUI offsets, truncated infusion rings and missing TC4 recipes."""
from __future__ import annotations
import json, re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]; checks=0

def text(rel):
    global checks
    checks+=1
    p=ROOT/rel
    if not p.is_file(): errors.append(f"missing: {rel}"); return ""
    return p.read_text(encoding="utf-8")

build=text("build.gradle")
for token in ("version = '11.62.92'",):
    checks+=1
    if token not in build: errors.append(f"build.gradle missing {token}")
mods=text("src/main/resources/META-INF/mods.toml")
checks+=1
if 'version="11.62.92"' not in mods: errors.append("mods.toml version mismatch")

book=text("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java")
required=(
 'page.recipe() ? -4 : aspectPage ? -8 : -15',
 'topPos - 6.0F * scale',
 'x + 48, y + 28', 'x + 48, y + 94', 'x + 56, y + 102',
 '360.0D / components.length', 'for (String component : components)',
 'renderInfusionAspectCostIcons', 'y + 164 - 10 * rows')
for token in required:
    checks+=1
    if token not in book: errors.append(f"book missing {token}")
for token in ('Math.min(8, components.length)', 'page.recipe() ? x - 4 : x'):
    checks+=1
    if token in book: errors.append(f"book contains regression {token}")

resolver=text("src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java")
for token in ('itemTrunkSpawner", "thaumcraft:tc4_travel_trunk', 'blockJar:1", "thaumcraft:tc4_jar_brain'):
    checks+=1
    if token not in resolver: errors.append(f"resolver missing {token}")

recipe_dir=ROOT/'src/main/resources/data/thaumcraft/thaumcraft_infusion'
files=sorted(recipe_dir.glob('*.json'))
manager=text("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipeManager.java")
listed=re.findall(r'"([a-z0-9_]+\.json)"',manager)
checks+=1
if set(listed)!={p.name for p in files}: errors.append("manager list/resource set mismatch")
keys={}
for p in files:
    checks+=1
    data=json.loads(p.read_text(encoding='utf-8'))
    if data.get('tc4_key'): keys[data['tc4_key']]=p.name
for key in ('JarBrain','Mirror','MirrorHand','MirrorEssentia','TravelTrunk'):
    checks+=1
    if key not in keys: errors.append(f"missing original infusion key {key}")
checks+=1
if len(keys)!=70: errors.append(f"expected 70 strict materialized infusion keys (63 originals with AdvancedGolem expanded to 8), got {len(keys)}")
checks+=1
if len(files)!=78: errors.append(f"expected 78 bundled JSON resources, got {len(files)}")

if errors:
    print(f"TC4 11.62.92 original GUI/infusion guard: FAIL ({len(errors)} problems)")
    for e in errors: print(' -',e)
    raise SystemExit(1)
print(f"TC4 11.62.92 original GUI/infusion guard: PASS ({checks} checks)")
