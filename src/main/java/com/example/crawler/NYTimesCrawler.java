package com.example.crawler;

import com.example.service.CSVWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * NYTimesCrawler
 * -----------------------
 * Crawls New York Times homepage and sections.
 * Works directly with CSVWriter and avoids duplicate URLs.
 */
public class NYTimesCrawler {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // ✅ Constructor
    public NYTimesCrawler(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.js = js;
    }

    // ✅ Crawl method using CSVWriter
    public void crawl(CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            System.out.println("========================================");
            System.out.println("Crawling: The New York Times");
            System.out.println("========================================");

            // Crawl homepage
            crawlSection("Homepage", "https://www.nytimes.com/", csvWriter, seenUrls);

            // Sections to crawl
            String[] sections = {
                "world", "us", "politics", "business", "technology", "science",
                "health", "sports", "arts", "books", "style", "food", "travel",
                "opinion", "climate", "education"
            };

            for (String section : sections) {
                String url = "https://www.nytimes.com/section/" + section;
                crawlSection(capitalize(section), url, csvWriter, seenUrls);
            }

        } catch (Exception e) {
            System.out.println("❌ Error during NYTimes crawl: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =============================================
    // Crawl a single section
    // =============================================
    private void crawlSection(String sectionName, String url, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            System.out.println("\n🌐 Visiting section: " + sectionName);
            driver.get(url);

            // Wait until at least one article is present
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section article")));
            Thread.sleep(2000);

            List<WebElement> articles = driver.findElements(By.cssSelector("section article"));
            System.out.println("📰 Found " + articles.size() + " articles in " + sectionName);

            int count = 0;
            for (WebElement article : articles) {
                try {
                    String headline = "";
                    String description = "";
                    String link = "";

                    // Extract headline
                    try { headline = article.findElement(By.cssSelector("h2, h3, h4")).getText().trim(); } catch (Exception ignored) {}

                    // Extract description
                    try { description = article.findElement(By.cssSelector("p, span")).getText().trim(); } catch (Exception ignored) {}

                    // Extract link
                    try { link = article.findElement(By.cssSelector("a")).getAttribute("href"); } catch (Exception ignored) {}

                    // Skip duplicates
                    if (!headline.isEmpty() && link != null && !seenUrls.contains(link)) {
                        seenUrls.add(link);

                        // ✅ Write to CSV using 8-argument writeRow
                        csvWriter.writeRow(
                                "NYTimes",                     // source
                                sectionName,                   // section
                                headline.replace("\"", "'"),   // headline
                                description.replace("\"", "'"),// description
                                LocalDateTime.now().toString(),// time
                                "",                             // category (empty)
                                link,                           // link
                                ""                              // imageLink (empty)
                        );

                        count++;
                        System.out.println("✔ Crawled: " + headline);
                    }

                } catch (Exception inner) {
                    System.out.println("⚠️ Error parsing article: " + inner.getMessage());
                }
            }

            System.out.println("✅ Done: " + sectionName + " | Total added: " + count);

        } catch (Exception e) {
            System.out.println("❌ Error on section " + sectionName + ": " + e.getMessage());
        }
    }

    // Helper: Capitalize section name
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
