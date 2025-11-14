package com.example.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.News;

@Service
public class RankedArticlesService {

    @Autowired
    private NewsService newsService;

    public List<News> getRankedNews() {
        List<News> allNews = newsService.getAllNews();

        if (allNews != null) {

            // Compute word frequency score for each article
            for (News news : allNews) {
                String content = "";
                if (news.getDescription() != null) content += news.getDescription();
                if (news.getTitle() != null) content += " " + news.getTitle(); // include title
                news.setScore(calculateWordFrequencyScore(content));
            }

            // Sort descending by score
            Collections.sort(allNews, (n1, n2) -> Double.compare(n2.getScore(), n1.getScore()));
        }

        return allNews;
    }

    // Helper method: calculate total frequency score of words
    private double calculateWordFrequencyScore(String text) {
        if (text.isEmpty()) return 0;

        String[] words = text.toLowerCase().split("\\W+"); // split by non-word characters
        Map<String, Integer> freq = new HashMap<>();

        for (String word : words) {
            if (!word.isEmpty()) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }

        // Total frequency (sum of all word occurrences)
        int total = 0;
        for (int count : freq.values()) total += count;

        return total;
    }
}
