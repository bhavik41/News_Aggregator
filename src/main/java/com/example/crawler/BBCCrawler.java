package com.example.crawler;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.service.CSVWriter;
import com.example.utils.Utils;

public class BBCCrawler {
    private  WebDriver driver;
    private  WebDriverWait wait;
    
    public BBCCrawler(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }
    
    public void crawl(CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            crawlMainPage(csvWriter, seenUrls);
            crawlSections(csvWriter, seenUrls);
        } catch (Exception e) {
            System.err.println("Error crawling BBC: " + e.getMessage());
        }
    }
    
    private void crawlMainPage(CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            driver.get("https://www.bbc.com/news");
            System.out.println("Crawling BBC News main page...");
            handleCookies();
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid*='section-outer']")));
            Utils.sleep(2000);
            
            List<WebElement> sections = driver.findElements(By.cssSelector("[data-testid*='section-outer']"));
            int sectionCount = 0;
            for (WebElement section : sections) {
                if (sectionCount >= 5) break; // Limit to 5 sections on main page
                String sectionTitle = getSectionTitle(section);
                List<WebElement> articles = section.findElements(By.cssSelector("[data-testid*='-card']"));
                int articleCount = 0;
                for (WebElement article : articles) {
                    if (articleCount >= 60) break; // Limit to 20 articles per section
                    extractArticle(sectionTitle, article, csvWriter, seenUrls);
                    articleCount++;
                }
                sectionCount++;
            }
        } catch (Exception e) {
            System.err.println("Error on BBC main page: " + e.getMessage());
        }
    }
    
    private void crawlSections(CSVWriter csvWriter, Set<String> seenUrls) {
        String[][] sections = {
            {"Israel-Gaza War", "https://www.bbc.com/news/topics/c2vdnvdg6xxt"},
            {"War in Ukraine", "https://www.bbc.com/news/war-in-ukraine"},
            {"US & Canada", "https://www.bbc.com/news/us-canada"},
            {"UK", "https://www.bbc.com/news/uk"},
            {"Africa", "https://www.bbc.com/news/world/africa"},
            {"Asia", "https://www.bbc.com/news/world/asia"},
            {"Australia", "https://www.bbc.com/news/world/australia"},
            {"Europe", "https://www.bbc.com/news/world/europe"},
            {"Latin America", "https://www.bbc.com/news/world/latin_america"},
            {"Middle East", "https://www.bbc.com/news/world/middle_east"},
            {"Business", "https://www.bbc.com/news/business"},
            {"Technology", "https://www.bbc.com/news/technology"}
        };
        
        for (String[] section : sections) {
            try {
                driver.get(section[1]);
                System.out.println("Crawling BBC: " + section[0]);
                Utils.sleep(2000);
                
                List<WebElement> articles = driver.findElements(By.cssSelector("article, [data-testid*='-card']"));
                for (WebElement article : articles) {
                    extractArticle(section[0], article, csvWriter, seenUrls);
                }
            } catch (Exception e) {
                System.err.println("Error on BBC section " + section[0]);
            }
        }
    }
    
    private void handleCookies() {
        try {
            WebElement cookieBtn = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='Consent']")));
            cookieBtn.click();
            System.out.println("BBC cookie popup dismissed.");
        } catch (Exception e) {
            System.out.println("No BBC cookie popup.");
        }
    }
    
    private String getSectionTitle(WebElement section) {
        try {
            return section.findElement(By.cssSelector("[data-testid$='-title']")).getText().trim();
        } catch (Exception e) {
            return "Top Stories";
        }
    }
    
    private void extractArticle(String section, WebElement article, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            String title = Utils.safeGetText(article, "[data-testid='card-headline'], h3, h2");
            String description = Utils.safeGetText(article, "[data-testid='card-description'], p");
            String time = Utils.safeGetText(article, "[data-testid='card-metadata-lastupdated']");
            String category = Utils.safeGetText(article, "[data-testid='card-metadata-tag']");
            String link = Utils.safeGetAttribute(article, "a, [data-testid='internal-link']", "href");
            String imageLink = extractImage(article);
            
            if (!link.isEmpty() && !link.startsWith("http")) {
                link = "https://www.bbc.com" + link;
            }
            
            if (!title.isEmpty() && !link.isEmpty() && !seenUrls.contains(link)) {
                seenUrls.add(link);
                csvWriter.writeRow("BBC", section, title, description, time, category, link, imageLink);
                System.out.println("✓ BBC: " + title.substring(0, Math.min(50, title.length())));
            }
        } catch (Exception e) {
            // Skip problematic articles
        }
    }
    
    private String extractImage(WebElement article) {
        try {
            WebElement imageWrapper = article.findElement(By.cssSelector("[data-testid='card-image-wrapper']"));
            WebElement imgElement = imageWrapper.findElement(By.tagName("img"));
            String src = imgElement.getAttribute("src");
            if (src != null && !src.isEmpty()) return src;
            
            String srcset = imgElement.getAttribute("srcset");
            if (srcset != null && !srcset.isEmpty()) {
                return srcset.split("\\s+")[0];
            }
        } catch (Exception e) {}
        return "";
    }
}