package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InfusionRecipe {
    private final ResourceLocation id;
    private final ResourceLocation catalystId;
    private final ResourceLocation resultItemId;
    private final int resultCount;
    private final int instability;
    private final String research;
    private final String tc4Key;
    private final String tc4Kind;
    private final int recipeType;
    private final ResourceLocation enchantmentId;
    private final String outputNbtLabel;
    private final Tag outputNbt;
    private final boolean catalystWildcard;
    private int catalystDamage = TC4InfusionItemMatcher.ANY_DAMAGE;
    private CompoundTag catalystTag = null;
    private final List<ResourceLocation> components = new ArrayList<>();
    private final List<ComponentSpec> componentSpecs = new ArrayList<>();
    private final EnumMap<Aspect, Integer> aspectCost = new EnumMap<>(Aspect.class);

    public InfusionRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, int instability) {
        this(id, catalystId, resultItemId, resultCount, instability, "");
    }

    public InfusionRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, int instability, String research) {
        this(id, catalystId, resultItemId, resultCount, instability, research, "", "");
    }

    private InfusionRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, int instability, String research, String tc4Key, String tc4Kind) {
        this(id, catalystId, resultItemId, resultCount, instability, research, tc4Key, tc4Kind, 0, null, "", null, false);
    }

    private InfusionRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, int instability,
                           String research, String tc4Key, String tc4Kind, int recipeType, ResourceLocation enchantmentId,
                           String outputNbtLabel, Tag outputNbt, boolean catalystWildcard) {
        this.id = id;
        this.catalystId = catalystId;
        this.resultItemId = resultItemId;
        this.resultCount = Math.max(1, resultCount);
        this.instability = Math.max(0, instability);
        this.research = research == null ? "" : research;
        this.tc4Key = tc4Key == null ? "" : tc4Key;
        this.tc4Kind = tc4Kind == null ? "" : tc4Kind;
        this.recipeType = recipeType;
        this.enchantmentId = enchantmentId;
        this.outputNbtLabel = outputNbtLabel == null ? "" : outputNbtLabel;
        this.outputNbt = outputNbt == null ? null : outputNbt.copy();
        this.catalystWildcard = catalystWildcard;
    }


    public static InfusionRecipe runicAugmentRecipe(ResourceLocation id) {
        return new InfusionRecipe(
                id,
                null,
                new ResourceLocation("minecraft", "air"),
                1,
                5,
                TC4InfusionRunicAugmentAdapter.RESEARCH,
                TC4InfusionRunicAugmentAdapter.RESEARCH,
                TC4InfusionRunicAugmentAdapter.TC4_KIND,
                0,
                null,
                "",
                null,
                true);
    }

    public static InfusionRecipe enchantmentRecipe(TC4InfusionEnchantmentIndex.Entry entry, List<ResourceLocation> components) {
        InfusionRecipe recipe = new InfusionRecipe(
                new ResourceLocation("thaumcraft", "tc4_enchantment_" + entry.stableId()),
                null,
                new ResourceLocation("minecraft", "air"),
                1,
                entry.instability(),
                entry.requiredResearch(),
                entry.tc4Key(),
                "INFUSION_ENCHANTMENT",
                1,
                new ResourceLocation(entry.modernEnchantmentId()),
                "",
                null,
                true);
        for (ResourceLocation component : components) {
            recipe.component(component);
        }
        for (Map.Entry<Aspect, Integer> aspect : entry.aspects().entrySet()) {
            recipe.require(aspect.getKey(), aspect.getValue());
        }
        return recipe;
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation catalystId() {
        return catalystId;
    }

    public ResourceLocation resultItemId() {
        return resultItemId;
    }

    public int resultCount() {
        return resultCount;
    }

    public int instability() {
        return instability;
    }

    public String research() {
        return research;
    }

    public String tc4Key() {
        return tc4Key;
    }

    public String tc4Kind() {
        return tc4Kind;
    }

    public int recipeType() {
        return recipeType;
    }

    public ResourceLocation enchantmentId() {
        return enchantmentId;
    }

    public String outputNbtLabel() {
        return outputNbtLabel;
    }

    public Tag outputNbt() {
        return outputNbt == null ? null : outputNbt.copy();
    }

    public boolean hasNbtOutput() {
        return outputNbt != null && !outputNbtLabel.isBlank();
    }

    public boolean isInfusionEnchantment() {
        return recipeType == 1 || "INFUSION_ENCHANTMENT".equals(tc4Kind);
    }

    public boolean isRunicAugmentRecipe() {
        return TC4InfusionRunicAugmentAdapter.TC4_KIND.equals(tc4Kind);
    }

    public List<ResourceLocation> componentsFor(ItemStack catalyst) {
        return isRunicAugmentRecipe() ? TC4InfusionRunicAugmentAdapter.componentsFor(catalyst) : components;
    }

    public List<ComponentSpec> componentSpecsFor(ItemStack catalyst) {
        List<ResourceLocation> ids = componentsFor(catalyst);
        if (!isRunicAugmentRecipe() && !isInfusionEnchantment() && componentSpecs.size() == ids.size()) {
            return componentSpecs;
        }
        List<ComponentSpec> specs = new ArrayList<>();
        for (ResourceLocation id : ids) {
            specs.add(new ComponentSpec(id, TC4InfusionItemMatcher.ANY_DAMAGE, null));
        }
        return specs;
    }

    public EnumMap<Aspect, Integer> aspectCostFor(ItemStack catalyst) {
        return isRunicAugmentRecipe() ? TC4InfusionRunicAugmentAdapter.aspectsFor(catalyst) : aspectCost;
    }

    public int instabilityFor(ItemStack catalyst) {
        return isRunicAugmentRecipe() ? TC4InfusionRunicAugmentAdapter.instabilityFor(catalyst) : instability;
    }

    public List<ResourceLocation> components() {
        return components;
    }

    public List<ComponentSpec> componentSpecs() {
        return componentSpecs;
    }

    public EnumMap<Aspect, Integer> aspectCost() {
        return aspectCost;
    }

    public InfusionRecipe component(ResourceLocation id) {
        return component(id, TC4InfusionItemMatcher.ANY_DAMAGE, null);
    }

    public InfusionRecipe component(ResourceLocation id, int damage, CompoundTag tag) {
        if (id != null) {
            components.add(id);
            componentSpecs.add(new ComponentSpec(id, damage, tag));
        }
        return this;
    }

    public InfusionRecipe catalystDamage(int damage) {
        this.catalystDamage = damage;
        return this;
    }

    public InfusionRecipe catalystTag(CompoundTag tag) {
        this.catalystTag = tag == null ? null : tag.copy();
        return this;
    }

    public InfusionRecipe require(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspectCost.put(aspect, amount);
        }

        return this;
    }

    public boolean catalystMatches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (isInfusionEnchantment()) {
            return TC4InfusionEnchantmentAdapter.canApply(this, stack);
        }
        if (isRunicAugmentRecipe()) {
            return TC4InfusionRunicAugmentAdapter.canApply(stack);
        }
        return TC4InfusionItemMatcher.catalystMatches(stack, catalystId, catalystWildcard, catalystDamage, catalystTag);
    }

    public boolean componentMatches(ItemStack stack, ResourceLocation componentId) {
        if (stack == null || stack.isEmpty() || componentId == null) {
            return false;
        }
        boolean sawSpec = false;
        for (ComponentSpec spec : componentSpecs) {
            if (!spec.itemId().equals(componentId)) {
                continue;
            }
            sawSpec = true;
            if (componentMatches(stack, spec)) {
                return true;
            }
        }
        return !sawSpec && TC4InfusionItemMatcher.matches(stack, componentId);
    }

    public boolean componentMatches(ItemStack stack, ComponentSpec spec) {
        if (stack == null || stack.isEmpty() || spec == null || spec.itemId() == null) {
            return false;
        }
        return TC4InfusionItemMatcher.matches(stack, spec.itemId(), spec.damage(), spec.tag(), true);
    }

    public ItemStack result() {
        if (isInfusionEnchantment() || isRunicAugmentRecipe() || resultItemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(resultItemId);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, resultCount);
    }

    public static InfusionRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation catalyst;
        int catalystDamage = TC4InfusionItemMatcher.ANY_DAMAGE;
        CompoundTag catalystTag = null;
        JsonElement catalystElement = json.get("catalyst");
        if (catalystElement != null && catalystElement.isJsonObject()) {
            JsonObject catalystObject = catalystElement.getAsJsonObject();
            catalyst = new ResourceLocation(catalystObject.get("item").getAsString());
            catalystDamage = readDamage(catalystObject);
            catalystTag = readNbt(catalystObject);
        } else {
            catalyst = new ResourceLocation(catalystElement.getAsString());
        }

        JsonObject resultObject = json.getAsJsonObject("result");
        ResourceLocation resultItem = new ResourceLocation(resultObject.get("item").getAsString());
        int count = resultObject.has("count") ? resultObject.get("count").getAsInt() : 1;
        int instability = json.has("instability") ? json.get("instability").getAsInt() : 0;
        String research = json.has("research") ? json.get("research").getAsString() : "";
        String tc4Key = json.has("tc4_key") ? json.get("tc4_key").getAsString() : "";
        String tc4Kind = json.has("tc4_kind") ? json.get("tc4_kind").getAsString() : (json.has("kind") ? json.get("kind").getAsString() : "");
        String outputNbtLabel = resultObject.has("output_nbt_label") ? resultObject.get("output_nbt_label").getAsString() : "";
        Tag outputNbt = readNbtOutput(resultObject);

        InfusionRecipe recipe = new InfusionRecipe(id, catalyst, resultItem, count, instability, research, tc4Key, tc4Kind, 0, null, outputNbtLabel, outputNbt, false)
                .catalystDamage(catalystDamage)
                .catalystTag(catalystTag);

        JsonArray components = json.getAsJsonArray("components");
        for (JsonElement element : components) {
            if (element.isJsonPrimitive()) {
                recipe.component(new ResourceLocation(element.getAsString()));
            } else if (element.isJsonObject()) {
                JsonObject component = element.getAsJsonObject();
                if (component.has("item")) {
                    recipe.component(new ResourceLocation(component.get("item").getAsString()), readDamage(component), readNbt(component));
                }
            }
        }

        JsonObject aspects = json.getAsJsonObject("aspects");
        for (Map.Entry<String, JsonElement> entry : aspects.entrySet()) {
            try {
                Aspect aspect = Aspect.valueOf(entry.getKey().toUpperCase());
                recipe.require(aspect, entry.getValue().getAsInt());
            } catch (IllegalArgumentException ignored) {
            }
        }

        return recipe;
    }
    private static Tag readNbtOutput(JsonObject object) {
        if (!object.has("output_nbt_label")) {
            return null;
        }
        String type = object.has("output_nbt_type") ? object.get("output_nbt_type").getAsString().toLowerCase(java.util.Locale.ROOT) : "compound";
        if ("byte".equals(type)) {
            return ByteTag.valueOf((byte) object.get("output_nbt_value").getAsInt());
        }
        if ("int".equals(type)) {
            return IntTag.valueOf(object.get("output_nbt_value").getAsInt());
        }
        if ("string".equals(type)) {
            return StringTag.valueOf(object.get("output_nbt_value").getAsString());
        }
        if (object.has("output_nbt")) {
            try {
                return TagParser.parseTag(object.get("output_nbt").getAsString());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static int readDamage(JsonObject object) {
        if (object.has("damage_wildcard") && object.get("damage_wildcard").getAsBoolean()) {
            return TC4InfusionItemMatcher.WILDCARD_DAMAGE;
        }
        if (object.has("damage")) {
            return object.get("damage").getAsInt();
        }
        if (object.has("meta")) {
            int meta = object.get("meta").getAsInt();
            return meta == TC4InfusionItemMatcher.WILDCARD_DAMAGE ? TC4InfusionItemMatcher.WILDCARD_DAMAGE : meta;
        }
        return TC4InfusionItemMatcher.ANY_DAMAGE;
    }

    private static CompoundTag readNbt(JsonObject object) {
        if (!object.has("nbt")) {
            return null;
        }
        try {
            return TagParser.parseTag(object.get("nbt").getAsString());
        } catch (Exception ignored) {
            return null;
        }
    }

    public record ComponentSpec(ResourceLocation itemId, int damage, CompoundTag tag) {
        public ComponentSpec {
            tag = tag == null ? null : tag.copy();
        }
    }

}
