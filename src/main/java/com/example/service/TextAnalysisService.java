package com.example.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TextAnalysisService {

    /**
     * Calculates word frequency in the given text.
     * @param text Input text
     * @return Map of word -> frequency
     */
    public Map<String, Integer> getWordFrequency(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        if (text == null || text.isEmpty()) {
            return frequencyMap; // empty map
        }

        // Remove punctuation and convert to lowercase
        String cleanedText = text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();

        // Split text into words
        String[] words = cleanedText.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            }
        }

        return frequencyMap;
    }
}
