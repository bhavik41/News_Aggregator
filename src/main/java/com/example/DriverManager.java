package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

/**
 * Robust driver initializer: retries remote Selenium, falls back to local driver.
 */
class DriverManager {
    private static final int REMOTE_RETRY = 6;
    private static final long RETRY_DELAY_MS = 20;

    public static WebDriver initializeDriver(boolean useRemote, String remoteUrl, String localPath) throws Exception {
        if (useRemote) {
            // try both with and without /wd/hub (some selenium setups require it)
            String[] candidates = new String[] { 
                remoteUrl, 
                remoteUrl.endsWith("/wd/hub") ? remoteUrl : remoteUrl + "/wd/hub" 
            };
            Exception lastEx = null;
            
            for (String candidate : candidates) {
                for (int i = 1; i <= REMOTE_RETRY; i++) {
                    try {
                        if (!isRemoteAvailable(candidate)) {
                            throw new IllegalStateException("Selenium remote not reachable at " + candidate);
                        }
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--no-sandbox");
                        options.addArguments("--disable-dev-shm-usage");
                        options.addArguments("--start-maximized");
                        options.addArguments("--disable-gpu");
                        options.addArguments("--disable-blink-features=AutomationControlled");
                        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                        // helpful when running in containerized selenium
                        options.setExperimentalOption("w3c", true);
                        return new RemoteWebDriver(new URL(candidate), options);
                    } catch (Exception e) {
                        lastEx = e;
                        System.err.println("Remote driver connect attempt " + i + " to " + candidate + " failed: " + e.getMessage());
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                }
            }
            throw new Exception("Failed to connect to remote Selenium. Last error: " + (lastEx == null ? "unknown" : lastEx.getMessage()), lastEx);
        } else {
            // local chrome driver
            System.setProperty("webdriver.chrome.driver", localPath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            return new ChromeDriver(options);
        }
    }

    private static boolean isRemoteAvailable(String urlStr) {
        try {
            URL statusUrl = new URL(urlStr.endsWith("/status") || urlStr.contains("/wd/hub") ? urlStr : urlStr + "/status");
            HttpURLConnection conn = (HttpURLConnection) statusUrl.openConnection();
            conn.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
            conn.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            return code >= 200 && code < 500; // selenium may return 200 or 403 etc but reachable is good
        } catch (Exception e) {
            return false;
        }
    }
}