package com.darkifov.thaumcraft;

import com.darkifov.thaumcraft.block.AvaritiaCreativeWandItem;
import com.darkifov.thaumcraft.wand.WandRodType;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipeManager;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipeManager;
import com.darkifov.thaumcraft.block.AddonCompletionLedgerItem;
import com.darkifov.thaumcraft.block.AdvancedNodeStabilizerBlock;
import com.darkifov.thaumcraft.block.AlchemicalFurnaceBlock;
import com.darkifov.thaumcraft.block.AlchemicalCentrifugeBlock;
import com.darkifov.thaumcraft.block.DeconstructionTableBlock;
import com.darkifov.thaumcraft.block.EssentiaCrystalizerBlock;
import com.darkifov.thaumcraft.block.EssentiaCrystalItem;
import com.darkifov.thaumcraft.block.AlembicBlock;
import com.darkifov.thaumcraft.block.EssentiaReservoirBlock;
import com.darkifov.thaumcraft.block.ThaumatoriumBlock;
import com.darkifov.thaumcraft.block.ArcanePedestalBlock;
import com.darkifov.thaumcraft.block.AspectCrystalItem;
import com.darkifov.thaumcraft.block.ArcaneWorkbenchBlock;
import com.darkifov.thaumcraft.block.AuraNodeBlock;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.block.CrucibleBlock;
import com.darkifov.thaumcraft.block.FilteredEssentiaJarBlock;
import com.darkifov.thaumcraft.block.EldritchAltarBlock;
import com.darkifov.thaumcraft.block.EldritchTrapBlock;
import com.darkifov.thaumcraft.block.EldritchLockBlock;
import com.darkifov.thaumcraft.block.EldritchCrystalBlock;
import com.darkifov.thaumcraft.block.EldritchCapBlock;
import com.darkifov.thaumcraft.block.EldritchCrabSpawnerBlock;
import com.darkifov.thaumcraft.block.EldritchNothingBlock;
import com.darkifov.thaumcraft.block.TC4LootBlock;
import com.darkifov.thaumcraft.block.TC4CrimsonPlateArmorItem;
import com.darkifov.thaumcraft.block.EldritchEyeItem;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.block.EssentiaTubeBlock;
import com.darkifov.thaumcraft.block.EldritchPortalBlock;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.ExperienceShardItem;
import com.darkifov.thaumcraft.block.ExperienceExtractorItem;
import com.darkifov.thaumcraft.block.EssentiaDriveBlock;
import com.darkifov.thaumcraft.block.EssentiaCellItem;
import com.darkifov.thaumcraft.block.EssentiaPartitionCardItem;
import com.darkifov.thaumcraft.block.EncodedEssentiaPatternItem;
import com.darkifov.thaumcraft.block.BottomlessPouchItem;
import com.darkifov.thaumcraft.block.FumeDissipatorBlock;
import com.darkifov.thaumcraft.block.FluxGasBlock;
import com.darkifov.thaumcraft.block.FluxGooBlock;
import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.FocusPouchBaubleItem;
import com.darkifov.thaumcraft.block.FocalManipulatorBlock;
import com.darkifov.thaumcraft.block.HelmetOfRevealingItem;
import com.darkifov.thaumcraft.block.IchorArmorItem;
import com.darkifov.thaumcraft.block.IchorPickaxeItem;
import com.darkifov.thaumcraft.block.IchorSwordItem;
import com.darkifov.thaumcraft.block.IchorGearItem;
import com.darkifov.thaumcraft.block.KamiResearchCoreItem;
import com.darkifov.thaumcraft.block.MatrixAuxiliaryBlock;
import com.darkifov.thaumcraft.block.EtherealPlatformBlock;
import com.darkifov.thaumcraft.block.ElectricShockBlock;
import com.darkifov.thaumcraft.block.EssentiaUpgradeCardItem;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.block.GogglesOfRevealingItem;
import com.darkifov.thaumcraft.block.GolemBellItem;
import com.darkifov.thaumcraft.block.JarLabelItem;
import com.darkifov.thaumcraft.block.GolemSealCollectItem;
import com.darkifov.thaumcraft.block.GolemCoreItem;
import com.darkifov.thaumcraft.block.GolemFilterItem;
import com.darkifov.thaumcraft.block.GolemDecorationItem;
import com.darkifov.thaumcraft.block.GolemTaskMarkerItem;
import com.darkifov.thaumcraft.block.GolemUpgradeItem;
import com.darkifov.thaumcraft.block.InfusionMatrixBlock;
import com.darkifov.thaumcraft.block.InfusionMatrixAuxiliaryBlock;
import com.darkifov.thaumcraft.block.PechLedgerItem;
import com.darkifov.thaumcraft.block.PechTradeTokenItem;
import com.darkifov.thaumcraft.block.NodeJarItem;
import com.darkifov.thaumcraft.block.NodeStabilizerBlock;
import com.darkifov.thaumcraft.block.NodeTransducerBlock;
import com.darkifov.thaumcraft.block.NitorLightBlock;
import com.darkifov.thaumcraft.block.NitorItem;
import com.darkifov.thaumcraft.block.VisRelayBlock;
import com.darkifov.thaumcraft.block.PortingLedgerItem;
import com.darkifov.thaumcraft.block.TableBlock;
import com.darkifov.thaumcraft.block.TC4SaplingBlock;
import com.darkifov.thaumcraft.block.ShardItem;
import com.darkifov.thaumcraft.block.ResearchTableBlock;
import com.darkifov.thaumcraft.block.SanitySoapItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ResearchPointItem;
import com.darkifov.thaumcraft.block.TaintSeedItem;
import com.darkifov.thaumcraft.block.VoidEssentiaJarBlock;
import com.darkifov.thaumcraft.block.WirelessEssentiaTerminalItem;
import com.darkifov.thaumcraft.block.TaintedSoilBlock;
import com.darkifov.thaumcraft.block.TaintBlock;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import com.darkifov.thaumcraft.block.ThaumicEnergisticsCardItem;
import com.darkifov.thaumcraft.block.ThaumicEnergisticsDeviceBlock;
import com.darkifov.thaumcraft.block.ThaumicEnergisticsUtilityItem;
import com.darkifov.thaumcraft.block.ThaumicAeGridToolItem;
import com.darkifov.thaumcraft.block.ThaumicTinkererUtilityItem;
import com.darkifov.thaumcraft.block.ThaumicTinkererDeviceBlock;
import com.darkifov.thaumcraft.block.ThaumcraftExtrasParityBlock;
import com.darkifov.thaumcraft.block.ThaumcraftExtrasParityItem;
import com.darkifov.thaumcraft.block.ThaumicTinkererParityBlock;
import com.darkifov.thaumcraft.block.ThaumicTinkererParityItem;
import com.darkifov.thaumcraft.block.TransvectorInterfaceBlock;
import com.darkifov.thaumcraft.block.TransvectorBinderItem;
import com.darkifov.thaumcraft.block.ThaumcraftExtrasFocusItem;
import com.darkifov.thaumcraft.block.ThaumcraftExtrasElementalBlock;
import com.darkifov.thaumcraft.block.ThaumometerItem;
import com.darkifov.thaumcraft.block.ThaumonomiconItem;
import com.darkifov.thaumcraft.block.TemporaryHoleBlock;
import com.darkifov.thaumcraft.block.WardedBlock;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.block.WarpWardTalismanItem;
import com.darkifov.thaumcraft.block.WarpCharmItem;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.blockentity.DeconstructionTableBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaCrystalizerBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import com.darkifov.thaumcraft.blockentity.EldritchPortalBlockEntity;
import com.darkifov.thaumcraft.blockentity.EldritchCrabSpawnerBlockEntity;
import com.darkifov.thaumcraft.blockentity.EldritchCrystalBlockEntity;
import com.darkifov.thaumcraft.blockentity.EldritchTrapBlockEntity;
import com.darkifov.thaumcraft.blockentity.EldritchLockBlockEntity;
import com.darkifov.thaumcraft.blockentity.EldritchCapBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import com.darkifov.thaumcraft.blockentity.FocalManipulatorBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.blockentity.TemporaryHoleBlockEntity;
import com.darkifov.thaumcraft.blockentity.WardedBlockEntity;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.entity.CrimsonCultistEntity;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.entity.EldritchCrabEntity;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.entity.MindSpiderEntity;
import com.darkifov.thaumcraft.entity.CultistPortalEntity;
import com.darkifov.thaumcraft.entity.TaintacleGiantEntity;
import com.darkifov.thaumcraft.entity.TaintacleSmallEntity;
import com.darkifov.thaumcraft.entity.TaintacleEntity;
import com.darkifov.thaumcraft.entity.PechEntity;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.darkifov.thaumcraft.entity.TC4FireBatEntity;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4EmberEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4EldritchOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ExplosiveOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4FrostShardEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4GolemOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PrimalOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PechBlastEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ShockOrbEntity;
import com.darkifov.thaumcraft.infusion.InfusionRecipeManager;
import com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import com.darkifov.thaumcraft.menu.EssentiaDriveMenu;
import com.darkifov.thaumcraft.menu.EssentiaTerminalMenu;
import com.darkifov.thaumcraft.menu.BottomlessPouchMenu;
import com.darkifov.thaumcraft.menu.FocusPouchMenu;
import com.darkifov.thaumcraft.menu.FocalManipulatorMenu;
import com.darkifov.thaumcraft.menu.GolemMenu;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import com.darkifov.thaumcraft.menu.OsmoticEnchanterMenu;
import com.darkifov.thaumcraft.menu.TransvectorInterfaceMenu;
import com.darkifov.thaumcraft.menu.PechTradeMenu;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.menu.DeconstructionTableMenu;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4RegistryGarbageGuard;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.recipe.CountedSmeltingRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.Map;

@Mod(ThaumcraftMod.MOD_ID)
public class ThaumcraftMod {
    public static final String MOD_ID = "thaumcraft";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<RecipeSerializer<SmeltingRecipe>> COUNTED_SMELTING =
            RECIPE_SERIALIZERS.register("counted_smelting", CountedSmeltingRecipeSerializer::new);

