package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.openqa.selenium.By;

class CBCCrawler {
    private WebDriver driver;
    private WebDriverWait wait;
    private WebDriverWait shortWait;
    private JavascriptExecutor js;
    private static final String CBC_ROOT = "https://www.cbc.ca";
    private static final int MAX_ARTICLES = 50;
    private static final int PAGE_LOAD_TIMEOUT = 2; // seconds
    
    public CBCCrawler(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        this.js = js;
    }
    
    public void crawl(CSVWriter csvWriter, Set<String> seenUrls) {
        String[][] sections = {
            {"Top Stories", "/news"},
            {"Canada", "/news/canada"},
            {"World", "/news/world"},
            {"Politics", "/news/politics"},
            {"Business", "/news/business"},
            {"Health", "/news/health"},
            {"Technology", "/news/technology"},
            {"Climate", "/news/climate"},
            {"Entertainment", "/news/entertainment"},
            {"Sports", "/sports"}
        };
        
        for (String[] section : sections) {
            crawlSection(section[0], section[1], csvWriter, seenUrls);
        }
    }
    
    private void crawlSection(String sectionName, String path, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            String url = CBC_ROOT + path;
            System.out.println("Crawling CBC: " + sectionName);
            
            // Navigate with timeout handling
            if (!navigateToUrl(url)) {
                System.err.println("❌ Failed to load " + sectionName);
                return;
            }
            
            // Wait for page to be ready
            waitForPageReady();
            
            // Scroll to load more content
            scrollPage();
            
            // Extract article URLs with multiple selector strategies
            List<String> urls = extractArticleUrls(seenUrls);
            System.out.println("📊 Found " + urls.size() + " potential articles in " + sectionName);
            
            // Process articles
            int count = 0;
            for (String articleUrl : urls) {
                try {
                    if (extractArticle(sectionName, articleUrl, csvWriter, seenUrls)) {
                        count++;
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Skipping article " + articleUrl + ": " + e.getMessage());
                }
            }
            System.out.println("✅ Extracted " + count + " articles from " + sectionName);
            
        } catch (Exception e) {
            System.err.println("❌ Error on CBC section " + sectionName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean navigateToUrl(String url) {
        try {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));
            driver.get(url);
            return true;
        } catch (TimeoutException e) {
            System.err.println("⚠️ Page load timeout for " + url + ", stopping load...");
            try {
                js.executeScript("window.stop();");
                return true; // Continue with partial page
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Navigation error: " + e.getMessage());
            return false;
        }
    }
    
    private void waitForPageReady() {
        try {
            shortWait.until(driver -> 
                ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete")
            );
        } catch (TimeoutException e) {
            System.err.println("⚠️ Page readyState timeout, continuing anyway...");
        }
        Utils.sleep(1000); // Additional buffer
    }
    
    private void scrollPage() {
        try {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight / 3);");
            Utils.sleep(500);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight / 2);");
            Utils.sleep(500);
        } catch (Exception e) {
            System.err.println("⚠️ Scroll error: " + e.getMessage());
        }
    }
    
    private List<String> extractArticleUrls(Set<String> seenUrls) {
        List<String> urls = new ArrayList<>();
        
        // Try multiple selector strategies
        String[] selectorStrategies = {
            "a[href*='/news/']",
            "article a[href]",
            "div[class*='card'] a[href]",
            "div[class*='story'] a[href]",
            "h3 a[href]",
            "h2 a[href]"
        };
        
        for (String selector : selectorStrategies) {
            try {
                List<WebElement> links = driver.findElements(By.cssSelector(selector));
                System.out.println("  🔍 Found " + links.size() + " links with selector: " + selector);
                
                for (WebElement link : links) {
                    try {
                        String articleUrl = link.getAttribute("href");
                        if (isValidArticleUrl(articleUrl, seenUrls)) {
                            urls.add(articleUrl);
                            if (urls.size() >= MAX_ARTICLES) {
                                return urls;
                            }
                        }
                    } catch (StaleElementReferenceException e) {
                        // Element no longer attached to DOM, skip it
                        continue;
                    } catch (Exception e) {
                        // Skip this link
                        continue;
                    }
                }
                
                if (!urls.isEmpty()) {
                    break; // Found articles with this strategy
                }
            } catch (Exception e) {
                System.err.println("  ⚠️ Selector failed: " + selector);
            }
        }
        
        return urls;
    }
    
