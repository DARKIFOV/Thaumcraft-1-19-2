#!/usr/bin/env python3
from __future__ import annotations
import csv, json, re, hashlib, math, os
from pathlib import Path
from collections import defaultdict, Counter
from typing import Any
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(os.environ['TC_ROOT']).resolve()
OUT=Path(os.environ['TC_OUT']).resolve(); OUT.mkdir(parents=True,exist_ok=True)
RES=ROOT/'src/main/resources/assets/thaumcraft'
MODELS=RES/'models'; TEX=RES/'textures'; BLOCKSTATES=RES/'blockstates'; ITEMS=MODELS/'item'; BLOCKMODELS=MODELS/'block'
ORIG=TEX/'original/thaumcraft4'
JAVA=ROOT/'src/main/java'

# ---------- basic helpers ----------
def sha256(p:Path)->str: return hashlib.sha256(p.read_bytes()).hexdigest()
def loadj(p:Path): return json.loads(p.read_text(encoding='utf-8'))
def writecsv(path:Path, rows:list[dict[str,Any]]):
    keys=[]
    for r in rows:
        for k in r:
            if k not in keys: keys.append(k)
    with path.open('w',newline='',encoding='utf-8-sig') as f:
        w=csv.DictWriter(f,fieldnames=keys,extrasaction='ignore'); w.writeheader(); w.writerows(rows)

def loc_parts(loc:str, kind:str):
    ns,path=(loc.split(':',1) if ':' in loc else ('minecraft',loc))
    if kind=='model': return ns,path+'.json'
    return ns,path+'.png'

def model_file(loc:str)->Path|None:
    ns,p=loc_parts(loc,'model')
    if ns!='thaumcraft': return None
    return RES/'models'/p

def texture_file(loc:str)->Path|None:
    if loc.startswith('#'): return None
    ns,p=loc_parts(loc,'texture')
    if ns!='thaumcraft': return None
    return RES/'textures'/p

def resolve_tex_var(v:str, textures:dict[str,str])->str:
    seen=set()
    while v.startswith('#') and v[1:] in textures and v not in seen:
        seen.add(v); v=textures[v[1:]]
    return v

# Original asset hashes
orig_hashes=defaultdict(list)
for p in ORIG.rglob('*.png'):
    orig_hashes[sha256(p)].append(p.relative_to(ORIG).as_posix())

# ---------- model resolution ----------
SYNTHETIC={
 'minecraft:block/cube_all': {'elements':[{'from':[0,0,0],'to':[16,16,16],'faces':{d:{'texture':'#all'} for d in ['down','up','north','south','west','east']}}]},
 'minecraft:block/cube_column': {'elements':[{'from':[0,0,0],'to':[16,16,16],'faces':{'down':{'texture':'#end'},'up':{'texture':'#end'},'north':{'texture':'#side'},'south':{'texture':'#side'},'west':{'texture':'#side'},'east':{'texture':'#side'}}}]},
 'minecraft:block/orientable': {'elements':[{'from':[0,0,0],'to':[16,16,16],'faces':{'down':{'texture':'#bottom'},'up':{'texture':'#top'},'north':{'texture':'#front'},'south':{'texture':'#side'},'west':{'texture':'#side'},'east':{'texture':'#side'}}}]},
 'minecraft:block/orientable_with_bottom': {'elements':[{'from':[0,0,0],'to':[16,16,16],'faces':{'down':{'texture':'#bottom'},'up':{'texture':'#top'},'north':{'texture':'#front'},'south':{'texture':'#side'},'west':{'texture':'#side'},'east':{'texture':'#side'}}}]},
 'minecraft:block/cross': {'synthetic_cross':True},
 'minecraft:block/tinted_cross': {'synthetic_cross':True},
 'minecraft:block/crop': {'synthetic_cross':True},
}
_model_cache={}
def resolve_model(loc:str, stack=())->dict[str,Any]:
    if loc in _model_cache: return _model_cache[loc]
    if loc in stack: return {'error':'parent_cycle','location':loc}
    p=model_file(loc)
    if p and p.exists():
        d=loadj(p)
    elif loc in SYNTHETIC:
        d=dict(SYNTHETIC[loc])
    else:
        d={'external_parent':loc}
    parent=d.get('parent')
    base={}
    if parent:
        base=resolve_model(parent,stack+(loc,))
    out={k:v for k,v in base.items() if k not in {'location'}}
    # child textures/display merge; elements override only if explicitly present
    textures=dict(base.get('textures',{})); textures.update(d.get('textures',{}) if isinstance(d.get('textures'),dict) else {})
    out.update({k:v for k,v in d.items() if k not in {'textures','display'}})
    out['textures']=textures
    display=dict(base.get('display',{})); display.update(d.get('display',{}) if isinstance(d.get('display'),dict) else {})
    out['display']=display
    out['location']=loc
    _model_cache[loc]=out
    return out

