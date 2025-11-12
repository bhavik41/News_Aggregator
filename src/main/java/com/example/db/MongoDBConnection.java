package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnection {

    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // Force TLS 1.2 to avoid SSL handshake issues
                System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

                // ✅ Load environment variables from .env
                Dotenv dotenv = Dotenv.configure()
                        .directory("./") // ensure it looks in your project root
                        .load();

                String url = dotenv.get("MONGO_URI");
                if (url == null || url.isEmpty()) {
                    throw new IllegalStateException("❌ MONGO_URI not found in .env file");
                }

                // ✅ Connect to MongoDB Atlas using URI from .env
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
