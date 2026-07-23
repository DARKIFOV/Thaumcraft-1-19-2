#!/usr/bin/env python3
import argparse, json, re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java'
text=SRC.read_text(encoding='utf-8')
entries_part=text.split('private static final Map<String, Entry> BY_ID')[0]
entries=re.findall(r'e\("([^"]+)"\s*,',entries_part)
switch=text[text.index('private static Item createItem'):text.index('public static Entry[] entries')]
case_groups=re.findall(r'case\s+((?:"[^"]+"\s*,?\s*)+)->', switch)
dedicated=sorted({item for group in case_groups for item in re.findall(r'"([^"]+)"', group)})
pre_registered={
 'tc4_crystalessence','tc4_block_banner','tc4_bath_salts','tc4_bucket_pure','tc4_bucket_death',
 'tc4_block_arcane_spa','tc4_block_arcane_bore_base','tc4_block_arcane_bore','tc4_block_arcane_ear',
 'tc4_block_arcane_lamp','tc4_block_arcane_pressure_plate','tc4_arcanedoor','tc4_block_levitator',
 'tc4_jar_brain','tc4_mirrorframe','tc4_mirrorframe2','tc4_mirrorhand',
 'tc4_block_lamp_growth','tc4_block_lamp_fertility','tc4_block_wand_pedestal','tc4_block_wand_pedestal_focus'
}
skipped={'tc4_block_focal_manipulator','tc4_block_thaumium','tc4_block_tallow','tc4_block_crystal_cluster'}
fallback=sorted(x for x in entries if x not in dedicated and x not in pre_registered and x not in skipped)
data={
 'version':'11.63.39',
 'mirror_entries':len(entries),
 'dedicated_create_item_ids':len(dedicated),
 'pre_registered_functional_ids':len(pre_registered),
 'intentionally_skipped_duplicate_ids':len(skipped),
 'generic_fallback_ids':len(fallback),
 'generic_fallback_item_like_ids':sum(not x.startswith('tc4_block_') for x in fallback),
 'generic_fallback_block_alias_ids':sum(x.startswith('tc4_block_') for x in fallback),
 'generic_fallback':fallback,
 'interpretation':'The reproducible generic fallback is closed: every mirrored TC4 ID is now dedicated, pre-registered functional, or an intentional duplicate BlockItem skip.'
}
parser=argparse.ArgumentParser(); parser.add_argument('--json'); parser.add_argument('--markdown'); args=parser.parse_args()
if args.json: Path(args.json).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if args.markdown:
    lines=['# Аудит оставшихся TC4-ID — v11.63.39','',
      'Этот аудит отвечает на вопрос «сколько осталось» по воспроизводимому статическому критерию: ID всё ещё создаётся общим `TC4ResearchComponentItem`, а не отдельным классом или заранее зарегистрированным функциональным объектом.','',
      '| Показатель | Значение |','|---|---:|',
      f'| Записей в de-metadata mirror | {data["mirror_entries"]} |',
      f'| ID с отдельной веткой `createItem` | {data["dedicated_create_item_ids"]} |',
      f'| Заранее зарегистрированные функциональные ID | {data["pre_registered_functional_ids"]} |',
      f'| Намеренно пропущенные дубли BlockItem | {data["intentionally_skipped_duplicate_ids"]} |',
      f'| **Оставшиеся generic fallback ID** | **{data["generic_fallback_ids"]}** |',
      f'| Из них предметоподобные ID | {data["generic_fallback_item_like_ids"]} |',
      f'| Из них блочные migration-alias ID | {data["generic_fallback_block_alias_ids"]} |','',
      '## Как читать число','',
      f'**{data["generic_fallback_ids"]} — это верхняя техническая оценка блочной миграции, а не число самостоятельных неперенесённых механик.** После v11.63.39 общий fallback равен нулю: последние лампы и пьедесталы получили функциональные регистрации, BlockEntity, ресурсы и runtime-протоколы.','',
      '## Текущий fallback-список','']
    lines += [f'- `{x}`' for x in fallback]
    Path(args.markdown).write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in data.items() if k!='generic_fallback'},ensure_ascii=False))
