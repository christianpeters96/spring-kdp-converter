package de.sharpadogge.merch.modules.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.sharpadogge.merch.modules.font.FontCollection;
import de.sharpadogge.merch.modules.pdf.dto.KdpRequestBody;
import de.sharpadogge.merch.modules.pdf.dto.KdpRequestExtraFile;
import de.sharpadogge.merch.modules.pdf.dto.KdpRequestFont;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class PdfService {
    // 6x9 inches (120 pages)
    protected final static float width = inchToPoint(12.5200787f);
    protected final static float height = inchToPoint(9.25f);
    protected final static float coverWidth = inchToPoint(6);
    protected final static float coverHeight = inchToPoint(9);

    protected final static float borderWidthFull = 18; // 6.35mm
    protected final static float borderWidthTolerance = 9; // 6.35mm

    protected final static float sideWidthFull = 19.457008f; // 6.864mm
    protected final static float sideTextWidth = 10.457008f; // 3.689mm
    protected final static float sideTextBorderWidth = 4.5014173f; // 1.588mm

    protected final static float coverCropWidth = coverWidth - 30; // 10.5833mm
    protected final static float coverCropHeight = coverHeight - 30; // 10.5833mm

    public static void generatePDF(final KdpRequestBody dto, final String title, final OutputStream outputStream) {
        try {
            final Document document = new Document(new Rectangle(width, height));
            final PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();

            setBackgroundColor(document, hexToBaseColor(dto.getBgColor().getHex()));

            final PdfContentByte canvas = writer.getDirectContent();

            Image image = Image.getInstance(Base64.getDecoder().decode(dto.getBase64()));
            if (image.getWidth() >= coverCropWidth || image.getHeight() >= coverCropHeight) {
                image.scaleToFit(new Rectangle(coverCropWidth, coverCropHeight));
            }
            float positionOffsetX = (coverWidth - image.getScaledWidth()) / 2;
            float positionOffsetY = (coverHeight - image.getScaledHeight()) / 2;

            image.setAbsolutePosition(borderWidthTolerance + coverWidth + sideWidthFull + positionOffsetX, borderWidthTolerance + positionOffsetY);

            document.add(image);

            for (KdpRequestExtraFile extraFile : dto.getExtraFiles()) {
                String base64 = extraFile.getBase64().replaceAll("data:.+;base64,", "");
                Image extraImage = Image.getInstance(Base64.getDecoder().decode(base64));

                float extraPositionOffsetX;
                float extraPositionOffsetY;
                switch (extraFile.getType()) {
                    case "Front Cover":
                        if (extraImage.getWidth() >= coverCropWidth || extraImage.getHeight() >= coverCropHeight) {
                            extraImage.scaleToFit(new Rectangle(coverCropWidth, coverCropHeight));
                        }
                        extraPositionOffsetX = (coverWidth - extraImage.getScaledWidth()) / 2;
                        extraPositionOffsetY = (coverHeight - extraImage.getScaledHeight()) / 2;

                        extraImage.setAbsolutePosition(borderWidthTolerance + coverWidth + sideWidthFull + extraPositionOffsetX, borderWidthTolerance + extraPositionOffsetY);
                        break;
                    case "Back Cover":
                        if (extraImage.getWidth() >= coverCropWidth || extraImage.getHeight() >= coverCropHeight) {
                            extraImage.scaleToFit(new Rectangle(coverCropWidth, coverCropHeight));
                        }
                        extraPositionOffsetX = (coverWidth - extraImage.getScaledWidth()) / 2;
                        extraPositionOffsetY = (coverHeight - extraImage.getScaledHeight()) / 2;

                        extraImage.setAbsolutePosition(borderWidthTolerance + extraPositionOffsetX, borderWidthTolerance + extraPositionOffsetY);
                        break;
                    case "Overall":
                        if (extraImage.getWidth() >= width || extraImage.getHeight() >= height) {
                            extraImage.scaleToFit(new Rectangle(width, height));
                        }

                        extraImage.setAbsolutePosition((width/2)-(extraImage.getScaledWidth()/2), (height/2)-(extraImage.getScaledHeight()/2));
                        break;
                }
                document.add(extraImage);
            }

            BaseFont bf = getBaseFont(dto.getFont());
//            float ascent = bf.getAscentPoint(dto.getBookTitle().replaceAll("[gjpqy]", ""), dto.getFont().getSize());
//            float descent = bf.getDescentPoint(dto.getBookTitle().replaceAll("[gjpqy]", ""), dto.getFont().getSize());
            float ascent = bf.getAscentPoint(title, dto.getFont().getSize());
            float descent = bf.getDescentPoint(title, dto.getFont().getSize());
            float textOffset = (ascent + descent) / 2;

            Font titleFont = createFont(dto.getFont());
            createSideText(canvas, title, titleFont, borderWidthFull + 20, textOffset);

            document.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean generatesPlainPDF(final KdpRequestBody dto) {
        return dto.getBookTitles().size() == 1;
    }

    public static ByteArrayOutputStream generateByteStreamForBook(final KdpRequestBody dto) throws Exception {
        if (generatesPlainPDF(dto)) {
            final String bookTitle = dto.getBookTitles().get(0).trim();
            if (bookTitle.length() == 0) {
                throw new RuntimeException("No book title specified.");
            }
            ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
            PdfService.generatePDF(dto, bookTitle, pdfBaos);
            return pdfBaos;
        } else {
            ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
            ZipOutputStream zipOs = new ZipOutputStream(zipBaos);
            for (String bookTitle : dto.getBookTitles()) {
                if (bookTitle.length() == 0) {
                    throw new RuntimeException("No book title specified.");
                }
                ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
                PdfService.generatePDF(dto, bookTitle, pdfBaos);
                zipOs.putNextEntry(new ZipEntry(bookTitle + dto.getFileSuffix() + ".pdf"));
                zipOs.write(pdfBaos.toByteArray());
                zipOs.closeEntry();
            }
            zipOs.close();
            return zipBaos;
        }
    }

    public static void streamByteArraysForBook(final KdpRequestBody dto, TriConsumer<String, String, ByteArrayOutputStream> biConsumer) {
        for (String bookTitle : dto.getBookTitles()) {
            if (bookTitle.length() == 0) {
                throw new RuntimeException("No book title specified.");
            }
            ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
            PdfService.generatePDF(dto, bookTitle, pdfBaos);
            biConsumer.accept(bookTitle + dto.getFileSuffix(), bookTitle + dto.getFileSuffix() + " " + dto.getBgColor().getName(), pdfBaos);
        }
    }

    private static float inchToPoint(float inches) {
        return inches * 72f;
    }

    private static BaseColor hexToBaseColor(String hex) {
        hex = hex.toUpperCase();
        hex = hex.replaceAll("..(?!$)", "$0;");
        String[] hexSplit = hex.split(";");

        return new BaseColor(
                Integer.parseInt(hexSplit[0], 16),
                Integer.parseInt(hexSplit[1], 16),
                Integer.parseInt(hexSplit[2], 16)
        );
    }

    private static void setBackgroundColor(final Document document, final BaseColor color) {
        try {
            Rectangle backgroundColor = new Rectangle(width, height);
            backgroundColor.setBackgroundColor(color);
            document.add(backgroundColor);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createSideText(final PdfContentByte pcb, final String text, final Font font, final float margin, final float textOffset) {
        ColumnText.showTextAligned(pcb, Element.ALIGN_MIDDLE, new Phrase(text, font), (width/2)+textOffset, margin, 90);
    }

    private static Font createFont(final KdpRequestFont krf) {
        try {
            Font font = new Font(getBaseFont(krf), krf.getSize());
            font.setColor(hexToBaseColor(krf.getColor().getHex()));
            return font;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BaseFont getBaseFont(final KdpRequestFont krf) {
        try {
            final String fontPath = FontCollection.getFontPath(krf.getFamily(), krf.getStyle());
            assert fontPath != null;
            return BaseFont.createFont(fontPath, BaseFont.CP1252, BaseFont.EMBEDDED);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
