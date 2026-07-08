#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig/Thaumcraft4-1.7.10-master')
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
    require(build, "version = '2.19.0'", 'Stage219 Gradle version')
    require(mods, 'version="2.19.0"', 'Stage219 mods.toml version')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, 'net.minecraftforge:forge:1.19.2-43', 'Forge 1.19.2 dependency')

    orig_render = read('thaumcraft/client/renderers/entity/RenderEldritchGuardian.java', ORIG)
    for needle, label in [
        ('textures/models/eldritch_guardian.png', 'original guardian texture'),
        ('textures/models/eldritch_warden.png', 'original warden texture'),
        ('BossStatus.func_82824_a', 'original boss bar render branch'),
        ('GL11.glScalef(1.5F, 1.5F, 1.5F)', 'original warden scale'),
        ('field_73013_u == EnumDifficulty.HARD ? 576.0F : 1024.0F', 'original guardian distance fade range'),
        ('base = 0.6F', 'original close alpha'),
    ]:
        require(orig_render, needle, label)
    orig_portal = read('thaumcraft/common/lib/world/dim/GenPortal.java', ORIG)
    for needle, label in [
        ('for (int a = 1; a <= 15; a++)', 'original portal 1..15 loops'),
        ('placeBlock(world, x + a, y + 13, z + b, 1, cell)', 'original ceiling placement'),
        ('GenCommon.generateConnections(world, random, cx, cz, y, cell, 3, true)', 'original connection call'),
        ('genObelisk(world, x + 8, y + 4, z + 8)', 'original portal obelisk'),
    ]:
        require(orig_portal, needle, label)
    orig_common = read('thaumcraft/common/lib/world/dim/GenCommon.java', ORIG)
    for needle in ['case 1:', 'case 2:', 'case 8:', 'case 10:', 'case 11:', 'case 99:']:
        require(orig_common, needle, f'original GenCommon palette {needle}')

    client = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    require(client, 'EntityRenderers.register(ThaumcraftMod.ELDRITCH_GUARDIAN.get(), TC4EldritchGuardianRenderer::new)', 'guardian renderer replaces block placeholder')
    require(client, 'onRegisterLayerDefinitions', 'layer definition registration event')
    require(client, 'TC4EldritchBossLayerDefinitions::createGuardianBodyLayer', 'guardian/warden layer registration')
    require(client, 'TC4EldritchBossLayerDefinitions::createGolemBodyLayer', 'golem layer registration')
    if 'ELDRITCH_GUARDIAN.get(), ctx -> new TC4BlockMobRenderer' in client:
        raise AssertionError('guardian still uses block placeholder renderer')

    guardian_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGuardianRenderer.java')
    for needle, label in [
        ('eldritch_guardian.png', 'guardian original texture'),
        ('this.shadowRadius = 0.5F', 'original guardian shadow radius'),
        ('Difficulty.HARD ? 576.0F : 1024.0F', 'original distance fade ranges'),
        ('return 0.6F', 'original close alpha branch'),
        ('entity.level.dimension().location().toString().contains("outer")', 'Outer Lands full alpha branch'),
        ('TC4EldritchBossModelParity.GUARDIAN_RENDER_SCALE', 'guardian scale constant'),
        ('RenderType.entityTranslucent', 'alpha blended renderer'),
    ]:
        require(guardian_renderer, needle, label)

    layers = read('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4EldritchBossLayerDefinitions.java')
    for needle, label in [
        ('ModelLayerLocation', '1.19.2 model layer contract'),
        ('LayerDefinition.create(mesh, 128, 64)', 'original texture atlas size'),
        ('"HoodEye"', 'guardian HoodEye part'),
        ('"LegpanelC3"', 'guardian LegpanelC3 part'),
        ('"Torso"', 'golem Torso part'),
        ('"HeadlessVent"', 'golem headless part'),
        ('PartPose.rotation(-0.5759587F', 'original Hood4 rotation'),
        ('PartPose.rotation(0.837758F', 'original golem collar/headless rotation'),
    ]:
        require(layers, needle, label)

    parity = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchBossModelParity.java')
    require(parity, 'GUARDIAN_RENDER_SCALE = 1.0F', 'guardian render scale constant')

    gen = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java')
    for needle, label in [
        ('CODE_BEDROCK_IF_AIR = 1', 'GenCommon code 1'),
        ('CODE_COSMETIC_SOLID = 2', 'GenCommon code 2'),
        ('CODE_ELDRITCH_NOTHING = 8', 'GenCommon code 8'),
        ('CODE_STAIRS_NORMAL = 10', 'GenCommon code 10'),
        ('CODE_STAIRS_INVERTED = 11', 'GenCommon code 11'),
        ('CODE_BEDROCK = 99', 'GenCommon code 99'),
        ('generatePortalRoom', 'GenPortal adapter'),
        ('for (int a = 1; a <= 15; a++)', 'portal wall loop'),
        ('generateConnections(level, origin, cell, 3, true)', 'portal connection parity call'),
        ('genObelisk(level, origin.offset(8, 4, 8))', 'portal obelisk placement'),
    ]:
        require(gen, needle, label)
    for legacy in ['func_', 'NBTTag', 'ForgeDirection', 'WorldProvider']:
        if legacy in gen + guardian_renderer + layers:
            raise AssertionError(f'legacy 1.7.10 API leaked into Stage219 code: {legacy}')

    placer = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java')
    require(placer, 'placePortalRoom', 'BossRoomPlacer exposes portal room adapter')
    portal_be = read('src/main/java/com/darkifov/thaumcraft/blockentity/EldritchPortalBlockEntity.java')
    require(portal_be, 'placePortalRoom(serverLevel, worldPosition.offset(-8, 0, -8))', 'portal encounter calls GenPortal adapter')

    prompt = read('docs/NEXT_CHAT_PROMPT_STAGE219.md')
    require(prompt, 'Stage219', 'next prompt stage marker')
    require(prompt, 'Minecraft/Forge 1.19.2', 'next prompt target version')
    require(prompt, 'Thaumcraft4-1.7.10-master.zip', 'next prompt original source')
    require(prompt, 'Stage220', 'next prompt next-stage marker')
    print('Stage219 guardian/model/portal parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage219 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
