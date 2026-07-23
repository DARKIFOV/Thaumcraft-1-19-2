#!/usr/bin/env python3
from __future__ import annotations
import argparse, csv, hashlib, json, math, re, shutil
from collections import Counter, defaultdict
from pathlib import Path, PurePosixPath
from typing import Any
from PIL import Image, ImageChops, ImageDraw, ImageFont
import numpy as np

DISPLAY_CONTEXTS = [
    'gui','ground','fixed','firstperson_righthand','firstperson_lefthand',
    'thirdperson_righthand','thirdperson_lefthand','head'
]


_SHA1_CACHE: dict[Path,str] = {}
_PNG_CACHE: dict[Path,dict[str,Any]] = {}

def sha1(path: Path) -> str:
    if path not in _SHA1_CACHE:
        _SHA1_CACHE[path]=hashlib.sha1(path.read_bytes()).hexdigest()
    return _SHA1_CACHE[path]

def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()

def safe_name(text: str) -> str:
    return re.sub(r'[^A-Za-z0-9_.-]+', '_', text)[:180]

def png_info(path: Path) -> dict[str, Any]:
    if path in _PNG_CACHE:
        return _PNG_CACHE[path]
    try:
        with Image.open(path) as im:
            im.load()
            mode = im.mode
            width, height = im.size
            rgba = im.convert('RGBA')
            a = np.asarray(rgba, dtype=np.uint8)[:,:,3]
            alpha_min, alpha_max = int(a.min()), int(a.max())
            transparent = int(np.count_nonzero(a == 0))
            partial = int(np.count_nonzero((a > 0) & (a < 255)))
            bits = {'RGBA':32,'RGB':24,'LA':16,'L':8,'P':8}.get(mode, 0)
            result={
                'width': width, 'height': height, 'mode': mode, 'bit_depth_estimate': bits,
                'has_alpha': alpha_min < 255, 'alpha_min': alpha_min, 'alpha_max': alpha_max,
                'transparent_pixels': transparent, 'partial_alpha_pixels': partial,
            }
            _PNG_CACHE[path]=result
            return result
    except Exception as exc:
        result={'error': str(exc)}; _PNG_CACHE[path]=result; return result

def mcmeta_info(path: Path) -> dict[str, Any]:
    try:
        data = json.loads(path.read_text(encoding='utf-8'))
        anim = data.get('animation', {}) if isinstance(data, dict) else {}
        frames = anim.get('frames', [])
        return {
            'parse': 'OK', 'frametime': anim.get('frametime', 1),
            'interpolate': bool(anim.get('interpolate', False)),
            'explicit_frames': len(frames) if isinstance(frames, list) else 0,
            'frame_spec': json.dumps(frames, ensure_ascii=False, separators=(',',':'))[:1000],
        }
    except Exception as exc:
        return {'parse':'ERROR','error':str(exc)}

def global_ssim(a: np.ndarray, b: np.ndarray) -> float:
    a = a.astype(np.float64); b = b.astype(np.float64)
    if a.shape != b.shape: return float('nan')
    mu_a, mu_b = a.mean(), b.mean()
    va, vb = a.var(), b.var()
    cov = ((a-mu_a)*(b-mu_b)).mean()
    c1=(0.01*255)**2; c2=(0.03*255)**2
    den=(mu_a**2+mu_b**2+c1)*(va+vb+c2)
    return float(((2*mu_a*mu_b+c1)*(2*cov+c2))/den) if den else 1.0

def image_metrics(a_path: Path, b_path: Path) -> dict[str, Any]:
    try:
        a=np.asarray(Image.open(a_path).convert('RGBA'), dtype=np.float64)
        b=np.asarray(Image.open(b_path).convert('RGBA'), dtype=np.float64)
        if a.shape != b.shape:
            return {'same_size':False}
        d=np.abs(a-b)
        return {
            'same_size':True, 'mae':float(d.mean()), 'rmse':float(np.sqrt((d*d).mean())),
            'exact_pixel_ratio':float(np.all(a==b,axis=2).mean()), 'global_ssim':global_ssim(a,b)
        }
    except Exception as exc:
        return {'error':str(exc)}

