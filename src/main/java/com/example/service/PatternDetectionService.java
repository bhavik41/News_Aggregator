package com.example.service;

import com.example.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PatternDetectionService {

    @Autowired
    private NewsService newsService;

    public List<News> detectPattern(String regex) {
        List<News> allNews = newsService.getAllNews(1, Integer.MAX_VALUE, "", "all");
        List<News> matchedNews = new ArrayList<>();

        try {
            if (allNews == null || allNews.isEmpty()) {
                System.out.println("No news found!");
                return matchedNews;
            }

            if (regex == null || regex.trim().isEmpty()) {
                System.out.println("Empty regex!");
                return matchedNews;
            }

            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            for (News news : allNews) {

                String title = (news.getTitle() != null) ? news.getTitle() : "";
                String description = (news.getDescription() != null) ? news.getDescription() : "";

                // Match either title or description
                if (pattern.matcher(title).find() || pattern.matcher(description).find()) {
                    matchedNews.add(news);
                }
            }

        } catch (Exception e) {
            System.out.println("PatternDetectionService ERROR → " + e.getMessage());
        }

        return matchedNews;
    }
}
