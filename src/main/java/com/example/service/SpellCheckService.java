package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SpellCheckService {

    private static final List<String> DICTIONARY = List.of(
            "hello", "world", "java", "spring", "crawler", "news", "project", "data"
    );

    public List<String> checkSpelling(String inputText) {
        List<String> wrongWords = new ArrayList<>();

        try {

            if (inputText == null || inputText.trim().isEmpty()) {
                throw new IllegalArgumentException("Input text cannot be empty");
            }

            String[] words = inputText.toLowerCase().split("\\s+");

            for (String word : words) {
                if (!DICTIONARY.contains(word)) {
                    wrongWords.add(word);
                }
            }

        } catch (Exception e) {
            System.out.println("SpellCheckService ERROR → " + e.getMessage());
        }

        return wrongWords;
    }
}
