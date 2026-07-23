#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re
R=Path(__file__).resolve().parents[1]
O=R/'reference/original_source/Thaumcraft4-1.7.10-master'
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def original(p): return (O/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.35 essentia jar full-closure guard: FAIL: '+msg)
def sha(p): return hashlib.sha256(Path(p).read_bytes()).digest()
req(any(v in text('build.gradle') for v in ("version = '11.64.35'", "version = '11.64.36'", "version = '11.64.37'", "version = '11.64.38'")),'build version')
req(any(v in text('src/main/resources/META-INF/mods.toml') for v in ('version="11.64.35"','version="11.64.36"', 'version="11.64.37"','version="11.64.38"')),'mods version')

parity=text('src/main/java/com/darkifov/thaumcraft/jar/TC4EssentiaJarParity.java')
for t in ('CONTRACT_VERSION = "11.64.35"','CAPACITY = 64','FILL_INTERVAL_TICKS = 5','PHIAL_TRANSFER = 8','NORMAL_SUCTION = 32','LABELLED_SUCTION = 64','VOID_LABELLED_SUCTION = 48','clamped < CAPACITY','return NORMAL_SUCTION','remainderAfterInsert','labelFacingDataValue','crookedLabelRotation','canFillEmptyPhial','canEmptyFilledPhial'):
    req(t in parity,'parity '+t)

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java')
for t in ('ORIGINAL_FILL_INTERVAL_TICKS = TC4EssentiaJarParity.FILL_INTERVAL_TICKS','doesContainerAcceptOriginal','addToContainerOriginal','takeFromContainerOriginal','originalMinimumSuction','originalSuctionAmount','suctionType()','worldPosition.above()','takeEssentiaOriginal(target, 1, Direction.DOWN)','tag.putString("Aspect"','tag.putString("AspectFilter"','tag.putShort("Amount"','tag.putByte("facing"','tag.contains("Aspects", Tag.TAG_COMPOUND)','tag.getString("FilterAspect")','ClientboundBlockEntityDataPacket.create(this)'):
    req(t in be,'block entity '+t)
req('tag.put("Aspects"' not in be and 'tag.putString("FilterAspect"' not in be,'block entity writes legacy NBT')

block=text('src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlock.java')
for t in ('Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D)','return Shapes.block()','state.is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get())','EssentiaJarBlockItem.writeJarData','removeLabelFromJar','applyLabelToJar','clearContentsLikeTC4','canFillEmptyPhial','canEmptyFilledPhial','TRANSFER_AMOUNT','input.shrink(1)'):
    req(t in block,'block '+t)

item=text('src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlockItem.java')
for t in ('ITEM_ASPECTS = "Aspects"','ITEM_ASPECT_KEY = "key"','ITEM_ASPECT_AMOUNT = "amount"','ListTag list = new ListTag()','entry.putString(ITEM_ASPECT_KEY','entry.putInt(ITEM_ASPECT_AMOUNT','root.put(ITEM_ASPECTS, list)','root.putString("AspectFilter"','root.contains(BLOCK_ENTITY_TAG','source.contains(ITEM_ASPECTS, Tag.TAG_LIST)','source.contains(ITEM_ASPECTS, Tag.TAG_COMPOUND)','root.remove("facing")'):
    req(t in item,'item '+t)
req('root.putString("Aspect"' not in item and 'root.putShort("Amount"' not in item,'filled jar item writes tile-only NBT')

label=text('src/main/java/com/darkifov/thaumcraft/block/JarLabelItem.java')
for t in ('Tag.TAG_LIST','entry.putString(EssentiaJarBlockItem.ITEM_ASPECT_KEY','entry.putInt(EssentiaJarBlockItem.ITEM_ASPECT_AMOUNT, 0)','root.put(EssentiaJarBlockItem.ITEM_ASPECTS, list)','tag.remove(EssentiaJarBlockItem.ITEM_ASPECTS)'):
    req(t in label,'label '+t)
recipe=text('src/main/java/com/darkifov/thaumcraft/recipe/JarLabelRecipe.java')
for t in ('amount == 8','occupied == 2','occupied == 1','getRemainingItems','JarLabelItem.withAspect','new ItemStack(ThaumcraftMod.JAR_LABEL.get())'):
    req(t in recipe,'label recipe '+t)

interaction=text('src/main/java/com/darkifov/thaumcraft/jar/JarTubeInteractionRuntime.java')
for t in ('jar.amount() > 0','if (aspect == null) return true','labelFacingDataValue(player.getYRot())','label.shrink(1)','clickedFace != jar.labelFacing()','new ItemStack(com.darkifov.thaumcraft.ThaumcraftMod.JAR_LABEL.get())','TC4Sounds.event("page")'):
    req(t in interaction,'interaction '+t)

resolver=text('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java')
req('(aspect == null || jar.canAcceptAspect(aspect))' in resolver,'untyped suction resolution')
harness=text('src/main/java/com/darkifov/thaumcraft/item/gear/HoverHarnessItem.java')
req('EssentiaJarBlockItem.writeItemData' in harness and 'put(EssentiaJarBlockItem.BLOCK_ENTITY_TAG' not in harness,'Hover Harness canonical jar NBT')

renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java')
for t in ('animatedglow.png','LIQUID_MIN_XZ = -0.250F','LIQUID_MIN_Y = 0.0625F','LIQUID_HEIGHT = 0.625F','textures/original/thaumcraft4/models/label.png','-0.250F, -0.250F','0.168F','crookedLabelRotation'):
    req(t in renderer,'renderer '+t)
itemrenderer=text('src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarItemRenderer.java')
for t in ('EssentiaJarBlockItem.readJarData','renderSingleBlock','renderItemContents'):
    req(t in itemrenderer,'item renderer '+t)

normal=json.loads(text('src/main/resources/assets/thaumcraft/models/block/essentia_jar.json'))
void=json.loads(text('src/main/resources/assets/thaumcraft/models/block/void_essentia_jar.json'))
for model,name in ((normal,'normal'),(void,'void')):
    req(len(model.get('elements',[]))==2,name+' exact two-part jar geometry')
    req(model['elements'][0]['from']==[3,0,3] and model['elements'][0]['to']==[13,12,13],name+' core geometry')
    req(model['elements'][1]['from']==[5,12,5] and model['elements'][1]['to']==[11,14,11],name+' lid geometry')

warded=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wardedjar.json'))
req(warded['research']=='DISTILESSENTIA' and warded['aspects']=={'AQUA':1},'Warded Jar research/cost')
req(warded['pattern']==['GWG','G G','GGG'] and warded['key']['W']=='#minecraft:wooden_slabs','Warded Jar shape')
void_recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_jar_void.json'))
req(void_recipe['research']=='JARVOID' and void_recipe['aspects']=={'AQUA':5,'PERDITIO':15},'Void Jar research/cost')
req(void_recipe['pattern']==['O','J','P'] and void_recipe['key']['P']=='minecraft:blaze_powder','Void Jar shape')
blank=json.loads(text('src/main/resources/data/thaumcraft/recipes/jar_label.json'))
req(blank['result']['count']==4 and len(blank['ingredients'])==6,'blank label recipe')
req(not (R/'src/main/resources/data/thaumcraft/recipes/essentia_jar.json').exists(),'wrong normal vanilla recipe remains')
req(not (R/'src/main/resources/data/thaumcraft/recipes/void_essentia_jar.json').exists(),'wrong void vanilla recipe remains')
req(not (R/'src/main/resources/data/thaumcraft/recipes/filtered_essentia_jar.json').exists(),'wrong filtered recipe remains')
hidden=json.loads(text('src/main/resources/data/c/tags/items/hidden_from_recipe_viewers.json'))
req('thaumcraft:filtered_essentia_jar' in hidden.get('values',[]),'migration alias not hidden from recipe viewers')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('new EssentiaJarBlockItem(block.get(), new Item.Properties())' in mod,'filtered jar migration item still in creative')

original_checks={
 'thaumcraft/common/tiles/TileJarFillable.java':('maxAmount = 64','this.aspectFilter != null ? 64 : 32','this.amount < this.maxAmount','++this.count % 5 == 0','face == ForgeDirection.UP','func_74777_a("Amount", (short)this.amount)','func_74774_a("facing", (byte)this.facing)'),
 'thaumcraft/common/tiles/TileJarFillableVoid.java':('this.amount += am','this.amount > this.maxAmount','this.aspectFilter != null ? 48 : 32','return 32'),
 'thaumcraft/common/blocks/BlockJar.java':('aspectFilter = null','player.func_70093_af()','aspectFilter = ((TileJarFillable)te).aspect','func_149676_a(0.1875F, 0.0F, 0.1875F, 0.8125F, 0.75F, 0.8125F)','func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F)'),
 'thaumcraft/common/blocks/ItemJarFilled.java':('func_77625_d(1)','aspects.writeToNBT(itemstack.func_77978_p())','AspectFilter'),
 'thaumcraft/api/aspects/AspectList.java':('func_150295_c("Aspects", 10)','func_74778_a("key", aspect.getTag())','func_74768_a("amount", getAmount(aspect))'),
 'thaumcraft/common/items/ItemEssence.java':('tile.amount >= 8','tile.amount <= tile.maxAmount - 8','tile.addToContainer(aspect, 8) == 0'),
 'thaumcraft/common/config/ConfigRecipes.java':('new ItemStack(ConfigItems.itemResource, 4, 13)','new AspectList().add(aspect, 0)','new AspectList().add(Aspect.WATER, 5).add(Aspect.ENTROPY, 15)'),
 'thaumcraft/client/renderers/tile/TileJarRenderer.java':('0.25D, 0.0625D, 0.25D, 0.75D, 0.0625D + level, 0.75D','aspectFilter.getTag().hashCode() + tile.field_145851_c','textures/models/label.png'),
 'thaumcraft/client/renderers/models/ModelJar.java':('func_78789_a(-5.0F, -12.0F, -5.0F, 10, 12, 10)','func_78789_a(-3.0F, 0.0F, -3.0F, 6, 2, 6)')}
for path,tokens in original_checks.items():
    src=original(path)
    for t in tokens: req(t in src,'original '+path+' '+t)

assets=[
 ('assets/thaumcraft/sounds/jar1.ogg','src/main/resources/assets/thaumcraft/sounds/jar1.ogg'),
 ('assets/thaumcraft/sounds/jar2.ogg','src/main/resources/assets/thaumcraft/sounds/jar2.ogg'),
 ('assets/thaumcraft/sounds/jar3.ogg','src/main/resources/assets/thaumcraft/sounds/jar3.ogg'),
 ('assets/thaumcraft/sounds/jar4.ogg','src/main/resources/assets/thaumcraft/sounds/jar4.ogg'),
 ('assets/thaumcraft/textures/blocks/animatedglow.png','src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/blocks/animatedglow.png'),
 ('assets/thaumcraft/textures/blocks/animatedglow.png.mcmeta','src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/blocks/animatedglow.png.mcmeta'),
 ('assets/thaumcraft/textures/blocks/jar_bottom.png','src/main/resources/assets/thaumcraft/textures/block/tc4/jar_bottom.png'),
 ('assets/thaumcraft/textures/blocks/jar_side.png','src/main/resources/assets/thaumcraft/textures/block/tc4/jar_side.png'),
 ('assets/thaumcraft/textures/blocks/jar_side_void.png','src/main/resources/assets/thaumcraft/textures/block/tc4/jar_side_void.png'),
 ('assets/thaumcraft/textures/blocks/jar_top.png','src/main/resources/assets/thaumcraft/textures/block/tc4/jar_top.png'),
 ('assets/thaumcraft/textures/blocks/jar_top_void.png','src/main/resources/assets/thaumcraft/textures/block/tc4/jar_top_void.png'),
 ('assets/thaumcraft/textures/items/label.png','src/main/resources/assets/thaumcraft/textures/item/tc4/label.png'),
 ('assets/thaumcraft/textures/items/label_over.png','src/main/resources/assets/thaumcraft/textures/item/tc4/label_over.png'),
 ('assets/thaumcraft/textures/models/label.png','src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/label.png')]
for a,b in assets: req(sha(O/a)==sha(R/b),'resource hash '+a)

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=274 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('labelledAndVoidJarsKeepTc4SuctionCapacityAndOverflow','filteredTubeLocksSuctionAndTransferToLabelAspect','essentiaJarCanonicalNbtMatchesOriginal','voidJarSuctionAndOverflowMatchOriginal','labelledJarRetainsTypeWhenEmptied','filledJarItemUsesRootTc4Nbt','jarFacingAndPhialBoundariesMatchOriginal','jarLabelUsesOriginalZeroAmountAspectList','hoverHarnessKeepsOriginalJarItemNbt'):
    req(name in methods,'GameTest '+name)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(manifest.get('version') in ('11.64.35','11.64.36','11.64.37','11.64.38'),'manifest version')
req(len(ids)>=807 and len(ids)==len(set(ids)),'manifest count/unique')
for sid in ('gameplay.essentia_jar_capacity_suction','gameplay.void_jar_overflow_suction','gameplay.jar_label_apply_remove','persistence.filled_jar_item_nbt','integration.hover_harness_jar_nbt','client.jar_liquid_label_renderer','integration.filtered_jar_alias_hidden'):
    req(sid in ids,'scenario '+sid)
for p in ('TC4_11.64.35_ESSENTIA_JAR_SOURCE_EVIDENCE.json','tools/data/tc4_essentia_jar_full_source_evidence_v11.64.35.json'):
    ev=json.loads(text(p)); req(ev.get('version')=='11.64.35','evidence version '+p); req(len(ev.get('original_sources',[]))==10,'evidence original count '+p); req(len(ev.get('production_contracts',[]))==15,'evidence production count '+p)
req((R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').is_file(),'universal prompt')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'),'mandatory prompt wording')
print(f'TC4 v11.64.35 essentia jar full-closure guard: PASS ({len(methods)} GameTests, {len(ids)} scenarios, 14 exact assets)')
