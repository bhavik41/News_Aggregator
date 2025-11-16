package com.example.controller;

import com.example.service.AutoCompleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
public class AutoCompleteController {

    @Autowired
    private AutoCompleteService autoService;

    @GetMapping("/test")
    public String test() {
        return "AutoComplete API is working!";
    }

    @PostMapping("/suggest")
    public List<String> suggest(@RequestBody String prefix) {
        return autoService.getSuggestions(prefix, 10);
    }
}
