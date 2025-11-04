
package com.launchiq.service;

import org.testng.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LiveTestListener implements ITestListener, ISuiteListener {

    private final String runId;
    private final ExecutionService service;
    private AtomicInteger total = new AtomicInteger(0);
    private AtomicInteger executed = new AtomicInteger(0);
    private AtomicInteger passed = new AtomicInteger(0);
    private AtomicInteger failed = new AtomicInteger(0);
    private AtomicInteger skipped = new AtomicInteger(0);

    public LiveTestListener(String runId, ExecutionService service) {
        this.runId = runId;
        this.service = service;
    }

    @Override public void onTestStart(ITestResult result) {
        total.incrementAndGet();
        executed.incrementAndGet();
        service.updateCounters(runId, total.get(), executed.get(), passed.get(), failed.get(), skipped.get());
    }
    @Override public void onTestSuccess(ITestResult result) {
        passed.incrementAndGet();
        service.updateCounters(runId, total.get(), executed.get(), passed.get(), failed.get(), skipped.get());
    }
    @Override public void onTestFailure(ITestResult result) {
        failed.incrementAndGet();
        service.updateCounters(runId, total.get(), executed.get(), passed.get(), failed.get(), skipped.get());
    }
    @Override public void onTestSkipped(ITestResult result) {
        skipped.incrementAndGet();
        service.updateCounters(runId, total.get(), executed.get(), passed.get(), failed.get(), skipped.get());
    }
    @Override public void onStart(ISuite suite) { }
    @Override public void onFinish(ISuite suite) { }
    @Override public void onStart(ITestContext context) { }
    @Override public void onFinish(ITestContext context) { }
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
}
