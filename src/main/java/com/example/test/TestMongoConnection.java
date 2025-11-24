package com.example.test;

import org.bson.Document;
import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TestMongoConnection {
    public static void main(String[] args) {
        try {
            System.out.println("Testing MongoDB Connection...");
            
            // Get database connection
            MongoDatabase database = MongoDBConnection.getDatabase();
            System.out.println("✅ Connected to database: " + database.getName());
            
            // Get articles collection
            MongoCollection<Document> collection = database.getCollection("articles");
            System.out.println("✅ Accessed collection: articles");
            
            // Count documents
            long count = collection.countDocuments();
            System.out.println("📊 Total documents in 'articles' collection: " + count);
            
            // Test insert
            Document testDoc = new Document("Source", "TEST")
                    .append("Headline", "Test Article")
                    .append("Link", "http://test.com/" + System.currentTimeMillis())
                    .append("Section", "Test")
                    .append("Description", "Test description")
                    .append("Time", "Just now")
                    .append("Category", "Test")
                    .append("ImageLink", "");
            
            collection.insertOne(testDoc);
            System.out.println("✅ Successfully inserted test document");
            
            // Count again
            count = collection.countDocuments();
            System.out.println("📊 Total documents after insert: " + count);
            
            // List some documents
            System.out.println("\n📄 Sample documents:");
            collection.find().limit(5).forEach(doc -> {
                System.out.println("  - " + doc.getString("Source") + ": " + doc.getString("Headline"));
            });
            
            System.out.println("\n✅ MongoDB connection test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Error testing MongoDB connection:");
            e.printStackTrace();
        }
    }
}
