package com.cucumber.tutorial.context.steps.db;

import com.cucumber.tutorial.client.SqlClient;
import com.cucumber.tutorial.context.BaseScenario;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.jtest.utils.common.ResourceUtils;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@ScenarioScoped
public class SqlSteps extends BaseScenario {

    private SqlClient client;

    @Given("[sql] Load data source from file path {}")
    public void setDataSource(String filePath) throws IOException {
        Properties dataSource = ResourceUtils.readProps(filePath);
        this.client = new SqlClient(dataSource.getProperty("url"), dataSource.getProperty("username"),
                dataSource.getProperty("password"), dataSource.getProperty("driver").trim());
    }

    @Then("[sql] Execute query {} and check result={}")
    public void executeQueryAndMatchWithJson(String query, List<Map<String, Object>> expected) throws SQLException {
        executeQueryAndMatch(query, expected);
    }

    @Then("[sql] Execute query {} and check result is")
    public void executeQueryAndMatchWithTable(String query, List<Map<String, Object>> expected) throws SQLException {
        executeQueryAndMatch(query, expected);
    }

    public void executeQueryAndMatch(String query, Object expected) throws SQLException {
        scenarioUtils.log("Execute query '{}' and match with: {}", query, expected);
        try {
            this.client.connect();
            this.client.prepareStatement(query);
            List<Map<String, Object>> result = client.executeQueryAndGetRsAsList();
            scenarioVars.putAll(ObjectMatcher.match(null, expected, result, MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_NON_EXTENSIBLE_ARRAY));
        } finally {
            this.client.close();
        }
    }

    @Then("[util] Execute query {} and check {}s until result is")
    public void executeQueryAndMatch(String query, Integer pollingTimeoutSeconds, List<Map<String, Object>> expected) throws SQLException {
        scenarioUtils.log("Execute query '{}' and check for {} until result = {}", query, pollingTimeoutSeconds, expected);
        try {
            this.client.connect();
            this.client.prepareStatement(query);
            scenarioVars.putAll(ObjectMatcher.match(null, expected, () -> {
                        try {
                            return client.executeQueryAndGetRsAsList();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }, Duration.ofSeconds(pollingTimeoutSeconds),
                    null, null, MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_NON_EXTENSIBLE_ARRAY));
        } finally {
            this.client.close();
        }
    }
}
