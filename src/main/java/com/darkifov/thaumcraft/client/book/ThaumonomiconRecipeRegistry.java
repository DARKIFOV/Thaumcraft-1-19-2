package com.darkifov.thaumcraft.client.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ThaumonomiconRecipeRegistry {
    private static final List<ThaumonomiconRecipePage> PAGES = new ArrayList<>();

    static {
        PAGES.add(new ThaumonomiconRecipePage(
                "All Addons True Parity",
                "ALL_ADDONS_TRUE_PARITY",
                "Audit",
                "1.7.10 → 1.19.2",
                new String[]{"Stage 82 finishes a deep public-surface parity pass for every uploaded addon.", "Thaumic Energistics is already covered through Stage 80/81, including AE2-like internal grid emulation.", "Thaumic Tinkerer and Thaumcraft Extras now have their original blocks, foci, KAMI resources, warded blocks, machines and API materials mapped into the 1.19.2 rebuild."},
                new String[]{"Use TT parity items/blocks for foci, KAMI, transvector, gas, magnet, warp and mobilizer systems.", "Use TCE parity items/blocks for chargers, exchangers, dark infuser, generator, teleporter, warded construction and dark thaumium tools.", "This is a standalone functional port; final 100% runtime proof still requires Gradle build and in-game testing."},
                "All Addons True Parity Ledger",
                "The final stage for addon surface parity."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "AE2 Internal Parity",
                "AE2_INTERNAL_PARITY",
                "Automation",
                "Grid Emulation",
                new String[]{"Stage 81 adds the missing internal AE2-like layer that cannot be copied directly from the old 1.7.10 API.", "Thaumic ME Controller, ME Cable, Energy Acceptor and Crafting CPU create a standalone grid with channels, energy and job scheduling diagnostics.", "The Arcane Assembler and Arcane Crafting Terminal can now check the grid before scheduling pattern crafts."},
                new String[]{"Use Thaumic Grid Tool to inspect the local grid.", "Add Controller for 32 channels, cables for connectivity, Energy Acceptor for virtual AE energy and Crafting CPU for automated jobs.", "This replaces the old AE2 grid/channel/crafting CPU API with a local Forge 1.19.2-compatible system."},
                "Thaumic ME Controller + Crafting CPU",
                "Internal parity layer for AE2-style networks."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumic Energistics True Parity",
                "THAUMIC_ENERGISTICS_TRUE_PARITY",
                "Automation",
                "Original Surface Map",
                new String[]{"Stage 80 maps the original Thaumic Energistics 1.7.10 blocks, items, parts and core classes onto this standalone 1.19.2 rebuild.", "Added 64k/creative cells, storage casing, knowledge/coalescence/diffusion cores, golem backpack, AE wrench, gear boxes, knowledge inscriber, level emitter, conversion monitor and vis interface.", "AE2-only internals are represented as standalone ME-style diagnostics because real AE2 1.7.10 APIs cannot be dropped into Forge 1.19.2 unchanged."},
                new String[]{"Use Wireless Terminal and Level Emitter to inspect digital essentia.", "Use Arcane Crafting Terminal or Arcane Assembler with Encoded Patterns for automated crafts.", "Use Gear Boxes, Providers and Vis Interface to complete the original Thaumic Energistics machine chain."},
                "Original Thaumic Energistics parity",
                "Surface-complete standalone port of the public blocks/items/parts from the original jar."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Addons Final Completion",
                "ADDONS_FINAL_COMPLETION",
                "Audit",
                "All Addons",
                new String[]{"Stage 79 is a unified final completion pass for all addon branches.", "Thaumic Energistics now has digital essentia, pattern selection, providers, wireless terminal and assembler automation.", "Thaumic Tinkerer and Thaumcraft Extras are tied together through final diagnostics, recipes and progression."},
                new String[]{"Use Addon Completion Ledger to unlock and inspect all addon branches.", "Use Arcane Assembler with Encoded Patterns for automated Thaumcraft item crafting.", "Use Transvector, Ethereal Platform, Fume Dissipator, Pech trades and Extras foci as the secondary addon layer."},
                "Addon Completion Ledger",
                "One final ledger to check and unlock the rebuilt addon systems."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumic Energistics Full",
                "THAUMIC_ENERGISTICS_FULL",
                "Automation",
                "AE + Thaumcraft",
                new String[]{"Stage 78 expands the Thaumic Energistics branch with a local ME-style essentia network.", "Arcane Assembler reads Encoded Essentia Patterns and crafts selected Thaumcraft targets.", "Essentia Providers import jars into nearby Essentia Drives and expose the network to recipes."},
                new String[]{"Shift-right-click an Encoded Pattern to choose the target.", "Right-click an Arcane Assembler with a pattern to diagnose missing items/essentia.", "Shift-right-click an Arcane Assembler with a pattern to craft automatically."},
                "Arcane Assembler + Providers",
                "A practical port of the original Thaumic Energistics automation ideas into this Forge 1.19.2 rebuild."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Matrix Auxiliaries",
                "MATRIX_AUXILIARIES",
                "Infusion",
                "Auxiliary Devices",
                new String[]{"The Infusion Matrix can now be improved with nearby auxiliary blocks.", "Matrix Accelerators increase infusion speed. One accelerator gives x2, and four nearby accelerators cap at x5.", "Matrix Stabilization Pylons reduce instability by 25% each, up to 100% with four effective pylons."},
                new String[]{"Pylons must be symmetric around the altar center.", "Pylons only stabilize while the required essentia is actively available.", "Removing auxiliaries during infusion changes the active process dynamically."},
                "Accelerator + Stabilizer",
                "Use these blocks around the infusion altar to tune speed and safety."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Infusion Matrix Auxiliaries",
                "INFUSION_MATRIX_AUXILIARIES",
                "Infusion",
                "Matrix Control",
                new String[]{"Matrix Accelerators speed up infusion. One accelerator gives x2 speed; up to four accelerators give x5 speed.", "Matrix Stabilizers add 25% stabilization each. Four pylons reach 100% stabilization.", "Accelerators increase risk when they are not matched by stabilizers."},
                new String[]{"Right-click an accelerator or stabilizer to link it with the nearest Infusion Matrix and print current status.", "Recommended setup: up to four accelerators plus four stabilizers around the altar.", "Stabilization is counted during active infusion structure analysis."},
                "Matrix Accelerator",
                "Speed for the price of risk."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Deep Gameplay Parity",
                "DEEP_GAMEPLAY_PARITY",
                "Audit",
                "Behavior Completion",
                new String[]{"Stage 68 focuses on gameplay behavior, not only registry/resource parity.", "Aspect crystals and shards now give insight/resonance when used.", "Goggles of Revealing are now real headgear and share the revealing overlay."},
                new String[]{"Research Notes now complete the next available research.", "Golem Seal Collect can bind a target block.", "Thaumic Energistics cards receive diagnostic active markers and tooltip behavior."},
                "Behavior Pass",
                "More items now do something useful in-game instead of existing only as recipes/resources."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Maximum Port Parity",
                "MAX_PORT_PARITY",
                "Audit",
                "Registry Completion",
                new String[]{"Stage 67 audits every registered item and block across Core Thaumcraft, Thaumcraft Extras, Thaumic Energistics and Thaumic Tinkerer.", "Adds module tags for ported items/blocks and a Porting Ledger item.", "Conservative missing recipes are added only where a registry item clearly belongs to a craftable progression path."},
                new String[]{"This does not claim legal/asset parity with original textures.", "Original assets still need explicit local import/remake.", "The next truth step is a real Forge build log."},
                "Porting Ledger",
                "A new in-game ledger reports the current branch status."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumic Tinkerer True Completion",
                "TT_TRUE_COMPLETION",
                "Addon Compat",
                "True Completion",
                new String[]{"Stage 65 converts the remaining placeholders into real systems.", "Ichor gear now uses real ArmorItem, SwordItem and PickaxeItem classes.", "Bottomless Pouch now opens a real 27-slot GUI backed by item NBT."},
                new String[]{"Helmet of Revealing is now real armor with a client overlay base.", "KAMI Core checks Eldritch progression and warp before unlocking KAMI.", "This pass also fixes old PlayerThaumData package references and registry/menu links."},
                "Build Fix Pack",
                "Thaumic Tinkerer is now much closer to a true 1.19.2 rewrite."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Ichor Gear Realization",
                "ICHOR_GEAR_REALIZATION",
                "Endgame",
                "Gear Base",
                new String[]{"Ichor gear is no longer generic placeholder utility items.", "IchorGearItem provides durability, enchantability and item-specific behavior.", "Ichor Sword ignites targets on hit."},
                new String[]{"This is still a compatibility-safe gear realization pass.", "A later build can replace these with full ArmorItem/TieredItem classes if the compiler mappings are confirmed."},
                "KAMI Equipment",
                "The Ichor branch now behaves like a real endgame gear family."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Fume Dissipator Area",
                "FUME_DISSIPATOR_AREA",
                "Devices",
                "Area Cleansing",
                new String[]{"Fume Dissipator теперь очищает не только игрока, а область вокруг блока.", "Обычный ПКМ очищает радиус 4.", "Shift+ПКМ усиливает радиус до 8 и снимает больше flux с Crucible."},
                new String[]{"Снимает fire, poison, wither, blindness, confusion, hunger, weakness и slowness.", "С Crucible снимает часть flux.", "Добавлены cloud/end rod particles."},
                "Fume Control",
                "Устройство стало полезным для лабораторий алхимии."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "TT Utility Items",
                "TT_UTILITY_ITEMS",
                "Items",
                "Knowledge / Tools / Storage",
                new String[]{"Добавлены Tome of Knowledge Sharing, Infused Scribing Tools, Bottomless Pouch и Helmet of Revealing.", "Сейчас это functional base: предметы имеют use behavior и tooltip.", "Следующий polish может дать отдельные GUI/armor overlay/container."},
                new String[]{"Tome даёт небольшой research insight.", "Pouch показывает scan инвентаря как база под container.", "Helmet подготавливает revealing overlay branch."},
                "Utility Branch",
                "Ветка полезных предметов Thaumic Tinkerer начала закрываться."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "KAMI and Ichor",
                "KAMI_ICHOR_BASE",
                "Endgame",
                "Ichor Materials",
                new String[]{"Добавлены KAMI Research Core, Ichor, Ichorcloth, Ichorium Ingot и базовые Ichor gear items.", "Это endgame branch под дорогие рецепты и будущий gear polish.", "Рецепты требуют Eldritch/Primordial материалы."},
                new String[]{"Пока gear зарегистрирован как base items, чтобы закрыть ветку исследований/рецептов.", "Следующий pass может перевести их в настоящую броню/инструменты со stats."},
                "KAMI Base",
                "Главная endgame-ветка Thaumic Tinkerer подготовлена."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Ethereal Platform Polish",
                "ETHEREAL_PLATFORM_POLISH",
                "Devices",
                "Phasing Platform",
                new String[]{"Ethereal Platform теперь имеет тонкую форму, отдельное collision-состояние и visual particles.", "Обычный ПКМ переключает solid/phase.", "Shift+ПКМ переключает redstone lock."},
                new String[]{"Когда redstone lock включён, redstone signal переводит платформу в phase mode, а отсутствие сигнала делает её solid.", "В phase mode платформа не имеет collision и сбрасывает fall distance у проходящих существ."},
                "Phase Walkway",
                "Эфирная платформа стала настоящим устройством, а не placeholder-блоком."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Transvector Active Use",
                "TRANSVECTOR_ACTIVE_USE",
                "Devices",
                "Remote Actions",
                new String[]{"Transvector Interface теперь умеет не только inspect, но и безопасные remote-действия.", "Jar → Cell и Cell → Jar работают с привязанным Essentia Jar.", "Drive → Cell и Cell → Drive работают с привязанным Essentia Drive."},
                new String[]{"Deep status показывает расширенный статус Arcane Workbench, Crucible, Alchemical Furnace и Infusion Matrix.", "Каждое активное remote-действие стоит 4 Praecantatio vis.", "Полное remote GUI opening всё ещё отключено ради безопасности."},
                "Remote Use",
                "Transvector branch теперь получил первые настоящие действия, а не только диагностику."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Transvector Remote Interface",
                "TRANSVECTOR_REMOTE_INTERFACE",
                "Devices",
                "Remote Interaction",
                new String[]{"Transvector Binder теперь записывает цель в Transvector Interface.", "Transvector Interface получил block entity, GUI и сохранение target-позиции.", "Кнопки GUI: Status, Inspect, Clear Target."},
                new String[]{"Inspect безопасно показывает информацию о целевом блоке: название блока, jar essentia, содержимое Essentia Drive или заполненность container slots.", "Полное удалённое открытие чужих GUI отключено в safe pass, чтобы не ломать menu data."},
                "Remote Link",
                "Это первый настоящий transvector pass: связь сохраняется в блоке, а не только в предмете."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Osmotic Enchanter GUI",
                "OSMOTIC_ENCHANTER_GUI",
                "Devices",
                "Selectable Enchanting",
                new String[]{"Osmotic Enchanter теперь открывает GUI.", "В GUI есть кнопки: Unbreaking, Efficiency, Sharpness, Protection, Power, Fortune.", "Каждая кнопка применяет следующий уровень выбранного enchantment к предмету в основной руке."},
                new String[]{"Стоимость зависит от enchantment и следующего уровня.", "Требуются vis в жезле и XP уровни.", "Structure check вынесен в отдельную кнопку."},
                "Enchantment Selection",
                "Теперь Osmotic Enchanter ближе к оригинальной идее выборочного зачарования."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumic Tinkerer",
                "THAUMIC_TINKERER",
                "Addon Compat",
                "Tinkering",
                new String[]{"Thaumic Tinkerer is a remake branch for extra useful Thaumcraft-style items and devices.", "This pass does not copy old code or old textures.", "It starts with core devices and structure logic."},
                new String[]{"Later passes should add more TT tools, enchantments, KAMI / Ichor tier and deeper mechanics."},
                "TT Branch",
                "A new addon branch has been started."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Osmotic Enchanter",
                "OSMOTIC_ENCHANTER",
                "Devices",
                "Structure Enchanting",
                new String[]{"The Osmotic Enchanter requires 6 Obsidian Totem pillars nearby.", "Each pillar must be 2-12 blocks high and capped with Nitor Light.", "Shift-right-click the enchanter to inspect the structure."},
                new String[]{"Right-click with a damageable item to apply Unbreaking step by step.", "Cost: 12 Praecantatio vis and 1 experience level.", "This is the first safe functional pass; full selectable enchantment GUI comes later."},
                "Osmotic Enchanting",
                "A first version of controlled magical enchanting."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "TT Devices",
                "TT_DEVICES",
                "Devices",
                "Platforms / Dissipation",
                new String[]{"Ethereal Platform begins the pass-through platform branch.", "Fume Dissipator clears fire and several harmful effects from the player.", "Both are foundations for later original-style device polish."},
                new String[]{"The platform currently reports behavior, while collision polish will come in a later pass."},
                "Utility Devices",
                "Useful Thaumcraft-flavoured devices have begun."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Transvector Tools",
                "TRANSVECTOR_TOOLS",
                "Devices",
                "Remote Binding",
                new String[]{"Transvector Binder can remember a block position.", "Transvector Interface is the receiving device for future remote interaction.", "This prepares the original-style transvector branch."},
                new String[]{"Right-click a block with the Binder to store its coordinates."},
                "Remote Interaction",
                "The first transvector infrastructure is in place."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Encoded Patterns",
                "ENCODED_PATTERNS",
                "Addon Compat",
                "Pattern NBT",
                new String[]{"Encoded Essentia Pattern теперь хранит тип шаблона в NBT.", "Типы: Empty, Arcane Workbench, Crucible, Infusion.", "ПКМ по pattern или ПКМ pattern по Arcane Pattern Encoder переключает тип."},
                new String[]{"Это ещё не настоящий автокрафт.", "Но теперь есть предмет, который может хранить назначение рецепта."},
                "Pattern Types",
                "Первый шаг к AE2-style autocrafting для Thaumcraft."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Autocrafting Base",
                "AUTOCRAFTING_BASE",
                "Addon Compat",
                "Pattern Provider",
                new String[]{"Добавлен Arcane Pattern Provider.", "Он видит encoded patterns и показывает их тип.", "Будущий pass сможет передавать эти шаблоны в AE2 crafting network."},
                new String[]{"План: Arcane Workbench, Crucible и Infusion autocrafting.", "Co-Processor cards уже подготовлены для ускорения будущих pattern jobs."},
                "Autocrafting Foundation",
                "Thaumic Energistics теперь имеет базу под автоматизацию рецептов."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Terminal Filter",
                "ESSENTIA_TERMINAL_FILTER",
                "Addon Compat",
                "Search / Filter",
                new String[]{"Essentia Terminal теперь имеет выбор aspect и кнопку filtered scan.", "Кнопки < и > переключают aspect.", "Скан aspect показывает количество в jar, Drive и ячейках игрока."},
                new String[]{"Это первый search/filter pass.", "Позже можно добавить строку поиска и список аспектов внутри GUI."},
                "Filtered Search",
                "Терминал стал полезнее для больших сетей essentia."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Drive Network",
                "ESSENTIA_DRIVE_NETWORK",
                "Addon Compat",
                "Drive Network",
                new String[]{"Import Bus теперь может загружать essentia из jar прямо в соседний Essentia Drive.", "Export Bus может выгружать essentia из Drive в соседние jar.", "Interface импортирует без Shift и экспортирует с Shift."},
                new String[]{"Terminal и Storage Monitor теперь читают соседний Drive.", "Если держать ячейку в руке, старый режим работы через held cell всё ещё работает."},
                "Network Pass",
                "Thaumic Energistics стал ближе к настоящей AE2-style сети essentia."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Real Essentia Drive",
                "REAL_ESSENTIA_DRIVE",
                "Addon Compat",
                "Drive Inventory",
                new String[]{"Essentia Drive теперь имеет собственный инвентарь.", "Внутри 10 слотов под Digital Essentia Cells и 4 слота под upgrade cards.", "Кнопка Scan показывает суммы аспектов, capacity и transfer limit."},
                new String[]{"Storage Monitor теперь сначала ищет соседний Essentia Drive.", "Если drive рядом есть, монитор показывает именно его содержимое.", "Это основа под настоящий ME Drive-style блок."},
                "Drive Inventory",
                "Thaumic Energistics получил настоящий накопитель ячеек, а не только скан инвентаря игрока."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Speed Cards",
                "ESSENTIA_SPEED_CARDS",
                "Addon Compat",
                "AE2-style Upgrades",
                new String[]{"Добавлены карты скорости в стиле AE2.", "Essentia Speed Card, Advanced Speed Card и Acceleration Card увеличивают transfer limit у Import Bus, Export Bus и Interface.", "Базовый лимит 64 essentia за действие."},
                new String[]{"Speed Card: +64.", "Advanced Speed Card: +192.", "Acceleration Card: +448.", "Максимум сейчас ограничен 1024 за действие."},
                "Speed Upgrades",
                "Теперь Thaumic Energistics можно ускорять как AE2-механизмы."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Advanced AE2 Cards",
                "ADVANCED_AE2_CARDS",
                "Addon Compat",
                "Co-Processor / Fuzzy",
                new String[]{"Thaumic Co-Processor Card ускоряет сканирование и будет использоваться для будущего автокрафта.", "Essentia Fuzzy Card пока является базой под будущие fuzzy-правила аспектов и рецептов.", "Partition Card теперь может привязывать Digital Essentia Cell к выбранному aspect."},
                new String[]{"Держи Digital Essentia Cell во второй руке и ПКМ Partition Card, чтобы привязать ячейку.", "После привязки ячейка принимает только выбранный aspect."},
                "Advanced Cards",
                "Эта стадия готовит систему к AE2-style upgrades и autocrafting."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Terminal GUI",
                "ESSENTIA_TERMINAL_GUI",
                "Addon Compat",
                "Terminal GUI",
                new String[]{"Essentia Terminal теперь открывает GUI.", "Кнопка 'Скан jar' показывает essentia в соседних jar.", "Кнопка 'Скан ячеек' показывает essentia во всех цифровых ячейках игрока."},
                new String[]{"Это ещё не полноценный AE2-терминал.", "Но теперь есть первый удобный интерфейс вместо одних сообщений по ПКМ."},
                "Digital Terminal",
                "Второй pass делает Thaumic Energistics похожим на настоящую систему хранения."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Drive",
                "ESSENTIA_DRIVE_RESEARCH",
                "Storage",
                "Drive / Monitor",
                new String[]{"Essentia Drive и Storage Monitor сканируют цифровые ячейки в инвентаре.", "Они показывают количество ячеек, общую capacity и суммы аспектов."},
                new String[]{"Позже это может стать настоящим drive block с собственным inventory."},
                "ME-like Drive",
                "Основа для будущего накопителя essentia."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Partitioning",
                "ESSENTIA_PARTITIONING",
                "Storage",
                "Partition Card",
                new String[]{"Partition Card переключает выбранный aspect по ПКМ.", "Пока карта только хранит настройку.", "Следующий pass сможет привязать её к ячейке."},
                new String[]{"Это база под partitioning как в AE2."},
                "Aspect Rules",
                "Ячейки смогут быть настроены под конкретные аспекты."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumic Energistics",
                "THAUMIC_ENERGISTICS",
                "Addon Compat",
                "AE2 Bridge",
                new String[]{"Thaumic Energistics is the bridge branch between Thaumcraft-style essentia and AE2-style digital storage.", "Stage 46 adds a safe first pass without hard AE2 dependency.", "Old code/textures are not copied."},
                new String[]{"Digital cells can move essentia to/from jars.", "Import/export/interface blocks move essentia around adjacent jars."},
                "Digital Essentia",
                "The goal is essentia storage, transportation and later autocrafting integration."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Digital Essentia Cells",
                "DIGITAL_ESSENTIA_STORAGE",
                "Storage",
                "1K / 4K / 16K Cells",
                new String[]{"ПКМ ячейкой по jar: загрузить essentia из jar.", "Shift+ПКМ ячейкой по jar: выгрузить essentia в jar.", "Ячейка хранит один aspect за раз."},
                new String[]{"1K, 4K and 16K capacities are available.", "Later stages can add partitioning and multi-aspect drives."},
                "Essentia Cell",
                "This is the first functional digital essentia storage loop."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Buses",
                "ESSENTIA_BUSES",
                "Automation",
                "Import / Export / Interface",
                new String[]{"Import Bus pulls essentia from adjacent jars into a held cell.", "Export Bus pushes essentia from a held cell into adjacent jars.", "Interface imports normally and exports with Shift."},
                new String[]{"Storage Bus shows cell status.", "Terminal reports held cell and adjacent jars."},
                "Digital Transport",
                "These blocks imitate the first layer of AE-style essentia logistics."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Arcane Pattern Encoding",
                "ARCANE_PATTERN_ENCODING",
                "Autocrafting",
                "Pattern Encoder",
                new String[]{"Arcane Pattern Encoder now writes a pattern type into Encoded Essentia Pattern NBT.", "It can encode Arcane Workbench, Crucible and Infusion pattern types as an autocrafting base.", "Stage 46 only adds the base block and item."},
                new String[]{"Full AE2 API integration should be a later dedicated pass."},
                "Future Automation",
                "This prepares the path for real automated Thaumcraft crafting."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Pech Trade GUI",
                "PECH_TRADE_GUI",
                "Addon Compat",
                "Pech Menu",
                new String[]{"ПКМ по Печу пустой рукой или неподходящим предметом открывает меню торговли.", "Кнопки T1-T5 ищут нужный Pech Trade Token в инвентаре.", "Кнопка подарка использует предмет в основной руке."},
                new String[]{"Обмен всё ещё можно делать напрямую ПКМ жетоном по Печу.", "Меню удобнее, когда жетоны лежат в инвентаре."},
                "Trade Interface",
                "Печ получил первое полноценное меню торговли без ручного перебора жетонов."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Pech Favor",
                "PECH_FAVOR",
                "Addon Compat",
                "Pech Ledger",
                new String[]{"Печи теперь принимают подарки и повышают репутацию игрока.", "Подарки: изумруд, золото, кристалл опыта, Ignis Fuel, Quicksilver, Eldritch Relic.", "Высокая репутация даёт шанс бонусной награды при торговле."},
                new String[]{"Pech Ledger показывает текущую репутацию.", "Печи бывают разных типов: торговец, охотник и древний Печ.", "Тип Печа может влиять на награды."},
                "Favor System",
                "Теперь торговля с Печами стала отдельной маленькой системой прогрессии."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Real Pech Trade",
                "REAL_PECH_TRADE",
                "Addon Compat",
                "Pech Entity",
                new String[]{"Фокус призыва Печа теперь создаёт отдельную сущность PechEntity.", "Печ больше не Wandering Trader placeholder.", "Дай Печу Pech Trade Token через ПКМ, и он выдаст награду по уровню жетона."},
                new String[]{"Tier 1 подходит для ранней торговли.", "Tier 5 может выдавать endgame-награды.", "Полноценное GUI торговли можно добавить позже."},
                "Pech Trading",
                "Этот этап превращает Pech Trade из placeholder в рабочую сущность с прямым обменом."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumcraft Extras Complete",
                "THAUMCRAFT_EXTRAS_COMPLETE",
                "Addon Compat",
                "Second Pass",
                new String[]{"Фокусы теперь тратят vis из любого заряженного жезла в инвентаре.", "Жетоны торговли Печа теперь можно использовать ПКМ и получать награды.", "Кристалл опыта можно использовать для возврата 1 уровня.", "Ignis Fuel теперь работает как топливо для Alchemical Furnace."},
                new String[]{"Exchange и Smelting получили больше правил.", "Pech GUI и настоящий Pech entity всё ещё можно сделать отдельным stage."},
                "Extras Complete Pass",
                "Второй проход делает аддон более играбельным, а не просто набором предметов."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumcraft Extras Compat",
                "THAUMCRAFT_EXTRAS",
                "Addon Compat",
                "Thaumcraft Extras",
                new String[]{"This is a remake/compatibility pass inspired by the old Thaumcraft Extras addon.", "No old code or old textures are copied.", "The goal is to bring the gameplay ideas into the 1.19.2 rebuild."},
                new String[]{"Adds foci, elemental blocks, Pech trade tokens and XP extraction.", "Later stages can deepen each system."},
                "Extras Branch",
                "Compatibility branch for the old Thaumcraft Extras concept."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Extra Wand Foci",
                "THAUMCRAFT_EXTRAS_FOCI",
                "Wands",
                "Arcane Workbench",
                new String[]{"Blink teleports forward.", "Arrow shoots an arrow.", "Heal restores health.", "Speed gives speed.", "Return teleports home.", "Dispel removes effects.", "Destroy erases blocks without drops.", "Freeze slows nearby creatures."},
                new String[]{"Exchange and Smelting work on a limited mapping first.", "Some foci add Warp because they bend rules too hard."},
                "Foci Collection",
                "A first functional pass of the classic extra focus set."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Pech Trade",
                "THAUMCRAFT_EXTRAS_PECH",
                "Items",
                "Pech Trade Tokens",
                new String[]{"Five tiers of Pech Trade tokens were added.", "Focus Pech Summon now calls the custom Pech entity.", "Pech trading now has entity interaction, favor and a basic GUI."},
                new String[]{"Tier 5 uses a Primordial Pearl.", "This is intentionally expensive."},
                "Trade Currency",
                "The first pass prepares a Pech economy branch."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Experience Extraction",
                "THAUMCRAFT_EXTRAS_EXPERIENCE",
                "Items",
                "Experience Extractor",
                new String[]{"Experience Extractor drains one player level into an Experience Shard.", "Focus Experience does the same as a wand focus."},
                new String[]{"Later this can feed infusion, arcane crafting or Pech trades."},
                "Crystallized XP",
                "XP becomes a magical crafting resource."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Elemental Blocks",
                "THAUMCRAFT_EXTRAS_ELEMENTAL_BLOCKS",
                "Blocks",
                "Fire/Air/Water/Earth/Light/Ender",
                new String[]{"Fire burns entities.", "Air gives levitation.", "Water clears fire and gives water breathing.", "Earth slows.", "Light glows.", "Ender randomly teleports on use."},
                new String[]{"Research Cache exists as a creative/testing placeholder."},
                "Aspect Blocks",
                "Functional remake blocks for the old elemental block idea."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Progression Guide",
                "FIRST_STEPS",
                "Guide",
                "Thaumonomicon",
                new String[]{"1. Thaumometer and scans.", "2. Thaumonomicon and tables.", "3. Arcane Workbench and wands.", "4. Crucible, essentia and tubes.", "5. Infusion and Warp.", "6. Eldritch portal and arena."},
                new String[]{"Use research requirements as the main route.", "Later systems depend on earlier original-style blocks."},
                "Main Progression",
                "Stage 36 cleans the research chain so the mod has a clearer playable route."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Balance Config",
                "BALANCE_CONFIG",
                "Config",
                "thaumcraft-common.toml",
                new String[]{"Warp event chance and cooldown are configurable.", "Infusion duration is configurable.", "Essentia tube speed/network size are configurable.", "Eldritch portal stability/cooldown are configurable."},
                new String[]{"The file appears in config after first launch.", "Server owners can tune the whole progression."},
                "Tuning",
                "This is the first release-candidate tuning pass."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Playable RC Notes",
                "PLAYABLE_RC",
                "Release Candidate",
                "Audit",
                new String[]{"The current route is playable from scanning to Eldritch arena.", "Not every TC4 system is complete.", "Textures and models still need a dedicated polish pass."},
                new String[]{"Use only one stage jar.", "Delete older stage jars.", "Report compile/runtime errors with full log."},
                "RC Pass",
                "The next stages should focus on fixing, polishing and packaging rather than adding huge new systems."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumonomicon",
                "FIRST_STEPS",
                "Wand Transformation",
                "Wand",
                new String[]{"Right-click a Bookshelf with any wand."},
                new String[]{},
                "Thaumonomicon",
                "This follows the original Thaumcraft progression: the book is not a normal crafting-table recipe."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Arcane Workbench",
                "ARCANE_WORKBENCH",
                "Wand Transformation",
                "Wand",
                new String[]{"Right-click a Thaumcraft Table with any wand."},
                new String[]{},
                "Arcane Workbench",
                "Original-style logic: use a Thaumcraft Table, not a vanilla Crafting Table."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Crucible",
                "CRUCIBLE",
                "Wand Transformation",
                "Wand",
                new String[]{"Right-click a Cauldron with any wand."},
                new String[]{},
                "Crucible",
                "The Crucible is the base block for catalyst-based alchemy."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Aura Node",
                "AURA_NODES",
                "World Aura",
                "Wand",
                new String[]{"Find or place an Aura Node.", "Right-click the node with a wand to draw vis."},
                new String[]{"Primal vis stored in the node"},
                "Charged Wand",
                "This is the first rebuilding step for the original aura and vis system. Natural node worldgen comes next."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Node Types",
                "NODE_TYPES",
                "World Aura",
                "Aura Node",
                new String[]{"Normal Node: basic vis source.", "Pure Node: can cleanse taint.", "Tainted Node: can spread taint.", "Hungry Node: pulls dropped items."},
                new String[]{"Different nodes store and regenerate different vis."},
                "Aura Knowledge",
                "This is the first pass of original-style special aura nodes."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Node Stabilizer",
                "NODE_STABILIZATION",
                "Aura Device",
                "Infusion Core",
                new String[]{"Thaumium Ingot x4", "Balanced Shard x4", "Place within 5 blocks of a node."},
                new String[]{"Calms tainted/hungry effects", "Slows wand charging"},
                "Node Stabilizer",
                "Stabilizers reduce dangerous node effects. They are not a replacement for proper node handling."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Research Table",
                "FIRST_STEPS",
                "Table Setup",
                "Scribing Tools",
                new String[]{"Place two Thaumcraft Tables side by side.", "Right-click one table with Scribing Tools."},
                new String[]{},
                "Research Table",
                "The full research minigame is still being rebuilt, but the research interface already exists."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Iron Capped Wooden Wand",
                "FIRST_STEPS",
                "Normal Crafting",
                "Wooden Wand Core",
                new String[]{"Iron Wand Cap", "Wooden Wand Core", "Iron Wand Cap"},
                new String[]{},
                "Iron Capped Wooden Wand",
                "Basic starter wand. Caps and core are separate items, matching original-style progression."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "JSON Arcane Recipes",
                "ARCANE_WORKBENCH",
                "Arcane Workbench",
                "Server JSON reload",
                new String[]{"Recipes are loaded from data/thaumcraft/thaumcraft_arcane_workbench/.", "Server sends recipe list to client when the workbench opens."},
                new String[]{"Client GUI uses synced recipe data when available."},
                "Dynamic Arcane Recipes",
                "This connects the real JSON recipe system to the client recipe interface."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Arcane Workbench GUI",
                "ARCANE_WORKBENCH",
                "Arcane Workbench",
                "Empty hand",
                new String[]{"Right-click Arcane Workbench with empty hand.", "Select recipe page.", "Press Craft."},
                new String[]{"Uses Ordo vis from any wand in inventory."},
                "Arcane Crafting Interface",
                "The Arcane Workbench now uses the shared Arcane Recipe Book data for GUI pages and Thaumonomicon explanations."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumometer",
                "FIRST_STEPS",
                "Arcane Workbench",
                "Glass Pane",
                new String[]{"Gold Ingot", "Gold Ingot", "Aer Shard", "Terra Shard"},
                new String[]{},
                "Thaumometer",
                "Hold the catalyst and use the Arcane Workbench. Sneak-right-click previews recipes."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Goggles of Revealing",
                "PRIMAL_ASPECTS",
                "Arcane Workbench",
                "Leather Helmet",
                new String[]{"Thaumometer", "Gold Ingot", "Gold Ingot", "Ordo Shard", "Aer Shard"},
                new String[]{},
                "Goggles of Revealing",
                "A classic utility item for thaumaturges."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Thaumium Ingot",
                "THAUMIUM",
                "Crucible Alchemy",
                "Iron Ingot",
                new String[]{"Dissolve magical items to fill the Crucible with aspects."},
                new String[]{"Praecantatio 4"},
                "Thaumium Ingot",
                "Throw/dissolve aspects first, then use Iron Ingot as the catalyst."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Nitor",
                "CRUCIBLE",
                "Crucible Alchemy",
                "Glowstone Dust",
                new String[]{"Fill the Crucible with required aspects."},
                new String[]{"Ignis 3", "Potentia 3", "Lux 3"},
                "Nitor",
                "Nitor follows the original idea: bright magical flame from fire, energy and light."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Alchemy Dust",
                "CRUCIBLE",
                "Crucible Alchemy",
                "Redstone",
                new String[]{"Fill the Crucible with required aspects."},
                new String[]{"Praecantatio 2", "Ordo 1", "Perditio 1"},
                "Alchemy Dust",
                "Temporary helper reagent for this rebuild."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Warp Events",
                "WARP_EVENTS",
                "Warp",
                "Forbidden magic",
                new String[]{"Warp now triggers events over time.", "Low warp causes whispers and confusion.", "Higher warp can spawn taint creatures or taint nearby blocks.", "Very high warp can unlock eldritch whispers."},
                new String[]{"Use Warp Ward Talisman to reduce event chance.", "Use Sanity Soap to reduce warp slightly and delay events.", "Warp Charm still removes more warp."},
                "Mind Fracture",
                "Warp is no longer just a number. It now actively pushes back against the player."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Warp Warding",
                "WARP_WARDING",
                "Warp",
                "Warp Ward Talisman",
                new String[]{"Warp Ward Talisman gives 5 minutes of protection.", "Sanity Soap removes 1 warp and delays events.", "Warp Charm removes stronger warp chunks."},
                new String[]{"Ward lowers event chance.", "It does not remove warp by itself.", "Danger grows at higher warp levels."},
                "Protection",
                "Warp protection is temporary. The best protection is still careful thaumaturgy."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Eldritch Arena",
                "ELDRITCH_ARENA",
                "Eldritch",
                "Eldritch Portal",
                new String[]{"Use Crimson Key on an Eldritch Portal.", "The portal creates a temporary arena around itself.", "Guardian waves appear over time.", "Survive until the portal stabilizes."},
                new String[]{"Portal has cooldown.", "Stability can collapse.", "Rewards appear at the portal when complete."},
                "Arena From Beyond",
                "This is a placeholder for later Eldritch dungeon/dimension gameplay."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Eldritch Guardian",
                "ELDRITCH_GUARDIAN",
                "Eldritch",
                "Guardian Waves",
                new String[]{"Sentinels and wisps appear from the portal.", "The final wave contains stronger guardians.", "Completion drops Eldritch Relic and sometimes Guardian Core."},
                new String[]{"Unstable portals add Warp.", "Cooldown prevents immediate farming.", "Awakened Crimson Key improves stability."},
                "Guardian Encounter",
                "The first combat-focused Eldritch progression step."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Awakened Crimson Key",
                "AWAKENED_CRIMSON_KEY",
                "Eldritch",
                "Crimson Key Upgrade",
                new String[]{"Craft from Crimson Key, Eldritch Relic, Guardian Core and Primordial Pearl.", "Using it on a portal starts a more stable encounter."},
                new String[]{"Higher portal stability.", "Lower Warp gain.", "Later it will unlock deeper structures."},
                "Stable Opening",
                "A better key for deeper Eldritch progression."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Eldritch Altar",
                "ELDRITCH_ALTAR",
                "Eldritch",
                "Eldritch Altar",
                new String[]{"Build an altar from Eldritch Stone.", "Place at least 4 Eldritch Obelisks around it.", "Use Eldritch Eye or Crimson Key on the altar.", "Requires Eldritch Whispers and enough Warp."},
                new String[]{"The portal appears above the altar.", "The ritual adds Warp.", "An Enderman sentinel may appear."},
                "The First Door",
                "This is the first functional Eldritch progression step. Later stages will add proper dimensions/structures."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Crimson Key",
                "CRIMSON_KEY",
                "Eldritch",
                "Crimson Key",
                new String[]{"Crafted from Void Metal and Eldritch Eye.", "Can open a valid Eldritch Altar structure.", "A stronger key system will come later."},
                new String[]{"Consumes the key.", "Adds Warp.", "Calls something through the door."},
                "Forced Opening",
                "A crude key for a door that should not exist."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Eldritch Whispers",
                "ELDRITCH_WHISPERS",
                "Eldritch",
                "Eldritch Eye",
                new String[]{"At high warp, forbidden thoughts begin to appear.", "Using Eldritch Eye increases attunement.", "Repeated exposure unlocks deeper Eldritch progression."},
                new String[]{"Requires enough Warp to awaken.", "Adds more Warp when used.", "This is only the first Eldritch step."},
                "Beyond",
                "Something has noticed you. Later stages will add Eldritch structures, portals and enemies."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Control",
                "ESSENTIA_CONTROL",
                "Essentia",
                "Essentia Valve + Jar Label",
                new String[]{"Essentia Valve behaves like a tube but closes when powered by redstone.", "A label locks a normal or void jar to one essentia aspect.", "Sneak-use the labelled face to remove the label."},
                new String[]{"Redstone can stop flow.", "Filtered jars can be configured before filling.", "Overlay now shows jar type and suction."},
                "Controlled Essentia Network",
                "This stage makes tube networks easier to control and debug."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Suction",
                "ESSENTIA_SUCTION",
                "Essentia",
                "Labelled Jar / Void Jar",
                new String[]{"Normal jar suction is 32, or 64 while labelled.", "A labelled void jar uses suction 48 until full, then 32 while voiding overflow.", "Tube networks choose the strongest compatible destination."},
                new String[]{"Filtered jars lock to one aspect.", "Void jars destroy overflow.", "Furnace now uses fuel and burn progress."},
                "Controlled Essentia Flow",
                "This is the first real suction-priority pass for essentia transport."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Essentia Transport",
                "ESSENTIA_TRANSPORT",
                "Essentia",
                "Alchemical Furnace + Essentia Tube",
                new String[]{"Burn items in the Alchemical Furnace to extract essentia.", "Connect the furnace to Essentia Jars with Essentia Tubes.", "Tubes move one essentia at a time into compatible jars."},
                new String[]{"Jars accept only one aspect type.", "Tube network range is limited.", "This is the first basic suction/transport pass."},
                "Stored Essentia",
                "This begins the original-style essentia automation path: furnace, tubes, jars and later suction."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Infusion Visuals",
                "TIMED_INFUSION",
                "Infusion",
                "Active Matrix",
                new String[]{"Active infusion now draws particles from pedestals and jars.", "Looking at an active matrix shows a progress overlay.", "Instability has more visible event types."},
                new String[]{"Pedestal beams", "Jar essentia beams", "Progress overlay", "Instability surges"},
                "Readable Ritual",
                "This stage makes infusion easier to read visually while keeping server-side ritual validation."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Timed Infusion",
                "INFUSION_STABILITY",
                "Infusion",
                "Wand",
                new String[]{"Start infusion with a wand.", "The process now runs over time.", "Use empty hand to inspect progress.", "Shift + empty hand cancels active infusion."},
                new String[]{"Instability events can happen during the process.", "Final validation happens when infusion completes."},
                "Active Infusion",
                "This is closer to the original Thaumcraft feeling: infusion is now a risky ritual, not instant crafting."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Infusion Altar Structure",
                "INFUSION",
                "Infusion",
                "Infusion Matrix",
                new String[]{"Matrix above a center pedestal.", "Components on nearby Arcane Pedestals.", "Symmetrical pedestal layout lowers instability.", "Arcane Stone Bricks and Node Stabilizers increase stability."},
                new String[]{"Bad symmetry raises instability.", "High instability can add warp, taint ground or throw components."},
                "Stable Infusion",
                "Use empty hand on the matrix to inspect altar stability before starting an infusion."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Infusion Core",
                "INFUSION",
                "Infusion",
                "Balanced Shard",
                new String[]{"Thaumium Plate", "Nitor", "Alchemy Dust", "Ordo Shard"},
                new String[]{"Praecantatio 6", "Ordo 4", "Aer 2"},
                "Infusion Core",
                "Place catalyst on the center pedestal, components on nearby pedestals, and essentia in jars."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Void Metal Ingot",
                "INFUSION",
                "Infusion",
                "Thaumium Ingot",
                new String[]{"Flux Crystal", "Perditio Shard", "Alchemy Dust", "Obsidian"},
                new String[]{"Perditio 6", "Vacuos 4", "Praecantatio 4"},
                "Void Metal Ingot",
                "A more unstable infusion recipe."
        ));

        PAGES.add(new ThaumonomiconRecipePage(
                "Golem Core",
                "GOLEMS",
                "Arcane Workbench",
                "Infusion Core",
                new String[]{"Thaumium Ingot", "Balanced Shard", "Ordo Shard", "Pumpkin"},
                new String[]{},
                "Golem Core",
                "Requires Golemancy research before use."
        ));
    }

    private ThaumonomiconRecipeRegistry() {
    }

    public static List<ThaumonomiconRecipePage> pages() {
        return Collections.unmodifiableList(PAGES);
    }

    public static ThaumonomiconRecipePage get(int index) {
        if (PAGES.isEmpty()) {
            return null;
        }

        int safe = Math.max(0, Math.min(index, PAGES.size() - 1));
        return PAGES.get(safe);
    }

    public static int size() {
        return PAGES.size();
    }
}
