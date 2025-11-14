package com.example.model;

import org.springframework.data.annotation.Id;

public class News {

    @Id
    private String id;
    private String title;       // Headline
    private String source;      // Source
    private String link;        // Link
    private String date;        // Time
    private String section;     // Section
    private String imageLink;   // ImageLink
    private String description; // Description
    private String category;    // Category

    private double score;       // Word frequency score for ranking

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getImageLink() { return imageLink; }
    public void setImageLink(String imageLink) { this.imageLink = imageLink; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
