#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage214/Thaumcraft4-1.7.10-master')
if not ORIG.exists():
    ORIG = Path('/mnt/data/tc4_orig_stage213/Thaumcraft4-1.7.10-master')


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
    require(build, "net.minecraftforge:forge:1.19.2-43", 'Forge 1.19.2 dependency')
    require_re(build, r"version = '(2\.(1[4-9]|[2-9][0-9])|3\.[0-9]+)\.0'", 'Stage214 Gradle version')
    require_re(mods, r'version="(2\.(1[4-9]|[2-9][0-9])|3\.[0-9]+)\.0"', 'Stage214 mods.toml version')

    if (ORIG / 'thaumcraft/client/lib/RenderEventHandler.java').exists():
        orig_render = read('thaumcraft/client/lib/RenderEventHandler.java', ORIG)
        require(orig_render, 'ChampionModifier.mods[t].effect.showFX(mob)', 'original RenderEventHandler showFX dispatch')
        orig_entity_event = read('thaumcraft/common/lib/events/EventHandlerEntity.java', ORIG)
        for needle, label in [
            ('Config.championMobs', 'original champion_mobs config branch'),
            ('EnumDifficulty.EASY', 'original easy difficulty branch'),
            ('EnumDifficulty.HARD', 'original hard difficulty branch'),
            ('isDangerousLocation', 'original dangerous-location branch'),
            ('ConfigEntities.championModWhitelist', 'original champion whitelist'),
            ('EntityUtils.makeChampion(mob, false)', 'original automatic champion generation'),
        ]:
            require(orig_entity_event, needle, label)
        orig_utils = read('thaumcraft/common/lib/utils/EntityUtils.java', ORIG)
        for needle, label in [
            ('entity instanceof EntityCreeper', 'original creeper forces bold'),
            ('ChampionModifier.mods[type].getModNameLocalized()', 'original champion display name'),
            ('entity.func_110163_bv()', 'original persistent champion branch'),
        ]:
            require(orig_utils, needle, label)
        orig_entities = read('thaumcraft/common/config/ConfigEntities.java', ORIG)
        for needle, label in [
            ('championWhiteList", "Zombie:0"', 'zombie whitelist'),
            ('championWhiteList", "Spider:0"', 'spider whitelist'),
            ('championWhiteList", "Witch:1"', 'witch whitelist'),
            ('EntityCultist.class, Integer.valueOf(1)', 'cultist whitelist'),
            ('EntityThaumcraftBoss.class, Integer.valueOf(200)', 'boss whitelist'),
        ]:
            require(orig_entities, needle, label)
    else:
        stage_doc = read('docs/TC4_CHAMPION_GENERATION_FX_STAGE214.md')
        stage_report = read('STAGE214_TC4_CHAMPION_GENERATION_FX_REPORT.json')
        for needle, label in [
            ('RenderEventHandler', 'carried showFX source anchor'),
            ('EventHandlerEntity', 'carried champion spawn source anchor'),
            ('EntityUtils.makeChampion', 'carried champion utility source anchor'),
            ('ConfigEntities.championModWhitelist', 'carried whitelist source anchor'),
            ('Config.championMobs', 'carried champion config source anchor'),
            ('Easy difficulty', 'carried easy difficulty branch'),
            ('Hard difficulty', 'carried hard difficulty branch'),
            ('dangerous-location', 'carried dangerous location branch'),
            ('creepers always become `bold` champions', 'carried creeper bold rule'),
            ('0 bold', 'carried bold FX branch'),
            ('12 infested', 'carried infested FX branch'),
        ]:
            if needle not in stage_doc and needle not in stage_report:
                raise AssertionError(f'missing {label}: {needle}')

    runtime = read('src/main/java/com/darkifov/thaumcraft/runic/TC4ChampionModifierRuntime.java')
    for needle, label in [
        ('Stage214 1.19.2 adapter', 'Stage214 runtime marker'),
        ('TC4_CHAMPION_GENERATION_CHECKED_TAG', 'generation checked sentinel'),
        ('CHAMPION_MOD_WHITELIST = Map.ofEntries', '1.19.2 whitelist map'),
        ('Map.entry("minecraft:zombie", 0)', 'zombie whitelist mirror'),
        ('Map.entry("minecraft:witch", 1)', 'witch whitelist mirror'),
        ('Map.entry("thaumcraft:crimson_praetor", 200)', 'boss/praetor whitelist mirror'),
        ('mob instanceof Creeper ? 0', 'creeper forces bold'),
        ('maybeMakeSpawnChampion(Entity entity)', 'spawn champion entry point'),
        ('ThaumcraftConfig.CHAMPION_MOBS.get()', 'champion config usage'),
        ('level.getDifficulty() == Difficulty.EASY', 'easy branch'),
        ('level.getDifficulty() == Difficulty.HARD', 'hard branch'),
        ('isNetherOrEnd(level)', 'nether/end dangerous biome branch'),
        ('isDangerousLocation(level, mob.blockPosition())', 'dangerous location branch'),
        ('makeChampion(mob, bossLike)', 'automatic champion creation'),
        ('refreshChampionDisplayName', 'display name refresh'),
        ('Component.translatable("champion.mod."', 'localized champion modifier names'),
        ('ThaumcraftNetwork.sendChampionFx', 'showFX packet dispatch'),
    ]:
        require(runtime, needle, label)

    common = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
    require(common, 'EntityJoinLevelEvent', '1.19.2 entity join event import')
    require(common, 'TC4ChampionModifierRuntime.maybeMakeSpawnChampion(event.getEntity())', 'spawn champion event hook')

    network = read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java')
    packet = read('src/main/java/com/darkifov/thaumcraft/network/PacketFXChampion.java')
    fx = read('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientChampionFx.java')
    for needle, label in [
        ('PacketFXChampion.class', 'PacketFXChampion registration'),
        ('sendChampionFx(ServerLevel level, LivingEntity entity, int mod, double range)', 'champion FX sender'),
        ('new PacketFXChampion(entity.getId(), mod)', 'champion FX packet payload'),
    ]:
        require(network, needle, label)
    for needle, label in [
        ('TC4ClientChampionFx.showFX(packet.entityId, packet.mod)', 'packet client handler'),
        ('FriendlyByteBuf', 'packet codec'),
        ('Dist.CLIENT', 'client-only dispatch'),
    ]:
        require(packet, needle, label)
    for needle, label in [
        ('Client-side Stage214 approximation', 'client showFX marker'),
        ('case 0 -> showBold', 'bold FX branch'),
        ('case 1 -> showSpined', 'spined FX branch'),
        ('case 2 -> showArmored', 'armored FX branch'),
        ('case 3 -> showMighty', 'mighty FX branch'),
        ('case 4 -> showGrim', 'grim FX branch'),
        ('case 5 -> showWarded', 'warded FX branch'),
        ('case 6 -> showWarp', 'warp FX branch'),
        ('case 7 -> showUndying', 'undying FX branch'),
        ('case 8 -> showFiery', 'fiery FX branch'),
        ('case 9 -> showSickly', 'sickly FX branch'),
        ('case 10 -> showVenomous', 'venomous FX branch'),
        ('case 11 -> showVampiric', 'vampiric FX branch'),
        ('case 12 -> showInfested', 'infested FX branch'),
        ('ParticleTypes.ELECTRIC_SPARK', 'bold spark particle'),
        ('ParticleTypes.PORTAL', 'warp particle'),
        ('ParticleTypes.FLAME', 'fiery particle'),
    ]:
        require(fx, needle, label)

    config = read('src/main/java/com/darkifov/thaumcraft/config/ThaumcraftConfig.java')
    require(config, 'CHAMPION_MOBS', 'champion_mobs config key')
    require(config, 'championMobs", true', 'TC4 default champion config')

    lang = read('src/main/resources/assets/thaumcraft/lang/en_us.json')
    for key in ['champion.mod.bold', 'champion.mod.spine', 'champion.mod.armor', 'champion.mod.mighty', 'champion.mod.grim', 'champion.mod.warded', 'champion.mod.warp', 'champion.mod.undying', 'champion.mod.fiery', 'champion.mod.sickly', 'champion.mod.venomous', 'champion.mod.vampiric', 'champion.mod.infested']:
        require(lang, f'"{key}"', f'localized {key}')
    for key in ['thaumcraft.champion.generic', 'thaumcraft.champion.eldritch_guardian', 'thaumcraft.champion.crimson_praetor', 'thaumcraft.champion.crimson_cultist']:
        require(lang, f'"{key}"', f'Stage214 display key {key}')

    touched = [
        'src/main/java/com/darkifov/thaumcraft/runic/TC4ChampionModifierRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/network/PacketFXChampion.java',
        'src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientChampionFx.java',
        'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java',
        'src/main/java/com/darkifov/thaumcraft/config/ThaumcraftConfig.java',
    ]
    forbidden = ['net.minecraft.item.', 'net.minecraft.nbt.NBTTag', 'thaumcraft.api.', 'cpw.mods', 'func_', 'field_']
    for rel in touched:
        text = read(rel)
        for needle in forbidden:
            if needle in text:
                raise AssertionError(f'1.19.2 guard failed: {needle} appears in {rel}')

    print('Stage214 champion generation + showFX 1.19.2 parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage214 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
