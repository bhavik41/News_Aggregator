package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@Service
public class SearchFrequencyService {

    public Map<String, Object> getFrequency(String keyword) {

        Map<String, Object> result = new LinkedHashMap<>();

        MongoDatabase db = MongoDBConnection.getDatabase();
        if (db == null) {
            return error("Database connection failed");
        }

        MongoCollection<Document> collection = db.getCollection("articles");
        if (collection == null) {
            return error("Collection 'articles' not found");
        }

        try {
            // If keyword is empty → return top words
            if (keyword == null || keyword.trim().isEmpty()) {
                return getTopWords(collection);
            }

            // If keyword provided → return keyword count
            return getKeywordCount(collection, keyword.trim().toLowerCase());

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ---------- ERROR HANDLER ----------
    private Map<String, Object> error(String msg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "error");
        map.put("message", msg);
        return map;
    }

    // ---------- TOP WORDS ----------
    private Map<String, Object> getTopWords(MongoCollection<Document> collection) {

        Map<String, Integer> wordCount = new HashMap<>();
        Map<String, Object> result = new LinkedHashMap<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {

            while (cursor.hasNext()) {
                Document doc = cursor.next();

                String title = Optional.ofNullable(doc.getString("Headline")).orElse("");
                String desc = Optional.ofNullable(doc.getString("Description")).orElse("");

                String combined = (title + " " + desc).toLowerCase();

                // Extract words (only alphabets)
                String[] words = combined.split("[^a-zA-Z]+");

                for (String w : words) {
                    if (w.length() > 2) {  // ignore small words
                        wordCount.put(w, wordCount.getOrDefault(w, 0) + 1);
                    }
                }
            }
        }

        // Sort by highest frequency
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(wordCount.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        List<Map<String, Object>> top = new ArrayList<>();
        int limit = Math.min(10, sorted.size());

        for (int i = 0; i < limit; i++) {
            top.add(Map.of(
                    "word", sorted.get(i).getKey(),
                    "count", sorted.get(i).getValue()
            ));
        }

        result.put("status", "success");
        result.put("top_words", top);
        return result;
    }

    // ---------- KEYWORD OCCURRENCE COUNT ----------
    private Map<String, Object> getKeywordCount(MongoCollection<Document> collection, String keyword) {

        int count = 0;
        Map<String, Object> result = new LinkedHashMap<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {

            while (cursor.hasNext()) {
                Document doc = cursor.next();

                String title = Optional.ofNullable(doc.getString("Headline")).orElse("");
                String desc = Optional.ofNullable(doc.getString("Description")).orElse("");

                String combined = (title + " " + desc).toLowerCase();

                // Whole-word split, more accurate
                String[] words = combined.split("[^a-zA-Z]+");

                for (String w : words) {
                    if (w.equals(keyword)) {
                        count++;
                    }
                }
            }
        }

        result.put("status", "success");
        result.put("keyword", keyword);
        result.put("count", count);
        return result;
    }
}
