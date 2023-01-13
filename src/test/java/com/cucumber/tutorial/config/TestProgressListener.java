package com.cucumber.tutorial.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.concurrent.atomic.AtomicInteger;

public class TestProgressListener implements TestExecutionListener {
    private static final Logger LOG = LogManager.getLogger(TestProgressListener.class);
    private final AtomicInteger testPassedCount = new AtomicInteger();
    private final AtomicInteger testFailedCount = new AtomicInteger();
    private final AtomicInteger testAbortedCount = new AtomicInteger();


    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        LOG.info("--- TEST PLAN EXECUTION STARTED ---");
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            LOG.info("Running scenario [{}]", testIdentifier.getDisplayName());
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            LOG.log(testExecutionResult.getStatus().equals(TestExecutionResult.Status.FAILED) ? Level.ERROR : Level.INFO,
                    "{} | {}", testExecutionResult.getStatus(), testIdentifier.getDisplayName());
            if (testExecutionResult.getStatus().equals(TestExecutionResult.Status.SUCCESSFUL)) {
                testPassedCount.incrementAndGet();
            } else if (testExecutionResult.getStatus().equals(TestExecutionResult.Status.FAILED)) {
                testFailedCount.incrementAndGet();
            } else if (testExecutionResult.getStatus().equals(TestExecutionResult.Status.ABORTED)) {
                testAbortedCount.incrementAndGet();
            }
            LOG.log(testFailedCount.get() > 0 ? Level.WARN : Level.INFO,
                    "Executed: {} | Passed: {} | Failed: {} | Aborted: {}", testPassedCount.get() + testFailedCount.get(),
                    testPassedCount.get(), testFailedCount.get(), testAbortedCount.get());
        }
    }
}
