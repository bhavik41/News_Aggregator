package com.example;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.crawler.BBCCrawler;
import com.example.crawler.CBCCrawler;
import com.example.crawler.GlobalCrawler;
import com.example.crawler.GuardianCrawler;
import com.example.crawler.NYTimesCrawler;
import com.example.service.CSVWriter;
import com.example.service.CSVtoMongoUploader;
import com.example.utils.DriverManager;

public class Main {
    private static final String OUTPUT_CSV = "all_news_data.csv";
    private static final boolean USE_REMOTE_DRIVER = true;
    private static final String LOCAL_CHROME_DRIVER_PATH = "C:\\Users\\hp\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe";

    private static String getRemoteDriverUrl() {
        String host = System.getenv("SELENIUM_HOST");
        if (host == null || host.isEmpty()) host = "localhost";
        String port = System.getenv("SELENIUM_PORT");
        if (port == null || port.isEmpty()) port = "4444";
        return "http://" + host + ":" + port;
    }

    public static void main(String[] args) {
        WebDriver driver = null;
        CSVWriter csvWriter = null;

        try {
            // Initialize WebDriver
            driver = DriverManager.initializeDriver(USE_REMOTE_DRIVER, getRemoteDriverUrl(), LOCAL_CHROME_DRIVER_PATH);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            csvWriter = new CSVWriter(OUTPUT_CSV);
            Set<String> seenUrls = new HashSet<>(); // to avoid duplicate articles in CSV

            System.out.println("========================================");
            System.out.println("Starting Crawlers");
            System.out.println("========================================");

            BBCCrawler bbcCrawler = new BBCCrawler(driver, wait);
            bbcCrawler.crawl(csvWriter, seenUrls);

                GlobalCrawler globalCrawler = new GlobalCrawler(driver, wait);
                globalCrawler.crawl(csvWriter, seenUrls);

                GuardianCrawler guardianCrawler = new GuardianCrawler(driver, wait, js);
                guardianCrawler.crawl(csvWriter, seenUrls);

                CBCCrawler cbcCrawler = new CBCCrawler(driver, wait, js);
                cbcCrawler.crawl(csvWriter, seenUrls);

                NYTimesCrawler nyTimesCrawler = new NYTimesCrawler(driver, wait, js);
                nyTimesCrawler.crawl(csvWriter, seenUrls);



            System.out.println("✅ Crawlers Completed");
            System.out.println("Output saved to: " + OUTPUT_CSV);

            // Upload only new CSV rows to MongoDB
            CSVtoMongoUploader.uploadCSV(OUTPUT_CSV, seenUrls);
            System.out.println("✅ New CSV data uploaded to MongoDB successfully!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error during crawling: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (csvWriter != null) csvWriter.close();
            if (driver != null) driver.quit();
        }
    }
}
