package com.launchiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "repo")
public class RepoConfig {
    private boolean enabled = false;
    private String url;
    private String branch = "main";
    private String suiteFile = "src/test/resources/testng.xml";
    private String tokenEnc;
    private String usernameEnc;
    private String passwordEnc;
    private String reportsDir = "C:/LaunchIQ/reports";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getSuiteFile() { return suiteFile; }
    public void setSuiteFile(String suiteFile) { this.suiteFile = suiteFile; }
    public String getTokenEnc() { return tokenEnc; }
    public void setTokenEnc(String tokenEnc) { this.tokenEnc = tokenEnc; }
    public String getUsernameEnc() { return usernameEnc; }
    public void setUsernameEnc(String usernameEnc) { this.usernameEnc = usernameEnc; }
    public String getPasswordEnc() { return passwordEnc; }
    public void setPasswordEnc(String passwordEnc) { this.passwordEnc = passwordEnc; }
    public String getReportsDir() { return reportsDir; }
    public void setReportsDir(String reportsDir) { this.reportsDir = reportsDir; }
}