def collect_models_from_blockstate(d:Any)->list[str]:
    out=[]
    def walk(o):
        if isinstance(o,dict):
            if isinstance(o.get('model'),str): out.append(o['model'])
            for v in o.values(): walk(v)
        elif isinstance(o,list):
            for v in o: walk(v)
    walk(d); return sorted(set(out))

def default_uv(face:str, fr:list[float], to:list[float]):
    x1,y1,z1=fr; x2,y2,z2=to
    return {
      'down':[x1,16-z2,x2,16-z1], 'up':[x1,z1,x2,z2],
      'north':[16-x2,16-y2,16-x1,16-y1], 'south':[x1,16-y2,x2,16-y1],
      'west':[z1,16-y2,z2,16-y1], 'east':[16-z2,16-y2,16-z1,16-y1],
    }.get(face,[0,0,16,16])

def alpha_metrics_for_uv(p:Path, uv:list[float]):
    try:
        im=Image.open(p).convert('RGBA'); w,h=im.size
        u1,v1,u2,v2=[float(x) for x in uv]
        xa=max(0,min(w,int(math.floor(min(u1,u2)/16*w))))
        xb=max(0,min(w,int(math.ceil(max(u1,u2)/16*w))))
        ya=max(0,min(h,int(math.floor(min(v1,v2)/16*h))))
        yb=max(0,min(h,int(math.ceil(max(v1,v2)/16*h))))
        if xb<=xa or yb<=ya: return (0,0,0)
        a=im.crop((xa,ya,xb,yb)).getchannel('A')
        hist=a.histogram(); transparent=hist[0]; opaque=hist[255]; total=sum(hist); partial=total-transparent-opaque
        return transparent,partial,total
    except Exception:
        return 0,0,0

def model_analysis(loc:str)->dict[str,Any]:
    m=resolve_model(loc); textures=m.get('textures',{}) if isinstance(m.get('textures'),dict) else {}
    elements=m.get('elements') if isinstance(m.get('elements'),list) else []
    cross=bool(m.get('synthetic_cross'))
    tex_refs=[]; missing=[]; exact=[]; adapted=[]; external=[]
    used_trans=used_partial=used_total=0; uv_faces=0; invalid_uv=0
    # collect all direct textures too
    for k,v in textures.items():
        if not isinstance(v,str): continue
        r=resolve_tex_var(v,textures)
        if r.startswith('#'): continue
        if ':' in r and not r.startswith('thaumcraft:'): external.append(r); continue
        p=texture_file(r)
        tex_refs.append(r)
        if not p or not p.exists(): missing.append(r)
        elif sha256(p) in orig_hashes: exact.append(r)
        else: adapted.append(r)
    if cross:
        v=resolve_tex_var(textures.get('cross',textures.get('texture','#missing')),textures)
        p=texture_file(v)
        if p and p.exists():
            t,pa,tot=alpha_metrics_for_uv(p,[0,0,16,16]); used_trans+=t; used_partial+=pa; used_total+=tot
            tex_refs.append(v); (exact if sha256(p) in orig_hashes else adapted).append(v)
        else: missing.append(v)
        uv_faces=2
    for e in elements:
        if not isinstance(e,dict): continue
        fr=e.get('from',[0,0,0]); to=e.get('to',[16,16,16])
        for face,fd in (e.get('faces') or {}).items():
            if not isinstance(fd,dict): continue
            v=fd.get('texture','')
            if not isinstance(v,str): continue
            v=resolve_tex_var(v,textures)
            p=texture_file(v)
            uv=fd.get('uv',default_uv(face,fr,to)); uv_faces+=1
            if not (isinstance(uv,list) and len(uv)==4 and all(isinstance(x,(int,float)) for x in uv)):
                invalid_uv+=1; continue
            if p and p.exists():
                t,pa,tot=alpha_metrics_for_uv(p,uv); used_trans+=t; used_partial+=pa; used_total+=tot
            elif p is not None: missing.append(v)
    # geometry classification
    rotations=sum(1 for e in elements if isinstance(e,dict) and e.get('rotation'))
    full_cube=False
    if len(elements)==1:
        e=elements[0]
        full_cube=e.get('from')==[0,0,0] and e.get('to')==[16,16,16] and not e.get('rotation')
    if cross: geom='cross'
    elif m.get('parent')=='minecraft:builtin/entity' or (not elements and not m.get('external_parent')): geom='dynamic_or_empty'
    elif full_cube: geom='full_cube'
    elif rotations or len(elements)>1: geom='complex'
    elif elements: geom='partial_cuboid'
    else: geom='external_parent'
    return {
      'model':loc,'parent':m.get('parent',''),'geometry':geom,'elements':len(elements),'faces':uv_faces,'rotations':rotations,
      'texture_refs':sorted(set(tex_refs)),'missing':sorted(set(missing)),'exact':sorted(set(exact)),'adapted':sorted(set(adapted)),'external':sorted(set(external)),
      'used_transparent_pixels':used_trans,'used_partial_alpha_pixels':used_partial,'used_sample_pixels':used_total,'invalid_uv':invalid_uv,
      'display_contexts':sorted((m.get('display') or {}).keys()) if isinstance(m.get('display'),dict) else [],
    }

