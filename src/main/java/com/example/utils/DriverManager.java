package com.example.utils;

import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverManager {
    public static WebDriver initializeDriver(boolean useRemote, String remoteUrl, String localPath) throws Exception {
        if (useRemote) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--start-maximized");
            // Retry connecting to remote Selenium a few times to allow container startup
            int attempts = 0;
            int maxAttempts = 10;
            while (true) {
                try {
                    attempts++;
                    System.out.println("Attempting to connect to remote Selenium: " + remoteUrl + " (attempt " + attempts + ")");
                    return new RemoteWebDriver(new URL(remoteUrl), options);
                } catch (Exception e) {
                    if (attempts >= maxAttempts) {
                        System.err.println("Failed to connect to remote Selenium after " + attempts + " attempts");
                        throw e;
                    }
                    System.out.println("Remote Selenium not reachable yet, retrying in 2s...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        } else {
            System.setProperty("webdriver.chrome.driver", localPath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-gpu", "--no-sandbox", "--window-size=1920,1080");
            return new ChromeDriver(options);
        }
    }
}