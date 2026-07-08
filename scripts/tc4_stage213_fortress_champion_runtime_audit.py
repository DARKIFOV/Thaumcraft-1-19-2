#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage213/Thaumcraft4-1.7.10-master')
if not ORIG.exists():
    ORIG = Path('/mnt/data/tc4_orig_stage211/Thaumcraft4-1.7.10-master')


def read(rel: str, root: Path = ROOT) -> str:
    path = root / rel
    if not path.exists():
        raise AssertionError(f'missing file: {rel}')
    return path.read_text(encoding='utf-8', errors='ignore')


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f'missing {label}: {needle}')


def require_re(text: str, pattern: str, label: str) -> None:
    if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
        raise AssertionError(f"missing {label}: /{pattern}/")


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, "net.minecraftforge:forge:1.19.2-43", 'Forge 1.19.2 dependency')
    require_re(build, r"version = '(2\.(1[3-9]|[2-9][0-9])|3\.[0-9]+)\.0'", 'Stage213 Gradle version')
    require_re(mods, r'version="(2\.(1[3-9]|[2-9][0-9])|3\.[0-9]+)\.0"', 'Stage213 mods.toml version')

    if (ORIG / 'thaumcraft/common/items/armor/ItemFortressArmor.java').exists():
        orig_fortress = read('thaumcraft/common/items/armor/ItemFortressArmor.java', ORIG)
        for needle, label in [
            ('ratio = this.field_77879_b / 25.0D', 'ordinary fortress armor ratio'),
            ('ratio = this.field_77879_b / 35.0D', 'unblockable fortress armor ratio'),
            ('ratio = this.field_77879_b / 20.0D', 'fire/explosion fortress armor ratio'),
            ('double set = 0.875D', 'fortress set base'),
            ('set += 0.125D', 'fortress set piece bonus'),
            ('set += 0.05D', 'fortress mask armor bonus'),
            ('ModelFortressArmor', 'original fortress model'),
            ('fortress_armor.png', 'original fortress texture'),
            ('IRevealer', 'revealer/goggles behavior'),
        ]:
            require(orig_fortress, needle, label)

        orig_runic = read('thaumcraft/common/lib/events/EventHandlerRunic.java', ORIG)
        for needle, label in [
            ('ChampionModifier.mods[t].type == 2', 'defensive champion hook'),
            ('ChampionModifier.mods[t].type == 1', 'offensive champion hook'),
            ('getFinalWarp', 'IWarpingGear helper'),
            ('getFinalCharge', 'IRunicArmor helper'),
        ]:
            require(orig_runic, needle, label)

        orig_mods = read('thaumcraft/common/entities/monster/mods/ChampionModifier.java', ORIG)
        for name in ['bold', 'spine', 'armor', 'mighty', 'grim', 'warded', 'warp', 'undying', 'fiery', 'sickly', 'venomous', 'vampiric', 'infested']:
            require(orig_mods, f'"{name}"', f'original champion modifier {name}')
    else:
        stage_doc = read('docs/TC4_FORTRESS_CHAMPION_RUNTIME_STAGE213.md')
        stage_report = read('STAGE213_TC4_FORTRESS_CHAMPION_RUNTIME_REPORT.json')
        for needle, label in [
            ('ItemFortressArmor', 'carried fortress armor source anchor'),
            ('ChampionModifier', 'carried champion modifier source anchor'),
            ('ordinary damage: defense / `25.0D`', 'ordinary fortress armor divisor'),
            ('bypass armor: defense / `35.0D`', 'unblockable fortress armor divisor'),
            ('fire/explosion/magic: defense / `20.0D`', 'fire/explosion/magic fortress armor divisor'),
            ('base `0.875D`', 'fortress set base'),
            ('+0.125D', 'fortress set piece bonus'),
            ('+0.05D', 'fortress mask bonus'),
            ('bold', 'champion bold anchor'),
            ('spine', 'champion spine anchor'),
            ('armor', 'champion armor anchor'),
            ('mighty', 'champion mighty anchor'),
            ('grim', 'champion grim anchor'),
            ('warded', 'champion warded anchor'),
            ('warp', 'champion warp anchor'),
            ('undying', 'champion undying anchor'),
            ('fiery', 'champion fiery anchor'),
            ('sickly', 'champion sickly anchor'),
            ('venomous', 'champion venomous anchor'),
            ('vampiric', 'champion vampiric anchor'),
            ('infested', 'champion infested anchor'),
        ]:
            if needle not in stage_doc and needle not in stage_report:
                raise AssertionError(f'missing {label}: {needle}')

    fortress_runtime = read('src/main/java/com/darkifov/thaumcraft/runic/TC4FortressArmorRuntime.java')
    for needle, label in [
        ('ORIGINAL_SET_BASE = 0.875D', 'ported set base'),
        ('ORIGINAL_SET_PIECE_BONUS = 0.125D', 'ported set piece bonus'),
        ('ORIGINAL_MASK_BONUS = 0.05D', 'ported mask armor bonus'),
        ('slotProtection(armor) / divisor(source) * set', 'per-piece special armor ratio'),
        ('source.isBypassArmor()', '1.19.2 unblockable source branch'),
        ('source.isFire() || source.isExplosion() || source.isMagic()', '1.19.2 fire/explosion/magic branch'),
        ('isFullFortressSet', 'full set helper'),
        ('tc4_thaumiumfortresshelm', 'helmet id'),
        ('tc4_thaumiumfortresschest', 'chest id'),
        ('tc4_thaumiumfortresslegs', 'legs id'),
    ]:
        require(fortress_runtime, needle, label)

    champion_runtime = read('src/main/java/com/darkifov/thaumcraft/runic/TC4ChampionModifierRuntime.java')
    for needle, label in [
        ('TC4_CHAMPION_MOD_TAG = "TC4ChampionMod"', 'champion persistent tag'),
        ('new ChampionData(0, "bold", -1)', 'bold champion id'),
        ('new ChampionData(1, "spine", 2)', 'spine champion id'),
        ('new ChampionData(2, "armor", 2)', 'armor champion id'),
        ('new ChampionData(4, "grim", 1)', 'grim champion id'),
        ('new ChampionData(5, "warded", 0)', 'warded champion id'),
        ('new ChampionData(6, "warp", 1)', 'warp champion id'),
        ('new ChampionData(7, "undying", 0)', 'undying champion id'),
        ('new ChampionData(8, "fiery", 1)', 'fiery champion id'),
        ('new ChampionData(9, "sickly", 1)', 'sickly champion id'),
        ('new ChampionData(10, "venomous", 1)', 'venomous champion id'),
        ('new ChampionData(11, "vampiric", 1)', 'vampiric champion id'),
        ('new ChampionData(12, "infested", 2)', 'infested champion id'),
        ('setChampion(Mob mob, int mod, boolean persist)', 'champion set API'),
        ('makeChampion(Mob mob, boolean persist)', 'champion random API'),
        ('mob.setAbsorptionAmount', 'warded periodic absorption'),
        ('mob.heal(1.0F)', 'undying periodic heal'),
        ('DamageSource.thorns(champion)', 'spined thorns damage'),
        ('event.setAmount(amount * 19.0F / 25.0F)', 'armored damage reduction'),
        ('TaintCrawlerEntity', 'infested spawn bridge'),
        ('MobEffects.WITHER, 200', 'grim wither'),
        ('PlayerThaumData.addWarpTemporary', 'warp champion player warp'),
        ('target.setSecondsOnFire(4)', 'fiery fire duration'),
        ('MobEffects.HUNGER, 500', 'sickly hunger'),
        ('MobEffects.POISON, 100', 'venomous poison'),
        ('champion.heal(Math.max(2.0F, event.getAmount() / 2.0F))', 'vampiric heal'),
    ]:
        require(champion_runtime, needle, label)

    warp_adapter = read('src/main/java/com/darkifov/thaumcraft/runic/TC4WarpingGearAdapter.java')
    for needle, label in [
        ('Stage213 1.19.2 adapter', 'warping adapter marker'),
        ('tc4_voidboots', 'void armor warp'),
        ('tc4_cultistboots', 'cultist gear warp'),
        ('tc4_primalcrusher', 'primal crusher warp'),
        ('PlayerThaumData.hasWarpWard', 'warp ward discount'),
        ('appendTooltip', 'warp tooltip'),
    ]:
        require(warp_adapter, needle, label)

    common = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
    for needle, label in [
        ('TC4ChampionModifierRuntime.handleHurt(event)', 'champion hurt hook'),
        ('TC4FortressArmorRuntime.handleHurt(event)', 'fortress special armor hook'),
        ('TC4ChampionModifierRuntime.tick(event.getEntity())', 'champion tick hook'),
    ]:
        require(common, needle, label)

    client = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    layer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4FortressArmorLayer.java')
    require(client, 'EntityRenderersEvent.AddLayers', 'client add-layers event')
    require(client, 'new TC4FortressArmorLayer(renderer)', 'fortress layer registration')
    for needle, label in [
        ('textures/models/fortress_armor.png', 'active fortress armor texture'),
        ('textures/models/runic_goggles.png', 'active runic goggles texture'),
        ('TC4FortressArmorRuntime.isFortressPiece', 'layer fortress-piece guard'),
        ('TC4FortressMaskRuntime.hasGoggles', 'goggles NBT branch'),
        ('setPartVisibility', 'armor-slot visibility'),
    ]:
        require(layer, needle, label)
    for rel in [
        'src/main/resources/assets/thaumcraft/textures/models/fortress_armor.png',
        'src/main/resources/assets/thaumcraft/textures/models/runic_goggles.png',
    ]:
        if not (ROOT / rel).exists():
            raise AssertionError(f'missing copied active model texture: {rel}')

    # Newly touched files must stay 1.19.2-source compatible and avoid direct old TC4 APIs.
    touched = [
        'src/main/java/com/darkifov/thaumcraft/runic/TC4FortressArmorRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/runic/TC4ChampionModifierRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/runic/TC4WarpingGearAdapter.java',
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4FortressArmorLayer.java',
        'src/main/java/com/darkifov/thaumcraft/block/TC4FortressArmorItem.java',
    ]
    forbidden = ['net.minecraft.item.ItemStack', 'net.minecraft.nbt.NBTTag', 'thaumcraft.api.', 'cpw.mods', 'func_', 'field_']
    for rel in touched:
        text = read(rel)
        for needle in forbidden:
            if needle in text:
                raise AssertionError(f'1.19.2 guard failed: {needle} appears in {rel}')

    print('Stage213 fortress armor + champion runtime 1.19.2 parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage213 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