# ---------- Java registration maps ----------
modjava=(JAVA/'com/darkifov/thaumcraft/ThaumcraftMod.java').read_text(encoding='utf-8',errors='ignore')
clientjava=(JAVA/'com/darkifov/thaumcraft/client/ClientModEvents.java').read_text(encoding='utf-8',errors='ignore')
# const -> block id, robust across helper names
block_const={}
pat=re.compile(r'public\s+static\s+final\s+RegistryObject<Block>\s+([A-Z0-9_]+)\s*=\s*(?:BLOCKS\.register|[A-Za-z0-9_]+)\(\s*"([a-z0-9_]+)"',re.S)
for m in pat.finditer(modjava): block_const[m.group(1)]=m.group(2)
# const -> rough class/helper call
block_factory={}
for m in re.finditer(r'public\s+static\s+final\s+RegistryObject<Block>\s+([A-Z0-9_]+)\s*=\s*([^;]+);',modjava,re.S):
    const,expr=m.group(1),m.group(2)
    cls=''
    mm=re.search(r'new\s+([A-Za-z0-9_$.]+)\s*\(',expr)
    if mm: cls=mm.group(1)
    else:
        mm=re.match(r'\s*([A-Za-z0-9_]+)\s*\(',expr)
        if mm: cls='helper:'+mm.group(1)
    block_factory[const]=cls
by_id={v:k for k,v in block_const.items()}
# explicit render layer map
layer_map={}
for m in re.finditer(r'ItemBlockRenderTypes\.setRenderLayer\(\s*ThaumcraftMod\.([A-Z0-9_]+)\.get\(\)\s*,\s*RenderType\.([A-Za-z0-9_]+)\(\)\s*\)',clientjava,re.S): layer_map[m.group(1)]=m.group(2)
# BE const -> block consts from full declaration statement
be_blocks={}
for m in re.finditer(r'public\s+static\s+final\s+RegistryObject<BlockEntityType<[^;]+?\s+([A-Z0-9_]+)\s*=\s*BLOCK_ENTITIES\.register\([^;]+?;',modjava,re.S):
    stmt=m.group(0); be=m.group(1)
    cs=[c for c in re.findall(r'\b([A-Z0-9_]+)\.get\(\)',stmt) if c in block_const]
    be_blocks[be]=cs
