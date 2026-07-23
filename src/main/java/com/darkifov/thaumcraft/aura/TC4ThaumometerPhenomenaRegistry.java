package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.AspectList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Forge 1.19.2 equivalent of TC4's public {@code IScanEventHandler} list.
 *
 * <p>Handlers are queried only after the ordinary entity/node/block ray did not
 * produce a target, preserving {@code ItemThaumometer#doScan}'s original
 * priority. The first non-null phenomenon wins. The returned key is stored in
 * the dedicated phenomena ledger and therefore must be stable across save/load.</p>
 */
public final class TC4ThaumometerPhenomenaRegistry {
    public static final String CONTRACT_VERSION = "11.64.25";

    @FunctionalInterface
    public interface Handler {
        @Nullable
        Phenomenon scanPhenomena(ItemStack scanner, Player player, float partialTick);
    }

    public record Phenomenon(String stableKey, Component displayName, AspectList aspects) {
        public Phenomenon {
            if (stableKey == null || stableKey.isBlank()) {
                throw new IllegalArgumentException("A Thaumometer phenomenon requires a stable key");
            }
            displayName = Objects.requireNonNullElseGet(displayName, Component::empty);
            AspectList copy = new AspectList();
            if (aspects != null) {
                copy.addAll(aspects);
            }
            aspects = copy;
        }
    }

    private static final CopyOnWriteArrayList<Handler> HANDLERS = new CopyOnWriteArrayList<>();

    private TC4ThaumometerPhenomenaRegistry() {
    }

    public static void register(Handler handler) {
        Handler value = Objects.requireNonNull(handler, "handler");
        if (!HANDLERS.contains(value)) {
            HANDLERS.add(value);
        }
    }

    public static boolean unregister(Handler handler) {
        return handler != null && HANDLERS.remove(handler);
    }

    public static List<Handler> handlers() {
        return List.copyOf(HANDLERS);
    }

    @Nullable
    public static TC4ThaumometerTargeting.ScanTarget find(Player player, ItemStack scanner, float partialTick) {
        if (player == null || scanner == null || scanner.isEmpty()) {
            return null;
        }
        for (Handler handler : HANDLERS) {
            Phenomenon phenomenon = handler.scanPhenomena(scanner, player, partialTick);
            if (phenomenon != null) {
                return new TC4ThaumometerTargeting.ScanTarget(
                        TC4ThaumometerTargeting.Kind.PHENOMENON,
                        null,
                        null,
                        phenomenon.displayName(),
                        phenomenon.aspects(),
                        phenomenon.stableKey(),
                        null
                );
            }
        }
        return null;
    }
}
