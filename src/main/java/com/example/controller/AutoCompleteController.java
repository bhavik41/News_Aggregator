package com.example.controller;

import com.example.service.AutoCompleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
@CrossOrigin("*")
public class AutoCompleteController {

    @Autowired
    private AutoCompleteService autoCompleteService;

    // Example: /api/autocomplete?prefix=the&limit=10
    @GetMapping
    public List<String> getAutoComplete(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return autoCompleteService.getSuggestions(prefix, limit);
    }
}