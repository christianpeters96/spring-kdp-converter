package de.sharpadogge.merch.modules.pdf;

import de.sharpadogge.merch.modules.pdf.dto.KdpRequestBody;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfResource {

    private int cur = 0;
    private int max = 0;
    private boolean running = false;

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public void generate(@RequestBody final KdpRequestBody dto, final HttpServletResponse response) throws Exception {
        for (String t : dto.getBookTitles()) {
            if (t.trim().length() == 0) {
                throw new Exception("No book title specified. Please check your fields.");
            }
        }

        String contentType = PdfService.generatesPlainPDF(dto) ? "pdf" : "zip";
        ByteArrayOutputStream baos = PdfService.generateByteStreamForBook(dto);
        byte[] encodedBytes = Base64.getEncoder().encode(baos.toByteArray());

        response.reset();
        response.addHeader("Pragma", "public");
        response.addHeader("Cache-Control", "max-age=0");
        response.setHeader("Content-disposition", "attachment;filename=generated." + contentType);
        response.setContentType("application/" + contentType);

        response.setContentLength(encodedBytes.length);

        ServletOutputStream servletOutputStream = response.getOutputStream();
        servletOutputStream.write(encodedBytes);
        servletOutputStream.flush();
        servletOutputStream.close();
    }

    @RequestMapping(value = "/generate/multi", method = RequestMethod.POST)
    public void generateMultipleFilesIntoZip(@RequestBody List<KdpRequestBody> dtoList, @RequestParam(value = "createFolders", required = false) Boolean useSeparatedFolders, final HttpServletResponse response) throws Exception {
        for (KdpRequestBody dto : dtoList) {
            for (String t : dto.getBookTitles()) {
                if (t.trim().length() == 0) {
                    response.setStatus(400);
                    throw new Exception("No book title specified. Please check your fields.");
                }
            }
        }


        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        ZipOutputStream zipOs = new ZipOutputStream(zipBaos);

        final ArrayList<String> createdFolders = new ArrayList<>();
        final ArrayList<String> createdFiles = new ArrayList<>();

        for (KdpRequestBody dto : dtoList) {
            String plainName = dto.getFileName() + dto.getFileSuffix();
            String fileName = plainName + " " + dto.getBgColor().getName();
            String ext = FilenameUtils.getExtension(dto.getFileName());

            if (ext.equals("pdf")) {
                plainName = FilenameUtils.removeExtension(dto.getFileName());
            }

            if (useSeparatedFolders) {
                PdfService.streamByteArraysForBook(dto, (cFolder, cTitle, cBaos) -> {
                    try {
                        String tempFileName = cTitle;
                        if (!createdFolders.contains(cFolder)) {
                            zipOs.putNextEntry(new ZipEntry(cFolder + "/"));
                            createdFolders.add(cFolder);
                        }
                        while (createdFiles.contains(cFolder + "/" + cTitle + ".pdf")) {
                            //noinspection StringConcatenationInLoop
                            tempFileName += "_";
                        }
                        zipOs.putNextEntry(new ZipEntry(cFolder + "/" + tempFileName + ".pdf"));
                        createdFiles.add(cFolder + "/" + tempFileName);
                        zipOs.write(cBaos.toByteArray());
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            else {
                while (createdFiles.contains(fileName)) {
                    fileName += "_";
                }
                String fileType = PdfService.generatesPlainPDF(dto) ? ".pdf" : ".zip";
                zipOs.putNextEntry(new ZipEntry(fileName + fileType));
                createdFiles.add(fileName);
                zipOs.write(PdfService.generateByteStreamForBook(dto).toByteArray());
            }
            zipOs.closeEntry();
            cur++;
        }
        zipOs.close();
        byte[] encodedBytes = Base64.getEncoder().encode(zipBaos.toByteArray());

        response.reset();
        response.addHeader("Pragma", "public");
        response.addHeader("Cache-Control", "max-age=0");
        response.setHeader("Content-disposition", "attachment;filename=generated.zip");
        response.setContentType("application/zip");

        response.setContentLength(encodedBytes.length);

        ServletOutputStream servletOutputStream = response.getOutputStream();
        servletOutputStream.write(encodedBytes);
        servletOutputStream.flush();
        servletOutputStream.close();
        running = false;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Integer>> getStatusPercent() {
        Map<String, Integer> output = new HashMap<>();
        output.put("value", 0);
        if (!running) return new ResponseEntity<>(output, HttpStatus.ACCEPTED);
        output.put("value", Math.round((float)cur / (float)max) * 100);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}