# Renderer registration
be_renderer={}
for m in re.finditer(r'BlockEntityRenderers\.register\(ThaumcraftMod\.([A-Z0-9_]+)\.get\(\),\s*blockEntityRenderer\(([^)]+)\)\)',clientjava,re.S):
    be=m.group(1); factory=m.group(2).strip(); rr=re.search(r'([A-Za-z0-9_]+)::new',factory); renderer=rr.group(1) if rr else factory
    be_renderer[be]=renderer
block_renderers=defaultdict(list); block_entities=defaultdict(list)
for be,cs in be_blocks.items():
    for c in cs:
        bid=block_const.get(c)
        if bid:
            block_entities[bid].append(be)
            if be in be_renderer: block_renderers[bid].append(be_renderer[be])

# block classes with explicit render shape; best-effort by class/helper names and direct source search
java_text={p.stem:p.read_text(encoding='utf-8',errors='ignore') for p in JAVA.rglob('*.java')}

def render_shape_for_factory(factory:str)->str:
    if not factory or factory.startswith('helper:'): return 'UNKNOWN'
    cls=factory.split('.')[-1]; txt=java_text.get(cls,'')
    if re.search(r'RenderShape\.INVISIBLE',txt): return 'INVISIBLE'
    if re.search(r'RenderShape\.ENTITYBLOCK_ANIMATED',txt): return 'ENTITYBLOCK_ANIMATED'
    if re.search(r'RenderShape\.MODEL',txt): return 'MODEL'
    return 'DEFAULT_MODEL'

# known original family heuristic
FAMILY_RULES=[
 ('flux_goo','blockFluxGoo'),('flux_gas','blockFluxGas'),('purifying_fluid','blockFluidPure'),('amber_ore','blockCustomOre'),('cinnabar_ore','blockCustomOre'),
 ('greatwood_log','blockMagicalLog'),('silverwood_log','blockMagicalLog'),('greatwood_leaves','blockMagicalLeaves'),('silverwood_leaves','blockMagicalLeaves'),('sapling','blockCustomPlant'),
 ('taint_fibres','blockTaintFibres'),('taint','blockTaint'),('flesh_block','blockTaint'),('arcane_stone','blockCosmeticOpaque'),('eldritch_stone','blockEldritch'),
 ('crystal','blockCrystal'),('essentia_tube','blockTube'),('mirror','blockMirror'),('table','blockTable'),('hungry_chest','blockChestHungry'),('door','blockArcaneDoor'),
 ('alchemical_furnace','blockAlchemyFurnace'),('essentia_jar','blockJar'),('candle','blockCandle'),('eldritch','blockEldritch'),('aura_node','blockAiry'),
 ('warded','blockWarded'),('temporary_hole','blockHole'),('reservoir','blockEssentiaReservoir'),('loot_urn','blockLootUrn'),('loot_crate','blockLootCrate')]
def family(bid:str)->str:
    for needle,f in FAMILY_RULES:
        if needle in bid: return f
    return 'UNMAPPED_OR_ADDON'

def scope(bid:str)->str:
    if bid.startswith(('tt_','tce_','extras_','thaumic_me_','essentia_','arcane_crafting_terminal','matrix_','mnemonic_','vis_interface')) and bid not in {'essentia_jar','essentia_reservoir','essentia_tube','essentia_tube_buffer','essentia_tube_filter','essentia_tube_oneway','essentia_tube_restrict'}:
        return 'ADDON_OR_COMPAT'
    return 'CORE_OR_LEGACY'

def priority(bid:str, dynamic:bool, geom:str)->str:
    if any(x in bid for x in ['jar','wand','node','infusion','crucible','research','arcane_workbench','focal','hungry_chest','mirror','alembic','centrifuge','furnace','thaumatorium','tube','relay','reservoir']): return 'P0'
    if dynamic or geom in {'complex','cross','partial_cuboid'}: return 'P1'
    return 'P2'

