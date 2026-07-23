# v11.64.35 — Essentia Jar proof boundary

- SOURCE CLOSED: YES. RESOURCE CLOSED: YES для обычной Essentia Jar, Void Jar, labels, phial-transfer, suction, tile/item NBT и rendering.
- BUILD VERIFIED: NO; RUNTIME VERIFIED: NO. Gradle Wrapper не разрешает `services.gradle.org`, активна OpenJDK 21 вместо требуемой Java 17.
- `filtered_essentia_jar` намеренно сохранён только как migration alias ранних версий порта. Он скрыт из creative/JEI, не имеет рецепта и преобразуется в обычную банку с сохранённой фильтрацией.
- Трубы, клапаны, buffers и mirrors не объявляются закрытыми этим релизом; проверены только интерфейсы взаимодействия с банкой.
- GameTest/source guards не заменяют реальную проверку длинных сетей, одновременных вставок, chunk unload/reload, multiplayer sync и client rendering.

# v11.64.27 — Research System proof boundary

- SOURCE CLOSED: YES. RESOURCE CLOSED: YES для Research Table, aspect knowledge/pool, оригинальных исследований, записок, покупки, копирования и изучения.
- BUILD VERIFIED: NO; RUNTIME VERIFIED: NO. Gradle Wrapper не разрешает `services.gradle.org`, активна Java 21 вместо JDK 17.
- `javac --release 17`, source guards и GameTest-контракты не заменяют Forge client/dedicated/multiplayer runtime.
- Обычный TC4 прогресс намеренно ограничен 201 оригинальным исследованием; addon/rebuild entries остаются отдельными и не подмешиваются в оригинальный контур.
- Старый второй aspect wallet мигрируется через max, а не sum, чтобы не создавать дублированные очки исследований.

# v11.64.26 — Thaumonomicon proof boundary

- Core item/browser/page/research/transformation source paths and original resources are closed.
- BUILD VERIFIED and RUNTIME VERIFIED remain NO: Wrapper cannot resolve `services.gradle.org`, active runtime is Java 21 rather than required Java 17.
- Wizard-tower generation is absent, so the exact tower chest entry (count 1, weight 20) is not active.
- An exact modern adapter for the seven legacy ChestGenHooks rare pools is absent; approximate loot-table injection is forbidden because it would change the original weight denominator.
- These two acquisition paths are retained as explicit exact contracts in `TC4ThaumonomiconLootParity`, not misreported as runtime parity.

# v11.64.23 — Arcane Spa full closure

- SOURCE CLOSED: YES. RESOURCE CLOSED: YES.
- BUILD VERIFIED: NO до фактического `BUILD SUCCESSFUL`; RUNTIME VERIFIED: NO до клиента/dedicated/GameTest.
- Новые NBT совместимы с оригинальным `TileSpa`; ранние портовые `Mix/Tank/Salts` читаются миграционно.
- Намеренно сохранён редкий оригинальный контракт потери 1000 mB/соли, если размещение было отменено после выбора допустимой позиции: расход идёт перед `setBlock`.
- Наполненная ёмкость при полном/несовместимом tank поглощает interaction и не открывает GUI, но не расходуется.
- Внутренняя Bath Salts/Purifying Fluid/Warp Ward механика не переоткрывалась; она остаётся доказательством v11.64.14.

# v11.64.18 — Arcane Bellows runtime proof boundary

- SOURCE CLOSED и RESOURCE CLOSED для Arcane Bellows и его реализованных потребителей.
- BUILD VERIFIED и RUNTIME VERIFIED остаются неподтверждёнными до успешного Gradle/Forge запуска.
- Оригинальная Infernal Furnace в текущем порте отсутствует как полностью закрытая система; точные bellows scan/cook/bonus формулы сохранены контрактом, но не подменены приблизительной машиной.
- Универсальный промт остаётся обязательным в корне полного архива.

# v11.64.15 — Brain in a Jar runtime proof boundary

