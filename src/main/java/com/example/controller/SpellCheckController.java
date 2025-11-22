package com.example.controller;

import com.example.service.SpellCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpellCheckController {

    @Autowired
    private SpellCheckService spellCheckService;

    @PostMapping("/spellcheck")
    public Map<String, Object> checkSpelling(@RequestBody String text) {
        return spellCheckService.checkSpelling(text);
    }

    @GetMapping("/spellcheck/test")
    public Map<String, String> testSpellCheck() {
        return Map.of("status", "success", "message", "SpellCheck API is working!");
    }
}
