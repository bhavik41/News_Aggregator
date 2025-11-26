package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Main API function:
     * 1️⃣ Increment search frequency for the term
     * 2️⃣ Return autocomplete suggestions based on prefix
     */
    public List<String> searchAndSuggest(String term, int suggestionLimit) {
        if (term == null || term.trim().isEmpty()) {
            return Collections.emptyList();
        }

        term = term.toLowerCase();

        // 1️⃣ Increment search frequency in MongoDB
        incrementSearchFrequency(term);

        // 2️⃣ Generate autocomplete suggestions
        return getSuggestions(term, suggestionLimit);
    }

    /**
     * Increment the search frequency for a term
     */
    private void incrementSearchFrequency(String term) {
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("search_frequency");

            collection.updateOne(
                Filters.eq("term", term),
                Updates.inc("count", 1),
                new UpdateOptions().upsert(true)
            );
        } catch (Exception e) {
            System.err.println("Error updating search frequency: " + e.getMessage());
        }
    }

    /**
     * Get autocomplete suggestions based on news titles/descriptions + search frequencies
     */
    private List<String> getSuggestions(String prefix, int limit) {
        try {
            Set<String> suggestions = new HashSet<>();

            // Load news
            List<News> allNews = newsService.getAllNews(1, 1000, "", "all");
            if (allNews != null) {
                for (News news : allNews) {
                    String combined = (news.getTitle() + " " + news.getDescription()).toLowerCase();
                    String[] words = combined.split("\\W+");
                    for (String word : words) {
                        if (!word.isEmpty() && word.startsWith(prefix)) {
                            suggestions.add(word);
                        }
                    }
                }
            }

            // Load top search frequency terms from MongoDB
            try {
                MongoDatabase db = MongoDBConnection.getDatabase();
                MongoCollection<Document> freqCollection = db.getCollection("search_frequency");

                freqCollection.find(Filters.regex("term", "^" + prefix))
                        .forEach(doc -> suggestions.add(doc.getString("term")));
            } catch (Exception e) {
                System.err.println("Error fetching search frequencies: " + e.getMessage());
            }

            List<String> sorted = new ArrayList<>(suggestions);
            Collections.sort(sorted);

            return (sorted.size() > limit) ? sorted.subList(0, limit) : sorted;

        } catch (Exception e) {
            System.err.println("Error generating suggestions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Optional: Get top searched terms (for analytics)
     */
    public List<String> getTopSearches(int limit) {
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("search_frequency");

            List<String> topTerms = new ArrayList<>();
            collection.find()
                    .sort(new Document("count", -1))
                    .limit(limit)
                    .forEach(doc -> topTerms.add(doc.getString("term")));
            return topTerms;

        } catch (Exception e) {
            System.err.println("Error fetching top searches: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
