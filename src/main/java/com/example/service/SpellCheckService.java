package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@Service
public class SpellCheckService {

    private final Set<String> dictionaryWords = new HashSet<>();

    public SpellCheckService() {
        try {
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> articlesCollection = database.getCollection("articles");

            // Load words from Headline and Description fields
            try (MongoCursor<Document> cursor = articlesCollection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String headline = doc.getString("Headline");
                    String description = doc.getString("Description");

                    if (headline != null) addWordsToDictionary(headline);
                    if (description != null) addWordsToDictionary(description);
                }
            }

            System.out.println("✅ Loaded " + dictionaryWords.size() + " unique words from news articles");

        } catch (Exception e) {
            System.err.println("❌ Error loading words from MongoDB: " + e.getMessage());
        }
    }

    private void addWordsToDictionary(String text) {
        // Lowercase and remove non-alphabetic characters
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", " ").split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                dictionaryWords.add(word);
            }
        }
    }

    public Map<String, Object> checkSpelling(String inputText) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> wrongWordsWithSuggestions = new ArrayList<>();

        if (inputText == null || inputText.trim().isEmpty()) {
            result.put("status", "error");
            result.put("message", "Input text cannot be empty");
            return result;
        }

        String cleanedText = inputText.toLowerCase().replaceAll("[^a-zA-Z\\s]", " ").trim();
        String[] words = cleanedText.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty() && !dictionaryWords.contains(word)) {
                String suggestion = findClosestWord(word);
                Map<String, String> wrongWord = new HashMap<>();
                wrongWord.put("wrong_word", word);
                wrongWord.put("suggestion", suggestion);
                wrongWordsWithSuggestions.add(wrongWord);
            }
        }

        result.put("status", "success");
        result.put("input_text", inputText);
        result.put("wrong_words", wrongWordsWithSuggestions);

        return result;
    }

    private String findClosestWord(String word) {
        String closestWord = "";
        int minDistance = Integer.MAX_VALUE;

        for (String dictWord : dictionaryWords) {
            int distance = levenshteinDistance(word, dictWord);
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = dictWord;
            }
        }

        return closestWord; // always return closest word
    }

    // Levenshtein distance algorithm
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,
                                 dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }
}
