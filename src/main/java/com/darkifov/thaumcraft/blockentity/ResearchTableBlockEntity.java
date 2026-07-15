package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import com.darkifov.thaumcraft.research.ResearchTableBonusRuntime;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Stage165: persistent 1.19.2 adapter for the original TC4 TileResearchTable.
 *
 * Original TC4 table inventory contract is kept exactly at the structural level:
 *   slot 0 = IScribeTools
 *   slot 1 = itemResearchNotes
 *
 * Modern Forge uses BlockEntity + Menu instead of the 1.7.10 TileEntity/Container
 * classes, but the data ownership is now table-local again rather than held-item
 * virtual slots.
 */
public class ResearchTableBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_SCRIBING_TOOLS = ResearchTableInventoryRuntime.SLOT_SCRIBING_TOOLS;
    public static final int SLOT_RESEARCH_NOTE = ResearchTableInventoryRuntime.SLOT_RESEARCH_NOTE;
    public static final int SIZE = 2;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final AspectList bonusAspects = new AspectList();
    private int nextRecalc = 0;

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.RESEARCH_TABLE_BLOCK_ENTITY.get(), pos, state);
    }


    public static void serverTick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity table) {
        if (level == null || level.isClientSide || table == null) {
            return;
        }
        if (table.nextRecalc++ > ResearchTableBonusRuntime.RECALCULATE_INTERVAL_TICKS) {
            table.nextRecalc = 0;
            if (ResearchTableBonusRuntime.recalculateInto(level, pos, table.bonusAspects)) {
                table.setChanged();
                table.syncToClient();
            }
        }
    }

    public AspectList bonusAspects() {
        return bonusAspects;
    }

    public int bonusAmount(Aspect aspect) {
        return bonusAspects.get(aspect);
    }

    public boolean hasBonusAspect(Aspect aspect) {
        return bonusAmount(aspect) > 0;
    }

    public boolean consumeBonusAspect(Aspect aspect) {
        if (aspect == null) {
            return false;
        }
        boolean consumed = bonusAspects.remove(aspect, 1);
        if (consumed) {
            setChanged();
            syncToClient();
        }
        return consumed;
    }

    public void recalculateBonusNow() {
        if (level != null && !level.isClientSide && ResearchTableBonusRuntime.recalculateInto(level, worldPosition, bonusAspects)) {
            setChanged();
            syncToClient();
        }
    }

    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Research Table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResearchTableMenu(containerId, playerInventory, this);
    }

    public boolean hasInkedScribingTools() {
        ItemStack tools = getItem(SLOT_SCRIBING_TOOLS);
        return tools.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(tools);
    }

    public boolean consumeInk(int amount, Player player) {
        if (player != null && player.getAbilities().instabuild) {
            return true;
        }
        ItemStack tools = getItem(SLOT_SCRIBING_TOOLS);
        boolean consumed = ScribingToolsItem.consumeInk(tools, amount);
        if (consumed) {
            setChanged();
        }
        return consumed;
    }

    public ItemStack researchNote() {
        ItemStack stack = getItem(SLOT_RESEARCH_NOTE);
        return stack.getItem() instanceof ResearchNoteItem ? stack : ItemStack.EMPTY;
    }

    public boolean createResearchNote(ServerPlayer player) {
        if (!getItem(SLOT_RESEARCH_NOTE).isEmpty()) {
            player.displayClientMessage(Component.literal("Research Table slot 1 already contains a note.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!hasInkedScribingTools() && !player.getAbilities().instabuild) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingToolsMessage(), false);
            return false;
        }
        if (!player.getAbilities().instabuild && !hasInventoryItem(player, Items.PAPER)) {
            player.displayClientMessage(Component.literal("A sheet of paper is required for a research note.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        // v11.62.24 transaction: snapshot both resources before mutation so a
        // packet race or depleted tool can never consume only one half.
        ItemStack toolsSnapshot = getItem(SLOT_SCRIBING_TOOLS).copy();
        int paperSlot = findInventoryItemSlot(player, Items.PAPER);
        ItemStack paperSnapshot = paperSlot < 0 ? ItemStack.EMPTY : player.getInventory().getItem(paperSlot).copy();
        if (!consumeInk(ResearchTableInventoryRuntime.INK_PER_NOTE_CREATE, player) || !consumePaper(player)) {
            setItem(SLOT_SCRIBING_TOOLS, toolsSnapshot);
            if (paperSlot >= 0) {
                player.getInventory().setItem(paperSlot, paperSnapshot);
            }
            player.displayClientMessage(Component.literal("Research note creation failed; paper and ink were restored.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ResearchTableFoundation.seed(player);
        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        ResearchEntry target = OriginalResearchBridge.selectedOrFirstAvailable(player).orElse(null);
        ResearchNoteState.initialize(note, target == null ? "" : target.key(), player.getRandom().nextLong());
        setItem(SLOT_RESEARCH_NOTE, note);

        recalculateBonusNow();
        if (!bonusAspects.isEmpty()) {
            player.displayClientMessage(Component.literal("Research table bonus aspects: " + ResearchTableBonusRuntime.summary(bonusAspects)).withStyle(ChatFormatting.DARK_AQUA), false);
        }
        ThaumcraftNetwork.syncAspectKnowledge(player);
        player.displayClientMessage(Component.literal("Research note prepared in table slot 1.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        playOriginalResearchSound("write", 0.35F, 1.0F);
        setChanged();
        return true;
    }

    /**
     * TC4 renders the note directly inside GuiResearchTable. This method only
     * synchronizes the note data; it deliberately does not open a second screen.
     */
    public void syncResearchNote(ServerPlayer player) {
        ThaumcraftNetwork.syncAspectKnowledge(player);
        ThaumcraftNetwork.syncResearchNote(player, researchNote());
    }

    public boolean openResearchNote(ServerPlayer player) {
        ItemStack note = researchNote();
        if (note.isEmpty()) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingNoteMessage(), false);
            return false;
        }
        if (!hasInkedScribingTools() && !player.getAbilities().instabuild) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingToolsMessage(), false);
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        ThaumcraftNetwork.openResearchNote(player, note);
        playOriginalResearchSound("page", 0.25F, 1.0F);
        return true;
    }

    public boolean completeResearchNote(ServerPlayer player) {
        ItemStack note = researchNote();
        if (note.isEmpty()) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingNoteMessage(), false);
            return false;
        }
        boolean converted = ResearchNoteSolver.convertSolvedNote(player, note);
        if (note.isEmpty()) {
            setItem(SLOT_RESEARCH_NOTE, ItemStack.EMPTY);
        }
        if (converted) {
            playOriginalResearchSound("learn", 0.45F, 1.0F);
        }
        setChanged();
        return converted;
    }

    public boolean copyCompletedResearchNote(ServerPlayer player) {
        ItemStack note = researchNote();
        if (note.isEmpty()) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingNoteMessage(), false);
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (!ResearchNoteState.solved(note)) {
            player.displayClientMessage(Component.literal("Only a completed research note can be copied.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!com.darkifov.thaumcraft.data.PlayerThaumData.hasResearch(player, "RESEARCHDUPE")) {
            player.displayClientMessage(Component.literal("Research Duplication is required to copy completed notes.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (note.getCount() >= note.getMaxStackSize()) {
            player.displayClientMessage(Component.literal("The completed research note stack is already full.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        Optional<ResearchEntry> target = OriginalResearchBridge.byKey(ResearchNoteState.target(note));
        if (target.isEmpty()) {
            player.displayClientMessage(Component.literal("This note is not bound to an original research key.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        int previousCopies = ResearchNoteState.copyCount(note);
        if (!player.getAbilities().instabuild && !hasCopyAspectCost(player, target.get(), previousCopies)) {
            player.displayClientMessage(Component.literal("You lack the original TC4 aspects required to duplicate this research.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!player.getAbilities().instabuild && !hasInventoryItem(player, Items.PAPER)) {
            player.displayClientMessage(Component.literal("A sheet of paper is required to copy a research note.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!player.getAbilities().instabuild && !hasInventoryItem(player, Items.INK_SAC)) {
            player.displayClientMessage(Component.literal("An ink sac is required to copy a research note.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!player.getAbilities().instabuild) {
            // Stage603-622: preserve TC4 duplicate-copy semantics. Materials are
            // validated before removal, then consumed as one transaction; copy
            // does not use scribing-tool durability.
            consumeInventoryItem(player, Items.PAPER);
            consumeInventoryItem(player, Items.INK_SAC);
            consumeCopyAspectCost(player, target.get(), previousCopies);
        }

        int nextCopies = ResearchNoteState.incrementCopyCount(note);
        note.grow(1);
        setItem(SLOT_RESEARCH_NOTE, note);
        setChanged();
        playOriginalResearchSound("write", 0.35F, 1.1F);
        player.displayClientMessage(Component.literal("Copied completed research note. Copies: " + nextCopies).withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    private boolean hasCopyAspectCost(Player player, ResearchEntry entry, int copies) {
        for (java.util.Map.Entry<String, Integer> aspectEntry : entry.aspects().entrySet()) {
            Aspect aspect = Aspect.byId(aspectEntry.getKey());
            int cost = Math.max(0, aspectEntry.getValue()) + Math.max(0, copies);
            if (aspect != null && cost > 0 && !PlayerAspectKnowledge.pool(player).contains(aspect, cost)) {
                return false;
            }
        }
        return true;
    }

    private void consumeCopyAspectCost(Player player, ResearchEntry entry, int copies) {
        for (java.util.Map.Entry<String, Integer> aspectEntry : entry.aspects().entrySet()) {
            Aspect aspect = Aspect.byId(aspectEntry.getKey());
            int cost = Math.max(0, aspectEntry.getValue()) + Math.max(0, copies);
            if (aspect != null && cost > 0) {
                PlayerAspectKnowledge.consumePool(player, aspect, cost);
            }
        }
    }

    private static int findInventoryItemSlot(Player player, net.minecraft.world.item.Item item) {
        if (player == null || item == null) {
            return -1;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasInventoryItem(Player player, net.minecraft.world.item.Item item) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private void playOriginalResearchSound(String key, float volume, float pitch) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, TC4Sounds.event(key), SoundSource.BLOCKS, volume, pitch);
        }
    }

    private boolean consumeInventoryItem(Player player, net.minecraft.world.item.Item item) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private boolean consumePaper(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SIZE) {
            return;
        }
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_SCRIBING_TOOLS) {
            return ResearchTableInventoryRuntime.isScribingTools(stack);
        }
        if (slot == SLOT_RESEARCH_NOTE) {
            return ResearchTableInventoryRuntime.isResearchNote(stack);
        }
        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("nextRecalc", nextRecalc);
        tag.put("bonusAspects", saveBonusAspects());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items);
        nextRecalc = tag.getInt("nextRecalc");
        loadBonusAspects(tag.getList("bonusAspects", 10));
    }

    private ListTag saveBonusAspects() {
        ListTag list = new ListTag();
        for (java.util.Map.Entry<Aspect, Integer> entry : bonusAspects.entries().entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                CompoundTag tag = new CompoundTag();
                tag.putString("tag", entry.getKey().id());
                list.add(tag);
            }
        }
        return list;
    }

    private void loadBonusAspects(ListTag list) {
        bonusAspects.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            Aspect aspect = Aspect.byId(tag.getString("tag"));
            if (aspect != null) {
                bonusAspects.add(aspect, 1);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

}
