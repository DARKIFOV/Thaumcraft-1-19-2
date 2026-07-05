package com.darkifov.thaumcraft.source;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Stage115 strict TC4 entity-aspect bridge generated from ConfigAspects.registerEntityAspects(). */
public final class TC4EntityAspectRegistry {
    private static final Map<String, AspectList> EXACT = new LinkedHashMap<>();

    static {
        exact("minecraft:zombie", aspects(Aspect.EXANIMIS, 2, Aspect.HUMANUS, 1, Aspect.TERRA, 1));
        exact("minecraft:giant", aspects(Aspect.EXANIMIS, 4, Aspect.HUMANUS, 3, Aspect.TERRA, 3));
        exact("minecraft:skeleton", aspects(Aspect.EXANIMIS, 3, Aspect.HUMANUS, 1, Aspect.TERRA, 1));
        exact("minecraft:creeper", aspects(Aspect.HERBA, 2, Aspect.IGNIS, 2));
        exact("minecraft:horse", aspects(Aspect.BESTIA, 4, Aspect.TERRA, 1, Aspect.AER, 1));
        exact("minecraft:pig", aspects(Aspect.BESTIA, 2, Aspect.TERRA, 2));
        exact("minecraft:experience_orb", aspects(Aspect.COGNITIO, 5));
        exact("minecraft:sheep", aspects(Aspect.BESTIA, 2, Aspect.TERRA, 2));
        exact("minecraft:cow", aspects(Aspect.BESTIA, 3, Aspect.TERRA, 3));
        exact("minecraft:mooshroom", aspects(Aspect.BESTIA, 3, Aspect.HERBA, 1, Aspect.TERRA, 2));
        exact("minecraft:snow_golem", aspects(Aspect.GELUM, 3, Aspect.AQUA, 1));
        exact("minecraft:ocelot", aspects(Aspect.BESTIA, 3, Aspect.PERDITIO, 3));
        exact("minecraft:chicken", aspects(Aspect.BESTIA, 2, Aspect.VOLATUS, 2, Aspect.AER, 1));
        exact("minecraft:squid", aspects(Aspect.BESTIA, 2, Aspect.AQUA, 2));
        exact("minecraft:wolf", aspects(Aspect.BESTIA, 3, Aspect.TERRA, 3));
        exact("minecraft:bat", aspects(Aspect.BESTIA, 1, Aspect.VOLATUS, 1, Aspect.AER, 1));
        exact("minecraft:boat", aspects(Aspect.MACHINA, 2, Aspect.AQUA, 2));
        exact("minecraft:spider", aspects(Aspect.BESTIA, 3, Aspect.PERDITIO, 2));
        exact("minecraft:slime", aspects(Aspect.LIMUS, 2, Aspect.AQUA, 2));
        exact("minecraft:ghast", aspects(Aspect.EXANIMIS, 3, Aspect.IGNIS, 2));
        exact("minecraft:zombified_piglin", aspects(Aspect.EXANIMIS, 4, Aspect.IGNIS, 2));
        exact("minecraft:enderman", aspects(Aspect.ALIENIS, 4, Aspect.ITER, 2, Aspect.AER, 2));
        exact("minecraft:cave_spider", aspects(Aspect.BESTIA, 2, Aspect.VENENUM, 2, Aspect.TERRA, 1));
        exact("minecraft:silverfish", aspects(Aspect.BESTIA, 1, Aspect.TERRA, 1));
        exact("minecraft:blaze", aspects(Aspect.ALIENIS, 4, Aspect.IGNIS, 1));
        exact("minecraft:magma_cube", aspects(Aspect.LIMUS, 3, Aspect.IGNIS, 2));
        exact("minecraft:ender_dragon", aspects(Aspect.ALIENIS, 20, Aspect.BESTIA, 20, Aspect.PERDITIO, 20));
        exact("minecraft:wither", aspects(Aspect.EXANIMIS, 20, Aspect.PERDITIO, 20, Aspect.IGNIS, 15));
        exact("minecraft:witch", aspects(Aspect.HUMANUS, 3, Aspect.PRAECANTATIO, 2, Aspect.IGNIS, 1));
        exact("minecraft:villager", aspects(Aspect.HUMANUS, 3, Aspect.AER, 2));
        exact("minecraft:iron_golem", aspects(Aspect.METALLUM, 4, Aspect.TERRA, 3));
        exact("minecraft:minecart", aspects(Aspect.MACHINA, 3, Aspect.AER, 2));
        exact("minecraft:chest_minecart", aspects(Aspect.MACHINA, 3, Aspect.AER, 1, Aspect.VACUOS, 1));
        exact("minecraft:furnace_minecart", aspects(Aspect.MACHINA, 3, Aspect.AER, 1, Aspect.IGNIS, 1));
        exact("minecraft:tnt_minecart", aspects(Aspect.MACHINA, 3, Aspect.AER, 1, Aspect.IGNIS, 1));
        exact("minecraft:hopper_minecart", aspects(Aspect.MACHINA, 3, Aspect.AER, 1, Aspect.PERMUTATIO, 1));
        exact("minecraft:spawner_minecart", aspects(Aspect.MACHINA, 3, Aspect.AER, 1, Aspect.PRAECANTATIO, 1));
        exact("minecraft:end_crystal", aspects(Aspect.ALIENIS, 3, Aspect.PRAECANTATIO, 3, Aspect.SANO, 3));
        exact("minecraft:item_frame", aspects(Aspect.SENSUS, 3, Aspect.PANNUS, 1));
        exact("minecraft:painting", aspects(Aspect.SENSUS, 5, Aspect.PANNUS, 3));
        exact("thaumcraft:pech", aspects(Aspect.HUMANUS, 2, Aspect.PRAECANTATIO, 2, Aspect.PERMUTATIO, 2, Aspect.LUCRUM, 2));
        exact("thaumcraft:taint_crawler", aspects(Aspect.LIMUS, 2, Aspect.PRAECANTATIO, 1, Aspect.AQUA, 1));
        exact("thaumcraft:brainy_zombie", aspects(Aspect.EXANIMIS, 3, Aspect.HUMANUS, 1, Aspect.COGNITIO, 1, Aspect.TERRA, 1));
        exact("thaumcraft:taintacle", aspects(Aspect.VITIUM, 3, Aspect.AQUA, 2));
        exact("thaumcraft:eldritch_guardian", aspects(Aspect.ALIENIS, 4, Aspect.MORTUUS, 2, Aspect.EXANIMIS, 4));
        exact("thaumcraft:thaum_golem", aspects(Aspect.AER, 2, Aspect.TERRA, 2, Aspect.PRAECANTATIO, 2));
    }

    private TC4EntityAspectRegistry() {
    }

    public static AspectList getAspectsForEntity(Entity entity) {
        if (entity == null) {
            return new AspectList();
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) {
            return new AspectList();
        }
        AspectList aspects = EXACT.get(id.toString().toLowerCase(Locale.ROOT));
        return aspects == null ? new AspectList() : copyOf(aspects);
    }

    public static int exactRuntimeEntries() {
        return EXACT.size();
    }

    private static void exact(String id, AspectList aspects) {
        EXACT.put(id, aspects);
    }

    private static AspectList aspects(Object... data) {
        AspectList list = new AspectList();
        for (int i = 0; i + 1 < data.length; i += 2) {
            list.add((Aspect) data[i], (Integer) data[i + 1]);
        }
        return list;
    }

    private static AspectList copyOf(AspectList source) {
        AspectList copy = new AspectList();
        copy.addAll(source);
        return copy;
    }
}