# ---------- audit every blockstate ----------
block_rows=[]; variant_rows=[]; item_rows=[]; signatures=defaultdict(list)
for bs in sorted(BLOCKSTATES.glob('*.json')):
    bid=bs.stem; const=by_id.get(bid,''); bsd=loadj(bs); model_locs=collect_models_from_blockstate(bsd)
    analyses=[model_analysis(x) for x in model_locs]
    geom_types=sorted(set(a['geometry'] for a in analyses))
    exact=sorted(set(x for a in analyses for x in a['exact'])); adapted=sorted(set(x for a in analyses for x in a['adapted']))
    missing=sorted(set(x for a in analyses for x in a['missing'])); external=sorted(set(x for a in analyses for x in a['external']))
    trans=sum(a['used_transparent_pixels'] for a in analyses); partial=sum(a['used_partial_alpha_pixels'] for a in analyses); samples=sum(a['used_sample_pixels'] for a in analyses)
    layer=layer_map.get(const,'solid')
    renderer=sorted(set(block_renderers.get(bid,[]))); be=sorted(set(block_entities.get(bid,[])))
    fac=block_factory.get(const,''); rshape=render_shape_for_factory(fac)
    dynamic=bool(renderer) or rshape in {'INVISIBLE','ENTITYBLOCK_ANIMATED'} or any(g=='dynamic_or_empty' for g in geom_types)
    # item model
    ip=ITEMS/f'{bid}.json'; item_status='MISSING'; item_parent=''; item_builtin=False; item_disp=[]
    if ip.exists():
        try:
            im=resolve_model(f'thaumcraft:item/{bid}'); item_parent=im.get('parent',''); item_builtin=item_parent=='minecraft:builtin/entity' or (not im.get('elements') and not im.get('external_parent'))
            item_disp=sorted((im.get('display') or {}).keys()) if isinstance(im.get('display'),dict) else []
            item_status='CUSTOM_RENDERER' if item_builtin else 'STATIC_MODEL'
        except Exception as e: item_status='INVALID'
    # exact status
    issues=[]; confirmed=[]; risks=[]
    if not model_locs: confirmed.append('NO_BLOCKSTATE_MODEL')
    if missing: confirmed.append('MISSING_TEXTURE_OR_MODEL')
    if any(a['invalid_uv'] for a in analyses): confirmed.append('INVALID_UV')
    if not dynamic:
        if layer=='solid' and partial>0: confirmed.append('PARTIAL_ALPHA_ON_SOLID_LAYER')
        elif layer=='solid' and trans>0: confirmed.append('TRANSPARENT_PIXELS_ON_SOLID_LAYER')
    # suspicious device placeholder: BE or original-special family but only full cubes, no renderer
    if be and not renderer and set(geom_types).issubset({'full_cube','external_parent'}) and family(bid)!='UNMAPPED_OR_ADDON':
        risks.append('BLOCK_ENTITY_STATIC_CUBE_REVIEW')
    # complex core special block with adapted-only textures
    if scope(bid)=='CORE_OR_LEGACY' and adapted and not exact:
        risks.append('CORE_USES_ONLY_ADAPTED_TEXTURES')
    if external: risks.append('EXTERNAL_TEXTURE_REFERENCE')
    if item_status=='MISSING': risks.append('NO_SAME_ID_ITEM_MODEL')
    if dynamic and not renderer and rshape=='INVISIBLE': confirmed.append('INVISIBLE_WITHOUT_REGISTERED_BER')
    if renderer and not item_builtin and ip.exists(): risks.append('WORLD_BER_BUT_ITEM_NOT_CUSTOM_RENDERED')
    # state and model diversity
    if len(model_locs)==1 and geom_types==['full_cube'] and be and scope(bid)=='CORE_OR_LEGACY': risks.append('SINGLE_FULL_CUBE_FOR_TILE_DEVICE')
    if confirmed: status='CONFIRMED_STATIC_MISMATCH'
    elif risks: status='REVIEW_REQUIRED'
    elif dynamic: status='STATIC_DYNAMIC_CONTRACT_PRESENT'
    else: status='STATIC_VALID'
    pr=priority(bid,dynamic,geom_types[0] if len(geom_types)==1 else 'complex')
    row={
      'block_id':bid,'scope':scope(bid),'priority':pr,'registry_constant':const,'factory_or_class':fac,'render_shape':rshape,'original_family':family(bid),
      'blockstate_models':' | '.join(model_locs),'model_count':len(model_locs),'geometry_types':' | '.join(geom_types),'block_entities':' | '.join(be),'world_renderers':' | '.join(renderer),
      'render_layer':layer,'used_transparent_pixels':trans,'used_partial_alpha_pixels':partial,'alpha_sample_pixels':samples,
      'exact_original_textures':' | '.join(exact),'exact_original_texture_count':len(exact),'adapted_or_new_textures':' | '.join(adapted),'adapted_texture_count':len(adapted),
      'external_textures':' | '.join(external),'missing_resources':' | '.join(missing),
      'item_model_status':item_status,'item_parent':item_parent,'item_display_contexts':' | '.join(item_disp),
      'status':status,'confirmed_issues':' | '.join(confirmed),'review_flags':' | '.join(risks),'runtime_status':'NOT_TESTED'
    }
    block_rows.append(row)
    # signature by models + texture hashes + geometry
    sig_parts=[]
    for a in analyses:
        texhash=[]
        for t in a['texture_refs']:
            p=texture_file(t); texhash.append(sha256(p) if p and p.exists() else t)
        sig_parts.append(json.dumps({'g':a['geometry'],'e':a['elements'],'f':a['faces'],'t':sorted(texhash)},sort_keys=True))
        variant_rows.append({'block_id':bid,**{k:(' | '.join(v) if isinstance(v,list) else v) for k,v in a.items()}})
    sig=hashlib.sha256('\n'.join(sorted(sig_parts)).encode()).hexdigest(); signatures[sig].append(bid)
    item_rows.append({'block_id':bid,'item_model_exists':ip.exists(),'item_model_status':item_status,'parent':item_parent,**{c:('EXPLICIT' if c in item_disp else ('CUSTOM_RENDERER' if item_builtin else 'INHERITED')) for c in ['gui','ground','fixed','firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand','head']},'runtime_status':'NOT_TESTED'})

