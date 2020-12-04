package com.cucumber.tutorial;

import io.cucumber.testng.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@CucumberOptions(features = "src/test/resources/features",
        glue = {"com.cucumber.utils", "com.cucumber.tutorial"},
        plugin = {"pretty", "junit:output", "json:target/cucumber-report/report.json"}, tags = "not @Ignore and not @ignore")
public class TutorialTest implements ITest {
    private final static Logger LOG = LogManager.getLogger();

    private static final Predicate<Pickle> isSerial = pickle -> pickle.getTags().contains("@Serial")
            || pickle.getTags().contains("@serial");

    private final ThreadLocal<String> testName = new ThreadLocal<>();
    private TestNGCucumberRunner testNGCucumberRunner;

    private final AtomicInteger totalTestCount = new AtomicInteger();
    private final AtomicInteger testCount = new AtomicInteger();

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Parallel Scenarios", dataProvider = "parallelScenarios")
    public void runParallelScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {
        LOG.info("Preparing Parallel scenario ---> {}", pickleWrapper.getPickle().getName());
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios in the Serial group", dataProvider = "serialScenarios")
    public void runSerialScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {
        LOG.info("Preparing Serial scenario ---> {}", pickleWrapper.getPickle().getName());
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    @DataProvider(parallel = true)
    public Object[][] parallelScenarios() {
        if (testNGCucumberRunner == null) {
            totalTestCount.set(1);
            return new Object[0][0];
        }
        return filter(testNGCucumberRunner.provideScenarios(), isSerial.negate());
    }

    @DataProvider
    public Object[][] serialScenarios() {
        if (testNGCucumberRunner == null) {
            totalTestCount.set(1);
            return new Object[0][0];
        }
        return filter(testNGCucumberRunner.provideScenarios(), isSerial);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (testNGCucumberRunner == null) {
            return;
        }
        testNGCucumberRunner.finish();
    }

    private Object[][] filter(Object[][] scenarios, Predicate<Pickle> accept) {
        totalTestCount.set(scenarios.length);
        return Arrays.stream(scenarios).filter(objects -> {
            PickleWrapper candidate = (PickleWrapper) objects[0];
            return accept.test(candidate.getPickle());
        }).toArray(Object[][]::new);
    }

    @BeforeMethod
    public void BeforeMethod(Method method, Object[] testData, ITestContext ctx) {
        if (testData.length > 0) {
            testName.set(testData[0].toString());
            ctx.setAttribute("testName", testName.get());
        } else
            ctx.setAttribute("testName", method.getName());
    }

    @AfterMethod
    public void AfterMethod(Method method, Object[] testData, ITestContext ctx) {
        LOG.info("Finished scenario {}/{}. Progress: {}%", testCount.incrementAndGet(), totalTestCount.get(),
                (testCount.get() * 100) / totalTestCount.get());
    }

    @Override
    public String getTestName() {
        return testName.get();
    }
}


