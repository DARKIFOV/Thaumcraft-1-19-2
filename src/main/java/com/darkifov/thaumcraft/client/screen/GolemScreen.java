package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.menu.GolemMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Stage201 GuiGolem parity adapter using the original guigolem.png. */
public class GolemScreen extends AbstractContainerScreen<GolemMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/thaumcraft_core_original/guigolem.png");
    private static final int[] TC4_COLORS = new int[]{
            0x111111, 0xCC3333, 0x3366CC, 0x33AA33, 0xAA6633, 0x9933CC, 0x33AAAA, 0xCCCCCC,
            0x666666, 0xFF6666, 0x6699FF, 0x66FF66, 0xFFCC66, 0xCC66FF, 0x66FFFF, 0xFFFFFF
    };

    public GolemScreen(GolemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        ThaumGolemEntity golem = menu.golem();
        int totalSlots = menu.totalGolemSlots();
        int visible = menu.visibleGolemSlots();
        int typeLoc = golem == null ? 0 : golem.getGolemMaterial().ordinal() * 24;
        for (int a = 0; a < visible; a++) {
            int x = leftPos + 96 + a / 2 * 28;
            int y = topPos + 12 + a % 2 * 31;
            blit(poseStack, x, y, 184, typeLoc, 24, 24);
            if (golem != null && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ORDER) > 0) {
                int color = menu.golemColor(a + menu.currentScroll() * 6);
                blit(poseStack, leftPos + 96 + a / 2 * 28, topPos + 4 + a % 2 * 31, 72, 168, 24, 12);
                if (color >= 0 && color < TC4_COLORS.length) {
                    fill(poseStack, leftPos + 105 + a / 2 * 28, topPos + 7 + a % 2 * 31,
                            leftPos + 111 + a / 2 * 28, topPos + 13 + a % 2 * 31, 0xFF000000 | TC4_COLORS[color]);
                }
            }
        }
        if (totalSlots > 6) {
            blit(poseStack, leftPos + 111, topPos + 68, menu.currentScroll() > 0 ? 0 : 0, menu.currentScroll() > 0 ? 200 : 208, 24, 8);
            blit(poseStack, leftPos + 135, topPos + 68, menu.currentScroll() < menu.maxScroll() ? 24 : 24, menu.currentScroll() < menu.maxScroll() ? 200 : 208, 24, 8);
        }
        // Original GuiGolem toggle slots use texture region 8,168/176. Draw the four guard toggles and two use/fill toggles as adapters.
        if (golem != null && golem.getCoreType().originalId() == 4
                && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ORDER) > 0) {
            for (int i = 0; i < 4; i++) {
                blit(poseStack, leftPos + 104, topPos + 5 + i * 16, 8, 168, 8, 8);
                if (menu.golemToggle(i + 1) == 0) {
                    blit(poseStack, leftPos + 104, topPos + 5 + i * 16, 8, 176, 8, 8);
                }
            }
        }
        if (golem != null && golem.getCoreType().originalId() == 0) {
            drawToggle(poseStack, 62, 54, menu.golemToggle(0) == 0);
        }
        if (golem != null && golem.getCoreType().originalId() == 8) {
            for (int i = 0; i < 3; i++) {
                drawToggle(poseStack, 42, 40 + i * 10, menu.golemToggle(i) == 0);
            }
        }
        if (golem != null && golem.getCoreType().canSort()
                && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ENTROPY) > 0) {
            int shiftX = golem.getCoreType().originalId() == 10 ? 66 : 180;
            int shiftY = golem.getCoreType().originalId() == 10 ? 12 : 0;
            for (int i = 0; i < 3; i++) {
                drawToggle(poseStack, shiftX, 24 + shiftY + i * 10, menu.golemToggle(5 + i) != 0);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        ThaumGolemEntity golem = menu.golem();
        if (golem != null) {
            font.draw(poseStack, golem.getCoreType().id() + " / " + golem.getGolemMaterial().id(), 40, 11, 14540253);
            if (menu.maxScroll() > 0) {
                font.draw(poseStack, (menu.currentScroll() + 1) + "/" + (menu.maxScroll() + 1), 161, 70, 14540253);
            }
            if (golem.getCoreType().originalId() == 4
                    && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ORDER) > 0) {
                font.draw(poseStack, "Monsters", 122, 6, 0xFFD0CC);
                font.draw(poseStack, "Animals", 122, 22, 0xFFFFCC);
                font.draw(poseStack, "Players", 122, 38, 0xCCCCFF);
                font.draw(poseStack, "Creepers", 122, 54, 0xCCFFCC);
            } else if (golem.getCoreType().originalId() == 0) {
                font.draw(poseStack, menu.golemToggle(0) == 0 ? "Precise amount" : "Any amount", 36, 47, 0xFDFDFD);
            } else if (golem.getCoreType().originalId() == 8) {
                font.draw(poseStack, menu.golemToggle(0) == 0 ? "Block" : "Empty space", 53, 42, 0xFDFDFD);
                font.draw(poseStack, menu.golemToggle(1) == 0 ? "Right click" : "Left click", 53, 52, 0xFDFDFD);
                font.draw(poseStack, menu.golemToggle(2) == 0 ? "Not sneaking" : "Sneaking", 53, 62, 0xFDFDFD);
            }
            if (golem.getCoreType().originalId() == 10
                    && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ENTROPY) > 0) {
                font.draw(poseStack, "Use tags", 76, 38, menu.golemToggle(5) != 0 ? 0xFDFDFD : 0x666666);
                font.draw(poseStack, "Ignore damage", 76, 48, menu.golemToggle(6) != 0 ? 0xFDFDFD : 0x666666);
                font.draw(poseStack, "Ignore NBT", 76, 58, menu.golemToggle(7) != 0 ? 0xFDFDFD : 0x666666);
            }
        }
        font.draw(poseStack, playerInventoryTitle, 8, inventoryLabelY, 4210752);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - leftPos;
        int y = (int) mouseY - topPos;
        if (x >= 111 && x < 135 && y >= 68 && y < 76) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 66);
            return true;
        }
        if (x >= 135 && x < 159 && y >= 68 && y < 76) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 67);
            return true;
        }
        for (int a = 0; a < menu.visibleGolemSlots(); a++) {
            int sx = 96 + a / 2 * 28;
            int sy = 4 + a % 2 * 31;
            if (x >= sx && x < sx + 24 && y >= sy && y < sy + 12) {
                int slot = a + menu.currentScroll() * 6;
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button == 1 ? slot + menu.totalGolemSlots() : slot);
                return true;
            }
        }
        ThaumGolemEntity golem = menu.golem();
        if (golem != null) {
            int core = golem.getCoreType().originalId();
            if (core == 4 && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ORDER) > 0) {
                for (int i = 0; i < 4; i++) {
                    int sy = 5 + i * 16;
                    if (x >= 104 && x < 112 && y >= sy && y < sy + 8) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 51 + i);
                        return true;
                    }
                }
            } else if (core == 0 && x >= 62 && x < 70 && y >= 54 && y < 62) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 50);
                return true;
            } else if (core == 8 && x >= 42 && x < 50) {
                for (int i = 0; i < 3; i++) {
                    int sy = 40 + i * 10;
                    if (y >= sy && y < sy + 8) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 50 + i);
                        return true;
                    }
                }
            } else if (core == 10
                    && golem.getUpgradeAmount(com.darkifov.thaumcraft.golem.GolemUpgradeType.ENTROPY) > 0
                    && x >= 66 && x < 74) {
                for (int i = 0; i < 3; i++) {
                    int sy = 36 + i * 10;
                    if (y >= sy && y < sy + 8) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 55 + i);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawToggle(PoseStack poseStack, int x, int y, boolean enabled) {
        blit(poseStack, leftPos + x, topPos + y, 8, 168, 8, 8);
        if (enabled) {
            blit(poseStack, leftPos + x, topPos + y, 8, 176, 8, 8);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
