package com.darkifov.thaumcraft.runic;

import com.darkifov.thaumcraft.block.TC4FortressArmorItem;
import com.darkifov.thaumcraft.block.TC4FortressArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * Stage213 runtime bridge for original ItemFortressArmor special protection.
 *
 * Original TC4 ItemFortressArmor implements a special-armor path with:
 * - ratio = armorProtection / 25 for ordinary damage;
 * - ratio = armorProtection / 35 for unblockable damage;
 * - ratio = armorProtection / 20 for fire/explosion damage;
 * - set multiplier starts at 0.875, adds 0.125 for legs/chest/helm, and adds
 *   0.05 when the scanned fortress piece carries a mask tag.
 */
public final class TC4FortressArmorRuntime {
    public static final double ORIGINAL_SET_BASE = 0.875D;
    public static final double ORIGINAL_SET_PIECE_BONUS = 0.125D;
    public static final double ORIGINAL_MASK_BONUS = 0.05D;
    public static final int ORIGINAL_FORTRESS_PIECE_COUNT = 3;
    public static final double ORIGINAL_ORDINARY_DIVISOR = 25.0D;
    public static final double ORIGINAL_UNBLOCKABLE_DIVISOR = 35.0D;
    public static final double ORIGINAL_FIRE_EXPLOSION_MAGIC_DIVISOR = 20.0D;

    private static final Set<String> FORTRESS_IDS = Set.of(
            "thaumcraft:tc4_thaumiumfortresshelm",
            "thaumcraft:tc4_thaumiumfortresschest",
            "thaumcraft:tc4_thaumiumfortresslegs"
    );

    private TC4FortressArmorRuntime() {
    }

    public static void handleHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getAmount() <= 0.0F || event.isCanceled()) {
            return;
        }
        double ratio = totalProtectionRatio(player, event.getSource());
        if (ratio <= 0.0D) {
            return;
        }
        float reduced = (float) Math.max(0.0D, event.getAmount() * (1.0D - Math.min(0.92D, ratio)));
        event.setAmount(reduced);
    }

    public static double totalProtectionRatio(Player player, DamageSource source) {
        if (player == null) {
            return 0.0D;
        }
        double set = fortressSetModifier(player);
        double ratio = 0.0D;
        for (ItemStack armor : player.getArmorSlots()) {
            if (!isFortressPiece(armor)) {
                continue;
            }
            ratio += slotProtection(armor) / divisor(source) * set;
        }
        return ratio;
    }

    public static double fortressSetModifier(Player player) {
        if (player == null) {
            return 0.0D;
        }
        double set = ORIGINAL_SET_BASE;
        // TC4 scanned armor array slots 1..3: legs, chest and helm.  Boots are
        // intentionally excluded because fortress armor has no boot piece.
        for (int index = 1; index < 4 && index < player.getInventory().armor.size(); index++) {
            ItemStack piece = player.getInventory().armor.get(index);
            if (isFortressPiece(piece)) {
                set += ORIGINAL_SET_PIECE_BONUS;
                if (TC4FortressMaskRuntime.mask(piece) >= 0) {
                    set += ORIGINAL_MASK_BONUS;
                }
            }
        }
        return set;
    }

    public static boolean isFullFortressSet(Player player) {
        return player != null
                && isFortressPiece(player.getItemBySlot(EquipmentSlot.HEAD))
                && isFortressPiece(player.getItemBySlot(EquipmentSlot.CHEST))
                && isFortressPiece(player.getItemBySlot(EquipmentSlot.LEGS));
    }

    public static boolean isFortressPiece(ItemStack stack) {
        String id = registryId(stack);
        return id != null && FORTRESS_IDS.contains(id);
    }

    private static double slotProtection(ItemStack stack) {
        if (stack.getItem() instanceof TC4FortressArmorItem armor) {
            return TC4FortressArmorMaterial.INSTANCE.getDefenseForSlot(armor.getSlot());
        }
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getDefense();
        }
        return 0.0D;
    }

    private static double divisor(DamageSource source) {
        if (source == null) {
            return ORIGINAL_ORDINARY_DIVISOR;
        }
        if (source.isBypassArmor()) {
            return ORIGINAL_UNBLOCKABLE_DIVISOR;
        }
        if (source.isFire() || source.isExplosion() || source.isMagic()) {
            return ORIGINAL_FIRE_EXPLOSION_MAGIC_DIVISOR;
        }
        return ORIGINAL_ORDINARY_DIVISOR;
    }

    private static String registryId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? null : id.toString();
    }
}