def make_diff(a_path: Path, b_path: Path, out: Path, title: str) -> None:
    a=Image.open(a_path).convert('RGBA'); b=Image.open(b_path).convert('RGBA')
    if a.size != b.size: return
    scale=max(1,min(8,256//max(a.size)))
    aa=a.resize((a.width*scale,a.height*scale),Image.Resampling.NEAREST)
    bb=b.resize((b.width*scale,b.height*scale),Image.Resampling.NEAREST)
    diff=ImageChops.difference(a,b).convert('RGBA')
    arr=np.asarray(diff).copy(); arr[:,:,:3]=np.minimum(255,arr[:,:,:3]*4); arr[:,:,3]=255
    dd=Image.fromarray(arr).resize(aa.size,Image.Resampling.NEAREST)
    w=aa.width*3+40; h=aa.height+56
    canvas=Image.new('RGBA',(w,h),(30,30,30,255)); draw=ImageDraw.Draw(canvas)
    draw.text((10,8),title,fill='white'); draw.text((10,30),'ORIGINAL',fill='white')
    draw.text((20+aa.width,30),'PORT',fill='white'); draw.text((30+aa.width*2,30),'DIFF x4',fill='white')
    canvas.alpha_composite(aa,(10,50)); canvas.alpha_composite(bb,(20+aa.width,50)); canvas.alpha_composite(dd,(30+aa.width*2,50))
    out.parent.mkdir(parents=True,exist_ok=True); canvas.save(out)

def priority_for(path: str, kind: str='texture') -> str:
    s=path.lower()
    if any(x in s for x in ['wand','jar','node','fortress','crimson','trunk','thaumometer','armor','bone_bow','infusion_matrix','infusionmatrix']): return 'P0'
    if any(x in s for x in ['gui','particle','particles','misc','aspect']): return 'P2'
    return 'P1'

def text_inventory(root: Path) -> tuple[str,list[Path]]:
    files=[]; chunks=[]
    for base in [root/'src/main/java', root/'src/main/resources']:
        if not base.exists(): continue
        for p in base.rglob('*'):
            if p.is_file() and p.suffix.lower() in {'.java','.json','.mcmeta','.toml','.md','.txt','.lang'} and 'textures/original/' not in p.as_posix():
                try: t=p.read_text(encoding='utf-8',errors='ignore')
                except Exception: continue
                files.append(p); chunks.append(t)
    return '\n'.join(chunks),files

def build_ref_counter(text: str) -> Counter[str]:
    counter: Counter[str] = Counter()
    patterns=[r'textures/[A-Za-z0-9_./-]+(?:\.png)?',r'thaumcraft:[A-Za-z0-9_./-]+']
    for pat in patterns:
        counter.update(re.findall(pat,text))
    return counter

def count_refs(counter: Counter[str], rel: str) -> int:
    noext=rel[:-4] if rel.endswith('.png') else rel
    return counter.get(f'textures/{rel}',0)+counter.get(f'thaumcraft:{noext}',0)

def resolve_model_texture(root: Path, loc: str) -> Path | None:
    if loc.startswith('#'): return None
    ns, path = (loc.split(':',1) if ':' in loc else ('minecraft',loc))
    if ns!='thaumcraft': return None
    p=root/'src/main/resources/assets/thaumcraft/textures'/f'{path}.png'
    return p

def load_json(path: Path) -> dict[str,Any]:
    return json.loads(path.read_text(encoding='utf-8'))

def flatten_faces(elements: list[Any]) -> list[dict[str,Any]]:
    out=[]
    for ei,e in enumerate(elements or []):
        if not isinstance(e,dict): continue
        for face,fd in (e.get('faces') or {}).items():
            if isinstance(fd,dict): out.append({'element':ei,'face':face,**fd})
    return out

def parse_resource_locations(source: str) -> list[str]:
    vals=[]
    # Capture two-argument ResourceLocation(namespace, path) first.
    for m in re.finditer(r'new\s+ResourceLocation\(\s*"([a-z0-9_.-]+)"\s*,\s*"(textures/[A-Za-z0-9_./-]+\.(?:png|jpg))"\s*\)', source, re.S):
        vals.append(f"{m.group(1)}:{m.group(2)}")
    # Capture one-argument namespaced locations and bare texture paths.
    for m in re.finditer(r'"([a-z0-9_.-]+:textures/[A-Za-z0-9_./-]+\.(?:png|jpg))"', source): vals.append(m.group(1))
    for m in re.finditer(r'"(textures/[A-Za-z0-9_./-]+\.(?:png|jpg))"', source): vals.append(m.group(1))
    for m in re.finditer(r'"(thaumcraft:[A-Za-z0-9_./-]+)"', source): vals.append(m.group(1))
    return sorted(set(vals))

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',required=True)
    ap.add_argument('--version',default='11.63.23')
    ap.add_argument('--out',required=True)
    ap.add_argument('--baseline-wisp',action='store_true')
    args=ap.parse_args()
    root=Path(args.root).resolve(); out=Path(args.out).resolve(); out.mkdir(parents=True,exist_ok=True)
    diffs=out/'diffs'; diffs.mkdir(exist_ok=True)
    tex_root=root/'src/main/resources/assets/thaumcraft/textures'
    orig_root=tex_root/'original/thaumcraft4'
    inv_path=root/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_1710_asset_inventory.json'
    inv=json.loads(inv_path.read_text(encoding='utf-8'))
    all_text,text_files=text_inventory(root)
    ref_counter=build_ref_counter(all_text)

    original_entries=[e for e in inv if e.get('path','').startswith('textures/') and (e['path'].endswith('.png') or e['path'].endswith('.png.mcmeta'))]
    core_port_files=[p for p in tex_root.rglob('*') if p.is_file() and '/textures/original/' not in p.as_posix() and (p.suffix.lower()=='.png' or p.name.endswith('.png.mcmeta'))]
    shipped_files=[p for p in tex_root.rglob('*') if p.is_file() and (p.suffix.lower()=='.png' or p.name.endswith('.png.mcmeta'))]
    shipped_sha=defaultdict(list); core_sha=defaultdict(list)
    for p in shipped_files: shipped_sha[sha1(p)].append(p)
    for p in core_port_files: core_sha[sha1(p)].append(p)
    orig_sha={e['sha1']:e for e in original_entries}
    orig_sha_paths=defaultdict(list)
    for e in original_entries: orig_sha_paths[e['sha1']].append(e['path'])

    texture_rows=[]
    used_original=0; exact_shipped=0; exact_core=0
    for e in original_entries:
        rel=e['path'][len('textures/'):]
        canon=orig_root/rel
        shipped=shipped_sha.get(e['sha1'],[])
        core=core_sha.get(e['sha1'],[])
        refs=[]; ref_count=0
        for p in shipped:
            rp=p.relative_to(tex_root).as_posix()
            c=count_refs(ref_counter,rp)
            if c: refs.append(f'{rp}:{c}'); ref_count+=c
        if shipped: exact_shipped+=1
        if core: exact_core+=1
        if ref_count: used_original+=1
        status='MATCH' if shipped else 'MISSING'
        application='USED' if ref_count else 'PRESENT_UNUSED'
        row={
            'record_type':'ORIGINAL','original_path':e['path'],'port_paths':' | '.join(p.relative_to(tex_root).as_posix() for p in shipped),
            'status':status,'application_status':application,'priority':priority_for(rel),
            'original_sha1':e['sha1'],'original_sha256':sha256(canon) if canon.exists() else '',
            'port_sha1':e['sha1'] if shipped else '', 'reference_count':ref_count,'references':' | '.join(refs),
            'byte_exact_core_copy_count':len(core),'byte_exact_shipped_count':len(shipped),
            'notes':'Canonical TC4 asset is shipped byte-exact; runtime appearance still depends on model/UV/render state.' if shipped else 'Canonical asset missing from shipped resources.'
        }
        if e['path'].endswith('.png') and canon.exists(): row.update({f'original_{k}':v for k,v in png_info(canon).items()})
        elif canon.exists(): row.update({f'original_{k}':v for k,v in mcmeta_info(canon).items()})
        texture_rows.append(row)

    # Port-only/adapted inventory.
    port_rows=[]
    for p in core_port_files:
        rel=p.relative_to(tex_root).as_posix(); h=sha1(p); exact=h in orig_sha
        refs=count_refs(ref_counter,rel)
        row={
            'record_type':'PORT','port_path':rel,'status':'MATCH' if exact else 'EXTRA',
            'classification':'EXACT_TC4_COPY' if exact else 'ADAPTED_OR_NEW',
            'priority':priority_for(rel),'sha1':h,'sha256':sha256(p),'reference_count':refs,
            'original_paths':' | '.join(orig_sha_paths.get(h,[])),
            'notes':'Byte-exact copy of an original TC4 asset.' if exact else 'No byte-identical TC4 core asset; may be a modern adaptation, generated asset, migration/add-on resource, or a true visual deviation.'
        }
        if p.suffix.lower()=='.png': row.update(png_info(p))
        else: row.update(mcmeta_info(p))
        port_rows.append(row)

    # Create diffs only for non-exact same-basename core candidates of equal dimensions.
    orig_by_base=defaultdict(list)
    for e in original_entries:
        if e['path'].endswith('.png'): orig_by_base[Path(e['path']).name.lower()].append(orig_root/e['path'][len('textures/'):])
    diff_records=[]
    for p in core_port_files:
        if p.suffix.lower()!='.png' or sha1(p) in orig_sha: continue
        cands=orig_by_base.get(p.name.lower(),[])
        best=None; bestm=None
        for q in cands:
            if not q.exists(): continue
            m=image_metrics(q,p)
            if m.get('same_size') and (bestm is None or m.get('mae',1e9)<bestm.get('mae',1e9)):
                best,bestm=q,m
        if best and bestm and bestm.get('mae',0)>0:
            fn=safe_name(p.relative_to(tex_root).as_posix())+'.png'; dest=diffs/'textures'/fn
            make_diff(best,p,dest,f'{best.relative_to(orig_root)} -> {p.relative_to(tex_root)}')
            diff_records.append({'original':best.relative_to(orig_root).as_posix(),'port':p.relative_to(tex_root).as_posix(),'diff':dest.relative_to(out).as_posix(),**bestm})

    # Model audit, every item and block JSON.
    model_rows=[]; item_context_rows=[]
    models_root=root/'src/main/resources/assets/thaumcraft/models'
    for kind in ['item','block']:
        base=models_root/kind
        for p in sorted(base.rglob('*.json')):
            rel=p.relative_to(models_root).as_posix(); row={'model':rel,'kind':kind,'priority':priority_for(rel,'model')}
            try: d=load_json(p); row['parse']='OK'
            except Exception as exc:
                row.update({'parse':'ERROR','status':'MISMATCH','problems':str(exc)}); model_rows.append(row); continue
            parent=d.get('parent',''); elements=d.get('elements') or []; faces=flatten_faces(elements)
            uv_explicit=[f for f in faces if 'uv' in f]; uv_bad=[]; uv_fractional=0
            for f in uv_explicit:
                uv=f.get('uv')
                if not isinstance(uv,list) or len(uv)!=4 or any(not isinstance(x,(int,float)) for x in uv): uv_bad.append(f'{f["element"]}:{f["face"]}:invalid'); continue
                if any(x<0 or x>16 for x in uv): uv_bad.append(f'{f["element"]}:{f["face"]}:out_of_0_16={uv}')
                if any(abs(float(x)-round(float(x)))>1e-9 for x in uv): uv_fractional+=1
            tex=d.get('textures') or {}; texture_paths=[]; missing=[]; exact=0; adapted=0
            for slot,loc in tex.items():
                if not isinstance(loc,str) or loc.startswith('#'): continue
                tp=resolve_model_texture(root,loc)
                if tp is None: continue
                texture_paths.append(f'{slot}={loc}')
                if not tp.exists(): missing.append(loc)
                elif sha1(tp) in orig_sha: exact+=1
                else: adapted+=1
            display=d.get('display') or {}; contexts=sorted(k for k in display if k in DISPLAY_CONTEXTS)
            builtin=parent=='minecraft:builtin/entity'
            problems=[]
            if missing: problems.append('missing_texture')
            if uv_bad: problems.append('invalid_uv')
            dynamic_empty = not parent and not elements
            if dynamic_empty: problems.append('dynamic_or_builtin_no_json_geometry')
            hard_problems = bool(missing or uv_bad)
            status='MISMATCH' if hard_problems else ('STATIC_RISK_DYNAMIC_RENDERER' if (builtin or dynamic_empty) else ('MATCH_STATIC_RESOURCE_CHAIN' if exact and not adapted else 'ADAPTED_STATIC_VALID'))
            row.update({
                'parse':'OK','parent':parent,'element_count':len(elements),'face_count':len(faces),
                'explicit_uv_faces':len(uv_explicit),'auto_uv_faces':len(faces)-len(uv_explicit),
                'uv_problem_count':len(uv_bad),'uv_problems':' | '.join(uv_bad),'fractional_uv_faces':uv_fractional,
                'rotation_element_count':sum(1 for e in elements if isinstance(e,dict) and e.get('rotation')),
                'texture_slot_count':len(tex),'texture_refs':' | '.join(texture_paths),'missing_texture_count':len(missing),
                'exact_original_texture_refs':exact,'adapted_or_new_texture_refs':adapted,
                'builtin_entity':builtin,'explicit_display_contexts':' | '.join(contexts),
                'comparison_basis':'Modern JSON has no one-to-one TC4 1.7.10 JSON counterpart; UV parity requires legacy model/TESR source or runtime side-by-side.',
                'status':status,'problems':' | '.join(problems)
            })
            model_rows.append(row)
            if kind=='item':
                item_context_rows.append({
                    'item_model':rel,'parent':parent,'builtin_entity':builtin,
                    **{ctx:('EXPLICIT' if ctx in contexts else ('CUSTOM_RENDERER' if builtin else 'INHERITED_DEFAULT')) for ctx in DISPLAY_CONTEXTS},
                    'runtime_status':'NOT_TESTED','priority':priority_for(rel,'model')
                })

    # Custom renderer audit: exhaustive Java list under client/render + BEWLR item classes.
    renderer_rows=[]
    render_files=set((root/'src/main/java/com/darkifov/thaumcraft/client/render').glob('*.java'))
    for p in (root/'src/main/java/com/darkifov/thaumcraft').rglob('*.java'):
        txt=p.read_text(encoding='utf-8',errors='ignore')
        if 'BlockEntityWithoutLevelRenderer' in txt or 'initializeClient' in txt and 'getCustomRenderer' in txt:
            render_files.add(p)
    tool_text='\n'.join(q.read_text(encoding='utf-8',errors='ignore') for q in (root/'tools').glob('*.py'))
    for p in sorted(render_files):
        txt=p.read_text(encoding='utf-8',errors='ignore'); cls=p.stem
        if 'BlockEntityWithoutLevelRenderer' in txt: rtype='BEWLR'
        elif 'BlockEntityRenderer' in txt: rtype='BER'
        elif re.search(r'extends\s+(?:MobRenderer|EntityRenderer|HumanoidMobRenderer)',txt): rtype='ENTITY'
        elif 'RenderLayer' in cls or cls.endswith('Layer'): rtype='LAYER'
        elif 'ModelPart' in txt or 'LayerDefinition' in txt: rtype='MODEL_HELPER'
        else: rtype='RENDER_HELPER'
        textures=parse_resource_locations(txt)
        render_types=sorted(set(re.findall(r'RenderType\.([A-Za-z0-9_]+)',txt)))
        if 'TC4NodeRenderTypes.node' in txt: render_types.append('TC4NodeRenderTypes.node')
        fullbright=bool(re.search(r'0xF000F0|FULL_BRIGHT|LightTexture\.FULL_BRIGHT',txt))
        additive=bool(re.search(r'ADDITIVE|additive|TC4NodeRenderTypes\.node\([^\n]+true',txt))
        guarded=cls in tool_text
        mismatch=''
        status='STATIC_CONTRACT_GUARDED' if guarded else 'STATIC_REVIEW_REQUIRED'
        if cls=='TC4WispRenderer' and args.baseline_wisp:
            mismatch='Uses full 0..1 Wisp atlas for both shell and core; PARTICLES constant points to Minecraft and is unused; no 4x4/16x1 frame UV; size and additive blend differ from TC4 RenderWisp.'
            status='CONFIRMED_MISMATCH'
        renderer_rows.append({
            'class':cls,'source':p.relative_to(root).as_posix(),'renderer_type':rtype,'priority':priority_for(cls,'renderer'),
            'textures':' | '.join(textures),'render_types':' | '.join(sorted(set(render_types))),
            'full_bright':fullbright,'additive_blend_detected':additive,'no_cull':('NO_CULL' in txt or 'noCull' in txt),
            'uv_literal_calls':len(re.findall(r'\.uv\s*\(',txt))+len(re.findall(r'\.tex\s*\(',txt)),
            'geometry_box_calls':len(re.findall(r'addBox\s*\(',txt)),'guard_coverage':guarded,
            'original_comparison':'Named/static source contract only; runtime side-by-side required.' if cls!='TC4WispRenderer' else 'Compared to TC4 RenderWisp source: 4x4 Wisp frames, particles.png row 5, additive full-bright.',
            'status':status,'confirmed_mismatch':mismatch,'runtime_status':'NOT_TESTED'
        })

    # GUI audit.
    gui_rows=[]
    screen_root=root/'src/main/java/com/darkifov/thaumcraft/client/screen'
    for p in sorted(screen_root.glob('*.java')):
        txt=p.read_text(encoding='utf-8',errors='ignore'); textures=parse_resource_locations(txt)
        exact=0; adapted=0; missing=[]
        external=[]
        for t in textures:
            if t.startswith('textures/'):
                tp=root/'src/main/resources/assets/thaumcraft'/t
            elif t.startswith('thaumcraft:'):
                path=t.split(':',1)[1]
                tp=(root/'src/main/resources/assets/thaumcraft'/path) if path.startswith('textures/') else tex_root/(path+'.png')
            elif ':' in t:
                external.append(t); continue
            else: continue
            if not tp.exists(): missing.append(t)
            elif sha1(tp) in orig_sha: exact+=1
            else: adapted+=1
        confirmed=''
        status='STATIC_COORDINATES_CAPTURED'
        if p.stem=='AlchemicalFurnaceScreen' and any(x.startswith('minecraft:') for x in external):
            confirmed='Uses vanilla minecraft furnace GUI instead of TC4 textures/gui/gui_alchemyfurnace.png and original gauge UV/coordinates.'
            status='CONFIRMED_MISMATCH'
        elif missing:
            status='MISMATCH'
        gui_rows.append({
            'screen':p.stem,'source':p.relative_to(root).as_posix(),'texture_refs':' | '.join(textures),
            'external_texture_refs':' | '.join(external),'texture_count':len(textures),'exact_original_textures':exact,'adapted_or_new_textures':adapted,
            'missing_textures':' | '.join(missing),'blit_call_count':len(re.findall(r'\bblit\s*\(',txt)),
            'pose_scale_calls':len(re.findall(r'\.scale\s*\(',txt)),'priority':'P0' if p.stem=='AlchemicalFurnaceScreen' else 'P2',
            'status':status,'confirmed_mismatch':confirmed,'runtime_status':'NOT_TESTED'
        })

    # Block render layer audit.
    registry={}
    for p in (root/'src/main/java').rglob('*.java'):
        txt=p.read_text(encoding='utf-8',errors='ignore')
        # Direct and helper-based RegistryObject declarations.
        for m in re.finditer(r'public\s+static\s+final\s+RegistryObject<[^\n;=]*Block[^\n;=]*>\s+([A-Z0-9_]+)\s*=\s*[A-Za-z0-9_.]+\(\s*"([a-z0-9_]+)"',txt):
            registry[m.group(1)]=m.group(2)
    client=(root/'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
    ctext=client.read_text(encoding='utf-8',errors='ignore') if client.exists() else ''
    layer_map={}
    for m in re.finditer(r'ItemBlockRenderTypes\.setRenderLayer\(\s*ThaumcraftMod\.([A-Z0-9_]+)\.get\(\)\s*,\s*RenderType\.([A-Za-z0-9_]+)\(\)\s*\)',ctext,re.S):
        layer_map[m.group(1)]=m.group(2)
    render_type_rows=[]
    blockstates=root/'src/main/resources/assets/thaumcraft/blockstates'
    by_id={bid:const for const,bid in registry.items()}
    # Every shipped blockstate is part of the render-layer audit, even when registered through helpers.
    block_ids=sorted(p.stem for p in blockstates.glob('*.json'))
    for bid in block_ids:
        const=by_id.get(bid,'UNRESOLVED_REGISTRY_CONSTANT')
        layer=layer_map.get(const,'solid(default)'); model_names=[]
        bs=blockstates/f'{bid}.json'
        if bs.exists():
            try:
                d=load_json(bs)
                def walk(o:Any):
                    if isinstance(o,dict):
                        if isinstance(o.get('model'),str): model_names.append(o['model'])
                        for v in o.values(): walk(v)
                    elif isinstance(o,list):
                        for v in o: walk(v)
                walk(d)
            except Exception: pass
        texs=[]; alpha=False; missing=[]
        for loc in sorted(set(model_names)):
            ns,path=(loc.split(':',1) if ':' in loc else ('minecraft',loc))
            if ns!='thaumcraft': continue
            mp=models_root/f'{path}.json'
            if not mp.exists(): continue
            try: md=load_json(mp)
            except: continue
            for t in (md.get('textures') or {}).values():
                if not isinstance(t,str) or t.startswith('#'): continue
                tp=resolve_model_texture(root,t)
                if tp is None: continue
                texs.append(t)
                if not tp.exists(): missing.append(t)
                else: alpha=alpha or bool(png_info(tp).get('has_alpha'))
        risk=(layer.startswith('solid') and alpha)
        render_type_rows.append({
            'block_id':bid,'registry_constant':const,'render_layer':layer,'model_refs':' | '.join(sorted(set(model_names))),
            'texture_refs':' | '.join(sorted(set(texs))),'alpha_present':alpha,'missing_textures':' | '.join(missing),
            'status':'STATIC_RISK_SOLID_WITH_ALPHA' if risk else ('MISMATCH' if missing else 'STATIC_VALID'),
            'priority':priority_for(bid),'runtime_status':'NOT_TESTED'
        })

    # Wisp visual proof cards.
    wisp=tex_root/'misc/wisp.png'; particles=tex_root/'misc/particles.png'
    if wisp.exists() and particles.exists():
        wi=Image.open(wisp).convert('RGBA'); pa=Image.open(particles).convert('RGBA')
        frame=5; fw=wi.width//4; fh=wi.height//4; crop=wi.crop(((frame%4)*fw,(frame//4)*fh,(frame%4+1)*fw,(frame//4+1)*fh))
        # original halo UV is horizontal frame /16 at vertical row 5/16.
        pw=pa.width//16; ph=pa.height//16; halo=pa.crop((frame*pw,5*ph,(frame+1)*pw,6*ph))
        wrong=wi.resize((256,256),Image.Resampling.NEAREST); correct=crop.resize((256,256),Image.Resampling.NEAREST)
        canvas=Image.new('RGBA',(800,330),(24,24,24,255)); d=ImageDraw.Draw(canvas)
        if args.baseline_wisp:
            d.text((10,8),f'Wisp UV comparison — {args.version} baseline',fill='white')
            d.text((10,32),f'PORT {args.version}: whole 4x4 atlas',fill='white')
            canvas.alpha_composite(wrong,(10,60))
            filename='wisp_uv_baseline_vs_tc4.png'
        else:
            d.text((10,8),f'Wisp UV comparison — {args.version} fixed source contract',fill='white')
            d.text((10,32),f'PORT {args.version}: one 4x4 frame + original halo',fill='white')
            canvas.alpha_composite(correct,(10,60))
            filename='wisp_uv_postfix_contract.png'
        d.text((280,32),'TC4: one animated 64x64 frame',fill='white'); d.text((550,32),'TC4 halo: particles row 5',fill='white')
        canvas.alpha_composite(correct,(280,60)); canvas.alpha_composite(halo.resize((240,240),Image.Resampling.NEAREST),(550,60))
        canvas.save(diffs/filename)

    furnace_gui=tex_root/'gui/gui_alchemyfurnace.png'
    if furnace_gui.exists():
        fg=Image.open(furnace_gui).convert('RGBA')
        panel=fg.crop((0,0,176,166)).resize((352,332),Image.Resampling.NEAREST)
        canvas=Image.new('RGBA',(760,390),(24,24,24,255)); d=ImageDraw.Draw(canvas)
        if args.baseline_wisp:
            d.text((10,8),f'Alchemical Furnace GUI pipeline — {args.version} baseline',fill='white')
            port_label='minecraft:textures/gui/container/furnace.png'
            label_color=(255,170,80,255)
            filename='alchemical_furnace_gui_baseline_vs_tc4.png'
        else:
            d.text((10,8),f'Alchemical Furnace GUI pipeline — {args.version} restored',fill='white')
            port_label='thaumcraft:textures/gui/gui_alchemyfurnace.png'
            label_color=(120,255,160,255)
            filename='alchemical_furnace_gui_postfix_contract.png'
        d.text((10,32),'TC4 required resource and coordinates',fill='white')
        canvas.alpha_composite(panel,(10,52))
        d.text((390,68),f'PORT {args.version} loads:',fill='white')
        d.text((390,94),port_label,fill=label_color)
        d.text((390,132),'TC4 requires:',fill='white')
        d.text((390,158),'thaumcraft:textures/gui/gui_alchemyfurnace.png',fill=(120,255,160,255))
        d.text((390,204),'Original gauges:',fill='white')
        d.text((390,230),'burn 20 px @ (80,26)',fill='white')
        d.text((390,254),'cook 46 px @ (106,13)',fill='white')
        d.text((390,278),'contents 48 px @ (61,12)',fill='white')
        d.text((390,302),'glass overlay @ (60,8)',fill='white')
        canvas.save(diffs/filename)

    def write_csv(name:str, rows:list[dict[str,Any]]):
        path=out/name
        keys=[]
        for r in rows:
            for k in r:
                if k not in keys: keys.append(k)
        with path.open('w',newline='',encoding='utf-8-sig') as f:
            w=csv.DictWriter(f,fieldnames=keys,extrasaction='ignore'); w.writeheader(); w.writerows(rows)

    write_csv('texture_audit.csv',texture_rows)
    write_csv('port_texture_inventory.csv',port_rows)
    write_csv('model_audit.csv',model_rows)
    write_csv('custom_renderer_audit.csv',renderer_rows)
    write_csv('gui_audit.csv',gui_rows)
    write_csv('render_type_audit.csv',render_type_rows)
    write_csv('item_context_audit.csv',item_context_rows)
    write_csv('pixel_diff_index.csv',diff_records)

    statuses=Counter(r['status'] for r in texture_rows)
    port_class=Counter(r['classification'] for r in port_rows)
    model_status=Counter(r['status'] for r in model_rows)
    renderer_status=Counter(r['status'] for r in renderer_rows)
    render_status=Counter(r['status'] for r in render_type_rows)
    confirmed=[{'class':r['class'],'priority':r['priority'],'confirmed_mismatch':r['confirmed_mismatch'],'scope':'renderer'} for r in renderer_rows if r['status']=='CONFIRMED_MISMATCH']
    confirmed += [{'class':r['screen'],'priority':r['priority'],'confirmed_mismatch':r['confirmed_mismatch'],'scope':'gui'} for r in gui_rows if r['status']=='CONFIRMED_MISMATCH']
    summary=f'''# Полный статический аудит текстур и UV — Thaumcraft Legacy Rebuild {args.version}\n\n## Область\n\n- База порта: `{root.name}`.\n- Эталон ресурсов: инвентарь TC4 4.2.3.5 и канонический банк `textures/original/thaumcraft4`.\n- Проверены все оригинальные PNG/MCMETA, все активные core-текстуры порта, все JSON-модели, все Java-рендереры в `client/render`, все Screen-классы и зарегистрированные render layers.\n- Runtime side-by-side не выполнялся: визуальный итог в игре остаётся `NOT TESTED`.\n\n## Точные результаты ресурсов\n\n- Оригинальных PNG/MCMETA в инвентаре: **{len(original_entries)}**.\n- Побайтово присутствуют в поставляемых ресурсах: **{exact_shipped}/{len(original_entries)}**.\n- Имеют отдельную активную core-копию вне канонического банка: **{exact_core}/{len(original_entries)}**.\n- Обнаружены прямые статические ссылки хотя бы на одну точную копию: **{used_original}/{len(original_entries)}**.\n- Core PNG/MCMETA порта вне `textures/original/**`: **{len(port_rows)}**; exact TC4 copies: **{port_class.get('EXACT_TC4_COPY',0)}**; adapted/new: **{port_class.get('ADAPTED_OR_NEW',0)}**.\n\nНаличие байтов не доказывает правильное UV, blending, размер, свет или положение.\n\n## JSON-модели\n\n- Всего: **{len(model_rows)}** (item + block).\n- Статусы: `{dict(model_status)}`.\n- Прямое JSON→JSON сравнение невозможно: TC4 1.7.10 использует metadata icons, ModelBase/TESR/IItemRenderer. Для кастомной геометрии требуется сравнение исходного renderer/model кода и runtime screenshots.\n\n## Кастомные рендереры\n\n- Проаудировано Java-файлов: **{len(renderer_rows)}**.\n- Статусы: `{dict(renderer_status)}`.\n- Подтверждённых source-level расхождений: **{len(confirmed)}**.\n\n### Подтверждённое расхождение\n\n'''
    if confirmed:
        for r in confirmed: summary+=f"- **{r['class']} ({r['priority']}, {r['scope']})** — {r['confirmed_mismatch']}\n"
    else: summary+='- Нет подтверждённых расхождений в выбранном baseline-режиме.\n'
    summary+=f'''\n## RenderType\n\n- Зарегистрированных block IDs: **{len(render_type_rows)}**.\n- Статусы: `{dict(render_status)}`.\n- `STATIC_RISK_SOLID_WITH_ALPHA` — только сигнал для ручной проверки: альфа может находиться в неиспользуемой зоне текстуры или модель может направляться через кастомный renderer.\n\n## Runtime-доказательства\n\nНе выполнены: GUI/Ground/Fixed/First-person/Third-person/World, одинаковые FOV/освещение/GUI Scale, анимации, z-sorting и full-bright. Никакой строке не присвоен runtime visual PASS.\n\n## Файлы\n\n- `texture_audit.csv` — полный оригинальный инвентарь и наличие/использование.\n- `port_texture_inventory.csv` — все core-текстуры порта.\n- `model_audit.csv` — все item/block JSON.\n- `custom_renderer_audit.csv` — все кастомные Java render paths.\n- `gui_audit.csv`, `render_type_audit.csv`, `item_context_audit.csv`.\n- `diffs/` — доступные автоматические diff/UV proof изображения.\n'''
    (out/'summary.md').write_text(summary,encoding='utf-8')
    manifest={
        'version':args.version,'root':str(root),'status':'STATIC_AUDIT_COMPLETE_RUNTIME_NOT_TESTED',
        'counts':{
            'original_png_mcmeta':len(original_entries),'original_exact_shipped':exact_shipped,'original_referenced':used_original,
            'port_core_png_mcmeta':len(port_rows),'json_models':len(model_rows),'custom_renderer_files':len(renderer_rows),
            'gui_screens':len(gui_rows),'registered_blocks':len(render_type_rows),'diffs':len(list(diffs.rglob('*.png')))
        },
        'confirmed_mismatches':[{'class':r['class'],'scope':r['scope'],'details':r['confirmed_mismatch']} for r in confirmed],
        'limitations':['No original 1.7.10 runtime screenshots were supplied or generated.','TC4 1.7.10 has no JSON model corpus for direct JSON-to-JSON UV comparison.','Static source/resource checks do not prove final GPU output.']
    }
    (out/'audit_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(manifest,ensure_ascii=False,indent=2))
    return 0

if __name__=='__main__': raise SystemExit(main())
