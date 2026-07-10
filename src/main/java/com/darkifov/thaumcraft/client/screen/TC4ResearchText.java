package com.darkifov.thaumcraft.client.screen;

import net.minecraft.network.chat.Component;

/** Converts TC4 lang page markup to readable 1.19.2 screen text. */
final class TC4ResearchText {
    private TC4ResearchText() {}

    static String pageText(String key) {
        return clean(rawPageText(key));
    }

    static String rawPageText(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return Component.translatable(key).getString();
    }

    static String clean(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw;
        text = text.replaceAll("(?i)<BR\\s*/?>", "\n");
        text = text.replaceAll("(?i)<LINE\\s*/?>", "\n\n");
        text = text.replaceAll("(?i)<IMG>.*?</IMG>", "\n");
        text = text.replaceAll("(?i)<[^>]+>", "");
        text = stripMinecraftFormatting(text);
        return text.trim();
    }

    static String cleanInline(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw;
        text = text.replaceAll("(?i)<BR\\s*/?>", "\n");
        text = text.replaceAll("(?i)<LINE\\s*/?>", "\n\n");
        text = text.replaceAll("(?i)<[^>]+>", "");
        return stripMinecraftFormatting(text).trim();
    }

    private static String stripMinecraftFormatting(String text) {
        return text.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }
}
