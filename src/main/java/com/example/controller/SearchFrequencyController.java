package com.example.controller;

import java.util.Map;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.SearchFrequencyService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/search-frequency")
public class SearchFrequencyController {

    private final SearchFrequencyService searchFrequencyService;

    public SearchFrequencyController(SearchFrequencyService searchFrequencyService) {
        this.searchFrequencyService = searchFrequencyService;
    }

    @GetMapping
    public Map<String, Object> getSearchFrequency(HttpServletRequest request) {

        // fetch keyword manually (no reflection needed)
        String keyword = request.getParameter("keyword");

        return searchFrequencyService.getFrequency(keyword);
    }
}
