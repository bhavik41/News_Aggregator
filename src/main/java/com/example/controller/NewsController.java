package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.example.model.News;
import com.example.service.NewsService;

@RestController
@RequestMapping("/api/news")
@CrossOrigin("*")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
public ResponseEntity<List<News>> getAllNews(
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "limit", defaultValue = "10") int limit,
        @RequestParam(name = "search", defaultValue = "") String search,
        @RequestParam(name = "section", defaultValue = "all") String section
) {
    try {
        List<News> result = newsService.getAllNews(page, limit, search, section);
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        // Log the exception and return 500 with no body (client can see server logs)
        e.printStackTrace();
        return ResponseEntity.status(500).build();
    }
}
}
