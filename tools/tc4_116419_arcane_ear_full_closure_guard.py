#!/usr/bin/env python3
"""v11.64.19 guard: complete Arcane Ear source/resource closure."""
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.19 Arcane Ear full-closure guard: FAIL: '+msg)
def ver(s):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s); req(m,'version parse'); return tuple(map(int,m.groups()))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
req(ver(text('build.gradle')) >= (11,64,19),'build version')
req(ver(text('src/main/resources/META-INF/mods.toml')) >= (11,64,19),'mods version')

c=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneEarParity.java')
for t in ('CONTRACT_VERSION = "11.64.19"','BLOCK_HARDNESS = 2.5F','BLOCK_RESISTANCE = 10.0F',
 'MIN_NOTE = 0','MAX_NOTE = 24','NOTE_COUNT = 25','REDSTONE_SIGNAL = 15',
 'REDSTONE_PULSE_TICKS = 10','LISTEN_RANGE_SQUARED = 4096.0D','NOTE_SOUND_VOLUME = 3.0F',
 'NOTE_PARTICLE_Y = 1.2D','SILENT_NOTE_EVENT = 5','OBJECT_SENSUS = 4',
 'Math.pow(2.0D, (clampNote(note) - 12) / 12.0D)','distanceSquared <= LISTEN_RANGE_SQUARED'):
    req(t in c,'contract token '+t)

b=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneEarBlock.java')
for t in ('BooleanProperty POWERED','TC4ArcaneEarParity.SILENT_NOTE_EVENT','ear.changePitch()',
 'ear.emitConfiguredNote(true)','ear.updateToneFromSupport()','isSignalSource','getDirectSignal',
 'TC4ArcaneEarParity.notePitch(note)','TC4ArcaneEarParity.NOTE_SOUND_VOLUME',
 'TC4ArcaneEarParity.NOTE_PARTICLE_Y','SoundEvents.NOTE_BLOCK_BASEDRUM','SoundEvents.NOTE_BLOCK_SNARE',
 'SoundEvents.NOTE_BLOCK_HAT','SoundEvents.NOTE_BLOCK_BASS'):
    req(t in b,'block path '+t)
# updateTone must remain in placement/neighbour paths but not manual use body.
use=b[b.index('public InteractionResult use'):b.index('@Override\n    public void onPlace')]
req('updateToneFromSupport' not in use,'manual right click still refreshes support tone')

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneEarBlockEntity.java')
for t in ('NOTE_EVENTS = new WeakHashMap<>()','TC4ArcaneEarParity.pulseAfterTick','TC4ArcaneEarParity.matchesPlayedNote',
 'List<PlayedNote> events = NOTE_EVENTS.get(serverLevel)','for (PlayedNote event : events)','ear.emitConfiguredNote(false)',
 'ear.signalTicks = TC4ArcaneEarParity.REDSTONE_PULSE_TICKS','break;','computeIfAbsent(level, ignored -> new ArrayList<>())',
 'clearNoteEvents(ServerLevel level)','events.clear()','TC4ArcaneEarParity.nextNote',
 'level.isEmptyBlock(worldPosition.above())','level.updateNeighborsAt(worldPosition',
 'level.updateNeighborsAt(worldPosition.below()','tag.putByte(TC4ArcaneEarParity.NBT_NOTE',
 'tag.putByte(TC4ArcaneEarParity.NBT_TONE','signalTicks = 0;',
 'material == Material.STONE','material == Material.SAND','material == Material.GLASS','material == Material.WOOD',
 'case HARP -> 0','case BASEDRUM -> 1','case SNARE -> 2','case HAT -> 3','case BASS -> 4'):
    req(t in be,'block entity path '+t)
for forbidden in ('tag.putInt("SignalTicks"','tag.getInt("SignalTicks"','tag.putBoolean("Powered"',
                  'signalTicks = Math.max(0, Math.min(10','LOADED_EARS','lastTriggerGameTime'):
    req(forbidden not in be,'transient pulse persistence remains '+forbidden)

