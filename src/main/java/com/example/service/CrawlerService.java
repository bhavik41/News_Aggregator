package com.example.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.crawler.BBCCrawler;
import com.example.utils.DriverManager;

@Service
public class CrawlerService {

    private static final String OUTPUT_CSV = "bbc_news_data.csv";
    private static final boolean USE_REMOTE_DRIVER = true;
    private static final String LOCAL_CHROME_DRIVER_PATH = "C:\\Users\\hp\\Downloads\\chromedriver-win64\\chromedriver.exe";

    private static String getRemoteDriverUrl() {
        String host = System.getenv("SELENIUM_HOST");
        if (host == null || host.isEmpty()) host = "localhost";
        String port = System.getenv("SELENIUM_PORT");
        if (port == null || port.isEmpty()) port = "4444";
        return "http://" + host + ":" + port;
    }

    /**
     * Crawl only BBC news and save the results to the database.
     * Runs on application startup and then every 3 hours.
     */
    @PostConstruct
    @Scheduled(fixedRate = 10800000) // Run every 3 hours (3 * 60 * 60 * 1000 ms)
    public void crawlBBCAndSaveToDB() {
        WebDriver driver = null;
        CSVWriter csvWriter = null;
        
        System.out.println("\n========================================");
        System.out.println("⏰ BBC NEWS CRAWLER TRIGGERED");
        System.out.println("Time: " + java.time.LocalDateTime.now());
        System.out.println("========================================");
        
        try {
            // System.out.println("🔧 Initializing Selenium WebDriver...");
            // System.out.println("   - Using remote driver: " + USE_REMOTE_DRIVER);
            // System.out.println("   - Remote URL: " + getRemoteDriverUrl());
            
            // driver = DriverManager.initializeDriver(USE_REMOTE_DRIVER, getRemoteDriverUrl(), LOCAL_CHROME_DRIVER_PATH);
            // System.out.println("✅ Selenium WebDriver initialized successfully");
            
            // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // System.out.println("📄 Initializing CSV writer: " + OUTPUT_CSV);
            // csvWriter = new CSVWriter(OUTPUT_CSV);
            // Set<String> seenUrls = new HashSet<>();

            // System.out.println("🌐 Starting BBC news crawl...");
            // new BBCCrawler(driver, wait).crawl(csvWriter, seenUrls);

            // System.out.println("✅ BBC News crawl completed");
            // System.out.println("📊 Total articles collected: " + seenUrls.size());
            // System.out.println("💾 Output saved to: " + OUTPUT_CSV);

            // // Upload CSV data to MongoDB
            // System.out.println("📤 Uploading data to MongoDB...");
            // // FIX: Pass seenUrls (populated from crawl) instead of empty HashSet
            // CSVtoMongoUploader.uploadCSV(OUTPUT_CSV, seenUrls);
            // System.out.println("✅ BBC news data uploaded to MongoDB successfully!");
            // System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR DURING BBC CRAWLING:");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("========================================\n");
        } finally {
            try {
                if (csvWriter != null) {
                    csvWriter.close();
                    System.out.println("📄 CSV writer closed");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error closing CSV writer: " + e.getMessage());
            }
            
            try {
                if (driver != null) {
                    driver.quit();
                    System.out.println("🌐 WebDriver closed");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error closing WebDriver: " + e.getMessage());
            }
        }
    }
}