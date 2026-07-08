#!/usr/bin/env python3
from pathlib import Path
import re
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage211/Thaumcraft4-1.7.10-master')


def read(rel: str, root: Path = ROOT) -> str:
    path = root / rel
    if not path.exists():
        raise AssertionError(f"missing file: {rel}")
    return path.read_text(encoding='utf-8', errors='ignore')


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f"missing {label}: {needle}")


def require_re(text: str, pattern: str, label: str) -> None:
    if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
        raise AssertionError(f"missing {label}: /{pattern}/")


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, "net.minecraftforge:forge:1.19.2-43", 'Forge 1.19.2 dependency')
    require_re(build, r"version = '(2\.(1[2-9]|[2-9][0-9])|3\.[0-9]+)\.0'", 'Stage212 Gradle version')
    require_re(mods, r'version="(2\.(1[2-9]|[2-9][0-9])|3\.[0-9]+)\.0"', 'Stage212 mods.toml version')

    if (ORIG / 'thaumcraft/common/lib/events/EventHandlerRunic.java').exists():
        orig_runic = read('thaumcraft/common/lib/events/EventHandlerRunic.java', ORIG)
        for needle, label in [
            ('field_77990_d.func_74764_b("mask")', 'original mask NBT checks'),
            ('field_77990_d.func_74762_e("mask") == 2', 'Sipping Fiend branch'),
            ('leecher.func_70691_i(1.0F)', 'Sipping Fiend heal amount'),
            ('field_77990_d.func_74762_e("mask") == 1', 'Angry Ghost branch'),
            ('Potion.field_82731_v', 'Angry Ghost wither potion'),
            ('IEldritchMob', 'eldritch shield FX branch'),
            ('ChampionModifier.mods[t].type', 'champion modifier branch'),
            ('new PacketFXShield', 'shield FX packet original'),
        ]:
            require(orig_runic, needle, label)

        orig_warp = read('thaumcraft/common/lib/WarpEvents.java', ORIG)
        require(orig_warp, 'field_77990_d.func_74762_e("mask") == 0', 'Grinning Devil warp damping original')
        require(orig_warp, 'eff -= 2 + player.field_70170_p.field_73012_v.nextInt(4)', 'warp severity reduction')

        orig_recipes = read('thaumcraft/common/config/ConfigRecipes.java', ORIG)
        for needle, label in [
            ('new Object[] { "goggles", new NBTTagByte(1) }', 'HelmGoggles NBT output'),
            ('new Object[] { "mask", new NBTTagInt(0) }', 'Grinning mask NBT output'),
            ('new Object[] { "mask", new NBTTagInt(1) }', 'Angry mask NBT output'),
            ('new Object[] { "mask", new NBTTagInt(2) }', 'Sipping mask NBT output'),
        ]:
            require(orig_recipes, needle, label)
    else:
        # External TC4 source extraction is not present in GitHub CI; validate
        # the carried Stage212 source anchors and generated recipes instead.
        stage_doc = read('docs/TC4_FORTRESS_MASK_CURIOS_STAGE212.md')
        stage_report = read('STAGE212_TC4_FORTRESS_MASK_CURIOS_REPORT.json')
        for needle, label in [
            ('EventHandlerRunic#entityHurt', 'carried mask runtime source anchor'),
            ('WarpEvents', 'carried warp source anchor'),
            ('ConfigRecipes', 'carried recipe source anchor'),
            ('NBTTagByte(1)', 'carried goggles NBT output anchor'),
            ('NBTTagInt(0)', 'carried Grinning mask NBT output anchor'),
            ('NBTTagInt(1)', 'carried Angry mask NBT output anchor'),
            ('NBTTagInt(2)', 'carried Sipping mask NBT output anchor'),
            ('Sipping Fiend', 'carried Sipping Fiend branch'),
            ('Angry Ghost', 'carried Angry Ghost branch'),
            ('Grinning Devil', 'carried Grinning Devil branch'),
            ('champion/eldritch shield FX', 'carried champion/eldritch branch'),
        ]:
            if needle not in stage_doc and needle not in stage_report:
                raise AssertionError(f'missing {label}: {needle}')

    material = read('src/main/java/com/darkifov/thaumcraft/block/TC4FortressArmorMaterial.java')
    require(material, 'implements ArmorMaterial', '1.19.2 ArmorMaterial adapter')
    require(material, 'getToughness', 'fortress toughness')
    require(material, 'tc4_thaumiumingot', 'thaumium repair ingredient')

    armor = read('src/main/java/com/darkifov/thaumcraft/block/TC4FortressArmorItem.java')
    for needle, label in [
        ('extends ArmorItem', 'wearable fortress armor'),
        ('GOGGLES', 'goggles tooltip branch'),
        ('mask(stack)', 'mask tooltip branch'),
        ('TC4RunicArmorHelper.appendTooltip', 'runic charge tooltip'),
        ('isFireResistant', 'fire/magic protection proxy'),
    ]:
        require(armor, needle, label)

    mask_runtime = read('src/main/java/com/darkifov/thaumcraft/runic/TC4FortressMaskRuntime.java')
    for needle, label in [
        ('GOGGLES_TAG = "goggles"', 'goggles tag constant'),
        ('MASK_TAG = "mask"', 'mask tag constant'),
        ('MASK_GRINNING_DEVIL = 0', 'mask 0 constant'),
        ('MASK_ANGRY_GHOST = 1', 'mask 1 constant'),
        ('MASK_SIPPING_FIEND = 2', 'mask 2 constant'),
        ('event.getAmount() / 12.0F', 'Sipping Fiend chance'),
        ('leecher.heal(1.0F)', 'Sipping Fiend heal'),
        ('event.getAmount() / 10.0F', 'Angry Ghost chance'),
        ('MobEffects.WITHER, 80', 'Angry Ghost wither'),
        ('EldritchGuardianEntity', 'eldritch shield FX'),
        ('TC4_CHAMPION_MOD_TAG', 'champion bridge tag'),
        ('championMod == 5', 'shielded champion branch'),
        ('sendRunicShieldFx', 'PacketFXShield adapter'),
    ]:
        require(mask_runtime, needle, label)

    warp = read('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
    require(warp, 'TC4FortressMaskRuntime.hasGrinningDevil(player)', 'Grinning Devil warp hook')
    require(warp, 'eff -= 2 + player.getRandom().nextInt(4)', 'Grinning Devil severity reduction')

    common = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
    require(common, 'TC4FortressMaskRuntime.handleHurt(event)', 'mask hurt hook')

    adapter = read('src/main/java/com/darkifov/thaumcraft/runic/TC4BaubleSlotAdapter.java')
    for needle, label in [
        ('CURIOS_API_CLASS', 'Curios reflective adapter'),
        ('LEGACY_BAUBLES_API_CLASS', 'Baubles reflective adapter'),
        ('Class.forName', 'optional no-hard-dependency loading'),
        ('TC4_BAUBLE_SLOT_LIMIT = 4', 'TC4 bauble slot cap'),
    ]:
        require(adapter, needle, label)

    runic = read('src/main/java/com/darkifov/thaumcraft/runic/TC4RunicShieldRuntime.java')
    require(runic, 'TC4BaubleSlotAdapter.findEquippedBaubles(player)', 'runic shield Curios/Baubles scan')
    require(runic, 'fxTargetId', 'public shield FX target sentinel helper')

    items = read('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
    for needle, label in [
        ('TC4FortressArmorItem', 'fortress item registration'),
        ('EquipmentSlot.HEAD', 'helmet registration'),
        ('TC4FortressMaskItem', 'mask sprite registration'),
    ]:
        require(items, needle, label)

    recipe = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java')
    for needle, label in [
        ('output_nbt_label', 'NBT output json label'),
        ('ByteTag.valueOf', 'NBT byte output'),
        ('IntTag.valueOf', 'NBT int output'),
        ('public Tag outputNbt()', 'generic NBT Tag output'),
    ]:
        require(recipe, needle, label)

    recipes = {
        'tc4_helm_goggles.json': ('goggles', 'byte', 1, 'HELMGOGGLES'),
        'tc4_mask_grinning_devil.json': ('mask', 'int', 0, 'MASKGRINNINGDEVIL'),
        'tc4_mask_angry_ghost.json': ('mask', 'int', 1, 'MASKANGRYGHOST'),
        'tc4_mask_sipping_fiend.json': ('mask', 'int', 2, 'MASKSIPPINGFIEND'),
    }
    for name, (label, typ, value, research) in recipes.items():
        data = json.loads(read('src/main/resources/data/thaumcraft/thaumcraft_infusion/' + name))
        if data['research'] != research:
            raise AssertionError(f'{name} research mismatch')
        result = data['result']
        if result.get('output_nbt_label') != label or result.get('output_nbt_type') != typ or result.get('output_nbt_value') != value:
            raise AssertionError(f'{name} NBT output mismatch')
        catalyst = data['catalyst']
        if catalyst.get('item') != 'thaumcraft:tc4_thaumiumfortresshelm' or not catalyst.get('damage_wildcard'):
            raise AssertionError(f'{name} catalyst wildcard mismatch')

    # Newly touched files must remain 1.19.2 source-compatible and avoid direct TC4 1.7.10 APIs.
    touched = [
        'src/main/java/com/darkifov/thaumcraft/block/TC4FortressArmorItem.java',
        'src/main/java/com/darkifov/thaumcraft/block/TC4FortressArmorMaterial.java',
        'src/main/java/com/darkifov/thaumcraft/block/TC4FortressMaskItem.java',
        'src/main/java/com/darkifov/thaumcraft/runic/TC4FortressMaskRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/runic/TC4BaubleSlotAdapter.java',
        'src/main/java/com/darkifov/thaumcraft/runic/TC4RunicShieldRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java',
    ]
    forbidden = ['net.minecraft.item.ItemStack', 'net.minecraft.nbt.NBTTag', 'thaumcraft.api.', 'func_', 'field_']
    for rel in touched:
        text = read(rel)
        for needle in forbidden:
            if needle in text:
                raise AssertionError(f'1.19.2 guard failed: {needle} appears in {rel}')

    print('Stage212 fortress mask + Curios/Baubles 1.19.2 parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage212 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
