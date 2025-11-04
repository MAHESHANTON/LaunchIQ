
package com.launchiq.service;

import org.springframework.stereotype.Service;
import com.launchiq.config.RepoConfig;
import org.testng.TestNG;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExecutionService {

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> counters = new ConcurrentHashMap<>();
    private final Map<String, String> statusMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();
    private final Map<String, Instant> startTimes = new ConcurrentHashMap<>();

    private final RepoRunnerService repoRunnerService;
    private final RepoConfig repoConfig;

    public ExecutionService(RepoRunnerService repoRunnerService, RepoConfig repoConfig) {
        this.repoRunnerService = repoRunnerService;
        this.repoConfig = repoConfig;
    }

    public void startRun(String runId, String suiteXmlPath, Map<String,String> systemProps) {
        progressMap.put(runId, 0);
        statusMap.put(runId, "RUNNING");
        counters.put(runId, new HashMap<>());
        stopFlags.put(runId, new AtomicBoolean(false));
        startTimes.put(runId, Instant.now());

        new Thread(() -> {
            try {
                if (repoConfig.isEnabled()) {
                    // External repo driven execution via Maven
                    repoRunnerService.runExternalMavenSuite(runId);
                } else {
                    TestNG testng = new TestNG(false);
                    List<String> suites = new ArrayList<>();
                    suites.add(suiteXmlPath);
                    testng.setTestSuites(suites);
                    LiveTestListener listener = new LiveTestListener(runId, this);
                    testng.addListener((Object) listener);
                    if (systemProps != null) systemProps.forEach(System::setProperty);
                    testng.run();
                }

                Map<String,Integer> c = counters.getOrDefault(runId, new HashMap<>());
                int passed = c.getOrDefault("passed", 0);
                int failed = c.getOrDefault("failed", 0);
                int skipped = c.getOrDefault("skipped", 0);
                int total = c.getOrDefault("total", 0);
                String status = (failed > 0) ? "FAILED" : "PASSED";
                statusMap.put(runId, status);

                DBHelper.saveExecutionRecord("suite", systemProps.getOrDefault("product",""), systemProps.getOrDefault("environment",""),
                        systemProps.getOrDefault("browser",""), systemProps.getOrDefault("cloud","Local"),
                        total, passed, failed, skipped, status, "reports/extent-" + runId + ".html");
                progressMap.put(runId, 100);
            } catch (Throwable t) {
                t.printStackTrace();
                statusMap.put(runId, "ERROR: " + t.getMessage());
            } finally {
                stopFlags.remove(runId);
            }
        }).start();
    }

    public int getProgress(String runId) {
        return progressMap.getOrDefault(runId, 0);
    }
    public Map<String,Integer> getCounters(String runId) {
        return counters.getOrDefault(runId, Map.of("total",0,"executed",0,"passed",0,"failed",0,"skipped",0));
    }
    public String getStatus(String runId) {
        return statusMap.getOrDefault(runId, "UNKNOWN");
    }
    public void requestStop(String runId) {
        AtomicBoolean f = stopFlags.get(runId);
        if (f != null) f.set(true);
        statusMap.put(runId, "STOP_REQUESTED");
    }
    public void updateCounters(String runId, int total, int executed, int passed, int failed, int skipped) {
        Map<String,Integer> c = counters.computeIfAbsent(runId, k-> new HashMap<>());
        c.put("total", total);
        c.put("executed", executed);
        c.put("passed", passed);
        c.put("failed", failed);
        c.put("skipped", skipped);
        int p = total==0 ? 0 : (int)((executed*100.0)/total);
        progressMap.put(runId, p);
    }
    public long getElapsedSeconds(String runId) {
        Instant s = startTimes.get(runId);
        if (s == null) return 0;
        return java.time.Duration.between(s, Instant.now()).getSeconds();
    }
}
