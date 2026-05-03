package com.jewel.image2jewel.controller;

import com.jewel.image2jewel.model.JewelryItem;
import com.jewel.image2jewel.model.SearchCriteria;
import com.jewel.image2jewel.service.CatalogueIndexer;
import com.jewel.image2jewel.service.EmbeddingService;
import com.jewel.image2jewel.service.LLMService;
import com.jewel.image2jewel.service.SearchService;
import dev.langchain4j.data.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jewelry")
@CrossOrigin(origins = "*") // For POC simplicity
public class JewelryController {

    private static final Logger logger = LoggerFactory.getLogger(JewelryController.class);

    private final EmbeddingService embeddingService;
    private final SearchService searchService;
    private final LLMService llmService; // nullable — LLM is optional
    private final CatalogueIndexer catalogueIndexer;

    public JewelryController(
            EmbeddingService embeddingService,
            SearchService searchService,
            @Autowired(required = false) LLMService llmService,
            CatalogueIndexer catalogueIndexer) {
        this.embeddingService = embeddingService;
        this.searchService = searchService;
        this.llmService = llmService;
        this.catalogueIndexer = catalogueIndexer;
    }

    @GetMapping("/bulk-index")
    public ResponseEntity<String> bulkIndex() {
        // Absolute path to the catalogue images
        String path = "/home/shivesh/Desktop/Image2Jewel/catalogue/images";
        catalogueIndexer.indexLocalCatalogue(path);
        return ResponseEntity.ok("Bulk indexing started for: " + path);
    }

    @PostMapping("/search")
    public ResponseEntity<List<JewelryItem>> searchByImage(@RequestParam("image") MultipartFile file) throws Exception {
        logger.info("📡 Incoming Search Request: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        float[] vector = embeddingService.extractEmbeddings(file.getInputStream());
        List<JewelryItem> results = searchService.findSimilar(vector, 10);
        logger.info("🎯 Search results found: {}", results.size());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/pair")
    public ResponseEntity<List<JewelryItem>> pairAndSearch(
            @RequestParam("image") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("text") String text) throws Exception {

        logger.info("🤖 Hybrid Search Triggered for: {}", category);

        // 1. Vector Extraction (The 'Eyes')
        float[] vector = embeddingService.extractEmbeddings(file.getInputStream());

        // 2. LLM Analysis (The 'Brain')
        SearchCriteria criteria = null;
        if (llmService != null) {
            try {
                byte[] bytes = file.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                Image image = Image.builder()
                        .base64Data(base64)
                        .mimeType(file.getContentType())
                        .build();

                criteria = llmService.findMatchingCriteria(category, text, image);
                logger.info("✨ AI Search Intent: Category={}, Metal={}, Stone={}, Style={}", 
                        criteria.category(), criteria.metal(), criteria.stone(), criteria.style());
            } catch (Exception e) {
                logger.error("⚠️ LLM reasoning failed: " + e.getMessage());
            }
        }

        // 3. Execution
        List<JewelryItem> results;
        if (criteria != null) {
            // HYBRID: Visual Similarity + LLM Metadata Filters
            results = searchService.findSimilarHybrid(vector, criteria, 5);
        } else {
            // FALLBACK: Just Visual Similarity
            results = searchService.findSimilar(vector, 5);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/index")
    public ResponseEntity<String> indexItem(
            @RequestParam("image") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("description") String description) throws Exception {

        float[] vector = embeddingService.extractEmbeddings(file.getInputStream());

        JewelryItem item = new JewelryItem(
                UUID.randomUUID().toString(),
                name,
                category,
                description,
                "/assets/catalogue/" + file.getOriginalFilename(),
                vector
        );

        searchService.save(item);
        return ResponseEntity.ok("Indexed: " + name);
    }
}
