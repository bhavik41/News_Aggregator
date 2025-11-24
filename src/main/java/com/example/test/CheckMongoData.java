package com.example.test;

import org.bson.Document;
import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Simple test to check MongoDB connection and count BBC articles
 */
public class CheckMongoData {
    public static void main(String[] args) {
        try {
            System.out.println("Connecting to MongoDB...");
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("articles");
            
            // Count all documents
            long totalCount = collection.countDocuments();
            System.out.println("Total articles in database: " + totalCount);
            
            // Count BBC articles
            long bbcCount = collection.countDocuments(new Document("Source", "BBC"));
            System.out.println("BBC articles in database: " + bbcCount);
            
            // Show sample BBC articles
            if (bbcCount > 0) {
                System.out.println("\nSample BBC articles:");
                collection.find(new Document("Source", "BBC"))
                    .limit(5)
                    .forEach(doc -> {
                        System.out.println("  - " + doc.getString("Headline"));
                    });
            } else {
                System.out.println("\n⚠️ NO BBC ARTICLES FOUND IN DATABASE!");
            }
            
            MongoDBConnection.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
