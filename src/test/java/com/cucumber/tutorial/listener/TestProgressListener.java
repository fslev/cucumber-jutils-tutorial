package com.cucumber.tutorial.listener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class TestProgressListener implements TestExecutionListener {
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println(testPlan.countTestIdentifiers(ti -> ti.isTest()));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            System.out.println(testIdentifier.getDisplayName());
            System.out.println(testIdentifier.getTags());
            System.out.println(testExecutionResult.getStatus());
        }
    }
}
