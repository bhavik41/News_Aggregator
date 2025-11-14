package com.example.service;

import com.example.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class RankedArticlesService {

    @Autowired
    private NewsService newsService;

    public List<News> getRankedNews() {
        List<News> allNews = newsService.getAllNews();

        if (allNews != null) {
            // Sort by title length descending
            Collections.sort(allNews, new Comparator<News>() {
                @Override
                public int compare(News n1, News n2) {
                    String t1 = n1.getTitle() != null ? n1.getTitle() : "";
                    String t2 = n2.getTitle() != null ? n2.getTitle() : "";
                    return t2.length() - t1.length(); // descending
                }
            });
        }

        return allNews;
    }
}