    public static final CreativeModeTab THAUMCRAFT_TAB = new CreativeModeTab("thaumcraft") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(THAUMONOMICON.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);
            items.removeIf(TC4RegistryGarbageGuard::isHiddenFromCreative);
        }
    };

    public static final RegistryObject<Item> ADDON_COMPLETION_LEDGER = specialItem("addon_completion_ledger",
            () -> new AddonCompletionLedgerItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> THAUMONOMICON = specialItem("thaumonomicon",
            () -> new ThaumonomiconItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> PORTING_LEDGER = specialItem("porting_ledger",
            () -> new PortingLedgerItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> NODE_JAR = specialItem("node_jar",
            () -> new NodeJarItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1)));

    public static final RegistryObject<Item> IRON_CAPPED_WOODEN_WAND = specialItem("iron_capped_wooden_wand",
            () -> new WandItem(new Item.Properties().tab(THAUMCRAFT_TAB), 50, WandRodType.WOOD, WandCapType.IRON));

    public static final RegistryObject<Item> GREATWOOD_WAND = specialItem("greatwood_wand",
            () -> new WandItem(new Item.Properties().tab(THAUMCRAFT_TAB), 150, WandRodType.GREATWOOD, WandCapType.GOLD));

    public static final RegistryObject<Item> SILVERWOOD_WAND = specialItem("silverwood_wand",
            () -> new WandItem(new Item.Properties().tab(THAUMCRAFT_TAB), 300, WandRodType.SILVERWOOD, WandCapType.THAUMIUM));

    public static final RegistryObject<Item> AVARITIA_CREATIVE_WAND = specialItem("avaritia_creative_wand",
            () -> new AvaritiaCreativeWandItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1)));

    public static final RegistryObject<Item> THAUMOMETER = specialItem("thaumometer",
            () -> new ThaumometerItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> GOGGLES_OF_REVEALING = specialItem("goggles_of_revealing",
            () -> new GogglesOfRevealingItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> RESEARCH_NOTE = specialItem("research_note",
            () -> new ResearchNoteItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ESSENTIA_CRYSTAL = specialItem("tc4_crystalessence",
            () -> new EssentiaCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> RESEARCH_POINT = specialItem("research_point",
            () -> new ResearchPointItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> SCRIBING_TOOLS = specialItem("scribing_tools",
            () -> new ScribingToolsItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final Map<String, RegistryObject<Item>> TC4_RESEARCH_ITEMS = TC4ResearchItems.registerAll(
            ITEMS,
            THAUMCRAFT_TAB,
            Map.of("tc4_crystalessence", ESSENTIA_CRYSTAL)
    );
    public static final Map<String, RegistryObject<SoundEvent>> TC4_SOUND_EVENTS = TC4Sounds.registerAll(SOUND_EVENTS);

    public static final RegistryObject<Item> IRON_WAND_CAP = item("iron_wand_cap");
    public static final RegistryObject<Item> GOLD_WAND_CAP = item("gold_wand_cap");
    public static final RegistryObject<Item> THAUMIUM_WAND_CAP = item("thaumium_wand_cap");
    public static final RegistryObject<Item> WOODEN_WAND_CORE = item("wooden_wand_core");
    public static final RegistryObject<Item> GREATWOOD_WAND_CORE = item("greatwood_wand_core");
    public static final RegistryObject<Item> SILVERWOOD_WAND_CORE = item("silverwood_wand_core");

    public static final RegistryObject<Item> AER_SHARD = specialItem("aer_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.AER, false));
    public static final RegistryObject<Item> TERRA_SHARD = specialItem("terra_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.TERRA, false));
    public static final RegistryObject<Item> IGNIS_SHARD = specialItem("ignis_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.IGNIS, false));
    public static final RegistryObject<Item> AQUA_SHARD = specialItem("aqua_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.AQUA, false));
    public static final RegistryObject<Item> ORDO_SHARD = specialItem("ordo_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.ORDO, false));
    public static final RegistryObject<Item> PERDITIO_SHARD = specialItem("perditio_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.PERDITIO, false));
    public static final RegistryObject<Item> BALANCED_SHARD = specialItem("balanced_shard", () -> new ShardItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.PRAECANTATIO, true));
    public static final RegistryObject<Item> VITREUS_SHARD = specialItem("vitreus_shard", () -> new ShardItem(new Item.Properties(), Aspect.VITREUS, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> METALLUM_SHARD = specialItem("metallum_shard", () -> new ShardItem(new Item.Properties(), Aspect.METALLUM, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> PRAECANTATIO_SHARD = specialItem("praecantatio_shard", () -> new ShardItem(new Item.Properties(), Aspect.PRAECANTATIO, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> VACUOS_SHARD = specialItem("vacuos_shard", () -> new ShardItem(new Item.Properties(), Aspect.VACUOS, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> HERBA_SHARD = specialItem("herba_shard", () -> new ShardItem(new Item.Properties(), Aspect.HERBA, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> LUX_SHARD = specialItem("lux_shard", () -> new ShardItem(new Item.Properties(), Aspect.LUX, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> POTENTIA_SHARD = specialItem("potentia_shard", () -> new ShardItem(new Item.Properties(), Aspect.POTENTIA, false)); // Stage205: legacy duplicate hidden from creative; TC4 core exposes primal + balanced shards.
    public static final RegistryObject<Item> SPELLBINDING_CLOTH = item("spellbinding_cloth");
    public static final RegistryObject<Item> OSMOTIC_ENCHANTMENT_FOCUS = item("osmotic_enchantment_focus");
    public static final RegistryObject<Item> TT_RESEARCH_STAMP = item("tt_research_stamp");
    public static final RegistryObject<Item> TRANSVECTOR_BINDER = specialItem("transvector_binder",
            () -> new TransvectorBinderItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1)));

    public static final RegistryObject<Item> TT_FOCUS_DISLOCATION = ttParityItem("tt_focus_dislocation", ThaumicTinkererParityItem.Mode.FOCUS_DISLOCATION);
    public static final RegistryObject<Item> TT_FOCUS_TELEKINESIS = ttParityItem("tt_focus_telekinesis", ThaumicTinkererParityItem.Mode.FOCUS_TELEKINESIS);
    public static final RegistryObject<Item> TT_FOCUS_FLIGHT = ttParityItem("tt_focus_flight", ThaumicTinkererParityItem.Mode.FOCUS_FLIGHT);
    public static final RegistryObject<Item> TT_FOCUS_DEFLECT = ttParityItem("tt_focus_deflect", ThaumicTinkererParityItem.Mode.FOCUS_DEFLECT);
    public static final RegistryObject<Item> TT_FOCUS_ENDER_CHEST = ttParityItem("tt_focus_ender_chest", ThaumicTinkererParityItem.Mode.FOCUS_ENDER_CHEST);
    public static final RegistryObject<Item> TT_FOCUS_XP_DRAIN = ttParityItem("tt_focus_xp_drain", ThaumicTinkererParityItem.Mode.FOCUS_XP_DRAIN);
    public static final RegistryObject<Item> TT_FOCUS_RECALL = ttParityItem("tt_focus_recall", ThaumicTinkererParityItem.Mode.FOCUS_RECALL);
    public static final RegistryObject<Item> TT_FOCUS_SHADOWBEAM = ttParityItem("tt_focus_shadowbeam", ThaumicTinkererParityItem.Mode.FOCUS_SHADOWBEAM);
    public static final RegistryObject<Item> TT_SKY_PEARL = ttParityItem("tt_sky_pearl", ThaumicTinkererParityItem.Mode.SKY_PEARL);
    public static final RegistryObject<Item> TT_PROTOCLAY = ttParityItem("tt_protoclay", ThaumicTinkererParityItem.Mode.PROTOCLAY);
    public static final RegistryObject<Item> TT_CAT_AMULET = ttParityItem("tt_cat_amulet", ThaumicTinkererParityItem.Mode.CAT_AMULET);
    public static final RegistryObject<Item> TT_PLACEMENT_MIRROR = ttParityItem("tt_placement_mirror", ThaumicTinkererParityItem.Mode.PLACEMENT_MIRROR);
    public static final RegistryObject<Item> TT_ICHOR_POUCH = ttParityItem("tt_ichor_pouch", ThaumicTinkererParityItem.Mode.ICHOR_POUCH);
    public static final RegistryObject<Item> TT_CLEANSING_TALISMAN = ttParityItem("tt_cleansing_talisman", ThaumicTinkererParityItem.Mode.CLEANSING_TALISMAN);
    public static final RegistryObject<Item> TT_XP_TALISMAN = ttParityItem("tt_xp_talisman", ThaumicTinkererParityItem.Mode.XP_TALISMAN);
    public static final RegistryObject<Item> TT_INFUSED_INKWELL = ttParityItem("tt_infused_inkwell", ThaumicTinkererParityItem.Mode.INFUSED_INKWELL);
    public static final RegistryObject<Item> TT_INFUSED_POTION = ttParityItem("tt_infused_potion", ThaumicTinkererParityItem.Mode.INFUSED_POTION);
    public static final RegistryObject<Item> TT_GAS_REMOVER = ttParityItem("tt_gas_remover", ThaumicTinkererParityItem.Mode.GAS_REMOVER);
    public static final RegistryObject<Item> TT_SOUL_MOULD = ttParityItem("tt_soul_mould", ThaumicTinkererParityItem.Mode.SOUL_MOULD);
    public static final RegistryObject<Item> TT_MOB_DISPLAY = ttParityItem("tt_mob_display", ThaumicTinkererParityItem.Mode.MOB_DISPLAY);
    public static final RegistryObject<Item> TT_CONNECTOR = ttParityItem("tt_connector", ThaumicTinkererParityItem.Mode.CONNECTOR);
    public static final RegistryObject<Item> TT_DARK_QUARTZ = ttParityItem("tt_dark_quartz", ThaumicTinkererParityItem.Mode.DARK_QUARTZ);
    public static final RegistryObject<Item> TT_INFUSED_SEED = ttParityItem("tt_infused_seed", ThaumicTinkererParityItem.Mode.INFUSED_SEED);
    public static final RegistryObject<Item> TT_INFUSED_GRAIN = ttParityItem("tt_infused_grain", ThaumicTinkererParityItem.Mode.INFUSED_GRAIN);
    public static final RegistryObject<Item> TT_KAMI_RESOURCE = ttParityItem("tt_kami_resource", ThaumicTinkererParityItem.Mode.KAMI_RESOURCE);
    public static final RegistryObject<Item> TT_ICHOR_AXE = ttParityItem("tt_ichor_axe", ThaumicTinkererParityItem.Mode.ICHOR_TOOL);
    public static final RegistryObject<Item> TT_ICHOR_SHOVEL = ttParityItem("tt_ichor_shovel", ThaumicTinkererParityItem.Mode.ICHOR_TOOL);
    public static final RegistryObject<Item> TT_ICHOR_GEM_HELM = ttParityItem("tt_ichor_gem_helm", ThaumicTinkererParityItem.Mode.ICHOR_GEM_ARMOR);
    public static final RegistryObject<Item> TT_ICHOR_GEM_CHEST = ttParityItem("tt_ichor_gem_chest", ThaumicTinkererParityItem.Mode.ICHOR_GEM_ARMOR);
    public static final RegistryObject<Item> TT_ICHOR_GEM_LEGS = ttParityItem("tt_ichor_gem_legs", ThaumicTinkererParityItem.Mode.ICHOR_GEM_ARMOR);
    public static final RegistryObject<Item> TT_ICHOR_GEM_BOOTS = ttParityItem("tt_ichor_gem_boots", ThaumicTinkererParityItem.Mode.ICHOR_GEM_ARMOR);
    public static final RegistryObject<Item> TT_ICHOR_WAND_ROD = ttParityItem("tt_ichor_wand_rod", ThaumicTinkererParityItem.Mode.ICHOR_WAND_PART);
    public static final RegistryObject<Item> TT_ICHOR_WAND_CAP = ttParityItem("tt_ichor_wand_cap", ThaumicTinkererParityItem.Mode.ICHOR_WAND_PART);
    public static final RegistryObject<Item> TT_BRIGHT_NITOR = ttParityItem("tt_bright_nitor", ThaumicTinkererParityItem.Mode.BRIGHT_NITOR);
    public static final RegistryObject<Item> TT_SPELL_CLOTH = ttParityItem("tt_spell_cloth", ThaumicTinkererParityItem.Mode.SPELL_CLOTH);

    public static final RegistryObject<Item> TCE_MAGIC_WRENCH = tceParityItem("tce_magic_wrench", ThaumcraftExtrasParityItem.Mode.MAGIC_WRENCH);
    public static final RegistryObject<Item> TCE_DARK_THAUMIUM_PICKAXE = tceParityItem("tce_dark_thaumium_pickaxe", ThaumcraftExtrasParityItem.Mode.DARK_THAUMIUM_TOOL);
    public static final RegistryObject<Item> TCE_DARK_THAUMIUM_SHOVEL = tceParityItem("tce_dark_thaumium_shovel", ThaumcraftExtrasParityItem.Mode.DARK_THAUMIUM_TOOL);
    public static final RegistryObject<Item> TCE_DARK_THAUMIUM_SWORD = tceParityItem("tce_dark_thaumium_sword", ThaumcraftExtrasParityItem.Mode.DARK_THAUMIUM_TOOL);
    public static final RegistryObject<Item> TCE_WAND_CAP = tceParityItem("tce_wand_cap", ThaumcraftExtrasParityItem.Mode.WAND_CAP);
    public static final RegistryObject<Item> TCE_WAND_ROD = tceParityItem("tce_wand_rod", ThaumcraftExtrasParityItem.Mode.WAND_ROD);
    public static final RegistryObject<Item> TCE_COLOR_POUCH = tceParityItem("tce_color_pouch", ThaumcraftExtrasParityItem.Mode.COLOR_POUCH);
    public static final RegistryObject<Item> TCE_EMPTY_FOCUS = tceParityItem("tce_empty_focus", ThaumcraftExtrasParityItem.Mode.EMPTY_FOCUS);
    public static final RegistryObject<Item> TCE_INFO_BOOK = tceParityItem("tce_info_book", ThaumcraftExtrasParityItem.Mode.INFO_BOOK);
    public static final RegistryObject<Item> TCE_COMB = tceParityItem("tce_comb", ThaumcraftExtrasParityItem.Mode.COMB);
    public static final RegistryObject<Item> TCE_DARK_CRYSTAL = tceParityItem("tce_dark_crystal", ThaumcraftExtrasParityItem.Mode.DARK_CRYSTAL);
    public static final RegistryObject<Item> TCE_DARK_SHARD = tceParityItem("tce_dark_shard", ThaumcraftExtrasParityItem.Mode.DARK_SHARD);
    public static final RegistryObject<Item> TCE_DARK_NUGGET = tceParityItem("tce_dark_nugget", ThaumcraftExtrasParityItem.Mode.DARK_NUGGET);
    public static final RegistryObject<Item> TCE_API_CRYSTAL = tceParityItem("tce_api_crystal", ThaumcraftExtrasParityItem.Mode.API_CRYSTAL);
    public static final RegistryObject<Item> TCE_API_SHARD = tceParityItem("tce_api_shard", ThaumcraftExtrasParityItem.Mode.API_SHARD);
    public static final RegistryObject<Item> TCE_API_NUGGET = tceParityItem("tce_api_nugget", ThaumcraftExtrasParityItem.Mode.API_NUGGET);

    public static final RegistryObject<Item> TOME_OF_KNOWLEDGE_SHARING = specialItem("tome_of_knowledge_sharing",
            () -> new ThaumicTinkererUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), ThaumicTinkererUtilityItem.Mode.TOME_KNOWLEDGE));
    public static final RegistryObject<Item> INFUSED_SCRIBING_TOOLS = specialItem("infused_scribing_tools",
            () -> new ThaumicTinkererUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), ThaumicTinkererUtilityItem.Mode.INFUSED_SCRIBING_TOOLS));
    public static final RegistryObject<Item> BOTTOMLESS_POUCH = specialItem("bottomless_pouch",
            () -> new BottomlessPouchItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> HELMET_OF_REVEALING = specialItem("helmet_of_revealing",
            () -> new HelmetOfRevealingItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> ICHOR = item("ichor");
    public static final RegistryObject<Item> ICHORCLOTH = item("ichorcloth");
    public static final RegistryObject<Item> ICHORIUM_INGOT = item("ichorium_ingot");
    public static final RegistryObject<Item> KAMI_RESEARCH_CORE = specialItem("kami_research_core",
            () -> new KamiResearchCoreItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ICHOR_PICKAXE = specialItem("ichor_pickaxe",
            () -> new IchorPickaxeItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ICHOR_SWORD = specialItem("ichor_sword",
            () -> new IchorSwordItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ICHORCLOTH_HOOD = specialItem("ichorcloth_hood",
            () -> new IchorArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ICHORCLOTH_ROBE = specialItem("ichorcloth_robe",
            () -> new IchorArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ICHORCLOTH_LEGGINGS = specialItem("ichorcloth_leggings",
            () -> new IchorArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ICHORCLOTH_BOOTS = specialItem("ichorcloth_boots",
            () -> new IchorArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> ASPECT_AER = specialItem("aspect_aer", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.AER));
    public static final RegistryObject<Item> ASPECT_TERRA = specialItem("aspect_terra", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.TERRA));
    public static final RegistryObject<Item> ASPECT_IGNIS = specialItem("aspect_ignis", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.IGNIS));
    public static final RegistryObject<Item> ASPECT_AQUA = specialItem("aspect_aqua", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.AQUA));
    public static final RegistryObject<Item> ASPECT_ORDO = specialItem("aspect_ordo", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.ORDO));
    public static final RegistryObject<Item> ASPECT_PERDITIO = specialItem("aspect_perditio", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.PERDITIO));
    public static final RegistryObject<Item> ASPECT_VITREUS = specialItem("aspect_vitreus", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.VITREUS));
    public static final RegistryObject<Item> ASPECT_METALLUM = specialItem("aspect_metallum", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.METALLUM));
    public static final RegistryObject<Item> ASPECT_PRAECANTATIO = specialItem("aspect_praecantatio", () -> new AspectCrystalItem(new Item.Properties().tab(THAUMCRAFT_TAB), Aspect.PRAECANTATIO));

    public static final RegistryObject<Item> THAUMIUM_INGOT = item("thaumium_ingot");
    public static final RegistryObject<Item> THAUMIUM_NUGGET = item("thaumium_nugget");
    public static final RegistryObject<Item> THAUMIUM_PLATE = item("thaumium_plate");
    public static final RegistryObject<Item> VOID_METAL_INGOT = item("void_metal_ingot");
    public static final RegistryObject<Item> AMBER = item("amber");
    public static final RegistryObject<Item> QUICKSILVER_DROP = item("quicksilver_drop");
    public static final RegistryObject<Item> PRIMORDIAL_PEARL = item("primordial_pearl");
    public static final RegistryObject<Item> NITOR = specialItem("nitor",
            () -> new NitorItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> ALCHEMY_DUST = item("alchemy_dust");
    public static final RegistryObject<Item> ESSENTIA_PHIAL = specialItem("essentia_phial",
            () -> new EssentiaPhialItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> TAINTED_SLIME = item("tainted_slime");
    public static final RegistryObject<Item> FLUX_CRYSTAL = item("flux_crystal");
    public static final RegistryObject<Item> INFUSION_CORE = item("infusion_core");
    public static final RegistryObject<Item> UNSTABLE_SINGULARITY = item("unstable_singularity");
    public static final RegistryObject<Item> WARP_CHARM = specialItem("warp_charm",
            () -> new WarpCharmItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> WARP_WARD_TALISMAN = specialItem("warp_ward_talisman",
            () -> new WarpWardTalismanItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> SANITY_SOAP = specialItem("sanity_soap",
            () -> new SanitySoapItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> ELDRITCH_EYE = specialItem("eldritch_eye",
            () -> new EldritchEyeItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> CRIMSON_KEY = item("crimson_key");
    public static final RegistryObject<Item> AWAKENED_CRIMSON_KEY = item("awakened_crimson_key");
    public static final RegistryObject<Item> CRIMSON_PLATE_HELM = specialItem("crimson_plate_helm",
            () -> new TC4CrimsonPlateArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(THAUMCRAFT_TAB), "ConfigItems.itemHelmetCultistPlate", "cultistplatehelm", false));
    public static final RegistryObject<Item> CRIMSON_PLATE_CHEST = specialItem("crimson_plate_chest",
            () -> new TC4CrimsonPlateArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(THAUMCRAFT_TAB), "ConfigItems.itemChestCultistPlate", "cultistplatechest", false));
    public static final RegistryObject<Item> CRIMSON_PLATE_LEGS = specialItem("crimson_plate_legs",
            () -> new TC4CrimsonPlateArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(THAUMCRAFT_TAB), "ConfigItems.itemLegsCultistPlate", "cultistplatelegs", false));
    public static final RegistryObject<Item> CRIMSON_PLATE_BOOTS = specialItem("crimson_plate_boots",
            () -> new TC4CrimsonPlateArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(THAUMCRAFT_TAB), "ConfigItems.itemBootsCultist", "cultistboots", false));
    public static final RegistryObject<Item> ELDRITCH_RELIC = item("eldritch_relic");
    public static final RegistryObject<Item> ELDRITCH_GUARDIAN_CORE = item("eldritch_guardian_core");

    public static final RegistryObject<Item> GOLEM_CORE = specialItem("golem_core",
            () -> new GolemCoreItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> GOLEM_BELL = specialItem("golem_bell",
            () -> new GolemBellItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> GOLEM_SEAL_COLLECT = specialItem("golem_seal_collect",
            () -> new GolemSealCollectItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> GOLEM_TASK_MARKER = specialItem("golem_task_marker",
            () -> new GolemTaskMarkerItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> GOLEM_FILTER = specialItem("golem_filter",
            () -> new GolemFilterItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> GOLEM_UPGRADE_AIR = specialItem("golem_upgrade_air",
            () -> new GolemUpgradeItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemUpgradeType.AIR));
    public static final RegistryObject<Item> GOLEM_UPGRADE_FIRE = specialItem("golem_upgrade_fire",
            () -> new GolemUpgradeItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemUpgradeType.FIRE));
    public static final RegistryObject<Item> GOLEM_UPGRADE_WATER = specialItem("golem_upgrade_water",
            () -> new GolemUpgradeItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemUpgradeType.WATER));
    public static final RegistryObject<Item> GOLEM_UPGRADE_EARTH = specialItem("golem_upgrade_earth",
            () -> new GolemUpgradeItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemUpgradeType.EARTH));
    public static final RegistryObject<Item> GOLEM_UPGRADE_ORDER = specialItem("golem_upgrade_order",
            () -> new GolemUpgradeItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemUpgradeType.ORDER));
    public static final RegistryObject<Item> GOLEM_UPGRADE_ENTROPY = specialItem("golem_upgrade_entropy",
            () -> new GolemUpgradeItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemUpgradeType.ENTROPY));

    public static final RegistryObject<Item> GOLEM_DECO_ARMOR = specialItem("golem_deco_armor",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.ARMOR));
    public static final RegistryObject<Item> GOLEM_DECO_TOP_HAT = specialItem("golem_deco_tophat",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.TOP_HAT));
    public static final RegistryObject<Item> GOLEM_DECO_FEZ = specialItem("golem_deco_fez",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.FEZ));
    public static final RegistryObject<Item> GOLEM_DECO_VISOR = specialItem("golem_deco_visor",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.VISOR));
    public static final RegistryObject<Item> GOLEM_DECO_GLASSES = specialItem("golem_deco_glasses",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.GLASSES));
    public static final RegistryObject<Item> GOLEM_DECO_BOWTIE = specialItem("golem_deco_bowtie",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.BOWTIE));
    public static final RegistryObject<Item> GOLEM_DECO_DART_LAUNCHER = specialItem("golem_deco_dart_launcher",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.DART_LAUNCHER));
    public static final RegistryObject<Item> GOLEM_DECO_MACE = specialItem("golem_deco_mace",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.MACE));
    public static final RegistryObject<Item> GOLEM_WIRELESS_BACKPACK = specialItem("golem_wireless_backpack",
            () -> new GolemDecorationItem(new Item.Properties().tab(THAUMCRAFT_TAB), GolemDecorationType.WIRELESS_BACKPACK));
    public static final RegistryObject<Item> JAR_LABEL = ITEMS.register("jar_label", () -> new JarLabelItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> TAINT_SEED = specialItem("taint_seed",
            () -> new TaintSeedItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> FOCUS_FIRE = focusItem("focus_fire", WandFocusType.FIRE);
    public static final RegistryObject<Item> FOCUS_FROST = focusItem("focus_frost", WandFocusType.FROST);
    public static final RegistryObject<Item> FOCUS_SHOCK = focusItem("focus_shock", WandFocusType.SHOCK);
    public static final RegistryObject<Item> FOCUS_EXCAVATION = focusItem("focus_excavation", WandFocusType.EXCAVATION);
    public static final RegistryObject<Item> FOCUS_PORTABLE_HOLE = focusItem("focus_portable_hole", WandFocusType.PORTABLE_HOLE);
    public static final RegistryObject<Item> FOCUS_EQUAL_TRADE = focusItem("focus_equal_trade", WandFocusType.EQUAL_TRADE);
    public static final RegistryObject<Item> FOCUS_WARDING = focusItem("focus_warding", WandFocusType.WARDING);
    public static final RegistryObject<Item> FOCUS_PRIMAL = focusItem("focus_primal", WandFocusType.PRIMAL);
    public static final RegistryObject<Item> FOCUS_POUCH = specialItem("focus_pouch",
            () -> new FocusPouchBaubleItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> FOCUS_BLINK = extrasFocus("focus_blink", ThaumcraftExtrasFocusItem.Mode.BLINK);
    public static final RegistryObject<Item> FOCUS_ARROW = extrasFocus("focus_arrow", ThaumcraftExtrasFocusItem.Mode.ARROW);
    public static final RegistryObject<Item> FOCUS_HEAL = extrasFocus("focus_heal", ThaumcraftExtrasFocusItem.Mode.HEAL);
    public static final RegistryObject<Item> FOCUS_SPEED = extrasFocus("focus_speed", ThaumcraftExtrasFocusItem.Mode.SPEED);
    public static final RegistryObject<Item> FOCUS_PECH_SUMMON = extrasFocus("focus_pech_summon", ThaumcraftExtrasFocusItem.Mode.PECH_SUMMON);
    public static final RegistryObject<Item> FOCUS_EXPERIENCE = extrasFocus("focus_experience", ThaumcraftExtrasFocusItem.Mode.EXPERIENCE);
    public static final RegistryObject<Item> FOCUS_RETURN = extrasFocus("focus_return", ThaumcraftExtrasFocusItem.Mode.RETURN);
    public static final RegistryObject<Item> FOCUS_EXCHANGE = extrasFocus("focus_exchange", ThaumcraftExtrasFocusItem.Mode.EXCHANGE);
    public static final RegistryObject<Item> FOCUS_SMELTING = extrasFocus("focus_smelting", ThaumcraftExtrasFocusItem.Mode.SMELTING);
    public static final RegistryObject<Item> FOCUS_DISPEL = extrasFocus("focus_dispel", ThaumcraftExtrasFocusItem.Mode.DISPEL);
    public static final RegistryObject<Item> FOCUS_DESTROY = extrasFocus("focus_destroy", ThaumcraftExtrasFocusItem.Mode.DESTROY);
    public static final RegistryObject<Item> FOCUS_FREEZE = extrasFocus("focus_freeze", ThaumcraftExtrasFocusItem.Mode.FREEZE);

    public static final RegistryObject<Item> PECH_TRADE_TIER_1 = pechToken("pech_trade_tier_1", 1);
    public static final RegistryObject<Item> PECH_TRADE_TIER_2 = pechToken("pech_trade_tier_2", 2);
    public static final RegistryObject<Item> PECH_TRADE_TIER_3 = pechToken("pech_trade_tier_3", 3);
    public static final RegistryObject<Item> PECH_TRADE_TIER_4 = pechToken("pech_trade_tier_4", 4);
    public static final RegistryObject<Item> PECH_TRADE_TIER_5 = pechToken("pech_trade_tier_5", 5);
    public static final RegistryObject<Item> IGNIS_FUEL = item("ignis_fuel");
    public static final RegistryObject<Item> EXPERIENCE_SHARD = specialItem("experience_shard",
            () -> new ExperienceShardItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> EXPERIENCE_EXTRACTOR = specialItem("experience_extractor",
            () -> new ExperienceExtractorItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> PECH_LEDGER = specialItem("pech_ledger",
            () -> new PechLedgerItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1)));

    public static final RegistryObject<Item> ESSENTIA_DIGITIZER_CORE = item("essentia_digitizer_core");
    public static final RegistryObject<Item> ESSENTIA_STORAGE_COMPONENT_1K = item("essentia_storage_component_1k");
    public static final RegistryObject<Item> ESSENTIA_STORAGE_COMPONENT_4K = item("essentia_storage_component_4k");
    public static final RegistryObject<Item> ESSENTIA_STORAGE_COMPONENT_16K = item("essentia_storage_component_16k");
    public static final RegistryObject<Item> ENCODED_ESSENTIA_PATTERN = specialItem("encoded_essentia_pattern",
            () -> new EncodedEssentiaPatternItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1)));
    public static final RegistryObject<Item> WIRELESS_ESSENTIA_TERMINAL = specialItem("wireless_essentia_terminal",
            () -> new WirelessEssentiaTerminalItem(new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Item> ESSENTIA_PARTITION_CARD = specialItem("essentia_partition_card",
            () -> new EssentiaPartitionCardItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1)));
    public static final RegistryObject<Item> ESSENTIA_CAPACITY_UPGRADE = item("essentia_capacity_upgrade");
    public static final RegistryObject<Item> ESSENTIA_VIEW_CARD = specialItem("essentia_view_card", () -> new ThaumicEnergisticsCardItem(new Item.Properties().tab(THAUMCRAFT_TAB), "View"));

    public static final RegistryObject<Item> ESSENTIA_SPEED_CARD = specialItem("essentia_speed_card",
            () -> new EssentiaUpgradeCardItem(new Item.Properties().tab(THAUMCRAFT_TAB), EssentiaUpgradeCardItem.Mode.SPEED));
    public static final RegistryObject<Item> ADVANCED_ESSENTIA_SPEED_CARD = specialItem("advanced_essentia_speed_card",
            () -> new EssentiaUpgradeCardItem(new Item.Properties().tab(THAUMCRAFT_TAB), EssentiaUpgradeCardItem.Mode.ADVANCED_SPEED));
    public static final RegistryObject<Item> ESSENTIA_ACCELERATION_CARD = specialItem("essentia_acceleration_card",
            () -> new EssentiaUpgradeCardItem(new Item.Properties().tab(THAUMCRAFT_TAB), EssentiaUpgradeCardItem.Mode.ACCELERATION));
    public static final RegistryObject<Item> THAUMIC_COPROCESSOR_CARD = specialItem("thaumic_coprocessor_card",
            () -> new EssentiaUpgradeCardItem(new Item.Properties().tab(THAUMCRAFT_TAB), EssentiaUpgradeCardItem.Mode.COPROCESSOR));
    public static final RegistryObject<Item> ESSENTIA_FUZZY_CARD = specialItem("essentia_fuzzy_card",
            () -> new EssentiaUpgradeCardItem(new Item.Properties().tab(THAUMCRAFT_TAB), EssentiaUpgradeCardItem.Mode.FUZZY));

    public static final RegistryObject<Item> DIGITAL_ESSENTIA_CELL_1K = specialItem("digital_essentia_cell_1k",
            () -> new EssentiaCellItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), 1024));
    public static final RegistryObject<Item> DIGITAL_ESSENTIA_CELL_4K = specialItem("digital_essentia_cell_4k",
            () -> new EssentiaCellItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), 4096));
    public static final RegistryObject<Item> DIGITAL_ESSENTIA_CELL_16K = specialItem("digital_essentia_cell_16k",
            () -> new EssentiaCellItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), 16384));
    public static final RegistryObject<Item> DIGITAL_ESSENTIA_CELL_64K = specialItem("digital_essentia_cell_64k",
            () -> new EssentiaCellItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), 65536));
    public static final RegistryObject<Item> CREATIVE_ESSENTIA_CELL = specialItem("creative_essentia_cell",
            () -> new EssentiaCellItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), 1048576));
    public static final RegistryObject<Item> ESSENTIA_STORAGE_COMPONENT_64K = specialItem("essentia_storage_component_64k",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.STORAGE_COMPONENT_64K));
    public static final RegistryObject<Item> ESSENTIA_CELL_CASING = specialItem("essentia_cell_casing",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.ESSENTIA_CELL_CASING));
    public static final RegistryObject<Item> FOCUS_AE_WRENCH = specialItem("focus_ae_wrench",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), ThaumicEnergisticsUtilityItem.Mode.AE_WRENCH));
    public static final RegistryObject<Item> KNOWLEDGE_CORE = specialItem("knowledge_core",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.KNOWLEDGE_CORE));
    public static final RegistryObject<Item> COALESCENCE_CORE = specialItem("coalescence_core",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.COALESCENCE_CORE));
    public static final RegistryObject<Item> DIFFUSION_CORE = specialItem("diffusion_core",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.DIFFUSION_CORE));
    public static final RegistryObject<Item> IRON_GEAR = specialItem("iron_gear",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.IRON_GEAR));
    public static final RegistryObject<Item> CRAFTING_ASPECT = specialItem("crafting_aspect",
            () -> new ThaumicEnergisticsUtilityItem(new Item.Properties().tab(THAUMCRAFT_TAB), ThaumicEnergisticsUtilityItem.Mode.CRAFTING_ASPECT));
    public static final RegistryObject<Item> THAUMIC_GRID_TOOL = specialItem("thaumic_grid_tool",
            () -> new ThaumicAeGridToolItem(new Item.Properties().tab(THAUMCRAFT_TAB)));
    public static final RegistryObject<Item> THAUMIC_CRAFTING_CPU_CORE = item("thaumic_crafting_cpu_core");
    public static final RegistryObject<Item> THAUMIC_CHANNEL_CORE = item("thaumic_channel_core");

    public static final RegistryObject<Block> ARCANE_STONE = block("arcane_stone",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> ARCANE_STONE_BRICKS = block("arcane_stone_bricks",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> INFUSION_PILLAR = block("infusion_pillar",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ELDRITCH_STONE = block("eldritch_stone",
            BlockBehaviour.Properties.of(Material.STONE).strength(5.0F, 10.0F).requiresCorrectToolForDrops().lightLevel(state -> 2));

    public static final RegistryObject<Block> ELDRITCH_OBELISK = block("eldritch_obelisk",
            BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 12.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));

    public static final RegistryObject<Block> ELDRITCH_ALTAR = eldritchAltarBlock("eldritch_altar",
            BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 16.0F).requiresCorrectToolForDrops().lightLevel(state -> 7));

    public static final RegistryObject<Block> ELDRITCH_PORTAL = eldritchPortalBlock("eldritch_portal",
            BlockBehaviour.Properties.of(Material.PORTAL).strength(-1.0F, 3600000.0F).noOcclusion().lightLevel(state -> 12));

    public static final RegistryObject<Block> ELDRITCH_NOTHING = BLOCKS.register("eldritch_nothing",
            () -> new EldritchNothingBlock(BlockBehaviour.Properties.of(Material.PORTAL).strength(-1.0F, 3600000.0F).noCollission().noOcclusion()));

    public static final RegistryObject<Block> ELDRITCH_CAP = BLOCKS.register("eldritch_cap",
            () -> new EldritchCapBlock(BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 16.0F).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> 4)));
    public static final RegistryObject<Item> ELDRITCH_CAP_ITEM = ITEMS.register("eldritch_cap",
            () -> new BlockItem(ELDRITCH_CAP.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Block> ELDRITCH_LOCK = BLOCKS.register("eldritch_lock",
            () -> new EldritchLockBlock(BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 16.0F).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> state.getValue(EldritchLockBlock.OPEN) ? 12 : 5)));
    public static final RegistryObject<Item> ELDRITCH_LOCK_ITEM = ITEMS.register("eldritch_lock",
            () -> new BlockItem(ELDRITCH_LOCK.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Block> ELDRITCH_TRAP = BLOCKS.register("eldritch_trap",
            () -> new EldritchTrapBlock(BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 16.0F).requiresCorrectToolForDrops().lightLevel(state -> 3)));
    public static final RegistryObject<Item> ELDRITCH_TRAP_ITEM = ITEMS.register("eldritch_trap",
            () -> new BlockItem(ELDRITCH_TRAP.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Block> ELDRITCH_CRYSTAL = BLOCKS.register("eldritch_crystal",
            () -> new EldritchCrystalBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(2.0F, 12.0F).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> 11)));
    public static final RegistryObject<Item> ELDRITCH_CRYSTAL_ITEM = ITEMS.register("eldritch_crystal",
            () -> new BlockItem(ELDRITCH_CRYSTAL.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));

    public static final RegistryObject<Block> ELDRITCH_CRUST = block("eldritch_crust",
            BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 16.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));

    public static final RegistryObject<Block> ELDRITCH_DECORATIVE = block("eldritch_decorative",
            BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 16.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));

    public static final RegistryObject<Block> ELDRITCH_DOOR = block("eldritch_door",
            BlockBehaviour.Properties.of(Material.STONE).strength(50.0F, 1200.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));

    public static final RegistryObject<Block> ELDRITCH_CRAB_SPAWNER = eldritchCrabSpawnerBlock("eldritch_crab_spawner",
            BlockBehaviour.Properties.of(Material.STONE).strength(5.0F, 10.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> OUTER_LANDS_LOOT_URN = tc4LootBlock("outer_lands_loot_urn", TC4LootBlock.Kind.URN,
            BlockBehaviour.Properties.of(Material.WOOD).strength(0.15F, 0.0F).noOcclusion());
    public static final RegistryObject<Block> OUTER_LANDS_LOOT_CRATE = tc4LootBlock("outer_lands_loot_crate", TC4LootBlock.Kind.CRATE,
            BlockBehaviour.Properties.of(Material.WOOD).strength(0.15F, 0.0F).noOcclusion());
    public static final RegistryObject<Block> AMBER_ORE = block("amber_ore",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> CINNABAR_ORE = block("cinnabar_ore",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> TAINTED_SOIL = taintedSoilBlock("tainted_soil",
            BlockBehaviour.Properties.of(Material.DIRT).strength(2.0F, 10.0F).randomTicks());

    public static final RegistryObject<Block> TAINT_CRUST = taintBlock("taint_crust", TaintBlock.Variant.CRUST,
            BlockBehaviour.Properties.of(Material.CLAY).strength(2.0F, 10.0F).randomTicks());

    public static final RegistryObject<Block> TAINT_SOIL = taintBlock("taint_soil", TaintBlock.Variant.SOIL,
            BlockBehaviour.Properties.of(Material.DIRT).strength(2.0F, 10.0F).randomTicks());

    public static final RegistryObject<Block> FLESH_BLOCK = taintBlock("flesh_block", TaintBlock.Variant.FLESH,
            BlockBehaviour.Properties.of(Material.CLAY).strength(2.0F, 10.0F).randomTicks());

    public static final RegistryObject<Block> TAINT_FIBRES = taintFibresBlock("taint_fibres",
            BlockBehaviour.Properties.of(Material.PLANT).strength(0.05F).randomTicks().noCollission().noOcclusion());
    public static final RegistryObject<Block> FLUX_GOO = fluxGooBlock("flux_goo",
            BlockBehaviour.Properties.of(Material.CLAY).strength(0.2F).randomTicks().noOcclusion());
    public static final RegistryObject<Block> FLUX_GAS = fluxGasBlock("flux_gas",
            BlockBehaviour.Properties.of(Material.AIR).strength(0.0F).randomTicks().noCollission().noOcclusion().lightLevel(state -> 3));
    public static final RegistryObject<Block> TEMPORARY_HOLE = temporaryHoleBlock("temporary_hole",
            BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 6000000.0F).noCollission().noOcclusion().lightLevel(state -> 10));
    public static final RegistryObject<Block> WARDED_BLOCK = BLOCKS.register("warded_block", () -> new WardedBlock(
            BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noOcclusion()));
    public static final RegistryObject<Block> ELECTRIC_SHOCK = electricShockBlock("electric_shock",
            BlockBehaviour.Properties.of(Material.AIR).strength(100.0F, 50.0F).randomTicks().noCollission().noOcclusion().lightLevel(state -> 8));
    public static final RegistryObject<Block> GREATWOOD_LOG = pillarBlock("greatwood_log",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F));

    public static final RegistryObject<Block> SILVERWOOD_LOG = pillarBlock("silverwood_log",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F).lightLevel(state -> 1));

    public static final RegistryObject<Block> GREATWOOD_LEAVES = block("greatwood_leaves",
            BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().noOcclusion());

    public static final RegistryObject<Block> SILVERWOOD_LEAVES = block("silverwood_leaves",
            BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().noOcclusion().lightLevel(state -> 1));

    public static final RegistryObject<Block> GREATWOOD_SAPLING = tc4SaplingBlock("greatwood_sapling", TC4SaplingBlock.Kind.GREATWOOD,
            BlockBehaviour.Properties.of(Material.PLANT).strength(0.0F).randomTicks().noCollission().noOcclusion());

    public static final RegistryObject<Block> SILVERWOOD_SAPLING = tc4SaplingBlock("silverwood_sapling", TC4SaplingBlock.Kind.SILVERWOOD,
            BlockBehaviour.Properties.of(Material.PLANT).strength(0.0F).randomTicks().noCollission().noOcclusion().lightLevel(state -> 1));

    public static final RegistryObject<Block> GREATWOOD_PLANKS = block("greatwood_planks",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F));
    public static final RegistryObject<Block> SILVERWOOD_PLANKS = block("silverwood_planks",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F));
    public static final RegistryObject<Block> TABLE = tableBlock("table",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F));

    public static final RegistryObject<Block> RESEARCH_TABLE = researchTableBlock("research_table",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F));

    public static final RegistryObject<Block> DECONSTRUCTION_TABLE = deconstructionTableBlock("deconstruction_table",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F).noOcclusion());

    public static final RegistryObject<Block> ARCANE_WORKBENCH = arcaneWorkbenchBlock("arcane_workbench",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 4.0F));

    public static final RegistryObject<Block> FOCAL_MANIPULATOR = focalManipulatorBlock("tc4_block_focal_manipulator",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> CRUCIBLE = crucibleBlock("crucible",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 6.0F).requiresCorrectToolForDrops());

    public static final RegistryObject<Block> BELLOWS = bellowsBlock("bellows",
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F).noOcclusion());
    public static final RegistryObject<Block> ESSENTIA_JAR = essentiaJarBlock("essentia_jar",
            BlockBehaviour.Properties.of(Material.GLASS).strength(2.0F, 4.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> FILTERED_ESSENTIA_JAR = filteredEssentiaJarBlock("filtered_essentia_jar",
            BlockBehaviour.Properties.of(Material.GLASS).strength(2.0F, 4.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> VOID_ESSENTIA_JAR = voidEssentiaJarBlock("void_essentia_jar",
            BlockBehaviour.Properties.of(Material.GLASS).strength(2.5F, 5.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_RESERVOIR = essentiaReservoirBlock("essentia_reservoir",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ALEMBIC = alembicBlock("alembic",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ALCHEMICAL_CENTRIFUGE = alchemicalCentrifugeBlock("alchemical_centrifuge",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_CRYSTALIZER = essentiaCrystalizerBlock("essentia_crystalizer",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> 3));

    public static final RegistryObject<Block> ESSENTIA_TUBE = essentiaTubeBlock("essentia_tube",
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 2.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_TUBE_FILTER = essentiaTubeBlock("essentia_tube_filter", EssentiaTubeSubtype.FILTER,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 2.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_TUBE_RESTRICT = essentiaTubeBlock("essentia_tube_restrict", EssentiaTubeSubtype.RESTRICT,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 2.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_TUBE_ONEWAY = essentiaTubeBlock("essentia_tube_oneway", EssentiaTubeSubtype.ONEWAY,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 2.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_TUBE_BUFFER = essentiaTubeBlock("essentia_tube_buffer", EssentiaTubeSubtype.BUFFER,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.2F, 2.5F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ESSENTIA_VALVE = essentiaValveBlock("essentia_valve",
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.2F, 2.5F).requiresCorrectToolForDrops().noOcclusion());

    public static final RegistryObject<Block> ALCHEMICAL_FURNACE = alchemicalFurnaceBlock("alchemical_furnace",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));

    public static final RegistryObject<Block> ADVANCED_ALCHEMICAL_FURNACE = alchemicalFurnaceBlock("advanced_alchemical_furnace",
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 10.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));

    public static final RegistryObject<Block> THAUMATORIUM = thaumatoriumBlock("thaumatorium",
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 10.0F).requiresCorrectToolForDrops().noOcclusion());

    /** Stage523-542: runtime replacement for original ConfigBlocks.blockMetalDevice meta 12 / TileMemory. */
    public static final RegistryObject<Block> MNEMONIC_MATRIX = block("mnemonic_matrix",
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 10.0F).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> 4));

    public static final RegistryObject<Block> EXTRAS_FIRE_BLOCK = extrasElementBlock("extras_fire_block", ThaumcraftExtrasElementalBlock.Mode.FIRE,
            BlockBehaviour.Properties.of(Material.STONE).strength(2.5F, 6.0F).lightLevel(state -> 10));
    public static final RegistryObject<Block> EXTRAS_AIR_BLOCK = extrasElementBlock("extras_air_block", ThaumcraftExtrasElementalBlock.Mode.AIR,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 2.0F).noOcclusion());
    public static final RegistryObject<Block> EXTRAS_WATER_BLOCK = extrasElementBlock("extras_water_block", ThaumcraftExtrasElementalBlock.Mode.WATER,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.5F, 3.0F).noOcclusion());
    public static final RegistryObject<Block> EXTRAS_EARTH_BLOCK = extrasElementBlock("extras_earth_block", ThaumcraftExtrasElementalBlock.Mode.EARTH,
            BlockBehaviour.Properties.of(Material.STONE).strength(3.5F, 8.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> EXTRAS_LIGHT_BLOCK = extrasElementBlock("extras_light_block", ThaumcraftExtrasElementalBlock.Mode.LIGHT,
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 2.0F).noOcclusion().lightLevel(state -> 15));
    public static final RegistryObject<Block> EXTRAS_ENDER_BLOCK = extrasElementBlock("extras_ender_block", ThaumcraftExtrasElementalBlock.Mode.ENDER,
            BlockBehaviour.Properties.of(Material.STONE).strength(4.0F, 12.0F).requiresCorrectToolForDrops().lightLevel(state -> 6));
    public static final RegistryObject<Block> RESEARCH_CACHE_BLOCK = extrasElementBlock("research_cache_block", ThaumcraftExtrasElementalBlock.Mode.RESEARCH,
            BlockBehaviour.Properties.of(Material.STONE).strength(2.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 9));

    public static final RegistryObject<Block> ESSENTIA_TERMINAL = thaumicEnergisticsDeviceBlock("essentia_terminal", ThaumicEnergisticsDeviceBlock.Mode.TERMINAL,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));
    public static final RegistryObject<Block> ESSENTIA_STORAGE_BUS = thaumicEnergisticsDeviceBlock("essentia_storage_bus", ThaumicEnergisticsDeviceBlock.Mode.STORAGE_BUS,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final RegistryObject<Block> ESSENTIA_IMPORT_BUS = thaumicEnergisticsDeviceBlock("essentia_import_bus", ThaumicEnergisticsDeviceBlock.Mode.IMPORT_BUS,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final RegistryObject<Block> ESSENTIA_EXPORT_BUS = thaumicEnergisticsDeviceBlock("essentia_export_bus", ThaumicEnergisticsDeviceBlock.Mode.EXPORT_BUS,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final RegistryObject<Block> ESSENTIA_INTERFACE = thaumicEnergisticsDeviceBlock("essentia_interface", ThaumicEnergisticsDeviceBlock.Mode.INTERFACE,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> ARCANE_PATTERN_ENCODER = thaumicEnergisticsDeviceBlock("arcane_pattern_encoder", ThaumicEnergisticsDeviceBlock.Mode.PATTERN_ENCODER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> ARCANE_PATTERN_PROVIDER = thaumicEnergisticsDeviceBlock("arcane_pattern_provider", ThaumicEnergisticsDeviceBlock.Mode.PATTERN_PROVIDER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));

    public static final RegistryObject<Block> OBSIDIAN_TILE = block("obsidian_tile",
            BlockBehaviour.Properties.of(Material.STONE).strength(10.0F, 1200.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> OBSIDIAN_TOTEM = block("obsidian_totem",
            BlockBehaviour.Properties.of(Material.STONE).strength(12.0F, 1200.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> NITOR_LIGHT = nitorLightBlock("nitor_light",
            BlockBehaviour.Properties.of(Material.FIRE)
                    .strength(0.0F, 0.0F)
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 15));
    public static final RegistryObject<Block> TT_MOB_MAGNET = ttParityBlock("tt_mob_magnet", ThaumicTinkererParityBlock.Mode.MOB_MAGNET,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_REPAIRER = ttParityBlock("tt_repairer", ThaumicTinkererParityBlock.Mode.REPAIRER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_ASPECT_ANALYZER = ttParityBlock("tt_aspect_analyzer", ThaumicTinkererParityBlock.Mode.ASPECT_ANALYZER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_ENCHANTER = ttParityBlock("tt_enchanter", ThaumicTinkererParityBlock.Mode.ENCHANTER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_GOLEM_CONNECTOR = ttParityBlock("tt_golem_connector", ThaumicTinkererParityBlock.Mode.GOLEM_CONNECTOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FUNNEL = ttParityBlock("tt_funnel", ThaumicTinkererParityBlock.Mode.FUNNEL,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FORCEFIELD = ttParityBlock("tt_forcefield", ThaumicTinkererParityBlock.Mode.FORCEFIELD,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_SUMMON_TABLET = ttParityBlock("tt_summon_tablet", ThaumicTinkererParityBlock.Mode.SUMMON_TABLET,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_REMOTE_PLACER = ttParityBlock("tt_remote_placer", ThaumicTinkererParityBlock.Mode.REMOTE_PLACER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_MOBILIZER = ttParityBlock("tt_mobilizer", ThaumicTinkererParityBlock.Mode.MOBILIZER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_MOBILIZER_RELAY = ttParityBlock("tt_mobilizer_relay", ThaumicTinkererParityBlock.Mode.MOBILIZER_RELAY,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_TRANSVECTOR_DISLOCATOR = ttParityBlock("tt_transvector_dislocator", ThaumicTinkererParityBlock.Mode.TRANSVECTOR_DISLOCATOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_WARP_GATE = ttParityBlock("tt_warp_gate", ThaumicTinkererParityBlock.Mode.WARP_GATE,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_BEDROCK_PORTAL = ttParityBlock("tt_bedrock_portal", ThaumicTinkererParityBlock.Mode.BEDROCK_PORTAL,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_DARK_QUARTZ_BLOCK = ttParityBlock("tt_dark_quartz_block", ThaumicTinkererParityBlock.Mode.DARK_QUARTZ,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_GASEOUS_LIGHT = ttParityBlock("tt_gaseous_light", ThaumicTinkererParityBlock.Mode.GAS_LIGHT,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_GASEOUS_SHADOW = ttParityBlock("tt_gaseous_shadow", ThaumicTinkererParityBlock.Mode.GAS_SHADOW,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_NITOR_GAS = ttParityBlock("tt_nitor_gas", ThaumicTinkererParityBlock.Mode.NITOR_GAS,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FIRE_AIR = ttParityBlock("tt_fire_air", ThaumicTinkererParityBlock.Mode.FIRE_ELEMENT,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FIRE_WATER = ttParityBlock("tt_fire_water", ThaumicTinkererParityBlock.Mode.FIRE_ELEMENT,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FIRE_EARTH = ttParityBlock("tt_fire_earth", ThaumicTinkererParityBlock.Mode.FIRE_ELEMENT,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FIRE_ORDER = ttParityBlock("tt_fire_order", ThaumicTinkererParityBlock.Mode.FIRE_ELEMENT,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_FIRE_CHAOS = ttParityBlock("tt_fire_chaos", ThaumicTinkererParityBlock.Mode.FIRE_ELEMENT,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_ANIMATION_TABLET = ttParityBlock("tt_animation_tablet", ThaumicTinkererParityBlock.Mode.ANIMATION_TABLET,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TT_INFUSED_FARMLAND = ttParityBlock("tt_infused_farmland", ThaumicTinkererParityBlock.Mode.INFUSED_FARMLAND,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));

    public static final RegistryObject<Block> TCE_CHARGER = tceParityBlock("tce_charger", ThaumcraftExtrasParityBlock.Mode.CHARGER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_EXCHANGER = tceParityBlock("tce_exchanger", ThaumcraftExtrasParityBlock.Mode.EXCHANGER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_DARK_INFUSER = tceParityBlock("tce_dark_infuser", ThaumcraftExtrasParityBlock.Mode.DARK_INFUSER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_MAGIC_GENERATOR = tceParityBlock("tce_magic_generator", ThaumcraftExtrasParityBlock.Mode.MAGIC_GENERATOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_MAGIC_SOLAR_PANEL = tceParityBlock("tce_magic_solar_panel", ThaumcraftExtrasParityBlock.Mode.MAGIC_SOLAR_PANEL,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_MAGIC_CHARGER = tceParityBlock("tce_magic_charger", ThaumcraftExtrasParityBlock.Mode.MAGIC_CHARGER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_TELEPORTER = tceParityBlock("tce_teleporter", ThaumcraftExtrasParityBlock.Mode.TELEPORTER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_TESLA = tceParityBlock("tce_tesla", ThaumcraftExtrasParityBlock.Mode.TESLA,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_COLOR_BLOCK = tceParityBlock("tce_color_block", ThaumcraftExtrasParityBlock.Mode.COLOR_BLOCK,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_HIDDEN_WARDED = tceParityBlock("tce_hidden_warded", ThaumcraftExtrasParityBlock.Mode.HIDDEN_WARDED,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_OPEN_WARDED = tceParityBlock("tce_open_warded", ThaumcraftExtrasParityBlock.Mode.OPEN_WARDED,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_WARDED_GLASS = tceParityBlock("tce_warded_glass", ThaumcraftExtrasParityBlock.Mode.WARDED_GLASS,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_WARDED_PILLAR = tceParityBlock("tce_warded_pillar", ThaumcraftExtrasParityBlock.Mode.WARDED_PILLAR,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_WARDED_SLAB = tceParityBlock("tce_warded_slab", ThaumcraftExtrasParityBlock.Mode.WARDED_SLAB,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_WARDED_WALL = tceParityBlock("tce_warded_wall", ThaumcraftExtrasParityBlock.Mode.WARDED_WALL,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_WARDED_CARPET = tceParityBlock("tce_warded_carpet", ThaumcraftExtrasParityBlock.Mode.WARDED_CARPET,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_WARDED_COVER = tceParityBlock("tce_warded_cover", ThaumcraftExtrasParityBlock.Mode.WARDED_COVER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_CACTUS = tceParityBlock("tce_cactus", ThaumcraftExtrasParityBlock.Mode.CACTUS,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_DARK_SILVERWOOD = tceParityBlock("tce_dark_silverwood", ThaumcraftExtrasParityBlock.Mode.DARK_SILVERWOOD,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_DARK_SILVERWOOD_PLANKS = tceParityBlock("tce_dark_silverwood_planks", ThaumcraftExtrasParityBlock.Mode.DARK_SILVERWOOD_PLANKS,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_IGNIS_FUEL_BLOCK = tceParityBlock("tce_ignis_fuel_block", ThaumcraftExtrasParityBlock.Mode.IGNIS_FUEL_BLOCK,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_INFUSION_INFO = tceParityBlock("tce_infusion_info", ThaumcraftExtrasParityBlock.Mode.INFUSION_INFO,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_LAVA_BLOCK = tceParityBlock("tce_lava_block", ThaumcraftExtrasParityBlock.Mode.LAVA_BLOCK,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_CABLE = tceParityBlock("tce_cable", ThaumcraftExtrasParityBlock.Mode.CABLE,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> TCE_CLEAR_GLASS = tceParityBlock("tce_clear_glass", ThaumcraftExtrasParityBlock.Mode.CLEAR_GLASS,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));

    public static final RegistryObject<Block> OSMOTIC_ENCHANTER = thaumicTinkererDeviceBlock("osmotic_enchanter", ThaumicTinkererDeviceBlock.Mode.OSMOTIC_ENCHANTER,
            BlockBehaviour.Properties.of(Material.STONE).strength(6.0F, 12.0F).requiresCorrectToolForDrops().lightLevel(state -> 6));
    public static final RegistryObject<Block> ETHEREAL_PLATFORM = etherealPlatformBlock("ethereal_platform",
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.0F, 3.0F).lightLevel(state -> 3).noOcclusion().noCollission());
    public static final RegistryObject<Block> FUME_DISSIPATOR = fumeDissipatorBlock("fume_dissipator",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> TRANSVECTOR_INTERFACE = transvectorInterfaceBlock("transvector_interface",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> ESSENTIA_DRIVE = essentiaDriveBlock("essentia_drive",
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> ESSENTIA_STORAGE_MONITOR = thaumicEnergisticsDeviceBlock("essentia_storage_monitor", ThaumicEnergisticsDeviceBlock.Mode.STORAGE_MONITOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 6));
    public static final RegistryObject<Block> ARCANE_ASSEMBLER = thaumicEnergisticsDeviceBlock("arcane_assembler", ThaumicEnergisticsDeviceBlock.Mode.ARCANE_ASSEMBLER,
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 6));
    public static final RegistryObject<Block> ESSENTIA_PROVIDER = thaumicEnergisticsDeviceBlock("essentia_provider", ThaumicEnergisticsDeviceBlock.Mode.ESSENTIA_PROVIDER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> INFUSION_PROVIDER = thaumicEnergisticsDeviceBlock("infusion_provider", ThaumicEnergisticsDeviceBlock.Mode.INFUSION_PROVIDER,
            BlockBehaviour.Properties.of(Material.METAL).strength(4.5F, 10.0F).requiresCorrectToolForDrops().lightLevel(state -> 7));
    public static final RegistryObject<Block> ESSENTIA_CELL_WORKBENCH = thaumicEnergisticsDeviceBlock("essentia_cell_workbench", ThaumicEnergisticsDeviceBlock.Mode.ESSENTIA_CELL_WORKBENCH,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> DISTILLATION_PATTERN_ENCODER = thaumicEnergisticsDeviceBlock("distillation_pattern_encoder", ThaumicEnergisticsDeviceBlock.Mode.DISTILLATION_PATTERN_ENCODER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 3));
    public static final RegistryObject<Block> ESSENTIA_VIBRATION_CHAMBER = thaumicEnergisticsDeviceBlock("essentia_vibration_chamber", ThaumicEnergisticsDeviceBlock.Mode.ESSENTIA_VIBRATION_CHAMBER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 8));

    public static final RegistryObject<Block> GEAR_BOX = thaumicEnergisticsDeviceBlock("gear_box", ThaumicEnergisticsDeviceBlock.Mode.GEAR_BOX,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> GOLEM_GEAR_BOX = thaumicEnergisticsDeviceBlock("golem_gear_box", ThaumicEnergisticsDeviceBlock.Mode.GOLEM_GEAR_BOX,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> KNOWLEDGE_INSCRIBER = thaumicEnergisticsDeviceBlock("knowledge_inscriber", ThaumicEnergisticsDeviceBlock.Mode.KNOWLEDGE_INSCRIBER,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> ARCANE_CRAFTING_TERMINAL = thaumicEnergisticsDeviceBlock("arcane_crafting_terminal", ThaumicEnergisticsDeviceBlock.Mode.ARCANE_CRAFTING_TERMINAL,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));
    public static final RegistryObject<Block> ESSENTIA_LEVEL_EMITTER = thaumicEnergisticsDeviceBlock("essentia_level_emitter", ThaumicEnergisticsDeviceBlock.Mode.ESSENTIA_LEVEL_EMITTER,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 6));
    public static final RegistryObject<Block> ESSENTIA_CONVERSION_MONITOR = thaumicEnergisticsDeviceBlock("essentia_conversion_monitor", ThaumicEnergisticsDeviceBlock.Mode.ESSENTIA_CONVERSION_MONITOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));
    public static final RegistryObject<Block> VIS_INTERFACE = thaumicEnergisticsDeviceBlock("vis_interface", ThaumicEnergisticsDeviceBlock.Mode.VIS_INTERFACE,
            BlockBehaviour.Properties.of(Material.METAL).strength(2.5F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));

    public static final RegistryObject<Block> THAUMIC_ME_CONTROLLER = thaumicEnergisticsDeviceBlock("thaumic_me_controller", ThaumicEnergisticsDeviceBlock.Mode.ME_CONTROLLER,
            BlockBehaviour.Properties.of(Material.METAL).strength(5.0F, 10.0F).requiresCorrectToolForDrops().lightLevel(state -> 7));
    public static final RegistryObject<Block> THAUMIC_ME_CABLE = thaumicEnergisticsDeviceBlock("thaumic_me_cable", ThaumicEnergisticsDeviceBlock.Mode.ME_CABLE,
            BlockBehaviour.Properties.of(Material.METAL).strength(1.2F, 4.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final RegistryObject<Block> THAUMIC_CRAFTING_CPU = thaumicEnergisticsDeviceBlock("thaumic_crafting_cpu", ThaumicEnergisticsDeviceBlock.Mode.CRAFTING_CPU,
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));
    public static final RegistryObject<Block> THAUMIC_ENERGY_ACCEPTOR = thaumicEnergisticsDeviceBlock("thaumic_energy_acceptor", ThaumicEnergisticsDeviceBlock.Mode.ENERGY_ACCEPTOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 4));

    public static final RegistryObject<Block> ARCANE_PEDESTAL = pedestalBlock("arcane_pedestal",
            BlockBehaviour.Properties.of(Material.STONE).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final RegistryObject<Block> INFUSION_MATRIX = infusionMatrixBlock("infusion_matrix",
            BlockBehaviour.Properties.of(Material.METAL).strength(5.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 8));
    public static final RegistryObject<Block> MATRIX_ACCELERATOR = infusionMatrixAuxiliaryBlock("matrix_accelerator", InfusionMatrixAuxiliaryBlock.Mode.ACCELERATOR,
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 7));
    public static final RegistryObject<Block> MATRIX_STABILIZER = infusionMatrixAuxiliaryBlock("matrix_stabilizer", InfusionMatrixAuxiliaryBlock.Mode.STABILIZER,
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));
    public static final RegistryObject<Block> GOLEM_SEAL_COLLECT_BLOCK = block("golem_seal_collect_block",
            BlockBehaviour.Properties.of(Material.STONE).strength(1.0F, 2.0F).lightLevel(state -> 5));

    public static final RegistryObject<Block> AURA_NODE = auraNodeBlock("aura_node",
            BlockBehaviour.Properties.of(Material.GLASS).strength(0.2F, 0.2F).lightLevel(state -> 12).noOcclusion().noCollission());

    public static final RegistryObject<Block> NODE_STABILIZER = nodeStabilizerBlock("node_stabilizer",
            BlockBehaviour.Properties.of(Material.METAL).strength(4.0F, 8.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));

    public static final RegistryObject<Block> ADVANCED_NODE_STABILIZER = advancedNodeStabilizerBlock("advanced_node_stabilizer",
            BlockBehaviour.Properties.of(Material.METAL).strength(5.0F, 10.0F).requiresCorrectToolForDrops().lightLevel(state -> 7));

    public static final RegistryObject<Block> NODE_TRANSDUCER = nodeTransducerBlock("node_transducer",
            BlockBehaviour.Properties.of(Material.METAL).strength(4.5F, 9.0F).requiresCorrectToolForDrops().lightLevel(state -> 6));

    public static final RegistryObject<Block> VIS_RELAY = visRelayBlock("vis_relay",
            BlockBehaviour.Properties.of(Material.GLASS).strength(1.5F, 4.0F).requiresCorrectToolForDrops().lightLevel(state -> 8).noOcclusion());

    public static final RegistryObject<Block> AER_CRYSTAL = crystalBlock("aer_crystal", 9);
    public static final RegistryObject<Block> TERRA_CRYSTAL = crystalBlock("terra_crystal", 7);
    public static final RegistryObject<Block> IGNIS_CRYSTAL = crystalBlock("ignis_crystal", 10);
    public static final RegistryObject<Block> AQUA_CRYSTAL = crystalBlock("aqua_crystal", 8);
    public static final RegistryObject<Block> ORDO_CRYSTAL = crystalBlock("ordo_crystal", 11);
    public static final RegistryObject<Block> PERDITIO_CRYSTAL = crystalBlock("perditio_crystal", 7);

    public static final RegistryObject<BlockEntityType<TemporaryHoleBlockEntity>> TEMPORARY_HOLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("temporary_hole", () -> BlockEntityType.Builder.of(TemporaryHoleBlockEntity::new, TEMPORARY_HOLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<WardedBlockEntity>> WARDED_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("warded_block", () -> BlockEntityType.Builder.of(WardedBlockEntity::new, WARDED_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArcaneWorkbenchBlockEntity>> ARCANE_WORKBENCH_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("arcane_workbench", () -> BlockEntityType.Builder.of(ArcaneWorkbenchBlockEntity::new, ARCANE_WORKBENCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<FocalManipulatorBlockEntity>> FOCAL_MANIPULATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("focal_manipulator", () -> BlockEntityType.Builder.of(
                    FocalManipulatorBlockEntity::new, FOCAL_MANIPULATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("research_table", () -> BlockEntityType.Builder.of(ResearchTableBlockEntity::new, RESEARCH_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<DeconstructionTableBlockEntity>> DECONSTRUCTION_TABLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("deconstruction_table", () -> BlockEntityType.Builder.of(DeconstructionTableBlockEntity::new, DECONSTRUCTION_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, CRUCIBLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<EssentiaJarBlockEntity>> ESSENTIA_JAR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("essentia_jar", () -> BlockEntityType.Builder.of(EssentiaJarBlockEntity::new, ESSENTIA_JAR.get(), FILTERED_ESSENTIA_JAR.get(), VOID_ESSENTIA_JAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<EssentiaReservoirBlockEntity>> ESSENTIA_RESERVOIR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("essentia_reservoir", () -> BlockEntityType.Builder.of(EssentiaReservoirBlockEntity::new, ESSENTIA_RESERVOIR.get()).build(null));

    public static final RegistryObject<BlockEntityType<AlembicBlockEntity>> ALEMBIC_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("alembic", () -> BlockEntityType.Builder.of(AlembicBlockEntity::new, ALEMBIC.get()).build(null));

    public static final RegistryObject<BlockEntityType<AlchemicalCentrifugeBlockEntity>> ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("alchemical_centrifuge", () -> BlockEntityType.Builder.of(AlchemicalCentrifugeBlockEntity::new, ALCHEMICAL_CENTRIFUGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<EssentiaCrystalizerBlockEntity>> ESSENTIA_CRYSTALIZER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("essentia_crystalizer", () -> BlockEntityType.Builder.of(EssentiaCrystalizerBlockEntity::new, ESSENTIA_CRYSTALIZER.get()).build(null));

    public static final RegistryObject<BlockEntityType<EssentiaTubeBlockEntity>> ESSENTIA_TUBE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("essentia_tube", () -> BlockEntityType.Builder.of(EssentiaTubeBlockEntity::new,
                    ESSENTIA_TUBE.get(),
                    ESSENTIA_TUBE_FILTER.get(),
                    ESSENTIA_TUBE_RESTRICT.get(),
                    ESSENTIA_TUBE_ONEWAY.get(),
                    ESSENTIA_TUBE_BUFFER.get(),
                    ESSENTIA_VALVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<AlchemicalFurnaceBlockEntity>> ALCHEMICAL_FURNACE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("alchemical_furnace", () -> BlockEntityType.Builder.of(AlchemicalFurnaceBlockEntity::new, ALCHEMICAL_FURNACE.get(), ADVANCED_ALCHEMICAL_FURNACE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ThaumatoriumBlockEntity>> THAUMATORIUM_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("thaumatorium", () -> BlockEntityType.Builder.of(ThaumatoriumBlockEntity::new, THAUMATORIUM.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArcanePedestalBlockEntity>> ARCANE_PEDESTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("arcane_pedestal", () -> BlockEntityType.Builder.of(ArcanePedestalBlockEntity::new, ARCANE_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<InfusionMatrixBlockEntity>> INFUSION_MATRIX_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infusion_matrix", () -> BlockEntityType.Builder.of(InfusionMatrixBlockEntity::new, INFUSION_MATRIX.get()).build(null));

    public static final RegistryObject<BlockEntityType<EldritchPortalBlockEntity>> ELDRITCH_PORTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eldritch_portal", () -> BlockEntityType.Builder.of(EldritchPortalBlockEntity::new, ELDRITCH_PORTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<EldritchCrabSpawnerBlockEntity>> ELDRITCH_CRAB_SPAWNER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eldritch_crab_spawner", () -> BlockEntityType.Builder.of(EldritchCrabSpawnerBlockEntity::new, ELDRITCH_CRAB_SPAWNER.get()).build(null));

    public static final RegistryObject<BlockEntityType<EldritchCapBlockEntity>> ELDRITCH_CAP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eldritch_cap", () -> BlockEntityType.Builder.of(EldritchCapBlockEntity::new, ELDRITCH_CAP.get()).build(null));

    public static final RegistryObject<BlockEntityType<EldritchLockBlockEntity>> ELDRITCH_LOCK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eldritch_lock", () -> BlockEntityType.Builder.of(EldritchLockBlockEntity::new, ELDRITCH_LOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<EldritchTrapBlockEntity>> ELDRITCH_TRAP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eldritch_trap", () -> BlockEntityType.Builder.of(EldritchTrapBlockEntity::new, ELDRITCH_TRAP.get()).build(null));

    public static final RegistryObject<BlockEntityType<EldritchCrystalBlockEntity>> ELDRITCH_CRYSTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eldritch_crystal", () -> BlockEntityType.Builder.of(EldritchCrystalBlockEntity::new, ELDRITCH_CRYSTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<AuraNodeBlockEntity>> AURA_NODE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("aura_node", () -> BlockEntityType.Builder.of(AuraNodeBlockEntity::new, AURA_NODE.get()).build(null));

    public static final RegistryObject<BlockEntityType<EssentiaDriveBlockEntity>> ESSENTIA_DRIVE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("essentia_drive", () -> BlockEntityType.Builder.of(EssentiaDriveBlockEntity::new, ESSENTIA_DRIVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TransvectorInterfaceBlockEntity>> TRANSVECTOR_INTERFACE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("transvector_interface", () -> BlockEntityType.Builder.of(TransvectorInterfaceBlockEntity::new, TRANSVECTOR_INTERFACE.get()).build(null));

    public static final RegistryObject<MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH_MENU =
            MENUS.register("arcane_workbench", () -> IForgeMenuType.create((windowId, inv, data) -> new ArcaneWorkbenchMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<FocalManipulatorMenu>> FOCAL_MANIPULATOR_MENU =
            MENUS.register("focal_manipulator", () -> IForgeMenuType.create(
                    (windowId, inv, data) -> new FocalManipulatorMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<ResearchTableMenu>> RESEARCH_TABLE_MENU =
            MENUS.register("research_table", () -> IForgeMenuType.create((windowId, inv, data) -> new ResearchTableMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<DeconstructionTableMenu>> DECONSTRUCTION_TABLE_MENU =
            MENUS.register("deconstruction_table", () -> IForgeMenuType.create((windowId, inv, data) -> new DeconstructionTableMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<ThaumatoriumMenu>> THAUMATORIUM_MENU =
            MENUS.register("thaumatorium", () -> IForgeMenuType.create((windowId, inv, data) -> new ThaumatoriumMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<PechTradeMenu>> PECH_TRADE_MENU =
            MENUS.register("pech_trade", () -> IForgeMenuType.create((windowId, inv, data) -> new PechTradeMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<EssentiaTerminalMenu>> ESSENTIA_TERMINAL_MENU =
            MENUS.register("essentia_terminal", () -> IForgeMenuType.create((windowId, inv, data) -> new EssentiaTerminalMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<EssentiaDriveMenu>> ESSENTIA_DRIVE_MENU =
            MENUS.register("essentia_drive", () -> IForgeMenuType.create((windowId, inv, data) -> new EssentiaDriveMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<OsmoticEnchanterMenu>> OSMOTIC_ENCHANTER_MENU =
            MENUS.register("osmotic_enchanter", () -> IForgeMenuType.create((windowId, inv, data) -> new OsmoticEnchanterMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<TransvectorInterfaceMenu>> TRANSVECTOR_INTERFACE_MENU =
            MENUS.register("transvector_interface", () -> IForgeMenuType.create((windowId, inv, data) -> new TransvectorInterfaceMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<BottomlessPouchMenu>> BOTTOMLESS_POUCH_MENU =
            MENUS.register("bottomless_pouch", () -> IForgeMenuType.create((windowId, inv, data) -> new BottomlessPouchMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<FocusPouchMenu>> FOCUS_POUCH_MENU =
            MENUS.register("focus_pouch", () -> IForgeMenuType.create((windowId, inv, data) -> new FocusPouchMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<GolemMenu>> GOLEM_MENU =
            MENUS.register("golem", () -> IForgeMenuType.create((windowId, inv, data) -> new GolemMenu(windowId, inv, data)));

    public static final RegistryObject<EntityType<ThaumGolemEntity>> THAUM_GOLEM =
            ENTITY_TYPES.register("thaum_golem", () -> EntityType.Builder.of(ThaumGolemEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.1F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":thaum_golem"));

    public static final RegistryObject<EntityType<TaintCrawlerEntity>> TAINT_CRAWLER =
            ENTITY_TYPES.register("taint_crawler", () -> EntityType.Builder.of(TaintCrawlerEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 0.45F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":taint_crawler"));

    public static final RegistryObject<EntityType<PechEntity>> PECH =
            ENTITY_TYPES.register("pech", () -> EntityType.Builder.of(PechEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.2F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":pech"));

    public static final RegistryObject<EntityType<EldritchGuardianEntity>> ELDRITCH_GUARDIAN =
            ENTITY_TYPES.register("eldritch_guardian", () -> EntityType.Builder.of(EldritchGuardianEntity::new, MobCategory.MONSTER)
                    .sized(0.72F, 2.2F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":eldritch_guardian"));

    public static final RegistryObject<EntityType<EldritchCrabEntity>> ELDRITCH_CRAB =
            ENTITY_TYPES.register("eldritch_crab", () -> EntityType.Builder.of(EldritchCrabEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.6F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":eldritch_crab"));

    public static final RegistryObject<EntityType<MindSpiderEntity>> MIND_SPIDER =
            ENTITY_TYPES.register("mind_spider", () -> EntityType.Builder.<MindSpiderEntity>of(MindSpiderEntity::new, MobCategory.MONSTER)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":mind_spider"));

    public static final RegistryObject<EntityType<EldritchWardenEntity>> ELDRITCH_WARDEN =
            ENTITY_TYPES.register("eldritch_warden", () -> EntityType.Builder.of(EldritchWardenEntity::new, MobCategory.MONSTER)
                    .sized(1.5F, 3.5F)
                    .clientTrackingRange(10)
                    .build(MOD_ID + ":eldritch_warden"));

    public static final RegistryObject<EntityType<EldritchGolemEntity>> ELDRITCH_GOLEM =
            ENTITY_TYPES.register("eldritch_golem", () -> EntityType.Builder.of(EldritchGolemEntity::new, MobCategory.MONSTER)
                    .sized(1.75F, 3.5F)
                    .clientTrackingRange(10)
                    .build(MOD_ID + ":eldritch_golem"));

    public static final RegistryObject<EntityType<CrimsonCultistEntity>> CRIMSON_CULTIST =
            ENTITY_TYPES.register("crimson_cultist", () -> EntityType.Builder.<CrimsonCultistEntity>of((type, level) -> new CrimsonCultistEntity(type, level, CrimsonCultistEntity.Role.CULTIST), MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":crimson_cultist"));

    public static final RegistryObject<EntityType<CrimsonCultistEntity>> CRIMSON_KNIGHT =
            ENTITY_TYPES.register("crimson_knight", () -> EntityType.Builder.<CrimsonCultistEntity>of((type, level) -> new CrimsonCultistEntity(type, level, CrimsonCultistEntity.Role.KNIGHT), MobCategory.MONSTER)
                    .sized(0.7F, 2.05F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":crimson_knight"));

    public static final RegistryObject<EntityType<CrimsonCultistEntity>> CRIMSON_CLERIC =
            ENTITY_TYPES.register("crimson_cleric", () -> EntityType.Builder.<CrimsonCultistEntity>of((type, level) -> new CrimsonCultistEntity(type, level, CrimsonCultistEntity.Role.CLERIC), MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":crimson_cleric"));

    public static final RegistryObject<EntityType<CrimsonCultistEntity>> CRIMSON_PRAETOR =
            ENTITY_TYPES.register("crimson_praetor", () -> EntityType.Builder.<CrimsonCultistEntity>of((type, level) -> new CrimsonCultistEntity(type, level, CrimsonCultistEntity.Role.LEADER), MobCategory.MONSTER)
                    .sized(0.74F, 2.1F)
                    .clientTrackingRange(10)
                    .build(MOD_ID + ":crimson_praetor"));



    public static final RegistryObject<EntityType<CultistPortalEntity>> CULTIST_PORTAL =
            ENTITY_TYPES.register("cultist_portal", () -> EntityType.Builder.of(CultistPortalEntity::new, MobCategory.MONSTER)
                    .sized(1.5F, 3.0F)
                    .clientTrackingRange(12)
                    .build(MOD_ID + ":cultist_portal"));


    public static final RegistryObject<EntityType<TaintacleEntity>> TAINTACLE =
            ENTITY_TYPES.register("taintacle", () -> EntityType.Builder.of(TaintacleEntity::new, MobCategory.MONSTER)
                    .sized(0.66F, 3.0F)
                    .clientTrackingRange(10)
                    .build(MOD_ID + ":taintacle"));

    public static final RegistryObject<EntityType<TaintacleSmallEntity>> TAINTACLE_SMALL =
            ENTITY_TYPES.register("taintacle_small", () -> EntityType.Builder.of(TaintacleSmallEntity::new, MobCategory.MONSTER)
                    .sized(0.22F, 1.0F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":taintacle_small"));

    public static final RegistryObject<EntityType<TaintacleGiantEntity>> TAINTACLE_GIANT =
            ENTITY_TYPES.register("taintacle_giant", () -> EntityType.Builder.of(TaintacleGiantEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 6.0F)
                    .clientTrackingRange(12)
                    .build(MOD_ID + ":taintacle_giant"));


    public static final RegistryObject<EntityType<TC4FireBatEntity>> FIREBAT =
            ENTITY_TYPES.register("firebat", () -> EntityType.Builder.<TC4FireBatEntity>of(TC4FireBatEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.9F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(MOD_ID + ":firebat"));

    public static final RegistryObject<EntityType<TC4PechBlastEntity>> FOCUS_PECH_BLAST =
            ENTITY_TYPES.register("focus_pech_blast", () -> EntityType.Builder.<TC4PechBlastEntity>of(TC4PechBlastEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(MOD_ID + ":focus_pech_blast"));

    public static final RegistryObject<EntityType<TC4EmberEntity>> FOCUS_EMBER =
            ENTITY_TYPES.register("focus_ember", () -> EntityType.Builder.<TC4EmberEntity>of(TC4EmberEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build(MOD_ID + ":focus_ember"));

    public static final RegistryObject<EntityType<TC4FrostShardEntity>> FOCUS_FROST_SHARD =
            ENTITY_TYPES.register("focus_frost_shard", () -> EntityType.Builder.<TC4FrostShardEntity>of(TC4FrostShardEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build(MOD_ID + ":focus_frost_shard"));

    public static final RegistryObject<EntityType<TC4ExplosiveOrbEntity>> FOCUS_EXPLOSIVE_ORB =
            ENTITY_TYPES.register("focus_explosive_orb", () -> EntityType.Builder.<TC4ExplosiveOrbEntity>of(TC4ExplosiveOrbEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build(MOD_ID + ":focus_explosive_orb"));

    public static final RegistryObject<EntityType<TC4ShockOrbEntity>> FOCUS_SHOCK_ORB =
            ENTITY_TYPES.register("focus_shock_orb", () -> EntityType.Builder.<TC4ShockOrbEntity>of(TC4ShockOrbEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(MOD_ID + ":focus_shock_orb"));

    public static final RegistryObject<EntityType<TC4PrimalOrbEntity>> FOCUS_PRIMAL_ORB =
            ENTITY_TYPES.register("focus_primal_orb", () -> EntityType.Builder.<TC4PrimalOrbEntity>of(TC4PrimalOrbEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(MOD_ID + ":focus_primal_orb"));

    public static final RegistryObject<EntityType<TC4EldritchOrbEntity>> ELDRITCH_ORB =
            ENTITY_TYPES.register("eldritch_orb", () -> EntityType.Builder.<TC4EldritchOrbEntity>of(TC4EldritchOrbEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(MOD_ID + ":eldritch_orb"));

    public static final RegistryObject<EntityType<TC4GolemOrbEntity>> GOLEM_ORB =
            ENTITY_TYPES.register("golem_orb", () -> EntityType.Builder.<TC4GolemOrbEntity>of(TC4GolemOrbEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(MOD_ID + ":golem_orb"));

    public ThaumcraftMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ThaumcraftConfig.SPEC);
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
        SOUND_EVENTS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(this::onEntityAttributeCreation);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        ThaumcraftNetwork.register();
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(THAUM_GOLEM.get(), ThaumGolemEntity.createAttributes().build());
        event.put(TAINT_CRAWLER.get(), TaintCrawlerEntity.createAttributes().build());
        event.put(FIREBAT.get(), TC4FireBatEntity.createAttributes().build());
        event.put(PECH.get(), PechEntity.createAttributes().build());
        event.put(ELDRITCH_GUARDIAN.get(), EldritchGuardianEntity.createAttributes().build());
        event.put(ELDRITCH_CRAB.get(), EldritchCrabEntity.createAttributes().build());
        event.put(MIND_SPIDER.get(), MindSpiderEntity.createAttributes().build());
        event.put(ELDRITCH_WARDEN.get(), EldritchWardenEntity.createAttributes().build());
        event.put(ELDRITCH_GOLEM.get(), EldritchGolemEntity.createAttributes().build());
        event.put(CRIMSON_CULTIST.get(), CrimsonCultistEntity.createCultistAttributes().build());
        event.put(CRIMSON_KNIGHT.get(), CrimsonCultistEntity.createKnightAttributes().build());
        event.put(CRIMSON_CLERIC.get(), CrimsonCultistEntity.createClericAttributes().build());
        event.put(CRIMSON_PRAETOR.get(), CrimsonCultistEntity.createLeaderAttributes().build());
        event.put(CULTIST_PORTAL.get(), CultistPortalEntity.createAttributes().build());
        event.put(TAINTACLE.get(), TaintacleEntity.createAttributes().build());
        event.put(TAINTACLE_SMALL.get(), TaintacleSmallEntity.createAttributes().build());
        event.put(TAINTACLE_GIANT.get(), TaintacleGiantEntity.createAttributes().build());
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AlchemyRecipeManager());
        event.addListener(new ArcaneWorkbenchRecipeManager());
        event.addListener(new InfusionRecipeManager());
    }

    private static RegistryObject<Item> item(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(THAUMCRAFT_TAB)));
    }

    private static RegistryObject<Item> specialItem(String name, java.util.function.Supplier<Item> item) {
        return ITEMS.register(name, item);
    }

    private static RegistryObject<Item> focusItem(String name, WandFocusType type) {
        return ITEMS.register(name, () -> new WandFocusItem(new Item.Properties().tab(THAUMCRAFT_TAB), type));
    }

    private static RegistryObject<Item> extrasFocus(String name, ThaumcraftExtrasFocusItem.Mode mode) {
        return ITEMS.register(name, () -> new ThaumcraftExtrasFocusItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1), mode));
    }

    private static RegistryObject<Item> pechToken(String name, int tier) {
        return ITEMS.register(name, () -> new PechTradeTokenItem(new Item.Properties().tab(THAUMCRAFT_TAB), tier));
    }

    private static RegistryObject<Block> temporaryHoleBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new TemporaryHoleBlock(properties));
    }

    private static RegistryObject<Block> electricShockBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new ElectricShockBlock(properties));
    }

    private static RegistryObject<Block> block(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new Block(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> nitorLightBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new NitorLightBlock(properties));
    }

    private static RegistryObject<Block> pillarBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new RotatedPillarBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> extrasElementBlock(String name, ThaumcraftExtrasElementalBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ThaumcraftExtrasElementalBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> thaumicEnergisticsDeviceBlock(String name, ThaumicEnergisticsDeviceBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ThaumicEnergisticsDeviceBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> thaumicTinkererDeviceBlock(String name, ThaumicTinkererDeviceBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ThaumicTinkererDeviceBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> transvectorInterfaceBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TransvectorInterfaceBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> etherealPlatformBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EtherealPlatformBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> fumeDissipatorBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FumeDissipatorBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> essentiaDriveBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EssentiaDriveBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> bellowsBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new BellowsBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> tableBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TableBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> researchTableBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ResearchTableBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> deconstructionTableBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new DeconstructionTableBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> arcaneWorkbenchBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ArcaneWorkbenchBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> focalManipulatorBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FocalManipulatorBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> tc4SaplingBlock(String name, TC4SaplingBlock.Kind kind, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TC4SaplingBlock(properties, kind));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> taintedSoilBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TaintedSoilBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> taintBlock(String name, TaintBlock.Variant variant, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TaintBlock(properties, variant));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> taintFibresBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TaintFibresBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }


    private static RegistryObject<Block> fluxGooBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FluxGooBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> fluxGasBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FluxGasBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> crucibleBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new CrucibleBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> alembicBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new AlembicBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> alchemicalCentrifugeBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new AlchemicalCentrifugeBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> essentiaCrystalizerBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EssentiaCrystalizerBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> essentiaReservoirBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EssentiaReservoirBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> thaumatoriumBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ThaumatoriumBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> essentiaTubeBlock(String name, BlockBehaviour.Properties properties) {
        return essentiaTubeBlock(name, EssentiaTubeSubtype.NORMAL, properties);
    }

    private static RegistryObject<Block> essentiaTubeBlock(String name, EssentiaTubeSubtype subtype, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EssentiaTubeBlock(properties, subtype));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> essentiaValveBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EssentiaValveBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> alchemicalFurnaceBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new AlchemicalFurnaceBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> essentiaJarBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EssentiaJarBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> filteredEssentiaJarBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FilteredEssentiaJarBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> voidEssentiaJarBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new VoidEssentiaJarBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> eldritchAltarBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EldritchAltarBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> eldritchPortalBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EldritchPortalBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> eldritchCrabSpawnerBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new EldritchCrabSpawnerBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> tc4LootBlock(String name, TC4LootBlock.Kind kind, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new TC4LootBlock(properties, kind));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> pedestalBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ArcanePedestalBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> infusionMatrixBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new InfusionMatrixBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> matrixAuxiliaryBlock(String name, MatrixAuxiliaryBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new MatrixAuxiliaryBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> infusionMatrixAuxiliaryBlock(String name, InfusionMatrixAuxiliaryBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new InfusionMatrixAuxiliaryBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> nodeStabilizerBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new NodeStabilizerBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> advancedNodeStabilizerBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new AdvancedNodeStabilizerBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> nodeTransducerBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new NodeTransducerBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> visRelayBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new VisRelayBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> auraNodeBlock(String name, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new AuraNodeBlock(properties));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> crystalBlock(String name, int light) {
        return block(name, BlockBehaviour.Properties.of(Material.GLASS)
                .strength(1.5F, 3.0F)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> light));
    }

    private static RegistryObject<Item> ttParityItem(String name, ThaumicTinkererParityItem.Mode mode) {
        return ITEMS.register(name, () -> new ThaumicTinkererParityItem(new Item.Properties().tab(THAUMCRAFT_TAB), mode));
    }

    private static RegistryObject<Item> tceParityItem(String name, ThaumcraftExtrasParityItem.Mode mode) {
        return ITEMS.register(name, () -> new ThaumcraftExtrasParityItem(new Item.Properties().tab(THAUMCRAFT_TAB), mode));
    }

    private static RegistryObject<Block> ttParityBlock(String name, ThaumicTinkererParityBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ThaumicTinkererParityBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }

    private static RegistryObject<Block> tceParityBlock(String name, ThaumcraftExtrasParityBlock.Mode mode, BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new ThaumcraftExtrasParityBlock(properties, mode));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)));
        return block;
    }


}
