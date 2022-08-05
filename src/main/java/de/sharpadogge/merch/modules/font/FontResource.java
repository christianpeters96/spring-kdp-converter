package de.sharpadogge.merch.modules.font;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/font")
public class FontResource {

    private FontService fontService;

    @Autowired
    public FontResource(FontService fontService) {
        this.fontService = fontService;
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, List<String>>> getFontCollection() {
        return new ResponseEntity<>(FontCollection.frontendMap(), HttpStatus.OK);
    }

    @GetMapping("/search/{query}/{resultCount}")
    public ResponseEntity<Map<String, List<String>>> searchInFontCollection(@PathVariable("query") String query, @PathVariable("resultCount") int resultCount) {
        if (resultCount < 1) resultCount = 1;
        if (resultCount > 20) resultCount = 20;
        return new ResponseEntity<>(FontCollection.searchInFrontendMap(query, resultCount), HttpStatus.OK);
    }

    @PostMapping("/reload")
    public Map<String, Integer> reloadFonts() {
        FontCollection.fontMap = new HashMap<>();
        fontService.init();
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("fontCount", FontCollection.fontCount());
        countMap.put("styleCount", FontCollection.styleCount());
        return countMap;
    }

    @GetMapping("/cache")
    public int checkCache() {
        return fontService.getCacheSize();
    }

    @PutMapping("/cache")
    public int saveCache() {
        return fontService.saveCacheFile();
    }

    @DeleteMapping("/cache")
    public boolean deleteCache() {
        fontService.deleteCacheFile();
        return !fontService.checkCache();
    }
}
