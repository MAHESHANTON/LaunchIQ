package com.launchiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "suite.estimate")
public class SuiteEstimateConfig {
    private Map<String, Integer> estimates = new HashMap<>();

    public Map<String, Integer> getEstimates() {
        return estimates;
    }

    public void setEstimates(Map<String, Integer> estimates) {
        this.estimates = estimates;
    }
}