clone_rows=[]
for sig,ids in signatures.items():
    if len(ids)>1:
        # only flag heterogeneous names; stairs/slabs/candles often legitimately share geometry but texture hashes distinguish them
        clone_rows.append({'signature':sig,'count':len(ids),'block_ids':' | '.join(sorted(ids)),'status':'REVIEW_REQUIRED' if len(ids)>2 else 'INFORMATIONAL'})

# renderer details
renderer_rows=[]
for bid in sorted(block_entities):
    for be in block_entities[bid]:
        rr=be_renderer.get(be,'')
        source=''
        texrefs=[]; rtypes=[]; fullbright=False; additive=False; objrefs=[]
        if rr:
            candidates=list(JAVA.rglob(rr+'.java'))
            if candidates:
                p=candidates[0]; source=p.relative_to(ROOT).as_posix(); txt=p.read_text(encoding='utf-8',errors='ignore')
                texrefs=sorted(set(re.findall(r'"((?:thaumcraft:)?textures/[A-Za-z0-9_./-]+\.png)"',txt)))
                rtypes=sorted(set(re.findall(r'RenderType\.([A-Za-z0-9_]+)',txt)))
                fullbright=bool(re.search(r'FULL_BRIGHT|0xF000F0',txt)); additive=bool(re.search(r'additive|ADDITIVE',txt,re.I))
                objrefs=sorted(set(re.findall(r'"([A-Za-z0-9_./-]+\.obj)"',txt)))
        renderer_rows.append({'block_id':bid,'block_entity_type':be,'renderer':rr or 'NONE','source':source,'textures':' | '.join(texrefs),'obj_models':' | '.join(objrefs),'render_types':' | '.join(rtypes),'full_bright':fullbright,'additive':additive,'status':'STATIC_REVIEW_REQUIRED' if rr else 'NO_CUSTOM_RENDERER','runtime_status':'NOT_TESTED'})

# summaries and top issue groups
status_counts=Counter(r['status'] for r in block_rows); scope_counts=Counter(r['scope'] for r in block_rows)
confirmed=[r for r in block_rows if r['status']=='CONFIRMED_STATIC_MISMATCH']
review=[r for r in block_rows if r['status']=='REVIEW_REQUIRED']
dynamic=[r for r in block_rows if r['status']=='STATIC_DYNAMIC_CONTRACT_PRESENT']
valid=[r for r in block_rows if r['status']=='STATIC_VALID']
issue_counts=Counter()
for r in block_rows:
    for x in (r['confirmed_issues']+' | '+r['review_flags']).split(' | '):
        if x.strip(): issue_counts[x.strip()]+=1

