package com.example.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

@Service
public class SearchFrequencyService {

    // Get the MongoDB collection
    private MongoCollection<Document> getSearchFrequencyCollection() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        return db.getCollection("search_frequency");
    }

    // Record a search keyword
    public void recordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) 
            return;

        String word = keyword.trim().toLowerCase();
        MongoCollection<Document> collection = getSearchFrequencyCollection();

        collection.updateOne(
                Filters.eq("word", word),
                Updates.inc("count", 1),
                new UpdateOptions().upsert(true)
        );
    }

    // Get frequency of a keyword or top searched keywords
    public Map<String, Object> getFrequency(String keyword) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            MongoCollection<Document> collection = getSearchFrequencyCollection();

            if (keyword == null || keyword.trim().isEmpty()) {
                // Return top 10 searched words
                List<Document> top = collection.find()
                        .sort(new Document("count", -1))
                        .limit(10)
                        .into(new ArrayList<>());

                List<Map<String, Object>> topWords = new ArrayList<>();
                for (Document doc : top) {
                    topWords.add(Map.of(
                            "word", doc.getString("word"),
                            "count", doc.getInteger("count", 0)
                    ));
                }

                result.put("status", "success");
                result.put("top_words", topWords);
            } else {
                // Return count for specific keyword
                Document doc = collection.find(Filters.eq("word", keyword.trim().toLowerCase())).first();
                int count = (doc != null) ? doc.getInteger("count", 0) : 0;

                result.put("status", "success");
                result.put("keyword", keyword.trim().toLowerCase());
                result.put("count", count);
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }
}
