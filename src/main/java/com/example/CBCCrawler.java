package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class CBCCrawler {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private static final String CBC_ROOT = "https://www.cbc.ca";
    private static final int MAX_ARTICLES = 50;

    
    public CBCCrawler(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
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
            driver.get(url);
            System.out.println("Crawling CBC: " + sectionName);
            
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            js.executeScript("window.scrollTo(0, document.body.scrollHeight / 2);");
            Utils.sleep(500);
            
            List<WebElement> articleLinks = driver.findElements(
                By.cssSelector("a[href*='/news/'], article a[href]"));
            
            List<String> urls = new ArrayList<>();
            for (WebElement link : articleLinks) {
                String articleUrl = link.getAttribute("href");
                if (articleUrl != null && !articleUrl.isEmpty() && 
                    !seenUrls.contains(articleUrl) &&
                    !articleUrl.contains("/player/") &&
                    !articleUrl.contains("gem.cbc.ca")) {
                    urls.add(articleUrl);
                }
                if (urls.size() >= MAX_ARTICLES) break;
            }
            
            int count = 0;
            for (String articleUrl : urls) {
                if (extractArticle(sectionName, articleUrl, csvWriter, seenUrls)) {
                    count++;
                }
            }
            System.out.println("Extracted " + count + " articles from " + sectionName);
            
        } catch (Exception e) {
            System.err.println("Error on CBC section " + sectionName);
        }
    }
    
    private boolean extractArticle(String section, String url, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            if (seenUrls.contains(url)) return false;
            
            driver.get(url);
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            String headline = extractMeta(new String[]{
                "meta[property='og:title']", "meta[name='twitter:title']", "h1"});
            String description = extractMeta(new String[]{
                "meta[property='og:description']", "meta[name='description']", "p"});
            
            if (description.toLowerCase().contains("advertising partners")) {
                description = "";
            }
            
            String time = Utils.safeGetText(driver.findElement(By.tagName("body")), 
                "time, span[class*='time'], span[class*='timestamp']");
            time = time.replace("Posted:", "").replace("Last updated:", "").trim();
            
            String category = extractCategory(url);
            
            if (!headline.isEmpty() && !url.isEmpty()) {
                seenUrls.add(url);
                csvWriter.writeRow("CBC", section, headline, description, time, category, url, "");
                System.out.println("✓ CBC: " + headline.substring(0, Math.min(50, headline.length())));
                driver.navigate().back();
                wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                return true;
            }
        } catch (Exception e) {}
        return false;
    }
    
    private String extractMeta(String[] selectors) {
        for (String sel : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(sel));
                for (WebElement e : elements) {
                    String content = e.getAttribute("content");
                    if (content != null && !content.trim().isEmpty()) return content.trim();
                    String text = e.getText();
                    if (text != null && !text.trim().isEmpty()) return text.trim();
                }
            } catch (Exception e) {}
        }
        return "";
    }
    
    private String extractCategory(String url) {
        try {
            if (url.contains("/news/")) {
                String path = url.split("/news/")[1];
                String[] parts = path.split("/");
                if (parts.length > 0) return parts[0];
            }
        } catch (Exception e) {}
        return "";
    }
}