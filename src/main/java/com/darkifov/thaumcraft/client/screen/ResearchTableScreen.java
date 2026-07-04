package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ResearchTableScreen extends Screen {
    private int page = 0;

    public ResearchTableScreen() {
        super(Component.literal("Research Table"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int bottom = height / 2 + 78;

        addRenderableWidget(new Button(centerX - 104, bottom, 72, 20, Component.literal("< Back"), button -> {
            page = Math.max(0, page - 1);
        }));

        addRenderableWidget(new Button(centerX + 32, bottom, 72, 20, Component.literal("Next >"), button -> {
            page = Math.min(ResearchRegistry.size() - 1, page + 1);
        }));

        addRenderableWidget(new Button(centerX - 45, bottom + 26, 90, 20, Component.literal("Research"), button -> {
            ThaumcraftNetwork.requestUnlockFromClient();
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        int panelX = width / 2 - 130;
        int panelY = height / 2 - 100;

        fill(poseStack, panelX, panelY, panelX + 260, panelY + 200, 0xCC201727);
        fill(poseStack, panelX + 4, panelY + 4, panelX + 256, panelY + 196, 0xCCEFE1BD);

        ResearchEntry entry = ResearchRegistry.get(page);
        boolean unlocked = ClientResearchData.hasResearch(entry.key());

        drawCenteredString(poseStack, font, Component.literal("Research Table").withStyle(ChatFormatting.GOLD), width / 2, panelY + 12, 0xE8A54F);

        font.draw(poseStack, "Entry " + (page + 1) + "/" + ResearchRegistry.size(), panelX + 18, panelY + 34, 0x4A2A11);
        font.draw(poseStack, unlocked ? "UNLOCKED" : "LOCKED", panelX + 178, panelY + 34, unlocked ? 0x247A2E : 0x9A2222);

        font.draw(poseStack, Component.literal(entry.title()).withStyle(unlocked ? ChatFormatting.DARK_AQUA : ChatFormatting.DARK_RED), panelX + 18, panelY + 56, unlocked ? 0x245A6E : 0x7A1F1F);

        font.drawWordWrap(Component.literal(entry.description()), panelX + 18, panelY + 76, 220, 0x3D2B1F);

        int y = panelY + 124;
        font.draw(poseStack, Component.literal("Requirements:").withStyle(ChatFormatting.DARK_PURPLE), panelX + 18, y, 0x5A2D75);
        y += 12;

        if (entry.requirements().length == 0) {
            font.draw(poseStack, "- none", panelX + 28, y, 0x555555);
        } else {
            for (String req : entry.requirements()) {
                boolean reqUnlocked = ClientResearchData.hasResearch(req);
                font.draw(poseStack, "- " + req + (reqUnlocked ? " ✓" : " ✗"), panelX + 28, y, reqUnlocked ? 0x247A2E : 0x9A2222);
                y += 11;
            }
        }

        font.draw(poseStack, "Unlocked: " + ClientResearchData.research().size() + " | Warp: " + ClientResearchData.warp(), panelX + 18, panelY + 174, 0x5A4632);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
