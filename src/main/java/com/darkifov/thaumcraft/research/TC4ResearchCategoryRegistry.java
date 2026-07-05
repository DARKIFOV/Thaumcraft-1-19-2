package com.darkifov.thaumcraft.research;

import java.util.List;

/** Strict TC4 category metadata extracted from ConfigResearch.initCategories(). */
public final class TC4ResearchCategoryRegistry {
    private TC4ResearchCategoryRegistry() {}

    public record TC4ResearchCategory(String key, String name, String iconTexture, String backgroundTexture) {}

    public static final List<TC4ResearchCategory> CATEGORIES = List.of(
            new TC4ResearchCategory("BASICS", "Basic Information", "thaumcraft:textures/items/thaumonomiconcheat.png", "thaumcraft:textures/gui/gui_researchback.png"),
            new TC4ResearchCategory("THAUMATURGY", "Thaumaturgy", "thaumcraft:textures/misc/r_thaumaturgy.png", "thaumcraft:textures/gui/gui_researchback.png"),
            new TC4ResearchCategory("ALCHEMY", "Alchemy", "thaumcraft:textures/misc/r_crucible.png", "thaumcraft:textures/gui/gui_researchback.png"),
            new TC4ResearchCategory("ARTIFICE", "Artifice", "thaumcraft:textures/misc/r_artifice.png", "thaumcraft:textures/gui/gui_researchback.png"),
            new TC4ResearchCategory("GOLEMANCY", "Golemancy", "thaumcraft:textures/misc/r_golemancy.png", "thaumcraft:textures/gui/gui_researchback.png"),
            new TC4ResearchCategory("ELDRITCH", "Eldritch", "thaumcraft:textures/misc/r_eldritch.png", "thaumcraft:textures/gui/gui_researchbackeldritch.png")
    );

    public static TC4ResearchCategory byKey(String key) {
        for (TC4ResearchCategory category : CATEGORIES) {
            if (category.key().equals(key)) {
                return category;
            }
        }
        return CATEGORIES.get(0);
    }
}
