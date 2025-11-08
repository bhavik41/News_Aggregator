package com.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Utils {
    public static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public static String safeGetText(WebElement parent, String cssSelector) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static String safeGetAttribute(WebElement parent, String cssSelector, String attribute) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getAttribute(attribute);
        } catch (Exception e) {
            return "";
        }
    }
}
