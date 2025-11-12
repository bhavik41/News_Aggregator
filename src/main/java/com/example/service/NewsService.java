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

@Service  // <-- This registers the class as a Spring bean
public class NewsService {

    public List<News> getAllNews() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("articles");
        List<News> newsList = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                News news = new News();

                news.setId(doc.getObjectId("_id").toHexString());
                news.setTitle(doc.getString("Headline"));
                news.setSource(doc.getString("Source"));
                news.setLink(doc.getString("Link"));
                news.setDate(doc.getString("Time"));
                news.setSection(doc.getString("Section"));
                news.setImageLink(doc.getString("ImageLink"));
                news.setDescription(doc.getString("Description"));
                news.setCategory(doc.getString("Category"));

                newsList.add(news);
            }
        } catch (Exception e) {
            e.printStackTrace();  // Optional: log errors properly in production
        }

        return newsList;
    }
}
