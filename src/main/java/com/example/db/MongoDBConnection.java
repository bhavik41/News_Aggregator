package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;   // <-- REQUIRED
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnection {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Load .env variables
    private static final Dotenv dotenv = Dotenv.load();   // <-- REQUIRED

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                // 1️⃣ Try system environment variable first (VS Code / OS-level)
                String mongoUri = System.getenv("MONGODB_URI");

                // 2️⃣ Try MONGO_URI environment variable
                if (mongoUri == null || mongoUri.isEmpty()) {
                    mongoUri = dotenv.get("MONGO_URI");
                }

                // 3️⃣ Try loading from .env if no system variables were found
                if (mongoUri == null || mongoUri.isEmpty()) {
                    mongoUri = dotenv.get("MONGODB_URI");
                }

                // 4️⃣ Final checks: try additional env keys and application.properties
                if (mongoUri == null || mongoUri.isEmpty()) {
                    // try SPRING_DATA_MONGODB_URI as an env var
                    mongoUri = System.getenv("SPRING_DATA_MONGODB_URI");
                }

                if (mongoUri == null || mongoUri.isEmpty()) {
                    // try dotenv (.env)
                    mongoUri = dotenv.get("MONGODB_URI");
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