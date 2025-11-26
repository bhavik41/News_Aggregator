package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
            throw new RuntimeException("Failed to obtain MongoDB database: " + e.getMessage(), e);
        }
        MongoCollection<Document> collection = db.getCollection("articles");

        List<News> newsList = new ArrayList<>();

        // Build query filter
        Document query = new Document();
        
        // Add section filter if not "all" - WITH CASE-INSENSITIVE MATCHING
        if (sectionFilter != null && !sectionFilter.trim().isEmpty() && !sectionFilter.equals("all")) {
            System.out.println("DEBUG: Adding section filter: " + sectionFilter);
            
            // Use case-insensitive regex for section matching
            String sectionPattern = "^" + Pattern.quote(sectionFilter.trim()) + "$";
            query.append("Section", new Document("$regex", sectionPattern).append("$options", "i"));
        }

        // Add search filter (regex search on Headline field)
        if (search != null && !search.trim().isEmpty()) {
            System.out.println("DEBUG: Adding search filter: " + search);
            query.append("Headline", new Document("$regex", search.trim()).append("$options", "i"));
        }

        // Calculate skip value for pagination
        int skip = (page - 1) * limit;

        System.out.println("===== NEWS SERVICE DEBUG =====");
        System.out.println("Query filter: " + query.toJson());
        System.out.println("Page: " + page + ", Limit: " + limit + ", Skip: " + skip);
        System.out.println("Section filter: '" + sectionFilter + "'");
        System.out.println("Search term: '" + search + "'");

        // Count total matching documents (for debugging)
        long totalCount = collection.countDocuments(query);
        System.out.println("Total matching documents in DB: " + totalCount);

        // MongoDB query execution order:
        // 1. Apply filter (query) - filters by section and search FIRST
        // 2. Apply skip - skips documents for pagination
        // 3. Apply limit - returns only 'limit' number of documents
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
            System.err.println("ERROR: Exception while fetching news: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returned " + newsList.size() + " items from page " + page);
        if (newsList.size() > 0) {
            System.out.println("Sample item - Section: " + newsList.get(0).getSection() + ", Title: " + newsList.get(0).getTitle());
        }
        System.out.println("==============================");
        
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