package com.example.service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.example.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@Service
public class SpellCheckService {

    private final Set<String> dictionaryWords = new HashSet<>();
    private final Map<String, Integer> wordFrequency = new HashMap<>();
    private static final int MIN_WORD_LENGTH = 2;
    private static final int MAX_SUGGESTIONS = 5;

    public SpellCheckService() {
        initializeDictionary();
    }

    /**
     * Initialize dictionary from MongoDB articles collection
     */
    private void initializeDictionary() {
        try {
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> articlesCollection = database.getCollection("articles");

            long startTime = System.currentTimeMillis();

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

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✅ Loaded " + dictionaryWords.size() + " unique words from news articles in " + duration + "ms");

        } catch (Exception e) {
            System.err.println("❌ Error loading words from MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add words from text to dictionary with frequency tracking
     */
    private void addWordsToDictionary(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        // Extract words: lowercase, remove special characters
        String[] words = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim()
                .split("\\s+");

        for (String word : words) {
            if (!word.isEmpty() && word.length() >= MIN_WORD_LENGTH) {
                dictionaryWords.add(word);
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
    }

    /**
     * Main spell check method
     * Returns map with status, original text, and wrong words with suggestions
     */
    public Map<String, Object> checkSpelling(String inputText) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> wrongWordsWithSuggestions = new ArrayList<>();

        if (inputText == null || inputText.trim().isEmpty()) {
            result.put("status", "error");
            result.put("message", "Input text cannot be empty");
            return result;
        }

        // Clean and split text into words
        String cleanedText = inputText.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim();
        String[] words = cleanedText.split("\\s+");

        // Check each word
        for (String word : words) {
            if (word.isEmpty() || word.length() < MIN_WORD_LENGTH) {
                continue;
            }

            // If word not in dictionary, find suggestion
            if (!dictionaryWords.contains(word)) {
                String suggestion = findClosestWord(word);
                if (suggestion != null && !suggestion.isEmpty()) {
                    Map<String, String> wrongWord = new LinkedHashMap<>();
                    wrongWord.put("wrong_word", word);
                    wrongWord.put("suggestion", suggestion);
                    wrongWordsWithSuggestions.add(wrongWord);
                }
            }
        }

        result.put("status", "success");
        result.put("input_text", inputText);
        result.put("has_errors", !wrongWordsWithSuggestions.isEmpty());
        result.put("error_count", wrongWordsWithSuggestions.size());
        result.put("wrong_words", wrongWordsWithSuggestions);

        return result;
    }

    /**
     * Find closest matching word using Levenshtein distance
     * Prioritizes words by frequency
     */
    private String findClosestWord(String word) {
        if (dictionaryWords.isEmpty()) {
            return null;
        }

        String closestWord = "";
        int minDistance = Integer.MAX_VALUE;

        for (String dictWord : dictionaryWords) {
            // Skip if word length differs by more than 3 characters
            if (Math.abs(word.length() - dictWord.length()) > 3) {
                continue;
            }

            int distance = levenshteinDistance(word, dictWord);

            // Prioritize closer matches or more frequent words at same distance
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = dictWord;
            } else if (distance == minDistance && distance <= 2) {
                // If same distance, pick more frequent word
                int currentFreq = wordFrequency.getOrDefault(dictWord, 0);
                int closestFreq = wordFrequency.getOrDefault(closestWord, 0);
                if (currentFreq > closestFreq) {
                    closestWord = dictWord;
                }
            }
        }

        // Only return if distance is reasonable (<=2)
        if (minDistance <= 2 && !closestWord.isEmpty()) {
            return closestWord;
        }

        return null;
    }

    /**
     * Calculate Levenshtein distance between two strings
     * Lower distance = more similar words
     */
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        // Initialize base cases
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        // Fill the matrix
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,      // deletion
                                dp[i][j - 1] + 1       // insertion
                        ),
                        dp[i - 1][j - 1] + cost         // substitution
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    /**
     * Get multiple suggestions for a misspelled word
     */
    public List<String> getSuggestions(String word, int count) {
        if (word == null || word.isEmpty() || dictionaryWords.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> suggestions = dictionaryWords.stream()
                .filter(w -> Math.abs(w.length() - word.length()) <= 3)
                .map(w -> new AbstractMap.SimpleEntry<>(w, levenshteinDistance(word, w)))
                .filter(e -> e.getValue() <= 2)
                .sorted((a, b) -> {
                    if (a.getValue().equals(b.getValue())) {
                        // Same distance: prioritize by frequency
                        return wordFrequency.getOrDefault(b.getKey(), 0)
                                .compareTo(wordFrequency.getOrDefault(a.getKey(), 0));
                    }
                    return a.getValue().compareTo(b.getValue());
                })
                .limit(Math.min(count, MAX_SUGGESTIONS))
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());

        return suggestions;
    }
}