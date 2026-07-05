package com.darkifov.thaumcraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class AspectDatabase {
    private AspectDatabase() {
    }

    public static AspectList getAspectsForBlock(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        String key = id == null ? "" : id.toString();

        AspectList list = new AspectList();

        if (key.contains("air") || key.contains("aer")) list.add(Aspect.AER, 4);
        if (key.contains("earth") || key.contains("terra") || key.contains("dirt") || key.contains("stone") || key.contains("grass")) list.add(Aspect.TERRA, 4);
        if (key.contains("fire") || key.contains("ignis") || key.contains("lava") || key.contains("magma")) list.add(Aspect.IGNIS, 4);
        if (key.contains("water") || key.contains("aqua") || key.contains("ice")) list.add(Aspect.AQUA, 4);
        if (key.contains("ordo") || key.contains("quartz") || key.contains("smooth")) list.add(Aspect.ORDO, 3);
        if (key.contains("perditio") || key.contains("taint") || key.contains("netherrack") || key.contains("soul")) list.add(Aspect.PERDITIO, 4);
        if (key.contains("glass") || key.contains("crystal")) list.add(Aspect.VITREUS, 3);
        if (key.contains("iron") || key.contains("gold") || key.contains("copper") || key.contains("metal") || key.contains("ore")) list.add(Aspect.METALLUM, 3);
        if (key.contains("thaumcraft") || key.contains("magic") || key.contains("arcane")) list.add(Aspect.PRAECANTATIO, 5);
        if (key.contains("leaves") || key.contains("log") || key.contains("wood") || key.contains("planks")) {
            list.add(Aspect.HERBA, 3);
            list.add(Aspect.TERRA, 1);
        }
        if (key.contains("torch") || key.contains("glowstone") || key.contains("nitor")) list.add(Aspect.LUX, 3);
        if (key.contains("redstone") || key.contains("powered") || key.contains("lever")) list.add(Aspect.POTENTIA, 3);

        if (list.isEmpty()) {
            list.add(Aspect.TERRA, 1);
        }

        return list;
    }

    public static AspectList getAspectsForItem(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String key = id == null ? "" : id.toString();

        AspectList list = new AspectList();

        if (key.contains("aer") || key.contains("feather")) list.add(Aspect.AER, 4);
        if (key.contains("terra") || key.contains("dirt") || key.contains("stone") || key.contains("clay")) list.add(Aspect.TERRA, 4);
        if (key.contains("ignis") || key.contains("fire") || key.contains("blaze") || key.contains("torch") || key.contains("lava")) list.add(Aspect.IGNIS, 4);
        if (key.contains("aqua") || key.contains("water") || key.contains("frost") || key.contains("ice") || key.contains("snow")) list.add(Aspect.AQUA, 4);
        if (key.contains("ordo") || key.contains("quartz") || key.contains("clock") || key.contains("compass")) list.add(Aspect.ORDO, 4);
        if (key.contains("perditio") || key.contains("void") || key.contains("gunpowder") || key.contains("tnt") || key.contains("obsidian")) list.add(Aspect.PERDITIO, 4);
        if (key.contains("thaum") || key.contains("wand") || key.contains("focus") || key.contains("shard") || key.contains("amethyst") || key.contains("lapis")) list.add(Aspect.PRAECANTATIO, 4);
        if (key.contains("iron") || key.contains("gold") || key.contains("copper") || key.contains("ingot") || key.contains("nugget") || key.contains("plate")) list.add(Aspect.METALLUM, 3);
        if (key.contains("glass") || key.contains("crystal")) list.add(Aspect.VITREUS, 2);
        if (key.contains("log") || key.contains("wood") || key.contains("planks") || key.contains("sapling") || key.contains("leaves") || key.contains("honeycomb")) list.add(Aspect.HERBA, 3);
        if (key.contains("glowstone") || key.contains("lantern") || key.contains("nitor")) list.add(Aspect.LUX, 4);
        if (key.contains("redstone") || key.contains("blaze_powder") || key.contains("coal") || key.contains("charcoal")) list.add(Aspect.POTENTIA, 4);
        if (key.contains("ender") || key.contains("pearl") || key.contains("star")) list.add(Aspect.VACUOS, 4);

        if (key.contains("slime") || key.contains("honey")) list.add(Aspect.LIMUS, 3);
        if (key.contains("poison") || key.contains("spider_eye")) list.add(Aspect.VENENUM, 3);
        if (key.contains("soul") || key.contains("ghast")) list.add(Aspect.SPIRITUS, 3);
        if (key.contains("bone") || key.contains("rotten") || key.contains("skull")) list.add(Aspect.MORTUUS, 3);
        if (key.contains("wool") || key.contains("string") || key.contains("leather")) list.add(Aspect.PANNUS, 3);
        if (key.contains("sword") || key.contains("arrow") || key.contains("bow")) list.add(Aspect.TELUM, 3);
        if (key.contains("helmet") || key.contains("chestplate") || key.contains("leggings") || key.contains("boots") || key.contains("shield")) list.add(Aspect.TUTAMEN, 3);
        if (key.contains("piston") || key.contains("rail") || key.contains("machine") || key.contains("gear")) list.add(Aspect.MACHINA, 3);
        if (key.contains("villager") || key.contains("player") || key.contains("book")) list.add(Aspect.COGNITIO, 2);
        if (key.contains("food") || key.contains("bread") || key.contains("apple") || key.contains("carrot") || key.contains("potato")) list.add(Aspect.FAMES, 3);
        if (key.contains("emerald") || key.contains("diamond") || key.contains("coin")) list.add(Aspect.LUCRUM, 3);

        if (list.isEmpty()) {
            list.add(Aspect.VACUOS, 1);
        }

        return list;
    }
}
