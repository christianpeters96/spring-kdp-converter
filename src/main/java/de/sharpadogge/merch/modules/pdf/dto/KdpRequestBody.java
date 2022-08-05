package de.sharpadogge.merch.modules.pdf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class KdpRequestBody {

    private String base64;

    private String fileName;

    private List<String> bookTitles;

    private String fileSuffix;

    private KdpRequestFont font;

    @JsonProperty("bgColor")
    private KdpRequestColor bgColor;

    private List<KdpRequestExtraFile> extraFiles;

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getBookTitles() {
        return bookTitles;
    }

    public void setBookTitles(List<String> bookTitles) {
        this.bookTitles = bookTitles;
    }

    public String getFileSuffix() {
        return fileSuffix != null ? fileSuffix : "";
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public KdpRequestFont getFont() {
        return font;
    }

    public void setFont(KdpRequestFont font) {
        this.font = font;
    }

    public KdpRequestColor getBgColor() {
        return bgColor;
    }

    public void setBgColor(KdpRequestColor bgColor) {
        this.bgColor = bgColor;
    }

    public List<KdpRequestExtraFile> getExtraFiles() {
        return extraFiles;
    }

    public void setExtraFiles(List<KdpRequestExtraFile> extraFiles) {
        this.extraFiles = extraFiles;
    }
}
