package de.sharpadogge.merch.modules.font;

import java.util.HashMap;
import java.util.Map;

public class FontStyleMap {
    public Map<String, String> styles;

    public FontStyleMap() {
        styles = new HashMap<>();
    }

    public Map<String, String> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }

    public void addStyle(String style, String fontPath) {
        if (!this.styles.containsKey(style)) {
            this.styles.put(style, fontPath);
        }
    }
}
