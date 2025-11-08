package com.example.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.service.CSVWriter;

public class GlobalCrawler {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ✅ Constructor (matches what Main.java expects)
    public GlobalCrawler(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // ✅ Main crawling method (matches Main.java)
    public void crawl(CSVWriter writer, Set<String> uniqueLinks) {
        List<String[]> cleanRows = new ArrayList<>();

        // ✅ Include ALL main sections of Global News
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
                "trending",
                "weather",
                "lifestyle",
                "videos"
        };

        try {
            for (String section : sections) {
                String sectionName = section.isEmpty() ? "Top Stories" : section.toUpperCase();

                for (int page = 1; page <= 3; page++) {
                    String url = section.isEmpty()
                            ? "https://globalnews.ca/"
                            : (page == 1
                            ? "https://globalnews.ca/" + section + "/"
                            : "https://globalnews.ca/" + section + "/page/" + page + "/");

                    System.out.println("\n🌍 Scraping section: " + sectionName + " | Page " + page);
                    driver.get(url);
                    Thread.sleep(2000);

                    // ✅ Handle cookie popup
                    try {
                        WebElement acceptBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]")));
                        acceptBtn.click();
                        System.out.println("🍪 Popup accepted.");
                    } catch (Exception e) {
                        System.out.println("ℹ️ No popup on this page.");
                    }

                    // ✅ Wait for article elements
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.c-posts__inner")));

                    // ✅ Scroll to load more content
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    for (int i = 0; i < 5; i++) {
                        js.executeScript("window.scrollBy(0, 1000)");
                        Thread.sleep(700);
                    }

                    List<WebElement> articles = driver.findElements(By.cssSelector("a.c-posts__inner"));
                    for (WebElement article : articles) {
                        try {
                            String link = article.getAttribute("href");
                            if (link == null || !link.contains("/news/") || uniqueLinks.contains(link))
                                continue; // skip duplicates or invalid links

                            String title = "N/A";
                            String desc = "N/A";
                            String time = "N/A";
                            String category = "N/A";
                            String img = "N/A";

                            // 📰 Headline
                            try {
                                title = article.findElement(By.cssSelector(".c-posts__headlineText")).getText().trim();
                            } catch (Exception ignored) {}

                            // 🕒 Time
                            try {
                                List<WebElement> infos = article.findElements(By.cssSelector(".c-posts__info"));
                                if (infos.size() > 1)
                                    time = infos.get(1).getText().trim();
                                time = cleanTime(time);
                            } catch (Exception ignored) {}

                            // 🏷️ Category
                            try {
                                category = article.findElement(By.cssSelector(".c-posts__info--highlight")).getText().trim();
                                if (category.matches("\\d+(?:\\s\\d+)*") ||
                                        category.equalsIgnoreCase("READ") ||
                                        category.equalsIgnoreCase("WATCH"))
                                    category = "N/A";
                            } catch (Exception ignored) {}

                            // 📝 Description
                            try {
                                WebElement descEl = article.findElement(By.cssSelector(".c-posts__excerpt"));
                                desc = cleanDescription(descEl.getText());
                            } catch (Exception ignored) {}

                            // 🖼️ Image
                            try {
                                WebElement imgEl = article.findElement(By.cssSelector("img"));
                                if (imgEl.getAttribute("src") != null && !imgEl.getAttribute("src").isEmpty())
                                    img = imgEl.getAttribute("src");
                                else if (imgEl.getAttribute("data-src") != null)
                                    img = imgEl.getAttribute("data-src");
                            } catch (Exception ignored) {}

                            // ✅ Add only valid, unique, complete articles
                            if (isValid(desc) && desc.length() >= 10 && isValid(time) && isValid(title)) {
                                uniqueLinks.add(link);
                                cleanRows.add(new String[]{
                                        sectionName,
                                        safe(title),
                                        safe(desc),
                                        safe(time),
                                        safe(category),
                                        safe(link),
                                        safe(img)
                                });
                                System.out.println("✅ Added: " + title);

                                // ✅ Also write directly to CSV file
                                synchronized (writer) {
                                    writer.writeRow("GlobalNews", sectionName, title, desc, time, category, link, img);
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("⚠️ Skipped article: " + e.getMessage());
                        }
                    }

                    System.out.println("✅ Done: " + sectionName + " Page " + page +
                            " | Total so far: " + cleanRows.size());
                }
            }

            System.out.println("\n🎯 " + cleanRows.size() + " clean, unique articles saved.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- HELPERS ----------------
    private static boolean isValid(String s) {
        return s != null && !s.trim().isEmpty() &&
                !s.equalsIgnoreCase("N/A") &&
                !s.equalsIgnoreCase("null");
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "N/A" : s.trim();
    }

    private static String cleanDescription(String desc) {
        if (desc == null) return "N/A";
        desc = desc.trim();
        if (desc.equalsIgnoreCase("null") || desc.equalsIgnoreCase("N/A") || desc.isEmpty()) return "N/A";
        desc = desc.replaceAll("\\b\\d+\\b", "").trim();
        desc = desc.replaceAll("\\s{2,}", " ");
        return desc.isEmpty() ? "N/A" : desc;
    }

    private static String cleanTime(String time) {
        if (time == null) return "N/A";
        time = time.trim();
        if (time.isEmpty() || time.equalsIgnoreCase("N/A") || time.equalsIgnoreCase("null")) return "N/A";
        if (time.matches("^[0-9]+$")) return "N/A";
        return time;
    }
}
