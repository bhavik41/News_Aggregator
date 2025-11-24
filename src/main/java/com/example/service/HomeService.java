package com.example.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.example.model.News;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@Service
public class HomeService {

    // Allowed sections (case-insensitive)
    private static final List<String> ALLOWED_SECTIONS = Arrays.asList(
        "Top Stories",
        "MORE TO EXPLORE",
        "MOST WATCHED",
        "Technology",
        "politics",
        "TRENDING"
    );

    public Map<String, List<News>> getNewsGroupedBySection() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("articles");

        // Initialize map with all allowed sections in order
        Map<String, List<News>> groupedNews = new LinkedHashMap<>();
        for (String section : ALLOWED_SECTIONS) {
            groupedNews.put(section, new ArrayList<>());
        }

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String section = trimQuotes(doc.getString("Section"));

                // Check if section matches any allowed section (case-insensitive)
                if (section != null) {
                    String matchedSection = findMatchingSection(section);

                    if (matchedSection != null) {
                            News news = new News();

                            // Safely handle _id which may be ObjectId or String
                            Object idObj = doc.get("_id");
                            if (idObj != null) {
                                try {
                                    if (idObj instanceof org.bson.types.ObjectId) {
                                        news.setId(((org.bson.types.ObjectId) idObj).toHexString());
                                    } else {
                                        news.setId(idObj.toString());
                                    }
                                } catch (Exception ex) {
                                    news.setId(idObj.toString());
                                }
                            }

                            news.setTitle(trimQuotes(safeGetString(doc, "Headline")));
                            news.setSource(trimQuotes(safeGetString(doc, "Source")));
                            news.setLink(trimQuotes(safeGetString(doc, "Link")));
                            news.setDate(trimQuotes(safeGetString(doc, "Time")));
                            news.setSection(matchedSection);
                            news.setImageLink(trimQuotes(safeGetString(doc, "ImageLink")));
                            news.setDescription(trimQuotes(safeGetString(doc, "Description")));
                            news.setCategory(trimQuotes(safeGetString(doc, "Category")));

                            groupedNews.get(matchedSection).add(news);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return groupedNews;
    }

    // Helper: Find matching section (case-insensitive)
    private String findMatchingSection(String value) {
        for (String allowedSection : ALLOWED_SECTIONS) {
            if (value.equalsIgnoreCase(allowedSection)) {
                return allowedSection;
            }
        }
        return null;
    }

    // Helper: Remove starting/ending quotes and trim whitespace
    private String trimQuotes(String value) {
        if (value == null) return null;
        return value.replaceAll("^\"|\"$", "").trim();
    }

    // Safe getter for any field from Document as String
    private String safeGetString(Document doc, String key) {
        try {
            Object o = doc.get(key);
            if (o == null) return null;
            if (o instanceof String) return (String) o;
            return o.toString();
        } catch (Exception e) {
            return null;
        }
    }
}