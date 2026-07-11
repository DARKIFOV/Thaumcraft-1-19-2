package com.darkifov.thaumcraft.client;

/**
 * Legacy class retained so older mixin/event references do not fail to load.
 *
 * <p>TC4 never opened a second screen-space Thaumometer when the player looked
 * at a jar. Revealing goggles rendered compact aspect tags in the world above
 * the actual aspect container. The old RenderGuiOverlayEvent implementation was
 * therefore removed; the Forge 1.19.2 block-entity renderers now call
 * {@code RevealerAspectTagRenderer} at the target position.</p>
 */
public final class EssentiaOverlayEvents {
    private EssentiaOverlayEvents() {
    }
}
