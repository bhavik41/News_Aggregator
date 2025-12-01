package com.example.controller;

import com.example.service.SpellCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SpellCheckController {

    @Autowired
    private SpellCheckService spellCheckService;

    /**
     * Endpoint: POST /api/spellcheck
     * Purpose: Check spelling and return suggestions
     * Expected body: { "text": "your text here" }
     */
    @PostMapping("/spellcheck")
    public ResponseEntity<Map<String, Object>> checkSpelling(@RequestBody Map<String, String> body) {
        try {
            String text = body.get("text");
            
            if (text == null || text.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Text parameter is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Map<String, Object> result = spellCheckService.checkSpelling(text);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Spell check failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Endpoint: POST /api/spellcheck-suggestions
     * Purpose: Get spell check suggestions for a search term (Google-like "Did you mean?")
     * Expected body: { "text": "your text here" }
     */
    @PostMapping("/spellcheck-suggestions")
    public ResponseEntity<Map<String, Object>> getSpellCheckSuggestions(@RequestBody Map<String, String> body) {
        try {
            String text = body.get("text");
            
            if (text == null || text.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Text parameter is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> spellCheckResult = spellCheckService.checkSpelling(text);
            
            // Extract wrong words and create suggestions
            Map<String, Object> suggestions = new HashMap<>();
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, String>> wrongWords = 
                (java.util.List<Map<String, String>>) spellCheckResult.get("wrong_words");
            
            if (wrongWords != null && !wrongWords.isEmpty()) {
                StringBuilder correctedText = new StringBuilder(text);
                suggestions.put("has_suggestions", true);
                suggestions.put("original_text", text);
                suggestions.put("wrong_words", wrongWords);
                
                // Build corrected text by replacing first wrong word
                if (!wrongWords.isEmpty()) {
                    String firstWrongWord = wrongWords.get(0).get("wrong_word");
                    String suggestion = wrongWords.get(0).get("suggestion");
                    String corrected = text.replaceFirst("\\b" + firstWrongWord + "\\b", suggestion);
                    suggestions.put("corrected_text", corrected);
                }
            } else {
                suggestions.put("has_suggestions", false);
                suggestions.put("original_text", text);
            }
            
            suggestions.put("status", "success");
            return ResponseEntity.ok(suggestions);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to get suggestions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}