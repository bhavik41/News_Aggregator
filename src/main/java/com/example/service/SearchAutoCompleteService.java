package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.example.model.News;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

@Service
public class SearchAutoCompleteService {

    private final NewsService newsService;

    public SearchAutoCompleteService(NewsService newsService) {
        this.newsService = newsService;
    }

    public List<Map<String, Object>> getSuggestions(String term, int suggestionLimit) {
        if (term == null || term.trim().isEmpty()) {
            return Collections.emptyList();
        }
        term = term.toLowerCase();
        return getSuggestionsWithFrequency(term, suggestionLimit);
    }

    public void incrementSearchFrequency(String term) {
        if (term == null || term.trim().isEmpty()) {
            return;
        }
        term = term.toLowerCase();

        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("search_frequency");

            collection.updateOne(
                Filters.eq("term", term),
                Updates.inc("count", 1),
                new UpdateOptions().upsert(true)
            );
            
            System.out.println("✅ Incremented search frequency for: " + term);
        } catch (Exception e) {
            System.err.println("Error updating search frequency: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> getSuggestionsWithFrequency(String prefix, int limit) {
        try {
            Map<String, Integer> termFrequency = new HashMap<>();
            Set<String> suggestions = new HashSet<>();

            List<News> allNews = newsService.getAllNews(1, 1000, "", "all");
            if (allNews != null) {
                for (News news : allNews) {
                    String combined = (news.getTitle() + " " + news.getDescription()).toLowerCase();
                    String[] words = combined.split("\\W+");
                    for (String word : words) {
                        if (!word.isEmpty() && word.startsWith(prefix)) {
                            suggestions.add(word);
                            termFrequency.putIfAbsent(word, 0);
                        }
                    }
                }
            }

            try {
                MongoDatabase db = MongoDBConnection.getDatabase();
                MongoCollection<Document> freqCollection = db.getCollection("search_frequency");

                freqCollection.find(Filters.regex("term", "^" + prefix))
                        .forEach(doc -> {
                            String t = doc.getString("term");
                            Integer count = doc.getInteger("count", 0);
                            suggestions.add(t);
                            termFrequency.put(t, count);
                        });
            } catch (Exception e) {
                System.err.println("Error fetching search frequencies: " + e.getMessage());
            }

            List<Map<String, Object>> result = suggestions.stream()
                    .map(t -> {
                        Map<String, Object> suggestionMap = new HashMap<>();
                        suggestionMap.put("term", t);
                        suggestionMap.put("frequency", termFrequency.getOrDefault(t, 0));
                        return suggestionMap;
                    })
                    .sorted((a, b) -> {
                        int freqCompare = Integer.compare(
                            (Integer) b.get("frequency"),
                            (Integer) a.get("frequency")
                        );
                        if (freqCompare != 0) return freqCompare;
                        return ((String) a.get("term")).compareTo((String) b.get("term"));
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            return result;

        } catch (Exception e) {
            System.err.println("Error generating suggestions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getTopSearches(int limit) {
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("search_frequency");

            List<Map<String, Object>> topTerms = new ArrayList<>();
            collection.find()
                    .sort(new Document("count", -1))
                    .limit(limit)
                    .forEach(doc -> {
                        Map<String, Object> termMap = new HashMap<>();
                        termMap.put("term", doc.getString("term"));
                        termMap.put("frequency", doc.getInteger("count", 0));
                        topTerms.add(termMap);
                    });
            return topTerms;

        } catch (Exception e) {
            System.err.println("Error fetching top searches: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}