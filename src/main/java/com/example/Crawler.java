package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class Crawler {
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        WebDriver driver = null;
        try {
            // Create data directory
            File dataDir = new File("/app/data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File outputFile = new File("/app/data/news_data.csv");
            System.out.println("Writing to file: " + outputFile.getAbsolutePath());
            
            // Initialize ChromeOptions without headless mode
         
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--start-maximized"); // Maximize the window
            options.setExperimentalOption("detach", true); // Keep browser open
            options.setHeadless(false); // Ensure browser is visible
            
            // Initialize WebDriver
            driver = new RemoteWebDriver(new URL("http://selenium:4444"), options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, false))) {
                // Write CSV Header
                writer.println("Section,Headline,Description,Time,Category,Link,ImageLink");
                
                // Crawl main page
                crawlMainNewsPage(driver, wait, writer);
                
                // Crawl additional sections using direct links
                crawlAdditionalSections(driver, wait, writer);
                
                // Crawl sections by searching for terms
                crawlSectionsBySearch(driver, wait, writer);
            }
        } catch (Exception e) {
            System.out.println("Error during crawling: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null)
                driver.quit();
        }
    }

    // Crawl BBC News main page
    private static void crawlMainNewsPage(WebDriver driver, WebDriverWait wait, PrintWriter writer)
            throws InterruptedException {
        driver.get("https://www.bbc.com/news");
        System.out.println("Accessing BBC News...");
        
        // Task 3: Handle cookie popup
        try {
            WebElement cookieBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[aria-label='Consent']")));
            cookieBtn.click();
            System.out.println("Cookie popup closed.");
        } catch (Exception e) {
            System.out.println("No cookie popup.");
         
        }
        
        // Wait for the main content to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid*='section-outer']")));
        Thread.sleep(2000);
        
        List<WebElement> sections = driver.findElements(By.cssSelector("[data-testid*='section-outer']"));
        for (WebElement section : sections) {
            String sectionTitle = "Top Stories";
            try {
                WebElement titleElement = section.findElement(By.cssSelector("[data-testid$='-title']"));
                sectionTitle = titleElement.getText().trim();
            } catch (Exception ignored) {
            }
            
            List<WebElement> articles = section.findElements(By.cssSelector("[data-testid*='-card']"));
            for (WebElement article : articles) {
                extractAndWriteArticle(sectionTitle, article, writer);
            }
        }
    }

    // Task 2: Crawl Multiple Pages
    // Crawl additional sections using direct links
    private static void crawlAdditionalSections(WebDriver driver, WebDriverWait wait, PrintWriter writer)
            throws InterruptedException {
        // Define the URLs for each section
        String[][] sections = {
                { "Israel-Gaza War", "https://www.bbc.com/news/topics/c2vdnvdg6xxt" },
                { "War in Ukraine", "https://www.bbc.com/news/war-in-ukraine" },
                { "US & Canada", "https://www.bbc.com/news/us-canada" },
                { "UK", "https://www.bbc.com/news/uk" },
                { "Africa", "https://www.bbc.com/news/world/africa" },
                { "Asia", "https://www.bbc.com/news/world/asia" },
                { "Australia", "https://www.bbc.com/news/world/australia" },
                { "Europe", "https://www.bbc.com/news/world/europe" },
                { "Latin America", "https://www.bbc.com/news/world/latin_america" },
                { "Middle East", "https://www.bbc.com/news/world/middle_east" },
                { "BBC InDepth", "https://www.bbc.com/news/bbcindepth" },
                { "BBC Verify", "https://www.bbc.com/news/bbcverify" }
        };
        
        for (String[] section : sections) {
            String sectionName = section[0];
          
            String url = section[1];
            driver.get(url);
            System.out.println("Crawling section: " + sectionName);
            Thread.sleep(2000);
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            List<WebElement> articles = driver.findElements(By.cssSelector("article, [data-testid*='-card']"));
            for (WebElement article : articles) {
                extractAndWriteArticle(sectionName, article, writer);
            }
        }
    }

    // Crawl sections by searching for terms
    private static void crawlSectionsBySearch(WebDriver driver, WebDriverWait wait, PrintWriter writer)
            throws InterruptedException {
        String[] searchTerms = { "Business", "Technology", "Culture", "Travel" };
        for (String term : searchTerms) {
            System.out.println("Searching for: " + term);
            searchBBC(driver, wait, term);
            Thread.sleep(2000);
            
            List<WebElement> articles = driver.findElements(By.cssSelector("article, [data-testid*='-card']"));
            for (WebElement article : articles) {
                extractAndWriteArticle(term, article, writer);
            }
        }
    }

    // Search for a term on BBC News
    private static void searchBBC(WebDriver driver, WebDriverWait wait, String searchTerm)
            throws InterruptedException {
        driver.get("https://www.bbc.com/news");
        try {
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[aria-label='Search BBC']")));
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.RETURN);
            System.out.println("Searching for: " + searchTerm);
            Thread.sleep(3000); // Wait for search results to load
        } catch (Exception e) {
            System.out.println("Error during search: " + e.getMessage());
        }
    }

    // Extract headline, description, metadata (time & category), link, and image link
    private static void extractAndWriteArticle(String section, WebElement article, PrintWriter writer) {
        try {
            String title = "", description = "", time = "", category = "", link = "", imageLink = "";
            
            try {
                title = article.findElement(By.cssSelector("[data-testid='card-headline'], h3, h2")).getText().trim();
            } catch (Exception ignored) {
            }
            
            try {
                description = article.findElement(By.cssSelector("[data-testid='card-description'], p")).getText().trim();
            } catch (Exception ignored) {
            }
            
            try {
                WebElement timeElement = article.findElement(By.cssSelector("[data-testid='card-metadata-lastupdated']"));
                time = timeElement.getText().trim();
            } catch (Exception ignored) {
            }
            
            try {
                WebElement catElement = article.findElement(By.cssSelector("[data-testid='card-metadata-tag']"));
                category = catElement.getText().trim();
            } catch (Exception ignored) {
            }
            
            try {
                WebElement linkElement = article.findElement(By.cssSelector("a, [data-testid='internal-link']"));
                link = linkElement.getAttribute("href");
                if (!link.startsWith("http")) {
                    link = "https://www.bbc.com" + link;
                }
            } catch (Exception ignored) {
            }
            
            try {
                imageLink = extractImageLink(article);
            } catch (Exception ignored) {
            }
            
            if (!title.isEmpty()) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        section,
                        title.replace("\"", "'"),
                        description.replace("\"", "'"),
                        time.replace("\"", "'"),
                        category.replace("\"", "'"),
                        link,
                        imageLink);
                System.out.println("Crawled: " + title);
            }
        } catch (Exception e) {
            System.out.println("Error processing article: " + e.getMessage());
        }
    }

    // Extract image link from article
    private static String extractImageLink(WebElement article) {
        try {
            WebElement imageWrapper = article.findElement(By.cssSelector("[data-testid='card-image-wrapper']"));
            WebElement imgElement = imageWrapper.findElement(By.tagName("img"));
            String src = imgElement.getAttribute("src");
            if (src != null && !src.isEmpty()) {
                return src;
            }
            // Fallback to srcset if src is not available
            String srcset = imgElement.getAttribute("srcset");
            if (srcset != null && !srcset.isEmpty()) {
                // Extract the first URL from srcset
                return srcset.split("\\s+")[0];
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}