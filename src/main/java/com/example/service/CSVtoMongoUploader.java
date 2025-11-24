package com.example.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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
        System.out.println("\n📤 === CSV TO MONGODB UPLOAD STARTED ===");
        System.out.println("📄 CSV File: " + csvPath);
        System.out.println("🔗 Pre-existing seen URLs: " + seenUrls.size());
        
        try {
            MongoDatabase database = MongoDBConnection.getDatabase();
            System.out.println("✅ MongoDB connection acquired");
            
            MongoCollection<Document> collection = database.getCollection("articles");
            System.out.println("✅ Collection 'articles' accessed");
            
            long initialCount = collection.countDocuments();
            System.out.println("📊 Initial document count in DB: " + initialCount);

            int lineNumber = 0;
            int skippedInvalid = 0;
            int skippedDuplicate = 0;
            int inserted = 0;
            
            try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
                String line;
                line = br.readLine(); // skip header
                lineNumber++;
                System.out.println("Header: " + line);

                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    String[] data = parseCSVLine(line);

                    if (data.length < 8) {
                        skippedInvalid++;
                        if (skippedInvalid <= 3) {
                            System.out.println("⚠️ Line " + lineNumber + " invalid (only " + data.length + " fields)");
                        }
                        continue;
                    }

                    String url = data[6]; // Link column

                    // Skip if already uploaded
                    if (seenUrls.contains(url)) {
                        skippedDuplicate++;
                        continue;
                    }

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
                    
                    inserted++;
                    
                    // Show first few inserts
                    if (inserted <= 5) {
                        System.out.println("✅ Inserted: " + data[2].substring(0, Math.min(50, data[2].length())));
                    }

                    // Mark URL as seen
                    seenUrls.add(url);
                }

            } catch (IOException e) {
                System.err.println("❌ IO Error reading CSV:");
                e.printStackTrace();
                throw e;
            }
            
            long finalCount = collection.countDocuments();
            System.out.println("\n📊 === UPLOAD SUMMARY ===");
            System.out.println("Total lines processed: " + (lineNumber - 1));
            System.out.println("Skipped (invalid): " + skippedInvalid);
            System.out.println("Skipped (duplicate): " + skippedDuplicate);
            System.out.println("Inserted to DB: " + inserted);
            System.out.println("DB count before: " + initialCount);
            System.out.println("DB count after: " + finalCount);
            System.out.println("Net change: " + (finalCount - initialCount));
            System.out.println("=========================\n");
            
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in uploadCSV:");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parse a CSV line into fields, handling quoted fields containing commas.
     * This is a lightweight parser sufficient for the CSVWriter format used in this project.
     */
    private static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null || line.isEmpty()) return new String[0];

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }
}
