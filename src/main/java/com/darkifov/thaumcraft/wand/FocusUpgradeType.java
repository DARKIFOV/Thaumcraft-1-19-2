package com.darkifov.thaumcraft.wand;

import java.util.Arrays;
import java.util.Locale;

/**
 * Stage173 strict TC4 FocusUpgradeType registry.
 *
 * IDs, icon paths and localization keys are copied from original TC4 1.7.10
 * FocusUpgradeType plus the focus-specific static upgrade declarations in the
 * original focus classes.  This class intentionally stores only metadata/NBT
 * parity; Stage173 does not invent upgrade effects that still need their own
 * later parity stages.
 */
public enum FocusUpgradeType {
    POTENCY(0, "potency", "thaumcraft:textures/foci/potency.png", "focus.upgrade.potency.name", "focus.upgrade.potency.text", "WEAPON"),
    FRUGAL(1, "frugal", "thaumcraft:textures/foci/frugal.png", "focus.upgrade.frugal.name", "focus.upgrade.frugal.text", "HUNGER"),
    TREASURE(2, "treasure", "thaumcraft:textures/foci/treasure.png", "focus.upgrade.treasure.name", "focus.upgrade.treasure.text", "GREED"),
    ENLARGE(3, "enlarge", "thaumcraft:textures/foci/enlarge.png", "focus.upgrade.enlarge.name", "focus.upgrade.enlarge.text", "TRAVEL"),
    ALCHEMISTS_FIRE(4, "alchemistsfire", "thaumcraft:textures/foci/alchemistsfire.png", "focus.upgrade.alchemistsfire.name", "focus.upgrade.alchemistsfire.text", "ENERGY,SLIME"),
    ALCHEMISTS_FROST(5, "alchemistsfrost", "thaumcraft:textures/foci/alchemistsfrost.png", "focus.upgrade.alchemistsfrost.name", "focus.upgrade.alchemistsfrost.text", "COLD,TRAP"),
    ARCHITECT(6, "architect", "thaumcraft:textures/foci/architect.png", "focus.upgrade.architect.name", "focus.upgrade.architect.text", "CRAFT"),
    EXTEND(7, "extend", "thaumcraft:textures/foci/extend.png", "focus.upgrade.extend.name", "focus.upgrade.extend.text", "EXCHANGE"),
    SILK_TOUCH(8, "silktouch", "thaumcraft:textures/foci/silktouch.png", "focus.upgrade.silktouch.name", "focus.upgrade.silktouch.text", "GREED"),
    FIREBALL(9, "fireball", "thaumcraft:textures/foci/fireball.png", "focus.upgrade.fireball.name", "focus.upgrade.fireball.text", "DARKNESS"),
    FIREBEAM(10, "firebeam", "thaumcraft:textures/foci/firebeam.png", "focus.upgrade.firebeam.name", "focus.upgrade.firebeam.text", "ENERGY,AIR"),
    SCATTERSHOT(11, "scattershot", "thaumcraft:textures/foci/scattershot.png", "focus.upgrade.scattershot.name", "focus.upgrade.scattershot.text", "COLD,WEAPON"),
    ICE_BOULDER(12, "iceboulder", "thaumcraft:textures/foci/iceboulder.png", "focus.upgrade.iceboulder.name", "focus.upgrade.iceboulder.text", "COLD,CRYSTAL"),
    BAT_BOMBS(13, "batbombs", "thaumcraft:textures/foci/batbombs.png", "focus.upgrade.batbombs.name", "focus.upgrade.batbombs.text", "ENERGY,TRAP"),
    DEVIL_BATS(14, "devilbats", "thaumcraft:textures/foci/devilbats.png", "focus.upgrade.devilbats.name", "focus.upgrade.devilbats.text", "ARMOR"),
    NIGHTSHADE(15, "nightshade", "thaumcraft:textures/foci/nightshade.png", "focus.upgrade.nightshade.name", "focus.upgrade.nightshade.text", "LIFE,POISON,MAGIC"),
    SEEKER(16, "seeker", "thaumcraft:textures/foci/seeker.png", "focus.upgrade.seeker.name", "focus.upgrade.seeker.text", "SENSES,MIND"),
    CHAIN_LIGHTNING(17, "chainlightning", "thaumcraft:textures/foci/chainlightning.png", "focus.upgrade.chainlightning.name", "focus.upgrade.chainlightning.text", "WEATHER"),
    EARTH_SHOCK(18, "earthshock", "thaumcraft:textures/foci/earthshock.png", "focus.upgrade.earthshock.name", "focus.upgrade.earthshock.text", "WEATHER"),
    VAMPIRE_BATS(19, "vampirebats", "thaumcraft:textures/foci/vampirebats.png", "focus.upgrade.vampirebats.name", "focus.upgrade.vampirebats.text", "HUNGER,LIFE"),
    DOWSING(20, "dowsing", "thaumcraft:textures/foci/dowsing.png", "focus.upgrade.dowsing.name", "focus.upgrade.dowsing.text", "MINE");

    private final short id;
    private final String key;
    private final String icon;
    private final String nameKey;
    private final String textKey;
    private final String originalAspects;

    FocusUpgradeType(int id, String key, String icon, String nameKey, String textKey, String originalAspects) {
        this.id = (short) id;
        this.key = key;
        this.icon = icon;
        this.nameKey = nameKey;
        this.textKey = textKey;
        this.originalAspects = originalAspects;
    }

    public short id() { return id; }
    public String key() { return key; }
    public String icon() { return icon; }
    public String nameKey() { return nameKey; }
    public String textKey() { return textKey; }
    public String originalAspects() { return originalAspects; }

    public static FocusUpgradeType byId(short id) {
        return Arrays.stream(values()).filter(t -> t.id == id).findFirst().orElse(null);
    }

    public static FocusUpgradeType byKey(String key) {
        if (key == null || key.isBlank()) return null;
        String k = key.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(t -> t.key.equals(k) || t.name().equalsIgnoreCase(key)).findFirst().orElse(null);
    }
}
