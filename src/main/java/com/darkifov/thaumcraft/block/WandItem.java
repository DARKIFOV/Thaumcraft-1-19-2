package com.darkifov.thaumcraft.block;

import java.util.function.Consumer;
import com.darkifov.thaumcraft.client.render.WandItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import com.darkifov.thaumcraft.wand.WandRodType;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
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

        for (Aspect aspect : Aspect.values()) {
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

    public static void addVis(ItemStack stack, Aspect aspect, int amount) {
        if (amount <= 0 || hasInfiniteVis(stack)) {
            return;
        }

        CompoundTag vis = stack.getOrCreateTagElement(TAG_VIS);
        vis.putInt(aspect.name(), Math.max(0, vis.getInt(aspect.name()) + amount));
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

        if (consumeVis(stack, aspect, amount)) {
            return true;
        }

        player.displayClientMessage(
                Component.literal("Not enough vis in wand. Need " + aspect.displayName() + " " + amount + ". Charge it from an Aura Node.").withStyle(ChatFormatting.RED),
                false
        );

        return false;
    }

    public String visText(ItemStack stack) {
        if (hasInfiniteVis(stack)) {
            return "∞ infinite primal vis";
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (Aspect aspect : Aspect.values()) {
            int amount = getVis(stack, aspect);

            if (amount <= 0) {
                continue;
            }

            if (!first) {
                builder.append(", ");
            }

            builder.append(aspect.displayName()).append(" ").append(amount).append("/").append(stackVisCapacity(stack));
            first = false;
        }

        return first ? "empty" : builder.toString();
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
        if (hasInfiniteVis(stack)) {
            tooltip.add(Component.literal("Infinite Vis").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.literal("Vis: " + visText(stack)).withStyle(ChatFormatting.GRAY));
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

        if (state.is(Blocks.BOOKSHELF)) {
            if (!consumeTransformationCost(wandStack, Aspect.PRAECANTATIO, 2, player)) {
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

        player.displayClientMessage(Component.literal("Wand vis: " + visText(context.getItemInHand())).withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }
}
