package com.example.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.bson.Document;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

public class CSVtoMongoUploader {

    /**
     * Upload CSV data to MongoDB, avoiding duplicates using the URL as a unique key.
     *
     * @param csvPath  Path to CSV file
     * @param seenUrls Set of URLs already processed
     */
    public static void uploadCSV(String csvPath, Set<String> seenUrls) {
        MongoDatabase database = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = database.getCollection("articles");

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length < 8) continue; // skip invalid rows

                String url = data[6]; // Link column

                // Skip if already uploaded
                if (seenUrls.contains(url)) continue;

                Document doc = new Document("Source", data[0])
                        .append("Section", data[1])
                        .append("Headline", data[2])
                        .append("Description", data[3])
                        .append("Time", data[4])
                        .append("Category", data[5])
                        .append("Link", url)
                        .append("ImageLink", data[7]);

                // Upsert to MongoDB (insert if not exists)
                collection.updateOne(
                        Filters.eq("Link", url),
                        new Document("$setOnInsert", doc),
                        new UpdateOptions().upsert(true)
                );

                // Mark URL as seen
                seenUrls.add(url);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
