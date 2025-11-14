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

    /**
     * Detect news articles whose title or description matches the given regex pattern.
     *
     * @param regex the regex pattern to search for
     * @return list of matching news articles
     */
    public List<News> detectPattern(String regex) {
        List<News> allNews = newsService.getAllNews();
        List<News> matchedNews = new ArrayList<>();

        if (allNews == null || regex == null || regex.isEmpty()) {
            return matchedNews;
        }

        try {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            for (News news : allNews) {
                String title = news.getTitle() != null ? news.getTitle() : "";
                String description = news.getDescription() != null ? news.getDescription() : "";

                if (pattern.matcher(title).find() || pattern.matcher(description).find()) {
                    matchedNews.add(news);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in PatternDetectionService: " + e.getMessage());
            e.printStackTrace();
        }

        return matchedNews;
    }
}