    private boolean isValidArticleUrl(String url, Set<String> seenUrls) {
        if (url == null || url.isEmpty()) return false;
        if (seenUrls.contains(url)) return false;
        if (!url.startsWith("http")) return false;
        if (!url.contains("cbc.ca")) return false;
        
        // Filter out non-article URLs
        String[] excludePatterns = {
            "/player/", "gem.cbc.ca", "/radio/", "/listen/",
            "/programs/", "/newsletters/", "/about/", "/contactus/"
        };
        
        for (String pattern : excludePatterns) {
            if (url.contains(pattern)) return false;
        }
        
        // Must contain /news/ or /sports/
        return url.contains("/news/") || url.contains("/sports/");
    }
    
    private boolean extractArticle(String section, String url, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            if (seenUrls.contains(url)) return false;
            
            // Navigate to article
            if (!navigateToUrl(url)) {
                return false;
            }
            
            waitForPageReady();
            
            // Extract metadata
            String headline = extractMeta(new String[]{
                "meta[property='og:title']", 
                "meta[name='twitter:title']", 
                "h1",
                "h1[class*='headline']"
            });
            
            String description = extractMeta(new String[]{
                "meta[property='og:description']", 
                "meta[name='description']",
                "p[class*='dek']",
                "p[class*='description']"
            });
            
            // Filter out ad text
            if (description.toLowerCase().contains("advertising partners")) {
                description = "";
            }
            
            // Extract timestamp
            String time = "";
            try {
                List<WebElement> timeElements = driver.findElements(By.tagName("time"));
                if (!timeElements.isEmpty()) {
                    time = timeElements.get(0).getAttribute("datetime");
                    if (time == null || time.isEmpty()) {
                        time = timeElements.get(0).getText();
                    }
                }
            } catch (Exception e) {
                time = "";
            }
            
            time = time.replace("Posted:", "").replace("Last updated:", "").trim();
            
            String category = extractCategory(url);
            
            if (!headline.isEmpty() && !url.isEmpty()) {
                seenUrls.add(url);
                csvWriter.writeRow("CBC", section, headline, description, time, category, url, "");
                System.out.println("  ✓ " + headline.substring(0, Math.min(60, headline.length())));
                
                // Navigate back with timeout handling
                try {
                    driver.navigate().back();
                    waitForPageReady();
                } catch (TimeoutException e) {
                    System.err.println("  ⚠️ Back navigation timeout, stopping load...");
                    js.executeScript("window.stop();");
                }
                
                return true;
            }
        } catch (Exception e) {
            System.err.println("  ❌ Article extraction failed: " + e.getMessage());
        }
        return false;
    }
    
    private String extractMeta(String[] selectors) {
        for (String sel : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(sel));
                for (WebElement e : elements) {
                    String content = e.getAttribute("content");
                    if (content != null && !content.trim().isEmpty()) {
                        return content.trim();
                    }
                    String text = e.getText();
                    if (text != null && !text.trim().isEmpty() && text.length() > 5) {
                        return text.trim();
                    }
                }
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        return "";
    }
    
    private String extractCategory(String url) {
        try {
            if (url.contains("/news/")) {
                String path = url.split("/news/")[1];
                String[] parts = path.split("/");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    return parts[0];
                }
            } else if (url.contains("/sports/")) {
                return "sports";
            }
        } catch (Exception e) {
            // Return empty string
        }
        return "";
    }
}