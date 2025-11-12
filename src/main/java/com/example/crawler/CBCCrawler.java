package com.example.crawler;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.service.CSVWriter;
import com.example.utils.Utils;

public class CBCCrawler {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private static final String CBC_ROOT = "https://www.cbc.ca";
    private static final int MAX_ARTICLES_PER_SECTION = 60;

    // Restored original constructor
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
            System.out.println("Crawling CBC: " + sectionName);
            driver.get(url);

            // Wait until at least one article/card appears
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("article, .card, [class*='story'], [class*='Card'], div[class*='contentPackage']")
            ));

            // Scroll gradually to load more content
            for (int i = 1; i <= 3; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight * arguments[0] / 3);", i);
                Utils.sleep(1000);
            }

            // Get all article cards after scrolling
            List<WebElement> articleCards = driver.findElements(
                By.cssSelector("article, .card, [class*='story'], [class*='Card'], div[class*='contentPackage']")
            );

            int count = 0;
            for (WebElement card : articleCards) {
                if (count >= MAX_ARTICLES_PER_SECTION) break;
                if (extractArticleFromCard(sectionName, card, csvWriter, seenUrls)) {
                    count++;
                }
            }

            System.out.println("Extracted " + count + " articles from " + sectionName);

        } catch (TimeoutException te) {
            System.err.println("Timeout loading section " + sectionName + ". Skipping...");
        } catch (Exception e) {
            System.err.println("Error on CBC section " + sectionName + ": " + e.getMessage());
        }
    }

    private boolean extractArticleFromCard(String section, WebElement card, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            // Extract link
            String link = "";
            try {
                WebElement linkElement = card.findElement(By.cssSelector("a[href]"));
                link = linkElement.getAttribute("href");

                if (link == null || link.isEmpty() ||
                    link.contains("/player/") ||
                    link.contains("gem.cbc.ca") ||
                    link.contains("/live/") ||
                    (!link.contains("/news/") && !link.contains("/sports/"))) {
                    return false;
                }

                if (!link.startsWith("http")) link = CBC_ROOT + link;
                if (seenUrls.contains(link)) return false;

            } catch (Exception e) {
                return false;
            }

            // Extract headline
            String headline = Utils.safeGetText(card,
                "h2, h3, h4, .headline, [class*='headline'], [class*='title']");

            // Extract description
            String description = Utils.safeGetText(card,
                "p, .description, [class*='description'], [class*='dek'], [class*='summary']");
            if (description.toLowerCase().contains("advertising partners") ||
                description.toLowerCase().contains("comments on this story")) {
                description = "";
            }

            // Extract time
            String time = Utils.safeGetText(card,
                "time, span[class*='time'], span[class*='timestamp'], span[class*='date'], .timestamp");
            time = time.replace("Posted:", "").replace("Last updated:", "").trim();

            // Category
            String category = extractCategory(link);

            // Image
            String imageLink = extractImage(card);

            if (!headline.isEmpty() && !link.isEmpty()) {
                seenUrls.add(link);
                csvWriter.writeRow("CBC", section, headline, description, time, category, link, imageLink);
                System.out.println("✓ CBC: " + headline.substring(0, Math.min(50, headline.length())));
                return true;
            }

        } catch (Exception e) {
            // Skip problematic articles
        }
        return false;
    }

    private String extractImage(WebElement card) {
        try {
            WebElement img = card.findElement(By.cssSelector("img"));
            String src = img.getAttribute("src");
            if (src != null && src.startsWith("http")) return src;

            String dataSrc = img.getAttribute("data-src");
            if (dataSrc != null && dataSrc.startsWith("http")) return dataSrc;

            String srcset = img.getAttribute("srcset");
            if (srcset != null && !srcset.isEmpty()) {
                String firstUrl = srcset.split(",")[0].trim().split("\\s+")[0];
                if (firstUrl.startsWith("http")) return firstUrl;
            }

        } catch (Exception e) {
            // No image
        }
        return "";
    }

    private String extractCategory(String url) {
        try {
            if (url.contains("/news/")) {
                String path = url.split("/news/")[1];
                String[] parts = path.split("/");
                if (parts.length > 0 && !parts[0].isEmpty()) return parts[0];
            } else if (url.contains("/sports/")) {
                String path = url.split("/sports/")[1];
                String[] parts = path.split("/");
                if (parts.length > 0 && !parts[0].isEmpty()) return parts[0];
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }
}
