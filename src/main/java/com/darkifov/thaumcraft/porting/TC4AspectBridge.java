package com.darkifov.thaumcraft.porting;

import com.darkifov.thaumcraft.Aspect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Strict source bridge generated from the uploaded Thaumcraft 4.2.3.5 decompiled source:
 * Thaumcraft4-1.7.10-master/thaumcraft/api/aspects/Aspect.java.
 *
 * This class does not invent new aspect data. It locks the 1.19.2 rebuild to the original
 * TC4 tags, RGB colors, primal/compound graph, blend values, chat color codes and texture ids.
 */
public final class TC4AspectBridge {
    public static final String SOURCE_CLASS = "thaumcraft/api/aspects/Aspect.java";
    public static final String SOURCE_VERSION = "Thaumcraft 4.2.3.5 / Minecraft 1.7.10";

    public record Definition(
            String originalConstantName,
            String tag,
            int color,
            String chatColorCode,
            int blend,
            String firstComponentTag,
            String secondComponentTag,
            String texture
    ) {
        public boolean isPrimal() {
            return firstComponentTag.isEmpty() || secondComponentTag.isEmpty();
        }

        public int nativeColor() {
            return color & 0xFFFFFF;
        }
    }

    private static final List<Definition> DEFINITIONS = List.of(
            new Definition("AIR", "aer", 16777086, "e", 1, "", "", "thaumcraft:textures/aspects/aer.png"),
            new Definition("EARTH", "terra", 5685248, "2", 1, "", "", "thaumcraft:textures/aspects/terra.png"),
            new Definition("FIRE", "ignis", 16734721, "c", 1, "", "", "thaumcraft:textures/aspects/ignis.png"),
            new Definition("WATER", "aqua", 3986684, "3", 1, "", "", "thaumcraft:textures/aspects/aqua.png"),
            new Definition("ORDER", "ordo", 14013676, "7", 1, "", "", "thaumcraft:textures/aspects/ordo.png"),
            new Definition("ENTROPY", "perditio", 4210752, "8", 771, "", "", "thaumcraft:textures/aspects/perditio.png"),
            new Definition("VOID", "vacuos", 8947848, "", 771, "aer", "perditio", "thaumcraft:textures/aspects/vacuos.png"),
            new Definition("LIGHT", "lux", 16774755, "", 1, "aer", "ignis", "thaumcraft:textures/aspects/lux.png"),
            new Definition("WEATHER", "tempestas", 16777215, "", 1, "aer", "aqua", "thaumcraft:textures/aspects/tempestas.png"),
            new Definition("MOTION", "motus", 13487348, "", 1, "aer", "ordo", "thaumcraft:textures/aspects/motus.png"),
            new Definition("COLD", "gelum", 14811135, "", 1, "ignis", "perditio", "thaumcraft:textures/aspects/gelum.png"),
            new Definition("CRYSTAL", "vitreus", 8454143, "", 1, "terra", "ordo", "thaumcraft:textures/aspects/vitreus.png"),
            new Definition("LIFE", "victus", 14548997, "", 1, "aqua", "terra", "thaumcraft:textures/aspects/victus.png"),
            new Definition("POISON", "venenum", 9039872, "", 1, "aqua", "perditio", "thaumcraft:textures/aspects/venenum.png"),
            new Definition("ENERGY", "potentia", 12648447, "", 1, "ordo", "ignis", "thaumcraft:textures/aspects/potentia.png"),
            new Definition("EXCHANGE", "permutatio", 5735255, "", 1, "perditio", "ordo", "thaumcraft:textures/aspects/permutatio.png"),
            new Definition("METAL", "metallum", 11908557, "", 1, "terra", "vitreus", "thaumcraft:textures/aspects/metallum.png"),
            new Definition("DEATH", "mortuus", 8943496, "", 1, "victus", "perditio", "thaumcraft:textures/aspects/mortuus.png"),
            new Definition("FLIGHT", "volatus", 15198167, "", 1, "aer", "motus", "thaumcraft:textures/aspects/volatus.png"),
            new Definition("DARKNESS", "tenebrae", 2236962, "", 1, "vacuos", "lux", "thaumcraft:textures/aspects/tenebrae.png"),
            new Definition("SOUL", "spiritus", 15461371, "", 1, "victus", "mortuus", "thaumcraft:textures/aspects/spiritus.png"),
            new Definition("HEAL", "sano", 16723764, "", 1, "victus", "ordo", "thaumcraft:textures/aspects/sano.png"),
            new Definition("TRAVEL", "iter", 14702683, "", 1, "motus", "terra", "thaumcraft:textures/aspects/iter.png"),
            new Definition("ELDRITCH", "alienis", 8409216, "", 1, "vacuos", "tenebrae", "thaumcraft:textures/aspects/alienis.png"),
            new Definition("MAGIC", "praecantatio", 9896128, "", 1, "vacuos", "potentia", "thaumcraft:textures/aspects/praecantatio.png"),
            new Definition("AURA", "auram", 16761087, "", 1, "praecantatio", "aer", "thaumcraft:textures/aspects/auram.png"),
            new Definition("TAINT", "vitium", 8388736, "", 1, "praecantatio", "perditio", "thaumcraft:textures/aspects/vitium.png"),
            new Definition("SLIME", "limus", 129024, "", 1, "victus", "aqua", "thaumcraft:textures/aspects/limus.png"),
            new Definition("PLANT", "herba", 109568, "", 1, "victus", "terra", "thaumcraft:textures/aspects/herba.png"),
            new Definition("TREE", "arbor", 8873265, "", 1, "aer", "herba", "thaumcraft:textures/aspects/arbor.png"),
            new Definition("BEAST", "bestia", 10445833, "", 1, "motus", "victus", "thaumcraft:textures/aspects/bestia.png"),
            new Definition("FLESH", "corpus", 15615885, "", 1, "mortuus", "bestia", "thaumcraft:textures/aspects/corpus.png"),
            new Definition("UNDEAD", "exanimis", 3817472, "", 1, "motus", "mortuus", "thaumcraft:textures/aspects/exanimis.png"),
            new Definition("MIND", "cognitio", 16761523, "", 1, "ignis", "spiritus", "thaumcraft:textures/aspects/cognitio.png"),
            new Definition("SENSES", "sensus", 1038847, "", 1, "aer", "spiritus", "thaumcraft:textures/aspects/sensus.png"),
            new Definition("MAN", "humanus", 16766912, "", 1, "bestia", "cognitio", "thaumcraft:textures/aspects/humanus.png"),
            new Definition("CROP", "messis", 14791537, "", 1, "herba", "humanus", "thaumcraft:textures/aspects/messis.png"),
            new Definition("MINE", "perfodio", 14471896, "", 1, "humanus", "terra", "thaumcraft:textures/aspects/perfodio.png"),
            new Definition("TOOL", "instrumentum", 4210926, "", 1, "humanus", "ordo", "thaumcraft:textures/aspects/instrumentum.png"),
            new Definition("HARVEST", "meto", 15641986, "", 1, "messis", "instrumentum", "thaumcraft:textures/aspects/meto.png"),
            new Definition("WEAPON", "telum", 12603472, "", 1, "instrumentum", "ignis", "thaumcraft:textures/aspects/telum.png"),
            new Definition("ARMOR", "tutamen", 49344, "", 1, "instrumentum", "terra", "thaumcraft:textures/aspects/tutamen.png"),
            new Definition("HUNGER", "fames", 10093317, "", 1, "victus", "vacuos", "thaumcraft:textures/aspects/fames.png"),
            new Definition("GREED", "lucrum", 15121988, "", 1, "humanus", "fames", "thaumcraft:textures/aspects/lucrum.png"),
            new Definition("CRAFT", "fabrico", 8428928, "", 1, "humanus", "instrumentum", "thaumcraft:textures/aspects/fabrico.png"),
            new Definition("CLOTH", "pannus", 15395522, "", 1, "instrumentum", "bestia", "thaumcraft:textures/aspects/pannus.png"),
            new Definition("MECHANISM", "machina", 8421536, "", 1, "motus", "instrumentum", "thaumcraft:textures/aspects/machina.png"),
            new Definition("TRAP", "vinculum", 10125440, "", 1, "motus", "perditio", "thaumcraft:textures/aspects/vinculum.png")
    );

