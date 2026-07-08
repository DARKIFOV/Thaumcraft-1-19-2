#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage215/Thaumcraft4-1.7.10-master')
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


def require_re(text: str, pattern: str, label: str) -> None:
    if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
        raise AssertionError(f'missing {label}: /{pattern}/')


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, 'net.minecraftforge:forge:1.19.2-43', 'Forge 1.19.2 dependency')
    require(build, "version = '2.16.0'", 'Stage216 Gradle version')
    require(mods, 'version="2.16.0"', 'Stage216 mods.toml version')

    orig_orb = read('thaumcraft/common/entities/projectile/EntityEldritchOrb.java', ORIG)
    for needle, label in [
        ('return 0.0F', 'original eldritch orb zero gravity'),
        ('field_70173_aa > 100', 'original eldritch orb 100 tick timeout'),
        ('func_72314_b(2.0D, 2.0D, 2.0D)', 'original radius-2 hit area'),
        ('* 0.666F', 'original attackDamage multiplier'),
        ('Potion.field_76437_t', 'original wither effect'),
        ('func_72960_a(this, (byte)16)', 'original orb burst status byte'),
    ]:
        require(orig_orb, needle, label)

    orb = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4EldritchOrbEntity.java')
    for needle, label in [
        ('life > 100', 'ported eldritch orb timeout'),
        ('inflate(2.0D)', 'ported radius-2 hit area'),
        ('* 0.666F', 'ported attack damage multiplier'),
        ('MobEffects.WITHER, 160, 0', 'ported wither effect'),
        ('sendEldritchOrbBurst', 'ported byte-16 burst bridge'),
        ('tc4ShootAt', 'ported ranged launch helper'),
    ]:
        require(orb, needle, label)

    golem_orb = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4GolemOrbEntity.java')
    for needle, label in [
        ('red ? 240 : 160', 'ported golem orb lifetime'),
        ('distanceToSqr(target)', 'ported TC4 distance-squared homing'),
        ('Mth.clamp(motion.x, -0.25D, 0.25D)', 'ported motion clamp'),
        ('red ? 1.0F : 0.6F', 'ported red/non-red damage multiplier'),
        ('setDeltaMovement(look.x * 0.9D', 'ported projectile deflection'),
    ]:
        require(golem_orb, needle, label)

    warden = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchWardenEntity.java')
    for needle, label in [
        ('TC4EldritchOrbEntity blast', 'Warden now spawns EldritchOrb entity'),
        ('random.nextFloat() <= 0.2F', 'Warden 20% sonic branch'),
        ('sendEldritchBossFx(serverLevel, this, status', 'Warden arm-lift status packet'),
        ('performFieldFrenzyLikeTC4', 'Warden field frenzy adapter'),
        ('teleportHomeLikeTC4', 'Warden frenzy home teleport adapter'),
        ('getArmLiftL', 'Warden renderer state getter left'),
        ('getArmLiftR', 'Warden renderer state getter right'),
    ]:
        require(warden, needle, label)

    golem = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchGolemEntity.java')
    for needle, label in [
        ('TC4GolemOrbEntity blast', 'Golem now spawns GolemOrb entity'),
        ('beamCharge >= 150', 'Golem 150 tick beam charge'),
        ('emitHeadlessArcLikeTC4', 'Golem headless arc adapter'),
        ('sendEldritchBossBlockFx(serverLevel, this, 19', 'Golem byte-19 arc packet'),
        ('tag.putBoolean(HEADLESS_TAG, headless)', 'Golem headless NBT persistence'),
    ]:
        require(golem, needle, label)

    net = read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java')
    require(net, 'PacketFXEldritchBoss.class', 'Stage216 boss FX packet registration')
    require(net, 'sendEldritchBossBlockFx', 'Stage216 boss block FX helper')

    renderer = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    require(renderer, 'ELDRITCH_ORB.get()', 'Eldritch orb renderer registration')
    require(renderer, 'GOLEM_ORB.get()', 'Golem orb renderer registration')

    meta = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomMetadata.java')
    require(meta, 'PAT_DOORWAY', 'Outer Lands PAT_DOORWAY mirror')
    require(meta, 'FEATURE_UPPER_LEFT = 2', 'Outer Lands feature 2')
    require(meta, 'FEATURE_LOWER_RIGHT = 5', 'Outer Lands feature 5')
    require(meta, 'LOCK_FACING_TAG', 'Outer Lands lock facing metadata')

    # Guard against accidentally porting legacy 1.7.10 API into new Stage216 classes.
    for rel in [
        'src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4EldritchOrbEntity.java',
        'src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4GolemOrbEntity.java',
        'src/main/java/com/darkifov/thaumcraft/network/PacketFXEldritchBoss.java',
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomMetadata.java',
    ]:
        text = read(rel)
        for legacy in ['NBTTag', 'func_', 'DataWatcher', 'IEntityAdditionalSpawnData', 'ForgeDirection']:
            if legacy in text:
                raise AssertionError(f'legacy 1.7.10 API leaked into {rel}: {legacy}')

    print('Stage216 eldritch orb/boss AI parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage216 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
