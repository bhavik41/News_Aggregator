package com.example.controller;

import com.example.service.SpellCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/spellcheck")
public class SpellCheckController {

    @Autowired
    private SpellCheckService spellCheckService;

    // POST: /api/spellcheck
    @PostMapping
    public Map<String, Object> checkSpelling(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        return spellCheckService.checkSpelling(text);
    }
}
