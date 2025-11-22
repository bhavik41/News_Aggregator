package com.example.service;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchFrequencyService {

    public Map<String, Object> getFrequency(String keyword) {

        Map<String, Object> result = new LinkedHashMap<>();

        MongoDatabase db = MongoDBConnection.getDatabase();
        if (db == null) {
            result.put("status", "error");
            result.put("message", "Database connection failed");
            return result;
        }

        MongoCollection<Document> collection = db.getCollection("articles");
        if (collection == null) {
            result.put("status", "error");
            result.put("message", "Collection 'articles' not found");
            return result;
        }

        try {
            // CASE 1: No keyword → return top 10 words
            if (keyword == null || keyword.trim().isEmpty()) {
                return getTopWords(collection);
            }

            // CASE 2: Count keyword occurrences
            return getKeywordCount(collection, keyword.trim().toLowerCase());

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            return result;
        }
    }

    // --------- TOP WORD CALCULATION ----------
    private Map<String, Object> getTopWords(MongoCollection<Document> collection) {

        Map<String, Integer> wordCount = new HashMap<>();
        Map<String, Object> result = new LinkedHashMap<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                String title = doc.getString("Headline");
                String desc = doc.getString("Description");

                if (title == null) title = "";
                if (desc == null) desc = "";

                String combined = (title + " " + desc).toLowerCase();
                String[] words = combined.split("\\W+");

                for (String w : words) {
                    if (w.length() > 2) {
                        wordCount.put(w, wordCount.getOrDefault(w, 0) + 1);
                    }
                }
            }
        }

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

    // --------- KEYWORD COUNT ----------
    private Map<String, Object> getKeywordCount(MongoCollection<Document> collection, String keyword) {

        Map<String, Object> result = new LinkedHashMap<>();
        int count = 0;

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                String title = doc.getString("Headline");
                String desc = doc.getString("Description");

                if (title == null) title = "";
                if (desc == null) desc = "";

                String combined = (title + " " + desc).toLowerCase();

                count += countOccurrences(combined, keyword);
            }
        }

        result.put("status", "success");
        result.put("keyword", keyword);
        result.put("count", count);
        return result;
    }

    // helper to count keyword
    private int countOccurrences(String text, String keyword) {
        int count = 0, pos = 0;
        while ((pos = text.indexOf(keyword, pos)) != -1) {
            count++;
            pos += keyword.length();
        }
        return count;
    }
}
