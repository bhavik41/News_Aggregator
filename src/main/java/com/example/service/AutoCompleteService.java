package com.example.service;

import com.example.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AutoCompleteService {

    @Autowired
    private NewsService newsService;

    public List<String> getSuggestions(String prefix, int limit) {
        try {
            if (prefix == null || prefix.isEmpty()) return Collections.emptyList();

            prefix = prefix.toLowerCase();
            Set<String> wordsSet = new HashSet<>();

            List<News> allNews = newsService.getAllNews();

            if (allNews == null) {
                System.out.println("News list is null!");
                return Collections.emptyList();
            }

            for (News news : allNews) {
                String title = news.getTitle() != null ? news.getTitle() : "";
                String description = news.getDescription() != null ? news.getDescription() : "";
                String content = title + " " + description;

                if (!content.isEmpty()) {
                    String[] words = content.toLowerCase().split("\\W+");
                    for (String word : words) {
                        if (!word.isEmpty() && word.startsWith(prefix)) {
                            wordsSet.add(word);
                        }
                    }
                }
            }

            List<String> suggestions = new ArrayList<>(wordsSet);
            Collections.sort(suggestions);

            if (suggestions.size() > limit) {
                return suggestions.subList(0, limit);
            }

            return suggestions;

        } catch (Exception e) {
            System.out.println("Error in AutoCompleteService: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}