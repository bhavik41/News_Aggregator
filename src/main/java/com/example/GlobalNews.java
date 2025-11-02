package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.*;

public class GlobalNews {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private static final String GLOBAL_NEWS_ROOT = "https://globalnews.ca";
    private static final int MAX_PAGES_PER_SECTION = 5; // Reduced for speed
    
    public GlobalNews(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.js = js;
    }
    
    public void crawl(CSVWriter csvWriter, Set<String> seenUrls) {
        String[] sections = {
            "", // Top stories (homepage)
            "world",
            "canada",
            "politics",
            "money",
            "entertainment",
            "sports",
            "health",
            "tech",
            "trending"
        };
        
        // Handle cookie popup once at the beginning
        handleCookiePopup();
        
        for (String section : sections) {
            String sectionName = section.isEmpty() ? "Top Stories" : capitalizeFirstLetter(section);
            crawlSection(sectionName, section, csvWriter, seenUrls);
        }
    }
    
    private void handleCookiePopup() {
        try {
            driver.get(GLOBAL_NEWS_ROOT);
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement acceptBtn = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]")));
            acceptBtn.click();
            System.out.println("🍪 Cookie popup accepted");
            Utils.sleep(1000);
        } catch (Exception e) {
            System.out.println("ℹ️ No cookie popup found");
        }
    }
    
    private void crawlSection(String sectionName, String section, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            System.out.println("Crawling Global News: " + sectionName);
            
            for (int page = 1; page <= MAX_PAGES_PER_SECTION; page++) {
                String url = buildUrl(section, page);
                
                if (!navigateToUrl(url)) {
                    System.err.println("❌ Failed to load " + sectionName + " page " + page);
                    continue;
                }
                
                // Wait for articles to load
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("a.c-posts__inner")));
                } catch (Exception e) {
                    System.err.println("⚠️ No articles found on " + sectionName + " page " + page);
                    break; // No more pages
                }
                
                // Scroll to load more content
                scrollPage();
                
                // Extract articles
                int count = extractArticles(sectionName, csvWriter, seenUrls);
                System.out.println("  ✅ Page " + page + ": Extracted " + count + " articles");
                
                if (count == 0) {
                    break; // No articles found, stop pagination
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error on Global News section " + sectionName + ": " + e.getMessage());
        }
    }
    
    private String buildUrl(String section, int page) {
        if (section.isEmpty()) {
            return GLOBAL_NEWS_ROOT + "/";
        } else if (page == 1) {
            return GLOBAL_NEWS_ROOT + "/" + section + "/";
        } else {
            return GLOBAL_NEWS_ROOT + "/" + section + "/page/" + page + "/";
        }
    }
    
    private boolean navigateToUrl(String url) {
        try {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
            driver.get(url);
            Utils.sleep(1000);
            return true;
        } catch (TimeoutException e) {
            System.err.println("⚠️ Page load timeout for " + url);
            try {
                js.executeScript("window.stop();");
                return true;
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Navigation error: " + e.getMessage());
            return false;
        }
    }
    
    private void scrollPage() {
        try {
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollBy(0, 1000)");
                Utils.sleep(10);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Scroll error: " + e.getMessage());
        }
    }
    
    private int extractArticles(String sectionName, CSVWriter csvWriter, Set<String> seenUrls) {
        int count = 0;
        
        try {
            List<WebElement> articles = driver.findElements(By.cssSelector("a.c-posts__inner"));
            
            for (WebElement article : articles) {
                try {
                    String link = article.getAttribute("href");
                    
                    // Validate link
                    if (link == null || !link.contains("/news/") || seenUrls.contains(link)) {
                        continue;
                    }
                    
                    // Extract article data
                    String title = extractTitle(article);
                    String desc = extractDescription(article);
                    String time = extractTime(article);
                    String category = extractCategory(article);
                    String img = extractImage(article);
                    
                    // Validate required fields
                    if (isValidArticle(title, desc, time)) {
                        seenUrls.add(link);
                        csvWriter.writeRow(
                            "Global News",
                            sectionName,
                            title,
                            desc,
                            time,
                            category,
                            link,
                            img
                        );
                        count++;
                        System.out.println("  ✓ " + title.substring(0, Math.min(60, title.length())));
                    }
                    
                } catch (StaleElementReferenceException e) {
                    continue; // Element no longer in DOM
                } catch (Exception e) {
                    System.err.println("  ⚠️ Skipped article: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting articles: " + e.getMessage());
        }
        
        return count;
    }
    
    private String extractTitle(WebElement article) {
        try {
            return article.findElement(By.cssSelector(".c-posts__headlineText"))
                         .getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
    
    private String extractDescription(WebElement article) {
        try {
            WebElement descEl = article.findElement(By.cssSelector(".c-posts__excerpt"));
            String desc = descEl.getText().trim();
            return cleanDescription(desc);
        } catch (Exception e) {
            return "";
        }
    }
    
    private String extractTime(WebElement article) {
        try {
            List<WebElement> infos = article.findElements(By.cssSelector(".c-posts__info"));
            if (infos.size() > 1) {
                String time = infos.get(1).getText().trim();
                return cleanTime(time);
            }
        } catch (Exception e) {
            // Time not found
        }
        return "";
    }
    
    private String extractCategory(WebElement article) {
        try {
            String category = article.findElement(By.cssSelector(".c-posts__info--highlight"))
                                    .getText().trim();
            
            // Filter out invalid categories
            if (category.matches("\\d+(?:\\s\\d+)*") ||
                category.equalsIgnoreCase("READ") ||
                category.equalsIgnoreCase("WATCH")) {
                return "";
            }
            return category;
        } catch (Exception e) {
            return "";
        }
    }
    
    private String extractImage(WebElement article) {
        try {
            WebElement imgEl = article.findElement(By.cssSelector("img"));
            String src = imgEl.getAttribute("src");
            if (src != null && !src.isEmpty()) {
                return src;
            }
            src = imgEl.getAttribute("data-src");
            if (src != null && !src.isEmpty()) {
                return src;
            }
        } catch (Exception e) {
            // Image not found
        }
        return "";
    }
    
    private boolean isValidArticle(String title, String desc, String time) {
        return isValid(title) && 
               isValid(desc) && 
               desc.length() >= 10 && 
               isValid(time);
    }
    
    private boolean isValid(String s) {
        return s != null && 
               !s.trim().isEmpty() &&
               !s.equalsIgnoreCase("N/A") &&
               !s.equalsIgnoreCase("null");
    }
    
    private String cleanDescription(String desc) {
        if (desc == null || desc.isEmpty()) return "";
        
        desc = desc.trim();
        if (desc.equalsIgnoreCase("null") || desc.equalsIgnoreCase("N/A")) {
            return "";
        }
        
        // Remove standalone numbers
        desc = desc.replaceAll("\\b\\d+\\b", "").trim();
        // Normalize whitespace
        desc = desc.replaceAll("\\s{2,}", " ");
        
        return desc.isEmpty() ? "" : desc;
    }
    
    private String cleanTime(String time) {
        if (time == null || time.isEmpty()) return "";
        
        time = time.trim();
        if (time.equalsIgnoreCase("N/A") || 
            time.equalsIgnoreCase("null") || 
            time.matches("^[0-9]+$")) {
            return "";
        }
        
        return time;
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}