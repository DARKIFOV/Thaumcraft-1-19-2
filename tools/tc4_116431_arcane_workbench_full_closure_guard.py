#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.31 Arcane Workbench full-closure guard: FAIL: '+msg)
def version(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p)); req(m,'version '+p); return tuple(map(int,m.groups()))
def sha(b): return hashlib.sha256(b).hexdigest()
req(version('build.gradle')>=(11,64,31),'build version')
req(version('src/main/resources/META-INF/mods.toml')>=(11,64,31),'mods version')
vis=text('src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchVisCostParity.java')
for token in ('CONTRACT_VERSION = "11.64.31"','INVENTORY_SIZE = 11','OUTPUT_SLOT = 9','WAND_SLOT = 10','AUTOMATION_SLOT = 10','EMPTY_ASPECT_LIST_COST = 0','CENTIVIS_MULTIPLIER = 100','zeroAspectListIsFree'):
    req(token in vis,'vis parity '+token)
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java')
for token in ('implements WorldlyContainer, MenuProvider','public static final int SIZE = 11','Component.translatable("container.arcaneworkbench")','transformFromTable','player.setItemInHand(hand, ItemStack.EMPTY)','SoundEvents.UI_BUTTON_CLICK','0.15F, 0.5F','return true;\n        }\n        for (Map.Entry<Aspect, Integer> entry','tag.put("Inventory", inventory)','tag.remove("Items")','getSlotsForFace(Direction side)','return AUTOMATION_SLOTS.clone()','slot == SLOT_WAND && canPlaceItem(slot, stack)','return slot == SLOT_WAND','ClientboundBlockEntityDataPacket.create(this)','level.sendBlockUpdated'):
    req(token in be,'block entity '+token)
for forbidden in ('public static final int SLOT_LEGACY_CATALYST','public static final int ORDO_COST','Aspect.ORDO, ORDO_COST','getItem(SLOT_LEGACY_CATALYST)','catalystSlot == SLOT_LEGACY_CATALYST'):
    req(forbidden not in be,'removed invention '+forbidden)
req('LEGACY_STAGE_CATALYST_SLOT = 11' in be and 'pendingLegacyCatalyst' in be,'one-time hidden-slot migration')
block=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneWorkbenchBlock.java')
req('if (player.isShiftKeyDown())' in block and 'return InteractionResult.PASS;' in block,'sneak pass')
table=text('src/main/java/com/darkifov/thaumcraft/block/TableBlock.java')
req('ArcaneWorkbenchBlockEntity.transformFromTable(level, pos, player, hand, held)' in table,'table transform production path')
wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
req('ArcaneWorkbenchBlockEntity.transformFromTable(' in wand,'wand transform production path')
segment=wand[wand.index('if (state.is(ThaumcraftMod.TABLE.get()))'):wand.index('if (state.is(Blocks.CAULDRON))')]
req('consumeTransformationCost' not in segment,'no invented table vis cost')
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/ArcaneWorkbenchRenderer.java')
for token in ('WAND_RENDER_X','WAND_RENDER_Y','WAND_RENDER_Z','WAND_RENDER_X_ROTATION','WAND_RENDER_Z_ROTATION','rendered.setCount(1)','ItemTransforms.TransformType.GROUND'):
    req(token in renderer,'renderer '+token)
client=text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
req('ARCANE_WORKBENCH_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneWorkbenchRenderer::new)' in client,'renderer registration')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()' in mod,'block properties')
recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodgreatwood.json'))
req(recipe['aspects']=={'perditio':3},'Greatwood rod 3 Perditio')
for lang,value in (('en_us.json','Arcane Workbench'),('ru_ru.json','Магический верстак')):
    req(json.loads(text('src/main/resources/assets/thaumcraft/lang/'+lang)).get('container.arcaneworkbench')==value,'localization '+lang)
model=json.loads(text('src/main/resources/assets/thaumcraft/models/block/arcane_workbench.json'))
req(model.get('ambientocclusion') is False and len(model.get('elements',[]))==6,'worktable model six exact boxes')
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
with zipfile.ZipFile(source_zip) as z:
    prefix='Thaumcraft4-1.7.10-master/'
    originals={
      'thaumcraft/common/tiles/TileMagicWorkbench.java':('new ItemStack[11]','"Inventory"','return new int[] { 10 }','return i == 10','!wand.isStaff(itemstack)'),
      'thaumcraft/common/tiles/TileArcaneWorkbench.java':('return "container.arcaneworkbench"',),
      'thaumcraft/common/container/ContainerArcaneWorkbench.java':('new SlotCraftingArcaneWorkbench','this.tileEntity, 9, 160, 64','this.tileEntity, 10, 160, 24','40 + var7 * 24','16 + var7 * 18, 151','16 + var6 * 18, 209','CraftingManager','consumeAllVisCrafting','findMatchingArcaneRecipe'),
      'thaumcraft/common/container/SlotCraftingArcaneWorkbench.java':('aspects.size() > 0','consumeAllVisCrafting','for (int var2 = 0; var2 < 9; var2++)','getContainerItem'),
      'thaumcraft/common/blocks/BlockTable.java':('func_149711_c(2.5F)','new TileArcaneWorkbench()','func_70299_a(10, wandstack.func_77946_l())','field_70461_c, null','"random.click", 0.15F, 0.5F'),
      'thaumcraft/client/renderers/tile/TileArcaneWorkbenchRenderer.java':('0.65F','1.0625F','0.25F','90.0F','20.0F','field_77994_a = 1'),
      'thaumcraft/common/config/ConfigRecipes.java':('((WandRod)WandRod.rods.get("greatwood")).getCraftCost()','Aspect.ENTROPY')}
    for rel,tokens in originals.items():
        src=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens: req(token in src,'original '+rel+' '+token)
    pairs=(
      ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_arcaneworkbench.png','assets/thaumcraft/textures/gui/gui_arcaneworkbench.png'),
      ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/worktable.png','assets/thaumcraft/textures/models/worktable.png'))
    for cur,orig in pairs:
        req(sha((R/cur).read_bytes())==sha(z.read(prefix+orig)),'resource hash '+cur)
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=248 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('arcaneWorkbenchInventoryAndAutomationMatchOriginal','arcaneWorkbenchZeroCostAndCentivisScalingMatchOriginal','arcaneWorkbenchTableTransformationInstallsWand','arcaneWorkbenchGuiCoordinatesMatchTc4Original'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.'))) >= (11,64,31),'manifest version')
req(len(ids)>=748 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.arcane_workbench_table_transform_wand','gameplay.arcane_workbench_table_transform_staff','gameplay.arcane_workbench_zero_aspect_cost','automation.arcane_workbench_wand_slot_only','persistence.arcane_workbench_inventory_nbt','visual.arcane_workbench_installed_wand','dedicated.arcane_workbench_server_authority'):
    req(sid in ids,'scenario '+sid)
prompt=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'
req(prompt.is_file(),'mandatory prompt missing')
req(prompt.read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes(),'mandatory prompt copies differ')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in prompt.read_text(encoding='utf-8'),'mandatory wording missing')
print('TC4 v11.64.31 Arcane Workbench full-closure guard: PASS')
