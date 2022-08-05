package de.sharpadogge.merch.modules.font;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontCollection {

    static Map<String, FontStyleMap> fontMap = new HashMap<>();

    static void addFont(final String fontName) {
        if (!fontMap.containsKey(fontName))
            fontMap.put(fontName, new FontStyleMap());
    }

    static void addFontFile(final String fontName, final String fontStyle, final Path filePath) {
        if (fontMap.containsKey(fontName)) {
            fontMap.get(fontName).addStyle(fontStyle, filePath.toString());
        }
    }

    static Map<String, List<String>> frontendMap() {
        final Map<String, List<String>> output = new HashMap<>();
        for (Map.Entry<String, FontStyleMap> fontEntry : fontMap.entrySet()) {
            output.put(fontEntry.getKey(), new ArrayList<>());
            for (Map.Entry<String, String> styleEntry : fontEntry.getValue().styles.entrySet()) {
                output.get(fontEntry.getKey()).add(styleEntry.getKey());
            }
        }
        return output;
    }

    static Map<String, List<String>> searchInFrontendMap(final String query, final int max) {
        final Map<String, List<String>> output = new HashMap<>();
        final Map<String, List<String>> frontendMap = frontendMap();
        for (String fontName : frontendMap.keySet()) {
            if (fontName.toLowerCase().startsWith(query.toLowerCase())) {
                output.put(fontName, frontendMap().get(fontName));
                if (output.size() >= max) break;
            }
        }
        if (output.size() < max) {
            for (String fontName : frontendMap.keySet()) {
                if (fontName.toLowerCase().contains(query.toLowerCase())) {
                    output.put(fontName, frontendMap().get(fontName));
                    if (output.size() >= max) break;
                }
            }
        }
        return output;
    }

    public static String getFontPath(final String fontFamily, final String fontStyle) {
        if (!fontMap.containsKey(fontFamily) || !fontMap.get(fontFamily).styles.containsKey(fontStyle)) return null;
        return fontMap.get(fontFamily).styles.get(fontStyle);
    }

    public static int fontCount() {
        return fontMap.size();
    }

    public static int styleCount() {
        int count = 0;
        for (FontStyleMap style : fontMap.values()) {
            count += style.styles.size();
        }
        return count;
    }
}
