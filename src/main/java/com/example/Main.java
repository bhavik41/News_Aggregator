package com.example;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Main {
    private static final String OUTPUT_CSV = "all_news_data.csv";
    private static final boolean USE_REMOTE_DRIVER = true;
    private static final String REMOTE_DRIVER_URL = "http://localhost:4444";
    private static final String LOCAL_CHROME_DRIVER_PATH = "C:\\Path\\To\\chromedriver.exe";
    
    public static void main(String[] args) {
        WebDriver driver = null;
        CSVWriter csvWriter = null;
        
        try {
            driver = DriverManager.initializeDriver(USE_REMOTE_DRIVER, REMOTE_DRIVER_URL, LOCAL_CHROME_DRIVER_PATH);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            csvWriter = new CSVWriter(OUTPUT_CSV);
            Set<String> seenUrls = new HashSet<>();
            
            int cycleCount = 0;
            while (true) {
                cycleCount++;
                System.out.println("========================================");
                System.out.println("Starting Crawl Cycle #" + cycleCount);
                System.out.println("========================================");

                System.out.println("Starting BBC News Crawl");
                System.out.println("========================================");
                BBCCrawler bbcCrawler = new BBCCrawler(driver, wait);
                bbcCrawler.crawl(csvWriter, seenUrls);

                System.out.println("\n========================================");
                System.out.println("Starting The Guardian Crawl");
                System.out.println("========================================");
                GuardianCrawler guardianCrawler = new GuardianCrawler(driver, wait, js);
                guardianCrawler.crawl(csvWriter, seenUrls);

                System.out.println("\n========================================");
                System.out.println("Starting CBC News Crawl");
                System.out.println("========================================");
                CBCCrawler cbcCrawler = new CBCCrawler(driver, wait, js);
                cbcCrawler.crawl(csvWriter, seenUrls);

                System.out.println("\n========================================");
                System.out.println("✅ Crawl Cycle #" + cycleCount + " completed!");
                System.out.println("Output saved to: " + OUTPUT_CSV);
                System.out.println("Continuing to next cycle immediately...");
                System.out.println("========================================");

                // Small delay between cycles to avoid overwhelming the sites
                try {
                    Thread.sleep(5000); // 5 seconds
                } catch (InterruptedException e) {
                    System.out.println("Crawl interrupted, stopping...");
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during crawling: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (csvWriter != null) csvWriter.close();
            if (driver != null) driver.quit();
        }
    }
}
