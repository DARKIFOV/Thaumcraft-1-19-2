package com.darkifov.thaumcraft.research;

public class ResearchEntry {
    private final String key;
    private final String title;
    private final String description;
    private final String[] requirements;

    public ResearchEntry(String key, String title, String description, String... requirements) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
    }

    public String key() {
        return key;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String[] requirements() {
        return requirements;
    }
}
