package com.cucumber.tutorial;

import io.cucumber.testng.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@CucumberOptions(features = "src/test/resources/features",
        glue = {"com.cucumber.utils", "com.cucumber.tutorial"},
        plugin = {"junit:output", "json:target/cucumber-report/report.json"}, tags = "not @Ignore and not @ignore")
public class TutorialTest implements ITest {
    private final static Logger LOG = LogManager.getLogger();

    private static final Predicate<Pickle> isSerial = pickle -> pickle.getTags().contains("@Serial")
            || pickle.getTags().contains("@serial");

    private final ThreadLocal<String> testName = new ThreadLocal<>();
    private TestNGCucumberRunner testNGCucumberRunner;

    private final AtomicInteger totalTestCount = new AtomicInteger();

    @BeforeClass(alwaysRun = true)
    public void setUpClass(ITestContext context) {
        XmlTest currentXmlTest = context.getCurrentXmlTest();
        Objects.requireNonNull(currentXmlTest);
        CucumberPropertiesProvider properties = currentXmlTest::getParameter;
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass(), properties);
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Parallel Scenarios", dataProvider = "parallelScenarios")
    public void runParallelScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        LOG.info("Running parallel scenario: [{}]", pickleWrapper.getPickle().getName());
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios in the Serial group", dataProvider = "serialScenarios")
    public void runSerialScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
        LOG.info("Running serial scenario: [{}]", pickleWrapper.getPickle().getName());
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

    private Object[][] filter(Object[][] scenarios, Predicate<Pickle> accept) {
        totalTestCount.set(scenarios.length);
        return Arrays.stream(scenarios).filter(objects -> {
            PickleWrapper candidate = (PickleWrapper) objects[0];
            return accept.test(candidate.getPickle());
        }).toArray(Object[][]::new);
    }

    @BeforeMethod
    public void testName(Method method, Object[] testData, ITestContext ctx) {
        if (testData.length > 0) {
            Pickle pickle = ((PickleWrapper) testData[0]).getPickle();
            testName.set(pickle.getName() + " [" + pickle.hashCode() + "]");
            ctx.setAttribute("testName", testName.get());
        } else
            ctx.setAttribute("testName", method.getName());
    }

    @AfterMethod
    public void testResult(ITestResult result) {
        LOG.log(ITestResult.FAILURE == result.getStatus() ? Level.ERROR : Level.INFO,
                "{} | {}", getStatus(result.getStatus()), result.getName());
    }

    @AfterMethod
    public void progress(Method method, Object[] testData, ITestContext ctx) {
        int passed = ctx.getPassedTests().size();
        int failed = ctx.getFailedTests().size();
        int skipped = ctx.getSkippedTests().size();
        LOG.log(failed > 0 ? Level.WARN : Level.INFO, "Progress: {}% (passed: {}, failed: {}, skipped: {}, total: {})",
                (passed + failed + skipped) * 100 / totalTestCount.get(), passed, failed, skipped, totalTestCount.get());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (this.testNGCucumberRunner != null) {
            this.testNGCucumberRunner.finish();
        }
    }

    @Override
    public String getTestName() {
        return testName.get();
    }

    private static String getStatus(int status) {
        switch (status) {
            case ITestResult.FAILURE:
                return "FAILURE";
            case ITestResult.SUCCESS:
                return "SUCCESS";
            case ITestResult.SKIP:
                return "SKIP";
        }
        return "UNKNOWN";
    }
}


