package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.service.SearchAutoCompleteService;

@RestController
@RequestMapping("/api")
public class SearchAutoController {

    @Autowired
    private SearchAutoCompleteService searchService;

    /**
     * Get autocomplete suggestions (NO frequency increment)
     * Called on every keystroke
     * POST /api/search-suggest
     * Body: "covid" (plain text)
     * Response: [{ "term": "covid", "frequency": 15 }, ...]
     */
    @PostMapping("/search-suggest")
    public List<Map<String, Object>> getSuggestions(@RequestBody String term,
                                                     @RequestParam(defaultValue = "10") int limit) {
        return searchService.getSuggestions(term, limit);
    }

    /**
     * Increment search frequency when user actually searches
     * Called ONLY when user presses Enter or selects a suggestion
     * POST /api/search-increment
     * Body: "covid" (plain text)
     * Response: "Frequency incremented"
     */
    @PostMapping("/search-increment")
    public String incrementFrequency(@RequestBody String term) {
        searchService.incrementSearchFrequency(term);
        return "Frequency incremented for: " + term;
    }

    /**
     * Get top searched terms (for analytics)
     * GET /api/search-top?limit=10
     * Response: [{ "term": "covid", "frequency": 25 }, ...]
     */
    @GetMapping("/search-top")
    public List<Map<String, Object>> getTopSearches(@RequestParam(defaultValue = "10") int limit) {
        return searchService.getTopSearches(limit);
    }
}