package com.darkifov.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/** Standalone TC4 node layers with LEQUAL/no-depth rendering and no depth writes. */
public final class TC4NodeRenderTypes extends RenderType {
    private static final Map<String, RenderType> CACHE = new HashMap<>();

    private TC4NodeRenderTypes() {
        super("tc4_node_dummy", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, false, () -> {}, () -> {});
    }

    public static RenderType node(ResourceLocation texture, boolean additive, boolean depthIgnore) {
        String key = texture + "|" + additive + "|" + depthIgnore;
        return CACHE.computeIfAbsent(key, ignored -> create(
                "tc4_node_" + (additive ? "additive" : "alpha") + (depthIgnore ? "_visible" : "_depth"),
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(depthIgnore ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)));
    }
}
