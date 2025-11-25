package com.example.controller;

import com.example.service.SearchFrequencyService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchFrequencyController {

    private final SearchFrequencyService searchFrequencyService;

    public SearchFrequencyController(SearchFrequencyService searchFrequencyService) {
        this.searchFrequencyService = searchFrequencyService;
    }

    // POST: record a search keyword
    @PostMapping("/record")
    public Map<String, Object> recordSearch(@RequestBody Map<String, String> payload) {
        String keyword = payload.get("keyword"); // get keyword from request body
        if (keyword == null || keyword.trim().isEmpty()) {
            return Map.of("status", "error", "message", "Keyword is empty");
        }
        searchFrequencyService.recordSearch(keyword);
        return Map.of("status", "success", "keyword", keyword);
    }

    // GET: top 10 searched keywords
    @GetMapping("/top")
    public Map<String, Object> getTopWords() {
        return searchFrequencyService.getFrequency(null);
    }
}
