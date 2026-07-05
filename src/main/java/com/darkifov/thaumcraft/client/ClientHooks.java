package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.client.screen.ArcaneWorkbenchScreen;
import com.darkifov.thaumcraft.client.screen.ResearchTableScreen;
import com.darkifov.thaumcraft.client.screen.ThaumonomiconScreen;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openThaumonomicon() {
        Minecraft.getInstance().setScreen(new ThaumonomiconScreen());
    }

    public static void openResearchTable() {
        Minecraft.getInstance().setScreen(new ResearchTableScreen());
    }

    public static void openArcaneWorkbench() {
        Minecraft.getInstance().setScreen(new ArcaneWorkbenchScreen());
    }
}
