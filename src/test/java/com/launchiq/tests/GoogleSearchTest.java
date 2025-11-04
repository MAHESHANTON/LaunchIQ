
package com.launchiq.tests;

import com.launchiq.util.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

public class GoogleSearchTest {

    private WebDriver driver;

    @BeforeMethod
    public void setup() throws Exception {
        Map<String,String> cloud = new HashMap<>();
        driver = WebDriverFactory.createDriver(cloud, "chrome", "Local");
    }

    @Test
    public void googleSearch() {
        driver.get("https://www.google.com");
        driver.findElement(By.name("q")).sendKeys("Selenium Test Automation");
        driver.findElement(By.name("q")).submit();
        String title = driver.getTitle();
        Assert.assertTrue(title.toLowerCase().contains("selenium"));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
