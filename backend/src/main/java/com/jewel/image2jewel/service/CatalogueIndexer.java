package com.jewel.image2jewel.service;

import com.jewel.image2jewel.model.JewelryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CatalogueIndexer {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueIndexer.class);
    
    private final EmbeddingService embeddingService;
    private final SearchService searchService;

    public CatalogueIndexer(EmbeddingService embeddingService, SearchService searchService) {
        this.embeddingService = embeddingService;
        this.searchService = searchService;
    }

    public void indexLocalCatalogue(String directoryPath) {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Catalogue directory not found: {}", directoryPath);
            return;
        }

        logger.info("🚀 Starting bulk indexing from: {}", directoryPath);

        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().toLowerCase().endsWith(".jpg") || p.toString().toLowerCase().endsWith(".png"))
                 .forEach(this::indexFile);
        } catch (IOException e) {
            logger.error("Error walking catalogue directory", e);
        }
        
        logger.info("✅ Bulk indexing complete.");
    }

    private void indexFile(Path file) {
        String filename = file.getFileName().toString();
        try {
            // Extract category (e.g., bracelet_abc.jpg -> Bracelet)
            String rawCategory = filename.split("_")[0];
            String category = rawCategory.substring(0, 1).toUpperCase() + rawCategory.substring(1).toLowerCase();
            
            String name = category + " " + UUID.randomUUID().toString().substring(0, 5);
            String description = "Premium " + category + " from the Image2Jewel catalogue.";
            
            logger.info("  Indexing: {} ({})", filename, category);

            // 1. Extract Embeddings
            float[] vector;
            try (FileInputStream fis = new FileInputStream(file.toFile())) {
                vector = embeddingService.extractEmbeddings(fis);
            }

            // 2. Create and Save Item
            JewelryItem item = new JewelryItem(
                UUID.randomUUID().toString(),
                name,
                category,
                description,
                "/assets/catalogue/" + filename, // Path relative to frontend assets
                vector
            );

            searchService.save(item);

        } catch (Exception e) {
            logger.error("Failed to index file: " + filename, e);
        }
    }
}
