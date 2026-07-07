package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Stage179 client keybind adapter for original TC4 KeyHandler.miscWandToggle; original TC4 KeyHandler keyG: "Misc Wand Toggle" (G / keycode 34). Stage188 also preserves keyF "Change Wand Focus" (F / keycode 33). */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientWandArchitectKeybinds {
    public static final KeyMapping KEY_CHANGE_WAND_FOCUS = new KeyMapping(
            "key.thaumcraft.change_wand_focus",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.thaumcraft"
    );

    public static final KeyMapping KEY_MISC_WAND_TOGGLE = new KeyMapping(
            "key.thaumcraft.misc_wand_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.thaumcraft"
    );

    private ClientWandArchitectKeybinds() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_CHANGE_WAND_FOCUS);
        event.register(KEY_MISC_WAND_TOGGLE);
    }
}
