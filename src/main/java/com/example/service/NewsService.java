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

    public List<News> getAllNews() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("articles");

        List<News> newsList = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                News news = new News();

                // _id can be ObjectId or String depending on how data was inserted
                Object idObj = doc.get("_id");
                if (idObj != null) {
                    try {
                        // prefer ObjectId hex string
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
                news.setSection(trimQuotes(safeGetString(doc, "Section")));
                news.setImageLink(trimQuotes(safeGetString(doc, "ImageLink")));
                news.setDescription(trimQuotes(safeGetString(doc, "Description")));
                news.setCategory(trimQuotes(safeGetString(doc, "Category")));

                newsList.add(news);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

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

    // Helper: Remove starting/ending quotes and trim whitespace
    private String trimQuotes(String value) {
        if (value == null) return null;
        return value.replaceAll("^\"|\"$", "").trim();
    }

}