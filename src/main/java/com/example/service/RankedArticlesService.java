package com.example.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.News;

@Service
public class RankedArticlesService {

    @Autowired
    private NewsService newsService;

    // Stop words to exclude from ranking (common words that don't add value)
    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "will", "with", "this", "but", "they", "have", "had",
        "what", "when", "where", "who", "which", "why", "how", "all", "each",
        "she", "or", "we", "been", "were", "their", "said", "can", "may"
    );

    /**
     * Get all news articles ranked by TF-IDF score
     * TF-IDF = Term Frequency × Inverse Document Frequency
     */
    public List<News> getRankedNews() {
        List<News> allNews = newsService.getAllNews(1, Integer.MAX_VALUE, "", "all");

        if (allNews == null || allNews.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 1: Prepare documents and calculate document frequency
        List<String> documents = new ArrayList<>();
        Map<String, Integer> documentFrequency = new ConcurrentHashMap<>();
        int totalDocuments = allNews.size();

        // Extract text content and build document frequency map
        for (News news : allNews) {
            String content = buildContent(news);
            documents.add(content);
            
            // Count in how many documents each term appears (for IDF calculation)
            Set<String> uniqueTerms = extractUniqueTerms(content);
            uniqueTerms.forEach(term -> 
                documentFrequency.merge(term, 1, Integer::sum)
            );
        }

        // Step 2: Calculate TF-IDF score for each article in parallel
        allNews.parallelStream().forEach(news -> {
            int index = allNews.indexOf(news);
            String content = documents.get(index);
            double score = calculateTFIDFScore(content, documentFrequency, totalDocuments);
            news.setScore(score);
        });

        // Step 3: Sort by score descending
        allNews.sort((n1, n2) -> Double.compare(n2.getScore(), n1.getScore()));

        return allNews;
    }

    /**
     * Get top ranked news with pagination support
     */
    public List<News> getTopRankedNews(int page, int limit) {
        List<News> rankedNews = getRankedNews();
        
        if (rankedNews.isEmpty()) {
            return Collections.emptyList();
        }
        
        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, rankedNews.size());
        
        if (startIndex >= rankedNews.size()) {
            return Collections.emptyList();
        }
        
        return rankedNews.subList(startIndex, endIndex);
    }

    /**
     * Build content from news article (title has higher weight)
     */
    private String buildContent(News news) {
        StringBuilder content = new StringBuilder();
        
        // Title is more important, so we add it 3 times for higher weight
        if (news.getTitle() != null) {
            String title = news.getTitle();
            content.append(title).append(" ")
                   .append(title).append(" ")
                   .append(title).append(" ");
        }
        
        if (news.getDescription() != null) {
            content.append(news.getDescription()).append(" ");
        }
        
        return content.toString();
    }

    /**
     * Extract unique terms from content (normalized and filtered)
     */
    private Set<String> extractUniqueTerms(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptySet();
        }

        return Arrays.stream(content.toLowerCase().split("\\W+"))
                .filter(word -> !word.isEmpty() && word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    /**
     * Calculate TF-IDF score for a document
     * TF-IDF = Σ(TF(term) × IDF(term)) for all terms in document
     */
    private double calculateTFIDFScore(String content, 
                                       Map<String, Integer> documentFrequency, 
                                       int totalDocuments) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }

        // Tokenize and count term frequencies
        List<String> terms = Arrays.stream(content.toLowerCase().split("\\W+"))
                .filter(word -> !word.isEmpty() && word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toList());

        if (terms.isEmpty()) {
            return 0.0;
        }

        // Calculate term frequency for this document
        Map<String, Long> termFrequency = terms.stream()
                .collect(Collectors.groupingBy(term -> term, Collectors.counting()));

        // Calculate TF-IDF score
        double score = 0.0;
        int docLength = terms.size();

        for (Map.Entry<String, Long> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            long tf = entry.getValue();
            
            // Term Frequency (normalized by document length)
            double termFreq = (double) tf / docLength;
            
            // Inverse Document Frequency
            int df = documentFrequency.getOrDefault(term, 1);
            double idf = Math.log((double) totalDocuments / df);
            
            // TF-IDF contribution for this term
            score += termFreq * idf;
        }

        return score;
    }
}