writecsv(OUT/'block_texture_uv_audit.csv',block_rows)
writecsv(OUT/'block_model_variant_audit.csv',variant_rows)
writecsv(OUT/'block_render_type_audit.csv',[{k:r[k] for k in ['block_id','priority','scope','render_layer','used_transparent_pixels','used_partial_alpha_pixels','world_renderers','render_shape','status','confirmed_issues','review_flags','runtime_status']} for r in block_rows])
writecsv(OUT/'block_custom_renderer_audit.csv',renderer_rows)
writecsv(OUT/'block_item_context_audit.csv',item_rows)
writecsv(OUT/'block_clone_groups.csv',clone_rows)

# Contact sheet: first texture or colored status card
thumbs=[]
status_color={'CONFIRMED_STATIC_MISMATCH':(150,40,40),'REVIEW_REQUIRED':(150,110,30),'STATIC_DYNAMIC_CONTRACT_PRESENT':(40,100,150),'STATIC_VALID':(40,120,65)}
for r in block_rows:
    tex=(r['exact_original_textures'] or r['adapted_or_new_textures']).split(' | ')[0]
    im=Image.new('RGBA',(64,64),(50,50,50,255))
    p=texture_file(tex) if tex else None
    if p and p.exists():
        try: im=Image.open(p).convert('RGBA').resize((64,64),Image.Resampling.NEAREST)
        except: pass
    thumbs.append((r,im))
