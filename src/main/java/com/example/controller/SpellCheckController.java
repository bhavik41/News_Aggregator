package com.example.controller;

import com.example.service.SpellCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SpellCheckController {

    @Autowired
    private SpellCheckService spellCheckService;

    @PostMapping("/spellcheck")
    public List<String> checkSpelling(@RequestBody String text) {
        return spellCheckService.checkSpelling(text);
    }

    @GetMapping("/spellcheck/test")
    public String testSpellCheck() {
        return "SpellCheck API is working!";
    }
}
