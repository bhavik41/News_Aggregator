package com.example.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.example.model.News;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@Service
public class NewsService {

    public List<News> getAllNews() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("articles");

        // Allowed categories (case-insensitive)
        Set<String> allowedCategories = new HashSet<>(Arrays.asList(
            "Top stories",
            "Trending",
            "Politics",
            "World",
            "Technology",
            "News" // include current documents
        ));

        List<News> newsList = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                News news = new News();

                // Clean all string fields
                String id = doc.getObjectId("_id").toHexString();
                String title = trimQuotes(doc.getString("Headline"));
                String source = trimQuotes(doc.getString("Source"));
                String link = trimQuotes(doc.getString("Link"));
                String date = trimQuotes(doc.getString("Time"));
                String section = trimQuotes(doc.getString("Section"));
                String imageLink = trimQuotes(doc.getString("ImageLink"));
                String description = trimQuotes(doc.getString("Description"));
                String category = trimQuotes(doc.getString("Category"));

                // Only add if category matches allowed categories (case-insensitive)
                if (category != null && allowedCategories.stream().anyMatch(c -> c.equalsIgnoreCase(category))) {
                    news.setId(id);
                    news.setTitle(title);
                    news.setSource(source);
                    news.setLink(link);
                    news.setDate(date);
                    news.setSection(section);
                    news.setImageLink(imageLink);
                    news.setDescription(description);
                    news.setCategory(category);

                    newsList.add(news);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

    // Helper: remove starting/ending quotes and trim whitespace
    private String trimQuotes(String value) {
        if (value == null) return null;
        return value.replaceAll("^\"|\"$", "").trim();
    }
}
