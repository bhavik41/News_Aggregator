package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.db.MongoDBConnection;
import com.example.service.CrawlerService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CrawlerService crawlerService;

    /**
     * Get statistics about articles in the database
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("articles");
            
            long totalCount = collection.countDocuments();
            long bbcCount = collection.countDocuments(new Document("Source", "BBC"));
            long cnnCount = collection.countDocuments(new Document("Source", "CNN"));
            long guardianCount = collection.countDocuments(new Document("Source", "Guardian"));
            
            stats.put("success", true);
            stats.put("totalArticles", totalCount);
            stats.put("bbcArticles", bbcCount);
            stats.put("cnnArticles", cnnCount);
            stats.put("guardianArticles", guardianCount);
            
        } catch (Exception e) {
            stats.put("success", false);
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    /**
     * Manually trigger BBC crawler
     */
    @PostMapping("/crawl/bbc")
    public Map<String, String> triggerBBCCrawl() {
        Map<String, String> response = new HashMap<>();
        try {
            System.out.println("Manual BBC crawl triggered via API");
            crawlerService.crawlBBCAndSaveToDB();
            response.put("status", "success");
            response.put("message", "BBC crawler executed successfully");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }
}
