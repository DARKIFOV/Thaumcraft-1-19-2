package com.darkifov.thaumcraft.compat.jei;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.block.JarLabelItem;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.infusion.InfusionRecipe;
import com.darkifov.thaumcraft.infusion.InfusionRecipes;
import com.darkifov.thaumcraft.wand.WandCraftingRuntime;
import com.darkifov.thaumcraft.wand.WandVariantRuntime;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Optional JEI bridge. No JEI classes are touched unless JEI discovers this plugin. */
@JeiPlugin
public final class TC4JeiPlugin implements IModPlugin {
    public static final RecipeType<ArcaneWorkbenchRecipe> ARCANE =
            RecipeType.create(ThaumcraftMod.MOD_ID, "arcane_workbench", ArcaneWorkbenchRecipe.class);
    public static final RecipeType<AlchemyRecipe> ALCHEMY =
            RecipeType.create(ThaumcraftMod.MOD_ID, "crucible", AlchemyRecipe.class);
    public static final RecipeType<InfusionRecipe> INFUSION =
            RecipeType.create(ThaumcraftMod.MOD_ID, "infusion", InfusionRecipe.class);
    public static final RecipeType<JarLabelJeiRecipe> JAR_LABEL =
            RecipeType.create(ThaumcraftMod.MOD_ID, "jar_label", JarLabelJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_recipe_viewer");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // All ordinary wands, staffs and sceptres share one registry item. Only
        // rod/cap/sceptre NBT defines the visual subtype; stored vis and focus
        // data must not split an otherwise identical component combination.
        registration.registerSubtypeInterpreter(
                ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get(),
                (stack, context) -> WandVariantRuntime.subtypeKey(stack)
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ArcaneCategory(gui), new AlchemyCategory(gui), new InfusionCategory(gui),
                new JarLabelCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        InfusionRecipes.ensureBundledRecipesLoaded();
        registration.addRecipes(ARCANE, List.copyOf(ArcaneWorkbenchRecipes.recipes()));
        registration.addRecipes(ALCHEMY, List.copyOf(AlchemyRecipes.recipes()));
        registration.addRecipes(INFUSION, List.copyOf(InfusionRecipes.recipes()));
        registration.addRecipes(JAR_LABEL, jarLabelRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ThaumcraftMod.ARCANE_WORKBENCH.get()), ARCANE);
        registration.addRecipeCatalyst(new ItemStack(ThaumcraftMod.CRUCIBLE.get()), ALCHEMY);
        registration.addRecipeCatalyst(new ItemStack(ThaumcraftMod.INFUSION_MATRIX.get()), INFUSION);
        registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), JAR_LABEL);
    }

    private abstract static class BaseCategory<T> implements IRecipeCategory<T> {
        private final RecipeType<T> type;
        private final Component title;
        private final IDrawable background;
        private final IDrawable icon;

        BaseCategory(IGuiHelper gui, RecipeType<T> type, String titleKey, ItemStack icon, int width, int height) {
            this.type = type;
            this.title = Component.translatable(titleKey);
            this.background = gui.createBlankDrawable(width, height);
            this.icon = gui.createDrawableItemStack(icon);
        }

        @Override public RecipeType<T> getRecipeType() { return type; }
        @Override public Component getTitle() { return title; }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }

        void drawAspects(PoseStack pose, Map<Aspect, Integer> aspects, int startX, int startY, int columns) {
            int index = 0;
            for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
                int x = startX + (index % columns) * 27;
                int y = startY + (index / columns) * 20;
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, new ResourceLocation(ThaumcraftMod.MOD_ID,
                        "textures/aspects/" + entry.getKey().id() + ".png"));
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                GuiComponent.blit(pose, x, y, 0, 0, 16, 16, 16, 16);
                Minecraft.getInstance().font.draw(pose, Integer.toString(entry.getValue()), x + 16, y + 8, 0xFFFFFF);
                index++;
            }
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        List<Component> aspectTooltip(Map<Aspect, Integer> aspects, double mouseX, double mouseY,
                                      int startX, int startY, int columns) {
            int index = 0;
            for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
                int x = startX + (index % columns) * 27;
                int y = startY + (index / columns) * 20;
                if (mouseX >= x && mouseX < x + 25 && mouseY >= y && mouseY < y + 18) {
                    return List.of(Component.literal(entry.getKey().displayName() + ": " + entry.getValue())
                            .withStyle(entry.getKey().color()));
                }
                index++;
            }
            return List.of();
        }

        void drawLabel(PoseStack pose, String key, String value, int x, int y) {
            Minecraft.getInstance().font.draw(pose,
                    Component.translatable(key, value), x, y, 0x404040);
        }
    }

    private static final class ArcaneCategory extends BaseCategory<ArcaneWorkbenchRecipe> {
        ArcaneCategory(IGuiHelper gui) {
            super(gui, ARCANE, "thaumcraft.jei.arcane_workbench",
                    new ItemStack(ThaumcraftMod.ARCANE_WORKBENCH.get()), 176, 108);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, ArcaneWorkbenchRecipe recipe, IFocusGroup focuses) {
            if (!recipe.pattern().isEmpty()) {
                Map<Character, ResourceLocation> key = recipe.inferredPatternMap();
                for (int row = 0; row < Math.min(3, recipe.pattern().size()); row++) {
                    String patternRow = recipe.pattern().get(row);
                    for (int column = 0; column < Math.min(3, patternRow.length()); column++) {
                        ResourceLocation id = key.get(patternRow.charAt(column));
                        if (id != null) {
                            builder.addSlot(RecipeIngredientRole.INPUT, 2 + column * 18, 2 + row * 18)
                                    .addIngredients(asIngredient(id));
                        }
                    }
                }
            } else {
                builder.setShapeless(54, 2);
                List<ResourceLocation> inputs = new ArrayList<>();
                if (recipe.catalystItemId() != null) inputs.add(recipe.catalystItemId());
                inputs.addAll(recipe.normalizedLooseIngredients());
                for (int i = 0; i < inputs.size(); i++) {
                    builder.addSlot(RecipeIngredientRole.INPUT, 2 + (i % 3) * 18, 2 + (i / 3) * 18)
                            .addIngredients(asIngredient(inputs.get(i)));
                }
            }
            ItemStack output = WandCraftingRuntime.isGeneratedAssembly(recipe)
                    ? WandCraftingRuntime.resultFor(recipe)
                    : recipe.result();
            builder.addSlot(RecipeIngredientRole.OUTPUT, 148, 20).addItemStack(output);
        }

        @Override
        public void draw(ArcaneWorkbenchRecipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
            drawLabel(pose, "thaumcraft.jei.research", blankAsDash(recipe.research()), 67, 4);
            drawLabel(pose, "thaumcraft.jei.original_recipe", blankAsDash(recipe.tc4Key()), 67, 15);
            drawAspects(pose, recipe.aspectCost(), 3, 70, 6);
        }

        @Override
        public List<Component> getTooltipStrings(ArcaneWorkbenchRecipe recipe, IRecipeSlotsView slots, double x, double y) {
            return aspectTooltip(recipe.aspectCost(), x, y, 3, 70, 6);
        }

        @Override public ResourceLocation getRegistryName(ArcaneWorkbenchRecipe recipe) { return recipe.id(); }
    }

    private static final class AlchemyCategory extends BaseCategory<AlchemyRecipe> {
        AlchemyCategory(IGuiHelper gui) {
            super(gui, ALCHEMY, "thaumcraft.jei.crucible",
                    new ItemStack(ThaumcraftMod.CRUCIBLE.get()), 176, 92);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, AlchemyRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 12, 18).addIngredients(recipe.catalystIngredient());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 146, 18).addItemStack(stack(recipe.resultItemId(), recipe.resultCount()));
        }

        @Override
        public void draw(AlchemyRecipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
            drawLabel(pose, "thaumcraft.jei.research", blankAsDash(recipe.research()), 42, 4);
            drawLabel(pose, "thaumcraft.jei.original_recipe", blankAsDash(recipe.tc4Key()), 42, 15);
            drawAspects(pose, recipe.cost(), 12, 54, 6);
        }

        @Override
        public List<Component> getTooltipStrings(AlchemyRecipe recipe, IRecipeSlotsView slots, double x, double y) {
            return aspectTooltip(recipe.cost(), x, y, 12, 54, 6);
        }

        @Override public ResourceLocation getRegistryName(AlchemyRecipe recipe) { return recipe.id(); }
    }

    private static final class InfusionCategory extends BaseCategory<InfusionRecipe> {
        InfusionCategory(IGuiHelper gui) {
            super(gui, INFUSION, "thaumcraft.jei.infusion",
                    new ItemStack(ThaumcraftMod.INFUSION_MATRIX.get()), 176, 128);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
            List<ItemStack> catalysts = recipe.isInfusionEnchantment()
                    ? infusionEnchantmentExamples(recipe)
                    : List.of(stack(recipe.catalystId(), 1));
            catalysts = catalysts.stream().filter(stack -> !stack.isEmpty()).toList();
            if (!catalysts.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, 2, 38).addItemStacks(catalysts);
            }

            List<ResourceLocation> components = recipe.components();
            for (int i = 0; i < Math.min(12, components.size()); i++) {
                builder.addSlot(RecipeIngredientRole.INPUT, 30 + (i % 6) * 18, 20 + (i / 6) * 18)
                        .addItemStack(stack(components.get(i), 1));
            }
            for (int i = 12; i < components.size(); i++) {
                builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(stack(components.get(i), 1));
            }

            List<ItemStack> outputs = recipe.isInfusionEnchantment()
                    ? infusionEnchantmentOutputs(recipe, catalysts)
                    : List.of(recipe.result());
            outputs = outputs.stream().filter(stack -> !stack.isEmpty()).toList();
            if (outputs.isEmpty() && !catalysts.isEmpty()) {
                outputs = List.of(catalysts.get(0).copy());
            }
            if (!outputs.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, 156, 38).addItemStacks(outputs);
            }
        }

        @Override
        public void draw(InfusionRecipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
            drawLabel(pose, "thaumcraft.jei.research", blankAsDash(recipe.research()), 2, 2);
            drawLabel(pose, "thaumcraft.jei.instability", Integer.toString(recipe.instability()), 104, 2);
            drawLabel(pose, "thaumcraft.jei.original_recipe", blankAsDash(recipe.tc4Key()), 2, 13);
            drawAspects(pose, recipe.aspectCost(), 3, 78, 6);
            if (recipe.isInfusionEnchantment()) {
                Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(recipe.enchantmentId());
                Component output = enchantment == null
                        ? Component.translatable("thaumcraft.jei.special_output")
                        : Component.translatable("thaumcraft.jei.infusion_enchantment_output", enchantment.getFullname(1));
                Minecraft.getInstance().font.draw(pose, output, 3, 116, 0x704090);
            } else if (recipe.isRunicAugmentRecipe()) {
                Minecraft.getInstance().font.draw(pose, Component.translatable("thaumcraft.jei.special_output"), 3, 116, 0x704090);
            }
        }

        @Override
        public List<Component> getTooltipStrings(InfusionRecipe recipe, IRecipeSlotsView slots, double x, double y) {
            return aspectTooltip(recipe.aspectCost(), x, y, 3, 78, 6);
        }

        @Override public ResourceLocation getRegistryName(InfusionRecipe recipe) { return recipe.id(); }
    }

    private static List<ItemStack> infusionEnchantmentExamples(InfusionRecipe recipe) {
        ResourceLocation id = recipe.enchantmentId();
        if (id == null) {
            return List.of();
        }
        String path = id.getPath();
        ItemStack example;
        if (path.equals("feather_falling") || path.equals("haste")) {
            example = new ItemStack(Items.DIAMOND_BOOTS);
        } else if (path.equals("respiration") || path.equals("aqua_affinity")) {
            example = new ItemStack(Items.DIAMOND_HELMET);
        } else if (path.equals("protection") || path.equals("fire_protection")
                || path.equals("blast_protection") || path.equals("projectile_protection")
                || path.equals("thorns")) {
            example = new ItemStack(Items.DIAMOND_CHESTPLATE);
        } else if (path.equals("sharpness") || path.equals("smite")
                || path.equals("bane_of_arthropods") || path.equals("knockback")
                || path.equals("fire_aspect") || path.equals("looting")) {
            example = new ItemStack(Items.DIAMOND_SWORD);
        } else if (path.equals("power") || path.equals("punch")
                || path.equals("flame") || path.equals("infinity")) {
            example = new ItemStack(Items.BOW);
        } else if (path.equals("repair")) {
            example = stack(new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_thaumiumpick"), 1);
            if (example.isEmpty()) {
                example = new ItemStack(Items.DIAMOND_PICKAXE);
            }
        } else {
            example = new ItemStack(Items.DIAMOND_PICKAXE);
        }
        return List.of(example);
    }

    private static List<ItemStack> infusionEnchantmentOutputs(InfusionRecipe recipe, List<ItemStack> catalysts) {
        Enchantment enchantment = recipe.enchantmentId() == null
                ? null
                : ForgeRegistries.ENCHANTMENTS.getValue(recipe.enchantmentId());
        if (enchantment == null) {
            return catalysts;
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack catalyst : catalysts) {
            ItemStack output = catalyst.copy();
            Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(output));
            enchantments.put(enchantment, Math.min(enchantment.getMaxLevel(), enchantments.getOrDefault(enchantment, 0) + 1));
            EnchantmentHelper.setEnchantments(enchantments, output);
            outputs.add(output);
        }
        return outputs;
    }

    private static final class JarLabelCategory extends BaseCategory<JarLabelJeiRecipe> {
        JarLabelCategory(IGuiHelper gui) {
            super(gui, JAR_LABEL, "thaumcraft.jei.jar_label",
                    new ItemStack(ThaumcraftMod.JAR_LABEL.get()), 120, 48);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JarLabelJeiRecipe recipe, IFocusGroup focuses) {
            if (recipe.clear()) {
                builder.setShapeless(34, 6);
                builder.addSlot(RecipeIngredientRole.INPUT, 14, 18).addItemStacks(allAspectLabels());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 18)
                        .addItemStack(new ItemStack(ThaumcraftMod.JAR_LABEL.get()));
                return;
            }

            builder.setShapeless(52, 6);
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 18)
                    .addItemStack(new ItemStack(ThaumcraftMod.JAR_LABEL.get()));
            builder.addSlot(RecipeIngredientRole.INPUT, 29, 18).addItemStack(recipe.phial());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 18).addItemStack(recipe.output());
        }

        @Override
        public void draw(JarLabelJeiRecipe recipe, IRecipeSlotsView slots, PoseStack pose,
                         double mouseX, double mouseY) {
            String text = recipe.clear() ? "Any aspect → blank" : recipe.aspect().displayName();
            Minecraft.getInstance().font.draw(pose, text, 5, 4,
                    recipe.clear() ? 0x606060 : recipe.aspect().nativeColor());
        }

        @Override
        public ResourceLocation getRegistryName(JarLabelJeiRecipe recipe) {
            String suffix = recipe.clear() ? "clear" : recipe.aspect().id();
            return new ResourceLocation(ThaumcraftMod.MOD_ID, "jei/jar_label/" + suffix);
        }
    }

    private static List<JarLabelJeiRecipe> jarLabelRecipes() {
        List<JarLabelJeiRecipe> recipes = new ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            ItemStack phial = new ItemStack(ThaumcraftMod.ESSENTIA_PHIAL.get());
            EssentiaPhialItem.setEssentia(phial, aspect, 8);
            recipes.add(new JarLabelJeiRecipe(aspect, phial, JarLabelItem.withAspect(aspect), false));
        }
        recipes.add(new JarLabelJeiRecipe(Aspect.AER, ItemStack.EMPTY, ItemStack.EMPTY, true));
        return List.copyOf(recipes);
    }

    private static List<ItemStack> allAspectLabels() {
        List<ItemStack> labels = new ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            labels.add(JarLabelItem.withAspect(aspect));
        }
        return labels;
    }

    private record JarLabelJeiRecipe(Aspect aspect, ItemStack phial, ItemStack output, boolean clear) {
    }

    private static Ingredient asIngredient(ResourceLocation id) {
        if (ArcaneWorkbenchRecipe.isTagIngredient(id)) {
            String text = ArcaneWorkbenchRecipe.ingredientText(id);
            ResourceLocation tag = new ResourceLocation(text.substring(1));
            return Ingredient.of(TagKey.create(Registry.ITEM_REGISTRY, tag));
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? Ingredient.EMPTY : Ingredient.of(item);
    }

    private static ItemStack stack(ResourceLocation id, int count) {
        if (id == null) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, count));
    }

    private static String blankAsDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
