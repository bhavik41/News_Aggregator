package com.example.controller;

import com.example.service.SearchFrequencyService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/frequency")
@CrossOrigin("*")
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
