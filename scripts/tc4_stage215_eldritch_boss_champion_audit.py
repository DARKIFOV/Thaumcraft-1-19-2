#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage215/Thaumcraft4-1.7.10-master')
if not ORIG.exists():
    ORIG = Path('/mnt/data/tc4_orig_stage214/Thaumcraft4-1.7.10-master')


def read(rel: str, root: Path = ROOT) -> str:
    path = root / rel
    if not path.exists():
        raise AssertionError(f'missing file: {rel}')
    return path.read_text(encoding='utf-8', errors='ignore')


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f'missing {label}: {needle}')


def require_re(text: str, pattern: str, label: str) -> None:
    if not re.search(pattern, text):
        raise AssertionError(f'missing {label}: /{pattern}/')


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, 'net.minecraftforge:forge:1.19.2-43', 'Forge 1.19.2 dependency')
    require_re(build, r"version = '2\.(15|16)\.0'", 'Stage215 Gradle version')
    require_re(mods, r'version="2\.(15|16)\.0"', 'Stage215 mods.toml version')

    orig_boss = read('thaumcraft/common/entities/monster/boss/EntityThaumcraftBoss.java', ORIG)
    for needle, label in [
        ('HomeD', 'original boss home distance NBT'),
        ('spawnTimer', 'original boss spawn timer'),
        ('getAnger', 'original anger watcher'),
        ('HPBUFF', 'original per-player health buff'),
        ('DMGBUFF', 'original per-player damage buff'),
        ('generateName', 'original boss champion name hook'),
    ]:
        require(orig_boss, needle, label)

    orig_warden = read('thaumcraft/common/entities/monster/boss/EntityEldritchWarden.java', ORIG)
    for needle, label in [
        ('EntityEldritchWarden extends EntityThaumcraftBoss', 'original warden boss type'),
        ('String[] titles = { "Aphoom-Zhah"', 'original warden title table'),
        ('func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(200.0D)', 'original warden max health 200'),
        ('func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.33D)', 'original warden speed 0.33'),
        ('func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(10.0D)', 'original warden damage 10'),
        ('nbt.func_74774_a("title"', 'original warden title NBT'),
        ('this.spawnTimer = 150', 'original warden spawn timer'),
        ('func_110149_m((float)(func_110139_bj() + func_110148_a(SharedMonsterAttributes.field_111267_a).func_111125_b() * 0.66D))', 'original warden absorption spawn bonus'),
    ]:
        require(orig_warden, needle, label)

    orig_golem = read('thaumcraft/common/entities/monster/boss/EntityEldritchGolem.java', ORIG)
    for needle, label in [
        ('EntityEldritchGolem extends EntityThaumcraftBoss', 'original golem boss type'),
        ('nbt.func_74757_a("headless"', 'original golem headless NBT'),
        ('func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(250.0D)', 'original golem max health 250'),
        ('func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3D)', 'original golem speed 0.3'),
        ('func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(10.0D)', 'original golem damage 10'),
        ('damage > func_110143_aJ()', 'original lethal headless branch'),
        ('beamCharge', 'original headless beam charge'),
    ]:
        require(orig_golem, needle, label)

    orig_utils = read('thaumcraft/common/lib/utils/EntityUtils.java', ORIG)
    for needle, label in [
        ('CHAMPION_MOD', 'original champion sentinel attribute'),
        ('BOLDBUFF', 'original bold speed buff'),
        ('MIGHTYBUFF', 'original mighty damage buff'),
        ('HPBUFF', 'original boss HPBUFF array'),
        ('DMGBUFF', 'original boss DMGBUFF array'),
        ('((EntityThaumcraftBoss)entity).generateName()', 'original boss generateName dispatch'),
    ]:
        require(orig_utils, needle, label)

    mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
    for needle, label in [
        ('import com.darkifov.thaumcraft.entity.EldritchWardenEntity;', 'warden import'),
        ('import com.darkifov.thaumcraft.entity.EldritchGolemEntity;', 'golem import'),
        ('RegistryObject<EntityType<EldritchWardenEntity>> ELDRITCH_WARDEN', 'warden registry object'),
        ('RegistryObject<EntityType<EldritchGolemEntity>> ELDRITCH_GOLEM', 'golem registry object'),
        ('EntityType.Builder.of(EldritchWardenEntity::new, MobCategory.MONSTER)', 'warden entity factory'),
        ('EntityType.Builder.of(EldritchGolemEntity::new, MobCategory.MONSTER)', 'golem entity factory'),
        ('event.put(ELDRITCH_WARDEN.get(), EldritchWardenEntity.createAttributes().build())', 'warden attribute registration'),
        ('event.put(ELDRITCH_GOLEM.get(), EldritchGolemEntity.createAttributes().build())', 'golem attribute registration'),
    ]:
        require(mod, needle, label)

    base = read('src/main/java/com/darkifov/thaumcraft/entity/TC4ThaumcraftBossEntity.java')
    for needle, label in [
        ('Stage215 1.19.2 base adapter for TC4 EntityThaumcraftBoss', 'base boss marker'),
        ('HOME_D_TAG = "HomeD"', 'HomeD tag'),
        ('SPAWN_TIMER_TAG = "SpawnTimer"', 'spawn timer tag'),
        ('ANGER_TAG = "Anger"', 'anger tag'),
        ('PLAYER_HEALTH_BUFF_UUIDS', 'HPBUFF UUID mirror'),
        ('PLAYER_DAMAGE_BUFF_UUIDS', 'DMGBUFF UUID mirror'),
        ('setTc4Home(blockPosition(), 24)', 'base home helper availability marker via method call target not required'),
        ('generateName()', 'base generateName hook'),
        ('removeWhenFarAway(double distanceToClosestPlayer)', 'persistent boss despawn guard'),
    ]:
        if label == 'base home helper availability marker via method call target not required':
            require(base, 'protected void setTc4Home(BlockPos center, int distance)', 'setTc4Home helper')
        else:
            require(base, needle, label)

    warden = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchWardenEntity.java')
    for needle, label in [
        ('Stage215 1.19.2 port of TC4 EntityEldritchWarden boss semantics', 'warden marker'),
        ('TITLES = new String[]', 'warden title table'),
        ('"Aphoom-Zhah"', 'warden Aphoom title'),
        ('"Zushakon"', 'warden Zushakon title'),
        ('TITLE_TAG = "title"', 'warden title NBT tag'),
        ('.add(Attributes.MAX_HEALTH, 200.0D)', 'warden health 200'),
        ('.add(Attributes.ATTACK_DAMAGE, 10.0D)', 'warden attack 10'),
        ('.add(Attributes.MOVEMENT_SPEED, 0.33D)', 'warden speed 0.33'),
        ('spawnTimer = 150', 'warden spawn timer'),
        ('getMaxHealth() * 0.66D', 'warden absorption parity'),
        ('TC4ChampionModifierRuntime.makeChampion(this, true)', 'warden persistent champion spawn'),
        ('entity.thaumcraft.eldritch_warden.champion', 'warden generateName lang key'),
        ('PlayerThaumData.addWarpTemporary', 'warden warp attack adapter'),
    ]:
        require(warden, needle, label)

    golem = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchGolemEntity.java')
    for needle, label in [
        ('Stage215 1.19.2 port of TC4 EntityEldritchGolem boss semantics', 'golem marker'),
        ('HEADLESS_TAG = "headless"', 'golem headless NBT tag'),
        ('BEAM_CHARGE_TAG = "BeamCharge"', 'golem beam NBT tag'),
        ('.add(Attributes.MAX_HEALTH, 250.0D)', 'golem health 250'),
        ('.add(Attributes.ATTACK_DAMAGE, 10.0D)', 'golem attack 10'),
        ('.add(Attributes.MOVEMENT_SPEED, 0.30D)', 'golem speed 0.30'),
        ('amount > getHealth() && !headless', 'golem lethal headless branch'),
        ('spawnTimer = 100', 'golem headless spawn timer'),
        ('beamCharge >= 150', 'golem beam charge cap'),
        ('TC4ChampionModifierRuntime.makeChampion(this, true)', 'golem persistent champion spawn'),
        ('entity.thaumcraft.eldritch_golem.champion', 'golem generateName lang key'),
    ]:
        require(golem, needle, label)

    runtime = read('src/main/java/com/darkifov/thaumcraft/runic/TC4ChampionModifierRuntime.java')
    for needle, label in [
        ('CHAMPION_MOD_SENTINEL = -2.0D', 'champion sentinel mirror'),
        ('BOLD_SPEED_UUID = UUID.fromString("4b1edd33-caa9-47ae-a702-d86c05701037")', 'BOLDBUFF UUID'),
        ('MIGHTY_DAMAGE_UUID = UUID.fromString("7163897f-07f5-49b3-9ce4-b74beb83d2d3")', 'MIGHTYBUFF UUID'),
        ('Map.entry("thaumcraft:eldritch_warden", 200)', 'warden whitelist boss level'),
        ('Map.entry("thaumcraft:eldritch_golem", 200)', 'golem whitelist boss level'),
        ('mob instanceof EldritchWardenEntity warden', 'warden champion display branch'),
        ('mob instanceof EldritchGolemEntity', 'golem champion display branch'),
        ('mob.getPersistentData().putDouble("tc.mobmod", mod)', 'champion mod sentinel persistent mirror'),
        ('new AttributeModifier(BOLD_SPEED_UUID', 'bold speed modifier application'),
        ('new AttributeModifier(MIGHTY_DAMAGE_UUID', 'mighty damage modifier application'),
    ]:
        require(runtime, needle, label)

    client = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    require(client, 'EntityRenderers.register(ThaumcraftMod.ELDRITCH_WARDEN.get()', 'warden renderer registration')
    require(client, 'EntityRenderers.register(ThaumcraftMod.ELDRITCH_GOLEM.get()', 'golem renderer registration')

    lang = read('src/main/resources/assets/thaumcraft/lang/en_us.json')
    for key in ['entity.thaumcraft.eldritch_warden', 'entity.thaumcraft.eldritch_warden.champion', 'entity.thaumcraft.eldritch_golem', 'entity.thaumcraft.eldritch_golem.champion', 'champion.mod.normal']:
        require(lang, f'"{key}"', f'lang key {key}')

    touched = [
        'src/main/java/com/darkifov/thaumcraft/entity/TC4ThaumcraftBossEntity.java',
        'src/main/java/com/darkifov/thaumcraft/entity/EldritchWardenEntity.java',
        'src/main/java/com/darkifov/thaumcraft/entity/EldritchGolemEntity.java',
        'src/main/java/com/darkifov/thaumcraft/runic/TC4ChampionModifierRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
    ]
    forbidden = ['net.minecraft.item.', 'net.minecraft.nbt.NBTTag', 'thaumcraft.api.', 'cpw.mods', 'func_', 'field_', 'SharedMonsterAttributes', 'DataWatcher']
    for rel in touched:
        text = read(rel)
        for needle in forbidden:
            if needle in text:
                raise AssertionError(f'1.19.2 guard failed: {needle} appears in {rel}')

    print('Stage215 eldritch boss + champion attribute 1.19.2 parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage215 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
