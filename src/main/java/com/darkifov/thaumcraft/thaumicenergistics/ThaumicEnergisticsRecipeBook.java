package com.darkifov.thaumcraft.thaumicenergistics;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EncodedEssentiaPatternItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ThaumicEnergisticsRecipeBook {
    private static final List<AutoRecipe> RECIPES = new ArrayList<>();

    static {
        recipe("thaumometer", "Thaumometer", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:thaumometer", 1)
                .input("minecraft:gold_ingot", 2).input("minecraft:glass_pane", 1).input("thaumcraft:balanced_shard", 1)
                .aspect(Aspect.ORDO, 8).aspect(Aspect.PRAECANTATIO, 8).register();

        recipe("arcane_workbench", "Arcane Workbench", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:arcane_workbench", 1)
                .input("thaumcraft:table", 1).input("thaumcraft:iron_capped_wooden_wand", 1)
                .aspect(Aspect.ORDO, 12).register();

        recipe("arcane_pedestal", "Arcane Pedestal", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:arcane_pedestal", 1)
                .input("thaumcraft:arcane_stone", 4).input("thaumcraft:balanced_shard", 1)
                .aspect(Aspect.TERRA, 12).aspect(Aspect.ORDO, 8).register();

        recipe("infusion_matrix", "Infusion Matrix", EncodedEssentiaPatternItem.PatternType.INFUSION, "thaumcraft:infusion_matrix", 1)
                .input("thaumcraft:arcane_stone", 4).input("thaumcraft:infusion_core", 1).input("thaumcraft:balanced_shard", 2).input("minecraft:ender_pearl", 1)
                .aspect(Aspect.PRAECANTATIO, 48).aspect(Aspect.ORDO, 32).aspect(Aspect.VITREUS, 16).register();

        recipe("matrix_accelerator", "Matrix Accelerator", EncodedEssentiaPatternItem.PatternType.INFUSION, "thaumcraft:matrix_accelerator", 1)
                .input("thaumcraft:infusion_matrix", 1).input("thaumcraft:thaumium_plate", 4).input("minecraft:redstone", 4)
                .aspect(Aspect.POTENTIA, 48).aspect(Aspect.PRAECANTATIO, 24).register();

        recipe("matrix_stabilizer", "Matrix Stabilizer", EncodedEssentiaPatternItem.PatternType.INFUSION, "thaumcraft:matrix_stabilizer", 1)
                .input("thaumcraft:node_stabilizer", 1).input("thaumcraft:ordo_shard", 4).input("thaumcraft:balanced_shard", 4)
                .aspect(Aspect.ORDO, 64).aspect(Aspect.PRAECANTATIO, 24).register();

        recipe("node_stabilizer", "Node Stabilizer", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:node_stabilizer", 1)
                .input("thaumcraft:arcane_stone", 4).input("thaumcraft:ordo_shard", 2).input("minecraft:redstone", 2)
                .aspect(Aspect.ORDO, 32).aspect(Aspect.POTENTIA, 12).register();

        recipe("essentia_drive", "Essentia Drive", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_drive", 1)
                .input("thaumcraft:essentia_digitizer_core", 1).input("thaumcraft:essentia_storage_component_1k", 2).input("minecraft:iron_ingot", 4)
                .aspect(Aspect.ORDO, 24).aspect(Aspect.VITREUS, 16).register();

        recipe("essentia_terminal", "Essentia Terminal", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_terminal", 1)
                .input("thaumcraft:essentia_digitizer_core", 1).input("minecraft:glass_pane", 2).input("minecraft:redstone", 2)
                .aspect(Aspect.VITREUS, 24).aspect(Aspect.PRAECANTATIO, 12).register();

        recipe("essentia_interface", "Essentia Interface", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_interface", 1)
                .input("thaumcraft:essentia_digitizer_core", 1).input("thaumcraft:essentia_tube", 4).input("minecraft:iron_ingot", 2)
                .aspect(Aspect.AQUA, 16).aspect(Aspect.ORDO, 16).register();

        recipe("essentia_import_bus", "Essentia Import Bus", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_import_bus", 1)
                .input("thaumcraft:essentia_tube", 2).input("minecraft:hopper", 1).input("minecraft:redstone", 1)
                .aspect(Aspect.AQUA, 8).aspect(Aspect.POTENTIA, 8).register();

        recipe("essentia_export_bus", "Essentia Export Bus", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_export_bus", 1)
                .input("thaumcraft:essentia_tube", 2).input("minecraft:dropper", 1).input("minecraft:redstone", 1)
                .aspect(Aspect.AQUA, 8).aspect(Aspect.POTENTIA, 8).register();

        recipe("essentia_storage_bus", "Essentia Storage Bus", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_storage_bus", 1)
                .input("thaumcraft:essentia_tube", 2).input("minecraft:chest", 1).input("minecraft:redstone", 1)
                .aspect(Aspect.VACUOS, 12).register();

        recipe("arcane_pattern_encoder", "Arcane Pattern Encoder", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:arcane_pattern_encoder", 1)
                .input("thaumcraft:essentia_digitizer_core", 1).input("thaumcraft:research_note", 1).input("minecraft:iron_ingot", 2)
                .aspect(Aspect.ORDO, 16).aspect(Aspect.PRAECANTATIO, 16).register();

        recipe("arcane_pattern_provider", "Arcane Pattern Provider", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:arcane_pattern_provider", 1)
                .input("thaumcraft:encoded_essentia_pattern", 1).input("thaumcraft:essentia_interface", 1).input("minecraft:redstone", 2)
                .aspect(Aspect.ORDO, 24).register();

        recipe("digital_cell_1k", "Digital Essentia Cell 1k", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:digital_essentia_cell_1k", 1)
                .input("thaumcraft:essentia_storage_component_1k", 1).input("thaumcraft:essentia_digitizer_core", 1).input("minecraft:glass_pane", 2)
                .aspect(Aspect.VACUOS, 16).aspect(Aspect.VITREUS, 8).register();

        recipe("digital_cell_4k", "Digital Essentia Cell 4k", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:digital_essentia_cell_4k", 1)
                .input("thaumcraft:digital_essentia_cell_1k", 3).input("thaumcraft:essentia_storage_component_4k", 1)
                .aspect(Aspect.VACUOS, 32).aspect(Aspect.VITREUS, 16).register();

        recipe("digital_cell_16k", "Digital Essentia Cell 16k", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:digital_essentia_cell_16k", 1)
                .input("thaumcraft:digital_essentia_cell_4k", 3).input("thaumcraft:essentia_storage_component_16k", 1)
                .aspect(Aspect.VACUOS, 64).aspect(Aspect.VITREUS, 32).register();

        recipe("encoded_pattern", "Encoded Essentia Pattern", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:encoded_essentia_pattern", 1)
                .input("thaumcraft:research_note", 1).input("thaumcraft:essentia_digitizer_core", 1).input("minecraft:redstone", 1)
                .aspect(Aspect.ORDO, 8).register();

        recipe("alchemical_furnace", "Alchemical Furnace", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:alchemical_furnace", 1)
                .input("minecraft:furnace", 1).input("thaumcraft:arcane_stone", 4).input("thaumcraft:ignis_shard", 1)
                .aspect(Aspect.IGNIS, 24).aspect(Aspect.TERRA, 12).register();

        recipe("essentia_jar", "Essentia Jar", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_jar", 1)
                .input("minecraft:glass", 5).input("thaumcraft:greatwood_planks", 1)
                .aspect(Aspect.VACUOS, 8).aspect(Aspect.AQUA, 8).register();

        recipe("essentia_tube", "Essentia Tube", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_tube", 4)
                .input("minecraft:glass", 2).input("thaumcraft:quicksilver_drop", 1)
                .aspect(Aspect.AQUA, 8).register();

        recipe("arcane_assembler", "Arcane Assembler", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:arcane_assembler", 1)
                .input("thaumcraft:arcane_pattern_provider", 1).input("thaumcraft:arcane_workbench", 1).input("thaumcraft:essentia_drive", 1)
                .aspect(Aspect.ORDO, 48).aspect(Aspect.PRAECANTATIO, 48).register();

        recipe("essentia_provider", "Essentia Provider", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_provider", 1)
                .input("thaumcraft:essentia_interface", 1).input("thaumcraft:essentia_export_bus", 1).input("thaumcraft:essentia_import_bus", 1)
                .aspect(Aspect.AQUA, 32).aspect(Aspect.ORDO, 24).register();

        recipe("infusion_provider", "Infusion Provider", EncodedEssentiaPatternItem.PatternType.INFUSION, "thaumcraft:infusion_provider", 1)
                .input("thaumcraft:essentia_provider", 1).input("thaumcraft:infusion_matrix", 1).input("thaumcraft:arcane_pedestal", 4)
                .aspect(Aspect.PRAECANTATIO, 64).aspect(Aspect.ORDO, 32).aspect(Aspect.POTENTIA, 24).register();

        recipe("digital_cell_64k", "Digital Essentia Cell 64k", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:digital_essentia_cell_64k", 1)
                .input("thaumcraft:digital_essentia_cell_16k", 3).input("thaumcraft:essentia_storage_component_64k", 1)
                .aspect(Aspect.VACUOS, 128).aspect(Aspect.VITREUS, 64).register();

        recipe("knowledge_inscriber", "Knowledge Inscriber", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:knowledge_inscriber", 1)
                .input("thaumcraft:knowledge_core", 1).input("thaumcraft:research_note", 2).input("minecraft:iron_ingot", 2)
                .aspect(Aspect.PRAECANTATIO, 32).aspect(Aspect.ORDO, 24).register();

        recipe("arcane_crafting_terminal", "Arcane Crafting Terminal", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:arcane_crafting_terminal", 1)
                .input("thaumcraft:essentia_terminal", 1).input("thaumcraft:arcane_assembler", 1).input("thaumcraft:encoded_essentia_pattern", 2)
                .aspect(Aspect.PRAECANTATIO, 32).aspect(Aspect.ORDO, 32).register();

        recipe("essentia_level_emitter", "Essentia Level Emitter", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:essentia_level_emitter", 1)
                .input("thaumcraft:essentia_terminal", 1).input("thaumcraft:coalescence_core", 1).input("minecraft:redstone", 4)
                .aspect(Aspect.POTENTIA, 24).aspect(Aspect.ORDO, 16).register();

        recipe("vis_interface", "Vis Interface", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:vis_interface", 1)
                .input("thaumcraft:essentia_terminal", 1).input("thaumcraft:iron_capped_wooden_wand", 1).input("thaumcraft:diffusion_core", 1)
                .aspect(Aspect.PRAECANTATIO, 40).aspect(Aspect.POTENTIA, 20).register();


        recipe("thaumic_me_controller", "Thaumic ME Controller", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:thaumic_me_controller", 1)
                .input("thaumcraft:thaumic_channel_core", 4).input("thaumcraft:knowledge_core", 1).input("thaumcraft:quicksilver_drop", 4)
                .aspect(Aspect.ORDO, 64).aspect(Aspect.POTENTIA, 32).register();

        recipe("thaumic_crafting_cpu", "Thaumic Crafting CPU", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:thaumic_crafting_cpu", 1)
                .input("thaumcraft:thaumic_crafting_cpu_core", 1).input("thaumcraft:thaumic_channel_core", 4).input("minecraft:iron_ingot", 4)
                .aspect(Aspect.ORDO, 48).aspect(Aspect.PRAECANTATIO, 48).register();

        recipe("thaumic_energy_acceptor", "Thaumic Energy Acceptor", EncodedEssentiaPatternItem.PatternType.ARCANE_WORKBENCH, "thaumcraft:thaumic_energy_acceptor", 1)
                .input("thaumcraft:potentia_shard", 4).input("thaumcraft:thaumic_channel_core", 1).input("minecraft:iron_ingot", 4)
                .aspect(Aspect.POTENTIA, 64).register();

    }

    private ThaumicEnergisticsRecipeBook() {
    }

    private static AutoRecipe recipe(String id, String displayName, EncodedEssentiaPatternItem.PatternType type, String result, int count) {
        return new AutoRecipe(id, displayName, type, new ResourceLocation(result), count);
    }

    public static List<AutoRecipe> recipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static AutoRecipe byId(String id) {
        for (AutoRecipe recipe : RECIPES) {
            if (recipe.id().equals(id)) {
                return recipe;
            }
        }

        return RECIPES.isEmpty() ? null : RECIPES.get(0);
    }

    public static String firstId() {
        return RECIPES.isEmpty() ? "" : RECIPES.get(0).id();
    }

    public static String nextId(String current) {
        if (RECIPES.isEmpty()) {
            return "";
        }

        for (int i = 0; i < RECIPES.size(); i++) {
            if (RECIPES.get(i).id().equals(current)) {
                return RECIPES.get((i + 1) % RECIPES.size()).id();
            }
        }

        return RECIPES.get(0).id();
    }

    public static String displayName(String id) {
        AutoRecipe recipe = byId(id);
        return recipe == null ? "None" : recipe.displayName();
    }

    public static void diagnose(Level level, net.minecraft.core.BlockPos pos, Player player, ItemStack pattern) {
        AutoRecipe recipe = byId(EncodedEssentiaPatternItem.getRecipeTarget(pattern));

        if (recipe == null) {
            player.displayClientMessage(Component.literal("Pattern пустой: выбери цель Shift+ПКМ.").withStyle(ChatFormatting.RED), false);
            return;
        }

        player.displayClientMessage(Component.literal("Pattern target: " + recipe.displayName()).withStyle(ChatFormatting.GOLD), false);
        player.displayClientMessage(Component.literal("Mode: " + recipe.type()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        player.displayClientMessage(Component.literal("Items: " + recipe.itemCostText()).withStyle(ChatFormatting.GRAY), false);
        player.displayClientMessage(Component.literal("Essentia: " + recipe.aspectCostText()).withStyle(ChatFormatting.AQUA), false);
        boolean ready = canCraft(level, pos, player, recipe);
        player.displayClientMessage(Component.literal("Ready: " + ready).withStyle(ready ? ChatFormatting.GREEN : ChatFormatting.RED), false);
    }

    public static boolean craft(Level level, net.minecraft.core.BlockPos pos, Player player, ItemStack pattern) {
        AutoRecipe recipe = byId(EncodedEssentiaPatternItem.getRecipeTarget(pattern));

        if (recipe == null) {
            player.displayClientMessage(Component.literal("Encoded Pattern пустой. Shift+ПКМ по pattern — выбрать цель.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        return craft(level, pos, player, recipe);
    }

    public static boolean craft(Level level, net.minecraft.core.BlockPos pos, Player player, AutoRecipe recipe) {
        if (!canCraft(level, pos, player, recipe)) {
            player.displayClientMessage(Component.literal("Недостаточно ресурсов для: " + recipe.displayName()).withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(Component.literal("Нужно: " + recipe.itemCostText()).withStyle(ChatFormatting.GRAY), false);
            player.displayClientMessage(Component.literal("Essentia: " + recipe.aspectCostText()).withStyle(ChatFormatting.AQUA), false);
            return false;
        }

        consumeItems(player, recipe.inputs());
        ThaumicEnergisticsNetwork.extractAll(level, pos, recipe.aspects());

        Item resultItem = ForgeRegistries.ITEMS.getValue(recipe.resultId());
        if (resultItem == null) {
            player.displayClientMessage(Component.literal("Result item не найден: " + recipe.resultId()).withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack result = new ItemStack(resultItem, recipe.resultCount());

        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }

        player.displayClientMessage(Component.literal("Arcane Assembler создал: ").append(result.getHoverName()).withStyle(ChatFormatting.GREEN), false);
        return true;
    }

    public static boolean canCraft(Level level, net.minecraft.core.BlockPos pos, Player player, AutoRecipe recipe) {
        return hasItems(player, recipe.inputs()) && ThaumicEnergisticsNetwork.hasAspects(level, pos, recipe.aspects());
    }

    private static boolean hasItems(Player player, Map<ResourceLocation, Integer> inputs) {
        for (Map.Entry<ResourceLocation, Integer> entry : inputs.entrySet()) {
            if (countItem(player.getInventory(), entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private static int countItem(Inventory inventory, ResourceLocation id) {
        Item item = ForgeRegistries.ITEMS.getValue(id);

        if (item == null) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static void consumeItems(Player player, Map<ResourceLocation, Integer> inputs) {
        for (Map.Entry<ResourceLocation, Integer> entry : inputs.entrySet()) {
            consumeItem(player.getInventory(), entry.getKey(), entry.getValue());
        }
    }

    private static void consumeItem(Inventory inventory, ResourceLocation id, int amount) {
        Item item = ForgeRegistries.ITEMS.getValue(id);

        if (item == null || amount <= 0) {
            return;
        }

        int remaining = amount;

        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.getItem() != item) {
                continue;
            }

            int take = Math.min(stack.getCount(), remaining);
            stack.shrink(take);
            remaining -= take;
        }
    }

    public static final class AutoRecipe {
        private final String id;
        private final String displayName;
        private final EncodedEssentiaPatternItem.PatternType type;
        private final ResourceLocation resultId;
        private final int resultCount;
        private final Map<ResourceLocation, Integer> inputs = new LinkedHashMap<>();
        private final EnumMap<Aspect, Integer> aspects = new EnumMap<>(Aspect.class);

        private AutoRecipe(String id, String displayName, EncodedEssentiaPatternItem.PatternType type, ResourceLocation resultId, int resultCount) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
            this.resultId = resultId;
            this.resultCount = Math.max(1, resultCount);
        }

        public AutoRecipe input(String id, int count) {
            inputs.merge(new ResourceLocation(id), Math.max(1, count), Integer::sum);
            return this;
        }

        public AutoRecipe aspect(Aspect aspect, int amount) {
            if (aspect != null && amount > 0) {
                aspects.merge(aspect, amount, Integer::sum);
            }

            return this;
        }

        public void register() {
            RECIPES.add(this);
        }

        public String id() { return id; }
        public String displayName() { return displayName; }
        public EncodedEssentiaPatternItem.PatternType type() { return type; }
        public ResourceLocation resultId() { return resultId; }
        public int resultCount() { return resultCount; }
        public Map<ResourceLocation, Integer> inputs() { return inputs; }
        public EnumMap<Aspect, Integer> aspects() { return aspects; }

        public String itemCostText() {
            if (inputs.isEmpty()) {
                return "none";
            }

            List<String> parts = new ArrayList<>();

            for (Map.Entry<ResourceLocation, Integer> entry : inputs.entrySet()) {
                parts.add(entry.getKey() + " x" + entry.getValue());
            }

            return String.join(", ", parts);
        }

        public String aspectCostText() {
            if (aspects.isEmpty()) {
                return "none";
            }

            List<String> parts = new ArrayList<>();

            for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
                parts.add(entry.getKey().displayName() + " " + entry.getValue());
            }

            return String.join(", ", parts);
        }
    }
}
