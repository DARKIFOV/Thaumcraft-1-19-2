package com.darkifov.thaumcraft.research;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime-safe 1.19.2 representation of a TC4 ResearchItem.
 *
 * Stage116 keeps the old constructor for the previous rebuild entries, but adds
 * the original TC4 category, coordinates, complexity, aspect tags, hidden parents,
 * trigger metadata, page keys and warp value extracted from ConfigResearch.java.
 */
public class ResearchEntry {
    private final String key;
    private final String title;
    private final String description;
    private final String[] requirements;
    private final String category;
    private final int displayColumn;
    private final int displayRow;
    private final int complexity;
    private final Map<String, Integer> aspects;
    private final String[] hiddenRequirements;
    private final String[] siblings;
    private final String[] flags;
    private final String[] pageTextKeys;
    private final String[] pageTypes;
    private final String[] recipeKeys;
    private final String[] entityTriggers;
    private final String[] aspectTriggers;
    private final int warp;

    public ResearchEntry(String key, String title, String description, String... requirements) {
        this(key, title, description, "REBUILD", 0, 0, 1, Collections.emptyMap(), requirements,
                new String[0], new String[0], new String[0], new String[0], new String[0],
                new String[0], new String[0], new String[0], 0);
    }

    public ResearchEntry(String key, String title, String description, String category,
                         int displayColumn, int displayRow, int complexity,
                         Map<String, Integer> aspects,
                         String[] requirements,
                         String[] hiddenRequirements,
                         String[] siblings,
                         String[] flags,
                         String[] pageTextKeys,
                         String[] pageTypes,
                         String[] recipeKeys,
                         String[] entityTriggers,
                         String[] aspectTriggers,
                         int warp) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.category = category == null ? "REBUILD" : category;
        this.displayColumn = displayColumn;
        this.displayRow = displayRow;
        this.complexity = Math.max(1, Math.min(3, complexity));
        this.aspects = Collections.unmodifiableMap(new LinkedHashMap<>(aspects));
        this.requirements = requirements == null ? new String[0] : requirements;
        this.hiddenRequirements = hiddenRequirements == null ? new String[0] : hiddenRequirements;
        this.siblings = siblings == null ? new String[0] : siblings;
        this.flags = flags == null ? new String[0] : flags;
        this.pageTextKeys = pageTextKeys == null ? new String[0] : pageTextKeys;
        this.pageTypes = pageTypes == null ? new String[0] : pageTypes;
        this.recipeKeys = recipeKeys == null ? new String[0] : recipeKeys;
        this.entityTriggers = entityTriggers == null ? new String[0] : entityTriggers;
        this.aspectTriggers = aspectTriggers == null ? new String[0] : aspectTriggers;
        this.warp = Math.max(0, warp);
    }

    public String key() { return key; }
    public String title() { return title; }
    public String description() { return description; }
    public String[] requirements() { return requirements; }
    public String category() { return category; }
    public int displayColumn() { return displayColumn; }
    public int displayRow() { return displayRow; }
    public int complexity() { return complexity; }
    public Map<String, Integer> aspects() { return aspects; }
    public String[] hiddenRequirements() { return hiddenRequirements; }
    public String[] siblings() { return siblings; }
    public String[] flags() { return flags; }
    public String[] pageTextKeys() { return pageTextKeys; }
    public String[] pageTypes() { return pageTypes; }
    public String[] recipeKeys() { return recipeKeys; }
    public String[] entityTriggers() { return entityTriggers; }
    public String[] aspectTriggers() { return aspectTriggers; }
    public int warp() { return warp; }

    public boolean hasFlag(String flag) {
        for (String value : flags) {
            if (value.equals(flag)) {
                return true;
            }
        }
        return false;
    }
}
