package com.darkifov.thaumcraft.porting;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.WispEssenceItem;
import com.darkifov.thaumcraft.item.TC4CrimsonBladeItem;
import com.darkifov.thaumcraft.item.TC4LegacyTextureArtifactItem;
import com.darkifov.thaumcraft.item.TC4ObeliskPlacerItem;
import com.darkifov.thaumcraft.item.TC4ResonatorItem;
import com.darkifov.thaumcraft.item.TC4SinisterStoneItem;
import com.darkifov.thaumcraft.block.GolemBellItem;
import com.darkifov.thaumcraft.block.BellowsBlockItem;
import com.darkifov.thaumcraft.block.AlembicBlockItem;
import com.darkifov.thaumcraft.block.EssentiaJarBlockItem;
import com.darkifov.thaumcraft.block.InfusionMatrixBlockItem;
import com.darkifov.thaumcraft.block.NodeStabilizerItem;
import com.darkifov.thaumcraft.block.NodeTransducerItem;
import com.darkifov.thaumcraft.item.AlumentumItem;
import com.darkifov.thaumcraft.item.ArcaneKeyItem;
import com.darkifov.thaumcraft.item.BottleTaintItem;
import com.darkifov.thaumcraft.item.ElementalAxeItem;
import com.darkifov.thaumcraft.item.ElementalHoeItem;
import com.darkifov.thaumcraft.item.ElementalPickaxeItem;
import com.darkifov.thaumcraft.item.ElementalShovelItem;
import com.darkifov.thaumcraft.item.ElementalSwordItem;
import com.darkifov.thaumcraft.item.PrimalArrowItem;
import com.darkifov.thaumcraft.item.PrimalCrusherItem;
import com.darkifov.thaumcraft.block.TC4FortressArmorItem;
import com.darkifov.thaumcraft.block.TC4FortressMaskItem;
import com.darkifov.thaumcraft.item.TC4ResearchComponentItem;
import com.darkifov.thaumcraft.item.TC4SimpleResourceItem;
import com.darkifov.thaumcraft.item.TC4EdibleNuggetItem;
import com.darkifov.thaumcraft.item.TC4OreClusterItem;
import com.darkifov.thaumcraft.item.TC4EmptyGolemUpgradeItem;
import com.darkifov.thaumcraft.item.ZombieBrainItem;
import com.darkifov.thaumcraft.item.TripleMeatTreatItem;
import com.darkifov.thaumcraft.item.TC4KnowledgeFragmentItem;
import com.darkifov.thaumcraft.item.TC4WandComponentItem;
import com.darkifov.thaumcraft.item.TC4LootBagItem;
import com.darkifov.thaumcraft.item.TC4CrimsonRitesItem;
import com.darkifov.thaumcraft.item.TC4EldritchObjectItem;
import com.darkifov.thaumcraft.item.TC4PrimordialPearlItem;
import com.darkifov.thaumcraft.item.TC4SanityCheckerItem;
import com.darkifov.thaumcraft.item.TC4EssenceItem;
import com.darkifov.thaumcraft.item.simple.TC4BaubleItem;
import com.darkifov.thaumcraft.item.simple.TC4RunicBaubleItem;
import com.darkifov.thaumcraft.item.simple.TC4LegacyFocusComponentItem;
import com.darkifov.thaumcraft.item.simple.TC4ManaBeanItem;
import com.darkifov.thaumcraft.item.simple.TC4VisAmuletItem;
import com.darkifov.thaumcraft.item.simple.TC4ClothRobeItem;
import com.darkifov.thaumcraft.item.simple.TC4CultistArmorItem;
import com.darkifov.thaumcraft.item.simple.TC4VoidRobeItem;
import com.darkifov.thaumcraft.block.JarLabelItem;
import com.darkifov.thaumcraft.block.ThaumonomiconItem;
import com.darkifov.thaumcraft.block.CreativeThaumonomiconItem;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.FocusPouchBaubleItem;
import com.darkifov.thaumcraft.block.GogglesOfRevealingItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.block.SanitySoapItem;
import com.darkifov.thaumcraft.block.ShardItem;
import com.darkifov.thaumcraft.block.NitorItem;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.item.TaintedResourceItem;
import com.darkifov.thaumcraft.item.BoneBowItem;
import com.darkifov.thaumcraft.item.TravelingTrunkItem;
import com.darkifov.thaumcraft.item.gear.BootsOfTravellerItem;
import com.darkifov.thaumcraft.item.gear.HoverHarnessItem;
import com.darkifov.thaumcraft.item.gear.HoverGirdleItem;
import com.darkifov.thaumcraft.item.gear.TC4ThaumiumArmorItem;
import com.darkifov.thaumcraft.item.gear.TC4ThaumiumAxeItem;
import com.darkifov.thaumcraft.item.gear.TC4ThaumiumHoeItem;
import com.darkifov.thaumcraft.item.gear.TC4ThaumiumPickaxeItem;
import com.darkifov.thaumcraft.item.gear.TC4ThaumiumShovelItem;
import com.darkifov.thaumcraft.item.gear.TC4ThaumiumSwordItem;
import com.darkifov.thaumcraft.item.gear.TC4VoidArmorItem;
import com.darkifov.thaumcraft.item.gear.TC4VoidAxeItem;
import com.darkifov.thaumcraft.item.gear.TC4VoidHoeItem;
import com.darkifov.thaumcraft.item.gear.TC4VoidPickaxeItem;
import com.darkifov.thaumcraft.item.gear.TC4VoidShovelItem;
import com.darkifov.thaumcraft.item.gear.TC4VoidSwordItem;
import com.darkifov.thaumcraft.item.TC4GolemPlacerItem;
import com.darkifov.thaumcraft.item.TC4GolemCoreComponentItem;
import com.darkifov.thaumcraft.block.GolemUpgradeItem;
import com.darkifov.thaumcraft.block.GolemDecorationItem;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandRodType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stage119: de-metadata'd 1.19.2 registry mirror for TC4 research/research-recipe items.
 *
 * TC4 1.7.10 stored many components as one Item + metadata, for example
 * ConfigItems.itemResource meta 0..18 or ConfigItems.itemWandCap meta 0..8.
 * Minecraft 1.19.2 does not use item metadata, so this class registers stable
 * dedicated item ids such as thaumcraft:tc4_alumentum and thaumcraft:tc4_wand_cap_void.
 */
public final class TC4ResearchItems {
    public record Entry(String id, String texture, String originalSource, String legacyTexture) {
        public ResourceLocation registryName() { return new ResourceLocation("thaumcraft", id); }
        public ResourceLocation textureName() { return new ResourceLocation("thaumcraft", "textures/item/tc4/" + texture + ".png"); }
    }

