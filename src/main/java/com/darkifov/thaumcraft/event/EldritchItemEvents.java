package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.eldritch.TC4EldritchProgression;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EldritchItemEvents {
    private EldritchItemEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        if (player.level.isClientSide) {
            return;
        }

        ItemStack stack = event.getItemStack();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (key == null || !ThaumcraftMod.MOD_ID.equals(key.getNamespace())) {
            return;
        }

        String path = key.getPath();

        if ("tc4_crimson_rites".equals(path)) {
            TC4EldritchProgression.readCrimsonRites(player);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if ("tc4_eldritch_object".equals(path) || "tc4_eldritch_object_2".equals(path) || "tc4_eldritch_object_3".equals(path)) {
            TC4EldritchProgression.attuneWithEldritchEye(player, false);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
