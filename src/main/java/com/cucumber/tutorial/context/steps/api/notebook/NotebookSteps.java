package com.cucumber.tutorial.context.steps.api.notebook;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.hooks.ScenarioCleanup;
import com.cucumber.tutorial.context.services.api.notebook.NotebookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;
import io.json.compare.util.JsonUtils;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

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
            HttpEntity entity = null;
            try (response) {
                if (response.getCode() == HttpStatus.SC_CREATED) {
                    entity = response.getEntity();
                    JsonNode jsonResponse = JsonUtils.toJson(EntityUtils.toString(entity));
                    cleanup.tagNotebook(jsonResponse.get("id").asText());
                }
            } catch (IOException | ParseException e) {
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