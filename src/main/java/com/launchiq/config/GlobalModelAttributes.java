package com.launchiq.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @ModelAttribute("appVersion")
    public String appVersion() {
        return appVersion;
    }

    @ModelAttribute("loggedInUser")
    public String loggedInUser(HttpServletRequest request) {
        Object o = request.getSession(false) != null ? request.getSession(false).getAttribute("loggedInUser") : null;
        return o == null ? null : o.toString();
    }

    @ModelAttribute("machineName")
    public String machineName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }

    @ModelAttribute("machineIp")
    public String machineIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    @ModelAttribute("nowDateTime")
    public String nowDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private final SuiteEstimateConfig suiteEstimateConfig;

    public GlobalModelAttributes(SuiteEstimateConfig suiteEstimateConfig) {
        this.suiteEstimateConfig = suiteEstimateConfig;
    }

    @ModelAttribute("suiteEstimatesByName")
    public Map<String,Integer> suiteEstimatesByName() {
        Map<String,Integer> m = new HashMap<>();
        Map<String,Integer> e = suiteEstimateConfig != null ? suiteEstimateConfig.getEstimates() : Collections.emptyMap();
        m.put("Sample Test", e.getOrDefault("sample-test", 60));
        m.put("Daily Checklist", e.getOrDefault("daily-checklist", 900));
        m.put("Deployment Checklist", e.getOrDefault("deployment-checklist", 1200));
        m.put("Smoke Checklist", e.getOrDefault("smoke-checklist", 600));
        m.put("Regression (Default)", e.getOrDefault("regression-default", 7200));
        return m;
    }
}


