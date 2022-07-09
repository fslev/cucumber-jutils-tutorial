package com.cucumber.tutorial.context.steps.api.notebook;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.hooks.ScenarioCleanup;
import com.cucumber.tutorial.context.services.api.notebook.NotebookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;
import io.jtest.utils.common.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

@ScenarioScoped
public class NotebookSteps extends BaseScenario {
    @Inject
    private NotebookService notebookService;
    @Inject
    private ScenarioCleanup cleanup;

    @Then("Get notebooks with queryParams={} and check {}s until response={}")
    public void getNotebooksAndMatch(Map<String, String> queryParams, Integer pollingTimeout, String expected) {
        notebookService.buildGetNotebooks(queryParams).executeAndMatch(expected, pollingTimeout);
    }

    @Then("Get notebook with id={} and check response={}")
    public void getNotebookAndMatch(String id, String expected) {
        notebookService.buildGetNotebook(id).executeAndMatch(expected);
    }

    @Then("Create notebook with requestBody={} and check response={}")
    public void createNotebookAndMatch(String requestBody, String expected) {
        // If notebook creation was successful, then tag notebook for later cleanup
        notebookService.buildCreateNotebook(requestBody).executeAndMatch(expected, response -> {
            HttpEntity entity = null;
            try (response) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    entity = response.getEntity();
                    JsonNode jsonResponse = JsonUtils.toJson(EntityUtils.toString(entity));
                    cleanup.tagNotebook(jsonResponse.get("id").asText());
                }
            } catch (IOException e) {
                LOG.warn("Cannot extract notebook id from response for later cleanup");
            } finally {
                EntityUtils.consumeQuietly(entity);
            }
        });
    }

    @Then("Update notebook having id={} with requestBody={} and check response={}")
    public void updateNotebookAndMatch(String id, String requestBody, String expected) {
        notebookService.buildUpdateNotebook(id, requestBody).executeAndMatch(expected);
    }

    @Then("Delete notebook with id={} and check response={}")
    public void deleteNotebookAndMatch(String id, String expected) {
        notebookService.buildDeleteNotebook(id).executeAndMatch(expected);
    }
}