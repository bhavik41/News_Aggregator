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
            return new RemoteWebDriver(new URL(remoteUrl), options);
        } else {
            System.setProperty("webdriver.chrome.driver", localPath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-gpu", "--no-sandbox", "--window-size=1920,1080");
            return new ChromeDriver(options);
        }
    }
}