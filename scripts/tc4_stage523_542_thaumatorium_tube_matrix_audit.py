#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def must(path, *needles):
    text = read(path)
    missing = [n for n in needles if n not in text]
    if missing:
        raise SystemExit(f"::error::{path} missing {missing}")

def exists(rel):
    if not (ROOT / rel).exists():
        raise SystemExit(f"::error::missing {rel}")

must('src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java',
     'implements MenuProvider', 'NBT_SELECTED_FORMULA', 'NBT_MNEMONIC_MATRIX',
     'visibleFormulaCandidates()', 'hasMnemonicMatrix()', 'activeRecipe()', 'cycleFormula()',
     'ThaumatoriumMenu')
must('src/main/java/com/darkifov/thaumcraft/menu/ThaumatoriumMenu.java',
     'original TC4 ContainerThaumatorium', 'quickMoveStack', 'ItemStack.EMPTY')
must('src/main/java/com/darkifov/thaumcraft/client/screen/ThaumatoriumScreen.java',
     'gui_thaumatorium.png', 'FORMULA_SLOTS', 'Mnemonic matrix', 'textures/aspects/')
must('src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaNetworkRuntime.java',
     'allowsAspectForTransfer', 'BufferTubeRef', 'sideAllows', 'allowsInputFrom')
must('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java',
     'allowsAspectForTransfer', 'drainBufferForNetwork')
must('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
     'MNEMONIC_MATRIX', 'THAUMATORIUM_MENU', 'ThaumatoriumMenu')
must('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
     'ThaumatoriumScreen', 'THAUMATORIUM_MENU')
must('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java',
     'blockMetalDevice:12", "thaumcraft:mnemonic_matrix')
for rel in [
    'src/main/resources/assets/thaumcraft/blockstates/mnemonic_matrix.json',
    'src/main/resources/assets/thaumcraft/models/block/mnemonic_matrix.json',
    'src/main/resources/assets/thaumcraft/models/item/mnemonic_matrix.json',
    'src/main/resources/assets/thaumcraft/textures/block/mnemonic_matrix.png',
    'src/main/resources/data/thaumcraft/loot_tables/blocks/mnemonic_matrix.json',
]:
    exists(rel)
recipe = json.loads(read('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_mnemonicmatrix.json'))
if recipe.get('result', {}).get('item') != 'thaumcraft:mnemonic_matrix':
    raise SystemExit('::error::tc4_mnemonicmatrix recipe still outputs placeholder mirror item')
if 'tc4_block_mnemonic_matrix' not in read('src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java'):
    raise SystemExit('::error::old mnemonic mirror item is not quarantined')
print('Stage523-542 thaumatorium/tube/mnemonic matrix audit: OK')
