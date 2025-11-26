package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.News;

@Service
public class AutoCompleteService {

    @Autowired
    private NewsService newsService;

    public List<String> getSuggestions(String prefix, int limit) {

        try {
            // Empty prefix → return nothing
            if (prefix == null || prefix.trim().isEmpty()) {
                return Collections.emptyList();
            }

            prefix = prefix.toLowerCase();

            // Load news
            // Load news (fetch a reasonable amount for suggestions, e.g., 1000)
            List<News> allNews = newsService.getAllNews(1, 1000, "", "all");
            if (allNews == null || allNews.isEmpty()) {
                System.out.println("No news available!");
                return Collections.emptyList();
            }

            Set<String> suggestions = new HashSet<>();

            // Extract words from news
            for (News news : allNews) {
                String title = (news.getTitle() != null) ? news.getTitle() : "";
                String description = (news.getDescription() != null) ? news.getDescription() : "";
                String combined = (title + " " + description).toLowerCase();

                if (!combined.isEmpty()) {
                    // \W+ splits on ANY non-word character
                    String[] words = combined.split("\\W+");

                    for (String word : words) {
                        if (!word.isEmpty() && word.startsWith(prefix)) {
                            suggestions.add(word);
                        }
                    }
                }
            }

            // Convert to list and sort
            List<String> sortedList = new ArrayList<>(suggestions);
            Collections.sort(sortedList);

            // Apply limit
            if (sortedList.size() > limit) {
                return sortedList.subList(0, limit);
            }

            return sortedList;

        } catch (Exception e) {
            System.out.println("AutoCompleteService ERROR → " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
