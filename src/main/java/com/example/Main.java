package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.File;

public class Main {
    // Output directory - will be mapped to local ./data folder via Docker volume
    private static final String OUTPUT_DIR = "data";
    private static final String OUTPUT_FILENAME_PREFIX = "news_data_";
    
    private static final boolean USE_REMOTE_DRIVER = true;
    private static final String REMOTE_DRIVER_URL = "http://selenium:4444/wd/hub";
    private static final String LOCAL_CHROME_DRIVER_PATH = "C:\\Path\\To\\chromedriver.exe";
    
    public static void main(String[] args) {
        WebDriver driver = null;
        CSVWriter csvWriter = null;
        int totalArticles = 0;
        
        try {
            System.out.println("========================================");
            System.out.println("INITIALIZING WEB CRAWLER - SINGLE RUN");
            System.out.println("========================================");
            
            // Create data directory if it doesn't exist
            File dataDir = new File(OUTPUT_DIR);
            if (!dataDir.exists()) {
                if (dataDir.mkdirs()) {
                    System.out.println("📁 Created data directory: " + OUTPUT_DIR);
                } else {
                    System.err.println("⚠️ Could not create data directory, using current directory");
                }
            }
            
            // Generate timestamped filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String outputPath = OUTPUT_DIR + "/" + OUTPUT_FILENAME_PREFIX + timestamp + ".csv";
            
            driver = DriverManager.initializeDriver(USE_REMOTE_DRIVER, REMOTE_DRIVER_URL, LOCAL_CHROME_DRIVER_PATH);
            // driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
            // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            // driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            csvWriter = new CSVWriter(outputPath);
            Set<String> seenUrls = new HashSet<>();
            
            System.out.println("✅ Driver initialized successfully");
            System.out.println("📝 CSV file path: " + outputPath);
            System.out.println("🔄 Data will be saved continuously during crawling\n");
            // Crawl BBC News
            try {
                System.out.println("========================================");
                System.out.println("🌐 CRAWLING BBC NEWS");
                System.out.println("========================================");
                long startTime = System.currentTimeMillis();
                BBCCrawler bbcCrawler = new BBCCrawler(driver, wait);
                bbcCrawler.crawl(csvWriter, seenUrls);
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("✅ BBC News crawl completed in " + duration + " seconds\n");
            } catch (Exception e) {
                System.err.println("❌ BBC News crawl failed: " + e.getMessage());
                e.printStackTrace();
                System.out.println("⏩ Continuing to next source...\n");
            }

            // Crawl The Guardian
            try {
                System.out.println("========================================");
                System.out.println("🌐 CRAWLING THE GUARDIAN");
                System.out.println("========================================");
                long startTime = System.currentTimeMillis();
                GuardianCrawler guardianCrawler = new GuardianCrawler(driver, wait, js);
                guardianCrawler.crawl(csvWriter, seenUrls);
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("✅ The Guardian crawl completed in " + duration + " seconds\n");
            } catch (Exception e) {
                System.err.println("❌ The Guardian crawl failed: " + e.getMessage());
                e.printStackTrace();
                System.out.println("⏩ Continuing to next source...\n");
            }
            // Crawl Global News
            try {
                System.out.println("========================================");
                System.out.println("🌐 CRAWLING GLOBAL NEWS");
                System.out.println("========================================");
                long startTime = System.currentTimeMillis();
                GlobalNews globalNewsCrawler = new GlobalNews(driver, wait, js);
                globalNewsCrawler.crawl(csvWriter, seenUrls);
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("✅ Global News crawl completed in " + duration + " seconds\n");
            } catch (Exception e) {
                System.err.println("❌ Global News crawl failed: " + e.getMessage());
                e.printStackTrace();
                System.out.println("⏩ Continuing to next source...\n");
            }

            // Crawl CBC News
            try {
                System.out.println("========================================");
                System.out.println("🌐 CRAWLING CBC NEWS");
                System.out.println("========================================");
                long startTime = System.currentTimeMillis();
                CBCCrawler cbcCrawler = new CBCCrawler(driver, wait, js);
                cbcCrawler.crawl(csvWriter, seenUrls);
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("✅ CBC News crawl completed in " + duration + " seconds\n");
            } catch (Exception e) {
                System.err.println("❌ CBC News crawl failed: " + e.getMessage());
                e.printStackTrace();
                System.out.println("⏩ Crawl will now finish...\n");
            }

            totalArticles = seenUrls.size();
            
            System.out.println("========================================");
            System.out.println("✅ CRAWL COMPLETED SUCCESSFULLY");
            System.out.println("========================================");
            System.out.println("📊 Total unique articles crawled: " + totalArticles);
            System.out.println("💾 Output saved to: " + outputPath);
            System.out.println("📂 Local path: ./" + outputPath);
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ CRITICAL ERROR DURING CRAWLING");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n🔧 Cleaning up resources...");
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                    System.out.println("✅ CSV file closed successfully");
                } catch (Exception e) {
                    System.err.println("⚠️ Error closing CSV: " + e.getMessage());
                }
            }
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("✅ Browser driver closed successfully");
                } catch (Exception e) {
                    System.err.println("⚠️ Error closing driver: " + e.getMessage());
                }
            }
            System.out.println("========================================");
            System.out.println("🏁 PROGRAM FINISHED");
            System.out.println("========================================");
        }
    }
}