package de.sharpadogge.merch.modules.font;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sharpadogge.merch.config.ApplicationConfiguration;
import de.sharpadogge.merch.utils.Counter;
import de.sharpadogge.merch.utils.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FontService {

    private static Logger log = LoggerFactory.getLogger(FontService.class);

    private final ApplicationConfiguration applicationConfiguration;

    @Autowired
    public FontService(final ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    public void init() {
        log.info("Loading fonts ...");
        log.info("Checking configuration ...");
        log.info("Checking cache ..");
        if (checkCache()) {
            log.info("Cache file exists. Loading fonts from cache ..");
            loadCacheFile();
            log.info("Successfully imported " + FontCollection.fontCount() + " fonts (" + FontCollection.styleCount() + " styles)");
        } else {
            final String s_fontPath = applicationConfiguration.getFontPath();
            File fontPath = new File(s_fontPath);
            if (!fontPath.exists() || !fontPath.isDirectory()) throw new RuntimeException("the directory specified in 'app.fontPath' does not exist or is not a directory");
            // loop through files
            try {
                final Counter count = new Counter();
                final Timer timer = new Timer();
                log.info("Reading fonts from 'app.fontPath' (" + s_fontPath + ")");
                Files.walk(Paths.get(s_fontPath)).filter((path) -> {
                    String fileExtension = FilenameUtils.getExtension(path.getFileName().toString());
                    return Files.isRegularFile(path) && Objects.equals(fileExtension, "ttf");
                }).forEach((path) -> {
                    try {
                        count.inc();
                        if (timer.checkAndReset(2000)) {
                            log.info("Scanning font files: " + count.get());
                        }
                        Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(path.toString()));
                        String family = font.getFamily(Locale.US);
                        String style = font.getName().replace(font.getFamily(), "").trim();
                        if (style.length() == 0) style = "Regular";
                        FontCollection.addFont(family);
                        FontCollection.addFontFile(family, style, path);
                    }
                    catch (FontFormatException ignored) {}
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                if (FontCollection.fontMap.size() != 0) {
                    log.info("Successfully imported " + FontCollection.fontCount() + " fonts (" + FontCollection.styleCount() + " styles)");
                } else {
                    throw new RuntimeException("no fonts found in the directory specified in 'app.fontPath'");
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    boolean checkCache() {
        try {
            File dataPath = new File("data");
            if (!dataPath.exists() || (dataPath.exists() && !dataPath.isDirectory())) return false;
            File cachePath = new File(dataPath, "cache");
            if (!cachePath.exists() || (cachePath.exists() && !cachePath.isDirectory())) return false;
            File saveFile = new File(cachePath, "fonts.data");
            if (!saveFile.exists() || (saveFile.exists() && saveFile.isDirectory())) return false;
            InputStream is = new FileInputStream(saveFile);
            byte[] bytes = IOUtils.toByteArray(is);
            is.close();
            return bytes.length >= 10;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    int getCacheSize() {
        try {
            File dataPath = new File("data");
            if (!dataPath.exists() || (dataPath.exists() && !dataPath.isDirectory())) return 0;
            File cachePath = new File(dataPath, "cache");
            if (!cachePath.exists() || (cachePath.exists() && !cachePath.isDirectory())) return 0;
            File saveFile = new File(cachePath, "fonts.data");
            if (!saveFile.exists() || (saveFile.exists() && saveFile.isDirectory())) return 0;
            InputStream is = new FileInputStream(saveFile);
            byte[] bytes = IOUtils.toByteArray(is);
            is.close();
            if (bytes.length < 10) return 0;
            return bytes.length;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    int saveCacheFile() {
        try {
            File dataPath = new File("data");
            if (!dataPath.exists() || (dataPath.exists() && !dataPath.isDirectory())) dataPath.mkdir();
            File cachePath = new File(dataPath, "cache");
            if (!cachePath.exists() || (cachePath.exists() && !cachePath.isDirectory())) cachePath.mkdir();
            File saveFile = new File(cachePath, "fonts.data");
            if (!saveFile.exists() || (saveFile.exists() && saveFile.isDirectory())) saveFile.createNewFile();

            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(FontCollection.fontMap);

            OutputStream os = new FileOutputStream(saveFile);
            os.write(bytes);
            os.close();

            return bytes.length;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCacheFile() {
        try {
            File dataPath = new File("data");
            if (!dataPath.exists() || (dataPath.exists() && !dataPath.isDirectory())) return;
            File cachePath = new File(dataPath, "cache");
            if (!cachePath.exists() || (cachePath.exists() && !cachePath.isDirectory())) return;
            File saveFile = new File(cachePath, "fonts.data");
            if (!saveFile.exists() || (saveFile.exists() && saveFile.isDirectory())) return;

            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new FileInputStream(saveFile);
            byte[] bytes = IOUtils.toByteArray(is);
            FontCollection.fontMap = new HashMap<>();

            Map<Object, Object> rm = mapper.readValue(bytes, Map.class);
            for (Map.Entry<Object, Object> r : rm.entrySet()) {
                String fontName = r.getKey().toString();
                FontCollection.addFont(fontName);
                Map<Object, Object> im = (Map) r.getValue();
                for (Map.Entry<Object, Object> i : im.entrySet()) {
                    Map<Object, Object> sm = (Map) i.getValue();
                    for (Map.Entry<Object, Object> s : sm.entrySet()) {
                        FontCollection.addFontFile(fontName, s.getKey().toString(), new File(s.getValue().toString()).toPath());
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void deleteCacheFile() {
        try {
            File dataPath = new File("data");
            if (!dataPath.exists() || (dataPath.exists() && !dataPath.isDirectory())) return;
            File cachePath = new File(dataPath, "cache");
            if (!cachePath.exists() || (cachePath.exists() && !cachePath.isDirectory())) return;
            File saveFile = new File(cachePath, "fonts.data");
            if (!saveFile.exists() || (saveFile.exists() && saveFile.isDirectory())) return;
            FileUtils.writeLines(saveFile, Collections.singleton(""));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String normalizeString(final String fontString) {
        return fontString.replaceAll("([A-Z][a-z])", " $1").replaceAll("([a-z])([A-Z])", "$1 $2").trim();
    }
}
