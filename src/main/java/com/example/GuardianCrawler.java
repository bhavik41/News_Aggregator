package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;

class GuardianCrawler {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    
    public GuardianCrawler(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.js = js;
    }
    
    public void crawl(CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            driver.get("https://www.theguardian.com/international");
            System.out.println("Accessing The Guardian...");
            dismissCookies();

            // Only crawl 2 sections for speed
            String[][] sections = {
                {"News", "https://www.theguardian.com/"},
                {"Tech", "https://www.theguardian.com/technology"}
            };

            for (String[] section : sections) {
                crawlSection(section[0], section[1], csvWriter, seenUrls);
            }
        } catch (Exception e) {
            System.err.println("❌ Error crawling Guardian: " + e.getMessage());
        }
    }
    
    private void dismissCookies() {
        try {
            long timeout = 15000;
            long start = System.currentTimeMillis();
            
            while (System.currentTimeMillis() - start < timeout) {
                List<WebElement> buttons = driver.findElements(By.xpath(
                    "//button[contains(., 'Yes, I accept') or contains(., 'Yes, I'm happy')]"));
                
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed()) {
                        js.executeScript("arguments[0].click();", btn);
                        System.out.println("Guardian cookie dismissed.");
                        Utils.sleep(1000);
                        return;
                    }
                }
                
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                for (WebElement frame : frames) {
                    try {
                        driver.switchTo().frame(frame);
                        List<WebElement> iframeButtons = driver.findElements(By.xpath(
                            "//button[contains(., 'Yes, I accept') or contains(., 'Yes, I'm happy')]"));
                        for (WebElement btn : iframeButtons) {
                            if (btn.isDisplayed()) {
                                js.executeScript("arguments[0].click();", btn);
                                System.out.println("Guardian cookie dismissed (iframe).");
                                driver.switchTo().defaultContent();
                                return;
                            }
                        }
                    } catch (Exception e) {}
                    driver.switchTo().defaultContent();
                }
                break;
            }
        } catch (Exception e) {
            System.out.println("No Guardian cookie popup.");
        }
    }
    
    private void crawlSection(String sectionName, String url, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            driver.get(url);
            System.out.println("Crawling Guardian: " + sectionName);
            Utils.sleep(1000); // Reduced wait time

            List<WebElement> articles = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("a[data-link-name='article']")));

            int count = 0;
            for (WebElement article : articles) {
                try {
                    if (extractArticle(sectionName, article, csvWriter, seenUrls)) {
                        count++;
                    }
                    if (count >= 10) break; // Only 10 articles per section
                } catch (Exception e) {
                    System.err.println("⚠️ Skipping article in " + sectionName + ": " + e.getMessage());
                }
            }
            System.out.println("✅ Extracted " + count + " articles from " + sectionName);
        } catch (Exception e) {
            System.err.println("❌ Error on Guardian section " + sectionName + ": " + e.getMessage());
        }
    }
    
    private boolean extractArticle(String section, WebElement article, CSVWriter csvWriter, Set<String> seenUrls) {
        try {
            String headline = article.getText().trim();
            String link = article.getAttribute("href");
            
            if (headline.isEmpty() || link.isEmpty() || seenUrls.contains(link)) {
                return false;
            }
            
            String description = "";
            String time = "";
            String imageLink = "";
            
            try {
                WebElement parent = article.findElement(By.xpath("./.."));
                List<WebElement> divs = parent.findElements(By.cssSelector("div"));
                for (WebElement div : divs) {
                    String text = div.getText().trim();
                    if (!text.isEmpty() && !text.equals(headline)) {
                        description = text;
                        break;
                    }
                }
            } catch (Exception e) {}
            
            try {
                WebElement timeEl = article.findElement(By.xpath(".//time"));
                time = timeEl.getText().trim();
            } catch (Exception e) {
                try {
                    WebElement guIsland = article.findElement(By.tagName("gu-island"));
                    WebElement shadowTime = (WebElement) js.executeScript(
                        "return arguments[0].shadowRoot.querySelector('time')", guIsland);
                    time = shadowTime.getText().trim();
                } catch (Exception e2) {}
            }
            
            try {
                WebElement imgEl = article.findElement(By.tagName("img"));
                imageLink = imgEl.getAttribute("src");
            } catch (Exception e) {}
            
            seenUrls.add(link);
            csvWriter.writeRow("The Guardian", section, headline, description, time, section, link, imageLink);
            System.out.println("✓ Guardian: " + headline.substring(0, Math.min(50, headline.length())));
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
}