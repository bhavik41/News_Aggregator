package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // Force TLS 1.2 to avoid SSL handshake issues
                System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

                // MongoDB connection string
                String url = "mongodb+srv://dhruvipatel:dhruviVpatel37@cluster0.ky8w9m9.mongodb.net/?appName=Cluster0";

                MongoClient client = MongoClients.create(url);
                database = client.getDatabase("newsAggregatorDB");

                System.out.println("✅ Connected to MongoDB Atlas successfully!");
            } catch (Exception e) {
                System.err.println("❌ Failed to connect to MongoDB Atlas:");
                e.printStackTrace();
            }
        }
        return database;
    }
}
