package com.example.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.TextAnalysisService;

@RestController
@RequestMapping("/api/word-frequency") // base path for this API
public class TextAnalysisController {

    @Autowired
    private TextAnalysisService textAnalysisService;

    /**
     * POST /api/frequency
     * Accepts raw text in request body and returns word frequency map.
     */
    @PostMapping
    public Map<String, Integer> analyzeText(@RequestBody String text) {
        return textAnalysisService.getWordFrequency(text);
    }
}
