
package com.darkifov.thaumcraft.client.screen;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.OsmoticEnchantmentHelper;
import com.darkifov.thaumcraft.menu.OsmoticEnchanterMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OsmoticEnchanterScreen extends AbstractContainerScreen<OsmoticEnchanterMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/osmotic_enchanter.png");

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
    @Override public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) { renderBackground(poseStack); super.render(poseStack, mouseX, mouseY, partialTick); renderTooltip(poseStack, mouseX, mouseY); }
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = leftPos + (imageWidth - 256) / 2;
        int y = topPos + (imageHeight - 256) / 2;
        OriginalGuiTextures.blitOriginal(poseStack, x, y, OriginalGuiTextures.OSMOTIC_ENCHANTER, 256, 256);
    }

}
