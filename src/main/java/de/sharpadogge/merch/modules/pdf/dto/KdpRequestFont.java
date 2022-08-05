package de.sharpadogge.merch.modules.pdf.dto;

public class KdpRequestFont {

    private String family;
    private String style;
    private Float size;
    private KdpRequestColor color;

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Float getSize() {
        return size;
    }

    public void setSize(Float size) {
        this.size = size;
    }

    public KdpRequestColor getColor() {
        return color;
    }

    public void setColor(KdpRequestColor color) {
        this.color = color;
    }
}
