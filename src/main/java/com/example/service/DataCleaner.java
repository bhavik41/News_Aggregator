package com.example.service;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;

public class DataCleaner {

    // Fields we expect in the articles collection
    private static final List<String> FIELDS = Arrays.asList(
            "Source", "Section", "Headline", "Description", "Time", "Category", "Link", "ImageLink"
    );

    public static void main(String[] args) {
        System.out.println("Starting DataCleaner...");
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> coll = db.getCollection("articles");

        int updated = 0;
        for (Document doc : coll.find()) {
            Document updates = new Document();
            ObjectId id = doc.getObjectId("_id");

            for (String field : FIELDS) {
                Object raw = doc.get(field);
                if (raw == null) continue;
                if (!(raw instanceof String)) continue;
                String s = (String) raw;
                String cleaned = cleanField(s);
                if (!cleaned.equals(s)) {
                    updates.append(field, cleaned);
                }
            }

            if (!updates.isEmpty()) {
                coll.updateOne(Filters.eq("_id", id), new Document("$set", updates));
                updated++;
                System.out.println("Updated doc: " + id.toHexString() + " -> " + updates.toJson());
            }
        }

        System.out.println("DataCleaner finished. Documents updated: " + updated);
        MongoDBConnection.close();
    }

    private static String cleanField(String s) {
        if (s == null) return null;
        String out = s.trim();

        // If value looks like a JSON escaped string (e.g. \"...\") remove backslash-escapes first
        out = out.replace("\\\"", "\"");

        // Remove surrounding double-quotes if present
        if (out.length() >= 2 && out.startsWith("\"") && out.endsWith("\"")) {
            out = out.substring(1, out.length() - 1);
        }

        // Trim again and return
        return out.trim();
    }
}
