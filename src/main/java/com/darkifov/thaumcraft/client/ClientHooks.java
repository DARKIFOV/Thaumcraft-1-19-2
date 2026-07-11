package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.client.screen.ResearchNoteScreen;
import com.darkifov.thaumcraft.client.screen.ThaumonomiconScreen;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openThaumonomicon() {
        Minecraft.getInstance().setScreen(new ThaumonomiconScreen());
    }


    public static void openResearchNote() {
        Minecraft.getInstance().setScreen(new ResearchNoteScreen());
    }
}
