
package com.darkifov.thaumcraft.client.screen;
import com.darkifov.thaumcraft.block.OsmoticEnchantmentHelper;
import com.darkifov.thaumcraft.menu.OsmoticEnchanterMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OsmoticEnchanterScreen extends AbstractContainerScreen<OsmoticEnchanterMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, \"textures/gui/osmotic_enchanter.png\");

    public OsmoticEnchanterScreen(OsmoticEnchanterMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); 
        this.imageWidth = 256;
        this.imageHeight = 256;imageWidth = 176; imageHeight = 204; inventoryLabelY = 108; }
    @Override protected void init() {
        super.init(); int y = topPos + 34;
        addButtonFor(OsmoticEnchantmentHelper.Choice.UNBREAKING, leftPos + 10, y);
        addButtonFor(OsmoticEnchantmentHelper.Choice.EFFICIENCY, leftPos + 92, y);
        addButtonFor(OsmoticEnchantmentHelper.Choice.SHARPNESS, leftPos + 10, y + 22);
        addButtonFor(OsmoticEnchantmentHelper.Choice.PROTECTION, leftPos + 92, y + 22);
        addButtonFor(OsmoticEnchantmentHelper.Choice.POWER, leftPos + 10, y + 44);
        addButtonFor(OsmoticEnchantmentHelper.Choice.FORTUNE, leftPos + 92, y + 44);
        addRenderableWidget(new Button(leftPos + 36, topPos + 100, 104, 18, Component.literal("Проверить структуру"), b -> ThaumcraftNetwork.requestOsmoticStructureCheck(menu.pos())));
    }
    private void addButtonFor(OsmoticEnchantmentHelper.Choice choice, int x, int y) { addRenderableWidget(new Button(x, y, 74, 18, OsmoticEnchantmentHelper.choiceName(choice), b -> ThaumcraftNetwork.requestOsmoticEnchant(menu.pos(), choice.ordinal()))); }
    @Override protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ORIGINAL_TEXTURE);
        // Stage91: old generic stretched GUI blit removed; strict original blit is used below.
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE100F22);
        fill(poseStack, leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF2C1B3E);
        fill(poseStack, leftPos + 8, topPos + 18, leftPos + imageWidth - 8, topPos + 96, 0x885E3EA8);
        font.draw(poseStack, Component.literal("Osmotic Enchanter").withStyle(ChatFormatting.LIGHT_PURPLE), leftPos + 12, topPos + 9, 0xF2DFB2);
        font.draw(poseStack, "Предмет держи в основной руке.", leftPos + 12, topPos + 22, 0xE8D4A7);
        font.draw(poseStack, "Нужны vis в жезле и XP.", leftPos + 12, topPos + 88, 0xCFEAFF);
    }
    @Override public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) { renderBackground(poseStack); super.render(poseStack, mouseX, mouseY, partialTick); renderTooltip(poseStack, mouseX, mouseY); }
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = leftPos + (imageWidth - 256) / 2;
        int y = topPos + (imageHeight - 256) / 2;
        OriginalGuiTextures.blitOriginal(poseStack, x, y, OriginalGuiTextures.OSMOTIC_ENCHANTER, 256, 256);
    }

}
