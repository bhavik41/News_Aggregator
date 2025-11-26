package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.service.SearchAutoCompleteService;

@RestController
@RequestMapping("/api")
public class SearchAutoController {

    @Autowired
    private SearchAutoCompleteService searchService;

    /**
     * Single API endpoint:
     * - Increments search frequency
     * - Returns autocomplete suggestions
     * Example: GET /api/search-auto?term=covid&limit=5
     */
    @GetMapping("/search-auto")
    public List<String> search(@RequestParam String term,
                               @RequestParam(defaultValue = "10") int limit) {
        return searchService.searchAndSuggest(term, limit);
    }
}
