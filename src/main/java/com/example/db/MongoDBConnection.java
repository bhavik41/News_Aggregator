package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // Try to get MongoDB URI from environment variable first
                String mongoUri = System.getenv("MONGODB_URI");
                
                // Fallback to MONGO_URI if MONGODB_URI doesn't exist
                if (mongoUri == null || mongoUri.isEmpty()) {
                    mongoUri = System.getenv("MONGO_URI");
                }
                
                // If still null, throw a clear error
                if (mongoUri == null || mongoUri.isEmpty()) {
                    throw new IllegalStateException(
                        "MongoDB connection string not found. " +
                        "Please set MONGODB_URI or MONGO_URI environment variable."
                    );
                }
                
                System.out.println("✅ Connecting to MongoDB Atlas...");
                mongoClient = MongoClients.create(mongoUri);
                database = mongoClient.getDatabase("news_aggregator"); // Your database name
                System.out.println("✅ Successfully connected to MongoDB Atlas!");
                
            } catch (Exception e) {
                System.err.println("❌ Failed to connect to MongoDB Atlas:");
                e.printStackTrace();
                throw e;
            }
        }
        return database;
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed");
        }
    }
}
