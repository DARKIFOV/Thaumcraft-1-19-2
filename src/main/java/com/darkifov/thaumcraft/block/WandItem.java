package com.darkifov.thaumcraft.block;

import java.util.function.Consumer;
import com.darkifov.thaumcraft.client.render.WandItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import com.darkifov.thaumcraft.wand.WandRodType;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.Aspect;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WandItem extends Item {
    private static final String TAG_VIS = "Vis";
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
        return WandComponentData.from(stack).capacity();
    }

    public float stackVisCostModifier(ItemStack stack) {
        return WandComponentData.from(stack).visCostModifier();
    }

    public boolean isInfiniteVis(ItemStack stack) {
        return false;
    }

    public static boolean hasInfiniteVis(ItemStack stack) {
        return stack.getItem() instanceof WandItem wandItem && wandItem.isInfiniteVis(stack);
    }

    public int chargeFromNode(ItemStack wandStack, AuraNodeBlockEntity node) {
        if (hasInfiniteVis(wandStack)) {
            return 0;
        }

        int movedTotal = 0;

        for (Aspect aspect : PRIMAL_VIS) {
            int current = getVis(wandStack, aspect);

            if (current >= stackVisCapacity(wandStack)) {
                continue;
            }

            int capacity = stackVisCapacity(wandStack);
            int space = capacity - current;
            int maxDrain = node.isStabilized() ? 2 : 4;
            int drained = node.drainToWand(aspect, Math.min(maxDrain, space));

            if (drained > 0) {
                addVis(wandStack, aspect, drained);
                movedTotal += drained;
            }
        }

        return movedTotal;
    }

    public static int getVis(ItemStack stack, Aspect aspect) {
        if (hasInfiniteVis(stack)) {
            return INFINITE_VIS_DISPLAY;
        }

        CompoundTag vis = stack.getOrCreateTagElement(TAG_VIS);
        return vis.getInt(aspect.name());
    }

    public static int modifiedVisCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        if (baseAmount <= 0 || hasInfiniteVis(wandStack)) {
            return 0;
        }
        float modifier = WandComponentData.from(wandStack).visCostModifier(aspect);
        return Math.max(1, (int) Math.ceil(baseAmount * modifier));
    }

    public static boolean hasVisForCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        return hasVis(wandStack, aspect, modifiedVisCost(wandStack, aspect, baseAmount));
    }

    public static boolean consumeVisCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        return consumeVis(wandStack, aspect, modifiedVisCost(wandStack, aspect, baseAmount));
    }

    public static void clampVisToCapacity(ItemStack stack) {
        if (!(stack.getItem() instanceof WandItem wandItem) || hasInfiniteVis(stack)) {
            return;
        }
        int capacity = wandItem.stackVisCapacity(stack);
        CompoundTag vis = stack.getOrCreateTagElement(TAG_VIS);
        for (Aspect aspect : PRIMAL_VIS) {
            int current = vis.getInt(aspect.name());
            if (current > capacity) {
                vis.putInt(aspect.name(), capacity);
            }
        }
    }

    public static void addVis(ItemStack stack, Aspect aspect, int amount) {
        if (amount <= 0 || hasInfiniteVis(stack)) {
            return;
        }

        CompoundTag vis = stack.getOrCreateTagElement(TAG_VIS);
        int next = Math.max(0, vis.getInt(aspect.name()) + amount);
        if (stack.getItem() instanceof WandItem wandItem && isPrimalVis(aspect)) {
            next = Math.min(next, wandItem.stackVisCapacity(stack));
        }
        vis.putInt(aspect.name(), next);
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

        CompoundTag vis = stack.getOrCreateTagElement(TAG_VIS);
        int current = vis.getInt(aspect.name());

        if (current < amount) {
            return false;
        }

        vis.putInt(aspect.name(), current - amount);
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

        int realCost = modifiedVisCost(stack, aspect, amount);
        if (consumeVis(stack, aspect, realCost)) {
            return true;
        }

        player.displayClientMessage(
                Component.literal("Not enough vis in wand. Need " + aspect.displayName() + " " + realCost + " after cap discount. Charge it from an Aura Node.").withStyle(ChatFormatting.RED),
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
            builder.append(aspect.displayName()).append(" ").append(getVis(stack, aspect)).append("/").append(capacity);
        }

        return builder.toString();
    }


    private boolean tryInstallWandComponent(ItemStack wandStack, ItemStack componentStack, Player player) {
        if (componentStack.isEmpty() || player.isShiftKeyDown()) {
            return false;
        }

        var rod = WandComponentData.rodFromComponent(componentStack);
        if (rod.isPresent()) {
            WandComponentData.writeRod(wandStack, rod.get());
            clampVisToCapacity(wandStack);
            if (!player.getAbilities().instabuild) {
                componentStack.shrink(1);
            }
            player.displayClientMessage(Component.literal("Installed TC4 wand rod: " + rod.get().id()).withStyle(ChatFormatting.DARK_AQUA), true);
            return true;
        }

        var cap = WandComponentData.capFromComponent(componentStack);
        if (cap.isPresent()) {
            WandComponentData.writeCap(wandStack, cap.get());
            clampVisToCapacity(wandStack);
            if (!player.getAbilities().instabuild) {
                componentStack.shrink(1);
            }
            player.displayClientMessage(Component.literal("Installed TC4 wand caps: " + cap.get().id()).withStyle(ChatFormatting.GOLD), true);
            return true;
        }

        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player) || hasInfiniteVis(stack)) {
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
        tooltip.add(Component.literal("Capacity: " + stackVisCapacity(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Cap cost: x" + data.cap().visCostModifier() + " base").withStyle(ChatFormatting.GRAY));
        if (data.rod().staff()) {
            tooltip.add(Component.literal("Staff-class rod").withStyle(ChatFormatting.DARK_PURPLE));
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
            tooltip.add(focus.visCost().toComponent().withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal("Focus: none").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wandStack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(wandStack);
        }

        ItemStack offhand = player.getOffhandItem();

        if (hand == InteractionHand.MAIN_HAND && tryInstallWandComponent(wandStack, offhand, player)) {
            return InteractionResultHolder.consume(wandStack);
        }

        if (hand == InteractionHand.MAIN_HAND && offhand.getItem() instanceof FocusPouchItem) {
            if (FocusPouchItem.equipNextFocusFromPouch(offhand, wandStack, player, player.isShiftKeyDown())) {
                return InteractionResultHolder.consume(wandStack);
            }
            return InteractionResultHolder.consume(wandStack);
        }

        if (hand == InteractionHand.MAIN_HAND && offhand.getItem() instanceof WandFocusItem focusItem) {
            WandFocusRuntime.setFocus(wandStack, focusItem.focusType());
            if (!player.getAbilities().instabuild) {
                offhand.shrink(1);
            }
            player.displayClientMessage(Component.literal("Equipped " + focusItem.focusType().displayName() + " on wand.").withStyle(ChatFormatting.GOLD), true);
            return InteractionResultHolder.consume(wandStack);
        }

        if (player.isShiftKeyDown() && WandFocusRuntime.hasFocus(wandStack)) {
            WandFocusType oldFocus = WandFocusRuntime.getFocus(wandStack);
            WandFocusRuntime.setFocus(wandStack, null);
            ItemStack focusStack = WandFocusRuntime.focusStack(oldFocus);
            if (!player.getInventory().add(focusStack)) {
                player.drop(focusStack, false);
            }
            player.displayClientMessage(Component.literal("Removed " + oldFocus.displayName() + " from wand.").withStyle(ChatFormatting.GRAY), true);
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

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        ItemStack wandStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            int moved = chargeFromNode(wandStack, node);
            if (moved > 0) {
                player.displayClientMessage(Component.literal("Wand draws " + moved + " primal vis from the aura node.").withStyle(ChatFormatting.AQUA), true);
            } else {
                player.displayClientMessage(Component.literal("No compatible primal vis moved. Wand may be full or node depleted.").withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResult.CONSUME;
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
            return WandFocusRuntime.cast(wandStack, level, player);
        }

        player.displayClientMessage(Component.literal("Wand vis: " + visText(context.getItemInHand())).withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }
}
