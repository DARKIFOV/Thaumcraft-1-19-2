#!/usr/bin/env python3
from pathlib import Path
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


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "version = '2.17.0'", 'Stage217 Gradle version')
    require(mods, 'version="2.17.0"', 'Stage217 mods.toml version')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, 'net.minecraftforge:forge:1.19.2-43', 'Forge 1.19.2 dependency')

    orig_guardian = read('thaumcraft/client/renderers/entity/RenderEldritchGuardian.java', ORIG)
    require(orig_guardian, 'textures/models/eldritch_warden.png', 'original warden texture')
    require(orig_guardian, 'GL11.glScalef(1.5F, 1.5F, 1.5F)', 'original warden scale')
    require(orig_guardian, 'getSpawnTimer() / 150.0F', 'original spawn sink')
    require(orig_guardian, 'GL11.glEnable(3042)', 'original alpha blend')

    orig_golem = read('thaumcraft/client/renderers/entity/RenderEldritchGolem.java', ORIG)
    require(orig_golem, 'textures/models/eldritch_golem.png', 'original golem texture')
    require(orig_golem, 'GL11.glScalef(2.15F, 2.15F, 2.15F)', 'original golem scale')
    require(orig_golem, 'BossStatus.func_82824_a', 'original boss status render hook')

    client = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    require(client, 'TC4EldritchWardenRenderer::new', 'dedicated Warden renderer registration')
    require(client, 'TC4EldritchGolemRenderer::new', 'dedicated Golem renderer registration')

    warden_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchWardenRenderer.java')
    for needle, label in [
        ('textures/original/thaumcraft4/models/eldritch_warden.png', 'ported original warden texture path'),
        ('poseStack.scale(1.5F, 1.5F, 1.5F)', 'ported warden 1.5 scale'),
        ('entity.getSpawnTimer() / 150.0F', 'ported warden spawn sink'),
        ('RenderType.entityTranslucent(WARDEN)', 'ported warden alpha blend'),
        ('entity.getArmLiftL()', 'ported left arm lift render state'),
        ('entity.getArmLiftR()', 'ported right arm lift render state'),
    ]:
        require(warden_renderer, needle, label)

    golem_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGolemRenderer.java')
    for needle, label in [
        ('textures/original/thaumcraft4/models/eldritch_golem.png', 'ported original golem texture path'),
        ('poseStack.scale(2.15F, 2.15F, 2.15F)', 'ported golem 2.15 scale'),
        ('entity.isHeadless()', 'ported headless renderer branch'),
        ('entity.isChargingBeam()', 'ported charging beam glow'),
        ('entity.getBeamCharge() / 150.0F', 'ported 150 tick beam charge ratio'),
        ('RenderType.entityTranslucent(GOLEM)', 'ported golem alpha blend'),
    ]:
        require(golem_renderer, needle, label)

    warden = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchWardenEntity.java')
    require(warden, 'clientArmLift(boolean left)', 'client arm lift state bridge')
    require(warden, 'playBossSound("egidle"', 'Warden TC4 idle sound cadence')

    golem = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchGolemEntity.java')
    require(golem, 'getBeamCharge()', 'Golem renderer beam charge getter')
    require(golem, 'isChargingBeam()', 'Golem renderer charging getter')
    require(golem, 'clientStartArc(BlockPos target)', 'Golem client arc state bridge')
    require(golem, 'sendEldritchBossFx(serverLevel, this, 18', 'Golem byte-18 spawn/headless FX')

    fx = read('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientEldritchBossFx.java')
    require(fx, 'warden.clientArmLift(left)', 'packet updates Warden arm state')
    require(fx, 'golem.clientStartArc(new BlockPos(bx, by, bz))', 'packet updates Golem arc state')

    placer = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java')
    for needle, label in [
        ('TC4OuterLandsBossRoomMetadata#PAT_DOORWAY', 'placer consumes PAT_DOORWAY contract'),
        ('placeBossRoom(ServerLevel level', 'boss room placement entrypoint'),
        ('placeKeyRoom(ServerLevel level', 'key room placement entrypoint'),
        ('placeDoorway(level', 'doorway pattern placement'),
        ('spawnWarden(level, center)', 'Warden room spawn'),
        ('spawnGolem(level, center)', 'Golem room spawn'),
        ('ThaumcraftMod.ELDRITCH_ALTAR', 'lock cell altar placement'),
    ]:
        require(placer, needle, label)

    portal = read('src/main/java/com/darkifov/thaumcraft/blockentity/EldritchPortalBlockEntity.java')
    require(portal, 'TC4OuterLandsBossRoomPlacer.placeBossRoom', 'portal integration starts boss-room placement')
    require(portal, 'TC4OuterLandsBossRoomPlacer.placeKeyRoom', 'portal integration starts key-room placement')

    for rel in [
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchWardenRenderer.java',
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGolemRenderer.java',
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java',
    ]:
        text = read(rel)
        for legacy in ['GL11', 'func_', 'DataWatcher', 'NBTTag', 'IEntityAdditionalSpawnData', 'ForgeDirection']:
            if legacy in text:
                raise AssertionError(f'legacy 1.7.10 API leaked into {rel}: {legacy}')

    print('Stage217 eldritch renderer + Outer Lands room parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage217 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
