package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.blockentity.FocalManipulatorBlockEntity;
import com.darkifov.thaumcraft.menu.FocalManipulatorMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.wand.FocusUpgradeRuntime;
import com.darkifov.thaumcraft.wand.FocusUpgradeType;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GuiFocalManipulator parity port.
 *
 * Uses the original 192x233 gui_wandtable texture, exact slot/icon/progress
 * coordinates, original upgrade icon textures and the original hover regions.
 */
public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
    private static final ResourceLocation GUI =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/gui_wandtable.png");

    private int selected = -1;
    private int rememberedRank = -1;

    public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 192;
        imageHeight = 233;
        inventoryLabelX = 16;
        inventoryLabelY = 139;
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int rank = nextRank();
        if (rank != rememberedRank) {
            selected = menu.isCrafting() ? menu.activeUpgrade() : -1;
            rememberedRank = rank;
        }
        if (menu.isCrafting()) selected = menu.activeUpgrade();
        validateSelectedUpgrade(rank);

        ItemStack focus = menu.focusStack();
        if (!menu.isCrafting() && rank > 0 && focus.getItem() instanceof WandFocusItem focusItem) {
            List<FocusUpgradeType> possible = possible(focus, focusItem.focusType(), rank);
            for (int i = 0; i < possible.size(); i++) {
                if (selected == possible.get(i).id()) {
                    RenderSystem.setShaderTexture(0, GUI);
                    blit(pose, leftPos + 48 + i * 16, topPos + 104, 200, 0, 16, 16);
                }
            }
        }

        if (rank > 0 && selected >= 0 && !focus.isEmpty()) {
            RenderSystem.setShaderTexture(0, GUI);
            blit(pose, leftPos + 108, topPos + 59, 200, 16, 16, 16);
        }
        if (!menu.isCrafting() && selected >= 0 && canAffordXp(rank)) {
            RenderSystem.setShaderTexture(0, GUI);
            blit(pose, leftPos + 48, topPos + 88, 8, 240, 96, 8);
        }
        if (menu.isCrafting() && menu.initialSize() > 0) {
            renderAspectProgress(pose);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        int rank = nextRank();
        if (rank > 0 && !menu.focusStack().isEmpty()) {
            int xp = rank * FocalManipulatorBlockEntity.XP_MULT;
            int color = canAffordXp(rank) ? 10092429 : 16151160;
            font.draw(pose, Integer.toString(xp), 125, 64, color);
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        renderUpgradeIcons(pose);
        renderAspectCosts(pose);
        renderTooltip(pose, mouseX, mouseY);
        renderUpgradeTooltips(pose, mouseX, mouseY);
        renderAspectCostTooltip(pose, mouseX, mouseY);
        renderOriginalRegionTooltips(pose, mouseX, mouseY);
    }

    private void renderUpgradeIcons(PoseStack pose) {
        ItemStack focus = menu.focusStack();
        if (!(focus.getItem() instanceof WandFocusItem focusItem)) return;

        short[] applied = FocusUpgradeRuntime.getAppliedUpgrades(focus);
        for (int i = 0; i < applied.length; i++) {
            FocusUpgradeType type = FocusUpgradeType.byId(applied[i]);
            if (type == null) continue;
            int x = leftPos + 56 + i * 16;
            int y = topPos + 32;
            drawUpgrade(pose, type, x, y);
        }

        int rank = nextRank();
        if (rank < 1 || menu.isCrafting()) return;
        List<FocusUpgradeType> possible = possible(focus, focusItem.focusType(), rank);
        for (int i = 0; i < possible.size(); i++) {
            int x = leftPos + 48 + i * 16;
            int y = topPos + 104;
            FocusUpgradeType type = possible.get(i);
            drawUpgrade(pose, type, x, y);
        }
    }

    private void renderAspectProgress(PoseStack pose) {
        int start = 0;
        int initial = Math.max(1, menu.initialSize());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, GUI);
        for (int i = 0; i < FocalManipulatorBlockEntity.PRIMALS.length; i++) {
            int amount = menu.remainingAspect(i);
            if (amount <= 0 || start >= 96) continue;
            int width = Math.max(1, (int) (amount / (float) initial * 96.0F));
            width = Math.min(width, 96 - start);
            Aspect aspect = FocalManipulatorBlockEntity.PRIMALS[i];
            int color = aspect.nativeColor();
            RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F, 0.9F);
            blit(pose, leftPos + 48 + start, topPos + 88, 112 + start, 240, width, 8);
            start += width;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderAspectCosts(PoseStack pose) {
        int rank = nextRank();
        FocusUpgradeType type = menu.isCrafting()
                ? FocusUpgradeType.byId((short) menu.activeUpgrade())
                : FocusUpgradeType.byId((short) selected);
        if (rank < 1 || type == null) return;

        com.darkifov.thaumcraft.AspectList cost = menu.isCrafting()
                ? remainingAspectList()
                : FocusUpgradeRuntime.primalVisCost(type, rank);
        int rows = countCostRows(cost);
        if (rows <= 0) return;

        pose.pushPose();
        pose.translate(leftPos + 49.0D, topPos + 68.0D - rows * 2.5D, 0.0D);
        pose.scale(0.5F, 0.5F, 1.0F);
        int row = 0;
        for (Aspect aspect : FocalManipulatorBlockEntity.PRIMALS) {
            int value = cost.get(aspect);
            if (value <= 0) continue;
            font.draw(pose, aspect.displayName(), 0.0F, row * 10.0F, aspect.nativeColor());
            font.draw(pose, formatCentivis(value), 48.0F, row * 10.0F, aspect.nativeColor());
            row++;
        }
        pose.popPose();
    }

    private com.darkifov.thaumcraft.AspectList remainingAspectList() {
        com.darkifov.thaumcraft.AspectList list = new com.darkifov.thaumcraft.AspectList();
        for (int i = 0; i < FocalManipulatorBlockEntity.PRIMALS.length; i++) {
            int value = menu.remainingAspect(i);
            if (value > 0) list.add(FocalManipulatorBlockEntity.PRIMALS[i], value);
        }
        return list;
    }

    private int countCostRows(com.darkifov.thaumcraft.AspectList cost) {
        int count = 0;
        for (Aspect aspect : FocalManipulatorBlockEntity.PRIMALS) {
            if (cost.get(aspect) > 0) count++;
        }
        return count;
    }

    private void validateSelectedUpgrade(int rank) {
        if (menu.isCrafting() || selected < 0) return;
        ItemStack focus = menu.focusStack();
        if (!(focus.getItem() instanceof WandFocusItem focusItem) || rank < 1) {
            selected = -1;
            return;
        }
        boolean valid = possible(focus, focusItem.focusType(), rank).stream()
                .anyMatch(type -> type.id() == selected);
        if (!valid) selected = -1;
    }

    private void renderUpgradeTooltips(PoseStack pose, int mouseX, int mouseY) {
        ItemStack focus = menu.focusStack();
        if (!(focus.getItem() instanceof WandFocusItem focusItem)) return;

        short[] applied = FocusUpgradeRuntime.getAppliedUpgrades(focus);
        for (int i = 0; i < applied.length; i++) {
            FocusUpgradeType type = FocusUpgradeType.byId(applied[i]);
            if (type == null) continue;
            int x = leftPos + 56 + i * 16;
            int y = topPos + 32;
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                renderComponentTooltip(pose, upgradeTooltip(type, false), mouseX, mouseY);
                return;
            }
        }

        int rank = nextRank();
        if (rank < 1 || menu.isCrafting()) return;
        List<FocusUpgradeType> possible = possible(focus, focusItem.focusType(), rank);
        for (int i = 0; i < possible.size(); i++) {
            int x = leftPos + 48 + i * 16;
            int y = topPos + 104;
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                renderComponentTooltip(pose, upgradeTooltip(possible.get(i), true), mouseX, mouseY);
                return;
            }
        }
    }

    private void renderAspectCostTooltip(PoseStack pose, int mouseX, int mouseY) {
        int rank = nextRank();
        FocusUpgradeType type = menu.isCrafting()
                ? FocusUpgradeType.byId((short) menu.activeUpgrade())
                : FocusUpgradeType.byId((short) selected);
        if (rank < 1 || type == null) return;
        com.darkifov.thaumcraft.AspectList cost = menu.isCrafting()
                ? remainingAspectList()
                : FocusUpgradeRuntime.primalVisCost(type, rank);
        int rows = countCostRows(cost);
        int y = topPos + 68 - (int) Math.ceil(rows * 2.5D);
        int row = 0;
        for (Aspect aspect : FocalManipulatorBlockEntity.PRIMALS) {
            int value = cost.get(aspect);
            if (value <= 0) continue;
            if (inside(mouseX, mouseY, leftPos + 49, y + row * 5, 38, 6)) {
                renderComponentTooltip(pose, List.of(Component.literal(
                        aspect.displayName() + " " + formatCentivis(value))
                        .withStyle(style -> style.withColor(aspect.textColor()))), mouseX, mouseY);
                return;
            }
            row++;
        }
    }

    private void renderOriginalRegionTooltips(PoseStack pose, int mouseX, int mouseY) {
        int rank = nextRank();
        if (selected < 0 || rank < 1 || isOverAspectCost(mouseX, mouseY)) return;
        if (inside(mouseX, mouseY, leftPos + 48, topPos + 48, 36, 36)) {
            renderComponentTooltip(pose, List.of(Component.translatable("wandtable.text1")), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + 108, topPos + 58, 36, 16)) {
            renderComponentTooltip(pose, List.of(Component.translatable("wandtable.text2")), mouseX, mouseY);
        } else if (!menu.isCrafting() && canAffordXp(rank)
                && inside(mouseX, mouseY, leftPos + 48, topPos + 88, 96, 8)) {
            renderComponentTooltip(pose, List.of(Component.translatable("wandtable.text3")), mouseX, mouseY);
        }
    }

    private boolean isOverAspectCost(int mouseX, int mouseY) {
        int rank = nextRank();
        FocusUpgradeType type = menu.isCrafting()
                ? FocusUpgradeType.byId((short) menu.activeUpgrade())
                : FocusUpgradeType.byId((short) selected);
        if (rank < 1 || type == null) return false;
        com.darkifov.thaumcraft.AspectList cost = menu.isCrafting()
                ? remainingAspectList()
                : FocusUpgradeRuntime.primalVisCost(type, rank);
        int rows = countCostRows(cost);
        int y = topPos + 68 - (int) Math.ceil(rows * 2.5D);
        return rows > 0 && inside(mouseX, mouseY, leftPos + 49, y, 38, rows * 5 + 1);
    }

    private void drawUpgrade(PoseStack pose, FocusUpgradeType type, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(type.icon()));
        blit(pose, x, y, 0, 0, 16, 16, 16, 16);
    }

    private List<Component> upgradeTooltip(FocusUpgradeType type, boolean selectable) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(type.nameKey())
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE));
        lines.add(Component.translatable(type.textKey()).withStyle(ChatFormatting.GRAY));
        if (selectable) lines.add(Component.translatable("wandtable.select").withStyle(ChatFormatting.GOLD));
        return lines;
    }

    private List<FocusUpgradeType> possible(ItemStack stack, WandFocusType focus, int rank) {
        List<FocusUpgradeType> out = new ArrayList<>();
        for (FocusUpgradeType type : FocusUpgradeRuntime.possibleUpgrades(focus, rank)) {
            if (FocusUpgradeRuntime.canApplyUpgrade(stack, focus, type, rank, minecraft.player)) out.add(type);
        }
        return out;
    }

    private int nextRank() {
        if (menu.isCrafting() && menu.activeRank() > 0) return menu.activeRank();
        return FocusUpgradeRuntime.nextOpenRank(menu.focusStack());
    }

    private boolean canAffordXp(int rank) {
        return rank > 0 && minecraft.player != null
                && minecraft.player.experienceLevel >= rank * FocalManipulatorBlockEntity.XP_MULT;
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static String formatCentivis(int centivis) {
        return String.format(Locale.ROOT, "%.1f", centivis / 100.0F);
    }

    private void playButtonClick() {
        if (minecraft.player != null) {
            minecraft.player.playSound(TC4Sounds.event("cameraclack"), 0.4F, 1.0F);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !menu.isCrafting()) {
            ItemStack focus = menu.focusStack();
            if (focus.getItem() instanceof WandFocusItem focusItem) {
                int rank = nextRank();
                List<FocusUpgradeType> possible = possible(focus, focusItem.focusType(), rank);
                for (int i = 0; i < possible.size(); i++) {
                    int x = leftPos + 48 + i * 16;
                    int y = topPos + 104;
                    if (inside(mouseX, mouseY, x, y, 16, 16)) {
                        int id = possible.get(i).id();
                        selected = selected == id ? -1 : id;
                        playButtonClick();
                        return true;
                    }
                }
            }
            if (selected >= 0 && canAffordXp(nextRank())
                    && inside(mouseX, mouseY, leftPos + 48, topPos + 88, 96, 8)) {
                if (minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selected);
                    playButtonClick();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
