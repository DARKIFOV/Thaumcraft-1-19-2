package com.darkifov.thaumcraft.block;

import java.util.function.Consumer;
import java.text.DecimalFormat;
import com.darkifov.thaumcraft.client.render.WandItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import com.darkifov.thaumcraft.wand.WandRodType;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.wand.TC4VisDiscountRuntime;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class WandItem extends Item {
    private static final String TAG_VIS = "Vis";
    private static final String TAG_VIS_FORMAT = "TC4VisCentivis";
    private static final byte VIS_FORMAT_CENTIVIS = 1;
    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("0.##");
    // Original TC4 ItemWandCasting stores the active IWandable coordinates at the root.
    private static final String TAG_NODE_X = "IIUX";
    private static final String TAG_NODE_Y = "IIUY";
    private static final String TAG_NODE_Z = "IIUZ";
    private static final String TAG_NODE_DIMENSION = "IIUD";
    private static final int NODE_TAP_INTERVAL = 5;
    private static final double NODE_TAP_REACH_SQR = 64.0D;
    private static final Aspect[] PRIMAL_VIS = new Aspect[]{Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};
    public static final int INFINITE_VIS_DISPLAY = Integer.MAX_VALUE / 8;

    private final int visCapacity;
    private final WandRodType defaultRod;
    private final WandCapType defaultCap;

    public WandItem(Properties properties, int visCapacity) {
        this(properties, visCapacity, WandRodType.WOOD, WandCapType.IRON);
    }

    public WandItem(Properties properties, int visCapacity, WandRodType defaultRod, WandCapType defaultCap) {
        super(properties);
        this.visCapacity = visCapacity;
        this.defaultRod = defaultRod;
        this.defaultCap = defaultCap;
    }

    public int visCapacity() {
        return WandComponentData.from(new ItemStack(this)).capacity();
    }

    public WandRodType defaultRod() {
        return defaultRod;
    }

    public WandCapType defaultCap() {
        return defaultCap;
    }

    public int stackVisCapacity(ItemStack stack) {
        return WandComponentData.from(stack).capacity(stack);
    }

    /**
     * Creates the same fully charged subtype stacks that original TC4 exposed
     * through ItemWandCasting#getSubItems. Values are stored in centivis and
     * therefore remain correct for wood, greatwood, elemental, silverwood,
     * staff and sceptre rods instead of falling back to the default 25-vis rod.
     */
    public static void fillToCapacity(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof WandItem wandItem)) {
            return;
        }
        int capacity = wandItem.stackVisCapacity(stack);
        CompoundTag root = stack.getOrCreateTag();
        root.putByte(TAG_VIS_FORMAT, VIS_FORMAT_CENTIVIS);
        for (Aspect aspect : PRIMAL_VIS) {
            storeVisRaw(stack, aspect, capacity);
        }
    }

    public float stackVisCostModifier(ItemStack stack) {
        return WandComponentData.from(stack).visCostModifier();
    }

    public boolean isInfiniteVis(ItemStack stack) {
        return false;
    }

    /** Stage189: original SlotLimitedByWand rejects staff rods from Arcane Workbench wand slot. */
    public static boolean isStaffStack(ItemStack stack) {
        return stack.getItem() instanceof WandItem && WandComponentData.from(stack).rod().staff();
    }

    public static boolean hasInfiniteVis(ItemStack stack) {
        return stack.getItem() instanceof WandItem wandItem && wandItem.isInfiniteVis(stack);
    }

    /**
     * Compatibility helper for relay/legacy calls.  Actual player tapping is handled by
     * the continuous-use path below and transfers one random primal every five ticks,
     * matching TileNode#onUsingWandTick from TC4 1.7.10.
     */
    public int chargeFromNode(ItemStack wandStack, AuraNodeBlockEntity node) {
        if (hasInfiniteVis(wandStack) || node == null) {
            return 0;
        }
        List<Aspect> candidates = aspectsWithRoom(wandStack, node, false);
        if (candidates.isEmpty()) {
            return 0;
        }
        Aspect aspect = candidates.get(0);
        int room = Math.max(0, (stackVisCapacity(wandStack) - getVis(wandStack, aspect) + 99) / 100);
        int drained = node.drainToWand(aspect, Math.min(1, room));
        if (drained > 0) {
            addVis(wandStack, aspect, drained);
        }
        return drained;
    }

    public static Aspect[] primalVisAspects() {
        return PRIMAL_VIS.clone();
    }

    public static int getVis(ItemStack stack, Aspect aspect) {
        if (hasInfiniteVis(stack)) {
            return INFINITE_VIS_DISPLAY;
        }
        if (stack == null || stack.isEmpty() || aspect == null) {
            return 0;
        }
        ensureCentivisStorage(stack);
        return readStoredVis(stack, aspect);
    }

    private static int readStoredVis(ItemStack stack, Aspect aspect) {
        CompoundTag root = stack.getTag();
        if (root == null) return 0;
        if (root.contains(aspect.id())) {
            return Math.max(0, root.getInt(aspect.id()));
        }
        if (root.contains(TAG_VIS)) {
            CompoundTag vis = root.getCompound(TAG_VIS);
            if (vis.contains(aspect.name())) return Math.max(0, vis.getInt(aspect.name()));
            if (vis.contains(aspect.id())) return Math.max(0, vis.getInt(aspect.id()));
        }
        return 0;
    }

    private static void storeVisRaw(ItemStack stack, Aspect aspect, int amount) {
        int value = Math.max(0, amount);
        CompoundTag root = stack.getOrCreateTag();
        root.putInt(aspect.id(), value);
        CompoundTag nested = root.contains(TAG_VIS) ? root.getCompound(TAG_VIS) : new CompoundTag();
        nested.putInt(aspect.name(), value);
        nested.putInt(aspect.id(), value);
        root.put(TAG_VIS, nested);
    }

    private static void storeVis(ItemStack stack, Aspect aspect, int amount) {
        if (stack == null || stack.isEmpty() || aspect == null || hasInfiniteVis(stack)) return;
        ensureCentivisStorage(stack);
        storeVisRaw(stack, aspect, amount);
    }

    /**
     * Save migration from the early rebuild's displayed-whole-vis storage to TC4's
     * original centivis NBT. Existing values at or below the old displayed capacity
     * are multiplied by 100 exactly once and marked with TC4VisCentivis=1.
     */
    private static void ensureCentivisStorage(ItemStack stack) {
        if (stack == null || stack.isEmpty() || hasInfiniteVis(stack)) return;
        CompoundTag root = stack.getOrCreateTag();
        if (root.getByte(TAG_VIS_FORMAT) >= VIS_FORMAT_CENTIVIS) return;

        CompoundTag nested = root.contains(TAG_VIS) ? root.getCompound(TAG_VIS) : new CompoundTag();
        WandComponentData components = WandComponentData.from(stack);
        int oldWholeCapacity = components.rod().baseCapacity();
        if (WandComponentData.isSceptre(stack)) {
            oldWholeCapacity = (int)Math.floor(oldWholeCapacity * 1.5F);
        }

        for (Aspect aspect : PRIMAL_VIS) {
            int raw;
            if (root.contains(aspect.id())) raw = Math.max(0, root.getInt(aspect.id()));
            else if (nested.contains(aspect.name())) raw = Math.max(0, nested.getInt(aspect.name()));
            else if (nested.contains(aspect.id())) raw = Math.max(0, nested.getInt(aspect.id()));
            else raw = 0;
            int centivis = raw > 0 && raw <= oldWholeCapacity ? raw * 100 : raw;
            root.putInt(aspect.id(), centivis);
            nested.putInt(aspect.name(), centivis);
            nested.putInt(aspect.id(), centivis);
        }
        root.put(TAG_VIS, nested);
        root.putByte(TAG_VIS_FORMAT, VIS_FORMAT_CENTIVIS);
    }

    private static void migrateLegacyVisStorage(ItemStack stack) {
        ensureCentivisStorage(stack);
    }

    /**
     * Compatibility overload for contexts without a player. It applies the cap
     * and sceptre modifiers but cannot apply equipped vis-discount gear.
     */
    public static int modifiedVisCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        return modifiedVisCost(wandStack, null, aspect, baseAmount, true);
    }

    /**
     * Exact ItemWandCasting#getConsumptionModifier adapter. Cap/aspect and
     * sceptre modifiers come from {@link WandComponentData}; equipped armor or
     * accessory discounts are subtracted through TC4VisDiscountRuntime. Focus
     * Frugal is applied only outside crafting, exactly like TC4.
     */
    public static float consumptionModifier(ItemStack wandStack, Player player, Aspect aspect, boolean crafting) {
        float modifier = WandComponentData.from(wandStack).visCostModifier(wandStack, aspect);
        modifier -= TC4VisDiscountRuntime.totalDiscount(player, aspect);

        // TC4 WandManager#getTotalVisDiscount subtracts ten percentage points
        // per level of Vis Exhaustion (normal or infectious). Since this port
        // stores discounts as a fraction subtracted from the cap modifier, the
        // harmful effect is represented by adding the same fraction here.
        if (player != null) {
            int exhaustAmplifier = -1;
            var normal = player.getEffect(ThaumcraftMod.VIS_EXHAUST.get());
            var infectious = player.getEffect(ThaumcraftMod.INFECTIOUS_VIS_EXHAUST.get());
            if (normal != null) exhaustAmplifier = Math.max(exhaustAmplifier, normal.getAmplifier());
            if (infectious != null) exhaustAmplifier = Math.max(exhaustAmplifier, infectious.getAmplifier());
            if (exhaustAmplifier >= 0) {
                modifier += (exhaustAmplifier + 1) / 10.0F;
            }
        }

        if (!crafting && WandFocusRuntime.hasFocus(wandStack)) {
            modifier -= WandFocusRuntime.focusUpgradeLevel(wandStack, com.darkifov.thaumcraft.wand.FocusUpgradeType.FRUGAL) / 10.0F;
        }
        return Math.max(modifier, 0.1F);
    }

    /**
     * TC4 multiplies centivis by the modifier and truncates with an int cast; it
     * does not round upward. This matters at percentage-discount boundaries.
     */
    public static int modifiedVisCost(ItemStack wandStack, Player player, Aspect aspect, int baseAmount, boolean crafting) {
        if (baseAmount <= 0 || hasInfiniteVis(wandStack)) {
            return 0;
        }
        return Math.max(0, (int) (baseAmount * consumptionModifier(wandStack, player, aspect, crafting)));
    }

    public static boolean hasVisForCost(ItemStack wandStack, Player player, Aspect aspect, int baseAmount, boolean crafting) {
        return hasVis(wandStack, aspect, modifiedVisCost(wandStack, player, aspect, baseAmount, crafting));
    }

    public static boolean consumeVisCost(ItemStack wandStack, Player player, Aspect aspect, int baseAmount, boolean crafting) {
        return consumeVis(wandStack, aspect, modifiedVisCost(wandStack, player, aspect, baseAmount, crafting));
    }

    public static boolean hasVisForCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        return hasVisForCost(wandStack, null, aspect, baseAmount, true);
    }

    public static boolean consumeVisCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        return consumeVisCost(wandStack, null, aspect, baseAmount, true);
    }

    public static void clampVisToCapacity(ItemStack stack) {
        if (!(stack.getItem() instanceof WandItem wandItem) || hasInfiniteVis(stack)) {
            return;
        }
        int capacity = wandItem.stackVisCapacity(stack);
        for (Aspect aspect : PRIMAL_VIS) {
            int current = getVis(stack, aspect);
            if (current > capacity) {
                storeVis(stack, aspect, capacity);
            }
        }
    }

    /** Original ItemWandCasting#addVis: amount is displayed whole vis. */
    public static void addVis(ItemStack stack, Aspect aspect, int amount) {
        addRealVis(stack, aspect, amount * 100);
    }

    /** Original addRealVis path: amount is already centivis. */
    public static void addRealVis(ItemStack stack, Aspect aspect, int centivis) {
        if (centivis <= 0 || hasInfiniteVis(stack)) return;
        int next = Math.max(0, getVis(stack, aspect) + centivis);
        if (stack.getItem() instanceof WandItem wandItem && isPrimalVis(aspect)) {
            next = Math.min(next, wandItem.stackVisCapacity(stack));
        }
        storeVis(stack, aspect, next);
    }

    private static boolean isPrimalVis(Aspect aspect) {
        for (Aspect primal : PRIMAL_VIS) {
            if (primal == aspect) return true;
        }
        return false;
    }

    public static boolean consumeVis(ItemStack stack, Aspect aspect, int amount) {
        if (amount <= 0 || hasInfiniteVis(stack)) {
            return true;
        }

        int current = getVis(stack, aspect);
        if (current < amount) {
            return false;
        }

        storeVis(stack, aspect, current - amount);
        return true;
    }

    public static boolean hasVis(ItemStack stack, Aspect aspect, int amount) {
        return amount <= 0 || hasInfiniteVis(stack) || getVis(stack, aspect) >= amount;
    }

    public static boolean consumeVisFromInventory(Player player, Aspect aspect, int amount) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem() instanceof WandItem && (hasInfiniteVis(stack) || getVis(stack, aspect) >= amount)) {
                return consumeVis(stack, aspect, amount);
            }
        }

        return false;
    }

    private boolean consumeTransformationCost(ItemStack stack, Aspect aspect, int amount, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int realCost = modifiedVisCost(stack, player, aspect, amount * 100, true);
        if (consumeVis(stack, aspect, realCost)) {
            return true;
        }

        player.displayClientMessage(
                Component.literal("Not enough vis in wand. Need " + aspect.displayName() + " " + formatVis(realCost) + " after cap discount. Charge it from an Aura Node.").withStyle(ChatFormatting.RED),
                false
        );

        return false;
    }

    public String visText(ItemStack stack) {
        if (hasInfiniteVis(stack)) {
            return "∞ infinite primal vis";
        }

        StringBuilder builder = new StringBuilder();
        int capacity = stackVisCapacity(stack);

        for (int i = 0; i < PRIMAL_VIS.length; i++) {
            Aspect aspect = PRIMAL_VIS[i];
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(aspect.displayName()).append(" ")
                    .append(formatVis(getVis(stack, aspect))).append("/").append(formatVis(capacity));
        }

        return builder.toString();
    }

    public static String formatVis(int centivis) {
        if (centivis >= INFINITE_VIS_DISPLAY) return "∞";
        return VIS_FORMAT.format(centivis / 100.0D);
    }

    public void beginNodeUse(ItemStack wandStack, Level level, BlockPos nodePos) {
        CompoundTag root = wandStack.getOrCreateTag();
        root.putInt(TAG_NODE_X, nodePos.getX());
        root.putInt(TAG_NODE_Y, nodePos.getY());
        root.putInt(TAG_NODE_Z, nodePos.getZ());
        root.putString(TAG_NODE_DIMENSION, level.dimension().location().toString());
    }

    public static boolean hasNodeTarget(ItemStack wandStack) {
        CompoundTag root = wandStack.getTag();
        return root != null && root.contains(TAG_NODE_X) && root.contains(TAG_NODE_Y) && root.contains(TAG_NODE_Z);
    }

    public static void clearNodeUse(ItemStack wandStack) {
        CompoundTag root = wandStack.getTag();
        if (root == null) {
            return;
        }
        root.remove(TAG_NODE_X);
        root.remove(TAG_NODE_Y);
        root.remove(TAG_NODE_Z);
        root.remove(TAG_NODE_DIMENSION);
    }

    private static BlockPos nodeTarget(ItemStack wandStack, Level level) {
        CompoundTag root = wandStack.getTag();
        if (root == null || !hasNodeTarget(wandStack)) {
            return null;
        }
        if (root.contains(TAG_NODE_DIMENSION)
                && !level.dimension().location().toString().equals(root.getString(TAG_NODE_DIMENSION))) {
            return null;
        }
        return new BlockPos(root.getInt(TAG_NODE_X), root.getInt(TAG_NODE_Y), root.getInt(TAG_NODE_Z));
    }

    private static boolean playerStillTargetsNode(Player player, BlockPos target) {
        if (player.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) > NODE_TAP_REACH_SQR) {
            return false;
        }
        HitResult hit = player.pick(8.0D, 1.0F, false);
        return hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(target);
    }

    private List<Aspect> aspectsWithRoom(ItemStack wandStack, AuraNodeBlockEntity node, boolean preserve) {
        List<Aspect> candidates = new ArrayList<>();
        int capacity = stackVisCapacity(wandStack);
        int minimumLeftInNode = preserve ? 1 : 0;
        for (Aspect aspect : PRIMAL_VIS) {
            if (getVis(wandStack, aspect) < capacity && node.aspects().get(aspect) > minimumLeftInNode) {
                candidates.add(aspect);
            }
        }
        return candidates;
    }

    private NodeTapResult tapNode(ItemStack wandStack, AuraNodeBlockEntity node, Player player) {
        if (hasInfiniteVis(wandStack)) {
            return NodeTapResult.NONE;
        }

        WandComponentData components = WandComponentData.from(wandStack);
        boolean preserve = !player.isShiftKeyDown()
                && PlayerThaumData.hasResearch(player, "NODEPRESERVE")
                && components.rod() != WandRodType.WOOD
                && components.cap() != WandCapType.IRON;

        List<Aspect> candidates = aspectsWithRoom(wandStack, node, preserve);
        if (candidates.isEmpty()) {
            return NodeTapResult.NONE;
        }

        Aspect aspect = candidates.get(player.getRandom().nextInt(candidates.size()));
        int tap = 1;
        if (PlayerThaumData.hasResearch(player, "NODETAPPER1")) {
            tap++;
        }
        if (PlayerThaumData.hasResearch(player, "NODETAPPER2")) {
            tap++;
        }

        int nodeAmount = node.aspects().get(aspect);
        int leaveBehind = preserve ? 1 : 0;
        int capacityRoom = Math.max(0, (stackVisCapacity(wandStack) - getVis(wandStack, aspect) + 99) / 100);
        int requested = Math.min(tap, Math.min(capacityRoom, Math.max(0, nodeAmount - leaveBehind)));
        if (requested <= 0) {
            return NodeTapResult.NONE;
        }

        int drained = node.drainToWand(aspect, requested);
        if (drained <= 0) {
            return NodeTapResult.NONE;
        }

        addVis(wandStack, aspect, drained);
        node.markWandDrain(aspect, player);
        return new NodeTapResult(aspect, drained);
    }

    private static void clearNodeDrain(Player player, ItemStack wandStack) {
        BlockPos target = nodeTarget(wandStack, player.level);
        if (target != null && player.level.getBlockEntity(target) instanceof AuraNodeBlockEntity node) {
            node.clearWandDrain(player);
        }
    }

    private void stopNodeUse(Player player, ItemStack wandStack) {
        clearNodeDrain(player, wandStack);
        clearNodeUse(wandStack);
        player.stopUsingItem();
    }

    private record NodeTapResult(Aspect aspect, int moved) {
        private static final NodeTapResult NONE = new NodeTapResult(null, 0);
    }



    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        WandComponentData data = WandComponentData.from(stack);
        // Vanilla JSON recipes cannot attach the original ItemWandCasting root
        // rod/cap tags, so write them immediately after the basic wand is crafted.
        WandComponentData.write(stack, data.rod(), data.cap());
        stack.getOrCreateTag().putByte(TAG_VIS_FORMAT, VIS_FORMAT_CENTIVIS);
        clampVisToCapacity(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!level.isClientSide) {
            CompoundTag root = stack.getTag();
            if (root == null || !root.contains(WandComponentData.ORIGINAL_TAG_ROD) || !root.contains(WandComponentData.ORIGINAL_TAG_CAP)) {
                WandComponentData data = WandComponentData.from(stack);
                WandComponentData.write(stack, data.rod(), data.cap());
            }
            migrateLegacyVisStorage(stack);
            clampVisToCapacity(stack);
        }

        if (hasNodeTarget(stack) && (!player.isUsingItem() || player.getUseItem() != stack)) {
            clearNodeUse(stack);
        }

        if (level.isClientSide || hasInfiniteVis(stack)) {
            return;
        }

        WandRodType rod = WandComponentData.from(stack).rod();
        int lowThreshold = Math.max(1, stackVisCapacity(stack) / 10);
        if (rod.regeneratesAllPrimals()) {
            if (player.tickCount % 50 != 0) {
                return;
            }
            java.util.List<Aspect> candidates = new java.util.ArrayList<>();
            for (Aspect aspect : PRIMAL_VIS) {
                if (getVis(stack, aspect) < lowThreshold) {
                    candidates.add(aspect);
                }
            }
            if (!candidates.isEmpty()) {
                addVis(stack, candidates.get(level.random.nextInt(candidates.size())), 1);
            }
            return;
        }

        Aspect regen = rod.regeneratedAspect();
        if (regen != null && player.tickCount % 200 == 0 && getVis(stack, regen) < lowThreshold) {
            addVis(stack, regen, 1);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(WandComponentData.from(stack).displayName(stack));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return WandItemRenderer.instance();
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        WandComponentData data = WandComponentData.from(stack);
        tooltip.add(Component.literal("Rod: " + data.rod().id()).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("Caps: " + data.cap().id()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Capacity: " + formatVis(stackVisCapacity(stack))).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Cap cost: x" + data.visCostModifier(stack, Aspect.AER) + " base").withStyle(ChatFormatting.GRAY));
        if (WandComponentData.isSceptre(stack)) {
            tooltip.add(Component.literal("Sceptre: +50% capacity, -10% vis cost, focus-capable")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (data.rod().staff()) {
            tooltip.add(Component.literal("Staff: focus-capable, cannot occupy the Arcane Workbench wand slot")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (data.rod().hasRodRegen()) {
            tooltip.add(Component.literal("TC4 rod recharge: up to 10% capacity").withStyle(ChatFormatting.DARK_GREEN));
        }
        if (hasInfiniteVis(stack)) {
            tooltip.add(Component.literal("Infinite Vis").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.literal("Vis: " + visText(stack)).withStyle(ChatFormatting.GRAY));
        WandFocusType focus = WandFocusRuntime.getFocus(stack);
        if (focus != null) {
            tooltip.add(Component.literal("Focus: " + focus.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE));
            com.darkifov.thaumcraft.AspectList focusCost = WandFocusRuntime.focusVisCost(stack, focus, net.minecraft.util.RandomSource.create());
            net.minecraft.network.chat.MutableComponent costLine = Component.literal("Vis cost: ");
            boolean firstCost = true;
            for (var entry : focusCost.entries().entrySet()) {
                if (!firstCost) costLine.append(Component.literal(", "));
                int modified = WandFocusRuntime.modifiedFocusVisCost(stack, entry.getKey(), entry.getValue());
                costLine.append(Component.literal(entry.getKey().displayName() + " " + formatVis(modified))
                        .withStyle(style -> style.withColor(entry.getKey().textColor())));
                firstCost = false;
            }
            tooltip.add(costLine.withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal("Focus: none").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return hasNodeTarget(stack) || WandFocusRuntime.shouldUseContinuously(stack)
                ? Integer.MAX_VALUE
                : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // Original TC4 ItemWandRenderer animates the wand itself with WandFocusAnimation.WAVE/CHARGE.
        // Do not let vanilla draw the wand as a bow; the custom renderer handles the held-use motion.
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        BlockPos target = nodeTarget(stack, level);
        if (target != null) {
            if (!(level.getBlockEntity(target) instanceof AuraNodeBlockEntity node) || !playerStillTargetsNode(player, target)) {
                stopNodeUse(player, stack);
                return;
            }

            // Original TileNode#onUsingWandTick taps one random eligible primal every five ticks.
            if (!level.isClientSide && player.getTicksUsingItem() % NODE_TAP_INTERVAL == 0) {
                NodeTapResult result = tapNode(stack, node, player);
                // TC4 keeps the use action active when the wand is full or the node
                // has no eligible primal.  It only clears the beam until a transfer
                // becomes possible or the player releases/looks away.
                if (result.moved() <= 0 || result.aspect() == null) {
                    node.clearWandDrain(player);
                    return;
                }
            }
            return;
        }

        // Original ItemWandCasting lets sceptres equip and cast foci; their
        // advantages are +50% capacity and -0.1 vis cost, not a focus ban.
        WandFocusRuntime.onUsingFocusTick(stack, level, player, remainingUseDuration);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        if (hasNodeTarget(stack)) {
            stopNodeUse(player, stack);
            return;
        }
        WandFocusRuntime.onPlayerStoppedUsingFocus(stack, level, player, remainingUseDuration);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wandStack = player.getItemInHand(hand);
        if (hasNodeTarget(wandStack) && !player.isUsingItem()) {
            clearNodeDrain(player, wandStack);
            clearNodeUse(wandStack);
        }

        if (level.isClientSide) {
            if (WandFocusRuntime.shouldUseContinuously(wandStack)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(wandStack);
            }
            return InteractionResultHolder.success(wandStack);
        }

        ItemStack offhand = player.getOffhandItem();

        if (hand == InteractionHand.MAIN_HAND && offhand.getItem() instanceof FocusPouchItem) {
            if (FocusPouchItem.equipNextFocusFromPouch(offhand, wandStack, player, player.isShiftKeyDown())) {
                return InteractionResultHolder.consume(wandStack);
            }
            return InteractionResultHolder.consume(wandStack);
        }

        if (hand == InteractionHand.MAIN_HAND && offhand.getItem() instanceof WandFocusItem focusItem) {
            ItemStack installedFocus = offhand.copy();
            installedFocus.setCount(1);
            WandFocusRuntime.setFocusStack(wandStack, installedFocus);
            if (!player.getAbilities().instabuild) {
                offhand.shrink(1);
            }
            player.displayClientMessage(Component.literal("Equipped " + focusItem.focusType().displayName() + " on wand.").withStyle(ChatFormatting.GOLD), true);
            return InteractionResultHolder.consume(wandStack);
        }

        if (player.isShiftKeyDown() && WandFocusRuntime.hasFocus(wandStack)) {
            WandFocusType oldFocus = WandFocusRuntime.getFocus(wandStack);
            ItemStack focusStack = WandFocusRuntime.getFocusStack(wandStack);
            WandFocusRuntime.setFocus(wandStack, null);
            if (focusStack.isEmpty()) {
                focusStack = WandFocusRuntime.focusStack(oldFocus);
            }
            if (!player.getInventory().add(focusStack)) {
                player.drop(focusStack, false);
            }
            player.displayClientMessage(Component.literal("Removed " + oldFocus.displayName() + " from wand.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.consume(wandStack);
        }

        if (WandFocusRuntime.shouldUseContinuously(wandStack)) {
            player.startUsingItem(hand);
            WandFocusRuntime.beginContinuousUse(wandStack, level, player);
            return InteractionResultHolder.consume(wandStack);
        }

        if (WandFocusRuntime.cast(wandStack, level, player).consumesAction()) {
            return InteractionResultHolder.consume(wandStack);
        }

        return InteractionResultHolder.pass(wandStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        ItemStack wandStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity) {
            beginNodeUse(wandStack, level, pos);
            player.startUsingItem(context.getHand());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) {
            if (WandFocusRuntime.shouldUseContinuously(wandStack)) {
                player.startUsingItem(context.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            if (crucible.spillRemnants(player)) {
                player.displayClientMessage(Component.literal("The wand spills the crucible remnants.").withStyle(ChatFormatting.DARK_PURPLE), true);
            } else {
                player.displayClientMessage(Component.literal("The crucible has no remnants to spill.").withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResult.CONSUME;
        }

        if (state.is(Blocks.BOOKSHELF)) {
            if (!consumeTransformationCost(wandStack, Aspect.ORDO, 1, player)) {
                return InteractionResult.CONSUME;
            }

            level.removeBlock(pos, false);
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, new ItemStack(ThaumcraftMod.THAUMONOMICON.get()));
            player.displayClientMessage(Component.literal("The bookshelf is transformed into a Thaumonomicon.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (state.is(ThaumcraftMod.TABLE.get())) {
            if (!consumeTransformationCost(wandStack, Aspect.ORDO, 3, player)) {
                return InteractionResult.CONSUME;
            }

            level.setBlock(pos, ThaumcraftMod.ARCANE_WORKBENCH.get().defaultBlockState(), 3);
            player.displayClientMessage(Component.literal("The table becomes an Arcane Workbench.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (state.is(Blocks.CAULDRON)) {
            if (!consumeTransformationCost(wandStack, Aspect.AQUA, 2, player)) {
                return InteractionResult.CONSUME;
            }

            level.setBlock(pos, ThaumcraftMod.CRUCIBLE.get().defaultBlockState(), 3);
            player.displayClientMessage(Component.literal("The cauldron becomes a Crucible.").withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (state.is(Blocks.CRAFTING_TABLE)) {
            player.displayClientMessage(Component.literal("Original TC4 uses a Thaumcraft Table, not a vanilla Crafting Table.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        WandFocusType focus = WandFocusRuntime.getFocus(wandStack);
        if (focus != null) {
            if (WandFocusRuntime.shouldUseContinuously(wandStack)) {
                player.startUsingItem(context.getHand());
                WandFocusRuntime.beginContinuousUse(wandStack, level, player);
                return InteractionResult.CONSUME;
            }
            return WandFocusRuntime.cast(wandStack, level, player);
        }

        player.displayClientMessage(Component.literal("Wand vis: " + visText(context.getItemInHand())).withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }
}
