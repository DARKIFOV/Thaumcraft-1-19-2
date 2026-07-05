package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaCellItem;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.menu.TransvectorInterfaceMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TransvectorInterfaceBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_BOUND = "Bound";
    private static final String TAG_X = "TargetX";
    private static final String TAG_Y = "TargetY";
    private static final String TAG_Z = "TargetZ";

    public static final int ACTION_STATUS = 0;
    public static final int ACTION_INSPECT = 1;
    public static final int ACTION_PULL_JAR_TO_CELL = 2;
    public static final int ACTION_PUSH_CELL_TO_JAR = 3;
    public static final int ACTION_PULL_DRIVE_TO_CELL = 4;
    public static final int ACTION_PUSH_CELL_TO_DRIVE = 5;
    public static final int ACTION_DEEP_STATUS = 6;

    private boolean bound;
    private BlockPos targetPos = BlockPos.ZERO;

    public TransvectorInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.TRANSVECTOR_INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Transvector Interface");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TransvectorInterfaceMenu(containerId, inventory, this.worldPosition);
    }

    public boolean isBound() {
        return bound;
    }

    public BlockPos targetPos() {
        return targetPos;
    }

    public void bind(BlockPos pos) {
        this.bound = true;
        this.targetPos = pos;
        setChangedAndSync();
    }

    public void clearTarget() {
        this.bound = false;
        this.targetPos = BlockPos.ZERO;
        setChangedAndSync();
    }

    public void performAction(Player player, int action) {
        if (action == ACTION_STATUS) {
            sendStatus(player);
            return;
        }

        if (action == ACTION_INSPECT) {
            inspectTarget(player);
            return;
        }

        if (action == ACTION_DEEP_STATUS) {
            deepStatus(player);
            return;
        }

        if (!consumeRemoteVis(player, 4)) {
            return;
        }

        if (action == ACTION_PULL_JAR_TO_CELL) {
            pullJarToHeldCell(player);
            return;
        }

        if (action == ACTION_PUSH_CELL_TO_JAR) {
            pushHeldCellToJar(player);
            return;
        }

        if (action == ACTION_PULL_DRIVE_TO_CELL) {
            pullDriveToHeldCell(player);
            return;
        }

        if (action == ACTION_PUSH_CELL_TO_DRIVE) {
            pushHeldCellToDrive(player);
        }
    }

    private boolean consumeRemoteVis(Player player, int amount) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        if (!WandItem.consumeVisFromInventory(player, Aspect.PRAECANTATIO, amount)) {
            player.displayClientMessage(Component.literal("Transvector action requires " + amount + " Praecantatio vis in a wand.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        return true;
    }

    private BlockEntity targetBlockEntity(Player player) {
        if (!bound) {
            player.displayClientMessage(Component.literal("Transvector Interface не привязан к цели.").withStyle(ChatFormatting.RED), false);
            return null;
        }

        if (level == null || !level.isLoaded(targetPos)) {
            player.displayClientMessage(Component.literal("Цель не загружена в мире.").withStyle(ChatFormatting.RED), false);
            return null;
        }

        return level.getBlockEntity(targetPos);
    }

    public void sendStatus(Player player) {
        if (!bound) {
            player.displayClientMessage(Component.literal("Transvector Interface не привязан к цели.").withStyle(ChatFormatting.RED), false);
            return;
        }

        player.displayClientMessage(Component.literal("Transvector Interface target: " + targetPos.toShortString()).withStyle(ChatFormatting.LIGHT_PURPLE), false);

        if (level == null || !level.isLoaded(targetPos)) {
            player.displayClientMessage(Component.literal("Цель не загружена в мире.").withStyle(ChatFormatting.RED), false);
            return;
        }

        BlockState targetState = level.getBlockState(targetPos);
        player.displayClientMessage(Component.literal("Target block: " + targetState.getBlock().getName().getString()).withStyle(ChatFormatting.AQUA), false);
        player.displayClientMessage(Component.literal("Remote actions cost: 4 Praecantatio vis.").withStyle(ChatFormatting.GRAY), false);
    }

    public void inspectTarget(Player player) {
        BlockEntity blockEntity = targetBlockEntity(player);

        if (blockEntity == null) {
            return;
        }

        BlockState targetState = level.getBlockState(targetPos);
        player.displayClientMessage(Component.literal("Remote inspect: " + targetState.getBlock().getName().getString()).withStyle(ChatFormatting.LIGHT_PURPLE), false);

        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            Aspect aspect = jar.aspects().firstAspect();
            int total = jar.aspects().totalAmount();

            if (aspect == null || total <= 0) {
                player.displayClientMessage(Component.literal("Target jar is empty.").withStyle(ChatFormatting.GRAY), false);
            } else {
                player.displayClientMessage(Component.literal("Jar essentia: ")
                        .append(Component.literal(aspect.displayName()).withStyle(aspect.color()))
                        .append(Component.literal(" x" + total)), false);
            }

            return;
        }

        if (blockEntity instanceof EssentiaDriveBlockEntity drive) {
            drive.sendStatus(player);
            return;
        }

        if (blockEntity instanceof Container container) {
            int filled = 0;

            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    filled++;
                }
            }

            player.displayClientMessage(Component.literal("Remote inventory: " + filled + "/" + container.getContainerSize() + " slots filled.").withStyle(ChatFormatting.AQUA), false);
            return;
        }

        if (blockEntity instanceof MenuProvider provider) {
            player.displayClientMessage(Component.literal("Target has menu provider: " + provider.getDisplayName().getString()).withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Full remote opening is still disabled; Stage 57 adds targeted safe actions instead.").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        player.displayClientMessage(Component.literal("Target block entity: " + blockEntity.getClass().getSimpleName()).withStyle(ChatFormatting.AQUA), false);
    }

    public void deepStatus(Player player) {
        BlockEntity blockEntity = targetBlockEntity(player);

        if (blockEntity == null) {
            return;
        }

        if (blockEntity instanceof ArcaneWorkbenchBlockEntity workbench) {
            int filled = 0;

            for (int i = 0; i < workbench.getContainerSize(); i++) {
                if (!workbench.getItem(i).isEmpty()) {
                    filled++;
                }
            }

            player.displayClientMessage(Component.literal("Remote Arcane Workbench: " + filled + "/" + workbench.getContainerSize() + " slots filled.").withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Wand slot: " + workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND).getHoverName().getString()).withStyle(ChatFormatting.GRAY), false);
            player.displayClientMessage(Component.literal("Output slot: " + workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_OUTPUT).getHoverName().getString()).withStyle(ChatFormatting.GRAY), false);
            return;
        }

        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            player.displayClientMessage(Component.literal("Remote Crucible | water: " + crucible.hasWater() + " | flux: " + crucible.flux() + " | essentia: " + crucible.aspects().totalAmount()).withStyle(ChatFormatting.AQUA), false);
            Aspect aspect = crucible.aspects().firstAspect();

            if (aspect != null) {
                player.displayClientMessage(Component.literal("First aspect: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())), false);
            }

            return;
        }

        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
            player.displayClientMessage(Component.literal("Remote Furnace | active: " + furnace.active() + " | fuel: " + furnace.fuelTime() + " | stored: " + furnace.aspects().totalAmount() + " | pending: " + furnace.pendingAspects().totalAmount()).withStyle(ChatFormatting.AQUA), false);
            Aspect aspect = furnace.firstAspect();

            if (aspect != null) {
                player.displayClientMessage(Component.literal("First stored aspect: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())), false);
            }

            return;
        }

        if (blockEntity instanceof InfusionMatrixBlockEntity matrix) {
            player.displayClientMessage(Component.literal("Remote Infusion Matrix | active: " + matrix.active() + " | progress: " + matrix.progress() + "/" + matrix.duration()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(matrix.statusComponent(), false);
            return;
        }

        inspectTarget(player);
    }

    private ItemStack heldCell(Player player) {
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof EssentiaCellItem)) {
            player.displayClientMessage(Component.literal("Нужна Digital Essentia Cell в основной руке.").withStyle(ChatFormatting.RED), false);
            return ItemStack.EMPTY;
        }

        return held;
    }

    private void pullJarToHeldCell(Player player) {
        BlockEntity blockEntity = targetBlockEntity(player);
        ItemStack cell = heldCell(player);

        if (cell.isEmpty()) {
            return;
        }

        if (!(blockEntity instanceof EssentiaJarBlockEntity jar)) {
            player.displayClientMessage(Component.literal("Target is not an Essentia Jar.").withStyle(ChatFormatting.RED), false);
            return;
        }

        int moved = EssentiaCellItem.transferFromJar(cell, jar, 64);
        player.displayClientMessage(Component.literal("Remote pull jar → cell: " + moved + " essentia.").withStyle(moved > 0 ? ChatFormatting.AQUA : ChatFormatting.RED), false);
    }

    private void pushHeldCellToJar(Player player) {
        BlockEntity blockEntity = targetBlockEntity(player);
        ItemStack cell = heldCell(player);

        if (cell.isEmpty()) {
            return;
        }

        if (!(blockEntity instanceof EssentiaJarBlockEntity jar)) {
            player.displayClientMessage(Component.literal("Target is not an Essentia Jar.").withStyle(ChatFormatting.RED), false);
            return;
        }

        int moved = EssentiaCellItem.transferToJar(cell, jar, 64);
        player.displayClientMessage(Component.literal("Remote push cell → jar: " + moved + " essentia.").withStyle(moved > 0 ? ChatFormatting.AQUA : ChatFormatting.RED), false);
    }

    private void pullDriveToHeldCell(Player player) {
        BlockEntity blockEntity = targetBlockEntity(player);
        ItemStack cell = heldCell(player);

        if (cell.isEmpty()) {
            return;
        }

        if (!(blockEntity instanceof EssentiaDriveBlockEntity drive)) {
            player.displayClientMessage(Component.literal("Target is not an Essentia Drive.").withStyle(ChatFormatting.RED), false);
            return;
        }

        EssentiaDriveBlockEntity.MovedEssentia pulled = drive.extractAnyEssentia(64);

        if (pulled.amount <= 0 || pulled.aspect == null) {
            player.displayClientMessage(Component.literal("Drive has no essentia to pull.").withStyle(ChatFormatting.RED), false);
            return;
        }

        int inserted = insertIntoCell(cell, pulled.aspect, pulled.amount);

        if (inserted < pulled.amount) {
            drive.insertEssentia(pulled.aspect, pulled.amount - inserted);
        }

        player.displayClientMessage(Component.literal("Remote pull drive → cell: " + inserted + " essentia.").withStyle(inserted > 0 ? ChatFormatting.AQUA : ChatFormatting.RED), false);
    }

    private void pushHeldCellToDrive(Player player) {
        BlockEntity blockEntity = targetBlockEntity(player);
        ItemStack cell = heldCell(player);

        if (cell.isEmpty()) {
            return;
        }

        if (!(blockEntity instanceof EssentiaDriveBlockEntity drive)) {
            player.displayClientMessage(Component.literal("Target is not an Essentia Drive.").withStyle(ChatFormatting.RED), false);
            return;
        }

        Aspect aspect = EssentiaCellItem.getAspect(cell);
        int amount = EssentiaCellItem.getAmount(cell);

        if (aspect == null || amount <= 0) {
            player.displayClientMessage(Component.literal("Held cell is empty.").withStyle(ChatFormatting.RED), false);
            return;
        }

        int inserted = drive.insertEssentia(aspect, Math.min(64, amount));

        if (inserted > 0) {
            EssentiaCellItem.setEssentia(cell, aspect, amount - inserted);
        }

        player.displayClientMessage(Component.literal("Remote push cell → drive: " + inserted + " essentia.").withStyle(inserted > 0 ? ChatFormatting.AQUA : ChatFormatting.RED), false);
    }

    private int insertIntoCell(ItemStack cell, Aspect aspect, int amount) {
        if (!(cell.getItem() instanceof EssentiaCellItem cellItem) || aspect == null || amount <= 0) {
            return 0;
        }

        Aspect partition = EssentiaCellItem.getPartitionAspect(cell);

        if (partition != null && partition != aspect) {
            return 0;
        }

        Aspect currentAspect = EssentiaCellItem.getAspect(cell);
        int current = EssentiaCellItem.getAmount(cell);

        if (currentAspect != null && currentAspect != aspect) {
            return 0;
        }

        int space = Math.max(0, cellItem.capacity() - current);
        int inserted = Math.min(space, amount);

        if (inserted > 0) {
            EssentiaCellItem.setEssentia(cell, aspect, current + inserted);
        }

        return inserted;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_BOUND, bound);
        tag.putInt(TAG_X, targetPos.getX());
        tag.putInt(TAG_Y, targetPos.getY());
        tag.putInt(TAG_Z, targetPos.getZ());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bound = tag.getBoolean(TAG_BOUND);
        targetPos = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
