package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.example.model.News;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@Service
public class NewsService {

    public List<News> getAllNews(int page, int limit, String search, String sectionFilter) {

        MongoDatabase db;
        try {
            db = MongoDBConnection.getDatabase();
        } catch (Exception e) {
            // Fail fast with clear message so controller can handle and return meaningful response
            throw new RuntimeException("Failed to obtain MongoDB database: " + e.getMessage(), e);
        }
        MongoCollection<Document> collection = db.getCollection("articles");

        List<News> newsList = new ArrayList<>();

        // Build query filter
        Document query = new Document();
        
        // Add section filter if not "all"
        if (sectionFilter != null && !sectionFilter.equals("all") && !sectionFilter.isEmpty()) {
            query.append("Section", sectionFilter);
        }

        // Add search filter (regex search on Headline field)
        if (search != null && !search.isEmpty()) {
            query.append("Headline", new Document("$regex", search).append("$options", "i"));
        }

        // Calculate skip value for pagination
        int skip = (page - 1) * limit;

        System.out.println("DEBUG: Query filter: " + query.toJson());
        System.out.println("DEBUG: Page: " + page + ", Limit: " + limit + ", Skip: " + skip);

        try (MongoCursor<Document> cursor = collection.find(query).skip(skip).limit(limit).iterator()) {

            while (cursor.hasNext()) {
                Document doc = cursor.next();

                String section = trimQuotes(safeGetString(doc, "Section"));
                String title = trimQuotes(safeGetString(doc, "Headline"));

                News news = new News();

                Object idObj = doc.get("_id");
                if (idObj != null) {
                    news.setId(idObj.toString());
                }

                news.setTitle(title);
                news.setSource(trimQuotes(safeGetString(doc, "Source")));
                news.setLink(trimQuotes(safeGetString(doc, "Link")));
                news.setDate(trimQuotes(safeGetString(doc, "Time")));
                news.setSection(section);
                news.setImageLink(trimQuotes(safeGetString(doc, "ImageLink")));
                news.setDescription(trimQuotes(safeGetString(doc, "Description")));
                news.setCategory(trimQuotes(safeGetString(doc, "Category")));

                newsList.add(news);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("DEBUG: Returned " + newsList.size() + " items");
        return newsList;
    }

    private String safeGetString(Document doc, String key) {
        try {
            Object o = doc.get(key);
            if (o == null) return null;
            return o.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String trimQuotes(String value) {
        if (value == null) return null;
        return value.replaceAll("^\"|\"$", "").trim();
    }
}
