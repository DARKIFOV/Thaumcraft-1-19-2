
package com.darkifov.thaumcraft.block;
import com.darkifov.thaumcraft.menu.OsmoticEnchanterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class ThaumicTinkererDeviceBlock extends Block {
    public enum Mode { OSMOTIC_ENCHANTER, ETHEREAL_PLATFORM, FUME_DISSIPATOR, TRANSVECTOR_INTERFACE }
    private final Mode mode;
    public ThaumicTinkererDeviceBlock(Properties properties, Mode mode) { super(properties); this.mode = mode; }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (mode == Mode.OSMOTIC_ENCHANTER) {
            if (player.isShiftKeyDown()) OsmoticEnchantmentHelper.showStructureStatus(level, pos, player);
            else if (player instanceof ServerPlayer sp) NetworkHooks.openScreen(sp, new SimpleMenuProvider((int id, Inventory inv, Player p) -> new OsmoticEnchanterMenu(id, inv, pos), Component.literal("Osmotic Enchanter")), buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }
        if (mode == Mode.FUME_DISSIPATOR) {
            player.clearFire(); player.removeEffect(MobEffects.POISON); player.removeEffect(MobEffects.WITHER); player.removeEffect(MobEffects.BLINDNESS); player.removeEffect(MobEffects.CONFUSION);
            player.displayClientMessage(Component.literal("Fume Dissipator очищает вредные испарения и эффекты.").withStyle(ChatFormatting.AQUA), false);
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 1.2F);
            return InteractionResult.CONSUME;
        }
        if (mode == Mode.ETHEREAL_PLATFORM) { player.displayClientMessage(Component.literal("Ethereal Platform: collision-polish будет в отдельном pass.").withStyle(ChatFormatting.AQUA), false); return InteractionResult.CONSUME; }
        if (mode == Mode.TRANSVECTOR_INTERFACE) { player.displayClientMessage(Component.literal("Transvector Interface: следующий pass подключит remote interaction.").withStyle(ChatFormatting.LIGHT_PURPLE), false); return InteractionResult.CONSUME; }
        return InteractionResult.PASS;
    }
}
