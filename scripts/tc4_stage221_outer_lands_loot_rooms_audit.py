from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
checks = []

def require(path, needle, label):
    text = (ROOT / path).read_text(encoding='utf-8', errors='ignore')
    if needle not in text:
        checks.append(f"Missing {label}: {needle} in {path}")

require('build.gradle', "version = '2.21.0'", 'Stage221 Gradle version')
require('src/main/resources/META-INF/mods.toml', 'version="2.21.0"', 'Stage221 mods.toml version')
require('src/main/java/com/darkifov/thaumcraft/entity/EldritchCrabEntity.java', 'CRIMSON_PLATE_CHEST', 'crab crimson plate chest drop')
require('src/main/java/com/darkifov/thaumcraft/block/TC4CrimsonPlateArmorItem.java', 'ConfigItems.itemChestCultistPlate', 'crimson plate original field marker')
require('src/main/java/com/darkifov/thaumcraft/block/TC4LootBlock.java', '1 + md + random.nextInt(3)', 'BlockLoot drop quantity parity')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java', 'generateLoot(int rarity, RandomSource random)', 'Utils.generateLoot bridge')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java', 'placeLootBlock', 'GenNestRoom loot placement')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsRoomAdapter.java', 'generate2x2', 'Gen2x2 adapter')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsRoomAdapter.java', 'generatePassage', 'GenPassage adapter')
require('docs/NEXT_CHAT_PROMPT_STAGE221.md', 'Stage222', 'next chat prompt')
if checks:
    for c in checks:
        print('::error::' + c)
    sys.exit(1)
print('Stage221 Outer Lands loot/rooms audit: OK')
