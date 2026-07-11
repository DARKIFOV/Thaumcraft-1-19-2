package com.darkifov.thaumcraft.porting;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage124: original TC4 sound-event registry mirror.
 *
 * The .ogg files and sounds.json are the original TC4 asset set. Before this
 * stage the assets were present but most runtime code still used vanilla
 * placeholder sounds. This registry makes every TC4 sound key addressable from
 * Forge 1.19.2 code while keeping the original sound names intact.
 */
public final class TC4Sounds {
    private TC4Sounds() {}

    public static final List<String> ORIGINAL_SOUND_KEYS = List.of(
            "alembicknock",
            "brain",
            "bubble",
            "cameraclack",
            "cameraticks",
            "chant",
            "coins",
            "crabclaw",
            "crabdeath",
            "crabtalk",
            "craftfail",
            "craftstart",
            "creak",
            "crystal",
            "doorfail",
            "egattack",
            "egdeath",
            "egidle",
            "egscreech",
            "erase",
            "evilportal",
            "fireloop",
            "fly",
            "golemironshoot",
            "gore",
            "heartbeat",
            "hhoff",
            "hhon",
            "ice",
            "infuser",
            "infuserstart",
            "jacobs",
            "jar",
            "key",
            "learn",
            "monolith",
            "page",
            "pech_charge",
            "pech_death",
            "pech_dice",
            "pech_hit",
            "pech_idle",
            "pech_trade",
            "pump",
            "roots",
            "rumble",
            "runicShieldCharge",
            "runicShieldEffect",
            "shock",
            "spill",
            "squeek",
            "swarm",
            "swarmattack",
            "swing",
            "tentacle",
            "tool",
            "upgrade",
            "urnbreak",
            "wand",
            "wandfail",
            "whispers",
            "wind",
            "wispdead",
            "wisplive",
            "write",
            "zap"
    );

    private static final Map<String, RegistryObject<SoundEvent>> REGISTERED = new LinkedHashMap<>();

    /**
     * Minecraft 1.19 resource identifiers must be lower-case.  TC4 shipped two
     * camel-case sound keys, so keep those legacy keys as lookup aliases while
     * registering modern, valid paths.
     */
    private static String registryPath(String legacyKey) {
        return switch (legacyKey) {
            case "runicShieldCharge" -> "runic_shield_charge";
            case "runicShieldEffect" -> "runic_shield_effect";
            default -> legacyKey;
        };
    }

    public static Map<String, RegistryObject<SoundEvent>> registerAll(DeferredRegister<SoundEvent> register) {
        if (!REGISTERED.isEmpty()) {
            return Collections.unmodifiableMap(REGISTERED);
        }
        for (String key : ORIGINAL_SOUND_KEYS) {
            String path = registryPath(key);
            ResourceLocation id = new ResourceLocation(ThaumcraftMod.MOD_ID, path);
            REGISTERED.put(key, register.register(path, () -> new SoundEvent(id)));
        }
        return Collections.unmodifiableMap(REGISTERED);
    }

    public static SoundEvent event(String key) {
        RegistryObject<SoundEvent> event = REGISTERED.get(key);
        if (event == null) {
            return SoundEvents.NOTE_BLOCK_BELL;
        }
        return event.get();
    }

    public static boolean has(String key) {
        return REGISTERED.containsKey(key) || ORIGINAL_SOUND_KEYS.contains(key);
    }

    public static int count() {
        return ORIGINAL_SOUND_KEYS.size();
    }
}
