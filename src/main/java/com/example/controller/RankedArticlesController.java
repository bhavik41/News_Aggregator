package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.News;
import com.example.service.RankedArticlesService;

@RestController
@RequestMapping("/api")
public class RankedArticlesController {

    @Autowired
    private RankedArticlesService rankedArticlesService;

    /**
     * Get all ranked articles
     */
    @GetMapping("/ranked-articles")
    public List<News> getRankedArticles(
            @RequestParam(name = "limit", defaultValue = "30") int limit) {
        List<News> rankedNews = rankedArticlesService.getRankedNews();
        
        // Return only the top 'limit' articles
        return rankedNews.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get top stories with pagination - dedicated endpoint for ranked news
     */
    @GetMapping("/news/top-stories")
    public List<News> getTopStories(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return rankedArticlesService.getTopRankedNews(page, limit);
    }
}