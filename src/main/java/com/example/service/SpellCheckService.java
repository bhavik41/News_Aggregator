package com.example.service;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class SpellCheckService {

    private static final List<String> DICTIONARY = List.of(
            "hello", "world", "java", "spring", "crawler", "news", "project", "data"
    );

    public Map<String, Object> checkSpelling(String inputText) {

        Map<String, Object> result = new LinkedHashMap<>();
        List<String> wrongWords = new ArrayList<>();

        try {

            if (inputText == null || inputText.trim().isEmpty()) {
                result.put("status", "error");
                result.put("message", "Input text cannot be empty");
                return result;
            }

            String[] words = inputText.toLowerCase().split("\\s+");

            for (String word : words) {
                if (!DICTIONARY.contains(word)) {
                    wrongWords.add(word);
                }
            }

            result.put("status", "success");
            result.put("input_text", inputText);
            result.put("wrong_words", wrongWords);
            result.put("count", wrongWords.size());

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }
}