- Source and resource closure are complete for the selected object.
- A successful Gradle build and JAR are unavailable because the Wrapper cannot resolve `services.gradle.org`.
- The exact model/particle frame, real ambient sound schedule, dedicated-server synchronization, multiplayer interaction and all 152 GameTests remain runtime `NOT VERIFIED` until infrastructure permits a Forge run.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` must remain in the root of every full source archive.

# v11.64.14 Bath Salts — открытые границы

- Gradle/Forge build не получен: Wrapper не разрешает `services.gradle.org`; JAR отсутствует.
- Реальный client/dedicated-server/multiplayer/GameTest runtime для Bath Salts не запускался.
- Основная item/fluid/ward/bucket/spa/recipe/research механика закрыта на source/resource/static уровне.
- Оригинальный wizard trade (5–7 emeralds → 1 Bath Salts) закреплён как точный контракт, но его production activation зависит от отдельного полного порта custom wizard villager и thaumaturge village tower. Неточная замена ванильным profession запрещена.

# v11.64.13 — Unnatural Hunger full-closure proof boundary

- Все найденные source/resource пути оригинальной механики закрыты единым production-контрактом: эффект, два warp-пути, тики истощения, special/ordinary food finish, curatives, сообщения, точная иконка и точный post chain.
- Статически подтверждены lifecycle клиента: создание отдельного `PostChain`, resize, обработка до полного HUD, возврат main framebuffer, закрытие при снятии эффекта и инвалидирование при resource reload.
- Это не заменяет реальный клиентский прогон. В Forge runtime ещё нужно зафиксировать: два пути получения эффекта, 5000/6000 тиков, уровни 0–3, hunger bar, rotten flesh/brain, оба сообщения, снятие/понижение, save/reload, resource reload, изменение разрешения и совместимость с другими post-effects.
- Multiplayer-синхронизация, latency/reconnect и полный запуск 146 GameTest также не выполнены.
- Полный build/JAR отсутствует: Gradle Wrapper не может разрешить `services.gradle.org`. Поэтому статус механики: **SOURCE/RESOURCE/STATIC CLOSED; FORGE RUNTIME NOT VERIFIED**.

# v11.64.12 — Unnatural Hunger food interaction proof boundary

- Source теперь обрабатывает завершение еды: rotten flesh/brain уменьшают duration/amplifier, обычная еда показывает оригинальное предупреждение без отмены nutrition.
- GameTest и pure-Java self-test проверяют только точную арифметику 600 тиков / один amplifier и строгий survival gate.
- Реальное событие Forge, порядок с `ZombieBrainItem.finishUsingItem`, hunger bar, оба чат-сообщения, curative metadata, клиент и multiplayer не запускались.
- Полный build/JAR отсутствует: Gradle Wrapper не может разрешить `services.gradle.org`.

# v11.64.11 — Sun Scorned runtime proof boundary

- Source теперь использует brightness-table значение сущности вместо линейного деления raw light на 15 и сохраняет оригинальные строгие пороги/случайные неравенства.
- GameTest и pure-Java self-test проверяют границы 0.5/0.25, sky admission и равенство случайного броска.
- Реальные сутки/погода/измерения, block light, клиентский значок эффекта, поджог и лечение в Forge runtime не запускались.
- Полный build/JAR отсутствует: Gradle Wrapper не может разрешить `services.gradle.org`.

# v11.64.10 — Death Gaze runtime proof boundary

- Source теперь воспроизводит оригинальную геометрию `EntityUtils.isVisibleTo(0.75F)`: половинная апертура для cosine-проверки, центр тела цели и axial round cap.
- GameTest и pure-Java self-test покрывают старый слишком широкий конус, точку за пределами сферы, но до круглой торцевой плоскости, строгую границу `< range` и цель позади игрока.
- Реальный выбор мобов, `isPickable`, line-of-sight, PVP, агр и Wither в Forge runtime не запускались.
- Полный build/JAR отсутствует: Gradle Wrapper не может разрешить `services.gradle.org`.

# v11.64.09 — Warp research pool sync proof boundary

- Source подтверждает, что оба `grantResearch` теперь изменяют server pool и затем вызывают полный `syncAspectKnowledge`.
- Сетевой адаптер намеренно агрегирует оригинальные per-point `PacketAspectPool` в один снимок пула после серии; конечное состояние клиента должно совпадать.
- Реальный клиент, Research Table, Thaumonomicon, latency/reconnect и двухклиентная синхронизация не запускались.
- Targeted `javac --release 17` self-test относится только к новому helper и не является Forge build.
- Полный build заблокирован отсутствием Gradle 7.5.1 в кеше и недоступностью `services.gradle.org`; JAR отсутствует.

# v11.64.08 — Eldritch milestone proof boundary

- Source now grants research silently at the ELDRITCHMINOR/ELDRITCHMAJOR warp milestones, matching TC4; the previous fabricated chat literals are gone.
- grantResearch mirrors the original algorithm statically, but the actual random aspect-pool distribution has NOT run under Forge.
- No javac and no Gradle network access in the sandbox: JAR not built, GameTest server not run, in-game chat/absence-of-chat not observed.

# v11.64.05 — Warp spawn collision runtime proof boundary

- Source now matches TC4 entity-aware admission instead of requiring a fixed two-block air column.
- The new GameTests prove the intended source contract and real entity dimensions, but have not run under Forge.
- Actual spawn frequency across terrain, unloaded chunks, liquids, multiplayer and mob-cap conditions remains NOT VERIFIED.
- The corrected v11.64.04 tri-state `-1/0/+1` offset distribution remains mandatory.

# v11.64.04 — Warp spawn tri-state proof boundary

- Оригинальный TC4 намеренно допускает нулевое смещение отдельной оси: `random(7,24) * random(-1,1)`.
- Исправленная версия сохраняет `-1/0/+1`; удаление нуля через `nextBoolean()` считается регрессией и запрещено guard-ом.
- 137 GameTest и 465 manifest-сценариев существуют в исходниках, но не считаются runtime PASS.
- Целевая сборка Java 17, dedicated GameTest server, реальное распределение точек и multiplayer остаются NOT VERIFIED.

# v11.64.03 — Warp runtime proof boundary

- Source and static checks now distinguish the uncurable initial warp-event effect from milk-curable propagated Infectious Vis Exhaust instances.
- Sanity Soap creative consumption, sticky-cleansing chance bonuses and Purifying Fluid ward duration are source-linked and guarded.
- The five new GameTests remain `NOT_TESTED` under Forge until a Java 17 build is available. Client shader overlays, real milk interaction, multiplayer effect propagation and 2000-tick scheduler behavior still require runtime artifacts.

# v11.64.02 — Research completion warp runtime boundary

- Source behavior now matches the original permanent/sticky split for research completion warp.
- The three new GameTests are present but have not run under Forge because no Java 17 build/JAR was obtained.
- Configuration parity for TC4 `Config.wuss` is not claimed; the rebuild currently applies normal warp rules.
- Client HUD, warp-event timing after these grants and multiplayer synchronization still require runtime artifacts.

# v11.64.00 — Research Note clear runtime proof boundary

- Source and static checks now cover type-2-only clear admission, strict Expertise/Mastery refund boundaries, no creative refund bypass, one-ink survival cost and full NBT/ink/pool rollback.
- Six new GameTests use a real Research Table menu and note state, but the complete 122-test Forge server suite remains `NOT_TESTED` until Java 17 and Gradle dependencies are available.
- Client right-click/erase audio, two-client synchronization, packet replay under latency and save/reload during an in-flight edit remain separate runtime work.

# v11.63.99 — Research Note graph runtime proof boundary

- Placement no longer invents a compatible-neighbour admission rule; graph connectivity remains a completion-time check.
- Server packet context, unknown-aspect rejection, occupied-hex rejection and placement rollback are source-guarded.
- These contracts remain runtime `NOT_TESTED` without a successful Java 17 Forge GameTest run.

# v11.63.97 — Research Table behavioral proof boundary

- Real `ResearchTableBlockEntity` GameTests now cover bonus NBT cardinality, legacy duplicate normalization, consumed-bonus save/reload and the exact `nextRecalc++ > 600` server-tick boundary.
- A completed recalc pass always marks/synchronizes the tile so the reset counter can persist even when RNG adds no aspect.
- These GameTests are mandatory but remain `NOT_TESTED` until a Java 17 Forge build and `runGameTestServer` execute the full 107-test suite.
- Client GUI interaction, two-player menu synchronization, chunk unload during a research edit and probabilistic world-source generation remain separate runtime work.

# v11.63.95 — Thaumometer runtime proof boundary

- Source-linked timing and target contracts are complete: 25-tick use, 20 stable ticks, ten pre-completion camera sounds, entity-before-block priority and handheld known-target rejection.
- The three added GameTests are contract tests; they do not prove client renderer alignment, camera sound audibility, packet latency, two-player scan ledgers or behavior under target despawn/chunk unload.
- A Java 17 build plus dedicated GameTest and client runtime artifacts are still required before any runtime PASS.

## 11.63.61 Infusion Matrix pause/resume boundary

- Source parity is restored for structural interruption: the matrix deactivates without clearing the locked craft and may be reactivated with the wand after the multiblock is repaired.
- Post-start component layout is not a recipe-selection authority. Missing components wait; they do not cause a second recipe lookup.
- These are source and static contracts only for v11.63.61. The four new GameTests and the full 56-test suite remain `NOT_TESTED` until a Java 17 Forge build and `runGameTestServer` complete.
- Cross-chunk unload, multiplayer ownership, save/reload during pause and all weighted instability effects still require runtime proof.

## v11.63.58 — Alchemical processing / Thaumatorium proof boundary

Closed in source and mandatory GameTest contracts:
- the Alchemical Furnace scans at most four alembics above it and advances on its persisted local 40-tick phase, reduced to 20 ticks by bellows;
- alembic capacity, aspect filter, blocked faces and NBT are covered;
- the centrifuge accepts one compound aspect from below, pauses under redstone and exposes its component only upward;
- the Thaumatorium requires heat, suspends suction while powered, uses suction 128, gains two formula slots per Mnemonic Matrix, preserves inputs while output is blocked and clears its entire internal AspectList after a successful craft.

Still not proven for this exact version:
- no v11.63.58 JAR exists because Gradle distribution download fails before project configuration;
- all 44 required GameTests remain NOT_TESTED until a Java 17 Forge run;
- long-running furnace/alembic/tube automation, simultaneous Thaumatorium requests, chunk unload/reload and multiplayer synchronization remain separate runtime work.

## v11.63.56 — essentia tube transport proof boundary

- Normal, filtered, restricted, one-way and buffer tube behavior is source-guarded and represented by four new mandatory GameTests.
- The buffer rollback defect is fixed: restoration cannot exceed eight points and the single-type transport view is synchronized immediately with mixed storage.
- This does not prove long branched networks, equal-suction conflicts, venting, valves/bellows under redstone, chunk unload, simultaneous races or multiplayer FX.
- Static CI: 121/121 PASS. The exact v11.63.56 runtime baseline is 36/36 GameTests, currently `NOT_TESTED` because no Java 17 JAR was obtained.

# Known deviations

## v11.63.55 — Essentia storage/mirror runtime proof boundary

- Source contracts now cover filtered and void jar suction/capacity/overflow, mixed 256-point reservoir storage, and linked Essentia Mirror one-point drain, forward `[0,8)` search volume and rollback.
- These four tests are mandatory but remain `NOT_TESTED` until the current sources build on Java 17 and the Forge GameTest server reports 32/32 PASS.
- Cross-dimension mirrors, chunk-unload queues, simultaneous transfer races and long-duration 48-aspect tube-network tests remain open.

## v11.63.51 — Java 17 build and server GameTest evidence

Closed in runtime:
- the supplied v11.63.50 source was compiled with Java 17 after correcting four Forge 1.19.2 API mismatches;
- the first GameTest run exposed an invalid energized-node fixture state (`Energized=true`, `EnergizedTicks=0`), which made the node correctly de-energize before the delayed refill assertion;
- all three energized-node test fixtures now persist a valid cooldown, and the complete required suite passes **18/18**;
- the dedicated GameTest server loaded the Overworld and Outer Lands dimensions, installed Magical Forest in the active biome source and loaded all three custom recipe managers.

Still requiring evidence:
- this headless pass does not validate client rendering, GUI scaling, JEI, two-client networking or historical world-migration fixtures;
- those categories remain `NOT_TESTED` until their named artifacts are captured.

## v11.63.50 — final original recipe-ledger closure

Closed in source:
- the original TC4 runtime-registration ledger is **258/258 statically mapped**: 54 crucible, 104 arcane shaped, 5 arcane shapeless, 63 infusion, 24 infusion-enchantment and 8 furnace-smelting registrations;
- the final nine already-materialized crucible/arcane records now carry exact source contracts for result metadata, patterns, components and aspects;
- `AdvancedGolem` preserves the legacy wildcard catalyst (`itemGolemPlacer`, damage 32767) through eight registry-specific recipes and copies the catalyst stack before adding byte `advanced=1`;
- the incorrect first component in all eight modern `AdvancedGolem` JSON files was corrected from redstone to shears;
- two new server-only GameTests raise the required suite to 18 and the runtime manifest to 346 scenarios.

Remaining runtime evidence:
- the historical recipe denominator has no unresolved records;
- all 18 GameTests remain `NOT_TESTED` because the Gradle Wrapper cannot resolve `services.gradle.org` in this environment;
- the installed OpenJDK is 21.0.10, while the Forge 1.19.2 development build must be reproduced with Java 17 before a JAR or runtime PASS can be claimed.

## v11.63.49 — MCP/OreDictionary/NBT recipe correction closure

Closed in source:
- twenty additional original recipe records now have exact runtime paths, bringing the source ledger to 248/258;
- `JarBrain` uses a water bucket, `TravelTrunk` and `MirrorEssentia` use iron ingots, and `MirrorHand` uses stick/compass/map according to the MCP field table;
- the Ice Wand Rod uses ordinary ice, not packed ice;
- all three fortress masks now consume the original dye/material/skull/food components and retain integer `mask=0/1/2` output tags; Helm Goggles retains byte `goggles=1`;
- Essentia Crystallizer accepts `plankWood` and `ingotIron` through modern tags; Tube Restrict and Tube Oneway retain `stone` and `dyeBlue` substitution semantics;
- two server-only GameTests cover the corrected arcane/wand family and NBT/mirror/brain/trunk infusion family.

Remaining runtime evidence / unresolved records:
- all sixteen required GameTests remain `NOT_TESTED` until a Java 17 Forge build and `runGameTestServer` succeed;
- ten records remain deliberately unresolved: one crucible record, eight arcane records and the wildcard `AdvancedGolem` infusion record;
- `JarVoid` remains blaze powder and `HungryChest` remains an oak trapdoor after rechecking; neither was changed in this stage.

## v11.63.48 — OreDictionary/tag and wand-cap recipe closure

Closed in source:
- direct TC4 OreDictionary names for tin, silver and lead ores map to `forge:ores/*` recipe tags;
- copper, silver, thaumium and void-metal cap nuggets map to `forge:nuggets/*`, while the gold cap keeps its exact vanilla nugget input;
- Arcane Workbench catalyst parsing/matching now accepts the same tag sentinel used by ordinary arcane ingredients;
- all five base/inert cap recipes and all three charged cap infusion recipes are represented with original Vis costs, instability, components and essentia;
- three old furnace recipes for magical logs, cinnabar ore and amber ore are promoted into the exact ledger;
- the source recipe ledger now proves 228/258 records and leaves thirty entries unresolved rather than guessing metadata/NBT/OreDictionary mappings.

Remaining runtime evidence:
- compatibility with real third-party items supplying the Forge tags requires a Java 17 game run with at least one content mod;
- all fourteen required GameTests remain `NOT_TESTED` until `runGameTestServer` exits successfully;
- the thirty unresolved source records still require exact old registry, metadata, NBT or ore-dictionary evidence.

## 11.63.46 Flux Scrubber runtime notes

- Source parity now covers the autonomous Aer-powered cleanup loop, original radius/check budget, cleanup-charge conversion, four-unit Praecantatio buffer, sided essentia output, NBT synchronization and original Cap/Tip renderer.
- The prior manual mob-effect/crucible cleanser was deliberately removed because it was not `TileFluxScrubber` behavior.
- Static guards and one required GameTest do not prove scan performance, multi-consumer relay contention, tube rollback, old-world block-entity upgrade, multiplayer synchronization or GPU rendering.
- All ten required GameTests remain `NOT_TESTED` until a Java 17 Forge build and successful `runGameTestServer` log exist.

## v11.63.45 — Energized Vis relay network and relay GameTests

Closed in source:
- energized Aura Nodes derive a persistent primary-aspect output profile from the node's permanent aspects, modifiers and unstable-node rerolls, while all consumers drain only a per-tick transient copy;
- Vis Relay links use the original eight-block distance, line of sight and wildcard/matching primary attunement, with a bounded loaded-chunk graph instead of TC4's global weak-reference cache;
- relay attunement, selected parent, pulse aspect and pulse timestamp persist and synchronize through the relay `BlockEntity`;
- Vis Amulets first transfer up to five centivis per primary aspect into a held wand and then independently recharge themselves from a nearby relay every five ticks;
- the Arcane Workbench Charger now requests exactly five centivis per primary aspect per tick instead of the previous 500-centivis adaptation;
- relay beams use the original `beam1.png`, crossed additive quads and aspect-coloured consume pulses;
- three required server GameTests cover transient-pool refill, LoS/attunement routing plus NBT persistence, and exact five-centivis workbench charging.

Remaining runtime evidence / modern adapters:
- the bounded graph intentionally scans only loaded block entities and stops after 512 relays; dense-network performance and chunk load/unload rebuilds require a real server soak test;
- the Curios bridge remains optional and reflection-based; accessory-slot discovery and duplicate suppression require testing both with and without Curios;
- beam blending, Revealing visibility, pulse synchronization and frustum behavior require client/GPU evidence;
- all nine required GameTests remain `NOT_TESTED` until `runGameTestServer` completes successfully under a Java 17 Forge build.

## v11.63.44 — Required GameTest smoke suite

Closed in source:
- six required server-side Forge GameTests cover deterministic persistence/interactions for Essentia Jar, Wand Pedestal + Aura Node, Node Jar, Mana Pod, legacy ItemStack migration and Arcane Door master/upper-half access;
- tests are registered through `RegisterGameTestsEvent`, use namespace `thaumcraft`, and share a packaged empty 9x5x9 structure;
- CI invokes `runGameTestServer` only after the Forge build succeeds and preserves a dedicated GameTest log before failing the job.

Remaining runtime evidence:
- all six manifest entries remain `NOT_TESTED` until ForgeGradle runs under Java 17 and the GameTest server exits with code 0;
- this suite is intentionally server-only and does not prove client render, multiplayer latency, GUI or GPU behavior.

## v11.63.43 — Deferred world migration runtime proof

Closed in source:
- safe duplicate-ID migration now covers player and Ender Chest inventories, dropped items, living equipment, container entities, loaded block-entity `Container`/writable `IItemHandler` storage and bounded nested portable inventories;
- stack conversion rewrites the registry id in a complete serialized ItemStack so ordinary tag data and Forge capability payloads are retained;
- Arcane Pedestal storage, Thaumatorium catalysts and Infusion Matrix in-flight snapshots expose explicit migration hooks;
- chunk-load events only enqueue work, with at most two already-loaded chunks inspected per level tick through `getChunkNow`;
- per-dimension `SavedData` records schema 116343, processed chunks and cumulative changed-stack/block-entity counters.

Remaining runtime evidence / adapters:
- the allow-list deliberately does not convert passive resource IDs or other objects whose exact legacy registry identity remains part of recipe/save compatibility;
- third-party item handlers that reject `setStackInSlot` are left unchanged rather than risking a world-load crash;
- real old-world fixtures, nested capability persistence, server tick cost, restart idempotence, multiplayer synchronization and five-version migration remain `NOT TESTED` without a built Java 17 Forge client/server.

## v11.63.42 — Mana Pod runtime proof

Closed in source:
- Mana Beans plant only from the lower face of supported oak, spruce, Greatwood or Silverwood logs in a biome included in `thaumcraft:is_magical`;
- Mana Pods preserve ages 0–7, the 1/30 random-growth attempt, age-three neighbour inheritance and the original weighted compound-aspect crossing rule;
- natural Magical Forest decoration performs ten attempts starting at Y=64 and advances legacy age 2–6 pods once immediately;
- age-dependent drops preserve the block-entity aspect, and only mature age-7 pods expose that dynamic aspect to the Thaumometer;
- original stem and pod textures, colour interpolation, pulse scale and age-based light are represented by a static stem model plus a synchronized block-entity renderer.

Remaining runtime evidence / modern adapters:
- the 1.7.10 decorator could place up to eight blocks across chunk borders; the 1.19.2 adapter clamps its random walk to the currently populated chunk to avoid synchronous neighbour loading;
- `BiomeDictionary.Type.MAGICAL` is represented by the datapack tag `thaumcraft:is_magical`, currently containing the rebuilt Magical Forest and extensible by datapacks;
- random-tick distribution, seeded crossbreeding frequencies, chunk-generation density, renderer light/blend behaviour, multiplayer synchronization and save/reload remain `NOT TESTED` without a built Java 17 Forge client/server.

## 11.63.36 Simple item mechanics runtime notes

- Mana Bean eating, random effects and the 25% aspect-pool branch are restored. Planting beans as Mana Pods remains deferred until the Mana Pod block and tile behavior are ported.
- Vis Amulets preserve the original 2,500/25,000-centivis per-primal capacities and five-centivis transfer cadence into a held wand. Recharge from aura relays remains deferred with the missing relay infrastructure.
- Curios integration is optional: vanilla item tags classify rings, necklaces and belts, while the existing reflection/fallback adapter keeps the mod loadable without Curios.
- Cloth and Void overlay sprite IDs are retained as migration items that instantiate the same usable armor class as their base counterpart. This avoids inert save aliases but means both IDs can represent an equipable piece.
- `tc4_focus_reversal` has no registered runtime item in the recovered TC4 source; it remains an explicit passive legacy component rather than receiving invented casting behavior.
- Exact armor rendering, Curios slot behavior, random distributions, save/reload and multiplayer synchronization remain `NOT_TESTED` until a Forge 1.19.2 JAR is built with Java 17 and exercised in-game.

## 11.63.35 Research utilities and Sanity Checker runtime notes

- The ten legacy registry IDs now instantiate the same concrete 1.19.2 item classes as their canonical counterparts. This intentionally preserves `instanceof`-based research-table and focus-pouch compatibility without copying inventories or menus.
- The Sanity Checker HUD is client-only and reads the already synchronized three warp buckets. Exact GUI scaling, overlap with other HUD mods, hide-GUI behavior and two-client synchronization remain `NOT_TESTED`.
- The original decompiled renderer used cumulative height arguments for coloured slices; this port draws non-overlapping segments from the same 48-pixel gauge, preserving the intended visual partition rather than reproducing that rendering defect.
- No v11.63.35 JAR is claimed until the Gradle Wrapper runs under JDK 17 and completes a Forge build.

## 11.63.33 Essentia/Loot/Eldritch simple-family runtime notes

- `tc4_essence` is a concrete subclass of the rebuilt essentia phial so all existing jar, alembic, crucible, tube and golem `instanceof EssentiaPhialItem` paths accept the original registry ID.
- The original two-pass phial rendering is represented by a two-layer generated model and item-color handler. Exact animation phase, inventory tint and resource reload require client screenshots.
- The port keeps the established rebuilt in-air Eldritch Eye attunement adaptation while also restoring acceptance of the exact legacy eye at the altar. The Runed Tablet is now the exact lock key instead of relying solely on the flattened modern eye.
- The Primordial Pearl (`tc4_eldritch_object_3`) remains outside this simple stage because its original aura-node mutation, explosion and flux-spawn behavior require a dedicated runtime stage.
- Loot generation is source-guarded, but random distribution, entity spawning, inventory pickup, multiplayer duplication resistance and save/reload remain NOT TESTED.

## 11.63.32 Wand Component Families runtime notes

- Component recognition and numerical cap/rod contracts are statically checked, but Arcane Workbench assembly, vis discounts, rod regeneration and save/reload still require Minecraft runtime testing.
- Inert cap variants intentionally do not map through `WandComponentData.capFromComponent`; they remain infusion precursors as in TC4.
- No JAR is claimed unless Gradle Wrapper runs under JDK 17 and reports `BUILD SUCCESSFUL` with exit code 0.

## 11.63.31 Simple Consumables and Legacy Jar Label runtime notes

- The original food probabilities are represented through vanilla 1.19.2 `FoodProperties`; random outcomes, client effect display and exact multiplayer synchronization still require runtime proof.
- Zombie Brain uses the already ported permanent/sticky warp buckets. Its original 10% sticky / 90% permanent branch is server-authoritative, but statistical distribution is not claimed as runtime-tested.
- Knowledge Fragment updates the modern aspect pool and sends the existing sync packet; two-client GUI refresh remains `NOT_TESTED`.
- The legacy `tc4_label` registry ID and the canonical `jar_label` intentionally coexist for save migration. Both are functional `JarLabelItem` instances; recipes produce the canonical item while accepting either input.
- JDK 17 and an accessible Gradle 7.5.1 distribution are still required before compile or runtime claims can be made.

## 11.63.30 Passive Crafting Components runtime notes

- These six entries are passive recipe components in TC4. No invented right-click or combat behavior was added.
- `tc4_golem_upgrade_empty` is the metadata-era blank icon that was not listed in the original creative variants; it is retained as a stack-64 uncommon crafting base with a translated explanatory tooltip.
- Tags are additive compatibility surfaces; the exact legacy IDs remain authoritative in preserved recipes and save migration.
- Recipe viewers, data-pack tag use, inventory persistence, multiplayer exchange and dedicated-server class loading remain `NOT_TESTED` without a successful Java 17 Wrapper build.
- The v11.63.29 input archive omitted the six Magical Forest/worldgen classes. They are restored byte-for-byte from v11.63.28; this packaging repair is statically verified but still needs a real Java 17 build.

## 11.63.27 Arcane Pressure Plate and keys runtime notes

- TC4 stored access by player name. The port stores UUIDs, preserving authorization across name changes.
- Bound keys also record the dimension in addition to coordinates. This deliberately prevents a key from authorizing an unrelated plate at identical coordinates in another dimension.
- Arcane Door support remains deferred until the separate Arcane Door stage; iron and gold keys are fully functional for Arcane Pressure Plates in this stage.
- The original ward-removal-by-wand interaction is represented by owner/creative break permission. Non-owner break, piston movement and entity destruction are blocked.
- Actual entity filtering, modded entities, multiplayer key transfer, redstone edge timing and save/reload remain `NOT_TESTED` until a Java 17 Wrapper build and in-game run are available.

## 11.63.26 Arcane Ear runtime notes

- TC4 buffered NoteBlock events for one server tick. The port dispatches the Forge `NoteBlockEvent.Play` directly to loaded ears on the same server thread, preserving note/instrument/radius matching without world polling.
- The old silent block-event ID `-1` is represented by ID `5`, because modern clientbound block-event IDs are byte-oriented; IDs `0..4` remain the five audible instruments.
- The ten-tick pulse is persisted to avoid a POWERED blockstate becoming stuck across chunk save/reload; TC4 persisted only note and tone.
- Modern extra note-block instruments are intentionally ignored because TC4 Arcane Ear supports only harp, bass drum, snare, hat and bass.
- Real client sound/particle timing, redstone edge behavior, chunk unload cleanup and interaction with event-canceling mods remain `NOT_TESTED` without a successful JDK 17 Gradle build.

## 11.63.23 Arcane Levitator runtime notes

- TC4 used a client-proxy global Shift query inside `TileLifter`; the port uses the affected entity's synchronized `isShiftKeyDown()` state, which matches the documented gameplay intent and works on dedicated servers.
- The original opaque-cube obstruction test is represented by `BlockState.isSolidRender`, preserving full-block stops while allowing transparent/non-solid modern states through the lift column.
- The original sparkle packet is represented by vanilla enchant particles. The retained `animatedglow.png` is used directly for the two original tinted overlay volumes.
- Runtime behavior, prediction, anti-cheat interaction, unusual modded entity pushability and chunk-boundary stacks are not claimed as tested without a compiled JAR.

## v11.63.15 — elemental tool family runtime proof

Static source/resource checks cover the reconstructed Hoe of Growth, Pickaxe of the Core, Shovel of the Earthmover and the new Sword of the Zephyr. They do not constitute in-game proof. The following still require a Java 17 Forge build and real client/dedicated-server runs:

- wide-arc combat under canceled attack events, enchantments, cooldowns and PVP rules;
- wind-sphere projectile deflection, entity interpolation, fall mitigation and anti-cheat interaction;
- Earthmover 3×3 placement with unusual modded BlockState invariants and protection mods;
- Pickaxe newborn-drop timing with delayed/custom loot systems;
- multiplayer-local previews and save/reload of all tool NBT.

The TC4 smoke spirals are represented by modern dust/smoke particles. The old float-counter reset has no direct public Forge 1.19.2 equivalent; normal server movement synchronization is retained.

## v11.63.11 — Axe of the Stream runtime proof

Closed in source:
- `tc4_elementalaxe` now instantiates a dedicated elemental `AxeItem` instead of the generic research-component placeholder;
- the elemental tier matches TC4 level 3, 1500 uses, speed 10, damage bonus 3 and enchantability 18, with thaumium repair and rare rarity;
- right-click use restores the ten-block item pull, 0.3 acceleration, ±0.35 per-axis cap and exclusion of Following Items that still have an active target;
- non-sneaking log breaks use the same-block greedy farthest search and route the actual harvest through `ServerPlayerGameMode.destroyBlock`, then convert newborn drops into bubble-type Following Items;
- sneaking bypass, bubble sound, blue particles and delayed neighbour ticks are represented.

Still requiring runtime evidence / known adapters:
- modern block tags replace the old `logWood` ore-dictionary test; modded logs must opt into `minecraft:logs` to participate;
- the old recursive search order is transcribed, but protection mods, event cancellation, unusual block entities and merged item drops need dedicated-server proof;
- TC4's exact `PacketFXBlockBubble` sprite is represented with a blue dust stream and bubble Following Item particles;
- tree selection, durability, pickup races, save/reload and two-client motion remain `NOT TESTED`.

## v11.63.10 — Special/Permanent/Following item runtime proof

Closed in source:
- all three TC4 item-entity registrations now have Forge 1.19.2 `EntityType` factories, spawn packets, item renderers and language keys;
- Special Item restores the original 0.25 body, launch motion, upward damping, gravity cancellation and explosion immunity, and is used by the Thaumonomicon transformation, Crucible output and boss-special-drop paths;
- Permanent Item continuously renews vanilla lifetime and is used by the Outer Lands key-room object;
- Following Item synchronizes its target id and particle type, saves the target UUID/position, follows through blocks for the original 20-step curve and returns to normal item collision/gravity after arrival.

Still requiring runtime evidence / known adapters:
- the original `RenderSpecialItem` additive lightning fan is not reproduced exactly; modern item rendering is used and Following Item sparkles are approximated with dust/bubble particles;
- original Following Item production came from `BlockUtils.harvestBlock` paths used by tools such as the Elemental Axe. That call-site is now restored by v11.63.11; Equal Trade overflow remains an additional exercised integration adapter;
- entity merging, pickup races, boss explosion survival, permanent-item save/reload and two-client target synchronization remain `NOT TESTED`.

- Multiplayer spawn-state is now serialized with `IEntityAdditionalSpawnData`; runtime client verification is still required.
- Crucible output entities are explicitly excluded from crucible input scans, as in the original `BlockMetalDevice`.

## v11.63.09 — Falling Taint runtime proof

Closed in source:
- taint crust no longer delegates collapse to vanilla `FallingBlockEntity`; a registered TC4-specific entity carries the complete block state and remembers the old source column;
- direct and lateral falls remove the source on the first server tick, then apply the original gravity, drag, landing damping, placement and 600-tick expiry contracts;
- the client renderer uses the block atlas with full-bright lighting, while NBT and Forge spawn synchronization preserve the carried state and source coordinates.

Still requiring runtime evidence:
- TC4 Flux Goo metadata levels `0–7` do not exist in the current single-state modern goo block, so any Flux Goo below the entity is treated as the original thick (`metadata >= 4`) terminal layer;
- the old `World#checkChunksExist` instant-fall fallback is not reproduced because random ticks already execute in loaded server chunks; dedicated-server chunk-boundary behaviour still needs evidence;
- landing placement, save/reload during a fall, particle timing and multiplayer interpolation remain `NOT TESTED`.

## v11.63.09 — Golem fishing bobber runtime proof

Closed in source:
- the fishing core no longer grants loot directly after an invisible cooldown; it casts a real, tracked `EntityGolemBobber` adapter;
- the original 300–499 tick attempt timeout, quality + strength bite formula, five-slice buoyancy, water collision damping, splash cues and 4000-tick lifetime are represented;
- Air/Fire upgrade handling, weighted 1.7.10 loot categories, reel trajectory, save/reload ownership and cleanup on task cancellation are represented;
- the client renderer supplies the half-scale bobber and sixteen-segment black line.

Still requiring runtime evidence:
- the old Minecraft 1.7.10 particle-atlas cell is represented by the modern fishing-hook sprite because that vanilla atlas no longer exists in 1.19.2;
- random durability/enchantment decoration on junk/treasure items is not yet byte-for-byte equivalent to `WeightedRandomFishable`;
- dedicated-server ownership recovery, line interpolation, cast/reel arm animation and multi-hour autonomous fishing remain `NOT TESTED`.

# Known deviations from Thaumcraft 4.2.3.5

This file records intentional or currently unavoidable differences. An entry is
not a waiver for release readiness; each deviation must remain visible in
`TC4_PORT_STATUS_V3.md` until accepted or removed.

## Active deviations

### 11.63.20 Tallow Candle dynamic-render runtime notes

- **Original:** metadata selected one of 16 tint colours; world geometry used `Random(x + y * z)` for 1–5 wax drips, while inventory geometry contained no drips.
- **Forge port:** metadata colours remain separate registry blocks with byte-retained textures; a lightweight render-only block entity supplies coordinates to the BER and all items share one BEWLR.
- **Remaining deviations:** the 1.7.10 fixed-function face lighting/tint path is represented by pre-coloured atlas sprites and modern cutout vertices; item transform, culling, shader/resource-pack behavior and multiplayer rendering require runtime evidence.
- **Release impact:** candle source parity is complete, but visual/runtime status remains `NOT TESTED`.


### Primal Arrow and golem dart runtime proof

- **Original:** six metadata variants used an arrow-specific entity with elemental damage/effects and a coloured wisp layer; the golem dart launcher used its own non-pickup projectile.
- **Forge port:** v11.63.07 restores separate arrow items, synchronized type/NBT, TC4 damage multipliers and effects, item pickup identity, coloured flight particles, and a dedicated golem dart entity.
- **Remaining deviations:** the original additive billboard wisp is represented by a coloured particle trail; actual bow interoperability, damage attribution, pickup, multiplayer synchronization and final visual parity have no built-client evidence.
- **Release impact:** projectile combat remains `PARTIAL / NOT TESTED`.



### Research-note and table runtime proof

- **Original:** primary research is selected in the Thaumonomicon, creates a targeted note in player inventory, and the hex puzzle is edited only inside the two-slot Research Table; an unfinished note does nothing useful in hand, while a completed discovery is learned by item use.
- **Forge port:** v11.62.82 restores that source-level flow, rejects legacy table-side note creation/learning actions, retains paper + scribing-tool ink consumption, and restores the bookshelf/Brain-in-a-Jar bonus sources.
- **Remaining deviations:** no built-client evidence yet covers note creation, insertion/removal, hex edits, Expertise/Mastery probabilities, duplication, bonus persistence or completed-discovery learning.
- **Release impact:** Research Table remains `PARTIAL / NOT TESTED`.

### Warp event visuals and runtime distribution

- **Original:** a single potion effect is authoritative for Warp Ward; warp events use a separate counter, signed three-axis valid-position searches, a 0.75 forward Death Gaze visibility cone and client-only `PacketMiscEvent` post-processing.
- **Forge port:** v11.62.81 migrates old persistent ward timers into the real effect, keeps the rare sticky cleanse from resetting the counter, restores valid guardian/spider spawn searches and the Death Gaze cone, and synchronizes all warp buckets/counter.
- **Remaining deviations:** the original warp pulse/mist GLSL/post-processing remains approximated by particles and GUI overlays; event frequency/distribution, milk removal, client bucket synchronization and two-player behavior have no runtime artifacts.
- **Release impact:** Warp/Eldritch remains `PARTIAL / NOT TESTED`.

### Essentia transport pressure and processing

- **Original:** reservoir suction 24 on one selectable six-direction face; direct five-tick pull; buffer side suction derived from bellows/chokes and compared with the real neighbour suction/minimum; centrifuge input may queue while output is occupied or redstone pauses processing.
- **Forge port:** v11.62.79 restores those source-level rules, preserves face-aware buffer arbitration and rolls rejected transfers back.
- **Remaining deviation:** no built-client suction-conflict test, rollback fault injection, redstone/output queue test or long-running network soak artifact exists.
- **Release impact:** essentia transport and processing remain `PARTIAL / NOT TESTED`.


### Golem task adapters

- **Original:** independent priority-based AI goals for every core, including `AIAvoidCreeperSwell`, marker-side-aware `AIUseItem` and water-quality-dependent `AIFish`.
- **Forge port:** v11.62.78 restores the marker-side/empty-hand USE path, first by-product carry behavior, priority creeper avoidance and TC4 fishing probability order/quality modifiers at source level.
- **Remaining deviation:** the complete materials × cores × upgrades × decorations × markers task matrix has not been executed in a built client or dedicated server.
- **Release impact:** golems remain `PARTIAL / NOT TESTED`; the three new runtime cases and the two existing broad golem cases require SHA-256-backed artifacts.


### Raw aura node item

- **Original player path:** capture and transport through Node in a Jar.
- **Forge port:** the direct `aura_node` item is migration-only and converts
  stored legacy node NBT to Node in a Jar when possible.
- **Reason:** prevents a static/debug node item from being mistaken for the
  original dynamic node-item renderer.
- **Release impact:** acceptable only after Node Jar runtime tests pass for all
  six node types and all three modifiers.

### Outer Lands generation adapter

- **Original:** dedicated dimension provider, maze generation lifecycle,
  rooms, locks, traps, boss progression and return path.
- **Forge port:** cell-aligned entry maze, TC4 portal-room geometry, existing-maze-only chunk population, Eldritch Lock-gated boss spawn and SavedData bridge are present at source level.
- **Reason:** the complete dimension bootstrap, room/loot coverage and runtime progression lifecycle are still unverified.
- **Release impact:** P0 remains open. No release PASS is permitted without the
  complete portal → maze → boss → save/reload → Overworld test evidence.

### Visual difference metrics

- **Port tool:** `tools/compare_visual_artifacts.py` produces MAE, RMSE, exact
  pixel ratio, global SSIM and an amplified difference image.
- **Deviation:** those metrics cannot automatically prove visual parity because
  UI scaling, interpolation and lighting can change pixels without changing the
  intended TC4 appearance.
- **Release impact:** human side-by-side review remains mandatory.

### Taint ecology and biome feedback

- **Original:** taint spread is coupled to the Tainted Lands biome state, includes the five fibre/stalk stages, mature spores, taint spiders, taint-specific poison and a flying Spore Swarmer path from crust.
- **Forge port:** v11.63.00 includes source-level column persistence, the five fibre/stalk states, stationary spores, taint spiders, crust-grown Spore Swarmers, particle-driven swarms, the dedicated Taint Poison effect, infected-resource inventory behavior, the original two-resource spider drop branch and the complete poison-death replacement table for six vanilla forms plus health-scaled Thaumic Slime fallback.
- **Remaining deviations:** the SavedData column bridge does not yet reproduce the complete biome palette, weather and biome-replacement feedback; the named taint/tentacle/swarm damage sources and death-message keys are restored at source level; final armour, attribution and multiplayer message behavior still require runtime evidence.
- **Release impact:** Taint remains `PARTIAL / NOT TESTED`. Spread, persistence, poison cadence, inventory infection, drops, rendering and dedicated-server synchronization require SHA-256-backed runtime artifacts.
### Full texture and UV audit evidence

- **Static coverage:** every original PNG/MCMETA resource, every shipped item/block JSON model, all client renderer files, GUI classes and block render layers are enumerated in `audit_reports/`.
- **Confirmed fixes in 11.62.92:** Wisp atlas/halo render pipeline and Alchemical Furnace GUI/slot layout.
- **Remaining limitation:** source and pixel hashes do not prove final GPU output, item transforms, lighting, interpolation or z-sorting. Runtime side-by-side screenshots remain mandatory.
- **Release impact:** visual/runtime status remains `NOT TESTED` until the evidence manifest references real client captures.

## Wand subtype catalogue and runtime visual verification (11.62.93)

- The original wand registry is represented by one Forge item plus root NBT `rod`, `cap` and optional `sceptre`, rather than one registered item per combination.
- The creative tab now exposes 108 craftable rod/cap wand-or-staff combinations plus the canonical silverwood/thaumium sceptre. The separate Avaritia creative wand remains its own compatibility item; infinity-only rod/cap combinations are intentionally not duplicated in the ordinary matrix.
- Active wand rod/cap PNG files are byte-identical to the original TC4 model textures. The confirmed defect was subtype identity: untagged JEI outputs fell back to wood/iron, so capacity and BEWLR textures both appeared wrong.
- Runtime screenshots for all 109 NBT combinations and JEI focus/recipe navigation remain NOT TESTED until a 11.62.93 client JAR is built and exercised.

## Alembic and vis-relay OBJ runtime verification (11.62.94)

- The original TC4 `alembic.obj`, `alembic.png`, `vis_relay.obj` and `vis_relay.png` are now used directly by BER/BEWLR paths.
- The previous cuboid Alembic and cube Vis Relay were confirmed source-level model mismatches and are removed.
- OBJ faces are triangles while Forge entity render buffers use quads; each triangle is preserved with a degenerate repeated fourth vertex.
- Final lighting, culling, item transforms and liquid depth remain `NOT TESTED` until a built 11.62.94 client is captured side-by-side with TC4 4.2.3.5.


## 11.62.95 — all-block render layer / core geometry pass

- All 49 block models previously confirmed as alpha-on-solid now declare an explicit Forge 1.19 render type.
- Six primal crystals and the balanced cluster use the original `vcrystal.obj` geometry.
- Flux Goo uses its real 3/16-height model; Flux Gas uses intersecting translucent planes.
- Bellows uses the five source model parts; v11.63.22 adds the original live compression cycle through a BlockEntity renderer.
- Eldritch Portal uses the original 16-frame camera-facing texture strip through a full-bright BER.
- Advanced Alchemical Furnace uses the original Base/Tank OBJ groups and textures; live on/off texture switching remains runtime work.
- Final appearance, sorting, culling and animation timing remain NOT TESTED until a client build is run.


## 11.62.96 item geometry notes

- Bellows item uses neutral inflation 0.70; v11.63.22 adds a dedicated world `BellowsBlockEntity` with live compression.
- Centrifuge item rotates decoratively; world rotation remains authoritative from its BlockEntity.
- Infusion Matrix item uses a stable inactive preview; active instability/glow remains world-only.
- Advanced Alchemical Furnace item uses a normalized copy of the original three-block OBJ so it fits GUI/hand contexts; UV/material assignments are unchanged.
- Final transform, culling, mipmap and translucency parity remain NOT TESTED in runtime.

## 11.62.97 Taint Swarmer / Swarm runtime notes

- The previously missing flying Spore Swarmer and Taint Swarm are now implemented and connected to the original taint-crust 1/200 spawn branch.
- TC4's swarm used a custom `swarmParticleFX` renderer. The 1.19.2 port preserves the invisible body and particle-cloud behavior with vanilla witch particles because the legacy particle class is not directly reusable.
- The old model used a negative core scale through fixed-function OpenGL. The modern model uses equivalent positive scaling to avoid inverted-face culling while preserving the two-cube proportions and pulse timing.
- Runtime spawn frequency, pathing, particle density, poison combat, drops, full-bright rendering and server/client synchronization remain NOT TESTED until a Forge client build is available.

## 11.62.98 Taint Poison / infected resource runtime notes

- Restores TC4's dedicated `Taint Poison` effect instead of routing taint contact through vanilla Poison.
- The effect keeps the original `0x663377` colour and `40 >> amplifier` cadence, heals marked tainted mobs by one point and deals one point of magic damage to non-undead victims.
- Tainted slime/goo and taint tendrils now reproduce the original inventory infection roll (`nextInt(4321) <= stack size`), 120-tick effect, player warning and one-item consumption.
- Taint blocks, fibres, swarms and infusion instability now apply the dedicated effect; taint spiders restore the original 1/6 dual-resource drop branch.
- The original potion-atlas cell `(3,1)` is exported as the modern mob-effect icon. Runtime cadence, distribution, icon rendering and multiplayer synchronization remain NOT TESTED.

## 11.63.00 Taint Poison death-conversion runtime notes

- Restores the original `LivingDeathEvent` replacement table for creeper, sheep, cow, pig, chicken and villager victims carrying Taint Poison.
- All other non-tainted living victims create a Thaumic Slime with `1 + min(maxHealth / 10, 6)` size.
- Adds the original mob attributes, hostile goals, Tainted Goo/Taint Tendril drops, tainted sheep shearing and the tainted creeper's 30-tick non-destructive infection burst.
- Uses original TC4 entity textures and synchronized client fuse state; runtime visual, AI and multiplayer evidence remains `NOT TESTED`.


## 11.63.00 Liquid Death and named damage-source runtime notes

- Registers source/flowing Liquid Death, its placeable bucket, original animated texture and purple bubble/pop feedback.
- Preserves the observable TC4 four-quanta damage ladder as 4/3/2/1 unblockable dissolve damage across source and early flow depths.
- Deaths caused by the fluid add aspect-attuned essentia crystals using TC4's 50% per-aspect branch and amount formula.
- Taint Poison now uses the original armour-bypassing magic `taint` identity; taintacles and swarms use attacker-aware `tentacle`/`swarm` identities.
- Modern `ForgeFlowingFluid` is non-source-generating but is not a conservation-perfect replacement for 1.7.10 `BlockFluidFinite`; exact finite-volume propagation remains a documented runtime/engine adaptation.
- Bucket pickup/placement, flow timing, death messages, armour interaction, particles and crystal distributions remain NOT TESTED until a Java 17 Forge client/server build is available.
## v11.63.03 runtime visual / Outer Lands hotfix

- The supplied first-person sceptre capture exposed a legacy `IItemRenderer` equipped transform being reused inside a modern BEWLR. The held transform is now bounded, but final FOV/left-hand/staff parity still requires client screenshots.
- Outer Lands no longer generates a 5x5 maze neighbourhood synchronously before `ServerPlayer.teleportTo`; only the portal cell is generated at transfer and live population is limited to one successful room per 40 ticks. Boss traversal and return remain runtime `NOT TESTED`.
## v11.63.04 Brainy Zombie family runtime notes

- Brainy Zombie and Giant Brainy Zombie are present at source level with the original TC4 attributes, spawn-egg colours, texture, scan aliases, drop branches and anger formulas.
- The normal form is injected into `#minecraft:is_overworld` with weight 10 and pack size 1. The original Eerie-biome weights (Brainy 32, Giant 8) cannot be restored exactly until the dedicated Eerie biome is completed.
- Modern collision dimensions are refreshed from synchronized anger instead of the 1.7.10 `setSize` call; side-by-side hitbox and renderer-scale evidence is still required.
- Natural spawn distribution, sunlight behavior, loot probabilities, JARBRAIN scan unlock, save/reload of anger and multiplayer synchronization remain runtime `NOT TESTED`.



## 11.63.05 Inhabited Zombie / Eldritch biome runtime notes

- Outer Lands now points at a dedicated `thaumcraft:eldritch` biome instead of `minecraft:the_void`.
- The original equal-weight Inhabited Zombie and Eldritch Guardian spawn list is restored data-driven.
- Inhabited Zombie armour, cultist hostility, local one-host cap and helmeted-crab death release are present at source level.
- Final mob-density, death timing, armour rendering and save/reload behavior remain NOT TESTED in a built client/server.

## 11.63.17 Boots of the Traveller runtime notes

- The original client-only direct assignment `stepHeight = 1.0` is represented by a transient `forge:step_height_addition` modifier of `+0.4`, preserving the vanilla 0.6 base and allowing other attribute modifiers to compose.
- The original mutable `jumpMovementFactor` values are not copied directly. Normal airborne control is represented by an additional `0.03` movement contribution, matching the difference between the old vanilla `0.02` and TC4 target `0.05`.
- TC4 lowered the target to `0.03` while its legacy Hover state was active. v11.63.18 now exposes the Hover Harness state and Hover Girdle runtime; exact combined movement with the Boots still requires client evidence.
- Ground acceleration is applied on both logical sides for prediction/server authority; exact integrated-client correction and dedicated-server behavior require runtime evidence.
- Step collision on slabs, fences, trapdoors and modded collision shapes; water movement; jump height; fall-damage threshold; Runic Augment/Repair interaction; multiplayer and save/reload remain `NOT TESTED`.

## 11.63.18 Thaumostatic Harness / Hover Girdle runtime notes

- `allowFlying`/`isFlying` from 1.7.10 is represented by modern player abilities (`mayfly`/`flying`) and synchronized with `ServerPlayer.onUpdateAbilities`; the server owns fuel, state and revocation.
- TC4 mutated its charge counter on both logical sides. The port consumes Potentia only on the server and lets the equipped `ItemStack` synchronize to clients, avoiding double consumption.
- The original `Utils.resetFloatCounter` has no stable public Forge 1.19.2 equivalent. Active flight resets `fallDistance`; exact anti-kick/floating behavior must be verified on a dedicated server.
- v11.63.19 wires the original Harness geometry to a modern armor model and player layer. Static conversion verifies all 124 OBJ triangles, UVs and normals, but final worn scale, culling, blend state and pose alignment remain `NOT_TESTED` without client captures.
- Curios and legacy Baubles remain optional reflective integrations. Without either API, the established Stage211 offhand/inventory mirror treats the first carried Hover Girdle as equipped; this deliberately favors standalone Forge usability over strict accessory-slot semantics.
- `NetworkHooks.openScreen` and an `IForgeMenuType` client constructor carry the source hand to the one-slot menu. Main-hand source-slot locking follows TC4; offhand opening is a modern extension.
- Flight prediction, repeated toggle packets, final-fuel shutdown, Haste damping, Girdle efficiency, creative/spectator transitions, death/respawn, dimension travel, multiplayer and save/reload remain `NOT TESTED` without a compiled client/server.

## 11.63.19 Hover Harness visual runtime notes

- Forge's client item extension supplies the custom torso-only armor model; the extra OBJ/effect layer is installed on every registered player skin renderer. This avoids common-side renderer initialization while keeping the chest `ItemStack` authoritative.
- The original fixed-function `GL11` operations are reproduced in the same scale/rotation/translation order. The retained OBJ is converted at source time into 124 immutable triangles because legacy `AdvancedModelLoader` is unavailable on Forge 1.19.2.
- Original lightning rings used the item atlas and `GL_SRC_ALPHA, GL_ONE`. The port samples all 16 vertical frames directly and uses the full-bright eyes render type, which is the closest built-in additive entity layer; exact bloom and sorting are a documented visual adaptation.
- Legacy `FXLightningBolt` performed target raycasts and maintained mutable per-entity wall-clock timers. The port renders deterministic local eight-segment arcs keyed by entity ID and a three-tick interval, avoiding client state leaks while preserving the energized appearance. Exact endpoint distribution differs from TC4.
- Default/slim skin geometry, crouch/swim/Elytra poses, invisibility, spectator rendering, shaders, resource reload and two-client tracking remain `NOT_TESTED`.
## 11.63.21 Hungry Chest runtime notes

- TC4 inserted colliding `EntityItem` stacks through `InventoryUtils.placeItemStackIntoInventory(..., side=1, true)`. The modern port uses Forge's documented stacked item-handler insertion algorithm; because Hungry Chest exposes an unsided inventory, the legacy side value imposed no slot restriction.
- The legacy tile periodically repaired its viewer count. The port recounts nearby players every 80 server ticks and accepts only a `ChestMenu` whose backing container is the exact block entity. Exact disconnect timing and modded menu wrappers require runtime evidence.
- World and item geometry use the original vanilla `ModelChest` cuboids and byte-exact TC4 texture. Modern BEWLR context transforms are retained for GUI/hand/ground usability; exact screen-space scale across GUI scales and third-party item renderers remains a visual adaptation pending screenshots.
- Hopper insertion/extraction, partial item entities, NBT/capability compatibility, comparator changes, block-event delivery, save/reload, resource reload and two-client lid synchronization remain `NOT_TESTED`.


## 11.63.22 Arcane Bellows runtime notes

- The original mutable `TileBellows.orientation` byte is represented by the six-value `DirectionalBlock.FACING` blockstate, allowing normal structure rotation and mirroring.
- TC4 accessed the private vanilla furnace cook field directly. The port performs the same guarded `CookTime + 1` mutation through the furnace's stable NBT serialization every two server ticks, avoiding reflection and access transformers.
- The original `mob.ghast.fireball` cue maps to `SoundEvents.GHAST_SHOOT`; volume `0.01` and triangular pitch are preserved.
- World animation is client-local exactly like TC4, while redstone gating and furnace acceleration are server authoritative. Exact visual phase agreement between clients is not guaranteed or required by the original implementation.
- Six-direction collision, normal-furnace timing, power transitions, chunk save/reload, resource reload and multiplayer observation remain `NOT_TESTED` without a compiled artifact.


## v11.63.24 Arcane Spa

- Static contracts cover the independent cadence, source expansion and capability sides, but actual fluid placement, automation races, chunk boundaries and multiplayer GUI synchronization remain NOT TESTED until a Forge 1.19.2 JAR is built and run on Java 17.
- The old Forge-fluid quanta metadata repair has no direct 1.19.2 equivalent; the port places the modern fluid legacy block state and relies on the registered FlowingFluid implementation.

## 11.63.28 Arcane Lamp runtime notes

- TC4 stored `facing` in `TileArcaneLamp` NBT and rendered the nozzle through `TileArcaneLampRenderer`. The port uses the six-value `BlockStateProperties.FACING` state and an equivalent three-part baked model; exact animated-texture phase and nozzle shading need client screenshots.
- The invisible `blockAiry` metadata-3 light is represented by a dedicated no-collision, no-occlusion block with light level 15 and a source-tracking block entity. It is not exposed as an item.
- Light placement preserves the original `rand(16)-rand(16)` distribution, one attempt per server tick, local raw brightness threshold 9, surface-height+4 ceiling and legacy minimum Y=5. Unloaded target chunks are deliberately skipped instead of force-loaded.
- Original removal scanned a 31×31×31 cube and removed all metadata-3 lamp lights, including markers from another overlapping lamp. The port preserves that behavior but skips unloaded chunks.
- Marker validation, block replacement, overlap cleanup, dimension build limits, resource reload and two-client lighting synchronization remain `NOT_TESTED` without a successful Java 17 Forge build.

- Source packaging repair: six `world` classes missing from the v11.63.27 ZIP were restored byte-for-byte from v11.63.26, and `gradle-wrapper.jar` was restored from v11.63.25. JDK 17 is unavailable and the Wrapper cannot resolve `services.gradle.org`, so no v11.63.28 JAR exists.


## 11.63.29 Simple Resource Families runtime notes

- Passive TC4 resources intentionally keep their original `tc4_*` registry IDs. Parallel rebuild IDs remain registered for compatibility, but the automatic duplicate migrator no longer rewrites the 12 newly completed shard/material mappings.
- Tin, silver and lead cluster outputs cannot target an item tag in vanilla smelting JSON. Their cluster IDs and Forge tags are complete, but doubled smelting output requires a concrete compatible ingot supplied by an installed mod/data pack.
- The neutral `tc4_shard` is retained only as a metadata-era migration ID and is not assigned an invented primal aspect.
- Food timing, tag recipe interoperability, recipe-viewer display, dropped-stack persistence and multiplayer save/reload remain `NOT_TESTED`.

## 11.63.37 Unique item systems runtime notes

- Arcane Door ownership/access is stored as UUIDs in the lower-half block entity and synchronized to the upper half. TC4's username strings were not retained because UUID identity is the stable modern equivalent. Runtime behavior with renamed/offline players, simultaneous key binding, chunk borders and two-client plate activation remains `NOT_TESTED`.
- The door ignores ordinary redstone and checks horizontally adjacent powered Arcane Pressure Plates whose owner/access set overlaps its own. Exact edge cases with overlapping double doors, pistons, explosions, structure movement and modded break events require runtime evidence.
- Crimson Blade source values and debuffs are reconstructed statically, but attack calculations, enchantments, self-repair synchronization and warp aggregation need a Java 17 client/server run.
- The Resonator currently supports the port's concrete tube, jar/void jar, reservoir and alembic block entities. Other future essentia devices must implement a shared readout contract before they can expose suction through this item.
- The original Eldritch Obelisk Placer checked offsets `+1..+6` but wrote an obelisk at `+7`; the port deliberately validates every actual target (`+1`, `+3..+7`) to prevent silent block replacement.
- `tc4_lightningring` was an `ItemHoverHarness` renderer texture and `tc4_sinister_stone_active` was the second sprite of one compass item in TC4. They are retained as hidden registry aliases for migrated saves and are not presented as new standalone mechanics.
- Sinister Lodestone discovery scans already-loaded client chunks only and performs a collider line-of-sight trace. It never force-loads chunks; range/type/occlusion transitions and performance with many carried stones remain `NOT_TESTED`.


## 11.63.38 Block alias normalization runtime notes

- TC4 Ethereal Bloom rewrote biome data. The 1.19.2 rebuild intentionally purifies explicit taint blocks in the original-sized radius because mutable biome persistence is not a safe migration primitive.
- Device migration aliases place the canonical functional block. This preserves old inventory registry IDs while avoiding duplicate BlockEntity networks and duplicated ticking state.
- Obsidian Totem metadata variants remain separate exact block IDs for save/model compatibility.
- Four complex block systems remain in generic fallback: Growth Lamp, Fertility Lamp, Wand Pedestal and Focused Wand Pedestal.

## 11.63.39 Final block systems runtime notes

- Growth uses the 1.19.2 `BonemealableBlock` contract instead of the removed 1.7.10 scheduled random-update pathway, while preserving radius, Herba charge and reserve accounting.
- Fertility preserves the TC4 species population cap and charge cadence; vanilla 1.19.2 love mode performs the actual offspring workflow.
- Focused pedestal compound aspects are recursively reduced to a primal component with available storage; the transfer remains one whole vis (100 centivis) per five ticks, matching TC4 ItemWandCasting#addVis(..., 1, true).
- Runtime behavior still requires an in-game Java 17 Forge validation pass; static guards do not substitute for that pass.

### 11.63.41 Node in a Jar runtime notes

- **Source parity restored:** exact 3×4×3 wooden-slab/glass ritual, six-primal 70-centivis cost, 75% one-step modifier degradation, exact aspect/base-aspect transfer, persistent NodeId, filled world block/item and wand release.
- **Corrected deviation:** released nodes are now marked `jarred=false`; the earlier adapter left them excluded from pedestal charging.
- **Migration:** raw `aura_node` inventory items remain migration-only and convert stored node NBT into a filled Node in a Jar. Empty historical `node_jar` stacks cannot be placed.
- **Still unverified:** multiplayer hand arbitration, chunk save/reload, 75/25 distribution, shader/translucency behavior, modded wooden-slab tags and all six node types/available modifier combinations in a real Forge runtime.
- **Release impact:** source contract is substantially closer to TC4, but the Node Jar subsystem remains `PARTIAL / NOT TESTED` until runtime evidence exists.


## v11.63.50 final recipe-ledger closure

- The historical recipe source ledger is closed at **258/258 statically mapped registrations**; runtime loading of the exact v11.63.61 resources remains unverified until Java 17 build and GameTest-server decoding succeed.
- `AdvancedGolem` remains represented by eight JSON files because 1.19.2 has eight registry items instead of one damage-valued `itemGolemPlacer`. This is an intentional exact de-metadata expansion and counts as one historical recipe record.
- The Advanced Golem first infusion component is `minecraft:shears`; earlier redstone materialization was a stale resolver error.
- These are static/source-backed claims only. All eighteen Forge GameTests remain `NOT_TESTED` until Java 17 build/runtime evidence exists.


## v11.63.52 — duplicate registry purge runtime boundary

- 63 exact duplicate item aliases are no longer registered.
- Legacy worlds depend on `MissingMappingsEvent` plus the deep ItemStack migrator; the source contract is guarded but an actual old-world fixture run is still required.
- The only repeated English display text left is `Scribing Tools` for normal and infused variants; these are semantically distinct and intentionally remain separate.
- v11.63.51 18/18 GameTest PASS is only a previous baseline. v11.63.52 requires 19/19 after a Java 17 rebuild.


## 11.63.54 validation boundary

The four new block-entity GameTests are source-complete but NOT_TESTED until this exact version is built on Java 17 and run with `runGameTestServer`. Client render, sound and GUI parity remain separate evidence.

## 11.63.60 Infusion Stability

- Runtime остаётся `NOT_TESTED` до сборки на Java 17 и запуска 52/52 GameTests.
- Source-контракт не доказывает полный altar lifecycle, instability events, save/reload, multiplayer и chunk reload.
- Dragon Head намеренно не входит в legacy TC4 skull whitelist этого этапа.

- Изменённый катализатор отменяет процесс после одной weighted instability-ветки без дополнительного Warp; успешная инфузия не начисляет Warp автоматически.
- Отсутствующий компонент сохраняет активный процесс в режиме ожидания.

## 11.64.01 Research Note completion runtime boundary

- Source parity now requires a live Research Table, the same note identity, an exact NBT snapshot and a graph solved with the requesting player's known aspects before the completion bit can be committed. Repeated or stale solve requests are rejected and do not trigger table/note synchronization.
- Completion consumes no additional ink: TC4 charged ink for the accepted edit that made the graph complete.
- Completed discovery conversion unlocks the target first, then eligible siblings, and consumes one note even when the player is in creative mode. A stale or no-longer-unlockable discovery is not consumed.
- The six new GameTest methods and 456-case manifest are source artifacts only. Java 17 Gradle build, dedicated GameTest execution, multiplayer packet races, client GUI behavior and actual sibling progression remain NOT VERIFIED.

## 11.64.04 — Warp spawn tri-state offset parity

- Нулевая ось сохранена намеренно и подтверждена строками 356–358 и 388–390 оригинального `WarpEvents.java`.
- Source/static guards не доказывают фактическое распределение RNG, доступность спавна, коллизии или сетевое поведение.
- До Java 17 build и dedicated GameTest run статус остаётся NOT VERIFIED.


## 11.64.07 — Warp text.8 переклассифицирован как веха BATHSALTS

- Исправление предыдущей записи: `warp.text.8` НЕ относится к blurred-vision.
  Оригинал: blurred-vision (effectRoll<=36) — только эффект, без чата.
  `warp.text.8` выводится один раз при вехе BATHSALTS (actual warp > 10).
- ОТКРЫТО (следующий раунд): вехи ELDRITCHMINOR (>25) и ELDRITCHMAJOR (>50) в оригинале
  не выводят чат и вызывают grantResearch(10)/grantResearch(20). В порте сейчас выдуманные
  строки и нет grantResearch — расхождение зафиксировано, будет исправлено.
- Runtime по-прежнему NOT VERIFIED (нет javac; JAR не собран).

## 11.64.06 — Warp blurred-vision message

- Восстановлен вывод `warp.text.8` на ветке blurred vision; это единственная оригинальная warp-строка, ранее не показывавшаяся портом.
- Source/static guards доказывают связь константы `BLURRED_VISION_MESSAGE_KEY` с production-веткой и совпадение текста с оригинальным `original_tc4_1710/lang/en_US.lang`, но НЕ доказывают фактическое runtime-отображение чат-строки клиенту.
- В песочнице нет `javac`: Java parse и pure-Java self-test — NOT VERIFIED. Gradle build недоступен из-за сети. Java 17 JAR и Forge GameTest server (140/140) остаются NOT VERIFIED.

## 11.64.16 Hungry Chest runtime boundary

- SOURCE CLOSED and RESOURCE CLOSED are static/source-backed claims only.
- Exact collision ingestion, partial stack behavior, opener block events, lid sounds, hopper interaction, comparator updates, break-drop random distribution, item transforms and multiplayer menu lifecycle still require a built Java 17 Forge runtime.
- Gradle Wrapper could not download Gradle 7.5.1 because `services.gradle.org` was unreachable; no JAR or 157/157 GameTest runtime evidence exists.
- The original TC4 menu validity intentionally checks only that the same block entity remains at the position; the removed modern eight-block distance gate is therefore not treated as parity.

## 11.64.20 Arcane Levitator runtime boundary

- SOURCE CLOSED and RESOURCE CLOSED are source/static claims, not a successful Forge runtime result.
- The original global client Shift query is intentionally represented by synchronized per-entity sneak state so motion is authoritative on a dedicated server.
- Legacy `isOpaqueCube` is represented by `BlockState.isSolidRender`; unusual modded block states still require in-game compatibility testing.
- Legacy lightmap brightness 180 is represented by Forge model block light 11 for the active glow layer.
- Actual redstone transitions, stacked chunk boundaries, entity prediction, particle settings, animated texture reload and two-client behavior remain NOT VERIFIED.
- Gradle Wrapper could not download Gradle 7.5.1 because `services.gradle.org` was unreachable; no JAR or 177/177 GameTest runtime evidence exists.

## 11.64.21 Arcane Pressure Plate runtime boundary

- SOURCE CLOSED and RESOURCE CLOSED are source/static claims, not a successful Forge runtime result.
- The original name-based `owner/access` schema is stored alongside UUID migration data; real old-world fixtures with renamed/offline players still require save/reload testing.
- The original key deliberately matches only `x,y,z` and has no dimension identity. Cross-dimensional coordinate collisions are retained for parity and require multiplayer observation rather than a non-original fix.
- Any tag compound enters the legacy bound-key branch. This intentionally prevents malformed tagged keys from being silently rebound, but actual inventory/full-inventory/drop behavior remains runtime-unverified.
- Actual entity trigger timing, ignored-trigger entities, redstone neighbor order, owner wand interaction, explosion immunity, client model switching, tooltip rendering, JEI decoding and two-client access changes remain NOT VERIFIED.
- Arcane Door is only a dependent key regression boundary in this release and is not declared fully closed.
- Gradle Wrapper could not download Gradle 7.5.1 because `services.gradle.org` was unreachable; the sandbox also exposes OpenJDK 21 instead of the required JDK 17. No JAR or 183/183 GameTest runtime evidence exists.



## 11.64.22 Arcane Lamp runtime boundary

- SOURCE CLOSED and RESOURCE CLOSED относятся только к обычной Arcane Lamp; Growth/Fertility lamps не закрывались этим раундом.
- `TileArcaneLampLight` намеренно не активирован: во всём оригинальном source tree он не имеет регистрации/ссылок, а `blockAiry` meta 3 не создаёт TileEntity.
- Marker может сохраняться бесконечно, когда лампа удалена в обход обычного break-cleanup или когда Arcane Bore поставил marker вне cleanup-куба; это исходное поведение, а не утечка, придуманная портом.
- Прямые world-access циклы могут затрагивать соседние чанки, как в TC4; реальная стоимость загрузки и межчанковое сохранение требуют runtime-проверки.
- Второй Bore Base connector, шесть nozzle-трансформаций, resource reload и multiplayer visibility остаются NOT VERIFIED до клиентского запуска.
- Gradle Wrapper не смог разрешить `services.gradle.org`; активна OpenJDK 21 вместо требуемой JDK 17. JAR и 190/190 GameTest runtime evidence отсутствуют.

## 11.64.24 Arcane Bore runtime boundary

- SOURCE CLOSED и RESOURCE CLOSED являются результатом source/static сверки, а не успешного Forge runtime.
- Точный client interpolation старого `rotX/rotZ/vRadX/vRadZ` адаптирован к синхронизированной target-линии и 1.19.2 renderer; фактическое движение на 30/60/144 FPS требует клиентской проверки.
- Machine repair зависит от актуальной object-aspect базы порта и требует проверки на нескольких модифицированных кирках.
- Native Cluster mappings для сторонних ore/raw items требуют runtime-проверки с соответствующими модами.
- Реальные hopper/inventory side capabilities, full-inventory ejection, chunk unload, base removal, vertical nozzle и два клиента остаются NOT VERIFIED.
- Gradle Wrapper не смог разрешить `services.gradle.org`; активна OpenJDK 21.0.10 вместо JDK 17. JAR и 204/204 GameTest runtime evidence отсутствуют.
## 11.64.29 Lamp of Growth runtime boundary

- SOURCE CLOSED и RESOURCE CLOSED являются source/static-результатом, а не успешным Forge runtime.
- Старый отложенный `updateTick` адаптирован к одному `BlockState.randomTick`; фактический однокадровый сдвиг и вероятность роста требуют клиент/server проверки.
- Исходный IMC `lampBlacklist` представлен datapack-тегом `thaumcraft:growth_lamp_blacklist`; совместимость сторонних модов требует явного datapack или integration layer.
- Прямой доступ к соседним позициям сохранён без `hasChunkAt`, как в TC4; межчанковая стоимость требует runtime-проверки.
- Частицы, динамическое освещение, шесть nozzle-трансформаций, второй Bore Base connector, dedicated server и два клиента остаются NOT VERIFIED.
- Lamp of Fertility использует общий block entity/renderer, но не входит в заявление полного закрытия v11.64.29.
- Gradle Wrapper не разрешил `services.gradle.org`; активна OpenJDK 21.0.10 вместо JDK 17. JAR и 240/240 GameTest runtime evidence отсутствуют.


## 11.64.30 Lamp of Fertility runtime boundary

- SOURCE CLOSED и RESOURCE CLOSED являются source/static-результатом.
- `setInLove(null)` является современным Forge-эквивалентом исходного `func_146082_f(null)`; фактический breeding AI, particles/hearts и cooldown требуют runtime.
- Exact-class population cap сохранён; совместимость modded subclasses требует in-game проверки.
- Direct jar output разрешён только через верхнюю сторону, как у `TileJarFillable`; capability/inter-mod transport требует runtime.
- Dynamic light, on/off animated textures, nozzle transforms, two-client synchronization и dedicated server остаются NOT VERIFIED.
- Gradle Wrapper не разрешил `services.gradle.org`; активна Java 21.0.10 вместо JDK 17.

## 11.64.31 Arcane Workbench runtime boundary

- SOURCE CLOSED и RESOURCE CLOSED являются результатом source/static-аудита, а не успешного Forge runtime.
- Старый скрытый rebuild-slot 11 читается только как одноразовая миграция: предмет переносится в видимую 3x3 сетку либо выбрасывается, после чего slot 11 больше не сохраняется.
- Оригинальное удаление обычного жезла из выбранной руки при превращении стола сохранено и для creative, как в `BlockTable#onWandRightClick`; фактическое взаимодействие item-use/block-use порядка требует runtime.
- Empty AspectList теперь стоит 0 vis; реальные wand discounts, research sync и многократный shift-click требуют runtime-проверки.
- Sided inventory ограничен slot 10, но фактические Forge capabilities/hopper wrappers должны быть проверены на dedicated server.
- Installed-wand renderer, dim ghost, `Insufficient vis`, multiplayer sync и NBT reload остаются NOT VERIFIED до клиентского/dedicated запуска.
- Gradle Wrapper не разрешил `services.gradle.org`; активна Java 21.0.10 вместо JDK 17. JAR отсутствует.

## 11.64.32 Wand rods/caps runtime boundary

- SOURCE CLOSED и RESOURCE CLOSED являются source/static-результатом для базовой wand/rod/cap-системы; wand foci, Focus Pouch и focus gameplay не входят в этот релиз.
- Четыре original creative variants, root `rod`/`cap` NBT, staff +6 damage, rarity, stack size и component catalogue подтверждены source guards, но фактическая creative/search/JEI выдача требует клиентского запуска.
- Nested `Wand` compound поддерживается только как read-once migration path; реальные старые world/player fixtures и multiplayer inventory sync требуют save/reload runtime-теста.
- Elemental/primal regeneration уже фактически добавляло 1 vis через существующее умножение `addVis` на 100. В релизе переименована неверная centivis-константа и добавлен контракт; это не заявляется как новый gameplay-fix.
- Staff attribute modifier, cap special discounts, sceptre capacity/discount, tick cadence и low-vis threshold требуют dedicated server/runtime проверки.
- 26 component textures совпадают с оригиналом по SHA-256, но first/third-person transforms, hand animation, resource reload и два клиента остаются NOT VERIFIED.
- Gradle Wrapper не разрешил `services.gradle.org`; активна OpenJDK 21.0.10 вместо JDK 17. JAR и runtime GameTests отсутствуют.


## 11.64.33 Wand foci runtime boundary

Source/resource/static parity закрыта. Реальный radial input, mouse capture, client/dedicated packet sync, projectile/beam effects и multiplayer persistence требуют успешной Forge build/runtime проверки. Baubles belt slot в базовом порте адаптирован через off-hand Focus Pouch до отдельного закрытия общей baubles-системы.


## 11.64.34 Infusion altar runtime boundary

Source/resource/static closure is complete. Forge build, client, dedicated server, multiplayer and executed GameTests remain unverified because Gradle 7.5.1 could not be downloaded. Existing jars/tubes/mirrors are external providers for this round and will receive their own strict audit next.


## 11.64.36 Essentia tubes runtime boundary

- SOURCE CLOSED и RESOURCE CLOSED являются source/static-результатом для normal/filter/restrict/one-way tubes, valves и buffers.
- Closed-valve topology, trapped-unit drain, one-way directional equalization, mixed buffer random-aspect choice и multi-branch suction competition требуют реального dedicated-server теста.
- Baked six-side connection state и BlockEntity renderer статически связаны, но actual neighbour updates, resource reload, chunk boundaries и два клиента остаются NOT VERIFIED.
- Старые compounds `tc4Subtype`/`buffer` читаются как migration path; реальные старые миры должны пройти save/reload fixture.
- Gradle Wrapper не разрешил `services.gradle.org`; активна OpenJDK 21.0.10 вместо JDK 17. JAR и выполненные GameTests отсутствуют.

## v11.64.37

- Normal Alchemical Furnace + Alembic are source/resource closed.
- Advanced Alchemical Furnace remains outside this release.
- Build/runtime are not verified because Gradle distribution download failed and the environment is Java 21 rather than Java 17.
