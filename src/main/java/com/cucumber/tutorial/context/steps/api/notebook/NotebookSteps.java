package com.cucumber.tutorial.context.steps.api.notebook;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.hooks.ScenarioCleanup;
import com.cucumber.tutorial.context.services.api.notebook.NotebookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;
import io.json.compare.util.JsonUtils;
import org.apache.hc.core5.http.HttpStatus;

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

    @Then("Get notebook with id={} and check {}s until response={}")
    public void getNotebookAndMatch(String id, Integer pollingTimeout, String expected) {
        notebookService.buildGetNotebook(id).executeAndMatch(expected, pollingTimeout);
    }

    @Then("Create notebook with requestBody={} and check response={}")
    public void createNotebookAndMatch(String requestBody, String expected) {
        // If notebook creation was successful, then tag notebook for later cleanup
        notebookService.buildCreateNotebook(requestBody).executeAndMatch(expected, response -> {
            try {
                if (Integer.parseInt(response.getStatus().toString()) == HttpStatus.SC_CREATED) {
                    JsonNode jsonResponse = JsonUtils.toJson(response.getEntity());
                    cleanup.tagNotebook(jsonResponse.get("id").asText());
                }
            } catch (IOException e) {
                LOG.warn("Cannot extract notebook id from response for later cleanup");
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