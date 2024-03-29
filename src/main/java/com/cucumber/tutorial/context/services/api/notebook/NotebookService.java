package com.cucumber.tutorial.context.services.api.notebook;

import com.cucumber.tutorial.context.services.api.HttpService;
import io.cucumber.guice.ScenarioScoped;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@ScenarioScoped
public class NotebookService extends HttpService {

    public static final String NOTEBOOKS_PATH = "/api/notebooks";
    public static final String NOTEBOOK_PATH = "/api/notebooks/#[notebookId]";

    public HttpService buildGetNotebooks(Map<String, String> queryParams) {
        request = getDefaultRequest(Method.GET, uri(NOTEBOOKS_PATH, null, queryParams));
        return this;
    }

    public HttpService buildGetNotebook(String id) {
        request = getDefaultRequest(Method.GET, uri(NOTEBOOK_PATH, Map.of("notebookId", id)));
        return this;
    }

    public HttpService buildCreateNotebook(String requestBody) {
        request = getDefaultRequest(Method.POST, uri(NOTEBOOKS_PATH));
        request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        return this;
    }

    public HttpService buildUpdateNotebook(String id, String requestBody) {
        request = getDefaultRequest(Method.PATCH, uri(NOTEBOOK_PATH, Map.of("notebookId", id)));
        request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        return this;
    }

    public HttpService buildDeleteNotebook(String id) {
        request = getDefaultRequest(Method.DELETE, uri(NOTEBOOK_PATH, Map.of("notebookId", id)));
        return this;
    }

    @Override
    protected String address() {
        return scenarioVars.getAsString("notebook.manager.address");
    }
}
