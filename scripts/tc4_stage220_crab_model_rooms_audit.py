#!/usr/bin/env python3
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_master/Thaumcraft4-1.7.10-master')
if not ORIG.exists():
    ORIG = Path('/mnt/data/Thaumcraft4-1.7.10-master')

def read(rel, root=ROOT):
    p = root / rel
    if not p.exists():
        raise AssertionError(f'missing file: {rel}')
    return p.read_text(encoding='utf-8', errors='ignore')

def require(text, needle, label):
    if needle not in text:
        raise AssertionError(f'missing {label}: {needle}')

def main():
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "version = '2.20.0'", 'Stage220 Gradle version')
    require(mods, 'version="2.20.0"', 'Stage220 mods.toml version')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, 'net.minecraftforge:forge:1.19.2-43', 'Forge 1.19.2 dependency')

    orig_crab = read('thaumcraft/common/entities/monster/EntityEldritchCrab.java', ORIG)
    for needle, label in [
        ('func_70105_a(0.8F, 0.6F)', 'original crab size'),
        ('field_70728_aV = 6', 'original crab xp'),
        ('EntityAILeapAtTarget(this, 0.63F)', 'original leap strength'),
        ('setHelm(this.field_70146_Z.nextFloat() < 0.33F)', 'original helm chance'),
        ('return hasHelm() ? 5 : 0', 'original helm armor'),
        ('func_85030_a("thaumcraft:crabclaw"', 'original claw sound'),
        ('par1NBTTagCompound.func_74774_a("Flags"', 'original Flags NBT'),
        ('return 160', 'original ambient interval'),
    ]:
        require(orig_crab, needle, label)
    orig_spawner = read('thaumcraft/common/tiles/TileEldritchCrabSpawner.java', ORIG)
    for needle, label in [
        ('public int count = 150', 'original spawner count'),
        ('this.count == 15', 'original spawner warmup event'),
        ('50 + this.field_145850_b.field_73012_v.nextInt(50)', 'original dry reset'),
        ('150 + this.field_145850_b.field_73012_v.nextInt(100)', 'original post-spawn reset'),
        ('func_72977_a', 'original player activation'),
        ('func_72314_b(32.0D, 32.0D, 32.0D)', 'original entity cap range'),
        ('ents.size() > 5', 'original entity cap count'),
        ('crab.setHelm(false)', 'original spawner strips helm'),
        ('writeCustomNBT', 'original facing NBT'),
    ]:
        require(orig_spawner, needle, label)
    orig_library = read('thaumcraft/common/lib/world/dim/GenLibraryRoom.java', ORIG)
    require(orig_library, 'GenCommon.generateConnections(world, random, cx, cz, y, cell, 3, true)', 'original library connection call')
    orig_nest = read('thaumcraft/common/lib/world/dim/GenNestRoom.java', ORIG)
    require(orig_nest, 'GenCommon.generateConnections(world, random, cx, cz, y, cell, 3, true)', 'original nest connection call')

    mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
    for needle, label in [
        ('ELDRITCH_CRAB_SPAWNER', 'crab spawner block registry'),
        ('ELDRITCH_CRAB_SPAWNER_BLOCK_ENTITY', 'crab spawner block entity registry'),
        ('ELDRITCH_CRAB =', 'crab entity registry'),
        ('.sized(0.8F, 0.6F)', 'crab entity size parity'),
        ('EldritchCrabEntity.createAttributes()', 'crab attributes registration'),
    ]:
        require(mod, needle, label)
    crab = read('src/main/java/com/darkifov/thaumcraft/entity/EldritchCrabEntity.java')
    for needle, label in [
        ('xpReward = 6', 'crab xp parity'),
        ('LeapAtTargetGoal(this, 0.63F)', 'crab leap parity'),
        ('level.getDifficulty() == Difficulty.HARD || random.nextFloat() < 0.33F', 'crab helm spawn parity'),
        ('return hasHelm() ? 5 : 0', 'crab armor parity'),
        ('TC4Sounds.event("crabclaw")', 'crab claw sound'),
        ('TC4Sounds.event("crabtalk")', 'crab ambient sound'),
        ('TC4Sounds.event("crabdeath")', 'crab death sound'),
        ('tag.putByte("Flags"', 'crab Flags NBT'),
        ('startRiding(target, true)', 'bare crab head latch'),
    ]:
        require(crab, needle, label)
    spawner = read('src/main/java/com/darkifov/thaumcraft/blockentity/EldritchCrabSpawnerBlockEntity.java')
    for needle, label in [
        ('ORIGINAL_START_COUNT = 150', 'spawner start count'),
        ('ORIGINAL_WARMUP_EVENT = 15', 'spawner warmup'),
        ('ORIGINAL_PLAYER_RANGE = 16.0D', 'spawner player range'),
        ('ORIGINAL_ENTITY_RANGE = 32.0D', 'spawner entity range'),
        ('ORIGINAL_MAX_CRABS = 5', 'spawner crab cap'),
        ('crab.setHelm(false)', 'spawner strips helm'),
        ('tag.putByte(FACING_TAG', 'spawner facing NBT'),
    ]:
        require(spawner, needle, label)
    layers = read('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4EldritchBossLayerDefinitions.java')
    for needle, label in [
        ('ELDRITCH_CRAB', 'crab model layer'),
        ('createCrabBodyLayer', 'crab layer factory'),
        ('"TailHelm"', 'crab TailHelm part'),
        ('"TailBare"', 'crab TailBare part'),
        ('"RClaw2"', 'crab claw part'),
        ('LayerDefinition.create(mesh, 128, 64)', 'original model atlas size'),
    ]:
        require(layers, needle, label)
    for rel in [
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGuardianRenderer.java',
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchWardenRenderer.java',
        'src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGolemRenderer.java',
    ]:
        text = read(rel)
        require(text, 'TC4BakedEldritchModel', f'{rel} uses baked model path')
        require(text, 'context.bakeLayer', f'{rel} bakes layer')
    gen = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java')
    for needle, label in [
        ('generateLibraryRoom', 'library room adapter'),
        ('generateNestRoom', 'nest room adapter'),
        ('CODE_CRAB_SPAWNER_A', 'crab spawner palette code'),
        ('ELDRITCH_CRAB_SPAWNER.get().defaultBlockState()', 'palette places real crab spawner'),
        ('generateConnections(level, origin, cell, 3, true)', 'room connection parity'),
    ]:
        require(gen, needle, label)
    client = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    require(client, 'EntityRenderers.register(ThaumcraftMod.ELDRITCH_CRAB.get(), TC4EldritchCrabRenderer::new)', 'crab renderer registration')
    require(client, 'TC4EldritchBossLayerDefinitions::createCrabBodyLayer', 'crab layer registration')
    prompt = read('docs/NEXT_CHAT_PROMPT_STAGE220.md')
    require(prompt, 'Stage220', 'next prompt stage marker')
    require(prompt, 'Minecraft/Forge 1.19.2', 'next prompt target')
    require(prompt, 'Thaumcraft4-1.7.10-master.zip', 'next prompt original')
    require(prompt, 'Stage221', 'next prompt next stage')
    for legacy in ['NBTTag', 'func_', 'ForgeDirection', 'DataWatcher']:
        if legacy in crab + spawner:
            raise AssertionError(f'legacy 1.7.10 API leaked into new crab runtime: {legacy}')
    print('Stage220 crab/model/rooms parity audit OK')

if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage220 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
