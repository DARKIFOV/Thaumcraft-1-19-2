package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.item.gear.TC4VoidArmorMaterial;
import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import com.darkifov.thaumcraft.wand.TC4VisDiscountGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;

/** TC4 Void robes: epic, dyeable, 5% discount, warp and one durability repair per second. */
public class TC4VoidRobeItem extends ArmorItem implements DyeableLeatherItem, TC4VisDiscountGear {
    private static final String LAST_REPAIR_TICK = "TC4VoidRobeLastRepairTick";
    private static final TagKey<Item> VOID_INGOTS = TagKey.create(Registry.ITEM_REGISTRY,
            new ResourceLocation("forge", "ingots/void_metal"));
    private final EquipmentSlot armorSlot;

    public TC4VoidRobeItem(EquipmentSlot slot, Properties properties) {
        super(TC4VoidArmorMaterial.INSTANCE, slot, properties.stacksTo(1));
        this.armorSlot = slot;
    }

    public boolean isRevealingHelmet() {
        return armorSlot == EquipmentSlot.HEAD;
    }

    @Override
    public int getColor(ItemStack stack) {
        CompoundTag display = stack.getTagElement("display");
        return display != null && display.contains("color", Tag.TAG_ANY_NUMERIC)
                ? display.getInt("color")
                : 6961280;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return type == null
                ? "thaumcraft:textures/models/void_robe_armor_overlay.png"
                : "thaumcraft:textures/models/void_robe_armor.png";
    }

    @Override
    public int getVisDiscount(ItemStack stack, LivingEntity wearer, Aspect aspect) {
        return 5;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack ingredient) {
        return ingredient.is(VOID_INGOTS) || super.isValidRepairItem(toRepair, ingredient);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        repair(stack, level, entity);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        repair(stack, level, player);
    }

    private static void repair(ItemStack stack, Level level, Entity holder) {
        if (level.isClientSide || !stack.isDamaged() || !(holder instanceof LivingEntity) || holder.tickCount % 20 != 0) {
            return;
        }
        long now = level.getGameTime();
        if (stack.getOrCreateTag().getLong(LAST_REPAIR_TICK) == now) {
            return;
        }
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        stack.getOrCreateTag().putLong(LAST_REPAIR_TICK, now);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Vis discount: 5%").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Self-repairs while worn").withStyle(ChatFormatting.DARK_AQUA));
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
        TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
    }
}
