package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.News;
import com.example.service.RankedArticlesService;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class RankedArticlesController {

    @Autowired
    private RankedArticlesService rankedArticlesService;

    @GetMapping("/ranked-articles")
    public List<News> getRankedArticles() {
        return rankedArticlesService.getRankedNews();
    }
}
