package com.example.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.crawler.BBCCrawler;
import com.example.crawler.CBCCrawler;
import com.example.crawler.GlobalCrawler;
import com.example.crawler.GuardianCrawler;
import com.example.crawler.NYTimesCrawler;
import com.example.utils.DriverManager;

@Service
public class CrawlerService {

    private static final String OUTPUT_CSV = "all_news_data.csv";
    private static final boolean USE_REMOTE_DRIVER = true;
    private static final String REMOTE_DRIVER_URL = "http://localhost:4444";
    private static final String LOCAL_CHROME_DRIVER_PATH = "C:\\Users\\hp\\Downloads\\chromedriver-win64\\chromedriver.exe";

    @Scheduled(fixedRate = 43200000) // every 12 hours
    public void runCrawlers() {
        WebDriver driver = null;
        CSVWriter csvWriter = null;

        try {
            driver = DriverManager.initializeDriver(USE_REMOTE_DRIVER, REMOTE_DRIVER_URL, LOCAL_CHROME_DRIVER_PATH);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            csvWriter = new CSVWriter(OUTPUT_CSV);
            Set<String> seenUrls = new HashSet<>();

            System.out.println("========================================");
            System.out.println("Starting Scheduled Crawlers");
            System.out.println("========================================");

            // Run all crawlers
            new BBCCrawler(driver, wait).crawl(csvWriter, seenUrls);
            new GlobalCrawler(driver, wait).crawl(csvWriter, seenUrls);
            new GuardianCrawler(driver, wait, js).crawl(csvWriter, seenUrls);
            new CBCCrawler(driver, wait, js).crawl(csvWriter, seenUrls);
            new NYTimesCrawler(driver, wait, js).crawl(csvWriter, seenUrls);

            System.out.println("✅ Crawlers Completed");
            System.out.println("Output saved to: " + OUTPUT_CSV);

            // Upload new CSV rows to MongoDB
            CSVtoMongoUploader.uploadCSV(OUTPUT_CSV, seenUrls);
            System.out.println("✅ New CSV data uploaded to MongoDB successfully!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error during scheduled crawling: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (csvWriter != null) csvWriter.close();
            if (driver != null) driver.quit();
        }
    }
}
