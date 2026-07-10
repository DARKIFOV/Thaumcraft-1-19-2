package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.GolemCoreItem;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.darkifov.thaumcraft.golem.GolemOriginalRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Functional de-metadata'd replacement for TC4 ItemGolemPlacer. */
public class TC4GolemPlacerItem extends TC4ResearchComponentItem {
    private final GolemMaterial material;

    public TC4GolemPlacerItem(Properties properties, GolemMaterial material,
                              String originalSource, String legacyTexture) {
        super(properties.stacksTo(1), originalSource, legacyTexture);
        this.material = material;
    }

    public GolemMaterial material() {
        return material;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        CompoundTag legacy = stack.getOrCreateTag();
        CompoundTag config = legacy.copy();
        migrateLegacyKeys(config);
        GolemCoreType core = resolveCore(config);
        boolean advanced = config.getBoolean(GolemOriginalRuntime.NBT_ADVANCED)
                || config.getBoolean("advanced");

        ThaumGolemEntity golem = ThaumcraftMod.THAUM_GOLEM.get().create(level);
        if (golem == null) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        golem.setOwnerUuid(player.getUUID());
        golem.setAdvancedGolem(advanced);
        golem.setGolemProfile(material, core);
        golem.setHomePos(pos);
        golem.setHomeFacing(context.getClickedFace().ordinal());
        golem.loadGolemConfiguration(config);
        if (stack.hasCustomHoverName()) golem.setCustomName(stack.getHoverName());
        golem.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(golem) || !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            player.displayClientMessage(Component.literal("Not enough room to place this golem.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        if (!level.addFreshEntity(golem)) {
            return InteractionResult.PASS;
        }
        level.playSound(null, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 0.7F, 1.2F);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    private static GolemCoreType resolveCore(CompoundTag tag) {
        if (tag.contains(GolemOriginalRuntime.NBT_CORE)) {
            return GolemCoreType.byOriginalId(tag.getByte(GolemOriginalRuntime.NBT_CORE));
        }
        if (tag.contains("core")) {
            return GolemCoreType.byOriginalId(tag.getByte("core"));
        }
        if (tag.contains(GolemCoreItem.TAG_CORE)) {
            return GolemCoreType.byName(tag.getString(GolemCoreItem.TAG_CORE));
        }
        return GolemCoreType.BLANK;
    }

    private static void migrateLegacyKeys(CompoundTag tag) {
        if (tag.contains("core") && !tag.contains(GolemOriginalRuntime.NBT_CORE)) {
            tag.putByte(GolemOriginalRuntime.NBT_CORE, tag.getByte("core"));
        }
        if (tag.contains("upgrades") && !tag.contains(GolemOriginalRuntime.NBT_UPGRADES)) {
            tag.putByteArray(GolemOriginalRuntime.NBT_UPGRADES, tag.getByteArray("upgrades"));
        }
        if (tag.contains("deco") && !tag.contains(GolemOriginalRuntime.NBT_DECORATION)) {
            tag.putString(GolemOriginalRuntime.NBT_DECORATION, tag.getString("deco"));
        }
        if (tag.contains("markers") && !tag.contains(GolemOriginalRuntime.NBT_MARKERS)) {
            tag.put(GolemOriginalRuntime.NBT_MARKERS, tag.getList("markers", 10).copy());
        }
        if (tag.contains("Inventory") && !tag.contains(GolemOriginalRuntime.NBT_INVENTORY)) {
            tag.put(GolemOriginalRuntime.NBT_INVENTORY, tag.getList("Inventory", 10).copy());
        }
        tag.putByte(GolemOriginalRuntime.NBT_GOLEM_TYPE, (byte) 0); // overwritten by fixed material profile
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        tooltip.add(Component.literal("Golem body: ").append(material.displayName()));
        GolemCoreType core = tag == null ? GolemCoreType.BLANK : resolveCore(tag);
        if (core != GolemCoreType.BLANK) tooltip.add(Component.literal("Core: ").append(core.displayName()));
        if (tag != null && tag.getBoolean("advanced")) {
            tooltip.add(Component.literal("Advanced golem").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (tag != null && tag.contains("upgrades")) {
            tooltip.add(Component.literal("Upgrades: " + GolemOriginalRuntime.upgradeDescription(tag.getByteArray("upgrades")))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (tag != null && tag.contains("markers")) {
            tooltip.add(Component.literal("Marked locations: " + tag.getList("markers", 10).size())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
