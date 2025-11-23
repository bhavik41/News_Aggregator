package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnection {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // Load .env file if available
                Dotenv dotenv = Dotenv.configure()
                        .ignoreIfMissing() // Won't crash if .env missing
                        .load();

                // Try environment variable first
                String mongoUri = System.getenv("MONGO_URI");

                // Fallback to .env file
                if (mongoUri == null || mongoUri.isEmpty()) {
                    mongoUri = dotenv.get("MONGO_URI");
                }

                // Throw error if still missing
                if (mongoUri == null || mongoUri.isEmpty()) {
                    throw new IllegalStateException(
                            "MongoDB connection string not found. Please set MONGO_URI in environment variables or in the .env file."
                    );
                }

                System.out.println("Loaded MONGO_URI from environment or .env");

                // Connect to MongoDB
                System.out.println("✅ Connecting to MongoDB Atlas...");
                mongoClient = MongoClients.create(mongoUri);
                database = mongoClient.getDatabase("newsAggregatorDB");
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