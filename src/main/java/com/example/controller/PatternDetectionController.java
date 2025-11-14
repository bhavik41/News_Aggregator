package com.example.controller;

import com.example.model.News;
import com.example.service.PatternDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pattern-detection")
@CrossOrigin("*")
public class PatternDetectionController {

    @Autowired
    private PatternDetectionService patternDetectionService;

    /**
     * Example request:
     * GET /api/pattern-detection?regex=Trump
     *
     * @param regex the regex pattern to detect
     * @return list of news articles matching the pattern
     */
    @GetMapping
    public List<News> detectPattern(@RequestParam String regex) {
        return patternDetectionService.detectPattern(regex);
    }
}