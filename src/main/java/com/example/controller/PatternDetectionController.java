package com.example.controller;

import com.example.model.News;
import com.example.service.PatternDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pattern")
public class PatternDetectionController {

    @Autowired
    private PatternDetectionService patternService;

    @GetMapping("/test")
    public String test() {
        return "Pattern detection API is working!";
    }

    @PostMapping("/detect")
    public List<News> detect(@RequestBody String regex) {
        return patternService.detectPattern(regex);
    }
}