    private static final Map<String, Definition> BY_TAG;
    private static final Map<Aspect, Definition> BY_CURRENT_ASPECT;

    static {
        Map<String, Definition> byTag = new LinkedHashMap<>();
        Map<Aspect, Definition> byAspect = new LinkedHashMap<>();

        for (Definition definition : DEFINITIONS) {
            byTag.put(definition.tag(), definition);
            Aspect current = Aspect.byId(definition.tag());
            if (current != null) {
                byAspect.put(current, definition);
            }
        }

        BY_TAG = Collections.unmodifiableMap(byTag);
        BY_CURRENT_ASPECT = Collections.unmodifiableMap(byAspect);
    }

    private TC4AspectBridge() {
    }

    public static List<Definition> all() {
        return DEFINITIONS;
    }

    public static Map<String, Definition> byTagMap() {
        return BY_TAG;
    }

    public static Definition byTag(String tag) {
        if (tag == null) {
            return null;
        }
        return BY_TAG.get(tag.toLowerCase(Locale.ROOT));
    }

    public static Definition require(String tag) {
        Definition definition = byTag(tag);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown TC4 aspect tag: " + tag);
        }
        return definition;
    }

    public static Definition forAspect(Aspect aspect) {
        return aspect == null ? null : BY_CURRENT_ASPECT.get(aspect);
    }

    public static int count() {
        return DEFINITIONS.size();
    }

    public static int primalCount() {
        int count = 0;
        for (Definition definition : DEFINITIONS) {
            if (definition.isPrimal()) {
                count++;
            }
        }
        return count;
    }

    public static int compoundCount() {
        return count() - primalCount();
    }
}
