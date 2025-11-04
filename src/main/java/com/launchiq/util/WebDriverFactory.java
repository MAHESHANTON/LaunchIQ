
package com.launchiq.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.Map;

public class WebDriverFactory {

    public static WebDriver createDriver(Map<String,String> cloudSettings, String browser, String cloudProvider) throws Exception {
        if (cloudProvider == null || cloudProvider.equalsIgnoreCase("Local")) {
            // Use WebDriverManager to auto-manage chromedriver (works on Windows)
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver();
        } else {
            String username = cloudSettings.getOrDefault("username", "");
            String accessKey = cloudSettings.getOrDefault("accessKey", "");
            String gridUrl = cloudSettings.getOrDefault("gridUrl", "");
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("browserName", browser);
            caps.setCapability("platform", "Windows 10");
            caps.setCapability("build", "LaunchIQ Run");
            caps.setCapability("name", "LaunchIQ Test");

            String remoteUrl = gridUrl;
            if (!remoteUrl.startsWith("http")) {
                remoteUrl = "https://" + username + ":" + accessKey + "@" + gridUrl;
            }
            return new RemoteWebDriver(new URL(remoteUrl), caps);
        }
    }
}