    private static final Entry[] ENTRIES = new Entry[] {
            e("tc4_alumentum", "alumentum", "ConfigItems.itemResource meta 0", "alumentum"),
            e("tc4_amber", "amber", "ConfigItems.itemResource meta 6", "amber"),
            e("tc4_arcanedoor", "arcanedoor", "TC4 texture sprite from assets/thaumcraft/textures/items", "arcanedoor"),
            e("tc4_bath_salts", "bath_salts", "TC4 texture sprite from assets/thaumcraft/textures/items", "bath_salts"),
            e("tc4_bauble_amulet", "bauble_amulet", "TC4 texture sprite from assets/thaumcraft/textures/items", "bauble_amulet"),
            e("tc4_bauble_belt", "bauble_belt", "TC4 texture sprite from assets/thaumcraft/textures/items", "bauble_belt"),
            e("tc4_bauble_ring", "bauble_ring", "TC4 texture sprite from assets/thaumcraft/textures/items", "bauble_ring"),
            e("tc4_bauble_ring_iron", "bauble_ring_iron", "TC4 texture sprite from assets/thaumcraft/textures/items", "bauble_ring_iron"),
            e("tc4_bonebow", "bonebow", "TC4 texture sprite from assets/thaumcraft/textures/items", "bonebow"),
            e("tc4_bootstraveler", "bootstraveler", "TC4 texture sprite from assets/thaumcraft/textures/items", "bootstraveler"),
            e("tc4_bottle_taint", "bottle_taint", "TC4 texture sprite from assets/thaumcraft/textures/items", "bottle_taint"),
            e("tc4_brain", "brain", "ConfigItems.itemResource meta 5", "brain"),
            e("tc4_jar_brain", "jar_brain", "ConfigBlocks.blockJar meta 1 migration output", "jar_brain"),
            e("tc4_bucket_death", "bucket_death", "TC4 texture sprite from assets/thaumcraft/textures/items", "bucket_death"),
            e("tc4_bucket_pure", "bucket_pure", "TC4 texture sprite from assets/thaumcraft/textures/items", "bucket_pure"),
            e("tc4_charm", "charm", "ConfigItems.itemResource meta 15", "charm"),
            e("tc4_cloth", "cloth", "ConfigItems.itemResource meta 7", "cloth"),
            e("tc4_clothboots", "clothboots", "TC4 texture sprite from assets/thaumcraft/textures/items", "clothboots"),
            e("tc4_clothbootsover", "clothbootsover", "TC4 texture sprite from assets/thaumcraft/textures/items", "clothbootsover"),
            e("tc4_clothchest", "clothchest", "TC4 texture sprite from assets/thaumcraft/textures/items", "clothchest"),
            e("tc4_clothchestover", "clothchestover", "TC4 texture sprite from assets/thaumcraft/textures/items", "clothchestover"),
            e("tc4_clothlegs", "clothlegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "clothlegs"),
            e("tc4_clothlegsover", "clothlegsover", "TC4 texture sprite from assets/thaumcraft/textures/items", "clothlegsover"),
            e("tc4_clustercinnabar", "clustercinnabar", "ConfigItems.itemNugget meta 21", "clustercinnabar"),
            e("tc4_clustercopper", "clustercopper", "ConfigItems.itemNugget meta 17", "clustercopper"),
            e("tc4_clustergold", "clustergold", "ConfigItems.itemNugget meta 31", "clustergold"),
            e("tc4_clusteriron", "clusteriron", "ConfigItems.itemNugget meta 16", "clusteriron"),
            e("tc4_clusterlead", "clusterlead", "ConfigItems.itemNugget meta 20", "clusterlead"),
            e("tc4_clustersilver", "clustersilver", "ConfigItems.itemNugget meta 19", "clustersilver"),
            e("tc4_clustertin", "clustertin", "ConfigItems.itemNugget meta 18", "clustertin"),
            e("tc4_coin", "coin", "ConfigItems.itemResource meta 18", "coin"),
            e("tc4_crimson_blade", "crimson_blade", "TC4 texture sprite from assets/thaumcraft/textures/items", "crimson_blade"),
            e("tc4_crimson_rites", "crimson_rites", "ConfigItems.itemEldritchObject meta 1", "crimson_rites"),
            e("tc4_crystalessence", "crystalessence", "TC4 texture sprite from assets/thaumcraft/textures/items", "crystalessence"),
            e("tc4_cultistboots", "cultistboots", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistboots"),
            e("tc4_cultistplatechest", "cultistplatechest", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistplatechest"),
            e("tc4_cultistplatehelm", "cultistplatehelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistplatehelm"),
            e("tc4_cultistplateleaderchest", "cultistplateleaderchest", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistplateleaderchest"),
            e("tc4_cultistplateleaderhelm", "cultistplateleaderhelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistplateleaderhelm"),
            e("tc4_cultistplateleaderlegs", "cultistplateleaderlegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistplateleaderlegs"),
            e("tc4_cultistplatelegs", "cultistplatelegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistplatelegs"),
            e("tc4_cultistrobechest", "cultistrobechest", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistrobechest"),
            e("tc4_cultistrobehelm", "cultistrobehelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistrobehelm"),
            e("tc4_cultistrobelegs", "cultistrobelegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "cultistrobelegs"),
            e("tc4_discovery", "discovery", "TC4 texture sprite from assets/thaumcraft/textures/items", "discovery"),
            e("tc4_dust", "dust", "ConfigItems.itemResource meta 14", "dust"),
            e("tc4_el_arrow_air", "el_arrow_air", "TC4 texture sprite from assets/thaumcraft/textures/items", "el_arrow_air"),
            e("tc4_el_arrow_earth", "el_arrow_earth", "TC4 texture sprite from assets/thaumcraft/textures/items", "el_arrow_earth"),
            e("tc4_el_arrow_entropy", "el_arrow_entropy", "TC4 texture sprite from assets/thaumcraft/textures/items", "el_arrow_entropy"),
            e("tc4_el_arrow_fire", "el_arrow_fire", "TC4 texture sprite from assets/thaumcraft/textures/items", "el_arrow_fire"),
            e("tc4_el_arrow_order", "el_arrow_order", "TC4 texture sprite from assets/thaumcraft/textures/items", "el_arrow_order"),
            e("tc4_el_arrow_water", "el_arrow_water", "TC4 texture sprite from assets/thaumcraft/textures/items", "el_arrow_water"),
            e("tc4_eldritch_object", "eldritch_object", "ConfigItems.itemEldritchObject meta 0", "eldritch_object"),
            e("tc4_eldritch_object_2", "eldritch_object_2", "ConfigItems.itemEldritchObject meta 2", "eldritch_object_2"),
            e("tc4_eldritch_object_3", "eldritch_object_3", "ConfigItems.itemEldritchObject meta 3", "eldritch_object_3"),
            e("tc4_elementalaxe", "elementalaxe", "TC4 texture sprite from assets/thaumcraft/textures/items", "elementalaxe"),
            e("tc4_elementalhoe", "elementalhoe", "TC4 texture sprite from assets/thaumcraft/textures/items", "elementalhoe"),
            e("tc4_elementalpick", "elementalpick", "TC4 texture sprite from assets/thaumcraft/textures/items", "elementalpick"),
            e("tc4_elementalshovel", "elementalshovel", "TC4 texture sprite from assets/thaumcraft/textures/items", "elementalshovel"),
            e("tc4_elementalsword", "elementalsword", "TC4 texture sprite from assets/thaumcraft/textures/items", "elementalsword"),
            e("tc4_essence", "essence", "TC4 texture sprite from assets/thaumcraft/textures/items", "essence"),
            e("tc4_filter", "filter", "ConfigItems.itemResource meta 8", "filter"),
            e("tc4_focus", "focus", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus"),
            e("tc4_focus_excavation", "focus_excavation", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_excavation"),
            e("tc4_focus_fire", "focus_fire", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_fire"),
            e("tc4_focus_frost", "focus_frost", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_frost"),
            e("tc4_focus_hellbat", "focus_hellbat", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_hellbat"),
            e("tc4_focus_pech", "focus_pech", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_pech"),
            e("tc4_focus_portablehole", "focus_portablehole", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_portablehole"),
            e("tc4_focus_primal", "focus_primal", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_primal"),
            e("tc4_focus_reversal", "focus_reversal", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_reversal"),
            e("tc4_focus_shock", "focus_shock", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_shock"),
            e("tc4_focus_trade", "focus_trade", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_trade"),
            e("tc4_focus_warding", "focus_warding", "TC4 texture sprite from assets/thaumcraft/textures/items", "focus_warding"),
            e("tc4_focuspouch", "focuspouch", "TC4 texture sprite from assets/thaumcraft/textures/items", "focuspouch"),
            e("tc4_focuspouchbauble", "focuspouchbauble", "TC4 texture sprite from assets/thaumcraft/textures/items", "focuspouchbauble"),
            e("tc4_gogglesrevealing", "gogglesrevealing", "TC4 texture sprite from assets/thaumcraft/textures/items", "gogglesrevealing"),
            e("tc4_golem_clay", "golem_clay", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_clay"),
            e("tc4_golem_core_blank", "golem_core_blank", "ConfigItems.itemGolemCore meta 100", "golem_core_blank"),
            e("tc4_golem_core_bodyguard", "golem_core_bodyguard", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_core_bodyguard"),
            e("tc4_golem_core_butcher", "golem_core_butcher", "ConfigItems.itemGolemCore meta 9", "golem_core_butcher"),
            e("tc4_golem_core_empty", "golem_core_empty", "ConfigItems.itemGolemCore meta 1", "golem_core_empty"),
            e("tc4_golem_core_essentia", "golem_core_essentia", "ConfigItems.itemGolemCore meta 6", "golem_core_essentia"),
            e("tc4_golem_core_fill", "golem_core_fill", "ConfigItems.itemGolemCore meta 0", "golem_core_fill"),
            e("tc4_golem_core_fish", "golem_core_fish", "ConfigItems.itemGolemCore meta 11", "golem_core_fish"),
            e("tc4_golem_core_gather", "golem_core_gather", "ConfigItems.itemGolemCore meta 2", "golem_core_gather"),
            e("tc4_golem_core_guard", "golem_core_guard", "ConfigItems.itemGolemCore meta 4", "golem_core_guard"),
            e("tc4_golem_core_harvest", "golem_core_harvest", "ConfigItems.itemGolemCore meta 3", "golem_core_harvest"),
            e("tc4_golem_core_liquid", "golem_core_liquid", "ConfigItems.itemGolemCore meta 5", "golem_core_liquid"),
            e("tc4_golem_core_lumber", "golem_core_lumber", "ConfigItems.itemGolemCore meta 7", "golem_core_lumber"),
            e("tc4_golem_core_patrol", "golem_core_patrol", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_core_patrol"),
            e("tc4_golem_core_sorting", "golem_core_sorting", "ConfigItems.itemGolemCore meta 10", "golem_core_sorting"),
            e("tc4_golem_core_use", "golem_core_use", "ConfigItems.itemGolemCore meta 8", "golem_core_use"),
            e("tc4_golem_flesh", "golem_flesh", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_flesh"),
            e("tc4_golem_iron", "golem_iron", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_iron"),
            e("tc4_golem_stone", "golem_stone", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_stone"),
            e("tc4_golem_straw", "golem_straw", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_straw"),
            e("tc4_golem_tallow", "golem_tallow", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_tallow"),
            e("tc4_golem_thaumium", "golem_thaumium", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_thaumium"),
            e("tc4_golem_upgrade_air", "golem_upgrade_air", "ConfigItems.itemGolemUpgrade meta 0", "golem_upgrade_air"),
            e("tc4_golem_upgrade_earth", "golem_upgrade_earth", "ConfigItems.itemGolemUpgrade meta 1", "golem_upgrade_earth"),
            e("tc4_golem_upgrade_empty", "golem_upgrade_empty", "ConfigItems.itemGolemUpgrade meta -1", "golem_upgrade_empty"),
            e("tc4_golem_upgrade_entropy", "golem_upgrade_entropy", "ConfigItems.itemGolemUpgrade meta 5", "golem_upgrade_entropy"),
            e("tc4_golem_upgrade_fire", "golem_upgrade_fire", "ConfigItems.itemGolemUpgrade meta 2", "golem_upgrade_fire"),
            e("tc4_golem_upgrade_order", "golem_upgrade_order", "ConfigItems.itemGolemUpgrade meta 4", "golem_upgrade_order"),
            e("tc4_golem_upgrade_water", "golem_upgrade_water", "ConfigItems.itemGolemUpgrade meta 3", "golem_upgrade_water"),
            e("tc4_golem_wood", "golem_wood", "TC4 texture sprite from assets/thaumcraft/textures/items", "golem_wood"),
            e("tc4_golemdecoarmor", "golemdecoarmor", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecoarmor"),
            e("tc4_golemdecobowtie", "golemdecobowtie", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecobowtie"),
            e("tc4_golemdecodart", "golemdecodart", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecodart"),
            e("tc4_golemdecofez", "golemdecofez", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecofez"),
            e("tc4_golemdecoglasses", "golemdecoglasses", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecoglasses"),
            e("tc4_golemdecomace", "golemdecomace", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecomace"),
            e("tc4_golemdecotophat", "golemdecotophat", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecotophat"),
            e("tc4_golemdecovisor", "golemdecovisor", "TC4 texture sprite from assets/thaumcraft/textures/items", "golemdecovisor"),
            e("tc4_hovergirdle", "hovergirdle", "TC4 texture sprite from assets/thaumcraft/textures/items", "hovergirdle"),
            e("tc4_hoverharness", "hoverharness", "TC4 texture sprite from assets/thaumcraft/textures/items", "hoverharness"),
            e("tc4_inkwell", "inkwell", "TC4 texture sprite from assets/thaumcraft/textures/items", "inkwell"),
            e("tc4_ironbell", "ironbell", "TC4 texture sprite from assets/thaumcraft/textures/items", "ironbell"),
            e("tc4_keygold", "keygold", "TC4 texture sprite from assets/thaumcraft/textures/items", "keygold"),
            e("tc4_keyiron", "keyiron", "TC4 texture sprite from assets/thaumcraft/textures/items", "keyiron"),
            e("tc4_knowledgefragment", "knowledgefragment", "ConfigItems.itemResource meta 9", "knowledgefragment"),
            e("tc4_label", "label", "ConfigItems.itemResource meta 13", "label"),
            e("tc4_lightningring", "lightningring", "TC4 texture sprite from assets/thaumcraft/textures/items", "lightningring"),
            e("tc4_lootbag", "lootbag", "TC4 texture sprite from assets/thaumcraft/textures/items", "lootbag"),
            e("tc4_lootbagrare", "lootbagrare", "TC4 texture sprite from assets/thaumcraft/textures/items", "lootbagrare"),
            e("tc4_lootbagunc", "lootbagunc", "TC4 texture sprite from assets/thaumcraft/textures/items", "lootbagunc"),
            e("tc4_mana_bean", "mana_bean", "TC4 texture sprite from assets/thaumcraft/textures/items", "mana_bean"),
            e("tc4_mirrorframe", "mirrorframe", "TC4 texture sprite from assets/thaumcraft/textures/items", "mirrorframe"),
            e("tc4_mirrorframe2", "mirrorframe2", "TC4 texture sprite from assets/thaumcraft/textures/items", "mirrorframe2"),
            e("tc4_mirrorglass", "mirrorglass", "ConfigItems.itemResource meta 10", "mirrorglass"),
            e("tc4_mirrorhand", "mirrorhand", "TC4 texture sprite from assets/thaumcraft/textures/items", "mirrorhand"),
            e("tc4_travel_trunk", "travel_trunk", "ConfigItems.itemTrunkSpawner migration output", "travel_trunk"),
            e("tc4_nitor", "nitor", "ConfigItems.itemResource meta 1", "nitor"),
            e("tc4_nuggetbeef", "nuggetbeef", "TC4 texture sprite from assets/thaumcraft/textures/items", "nuggetbeef"),
            e("tc4_nuggetchicken", "nuggetchicken", "TC4 texture sprite from assets/thaumcraft/textures/items", "nuggetchicken"),
            e("tc4_nuggetcopper", "nuggetcopper", "ConfigItems.itemNugget meta 1", "nuggetcopper"),
            e("tc4_nuggetfish", "nuggetfish", "TC4 texture sprite from assets/thaumcraft/textures/items", "nuggetfish"),
            e("tc4_nuggetiron", "nuggetiron", "ConfigItems.itemNugget meta 0", "nuggetiron"),
            e("tc4_nuggetlead", "nuggetlead", "ConfigItems.itemNugget meta 4", "nuggetlead"),
            e("tc4_nuggetpork", "nuggetpork", "TC4 texture sprite from assets/thaumcraft/textures/items", "nuggetpork"),
            e("tc4_nuggetquicksilver", "nuggetquicksilver", "ConfigItems.itemNugget meta 5", "nuggetquicksilver"),
            e("tc4_nuggetsilver", "nuggetsilver", "ConfigItems.itemNugget meta 3", "nuggetsilver"),
            e("tc4_nuggetthaumium", "nuggetthaumium", "ConfigItems.itemNugget meta 6", "nuggetthaumium"),
            e("tc4_nuggettin", "nuggettin", "ConfigItems.itemNugget meta 2", "nuggettin"),
            e("tc4_nuggetvoid", "nuggetvoid", "ConfigItems.itemNugget meta 7", "nuggetvoid"),
            e("tc4_ob_placer", "ob_placer", "ConfigItems.itemEldritchObject meta 4", "ob_placer"),
            e("tc4_phial", "phial", "TC4 texture sprite from assets/thaumcraft/textures/items", "phial"),
            e("tc4_primal_crusher", "primal_crusher", "TC4 texture sprite from assets/thaumcraft/textures/items", "primal_crusher"),
            e("tc4_quicksilver", "quicksilver", "ConfigItems.itemResource meta 3", "quicksilver"),
            e("tc4_researchnotes", "researchnotes", "TC4 texture sprite from assets/thaumcraft/textures/items", "researchnotes"),
            e("tc4_resonator", "resonator", "TC4 texture sprite from assets/thaumcraft/textures/items", "resonator"),
            e("tc4_runic_amulet", "runic_amulet", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_amulet"),
            e("tc4_runic_amulet_emergency", "runic_amulet_emergency", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_amulet_emergency"),
            e("tc4_runic_girdle", "runic_girdle", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_girdle"),
            e("tc4_runic_girdle_kinetic", "runic_girdle_kinetic", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_girdle_kinetic"),
            e("tc4_runic_ring", "runic_ring", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_ring"),
            e("tc4_runic_ring_charged", "runic_ring_charged", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_ring_charged"),
            e("tc4_runic_ring_lesser", "runic_ring_lesser", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_ring_lesser"),
            e("tc4_runic_ring_regen", "runic_ring_regen", "TC4 texture sprite from assets/thaumcraft/textures/items", "runic_ring_regen"),
            e("tc4_sanitychecker", "sanitychecker", "TC4 texture sprite from assets/thaumcraft/textures/items", "sanitychecker"),
            e("tc4_shard", "shard", "TC4 texture sprite from assets/thaumcraft/textures/items", "shard"),
            e("tc4_shard_aer", "shard", "ConfigItems.itemShard meta 0", "shard_aer"),
            e("tc4_shard_aqua", "shard", "ConfigItems.itemShard meta 2", "shard_aqua"),
            e("tc4_shard_balanced", "shard_balanced", "ConfigItems.itemShard meta 6", "shard_balanced"),
            e("tc4_shard_ignis", "shard", "ConfigItems.itemShard meta 1", "shard_ignis"),
            e("tc4_shard_ordo", "shard", "ConfigItems.itemShard meta 4", "shard_ordo"),
            e("tc4_shard_perditio", "shard", "ConfigItems.itemShard meta 5", "shard_perditio"),
            e("tc4_shard_terra", "shard", "ConfigItems.itemShard meta 3", "shard_terra"),
            e("tc4_sinister_stone", "sinister_stone", "TC4 texture sprite from assets/thaumcraft/textures/items", "sinister_stone"),
            e("tc4_sinister_stone_active", "sinister_stone_active", "TC4 texture sprite from assets/thaumcraft/textures/items", "sinister_stone_active"),
            e("tc4_soap", "soap", "TC4 texture sprite from assets/thaumcraft/textures/items", "soap"),
            e("tc4_staff_rod_blaze", "staff_rod_blaze", "ConfigItems.itemWandRod meta 56", "staff_rod_blaze"),
            e("tc4_staff_rod_bone", "staff_rod_bone", "ConfigItems.itemWandRod meta 57", "staff_rod_bone"),
            e("tc4_staff_rod_greatwood", "staff_rod_greatwood", "ConfigItems.itemWandRod meta 50", "staff_rod_greatwood"),
            e("tc4_staff_rod_ice", "staff_rod_ice", "ConfigItems.itemWandRod meta 53", "staff_rod_ice"),
            e("tc4_staff_rod_obsidian", "staff_rod_obsidian", "ConfigItems.itemWandRod meta 51", "staff_rod_obsidian"),
            e("tc4_staff_rod_primal", "staff_rod_primal", "ConfigItems.itemWandRod meta 100", "staff_rod_primal"),
            e("tc4_staff_rod_quartz", "staff_rod_quartz", "ConfigItems.itemWandRod meta 54", "staff_rod_quartz"),
            e("tc4_staff_rod_reed", "staff_rod_reed", "ConfigItems.itemWandRod meta 55", "staff_rod_reed"),
            e("tc4_staff_rod_silverwood", "staff_rod_silverwood", "ConfigItems.itemWandRod meta 52", "staff_rod_silverwood"),
            e("tc4_taint_slime", "taint_slime", "ConfigItems.itemResource meta 11", "taint_slime"),
            e("tc4_taint_tendril", "taint_tendril", "ConfigItems.itemResource meta 12", "taint_tendril"),
            e("tc4_tallow", "tallow", "ConfigItems.itemResource meta 4", "tallow"),
            e("tc4_thaumiumaxe", "thaumiumaxe", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumaxe"),
            e("tc4_thaumiumboots", "thaumiumboots", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumboots"),
            e("tc4_thaumiumchest", "thaumiumchest", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumchest"),
            e("tc4_thaumiumfortresschest", "thaumiumfortresschest", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumfortresschest"),
            e("tc4_thaumiumfortresshelm", "thaumiumfortresshelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumfortresshelm"),
            e("tc4_thaumiumfortresslegs", "thaumiumfortresslegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumfortresslegs"),
            e("tc4_thaumiumhelm", "thaumiumhelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumhelm"),
            e("tc4_thaumiumhoe", "thaumiumhoe", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumhoe"),
            e("tc4_thaumiumingot", "thaumiumingot", "ConfigItems.itemResource meta 2", "thaumiumingot"),
            e("tc4_thaumiumlegs", "thaumiumlegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumlegs"),
            e("tc4_thaumiumpick", "thaumiumpick", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumpick"),
            e("tc4_thaumiumshovel", "thaumiumshovel", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumshovel"),
            e("tc4_thaumiumsword", "thaumiumsword", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumiumsword"),
            e("tc4_thaumonomicon", "thaumonomicon", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumonomicon"),
            e("tc4_thaumonomiconcheat", "thaumonomiconcheat", "TC4 texture sprite from assets/thaumcraft/textures/items", "thaumonomiconcheat"),
            e("tc4_tripletreat", "tripletreat", "TC4 texture sprite from assets/thaumcraft/textures/items", "tripletreat"),
            e("tc4_vis_amulet", "vis_amulet", "TC4 texture sprite from assets/thaumcraft/textures/items", "vis_amulet"),
            e("tc4_vis_amulet_lesser", "vis_amulet_lesser", "TC4 texture sprite from assets/thaumcraft/textures/items", "vis_amulet_lesser"),
            e("tc4_voidaxe", "voidaxe", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidaxe"),
            e("tc4_voidboots", "voidboots", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidboots"),
            e("tc4_voidchest", "voidchest", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidchest"),
            e("tc4_voidhelm", "voidhelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidhelm"),
            e("tc4_voidhoe", "voidhoe", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidhoe"),
            e("tc4_voidingot", "voidingot", "ConfigItems.itemResource meta 16", "voidingot"),
            e("tc4_voidlegs", "voidlegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidlegs"),
            e("tc4_voidpick", "voidpick", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidpick"),
            e("tc4_voidrobechest", "voidrobechest", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidrobechest"),
            e("tc4_voidrobechestover", "voidrobechestover", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidrobechestover"),
            e("tc4_voidrobehelm", "voidrobehelm", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidrobehelm"),
            e("tc4_voidrobelegs", "voidrobelegs", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidrobelegs"),
            e("tc4_voidrobelegsover", "voidrobelegsover", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidrobelegsover"),
            e("tc4_voidseed", "voidseed", "ConfigItems.itemResource meta 17", "voidseed"),
            e("tc4_voidshovel", "voidshovel", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidshovel"),
            e("tc4_voidsword", "voidsword", "TC4 texture sprite from assets/thaumcraft/textures/items", "voidsword"),
            e("tc4_wand_cap_copper", "wand_cap_copper", "ConfigItems.itemWandCap meta 3", "wand_cap_copper"),
            e("tc4_wand_cap_gold", "wand_cap_gold", "ConfigItems.itemWandCap meta 1", "wand_cap_gold"),
            e("tc4_wand_cap_iron", "wand_cap_iron", "ConfigItems.itemWandCap meta 0", "wand_cap_iron"),
            e("tc4_wand_cap_silver", "wand_cap_silver", "ConfigItems.itemWandCap meta 4", "wand_cap_silver"),
            e("tc4_wand_cap_silver_inert", "wand_cap_silver_inert", "ConfigItems.itemWandCap meta 5", "wand_cap_silver_inert"),
            e("tc4_wand_cap_thaumium", "wand_cap_thaumium", "ConfigItems.itemWandCap meta 2", "wand_cap_thaumium"),
            e("tc4_wand_cap_thaumium_inert", "wand_cap_thaumium_inert", "ConfigItems.itemWandCap meta 6", "wand_cap_thaumium_inert"),
            e("tc4_wand_cap_void", "wand_cap_void", "ConfigItems.itemWandCap meta 7", "wand_cap_void"),
            e("tc4_wand_cap_void_inert", "wand_cap_void_inert", "ConfigItems.itemWandCap meta 8", "wand_cap_void_inert"),
            e("tc4_wand_rod_blaze", "wand_rod_blaze", "ConfigItems.itemWandRod meta 6", "wand_rod_blaze"),
            e("tc4_wand_rod_bone", "wand_rod_bone", "ConfigItems.itemWandRod meta 7", "wand_rod_bone"),
            e("tc4_wand_rod_greatwood", "wand_rod_greatwood", "ConfigItems.itemWandRod meta 0", "wand_rod_greatwood"),
            e("tc4_wand_rod_ice", "wand_rod_ice", "ConfigItems.itemWandRod meta 3", "wand_rod_ice"),
            e("tc4_wand_rod_obsidian", "wand_rod_obsidian", "ConfigItems.itemWandRod meta 1", "wand_rod_obsidian"),
            e("tc4_wand_rod_quartz", "wand_rod_quartz", "ConfigItems.itemWandRod meta 4", "wand_rod_quartz"),
            e("tc4_wand_rod_reed", "wand_rod_reed", "ConfigItems.itemWandRod meta 5", "wand_rod_reed"),
            e("tc4_wand_rod_silverwood", "wand_rod_silverwood", "ConfigItems.itemWandRod meta 2", "wand_rod_silverwood"),
            e("tc4_wispessence", "wispessence", "TC4 texture sprite from assets/thaumcraft/textures/items", "wispessence"),
            e("tc4_block_greatwood_sapling", "tc4_block_greatwood_sapling", "ConfigBlocks.blockCustomPlant meta 0", "greatwoodsapling"),
            e("tc4_block_silverwood_sapling", "tc4_block_silverwood_sapling", "ConfigBlocks.blockCustomPlant meta 1", "silverwoodsapling"),
            e("tc4_block_shimmerleaf", "tc4_block_shimmerleaf", "ConfigBlocks.blockCustomPlant meta 2", "shimmerleaf"),
            e("tc4_block_cinderpearl", "tc4_block_cinderpearl", "ConfigBlocks.blockCustomPlant meta 3", "cinderpearl"),
            e("tc4_block_ethereal_bloom", "tc4_block_ethereal_bloom", "ConfigBlocks.blockCustomPlant meta 4", "purifier_seed"),
            e("tc4_block_vishroom", "tc4_block_vishroom", "ConfigBlocks.blockCustomPlant meta 5", "manashroom"),
            e("tc4_block_obsidian_tile", "tc4_block_obsidian_tile", "ConfigBlocks.blockCosmeticSolid meta 0", "obsidiantile"),
            e("tc4_block_obsidian_totem_base", "tc4_block_obsidian_totem_base", "ConfigBlocks.blockCosmeticSolid meta 1", "obsidiantotembase"),
            e("tc4_block_obsidian_totem_1", "tc4_block_obsidian_totem_1", "ConfigBlocks.blockCosmeticSolid meta 2", "obsidiantotem1"),
            e("tc4_block_obsidian_totem_2", "tc4_block_obsidian_totem_2", "ConfigBlocks.blockCosmeticSolid meta 3", "obsidiantotem2"),
            e("tc4_block_obsidian_totem_3", "tc4_block_obsidian_totem_3", "ConfigBlocks.blockCosmeticSolid meta 4", "obsidiantotem3"),
            e("tc4_block_obsidian_totem_4", "tc4_block_obsidian_totem_4", "ConfigBlocks.blockCosmeticSolid meta 5", "obsidiantotem4"),
            e("tc4_block_obsidian_totem_shaded", "tc4_block_obsidian_totem_shaded", "ConfigBlocks.blockCosmeticSolid meta 6", "obsidiantotembaseshaded"),
            e("tc4_block_paving_travel", "tc4_block_paving_travel", "ConfigBlocks.blockCosmeticSolid meta 7", "paving_stone_travel"),
            e("tc4_block_paving_warding", "tc4_block_paving_warding", "ConfigBlocks.blockCosmeticSolid meta 8", "paving_stone_warding"),
            e("tc4_block_thaumium", "tc4_block_thaumium", "ConfigBlocks.blockCosmeticSolid meta 9", "thaumiumblock"),
            e("tc4_block_tallow", "tc4_block_tallow", "ConfigBlocks.blockCosmeticSolid meta 10", "tallowblock"),
            e("tc4_block_arcane_stone", "tc4_block_arcane_stone", "ConfigBlocks.blockCosmeticSolid meta 6/13 alias", "arcane_stone"),
            e("tc4_block_arcane_stone_slab", "tc4_block_arcane_stone_slab", "ConfigBlocks.blockSlabStone meta 0", "arcane_stone"),
            e("tc4_block_bellows", "tc4_block_bellows", "ConfigBlocks.blockWoodenDevice meta 0", "woodplain"),
            e("tc4_block_arcane_ear", "tc4_block_arcane_ear", "ConfigBlocks.blockWoodenDevice meta 1", "arcaneearsideoff"),
            e("tc4_block_arcane_pressure_plate", "tc4_block_arcane_pressure_plate", "ConfigBlocks.blockWoodenDevice meta 2", "applate1"),
            e("tc4_block_arcane_bore_base", "tc4_block_arcane_bore_base", "ConfigBlocks.blockWoodenDevice meta 4", "woodplain"),
            e("tc4_block_arcane_bore", "tc4_block_arcane_bore", "ConfigBlocks.blockWoodenDevice meta 5", "woodplain"),
            e("tc4_block_greatwood_planks", "tc4_block_greatwood_planks", "ConfigBlocks.blockWoodenDevice meta 6", "planks_greatwood"),
            e("tc4_block_silverwood_planks", "tc4_block_silverwood_planks", "ConfigBlocks.blockWoodenDevice meta 7", "planks_silverwood"),
            e("tc4_block_banner", "tc4_block_banner", "ConfigBlocks.blockWoodenDevice meta 8", "banner_blank"),
            e("tc4_block_metal_base", "tc4_block_metal_base", "ConfigBlocks.blockMetalDevice meta 0", "metalbase"),
            e("tc4_block_alembic", "tc4_block_alembic", "ConfigBlocks.blockMetalDevice meta 1", "alembic"),
            e("tc4_block_advanced_alchemical_furnace", "tc4_block_advanced_alchemical_furnace", "ConfigBlocks.blockMetalDevice meta 3", "alchemyblockadv"),
            e("tc4_block_arcane_lamp", "tc4_block_arcane_lamp", "ConfigBlocks.blockMetalDevice meta 7", "lamp_side"),
            e("tc4_block_lamp_growth", "tc4_block_lamp_growth", "ConfigBlocks.blockMetalDevice meta 8", "lamp_grow_side"),
            e("tc4_block_alchemical_construct", "tc4_block_alchemical_construct", "ConfigBlocks.blockMetalDevice meta 9", "alchemyblock"),
            e("tc4_block_mnemonic_matrix", "tc4_block_mnemonic_matrix", "ConfigBlocks.blockMetalDevice meta 12", "brainbox"),
            e("tc4_block_lamp_fertility", "tc4_block_lamp_fertility", "ConfigBlocks.blockMetalDevice meta 13", "lamp_fert_side"),
            e("tc4_block_arcane_pedestal", "tc4_block_arcane_pedestal", "ConfigBlocks.blockStoneDevice meta 1", "pedestal_top"),
            e("tc4_block_infusion_matrix", "tc4_block_infusion_matrix", "ConfigBlocks.blockStoneDevice meta 2", "pedestal_top"),
            e("tc4_block_wand_pedestal", "tc4_block_wand_pedestal", "ConfigBlocks.blockStoneDevice meta 5", "wandpedestal_top"),
            e("tc4_block_wand_pedestal_focus", "tc4_block_wand_pedestal_focus", "ConfigBlocks.blockStoneDevice meta 8", "wandpedestal_focus_top"),
            e("tc4_block_node_stabilizer", "tc4_block_node_stabilizer", "ConfigBlocks.blockStoneDevice meta 9", "redstone_ring"),
            e("tc4_block_node_stabilizer_advanced", "tc4_block_node_stabilizer_advanced", "ConfigBlocks.blockStoneDevice meta 10", "redstone_ring"),
            e("tc4_block_node_transducer", "tc4_block_node_transducer", "ConfigBlocks.blockStoneDevice meta 11", "redstone_ring"),
            e("tc4_block_arcane_spa", "tc4_block_arcane_spa", "ConfigBlocks.blockStoneDevice meta 12", "spa_top"),
            e("tc4_block_focal_manipulator", "tc4_block_focal_manipulator", "ConfigBlocks.blockStoneDevice meta 13", "arcane_stone"),
            e("tc4_block_flux_scrubber", "tc4_block_flux_scrubber", "ConfigBlocks.blockStoneDevice meta 14", "redstone_ring"),
            e("tc4_block_levitator", "tc4_block_levitator", "ConfigBlocks.blockLifter", "liftertop"),
            e("tc4_block_essentia_reservoir", "tc4_block_essentia_reservoir", "ConfigBlocks.blockEssentiaReservoir", "jar_top"),
            e("tc4_block_greatwood_log", "tc4_block_greatwood_log", "ConfigBlocks.blockMagicalLog meta 0", "greatwoodside"),
            e("tc4_block_silverwood_log", "tc4_block_silverwood_log", "ConfigBlocks.blockMagicalLog meta 1", "silverwoodside"),
            e("tc4_block_greatwood_leaves", "tc4_block_greatwood_leaves", "ConfigBlocks.blockMagicalLeaves meta 0", "greatwoodleaves"),
            e("tc4_block_silverwood_leaves", "tc4_block_silverwood_leaves", "ConfigBlocks.blockMagicalLeaves meta 1", "silverwoodleaves"),
            e("tc4_block_essentia_tube", "tc4_block_essentia_tube", "ConfigBlocks.blockTube meta 0/1/4", "metalbase"),
            e("tc4_block_essentia_jar", "tc4_block_essentia_jar", "ConfigBlocks.blockJar meta 0", "jar"),
            e("tc4_block_crystal_cluster", "tc4_block_crystal_cluster", "ConfigBlocks.blockCrystal meta 6", "shard"),
            e("tc4_voidrobeboots", "tc4_voidrobeboots", "ConfigItems.itemBootsVoidRobe", "voidrobeboots"),
            e("tc4_mask_grinning_devil", "tc4_mask_grinning_devil", "ConfigItems.itemHelmetFortress mask 0", "thaumiumfortressmaskgrinning"),
            e("tc4_mask_angry_ghost", "tc4_mask_angry_ghost", "ConfigItems.itemHelmetFortress mask 1", "thaumiumfortressmaskangry"),
            e("tc4_mask_sipping_fiend", "tc4_mask_sipping_fiend", "ConfigItems.itemHelmetFortress mask 2", "thaumiumfortressmasksipping")
    };

    private static final Map<String, Entry> BY_ID = buildById();
    private static final Map<String, Entry> BY_TEXTURE = buildByTexture();
    private static final Map<String, String> FIELD_TEXTURE = new LinkedHashMap<>();
    private static final Map<Integer, String> RESOURCE_META;
    private static final Map<Integer, String> NUGGET_META;
    private static final Map<Integer, String> SHARD_META;
    private static final Map<Integer, String> WAND_CAP_META;
    private static final Map<Integer, String> WAND_ROD_META;
    private static final Map<Integer, String> ELDRITCH_META;
    private static final Map<Integer, String> GOLEM_CORE_META;
    private static final Map<Integer, String> GOLEM_UPGRADE_META;
    private static Map<String, RegistryObject<Item>> registered = Map.of();

    static {
        FIELD_TEXTURE.put("itemArcaneDoor", "arcanedoor");
        FIELD_TEXTURE.put("itemBootsTraveller", "bootstraveler");
        FIELD_TEXTURE.put("itemBottleTaint", "bottle_taint");
        FIELD_TEXTURE.put("itemBowBone", "bonebow");
        FIELD_TEXTURE.put("itemCrystalEssence", "crystalessence");
        FIELD_TEXTURE.put("itemEldritchObject", "eldritch_object");
        FIELD_TEXTURE.put("itemEssence", "essence");
        FIELD_TEXTURE.put("itemFocusExcavation", "focus_excavation");
        FIELD_TEXTURE.put("itemFocusFire", "focus_fire");
        FIELD_TEXTURE.put("itemFocusFrost", "focus_frost");
        FIELD_TEXTURE.put("itemFocusHellbat", "focus_hellbat");
        FIELD_TEXTURE.put("itemFocusPech", "focus_pech");
        FIELD_TEXTURE.put("itemFocusPortableHole", "focus_portablehole");
        FIELD_TEXTURE.put("itemFocusPouch", "focuspouch");
        FIELD_TEXTURE.put("itemFocusPrimal", "focus_primal");
        FIELD_TEXTURE.put("itemFocusShock", "focus_shock");
        FIELD_TEXTURE.put("itemFocusTrade", "focus_trade");
        FIELD_TEXTURE.put("itemFocusWarding", "focus_warding");
        FIELD_TEXTURE.put("itemGoggles", "gogglesrevealing");
        FIELD_TEXTURE.put("itemGolemBell", "ironbell");
        FIELD_TEXTURE.put("itemHandMirror", "mirrorhand");
        FIELD_TEXTURE.put("itemInkwell", "inkwell");
        FIELD_TEXTURE.put("itemLootbag", "lootbag");
        FIELD_TEXTURE.put("itemPickElemental", "elementalpick");
        FIELD_TEXTURE.put("itemPickThaumium", "thaumiumpick");
        FIELD_TEXTURE.put("itemPickVoid", "voidpick");
        FIELD_TEXTURE.put("itemShovelElemental", "elementalshovel");
        FIELD_TEXTURE.put("itemShovelThaumium", "thaumiumshovel");
        FIELD_TEXTURE.put("itemShovelVoid", "voidshovel");
        FIELD_TEXTURE.put("itemAxeElemental", "elementalaxe");
        FIELD_TEXTURE.put("itemAxeThaumium", "thaumiumaxe");
        FIELD_TEXTURE.put("itemSwordElemental", "elementalsword");
        FIELD_TEXTURE.put("itemSwordThaumium", "thaumiumsword");
        FIELD_TEXTURE.put("itemNugget", "nuggetiron");
        FIELD_TEXTURE.put("itemPrimalCrusher", "primal_crusher");
        FIELD_TEXTURE.put("itemResearchNotes", "researchnotes");
        FIELD_TEXTURE.put("itemResource", "alumentum");
        FIELD_TEXTURE.put("itemSanityChecker", "sanitychecker");
        FIELD_TEXTURE.put("itemSanitySoap", "soap");
        FIELD_TEXTURE.put("itemShard", "shard");
        FIELD_TEXTURE.put("itemThaumometer", "thaumometer");
        FIELD_TEXTURE.put("itemThaumonomicon", "thaumonomicon");
        FIELD_TEXTURE.put("itemWispEssence", "wispessence");
        RESOURCE_META = new java.util.LinkedHashMap<>();
        RESOURCE_META.put(0, "alumentum");
        RESOURCE_META.put(1, "nitor");
        RESOURCE_META.put(2, "thaumiumingot");
        RESOURCE_META.put(3, "quicksilver");
        RESOURCE_META.put(4, "tallow");
        RESOURCE_META.put(5, "brain");
        RESOURCE_META.put(6, "amber");
        RESOURCE_META.put(7, "cloth");
        RESOURCE_META.put(8, "filter");
        RESOURCE_META.put(9, "knowledgefragment");
        RESOURCE_META.put(10, "mirrorglass");
        RESOURCE_META.put(11, "taint_slime");
        RESOURCE_META.put(12, "taint_tendril");
        RESOURCE_META.put(13, "label");
        RESOURCE_META.put(14, "dust");
        RESOURCE_META.put(15, "charm");
        RESOURCE_META.put(16, "voidingot");
        RESOURCE_META.put(17, "voidseed");
        RESOURCE_META.put(18, "coin");
        NUGGET_META = new java.util.LinkedHashMap<>();
        NUGGET_META.put(0, "nuggetiron");
        NUGGET_META.put(1, "nuggetcopper");
        NUGGET_META.put(2, "nuggettin");
        NUGGET_META.put(3, "nuggetsilver");
        NUGGET_META.put(4, "nuggetlead");
        NUGGET_META.put(5, "nuggetquicksilver");
        NUGGET_META.put(6, "nuggetthaumium");
        NUGGET_META.put(7, "nuggetvoid");
        NUGGET_META.put(16, "clusteriron");
        NUGGET_META.put(17, "clustercopper");
        NUGGET_META.put(18, "clustertin");
        NUGGET_META.put(19, "clustersilver");
        NUGGET_META.put(20, "clusterlead");
        NUGGET_META.put(21, "clustercinnabar");
        NUGGET_META.put(31, "clustergold");
        SHARD_META = new java.util.LinkedHashMap<>();
        SHARD_META.put(0, "shard_aer");
        SHARD_META.put(1, "shard_ignis");
        SHARD_META.put(2, "shard_aqua");
        SHARD_META.put(3, "shard_terra");
        SHARD_META.put(4, "shard_ordo");
        SHARD_META.put(5, "shard_perditio");
        SHARD_META.put(6, "shard_balanced");
        WAND_CAP_META = new java.util.LinkedHashMap<>();
        WAND_CAP_META.put(0, "wand_cap_iron");
        WAND_CAP_META.put(1, "wand_cap_gold");
        WAND_CAP_META.put(2, "wand_cap_thaumium");
        WAND_CAP_META.put(3, "wand_cap_copper");
        WAND_CAP_META.put(4, "wand_cap_silver");
        WAND_CAP_META.put(5, "wand_cap_silver_inert");
        WAND_CAP_META.put(6, "wand_cap_thaumium_inert");
        WAND_CAP_META.put(7, "wand_cap_void");
        WAND_CAP_META.put(8, "wand_cap_void_inert");
        WAND_ROD_META = new java.util.LinkedHashMap<>();
        WAND_ROD_META.put(0, "wand_rod_greatwood");
        WAND_ROD_META.put(1, "wand_rod_obsidian");
        WAND_ROD_META.put(2, "wand_rod_silverwood");
        WAND_ROD_META.put(3, "wand_rod_ice");
        WAND_ROD_META.put(4, "wand_rod_quartz");
        WAND_ROD_META.put(5, "wand_rod_reed");
        WAND_ROD_META.put(6, "wand_rod_blaze");
        WAND_ROD_META.put(7, "wand_rod_bone");
        WAND_ROD_META.put(50, "staff_rod_greatwood");
        WAND_ROD_META.put(51, "staff_rod_obsidian");
        WAND_ROD_META.put(52, "staff_rod_silverwood");
        WAND_ROD_META.put(53, "staff_rod_ice");
        WAND_ROD_META.put(54, "staff_rod_quartz");
        WAND_ROD_META.put(55, "staff_rod_reed");
        WAND_ROD_META.put(56, "staff_rod_blaze");
        WAND_ROD_META.put(57, "staff_rod_bone");
        WAND_ROD_META.put(100, "staff_rod_primal");
        ELDRITCH_META = new java.util.LinkedHashMap<>();
        ELDRITCH_META.put(0, "eldritch_object");
        ELDRITCH_META.put(1, "crimson_rites");
        ELDRITCH_META.put(2, "eldritch_object_2");
        ELDRITCH_META.put(3, "eldritch_object_3");
        ELDRITCH_META.put(4, "ob_placer");
        GOLEM_CORE_META = new java.util.LinkedHashMap<>();
        GOLEM_CORE_META.put(0, "golem_core_fill");
        GOLEM_CORE_META.put(1, "golem_core_empty");
        GOLEM_CORE_META.put(2, "golem_core_gather");
        GOLEM_CORE_META.put(3, "golem_core_harvest");
        GOLEM_CORE_META.put(4, "golem_core_guard");
        GOLEM_CORE_META.put(5, "golem_core_liquid");
        GOLEM_CORE_META.put(6, "golem_core_essentia");
        GOLEM_CORE_META.put(7, "golem_core_lumber");
        GOLEM_CORE_META.put(8, "golem_core_use");
        GOLEM_CORE_META.put(9, "golem_core_butcher");
        GOLEM_CORE_META.put(10, "golem_core_sorting");
        GOLEM_CORE_META.put(11, "golem_core_fish");
        GOLEM_CORE_META.put(100, "golem_core_blank");
        GOLEM_UPGRADE_META = new java.util.LinkedHashMap<>();
        GOLEM_UPGRADE_META.put(-1, "golem_upgrade_empty");
        GOLEM_UPGRADE_META.put(0, "golem_upgrade_air");
        GOLEM_UPGRADE_META.put(1, "golem_upgrade_earth");
        GOLEM_UPGRADE_META.put(2, "golem_upgrade_fire");
        GOLEM_UPGRADE_META.put(3, "golem_upgrade_water");
        GOLEM_UPGRADE_META.put(4, "golem_upgrade_order");
        GOLEM_UPGRADE_META.put(5, "golem_upgrade_entropy");
    }

    private TC4ResearchItems() {}

    private static Entry e(String id, String texture, String originalSource, String legacyTexture) {
        return new Entry(id, texture, originalSource, legacyTexture);
    }

    public static Map<String, RegistryObject<Item>> registerAll(DeferredRegister<Item> items, CreativeModeTab tab) {
        return registerAll(items, tab, Map.of());
    }

    /**
     * Registers the legacy research-item mirror while reusing functional items
     * that were already registered by the main mod class.
     *
     * <p>Forge's {@link DeferredRegister} rejects the same registry id twice.
     * Functional replacements such as {@code tc4_crystalessence} therefore have
     * to be supplied here instead of being registered again as flat placeholders.</p>
     */
    public static Map<String, RegistryObject<Item>> registerAll(
            DeferredRegister<Item> items,
            CreativeModeTab tab,
            Map<String, RegistryObject<Item>> preRegistered
    ) {
        Map<String, RegistryObject<Item>> out = new LinkedHashMap<>();
        if (preRegistered != null) out.putAll(preRegistered);

        for (Entry entry : ENTRIES) {
            // v11.63.52: exact duplicate aliases are removed from the registry.
            // Their historical source records remain in ENTRIES and old saves are
            // remapped by MissingMappingsEvent to one canonical functional item.
            if (TC4LegacyDuplicateItemMigrator.isRemovedDuplicateId(entry.id())) continue;

            // v11.62.14: metadata 13 is now a real block + BlockItem. Do not
            // double-register the old flat research-component placeholder.
            if (entry.id().equals("tc4_block_focal_manipulator")
                    || entry.id().equals("tc4_block_thaumium")
                    || entry.id().equals("tc4_block_tallow")
                    || entry.id().equals("tc4_block_crystal_cluster")) continue;

            // v11.62.26: a functional replacement may already own this exact
            // registry id. Keep it in the lookup map and never register it twice.
            if (out.containsKey(entry.id())) continue;

            out.put(entry.id(), items.register(entry.id(), () -> createItem(entry, tab)));
        }
        registered = Collections.unmodifiableMap(out);
        return registered;
    }

    private static Item createItem(Entry entry, CreativeModeTab tab) {
        // Every tc4_* entry is a save-migration alias. Keeping a creative tab
        // here made the vanilla Search tab expose hundreds of inert clones even
        // though the Thaumcraft tab filtered them after population.
        Item.Properties properties = new Item.Properties();
        Item.Properties functionalProperties = new Item.Properties().tab(tab);
        return switch (entry.id()) {
            case "tc4_alumentum" -> new AlumentumItem(functionalProperties, entry.originalSource(), entry.legacyTexture());
            case "tc4_thaumonomicon" -> new ThaumonomiconItem(functionalProperties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_thaumonomiconcheat" -> new CreativeThaumonomiconItem(functionalProperties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_researchnotes" -> new ResearchNoteItem(functionalProperties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_discovery" -> new ResearchNoteItem(functionalProperties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_focuspouch" -> new FocusPouchItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_focuspouchbauble" -> new FocusPouchBaubleItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_gogglesrevealing" -> new GogglesOfRevealingItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_inkwell" -> new ScribingToolsItem(functionalProperties);
            case "tc4_soap" -> new SanitySoapItem(functionalProperties);
            case "tc4_sanitychecker" -> new TC4SanityCheckerItem(functionalProperties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_crimson_blade" -> new TC4CrimsonBladeItem(functionalProperties);
            case "tc4_ironbell" -> new GolemBellItem(functionalProperties);
            case "tc4_lightningring" -> new TC4LegacyTextureArtifactItem(properties,
                    "ItemHoverHarness renderer texture; not a standalone TC4 registry item");
            case "tc4_ob_placer" -> new TC4ObeliskPlacerItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_resonator" -> new TC4ResonatorItem(functionalProperties);
            case "tc4_sinister_stone" -> new TC4SinisterStoneItem(functionalProperties, false);
            case "tc4_sinister_stone_active" -> new TC4SinisterStoneItem(properties, true);
            case "tc4_bauble_amulet" -> new TC4BaubleItem(functionalProperties, TC4BaubleItem.SlotType.AMULET);
            case "tc4_bauble_belt" -> new TC4BaubleItem(functionalProperties, TC4BaubleItem.SlotType.BELT);
            case "tc4_bauble_ring", "tc4_bauble_ring_iron" -> new TC4BaubleItem(functionalProperties, TC4BaubleItem.SlotType.RING);
            case "tc4_runic_ring_lesser", "tc4_runic_ring", "tc4_runic_ring_charged", "tc4_runic_ring_regen" ->
                    new TC4RunicBaubleItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE), TC4BaubleItem.SlotType.RING);
            case "tc4_runic_amulet", "tc4_runic_amulet_emergency" ->
                    new TC4RunicBaubleItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE), TC4BaubleItem.SlotType.AMULET);
            case "tc4_runic_girdle", "tc4_runic_girdle_kinetic" ->
                    new TC4RunicBaubleItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE), TC4BaubleItem.SlotType.BELT);
            case "tc4_vis_amulet_lesser" -> new TC4VisAmuletItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON), 2500);
            case "tc4_vis_amulet" -> new TC4VisAmuletItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE), 25000);
            case "tc4_focus" -> new TC4LegacyFocusComponentItem(functionalProperties, false);
            case "tc4_focus_reversal" -> new TC4LegacyFocusComponentItem(functionalProperties, true);
            case "tc4_focus_excavation" -> new WandFocusItem(functionalProperties, WandFocusType.EXCAVATION);
            case "tc4_focus_fire" -> new WandFocusItem(functionalProperties, WandFocusType.FIRE);
            case "tc4_focus_frost" -> new WandFocusItem(functionalProperties, WandFocusType.FROST);
            case "tc4_focus_portablehole" -> new WandFocusItem(functionalProperties, WandFocusType.PORTABLE_HOLE);
            case "tc4_focus_primal" -> new WandFocusItem(functionalProperties, WandFocusType.PRIMAL);
            case "tc4_focus_shock" -> new WandFocusItem(functionalProperties, WandFocusType.SHOCK);
            case "tc4_focus_trade" -> new WandFocusItem(functionalProperties, WandFocusType.EQUAL_TRADE);
            case "tc4_focus_warding" -> new WandFocusItem(functionalProperties, WandFocusType.WARDING);
            case "tc4_mana_bean" -> new TC4ManaBeanItem(functionalProperties.food(new FoodProperties.Builder()
                    .nutrition(1).saturationMod(0.5F).alwaysEat().meat().build()));
            case "tc4_nitor" -> new NitorItem(functionalProperties);
            case "tc4_phial" -> new EssentiaPhialItem(functionalProperties);
            case "tc4_charm" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.CRAFTING_COMPONENT, "primal_charm");
            case "tc4_clothboots", "tc4_clothbootsover" -> new TC4ClothRobeItem(EquipmentSlot.FEET, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_clothchest", "tc4_clothchestover" -> new TC4ClothRobeItem(EquipmentSlot.CHEST, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_clothlegs", "tc4_clothlegsover" -> new TC4ClothRobeItem(EquipmentSlot.LEGS, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistboots" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.ROBE, EquipmentSlot.FEET, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistrobehelm" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.ROBE, EquipmentSlot.HEAD, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistrobechest" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.ROBE, EquipmentSlot.CHEST, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistrobelegs" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.ROBE, EquipmentSlot.LEGS, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistplatehelm" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.PLATE, EquipmentSlot.HEAD, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistplatechest" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.PLATE, EquipmentSlot.CHEST, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistplatelegs" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.PLATE, EquipmentSlot.LEGS, functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_cultistplateleaderhelm" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.LEADER, EquipmentSlot.HEAD, functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_cultistplateleaderchest" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.LEADER, EquipmentSlot.CHEST, functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_cultistplateleaderlegs" -> new TC4CultistArmorItem(TC4CultistArmorItem.Family.LEADER, EquipmentSlot.LEGS, functionalProperties.rarity(net.minecraft.world.item.Rarity.RARE));
            case "tc4_voidrobeboots" -> new TC4VoidRobeItem(EquipmentSlot.FEET, functionalProperties.rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_voidrobehelm" -> new TC4VoidRobeItem(EquipmentSlot.HEAD, functionalProperties.rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_voidrobechest", "tc4_voidrobechestover" -> new TC4VoidRobeItem(EquipmentSlot.CHEST, functionalProperties.rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_voidrobelegs", "tc4_voidrobelegsover" -> new TC4VoidRobeItem(EquipmentSlot.LEGS, functionalProperties.rarity(net.minecraft.world.item.Rarity.EPIC));
            case "tc4_nuggetbeef", "tc4_nuggetchicken", "tc4_nuggetfish", "tc4_nuggetpork" ->
                    new TC4EdibleNuggetItem(functionalProperties);
            case "tc4_nuggetiron" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "iron");
            case "tc4_nuggetcopper" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "copper");
            case "tc4_nuggettin" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "tin");
            case "tc4_nuggetsilver" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "silver");
            case "tc4_nuggetlead" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "lead");
            case "tc4_nuggetquicksilver" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "quicksilver");
            case "tc4_nuggetthaumium" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "thaumium");
            case "tc4_nuggetvoid" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.METAL_NUGGET, "void_metal");
            case "tc4_shard_aer" -> new ShardItem(functionalProperties, Aspect.AER, false);
            case "tc4_shard_terra" -> new ShardItem(functionalProperties, Aspect.TERRA, false);
            case "tc4_shard_ignis" -> new ShardItem(functionalProperties, Aspect.IGNIS, false);
            case "tc4_shard_aqua" -> new ShardItem(functionalProperties, Aspect.AQUA, false);
            case "tc4_shard_ordo" -> new ShardItem(functionalProperties, Aspect.ORDO, false);
            case "tc4_shard_perditio" -> new ShardItem(functionalProperties, Aspect.PERDITIO, false);
            case "tc4_shard_balanced" -> new ShardItem(functionalProperties, Aspect.PRAECANTATIO, true);
            case "tc4_shard" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.NEUTRAL_SHARD, "untyped");
            case "tc4_clusteriron" -> new TC4OreClusterItem(functionalProperties, "iron");
            case "tc4_clustergold" -> new TC4OreClusterItem(functionalProperties, "gold");
            case "tc4_clustercopper" -> new TC4OreClusterItem(functionalProperties, "copper");
            case "tc4_clustertin" -> new TC4OreClusterItem(functionalProperties, "tin");
            case "tc4_clustersilver" -> new TC4OreClusterItem(functionalProperties, "silver");
            case "tc4_clusterlead" -> new TC4OreClusterItem(functionalProperties, "lead");
            case "tc4_clustercinnabar" -> new TC4OreClusterItem(functionalProperties, "cinnabar");
            case "tc4_thaumiumingot" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.MATERIAL, "thaumium_ingot");
            case "tc4_voidingot" -> new TC4SimpleResourceItem(functionalProperties.fireResistant(), TC4SimpleResourceItem.Kind.MATERIAL, "void_metal_ingot");
            case "tc4_quicksilver" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.MATERIAL, "quicksilver");
            case "tc4_amber" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.MATERIAL, "amber");
            case "tc4_tallow" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.MATERIAL, "tallow");
            case "tc4_cloth" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.MATERIAL, "enchanted_fabric");
            case "tc4_coin" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.CRAFTING_COMPONENT, "gold_coin");
            case "tc4_dust" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.CRAFTING_COMPONENT, "salis_mundus");
            case "tc4_filter" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.CRAFTING_COMPONENT, "vis_filter");
            case "tc4_mirrorglass" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.CRAFTING_COMPONENT, "mirrored_glass");
            case "tc4_voidseed" -> new TC4SimpleResourceItem(functionalProperties, TC4SimpleResourceItem.Kind.CRAFTING_COMPONENT, "void_seed");
            case "tc4_golem_upgrade_empty" -> new TC4EmptyGolemUpgradeItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            case "tc4_brain" -> new ZombieBrainItem(functionalProperties);
            case "tc4_tripletreat" -> new TripleMeatTreatItem(functionalProperties);
            case "tc4_knowledgefragment" -> new TC4KnowledgeFragmentItem(functionalProperties);
            case "tc4_label" -> new JarLabelItem(functionalProperties);
            case "tc4_essence" -> new TC4EssenceItem(functionalProperties);
            case "tc4_lootbag" -> new TC4LootBagItem(functionalProperties, 0);
            case "tc4_lootbagunc" -> new TC4LootBagItem(functionalProperties, 1);
            case "tc4_lootbagrare" -> new TC4LootBagItem(functionalProperties, 2);
            case "tc4_crimson_rites" -> new TC4CrimsonRitesItem(functionalProperties);
            case "tc4_eldritch_object" -> new TC4EldritchObjectItem(functionalProperties, TC4EldritchObjectItem.Variant.ELDRITCH_EYE);
            case "tc4_eldritch_object_2" -> new TC4EldritchObjectItem(functionalProperties, TC4EldritchObjectItem.Variant.RUNED_TABLET);
            case "tc4_eldritch_object_3" -> new TC4PrimordialPearlItem(functionalProperties);
            case "tc4_wand_cap_iron" -> TC4WandComponentItem.activeCap(properties, WandCapType.IRON);
            case "tc4_wand_cap_gold" -> TC4WandComponentItem.activeCap(properties, WandCapType.GOLD);
            case "tc4_wand_cap_thaumium" -> TC4WandComponentItem.activeCap(properties, WandCapType.THAUMIUM);
            case "tc4_wand_cap_copper" -> TC4WandComponentItem.activeCap(functionalProperties, WandCapType.COPPER);
            case "tc4_wand_cap_silver" -> TC4WandComponentItem.activeCap(functionalProperties, WandCapType.SILVER);
            case "tc4_wand_cap_void" -> TC4WandComponentItem.activeCap(functionalProperties, WandCapType.VOID);
            case "tc4_wand_cap_silver_inert" -> TC4WandComponentItem.inertCap(functionalProperties, WandCapType.SILVER);
            case "tc4_wand_cap_thaumium_inert" -> TC4WandComponentItem.inertCap(functionalProperties, WandCapType.THAUMIUM);
            case "tc4_wand_cap_void_inert" -> TC4WandComponentItem.inertCap(functionalProperties, WandCapType.VOID);
            case "tc4_wand_rod_greatwood" -> TC4WandComponentItem.rod(properties, WandRodType.GREATWOOD);
            case "tc4_wand_rod_obsidian" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.OBSIDIAN);
            case "tc4_wand_rod_blaze" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.BLAZE);
            case "tc4_wand_rod_ice" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.ICE);
            case "tc4_wand_rod_quartz" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.QUARTZ);
            case "tc4_wand_rod_bone" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.BONE);
            case "tc4_wand_rod_reed" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.REED);
            case "tc4_wand_rod_silverwood" -> TC4WandComponentItem.rod(properties, WandRodType.SILVERWOOD);
            case "tc4_staff_rod_greatwood" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.GREATWOOD_STAFF);
            case "tc4_staff_rod_obsidian" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.OBSIDIAN_STAFF);
            case "tc4_staff_rod_blaze" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.BLAZE_STAFF);
            case "tc4_staff_rod_ice" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.ICE_STAFF);
            case "tc4_staff_rod_quartz" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.QUARTZ_STAFF);
            case "tc4_staff_rod_bone" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.BONE_STAFF);
            case "tc4_staff_rod_reed" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.REED_STAFF);
            case "tc4_staff_rod_silverwood" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.SILVERWOOD_STAFF);
            case "tc4_staff_rod_primal" -> TC4WandComponentItem.rod(functionalProperties, WandRodType.PRIMAL_STAFF);
            case "tc4_bottle_taint" -> new BottleTaintItem(functionalProperties.stacksTo(8), entry.originalSource(), entry.legacyTexture());
            case "tc4_taint_slime", "tc4_taint_tendril" ->
                    new TaintedResourceItem(properties, entry.originalSource(), entry.legacyTexture());
            case "tc4_bonebow" -> new BoneBowItem(functionalProperties);
            case "tc4_bootstraveler" -> new BootsOfTravellerItem(functionalProperties);
            case "tc4_hoverharness" -> new HoverHarnessItem(functionalProperties);
            case "tc4_hovergirdle" -> new HoverGirdleItem(functionalProperties);
            case "tc4_elementalaxe" -> new ElementalAxeItem(functionalProperties);
            case "tc4_elementalhoe" -> new ElementalHoeItem(functionalProperties);
            case "tc4_elementalpick" -> new ElementalPickaxeItem(functionalProperties);
            case "tc4_elementalshovel" -> new ElementalShovelItem(functionalProperties);
            case "tc4_elementalsword" -> new ElementalSwordItem(functionalProperties);
            case "tc4_primal_crusher" -> new PrimalCrusherItem(functionalProperties);
            case "tc4_el_arrow_air" -> new PrimalArrowItem(functionalProperties, 0);
            case "tc4_el_arrow_fire" -> new PrimalArrowItem(functionalProperties, 1);
            case "tc4_el_arrow_water" -> new PrimalArrowItem(functionalProperties, 2);
            case "tc4_el_arrow_earth" -> new PrimalArrowItem(functionalProperties, 3);
            case "tc4_el_arrow_order" -> new PrimalArrowItem(functionalProperties, 4);
            case "tc4_el_arrow_entropy" -> new PrimalArrowItem(functionalProperties, 5);
            case "tc4_keyiron" -> new ArcaneKeyItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON), false);
            case "tc4_keygold" -> new ArcaneKeyItem(functionalProperties.rarity(net.minecraft.world.item.Rarity.UNCOMMON), true);
            case "tc4_wispessence" -> new WispEssenceItem(functionalProperties, entry.originalSource(), entry.legacyTexture());
            case "tc4_travel_trunk" -> new TravelingTrunkItem(functionalProperties);
            case "tc4_thaumiumhelm" -> new TC4ThaumiumArmorItem(EquipmentSlot.HEAD, functionalProperties);
            case "tc4_thaumiumchest" -> new TC4ThaumiumArmorItem(EquipmentSlot.CHEST, functionalProperties);
            case "tc4_thaumiumlegs" -> new TC4ThaumiumArmorItem(EquipmentSlot.LEGS, functionalProperties);
            case "tc4_thaumiumboots" -> new TC4ThaumiumArmorItem(EquipmentSlot.FEET, functionalProperties);
            case "tc4_thaumiumshovel" -> new TC4ThaumiumShovelItem(functionalProperties);
            case "tc4_thaumiumpick" -> new TC4ThaumiumPickaxeItem(functionalProperties);
            case "tc4_thaumiumaxe" -> new TC4ThaumiumAxeItem(functionalProperties);
            case "tc4_thaumiumhoe" -> new TC4ThaumiumHoeItem(functionalProperties);
            case "tc4_thaumiumsword" -> new TC4ThaumiumSwordItem(functionalProperties);
            case "tc4_voidhelm" -> new TC4VoidArmorItem(EquipmentSlot.HEAD, functionalProperties);
            case "tc4_voidchest" -> new TC4VoidArmorItem(EquipmentSlot.CHEST, functionalProperties);
            case "tc4_voidlegs" -> new TC4VoidArmorItem(EquipmentSlot.LEGS, functionalProperties);
            case "tc4_voidboots" -> new TC4VoidArmorItem(EquipmentSlot.FEET, functionalProperties);
            case "tc4_voidshovel" -> new TC4VoidShovelItem(functionalProperties);
            case "tc4_voidpick" -> new TC4VoidPickaxeItem(functionalProperties);
            case "tc4_voidaxe" -> new TC4VoidAxeItem(functionalProperties);
            case "tc4_voidhoe" -> new TC4VoidHoeItem(functionalProperties);
            case "tc4_voidsword" -> new TC4VoidSwordItem(functionalProperties);
            case "tc4_thaumiumfortresshelm" -> new TC4FortressArmorItem(EquipmentSlot.HEAD, functionalProperties.stacksTo(1), entry.originalSource(), entry.legacyTexture());
            case "tc4_thaumiumfortresschest" -> new TC4FortressArmorItem(EquipmentSlot.CHEST, functionalProperties.stacksTo(1), entry.originalSource(), entry.legacyTexture());
            case "tc4_thaumiumfortresslegs" -> new TC4FortressArmorItem(EquipmentSlot.LEGS, functionalProperties.stacksTo(1), entry.originalSource(), entry.legacyTexture());
            case "tc4_mask_grinning_devil" -> new TC4FortressMaskItem(functionalProperties, 0, entry.originalSource(), entry.legacyTexture());
            case "tc4_mask_angry_ghost" -> new TC4FortressMaskItem(functionalProperties, 1, entry.originalSource(), entry.legacyTexture());
            case "tc4_mask_sipping_fiend" -> new TC4FortressMaskItem(functionalProperties, 2, entry.originalSource(), entry.legacyTexture());
            case "tc4_focus_hellbat" -> new WandFocusItem(functionalProperties, WandFocusType.HELLBAT);
            case "tc4_focus_pech" -> new WandFocusItem(functionalProperties, WandFocusType.PECH_CURSE);
            case "tc4_golem_straw" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.STRAW, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_wood" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.WOOD, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_tallow" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.TALLOW, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_clay" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.CLAY, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_flesh" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.FLESH, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_stone" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.STONE, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_iron" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.IRON, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_thaumium" -> new TC4GolemPlacerItem(functionalProperties, GolemMaterial.THAUMIUM, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_blank" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.BLANK, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_fill" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.FILL, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_empty" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.EMPTY, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_gather" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.GATHER, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_harvest" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.HARVEST, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_guard" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.GUARD, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_liquid" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.LIQUID, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_essentia" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.ESSENTIA, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_lumber" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.LUMBER, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_use" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.USE, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_butcher" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.BUTCHER, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_sorting" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.SORTING, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_fish" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.FISH, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_bodyguard" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.BODYGUARD, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_core_patrol" -> new TC4GolemCoreComponentItem(functionalProperties, GolemCoreType.PATROL, entry.originalSource(), entry.legacyTexture());
            case "tc4_golem_upgrade_air" -> new GolemUpgradeItem(functionalProperties, GolemUpgradeType.AIR);
            case "tc4_golem_upgrade_earth" -> new GolemUpgradeItem(functionalProperties, GolemUpgradeType.EARTH);
            case "tc4_golem_upgrade_fire" -> new GolemUpgradeItem(functionalProperties, GolemUpgradeType.FIRE);
            case "tc4_golem_upgrade_water" -> new GolemUpgradeItem(functionalProperties, GolemUpgradeType.WATER);
            case "tc4_golem_upgrade_order" -> new GolemUpgradeItem(functionalProperties, GolemUpgradeType.ORDER);
            case "tc4_golem_upgrade_entropy" -> new GolemUpgradeItem(functionalProperties, GolemUpgradeType.ENTROPY);
            case "tc4_golemdecotophat" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.TOP_HAT);
            case "tc4_golemdecoglasses" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.GLASSES);
            case "tc4_golemdecobowtie" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.BOWTIE);
            case "tc4_golemdecofez" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.FEZ);
            case "tc4_golemdecodart" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.DART_LAUNCHER);
            case "tc4_golemdecovisor" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.VISOR);
            case "tc4_golemdecoarmor" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.ARMOR);
            case "tc4_golemdecomace" -> new GolemDecorationItem(functionalProperties, GolemDecorationType.MACE);

            // v11.63.38: block migration aliases. Existing systems receive a
            // real BlockItem that places the functional 1.19.2 block; variants
            // without a modern canonical id use exact tc4_block_* registrations.
            case "tc4_block_advanced_alchemical_furnace" -> new BlockItem(ThaumcraftMod.ADVANCED_ALCHEMICAL_FURNACE.get(), functionalProperties);
            case "tc4_block_alchemical_construct" -> new BlockItem(ThaumcraftMod.TC4_ALCHEMICAL_CONSTRUCT.get(), functionalProperties);
            case "tc4_block_alembic" -> new AlembicBlockItem(ThaumcraftMod.ALEMBIC.get(), functionalProperties);
            case "tc4_block_arcane_pedestal" -> new BlockItem(ThaumcraftMod.ARCANE_PEDESTAL.get(), functionalProperties);
            case "tc4_block_arcane_stone" -> new BlockItem(ThaumcraftMod.ARCANE_STONE.get(), functionalProperties);
            case "tc4_block_arcane_stone_slab" -> new BlockItem(ThaumcraftMod.TC4_ARCANE_STONE_SLAB.get(), functionalProperties);
            case "tc4_block_bellows" -> new BellowsBlockItem(ThaumcraftMod.BELLOWS.get(), functionalProperties);
            case "tc4_block_cinderpearl" -> new BlockItem(ThaumcraftMod.TC4_CINDERPEARL.get(), functionalProperties);
            case "tc4_block_essentia_jar" -> new EssentiaJarBlockItem(ThaumcraftMod.ESSENTIA_JAR.get(), functionalProperties);
            case "tc4_block_essentia_reservoir" -> new BlockItem(ThaumcraftMod.ESSENTIA_RESERVOIR.get(), functionalProperties);
            case "tc4_block_essentia_tube" -> new BlockItem(ThaumcraftMod.ESSENTIA_TUBE.get(), functionalProperties);
            case "tc4_block_ethereal_bloom" -> new BlockItem(ThaumcraftMod.TC4_ETHEREAL_BLOOM.get(), functionalProperties);
            case "tc4_block_flux_scrubber" -> new BlockItem(ThaumcraftMod.FUME_DISSIPATOR.get(), functionalProperties);
            case "tc4_block_greatwood_leaves" -> new BlockItem(ThaumcraftMod.GREATWOOD_LEAVES.get(), functionalProperties);
            case "tc4_block_greatwood_log" -> new BlockItem(ThaumcraftMod.GREATWOOD_LOG.get(), functionalProperties);
            case "tc4_block_greatwood_planks" -> new BlockItem(ThaumcraftMod.GREATWOOD_PLANKS.get(), functionalProperties);
            case "tc4_block_greatwood_sapling" -> new BlockItem(ThaumcraftMod.GREATWOOD_SAPLING.get(), functionalProperties);
            case "tc4_block_infusion_matrix" -> new InfusionMatrixBlockItem(ThaumcraftMod.INFUSION_MATRIX.get(), functionalProperties);
            case "tc4_block_metal_base" -> new BlockItem(ThaumcraftMod.TC4_METAL_BASE.get(), functionalProperties);
            case "tc4_block_mnemonic_matrix" -> new BlockItem(ThaumcraftMod.MNEMONIC_MATRIX.get(), functionalProperties);
            case "tc4_block_node_stabilizer" -> new NodeStabilizerItem(ThaumcraftMod.NODE_STABILIZER.get(), functionalProperties, false);
            case "tc4_block_node_stabilizer_advanced" -> new NodeStabilizerItem(ThaumcraftMod.ADVANCED_NODE_STABILIZER.get(), functionalProperties, true);
            case "tc4_block_node_transducer" -> new NodeTransducerItem(ThaumcraftMod.NODE_TRANSDUCER.get(), functionalProperties);
            case "tc4_block_obsidian_tile" -> new BlockItem(ThaumcraftMod.OBSIDIAN_TILE.get(), functionalProperties);
            case "tc4_block_obsidian_totem_base" -> new BlockItem(ThaumcraftMod.TC4_OBSIDIAN_TOTEM_BASE.get(), functionalProperties);
            case "tc4_block_obsidian_totem_1" -> new BlockItem(ThaumcraftMod.TC4_OBSIDIAN_TOTEM_1.get(), functionalProperties);
            case "tc4_block_obsidian_totem_2" -> new BlockItem(ThaumcraftMod.TC4_OBSIDIAN_TOTEM_2.get(), functionalProperties);
            case "tc4_block_obsidian_totem_3" -> new BlockItem(ThaumcraftMod.TC4_OBSIDIAN_TOTEM_3.get(), functionalProperties);
            case "tc4_block_obsidian_totem_4" -> new BlockItem(ThaumcraftMod.TC4_OBSIDIAN_TOTEM_4.get(), functionalProperties);
            case "tc4_block_obsidian_totem_shaded" -> new BlockItem(ThaumcraftMod.TC4_OBSIDIAN_TOTEM_SHADED.get(), functionalProperties);
            case "tc4_block_paving_travel" -> new BlockItem(ThaumcraftMod.TC4_PAVING_TRAVEL.get(), functionalProperties);
            case "tc4_block_paving_warding" -> new BlockItem(ThaumcraftMod.TC4_PAVING_WARDING.get(), functionalProperties);
            case "tc4_block_shimmerleaf" -> new BlockItem(ThaumcraftMod.TC4_SHIMMERLEAF.get(), functionalProperties);
            case "tc4_block_silverwood_leaves" -> new BlockItem(ThaumcraftMod.SILVERWOOD_LEAVES.get(), functionalProperties);
            case "tc4_block_silverwood_log" -> new BlockItem(ThaumcraftMod.SILVERWOOD_LOG.get(), functionalProperties);
            case "tc4_block_silverwood_planks" -> new BlockItem(ThaumcraftMod.SILVERWOOD_PLANKS.get(), functionalProperties);
            case "tc4_block_silverwood_sapling" -> new BlockItem(ThaumcraftMod.SILVERWOOD_SAPLING.get(), functionalProperties);
            case "tc4_block_vishroom" -> new BlockItem(ThaumcraftMod.TC4_VISHROOM.get(), functionalProperties);
            default -> new TC4ResearchComponentItem(properties, entry.originalSource(), entry.legacyTexture());
        };
    }

    public static Entry[] entries() { return ENTRIES.clone(); }
    public static int count() { return ENTRIES.length; }
    public static Optional<Entry> byId(String id) { return Optional.ofNullable(BY_ID.get(id)); }
    public static Optional<Entry> byLegacyTexture(String texture) { return Optional.ofNullable(BY_TEXTURE.get(texture)); }
    public static Optional<RegistryObject<Item>> registered(String id) { return Optional.ofNullable(registered.get(id)); }

    public static Optional<ItemStack> stackByLegacyTexture(String texture) {
        Entry entry = BY_TEXTURE.get(texture);
        if (entry == null) return Optional.empty();
        RegistryObject<Item> object = registered.get(entry.id());
        if (object != null && object.isPresent()) return Optional.of(new ItemStack(object.get()));
        return canonicalReplacementStack(entry.id());
    }

    private static Optional<ItemStack> canonicalReplacementStack(String legacyId) {
        String canonicalPath = TC4LegacyDuplicateItemMigrator.canonicalPath(legacyId).orElse(null);
        if (canonicalPath == null) return Optional.empty();
        Item canonical = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", canonicalPath));
        return canonical == null || canonical == Items.AIR
                ? Optional.empty()
                : Optional.of(new ItemStack(canonical));
    }

    public static Optional<Entry> resolveLegacyExpression(String expression) {
        if (expression == null || expression.isBlank()) return Optional.empty();
        String texture = textureFromExpression(expression);
        return texture == null ? Optional.empty() : byLegacyTexture(texture);
    }

    /**
     * Resolves a preserved 1.7.10 recipe expression to a real 1.19.2 stack.
     *
     * <p>The old book renderer only resolved {@code ConfigItems.itemResource}
     * style metadata entries. As a result catalysts, vanilla ingredients,
     * blocks and most tools appeared as empty beige squares in the runtime
     * Thaumonomicon. This method is deliberately broader: it resolves the
     * de-metadata'd TC4 registry mirror first, then functional block replacements,
     * vanilla MCP fields and common ore-dictionary literals.</p>
     */
    public static Optional<ItemStack> resolveLegacyStack(String expression) {
        if (expression == null || expression.isBlank()) return Optional.empty();

        String compact = expression.replace(" ", "");
        // Resolve metadata-heavy ConfigBlocks families before the generic
        // source-ledger aliases. Otherwise blockTable/blockStoneDevice can be
        // captured by a visually similar component item and the book shows an
        // icon that cannot actually be placed or crafted.
        ItemStack blockReplacement = resolveFunctionalBlockStack(compact);
        if (!blockReplacement.isEmpty()) return Optional.of(blockReplacement);

        ItemStack itemReplacement = resolveFunctionalItemStack(compact);
        if (!itemReplacement.isEmpty()) return Optional.of(itemReplacement);

        Optional<Entry> entry = resolveLegacyExpression(expression);
        if (entry.isPresent()) {
            RegistryObject<Item> object = registered.get(entry.get().id());
            if (object != null && object.isPresent()) {
                return Optional.of(new ItemStack(object.get()));
            }
            Optional<ItemStack> canonical = canonicalReplacementStack(entry.get().id());
            if (canonical.isPresent()) return canonical;
            Item registryItem = ForgeRegistries.ITEMS.getValue(entry.get().registryName());
            if (registryItem != null && registryItem != Items.AIR) {
                return Optional.of(new ItemStack(registryItem));
            }
        }

        Item vanilla = resolveVanillaItem(compact);
        if (vanilla != null && vanilla != Items.AIR) return Optional.of(new ItemStack(vanilla));

        String ore = expression.replace(Character.toString((char) 39), "")
                .replace(Character.toString((char) 34), "")
                .trim();
        Item oreRepresentative = switch (ore) {
            case "slabWood" -> Blocks.OAK_SLAB.asItem();
            case "plankWood" -> Blocks.OAK_PLANKS.asItem();
            case "logWood" -> Blocks.OAK_LOG.asItem();
            case "stickWood" -> Items.STICK;
            case "dustGlowstone" -> Items.GLOWSTONE_DUST;
            case "dustRedstone" -> Items.REDSTONE;
            case "oreIron" -> Blocks.IRON_ORE.asItem();
            case "oreGold" -> Blocks.GOLD_ORE.asItem();
            case "nuggetIron" -> Items.IRON_NUGGET;
            case "nuggetGold" -> Items.GOLD_NUGGET;
            case "gemDiamond" -> Items.DIAMOND;
            case "ingotIron" -> Items.IRON_INGOT;
            case "ingotGold" -> Items.GOLD_INGOT;
            default -> Items.AIR;
        };
        return oreRepresentative == Items.AIR ? Optional.empty() : Optional.of(new ItemStack(oreRepresentative));
    }

    public static String resolveLegacyExpressionLabel(String expression) {
        return resolveLegacyStack(expression)
                .map(stack -> String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem())))
                .orElse("");
    }

    private static ItemStack resolveFunctionalItemStack(String compact) {
        Item item = Items.AIR;
        int meta = expressionMeta(compact);
        if (compact.contains("ConfigItems.itemResource")) {
            item = switch (meta) {
                case 1 -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "nitor"));
                case 2 -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "thaumium_ingot"));
                case 3 -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "quicksilver_drop"));
                case 6 -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "amber"));
                case 16 -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "void_metal_ingot"));
                default -> Items.AIR;
            };
        } else if (compact.contains("ConfigItems.itemNugget")) {
            item = switch (meta) {
                case 0 -> Items.IRON_NUGGET;
                case 6 -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "thaumium_nugget"));
                default -> Items.AIR;
            };
        }
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static ItemStack resolveFunctionalBlockStack(String compact) {
        String id = null;
        if (compact.contains("ConfigBlocks.blockTable")) {
            int meta = expressionMeta(compact);
            id = meta == 1 ? "research_table" : meta == 15 ? "arcane_workbench" : "table";
        } else if (compact.contains("ConfigBlocks.blockMagicalLog")) {
            id = expressionMeta(compact) == 1 ? "silverwood_log" : "greatwood_log";
        } else if (compact.contains("ConfigBlocks.blockJar")) {
            int meta = expressionMeta(compact);
            id = meta == 3 ? "void_essentia_jar" : "essentia_jar";
        } else if (compact.contains("ConfigBlocks.blockStoneDevice")) {
            int meta = expressionMeta(compact);
            id = switch (meta) {
                case 1 -> "arcane_pedestal";
                case 2 -> "infusion_matrix";
                case 9 -> "node_stabilizer";
                case 10 -> "advanced_node_stabilizer";
                case 11 -> "node_transducer";
                case 12 -> "arcane_spa";
                case 13 -> "focal_manipulator";
                default -> null;
            };
        } else if (compact.contains("ConfigBlocks.blockAiry") && expressionMeta(compact) == 5) {
            id = "aura_node";
        }
        if (id == null) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", id));
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static int expressionMeta(String compact) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("newItemStack\\([^,()]+(?:\\.[^,()]+)?(?:,(-?\\d+))?(?:,(-?\\d+))?\\)")
                .matcher(compact);
        if (!matcher.find()) return 0;
        String meta = matcher.group(2);
        if (meta == null) return 0;
        try { return Integer.parseInt(meta); } catch (NumberFormatException ignored) { return 0; }
    }

    private static Item resolveVanillaItem(String compact) {
        // MCP 1.7.10 fields used by the preserved ConfigRecipes source. Keep
        // this explicit so the book never guesses a visually unrelated item.
        if (compact.contains("Items.field_151045_i")) return Items.DIAMOND;
        if (compact.contains("Items.field_151042_j")) return Items.IRON_INGOT;
        if (compact.contains("Items.field_151043_k")) return Items.GOLD_INGOT;
        if (compact.contains("Items.field_151044_h")) return Items.COAL;
        if (compact.contains("Items.field_151016_H")) return Items.GUNPOWDER;
        if (compact.contains("Items.field_151123_aH")) return Items.SLIME_BALL;
        if (compact.contains("Items.field_151119_aD")) return Items.CLAY_BALL;
        if (compact.contains("Items.field_151114_aO")) return Items.GLOWSTONE_DUST;
        if (compact.contains("Items.field_151100_aR")) return Items.INK_SAC;
        if (compact.contains("Items.field_151007_F")) return Items.STRING;
        if (compact.contains("Items.field_151103_aS")) return Items.BONE;
        if (compact.contains("Items.field_151074_bl")) return Items.GOLD_NUGGET;
        if (compact.contains("Items.field_151014_N")) return Items.ENDER_PEARL;
        if (compact.contains("Items.field_151078_bh")) return Items.ROTTEN_FLESH;
        if (compact.contains("Items.field_151065_br")) return Items.QUARTZ;

        if (compact.contains("Blocks.field_150359_w")) return Blocks.GLASS.asItem();
        if (compact.contains("Blocks.field_150342_X")) return Blocks.BOOKSHELF.asItem();
        if (compact.contains("Blocks.field_150410_aZ")) return Blocks.GLASS_PANE.asItem();
        if (compact.contains("Blocks.field_150343_Z")) return Blocks.OBSIDIAN.asItem();
        if (compact.contains("Blocks.field_150347_e")) return Blocks.COBBLESTONE.asItem();
        if (compact.contains("Blocks.field_150341_Y")) return Blocks.MOSSY_COBBLESTONE.asItem();
        if (compact.contains("Blocks.field_150432_aD")) return Blocks.ICE.asItem();
        if (compact.contains("Blocks.field_150433_aE")) return Blocks.PACKED_ICE.asItem();
        if (compact.contains("Blocks.field_150417_aV")) return Blocks.STONE_BRICKS.asItem();
        return Items.AIR;
    }

    private static String textureFromExpression(String expression) {
        String compact = expression.replace(" ", "");
        if (compact.contains("ConfigItems.itemResource")) return resolveMeta(compact, RESOURCE_META, "itemResource");
        if (compact.contains("ConfigItems.itemNugget")) return resolveMeta(compact, NUGGET_META, "itemNugget");
        if (compact.contains("ConfigItems.itemShard")) return resolveMeta(compact, SHARD_META, "itemShard");
        if (compact.contains("ConfigItems.itemWandCap")) return resolveMeta(compact, WAND_CAP_META, "itemWandCap");
        if (compact.contains("ConfigItems.itemWandRod")) return resolveMeta(compact, WAND_ROD_META, "itemWandRod");
        if (compact.contains("ConfigItems.itemEldritchObject")) return resolveMeta(compact, ELDRITCH_META, "itemEldritchObject");
        if (compact.contains("ConfigItems.itemGolemCore")) return resolveMeta(compact, GOLEM_CORE_META, "itemGolemCore");
        if (compact.contains("ConfigItems.itemGolemUpgrade")) return resolveMeta(compact, GOLEM_UPGRADE_META, "itemGolemUpgrade");
        for (Map.Entry<String, String> e : FIELD_TEXTURE.entrySet()) {
            if (compact.contains("ConfigItems." + e.getKey())) return e.getValue();
        }
        // Non-metadata equipment fields (itemPickElemental, itemPickThaumium,
        // etc.) are already represented by ENTRIES. Derive those mappings from
        // the source ledger instead of maintaining another fragile hand list.
        for (Entry entry : ENTRIES) {
            java.util.regex.Matcher itemField = java.util.regex.Pattern
                    .compile("ConfigItems\\.([A-Za-z0-9_]+)")
                    .matcher(entry.originalSource());
            if (itemField.find() && compact.contains("ConfigItems." + itemField.group(1))) {
                return entry.legacyTexture();
            }
        }
        // The same fallback covers de-metadata'd block aliases. Metadata-heavy
        // block families are resolved to functional replacements by
        // resolveFunctionalBlockStack before this path is needed.
        int requestedMeta = expressionMeta(compact);
        for (Entry entry : ENTRIES) {
            java.util.regex.Matcher blockField = java.util.regex.Pattern
                    .compile("ConfigBlocks\\.([A-Za-z0-9_]+)")
                    .matcher(entry.originalSource());
            if (!blockField.find() || !compact.contains("ConfigBlocks." + blockField.group(1))) continue;
            java.util.regex.Matcher sourceMeta = java.util.regex.Pattern.compile("meta\\s+(-?\\d+)").matcher(entry.originalSource());
            if (!sourceMeta.find() || Integer.parseInt(sourceMeta.group(1)) == requestedMeta) {
                return entry.legacyTexture();
            }
        }
        return null;
    }

    private static String resolveMeta(String compact, Map<Integer, String> map, String field) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(field + ",(?:[^,)]*,)?(-?\\d+)\\)").matcher(compact);
        int meta = 0;
        while (matcher.find()) {
            try { meta = Integer.parseInt(matcher.group(1)); } catch (NumberFormatException ignored) { meta = 0; }
        }
        return map.getOrDefault(meta, map.get(0));
    }

    private static Map<String, Entry> buildById() {
        Map<String, Entry> out = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) out.put(entry.id(), entry);
        return Collections.unmodifiableMap(out);
    }

    private static Map<String, Entry> buildByTexture() {
        Map<String, Entry> out = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) out.put(entry.legacyTexture(), entry);
        return Collections.unmodifiableMap(out);
    }
}
