package com.darkifov.thaumcraft.porting;

import java.util.List;

/**
 * Strict TC4 1.7.10 block registry map extracted from thaumcraft.common.config.ConfigBlocks.
 * This is source-of-truth metadata for porting; it does not register placeholder blocks.
 */
public final class TC4OriginalBlockMap {
    private TC4OriginalBlockMap() {}

    public record BlockEntry(String field, String registryName, String originalClass, String originalItemClass) {}

    public static final List<BlockEntry> BLOCKS = List.of(
            new BlockEntry("blockFluxGoo", "blockFluxGoo", "BlockFluxGoo", "BlockFluxGooItem"),
            new BlockEntry("blockFluxGas", "blockFluxGas", "BlockFluxGas", "BlockFluxGasItem"),
            new BlockEntry("blockFluidPure", "blockFluidPure", "BlockFluidPure", ""),
            new BlockEntry("blockFluidDeath", "blockFluidDeath", "BlockFluidDeath", ""),
            new BlockEntry("blockCustomOre", "blockCustomOre", "BlockCustomOre", "BlockCustomOreItem"),
            new BlockEntry("blockMagicalLog", "blockMagicalLog", "BlockMagicalLog", "BlockMagicalLogItem"),
            new BlockEntry("blockMagicalLeaves", "blockMagicalLeaves", "BlockMagicalLeaves", "BlockMagicalLeavesItem"),
            new BlockEntry("blockCustomPlant", "blockCustomPlant", "BlockCustomPlant", "BlockCustomPlantItem"),
            new BlockEntry("blockTaint", "blockTaint", "BlockTaint", "BlockTaintItem"),
            new BlockEntry("blockTaintFibres", "blockTaintFibres", "BlockTaintFibres", "BlockTaintFibresItem"),
            new BlockEntry("blockCosmeticOpaque", "blockCosmeticOpaque", "BlockCosmeticOpaque", "BlockCosmeticOpaqueItem"),
            new BlockEntry("blockCosmeticSolid", "blockCosmeticSolid", "BlockCosmeticSolid", "BlockCosmeticSolidItem"),
            new BlockEntry("blockCrystal", "blockCrystal", "BlockCrystal", "BlockCrystalItem"),
            new BlockEntry("blockTube", "blockTube", "BlockTube", "thaumcraft.common.blocks.BlockTubeItem"),
            new BlockEntry("blockMetalDevice", "blockMetalDevice", "BlockMetalDevice", "BlockMetalDeviceItem"),
            new BlockEntry("blockWoodenDevice", "blockWoodenDevice", "BlockWoodenDevice", "BlockWoodenDeviceItem"),
            new BlockEntry("blockStoneDevice", "blockStoneDevice", "BlockStoneDevice", "BlockStoneDeviceItem"),
            new BlockEntry("blockMirror", "blockMirror", "BlockMirror", "BlockMirrorItem"),
            new BlockEntry("blockTable", "blockTable", "BlockTable", "BlockTableItem"),
            new BlockEntry("blockChestHungry", "blockChestHungry", "BlockChestHungry", ""),
            new BlockEntry("blockArcaneDoor", "blockArcaneDoor", "BlockArcaneDoor", ""),
            new BlockEntry("blockLifter", "blockLifter", "BlockLifter", ""),
            new BlockEntry("blockMagicBox", "blockMagicBox", "BlockMagicBox", ""),
            new BlockEntry("blockAlchemyFurnace", "blockAlchemyFurnace", "BlockAlchemyFurnace", ""),
            new BlockEntry("blockJar", "blockJar", "BlockJar", "BlockJarItem"),
            new BlockEntry("blockCandle", "blockCandle", "BlockCandle", "BlockCandleItem"),
            new BlockEntry("blockEldritch", "blockEldritch", "BlockEldritch", "BlockEldritchItem"),
            new BlockEntry("blockAiry", "blockAiry", "BlockAiry", "BlockAiryItem"),
            new BlockEntry("blockManaPod", "blockManaPod", "BlockManaPod", ""),
            new BlockEntry("blockArcaneFurnace", "blockArcaneFurnace", "BlockArcaneFurnace", ""),
            new BlockEntry("blockWarded", "blockWarded", "BlockWarded", ""),
            new BlockEntry("blockHole", "blockHole", "BlockHole", ""),
            new BlockEntry("blockEldritchPortal", "blockPortalEldritch", "BlockEldritchPortal", ""),
            new BlockEntry("blockEssentiaReservoir", "blockEssentiaReservoir", "BlockEssentiaReservoir", "BlockEssentiaReservoirItem"),
            new BlockEntry("blockEldritchNothing", "blockEldritchNothing", "BlockEldritchNothing", ""),
            new BlockEntry("blockStairsArcaneStone", "blockStairsArcaneStone", "BlockCosmeticStairs", ""),
            new BlockEntry("blockStairsGreatwood", "blockStairsGreatwood", "BlockCosmeticStairs", ""),
            new BlockEntry("blockStairsSilverwood", "blockStairsSilverwood", "BlockCosmeticStairs", ""),
            new BlockEntry("blockStairsEldritch", "blockStairsEldritch", "BlockCosmeticStairs", ""),
            new BlockEntry("blockSlabWood", "blockCosmeticSlabWood", "BlockCosmeticWoodSlab", "BlockCosmeticWoodSlabItem"),
            new BlockEntry("blockSlabStone", "blockCosmeticSlabStone", "BlockCosmeticStoneSlab", "BlockCosmeticStoneSlabItem"),
            new BlockEntry("blockDoubleSlabWood", "blockCosmeticDoubleSlabWood", "BlockCosmeticWoodSlab", ""),
            new BlockEntry("blockDoubleSlabStone", "blockCosmeticDoubleSlabStone", "BlockCosmeticStoneSlab", ""),
            new BlockEntry("blockLootUrn", "blockLootUrn", "BlockLoot", "BlockLootItem"),
            new BlockEntry("blockLootCrate", "blockLootCrate", "BlockLoot", "BlockLootItem")
    );

    public static int count() {
        return BLOCKS.size();
    }
}
