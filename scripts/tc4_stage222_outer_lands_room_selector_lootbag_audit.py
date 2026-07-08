#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
checks={
 'build version 2.22.0': "version = '2.22.0'" in (root/'build.gradle').read_text(),
 'mods version 2.22.0': 'version="2.22.0"' in (root/'src/main/resources/META-INF/mods.toml').read_text(),
 'handoff prompt': (root/'docs/NEXT_CHAT_PROMPT_STAGE222.md').exists(),
 'room selector ring': 'placeRoomSelectorRing' in (root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java').read_text(),
 'live ring call': 'placeRoomSelectorRing' in (root/'src/main/java/com/darkifov/thaumcraft/blockentity/EldritchPortalBlockEntity.java').read_text(),
 'lootbag open count': '8 + random.nextInt(5)' in (root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java').read_text(),
 'weighted common': 'COMMON' in (root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java').read_text(),
 'weighted uncommon': 'UNCOMMON' in (root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java').read_text(),
 'weighted rare': 'RARE' in (root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java').read_text(),
 'right click lootbag': 'lootbagRarity()' in (root/'src/main/java/com/darkifov/thaumcraft/item/TC4ResearchComponentItem.java').read_text(),
 'stage222 no 1.7 imports': all(x not in ''.join(p.read_text(errors='ignore') for p in [(root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java'), (root/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java'), (root/'src/main/java/com/darkifov/thaumcraft/item/TC4ResearchComponentItem.java')]) for x in ['import net.minecraftforge.common.util.ForgeDirection','import net.minecraft.nbt.NBTTag','func_'])
}
failed=[name for name, ok in checks.items() if not ok]
if failed:
    print('Stage222 audit failed:', failed)
    raise SystemExit(1)
print('Stage222 audit passed:', ', '.join(checks))
