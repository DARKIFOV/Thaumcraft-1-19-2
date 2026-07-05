package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 research categories from ConfigResearch.initCategories().
 * Stage117 removes rebuild-only fake category routing from the browser and uses
 * the original six Thaumcraft 4 tabs, icons and background maps.
 */
enum OriginalResearchCategory {
    BASICS("BASICS", "Basic Information", tex("textures/items/thaumonomiconcheat.png"), gui("gui_researchback.png")),
    THAUMATURGY("THAUMATURGY", "Thaumaturgy", tex("textures/misc/r_thaumaturgy.png"), gui("gui_researchback.png")),
    ALCHEMY("ALCHEMY", "Alchemy", tex("textures/misc/r_crucible.png"), gui("gui_researchback.png")),
    ARTIFICE("ARTIFICE", "Artifice", tex("textures/misc/r_artifice.png"), gui("gui_researchback.png")),
    GOLEMANCY("GOLEMANCY", "Golemancy", tex("textures/misc/r_golemancy.png"), gui("gui_researchback.png")),
    ELDRITCH("ELDRITCH", "Eldritch", tex("textures/misc/r_eldritch.png"), gui("gui_researchbackeldritch.png"));

    private final String key;
    private final String title;
    private final ResourceLocation icon;
    private final ResourceLocation background;

    OriginalResearchCategory(String key, String title, ResourceLocation icon, ResourceLocation background) {
        this.key = key;
        this.title = title;
        this.icon = icon;
        this.background = background;
    }

    String key() { return key; }
    String title() { return title; }
    ResourceLocation icon() { return icon; }
    ResourceLocation background() { return background; }

    static OriginalResearchCategory byKey(String key) {
        if (key != null) {
            for (OriginalResearchCategory category : values()) {
                if (category.key.equalsIgnoreCase(key)) {
                    return category;
                }
            }
        }
        return BASICS;
    }

    private static ResourceLocation tex(String path) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, path);
    }

    private static ResourceLocation gui(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/" + name);
    }
}
