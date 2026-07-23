package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.client.screen.ResearchNoteScreen;
import com.darkifov.thaumcraft.client.screen.TC4ThaumonomiconPageHistory;
import com.darkifov.thaumcraft.client.screen.ThaumonomiconScreen;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.research.TC4ThaumonomiconParity;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {}

    public static void openThaumonomicon() {
        Minecraft minecraft = Minecraft.getInstance();
        TC4ThaumonomiconPageHistory.clear();
        if (minecraft.player != null) {
            minecraft.player.playSound(TC4Sounds.event("page"),
                    TC4ThaumonomiconParity.OPEN_PAGE_VOLUME, TC4ThaumonomiconParity.GUI_SOUND_PITCH);
        }
        minecraft.setScreen(new ThaumonomiconScreen());
    }

    public static void openResearchNote() {
        Minecraft.getInstance().setScreen(new ResearchNoteScreen());
    }
}