events=text('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
for t in ('onNoteBlockPlay(NoteBlockEvent.Play event)','EventPriority.LOWEST','event.isCanceled()',
          'event.getInstrument()','event.getVanillaNoteId()','ArcaneEarBlockEntity.onNotePlayed',
          'ArcaneEarBlockEntity.clearNoteEvents(level)'):
    req(t in events,'note event bridge '+t)

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('ARCANE_EAR = BLOCKS.register("tc4_block_arcane_ear"',
 'strength(TC4ArcaneEarParity.BLOCK_HARDNESS, TC4ArcaneEarParity.BLOCK_RESISTANCE)',
 'ARCANE_EAR_ITEM = ITEMS.register("tc4_block_arcane_ear"',
 'new BlockItem(ARCANE_EAR.get(), new Item.Properties().tab(THAUMCRAFT_TAB))',
 'ARCANE_EAR_BLOCK_ENTITY'):
    req(t in mod,'registration '+t)
segment=mod[mod.index('ARCANE_EAR_ITEM ='):mod.index('ARCANE_LAMP =')]
req('.rarity(' not in segment,'Arcane Ear still has invented non-common rarity')

aspect=text('src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java')
req('exact("thaumcraft:tc4_block_arcane_ear", aspects(Aspect.SENSUS, TC4ArcaneEarParity.OBJECT_SENSUS))' in aspect,
    'Sensus 4 object aspect registration')

recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcaneear.json'))
req(recipe['research']=='ARCANEEAR' and recipe['pattern']==['GIG','GBG','WRW'],'recipe pattern/research')
req(recipe['key']=={'W':'thaumcraft:greatwood_planks','R':'minecraft:redstone','I':'minecraft:iron_ingot','G':'minecraft:gold_ingot','B':'thaumcraft:tc4_brain'},'recipe key mapping')
req(recipe['aspects']=={'AER':10,'ORDO':10} and recipe['result']=={'item':'thaumcraft:tc4_block_arcane_ear','count':1},'recipe costs/result')
req(recipe.get('v11_64_19_exact_source') is True,'recipe exact-source marker')
req('minecraft:shears' not in json.dumps(recipe),'stale shears recipe mapping remains')

research=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for t in ('"ARCANEEAR", "Arcane Ear", "Shhh, do you hear something?"','"ARTIFICE", 6, 0, 1',
 'aspects("SENSUS", 3, "POTENTIA", 3, "AER", 3)','new String[] {"GOGGLES"}',
 'new String[] {"concealed"}','new String[] {"TEXT", "ARCANE_CRAFTING"}',
 'new String[] {"ArcaneEar"}'):
    req(t in research,'research '+t)

model=json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_ear_base.json'))
expected=[([0,0,0],[16,3,16]),([4,3,4],[12,16,12]),([4,8,1],[12,16,3]),([5,8,3],[11,15,4]),
 ([1,8,4],[3,16,12]),([3,8,5],[4,15,11]),([4,8,13],[12,16,15]),([5,8,12],[11,15,13]),
 ([13,8,4],[15,16,12]),([12,8,5],[13,15,11])]
req([(e.get('from'),e.get('to')) for e in model.get('elements',[])]==expected,'ten-cuboid model geometry')
req(model.get('ambientocclusion') is False,'model AO')
on=json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_ear_on.json'))
req(on.get('textures',{}).get('top')=='thaumcraft:block/tc4/arcaneeartopon' and
    on.get('textures',{}).get('side')=='thaumcraft:block/tc4/arcaneearsideon','powered textures')
textures={
 'arcaneearbellside':'6f36322638f81f241c12d2414182a1a500e2cbd315d0294264e806a6e0c74a2e',
 'arcaneearbelltop':'952b4f5b923a5f49426fec77c66f741f84d1e6881d214cf108d54bb78fdf5be8',
 'arcaneearbottom':'f00cebc4ec29f04a1979c74f0a18c50f7a979a6d448d1a2a1b0d2977c69eaaf7',
 'arcaneearsideoff':'cd49d201640cd973385d79d17644c53c047e8b1812bc386f52780093e9e96b0b',
 'arcaneearsideon':'25fc52e8de28b03568665b94ba075635bf2aed9db6d12e5cdb554dbb662edbe1',
 'arcaneeartopoff':'ed1c6ca6406c27c1dcf8a5f521de62c7a3b579cda4072dbb875bf2161f007e96',
 'arcaneeartopon':'7fc76927d44d0ca0117be48ce67850359de740a3ee28144b52c658c18a5b74a5'}
for name,h in textures.items():
    req(sha('src/main/resources/assets/thaumcraft/textures/block/tc4/'+name+'.png')==h,'texture '+name)

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def orig(suffix):
        n=next((n for n in z.namelist() if n.endswith('/'+suffix)),None); req(n,'original missing '+suffix); return z.read(n).decode(errors='replace')
    ot=orig('thaumcraft/common/tiles/TileSensor.java')
    ob=orig('thaumcraft/common/blocks/BlockWoodenDevice.java')
    ore=orig('thaumcraft/client/renderers/block/BlockWoodenDeviceRenderer.java')
    oe=orig('thaumcraft/common/lib/events/EventHandlerWorld.java')
    os=orig('thaumcraft/common/lib/events/ServerTickEventsFML.java')
    orc=orig('thaumcraft/common/config/ConfigRecipes.java')
    ors=orig('thaumcraft/common/config/ConfigResearch.java')
    oa=orig('thaumcraft/common/config/ConfigAspects.java')
for t in ('par1NBTTagCompound.func_74774_a("note", this.note)','par1NBTTagCompound.func_74774_a("tone", this.tone)',
 'this.redstoneSignal -= 1','this.redstoneSignal = 10','<= 4096.0D','this.note = ((byte)((this.note + 1) % 25))',
 'par1World.func_147439_a(par2, par3 + 1, par4).func_149688_o() == Material.field_151579_a'):
    req(t in ot,'original TileSensor '+t)
req('SignalTicks' not in ot and 'redstoneSignal"' not in ot,'original unexpectedly persists pulse')
for t in ('var6.changePitch();','var6.triggerNote(w, x, y, z, true)','return ((TileSensor)tile).redstoneSignal > 0 ? 15 : 0',
 'Math.pow(2.0D, (par6 - 12) / 12.0D)','"note." + var8, 3.0F, var7','par3 + 1.2D','func_149711_c(2.5F)','func_149752_b(10.0F)'):
    req(t in ob,'original block '+t)
for t in ('block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, W3, 1.0F)','block.func_149676_a(W4, W3, W4, W12, 1.0F, W12)',
 '((BlockWoodenDevice)block).renderState = 2'):
    req(t in ore,'original renderer '+t)
req('list.add(new Integer[]' in oe and 'event.instrument.ordinal()' in oe and 'event.getVanillaNoteId()' in oe,'original note buffer')
req('TileSensor.noteBlockEvents.get(event.world)).clear()' in os,'original buffer clear')
req('addArcaneCraftingRecipe("ARCANEEAR"' in orc and '"GIG", "GBG", "WRW"' in orc and
    "Character.valueOf('W'), new ItemStack(ConfigBlocks.blockWoodenDevice, 1, 6)" in orc and
    "Character.valueOf('R'), Items.field_151137_ax" in orc and
    "Character.valueOf('G'), Items.field_151043_k" in orc and
    "Character.valueOf('B'), new ItemStack(ConfigItems.itemZombieBrain)" in orc,'original recipe')
req('new ResearchItem("ARCANEEAR", "ARTIFICE"' in ors and 'Aspect.SENSES, 3' in ors and 'Aspect.ENERGY, 3' in ors and 'Aspect.AIR, 3' in ors,'original research')
req('registerComplexObjectTag(new ItemStack(ConfigBlocks.blockWoodenDevice, 1, 1), new AspectList().add(Aspect.SENSES, 4))' in oa,'original object aspects')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=172 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcaneEarNoteCyclePitchAndPulseMathMatchOriginal','arcaneEarSupportToneAndBlockedOutputMatchOriginal',
 'arcaneEarDetectsMatchingNoteAndEmitsTransientRedstone','arcaneEarPersistsOnlyNoteAndTone',
 'arcaneEarResearchAspectsRarityAndBlockPropertiesMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,19) and len(ids)>=564 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('gametest.arcane_ear_note_cycle_pitch_pulse','gametest.arcane_ear_support_tone_blocked_output',
 'gametest.arcane_ear_matching_range_redstone','gametest.arcane_ear_transient_nbt',
 'gametest.arcane_ear_research_aspects_rarity','gameplay.arcane_ear_remote_note_detection',
 'persistence.arcane_ear_note_tone_only','client.arcane_ear_exact_multipart_model',
 'research.arcane_ear_recipe_and_entry','aspects.arcane_ear_sensus_four','jei.arcane_ear_exact_arcane_recipe'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_ear_full_source_evidence_v11.64.19.json'))
req(ev['round']=='11.64.19' and ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
report=R/'TC4_11.64.19_ARCANE_EAR_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md'
if report.exists():
    rt=report.read_text(encoding='utf-8')
    req('BUILD VERIFIED: нет' in rt and 'RUNTIME VERIFIED: нет' in rt,'report proof boundary')
print(f'TC4 v11.64.19 Arcane Ear full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
