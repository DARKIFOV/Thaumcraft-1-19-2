#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage218_full/Thaumcraft4-1.7.10-master')
if not ORIG.exists():
    ORIG = Path('/mnt/data/Thaumcraft4-1.7.10-master')


def read(rel: str, root: Path = ROOT) -> str:
    path = root / rel
    if not path.exists():
        raise AssertionError(f'missing file: {rel}')
    return path.read_text(encoding='utf-8', errors='ignore')


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f'missing {label}: {needle}')


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "version = '2.18.0'", 'Stage218 Gradle version')
    require(mods, 'version="2.18.0"', 'Stage218 mods.toml version')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, 'net.minecraftforge:forge:1.19.2-43', 'Forge 1.19.2 dependency')

    orig_guardian_model = read('thaumcraft/client/renderers/models/entities/ModelEldritchGuardian.java', ORIG)
    for needle in ['BeltR', 'HoodEye', 'Cloak1', 'ShoulderplateTopR', 'LegpanelC3']:
        require(orig_guardian_model, needle, f'original ModelEldritchGuardian part {needle}')
    orig_golem = read('thaumcraft/common/entities/monster/boss/EntityEldritchGolem.java', ORIG)
    require(orig_golem, 'this.spawnTimer = 100', 'original golem headless spawn timer 100')
    require(orig_golem, 'this.beamCharge == 150', 'original golem beam charge 150')
    orig_boss = read('thaumcraft/common/entities/monster/boss/EntityThaumcraftBoss.java', ORIG)
    require(orig_boss, 'IBossDisplayData', 'original boss display interface')
    require(orig_boss, 'ConfigItems.itemEldritchObject, 1, 3', 'original boss eldritch object meta 3 drop')
    require(orig_boss, 'ConfigItems.itemLootbag, 1, 2', 'original rare lootbag drop')
    orig_key = read('thaumcraft/common/lib/world/dim/GenKeyRoom.java', ORIG)
    for needle, label in [
        ('new EntityPermanentItem', 'original permanent item'),
        ('new ItemStack(ConfigItems.itemEldritchObject, 1, 2)', 'original key item meta 2'),
        ('EnumDifficulty.NORMAL ? 1', 'original normal difficulty count'),
        ('EnumDifficulty.HARD ? 2', 'original hard difficulty count'),
        ('EntityUtils.makeChampion(eg, true)', 'original hard key-room champion'),
    ]:
        require(orig_key, needle, label)

    model = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchBossModelParity.java')
    for needle, label in [
        ('ORIGINAL_GUARDIAN_MODEL = "ModelEldritchGuardian"', 'guardian model anchor'),
        ('ORIGINAL_GOLEM_MODEL = "ModelEldritchGolem"', 'golem model anchor'),
        ('WARDEN_RENDER_SCALE = 1.5F', 'warden scale constant'),
        ('GOLEM_RENDER_SCALE = 2.15F', 'golem scale constant'),
        ('SPAWN_TIMER_TICKS = 150.0F', 'spawn timer constant'),
        ('GOLEM_HEADLESS_SPAWN_TIMER = 100', 'headless timer constant'),
        ('GOLEM_BEAM_CHARGE_TICKS = 150', 'beam charge constant'),
        ('GUARDIAN_ORIGINAL_PARTS', 'guardian part list'),
        ('GOLEM_ORIGINAL_PARTS', 'golem part list'),
        ('HoodEye', 'guardian HoodEye part recorded'),
        ('LegpanelC3', 'guardian legpanel part recorded'),
    ]:
        require(model, needle, label)

    warden_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchWardenRenderer.java')
    require(warden_renderer, 'TC4EldritchBossModelParity.WARDEN_RENDER_SCALE', 'Warden renderer uses model parity scale')
    require(warden_renderer, 'TC4EldritchBossModelParity.WARDEN_HEAD', 'Warden renderer uses model parity box')
    require(warden_renderer, 'TC4EldritchBossModelParity.SPAWN_TIMER_TICKS', 'Warden renderer uses shared spawn timer')

    golem_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGolemRenderer.java')
    require(golem_renderer, 'TC4EldritchBossModelParity.GOLEM_RENDER_SCALE', 'Golem renderer uses model parity scale')
    require(golem_renderer, 'TC4EldritchBossModelParity.GOLEM_HEADLESS_GLOW', 'Golem renderer uses headless bridge')
    require(golem_renderer, 'TC4EldritchBossModelParity.GOLEM_BEAM_CHARGE_TICKS', 'Golem renderer uses original beam timer')

    boss = read('src/main/java/com/darkifov/thaumcraft/entity/TC4ThaumcraftBossEntity.java')
    for needle, label in [
        ('ServerBossEvent', '1.19.2 boss bar adapter'),
        ('BossEvent.BossBarColor.PURPLE', 'boss bar purple color'),
        ('startSeenByPlayer', 'boss bar add player hook'),
        ('stopSeenByPlayer', 'boss bar remove player hook'),
        ('updateTc4BossBar()', 'boss bar tick update'),
    ]:
        require(boss, needle, label)

    loot = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java')
    for needle, label in [
        ('ORIGINAL_ELDRITCH_OBJECT_META_2', 'original key-room meta 2 marker'),
        ('ORIGINAL_ELDRITCH_OBJECT_META_3', 'original boss meta 3 marker'),
        ('ORIGINAL_RARE_LOOTBAG', 'original rare lootbag marker'),
        ('dropBossDeathLoot', 'boss death loot adapter'),
        ('spawnPermanentKeyItem', 'permanent key item adapter'),
        ('keyRoomGuardianCount', 'key-room difficulty count'),
        ('Difficulty.NORMAL ? 1', 'normal difficulty parity branch'),
        ('Difficulty.HARD ? 2', 'hard difficulty parity branch'),
        ('applyKeyRoomChampionRule', 'key-room champion rule'),
        ('TC4PermanentItem', 'permanent item tag'),
    ]:
        require(loot, needle, label)

    placer = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java')
    for needle, label in [
        ('15x15x13', 'key-room original size note'),
        ('center.above(2)', 'center y+2 altar/item placement'),
        ('spawnPermanentKeyItem(level, center.above(2))', 'permanent item spawn'),
        ('spawnKeyRoomGuardians(level, center.above(2))', 'guardian spawn'),
        ('guardian.restrictTo(center, 16)', 'guardian home radius 16'),
        ('TC4OuterLandsLootAdapter.applyKeyRoomChampionRule', 'guardian champion rule'),
    ]:
        require(placer, needle, label)
    if 'Blocks.CHEST.defaultBlockState()' in placer:
        raise AssertionError('Key room still uses non-original chest placeholder')

    for rel in [
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchBossModelParity.java',
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java',
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java',
        'src/main/java/com/darkifov/thaumcraft/entity/TC4ThaumcraftBossEntity.java',
    ]:
        text = read(rel)
        for legacy in ['func_', 'DataWatcher', 'NBTTag', 'ForgeDirection', 'WorldProvider']:
            if legacy in text:
                raise AssertionError(f'legacy 1.7.10 API leaked into {rel}: {legacy}')

    next_prompt = read('docs/NEXT_CHAT_PROMPT_STAGE218.md')
    require(next_prompt, 'Stage218', 'next chat prompt stage marker')
    require(next_prompt, 'Minecraft/Forge 1.19.2', 'next chat prompt target version')
    require(next_prompt, 'Thaumcraft4-1.7.10-master.zip', 'next chat prompt original source')
    print('Stage218 boss model + key-room parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage218 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
