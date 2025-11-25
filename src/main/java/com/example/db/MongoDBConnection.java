package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
// dotenv intentionally not loaded in production to avoid runtime issues
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;



public class MongoDBConnection {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Load .env variables (NOT loaded here in order to avoid side-effects in production)

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // 1️⃣ Try system environment variables (common names)
                String mongoUri = System.getenv("MONGODB_URI");

                // 2️⃣ Try alternate environment variable name
                if (mongoUri == null || mongoUri.isEmpty()) {
                    mongoUri = System.getenv("MONGO_URI");
                }

                // 3️⃣ Try Spring property environment variable
                if (mongoUri == null || mongoUri.isEmpty()) {
                    mongoUri = System.getenv("SPRING_DATA_MONGODB_URI");
                }

                if (mongoUri == null || mongoUri.isEmpty()) {
                    // try loading from classpath application.properties (spring property)
                    try (InputStream in = MongoDBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
                        if (in != null) {
                            Properties props = new Properties();
                            try {
                                props.load(in);
                                String propUri = props.getProperty("spring.data.mongodb.uri");
                                if (propUri != null && !propUri.isEmpty()) {
                                    mongoUri = propUri;
                                    System.out.println("ℹ️ Loaded Mongo URI from application.properties");
                                }
                            } catch (IOException ioe) {
                                System.err.println("⚠️ Could not load application.properties: " + ioe.getMessage());
                            }
                        }
                    } catch (IOException ioe) {
                        System.err.println("⚠️ Error opening application.properties resource: " + ioe.getMessage());
                    }
                }

                if (mongoUri == null || mongoUri.isEmpty()) {
                    throw new IllegalStateException(
                        "MongoDB connection string not found. " +
                        "Please set MONGODB_URI (or SPRING_DATA_MONGODB_URI) in system env, in .env, or set spring.data.mongodb.uri in application.properties."
                    );
                }

                System.out.println("✅ Connecting to MongoDB using provided URI...");
                mongoClient = MongoClients.create(mongoUri);
                // Use a specific database; keep the project default name
                database = mongoClient.getDatabase("newsAggregatorDB");
                System.out.println("✅ Successfully connected to MongoDB!");

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