package com.darkifov.thaumcraft.client.screen;

import net.minecraft.network.chat.Component;

/** Converts TC4 lang page markup to readable 1.19.2 screen text. */
final class TC4ResearchText {
    private TC4ResearchText() {}

    static String pageText(String key) {
        String raw = Component.translatable(key).getString();
        return clean(raw);
    }

    static String clean(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw;
        text = text.replaceAll("(?i)<BR\\s*/?>", "\n");
        text = text.replaceAll("(?i)<LINE\\s*/?>", "\n\n");
        text = text.replaceAll("(?i)<IMG>.*?</IMG>", "\n[image]\n");
        text = text.replaceAll("(?i)<[^>]+>", "");
        text = text.replace("§0", "").replace("§r", "");
        return text.trim();
    }
}