cols=8; cellw=180; cellh=96; rowsn=math.ceil(len(thumbs)/cols)
canvas=Image.new('RGBA',(cols*cellw,rowsn*cellh),(24,24,24,255)); d=ImageDraw.Draw(canvas)
for i,(r,im) in enumerate(thumbs):
    x=(i%cols)*cellw; y=(i//cols)*cellh
    canvas.alpha_composite(im,(x+4,y+4)); col=status_color[r['status']]
    d.rectangle((x+72,y+5,x+176,y+23),fill=col); d.text((x+75,y+8),r['status'].replace('STATIC_','')[:16],fill='white')
    d.text((x+72,y+28),r['block_id'][:20],fill='white')
    d.text((x+72,y+44),r['geometry_types'][:20],fill=(200,200,200))
    d.text((x+72,y+60),('RT:'+r['render_layer'])[:20],fill=(180,180,180))
    d.text((x+72,y+76),('P:'+r['priority']+' '+r['scope'][:8]),fill=(180,180,180))
canvas.save(OUT/'all_blocks_static_contact_sheet.png')

# Separate issue contact sheet
issues=confirmed+review
cols=5; cellw=240; cellh=120; rowsn=max(1,math.ceil(len(issues)/cols))
canvas=Image.new('RGBA',(cols*cellw,rowsn*cellh),(24,24,24,255)); d=ImageDraw.Draw(canvas)
for i,r in enumerate(issues):
    x=(i%cols)*cellw; y=(i//cols)*cellh
    tex=(r['exact_original_textures'] or r['adapted_or_new_textures']).split(' | ')[0]; p=texture_file(tex) if tex else None
    im=Image.new('RGBA',(80,80),(50,50,50,255))
    if p and p.exists():
        try: im=Image.open(p).convert('RGBA').resize((80,80),Image.Resampling.NEAREST)
        except: pass
    canvas.alpha_composite(im,(x+4,y+4)); d.text((x+90,y+5),r['block_id'][:24],fill='white')
    d.text((x+90,y+22),r['status'][:22],fill=status_color[r['status']])
    detail=r['confirmed_issues'] or r['review_flags']
    # wrap simple
    for j in range(0,min(len(detail),90),30): d.text((x+90,y+42+(j//30)*15),detail[j:j+30],fill=(210,210,210))
canvas.save(OUT/'block_visual_risk_contact_sheet.png')

manifest={
 'version':'11.63.10','scope':'all shipped Thaumcraft blockstates','status':'STATIC_AUDIT_COMPLETE_RUNTIME_NOT_TESTED',
 'counts':{'blockstates':len(block_rows),'core_or_legacy':scope_counts['CORE_OR_LEGACY'],'addon_or_compat':scope_counts['ADDON_OR_COMPAT'],'model_variants':len(variant_rows),'block_entity_bindings':len(renderer_rows),'clone_groups':len(clone_rows),
           'static_valid':len(valid),'dynamic_contract_present':len(dynamic),'review_required':len(review),'confirmed_static_mismatch':len(confirmed)},
 'issue_counts':dict(issue_counts),
 'limitations':['No automated Minecraft 1.7.10/1.19.2 runtime side-by-side captures.','External vanilla parent geometry is classified but not re-rendered by this static parser.','Static validity does not prove lighting, culling, z-sorting, animation or transforms in game.']
}
(OUT/'audit_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

summary=['# Полный статический аудит всех блоков — Thaumcraft Legacy Rebuild 11.63.10','',
'## Область','',f'- Все поставляемые blockstate-файлы: **{len(block_rows)}**.','- Проверены модели/варианты, texture chains, SHA-идентичность оригинальным PNG, UV, реально используемая альфа, RenderType, BlockEntityRenderer, item-модель и восемь display-контекстов.','- Runtime side-by-side не выполнялся: итоговая визуальная parity остаётся `NOT TESTED`.','',
'## Результат','',f'- `STATIC_VALID`: **{len(valid)}**.',f'- `STATIC_DYNAMIC_CONTRACT_PRESENT`: **{len(dynamic)}**.',f'- `REVIEW_REQUIRED`: **{len(review)}**.',f'- `CONFIRMED_STATIC_MISMATCH`: **{len(confirmed)}**.','',
'## Подтверждённые статические расхождения','']
if confirmed:
    for r in sorted(confirmed,key=lambda x:(x['priority'],x['block_id'])): summary.append(f"- **{r['block_id']} ({r['priority']})** — {r['confirmed_issues']}")
else: summary.append('- Нет.')
summary += ['','## Группы, требующие source/runtime-проверки','']
for k,n in issue_counts.most_common(): summary.append(f'- `{k}`: **{n}**')
summary += ['','## Правила интерпретации','',
'- `TRANSPARENT_PIXELS_ON_SOLID_LAYER` и `PARTIAL_ALPHA_REQUIRES_TRANSLUCENT` основаны не на альфе всего PNG, а на пикселях, попадающих в UV реально используемых граней.','- `BLOCK_ENTITY_STATIC_CUBE_REVIEW` и `SINGLE_FULL_CUBE_FOR_TILE_DEVICE` — не автоматический FAIL: это очередь на сравнение с legacy ISBRH/TESR и runtime-скриншотами.','- `STATIC_DYNAMIC_CONTRACT_PRESENT` означает наличие BER/динамического пути, но не доказывает масштаб, ориентацию, свет и UV в игре.','',
'## Артефакты','',
'- `block_texture_uv_audit.csv` — одна строка на каждый block ID.','- `block_model_variant_audit.csv` — одна строка на каждую модель, достижимую из blockstate.','- `block_render_type_audit.csv`, `block_custom_renderer_audit.csv`, `block_item_context_audit.csv`.','- `block_clone_groups.csv`.','- `all_blocks_static_contact_sheet.png` и `block_visual_risk_contact_sheet.png`.']
(OUT/'summary.md').write_text('\n'.join(summary)+'\n',encoding='utf-8')

known=['# KNOWN_DEVIATIONS — блоки 11.63.10','',
'## Подтверждённые','']
if confirmed:
    for r in confirmed: known.append(f"- `{r['block_id']}` — {r['confirmed_issues']}; runtime: NOT TESTED.")
else: known.append('- Нет подтверждённых статических расхождений.')
known += ['','## Не доказано','',
'- Для всех 202 блоков runtime-внешний вид в World/GUI/Ground/Fixed/First-person/Third-person не подтверждён одинаковыми скриншотами.','- Все BER-пути требуют проверки culling, lighting, full-bright, translucent sorting и item transforms.','- Блоки с legacy TileEntity и статической кубической моделью перечислены как REVIEW_REQUIRED, пока не выполнено поимённое сравнение с оригинальным renderer-кодом.']
(OUT/'KNOWN_DEVIATIONS_BLOCKS.md').write_text('\n'.join(known)+'\n',encoding='utf-8')
print(json.dumps(manifest,ensure_ascii=False,indent=2))
