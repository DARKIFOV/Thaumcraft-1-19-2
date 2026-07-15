#!/usr/bin/env python3
"""Static release guard for v11.62.69 Arcane Spa GUI and fluid isolation."""
from __future__ import annotations

import argparse
import json
import pathlib
import sys
from typing import Any


def read(root: pathlib.Path, relative: str) -> str:
    return (root / relative).read_text(encoding="utf-8")


def read_json(root: pathlib.Path, relative: str) -> Any:
    return json.loads(read(root, relative))


def all_in(text: str, tokens: list[str]) -> bool:
    return all(token in text for token in tokens)


def add(checks: list[dict[str, Any]], name: str, passed: bool, details: str) -> None:
    checks.append({"name": name, "passed": bool(passed), "details": details})


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--version", default="11.62.69")
    parser.add_argument("--json-out", default="reports/tc4_arcane_spa_gui_audit_v11.62.69.json")
    args = parser.parse_args()

    root = pathlib.Path(args.root).resolve()
    checks: list[dict[str, Any]] = []

    build = read(root, "build.gradle")
    mod = read(root, "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
    block = read(root, "src/main/java/com/darkifov/thaumcraft/block/ArcaneSpaBlock.java")
    be = read(root, "src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneSpaBlockEntity.java")
    menu = read(root, "src/main/java/com/darkifov/thaumcraft/menu/ArcaneSpaMenu.java")
    screen = read(root, "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneSpaScreen.java")
    client = read(root, "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
    en = read_json(root, "src/main/resources/assets/thaumcraft/lang/en_us.json")
    ru = read_json(root, "src/main/resources/assets/thaumcraft/lang/ru_ru.json")
    custom_tag = read_json(root, "src/main/resources/data/thaumcraft/tags/fluids/purifying_fluid.json")
    arcane = read_json(root, "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanespa.json")
    alchemy = read_json(root, "src/main/resources/data/thaumcraft/thaumcraft_alchemy/tc4_bathsalts.json")

    add(checks, "version", f"version = '{args.version}'" in build, args.version)
    add(checks, "menu_registered", all_in(mod, [
        "import com.darkifov.thaumcraft.menu.ArcaneSpaMenu;",
        "RegistryObject<MenuType<ArcaneSpaMenu>> ARCANE_SPA_MENU",
        'MENUS.register("arcane_spa"',
        "new ArcaneSpaMenu(windowId, inv, data)",
    ]), "Forge MenuType registration")
    add(checks, "screen_registered", all_in(client, [
        "import com.darkifov.thaumcraft.client.screen.ArcaneSpaScreen;",
        "MenuScreens.register(ThaumcraftMod.ARCANE_SPA_MENU.get()",
        "screenConstructor(ArcaneSpaScreen::new)",
    ]), "client screen registration")

    add(checks, "block_opens_gui", all_in(block, [
        "player instanceof ServerPlayer", "NetworkHooks.openScreen(serverPlayer, spa",
        "buffer.writeBlockPos(pos)",
    ]), "normal right click opens the original-style container")
    add(checks, "temporary_direct_controls_removed", all(token not in block for token in [
        "player.isShiftKeyDown()", "FluidUtil", "insertBathSalts", "removeBathSalts()",
    ]), "mode and salt handling moved into ContainerSpa/GuiSpa")

    add(checks, "menu_provider", all_in(be, [
        "implements MenuProvider", 'Component.translatable("container.thaumcraft.arcane_spa")',
        "new ArcaneSpaMenu(id, inventory, this, menuData)",
    ]), "block entity supplies its menu")
    add(checks, "menu_data_contract", all_in(be, [
        "private final ContainerData menuData", "case 0 -> mixing ? 1 : 0",
        "case 1 -> tank.getFluidAmount()", "Registry.FLUID.getId",
        "getCount()", "return 3",
    ]), "mix state, amount and fluid id are synchronised")
    add(checks, "spa_slot_original_position", all_in(menu, [
        "new SlotItemHandler(spa.saltsHandler(), 0, 65, 31)",
        "stack.is(ThaumcraftMod.BATH_SALTS.get())",
    ]), "one Bath Salts slot at original ContainerSpa coordinates")
    add(checks, "player_inventory_original_position", all_in(menu, [
        "8 + col * 18, 84 + row * 18", "8 + col * 18, 142",
    ]), "3x9 inventory and hotbar coordinates match TC4")
    add(checks, "shift_click_contract", all_in(menu, [
        "quickMoveStack", "stack.is(ThaumcraftMod.BATH_SALTS.get())",
        "moveItemStackTo(stack, 0, SPA_SLOT_COUNT, false)",
    ]), "Bath Salts shift-click into the single machine slot")
    add(checks, "toggle_packet", all_in(menu, [
        "BUTTON_TOGGLE_MIX = 1", "clickMenuButton", "spa.toggleMixing()",
    ]), "original container button id 1")

    add(checks, "original_gui_texture", all_in(screen, [
        'new ResourceLocation("thaumcraft", "textures/gui/gui_spa.png")',
        "imageWidth = 176", "imageHeight = 166",
    ]) and (root / "src/main/resources/assets/thaumcraft/textures/gui/gui_spa.png").is_file(),
        "original 256x256 gui_spa texture and 176x166 viewport")
    add(checks, "toggle_icon_coordinates", all_in(screen, [
        "TOGGLE_X = 89", "TOGGLE_Y = 35", "TOGGLE_SIZE = 8",
        "menu.isMixing() ? 16 : 32", "208, iconV",
    ]), "TC4 mode icon atlas coordinates")
    add(checks, "tank_gauge_coordinates", all_in(screen, [
        "TANK_X = 107", "TANK_Y = 15", "TANK_WIDTH = 10", "TANK_HEIGHT = 48",
        "232, 0, 10, 55",
    ]), "TC4 tank gauge and frame coordinates")
    add(checks, "dynamic_fluid_render", all_in(screen, [
        "IClientFluidTypeExtensions.of(fluid.getFluid())", "getStillTexture()",
        "getTintColor()", "InventoryMenu.BLOCK_ATLAS", "TextureAtlasSprite",
    ]), "tank gauge renders any stored Forge fluid")
    add(checks, "gui_tooltips", all_in(screen, [
        "fluid.getDisplayName()", 'menu.fluidAmount() + " mB"',
        '"text.spa.mix.true"', '"text.spa.mix.false"',
    ]), "fluid and mode tooltips")
    add(checks, "gui_button_sound", all_in(screen, [
        "handleInventoryButtonClick", "ArcaneSpaMenu.BUTTON_TOGGLE_MIX",
        'TC4Sounds.event("cameraclack")', "0.4F, 1.0F",
    ]), "original camera-clack button feedback")

    vanilla_water_tag = root / "src/main/resources/data/minecraft/tags/fluids/water.json"
    add(checks, "not_in_vanilla_water_tag", not vanilla_water_tag.exists(),
        "Purifying Fluid no longer participates in #minecraft:water")
    add(checks, "isolated_fluid_tag", set(custom_tag.get("values", [])) == {
        "thaumcraft:purifying_fluid", "thaumcraft:flowing_purifying_fluid"
    }, "dedicated #thaumcraft:purifying_fluid tag")

    add(checks, "translations", en.get("container.thaumcraft.arcane_spa") == "Arcane Spa"
        and ru.get("container.thaumcraft.arcane_spa") == "Магическая ванна",
        "English and Russian container names")

    add(checks, "arcane_spa_recipe", arcane.get("research") == "ARCANESPA"
        and arcane.get("pattern") == ["QIQ", "SJS", "SPS"]
        and arcane.get("aspects") == {"AQUA": 16, "ORDO": 8, "TERRA": 4},
        "1/1 original Arcane Spa arcane recipe")
    add(checks, "bath_salts_recipe", alchemy.get("research") == "BATHSALTS"
        and alchemy.get("catalyst") == "thaumcraft:tc4_dust"
        and alchemy.get("aspects") == {"COGNITIO": 6, "AURAM": 6, "ORDO": 6, "SANO": 6},
        "1/1 original Bath Salts crucible recipe")

    failures = [check for check in checks if not check["passed"]]
    result = {
        "version": args.version,
        "status": "PASS" if not failures else "FAIL",
        "passed": len(checks) - len(failures),
        "total": len(checks),
        "checks": checks,
        "failures": failures,
        "scope": "static source/resource guard; not a Forge compile or runtime proof",
        "recipe_coverage": {
            "original_registrations": 2,
            "mapped": 2,
            "arcane": ["ArcaneSpa"],
            "alchemy": ["BathSalts"],
            "world_interactions_not_counted_as_recipes": [
                "Bath Salts expiration in source water",
                "Arcane Spa fluid placement"
            ],
        },
        "known_deferred": [
            "successful Forge compileJava/build",
            "client fluid animation and GUI click runtime test",
            "dedicated-server NBT/menu synchronization test",
            "hopper/pipe automation and 5x5 output runtime test",
        ],
    }

    out = pathlib.Path(args.json_out)
    if not out.is_absolute():
        out = root / out
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    sys.exit(main())
