package com.cucumber.tutorial.context.services.api.notebook;

import com.cucumber.tutorial.context.services.api.HttpService;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.common.StringFormat;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.Map;

@ScenarioScoped
public class NotebookService extends HttpService {

    public static final String NOTEBOOKS_PATH = "/api/notebooks";
    public static final String NOTEBOOK_PATH = "/api/notebooks/#[notebookId]";

    public HttpService buildGetNotebooks(Map<String, String> queryParams) {
        request = getDefaultRequest(Method.GET, uri(address() + NOTEBOOKS_PATH, queryParams));
        return this;
    }

    public HttpService buildGetNotebook(String id) {
        request = getDefaultRequest(Method.GET, uri(address() + StringFormat.replaceProps(NOTEBOOK_PATH,
                Map.of("notebookId", id)), null));
        return this;
    }

    public HttpService buildCreateNotebook(String requestBody) {
        request = getDefaultRequest(Method.POST, uri(address() + NOTEBOOKS_PATH, null));
        request.setEntity(new StringEntity(requestBody));
        return this;
    }

    public HttpService buildUpdateNotebook(String id, String requestBody) {
        request = getDefaultRequest(Method.PATCH, uri(address() + StringFormat.replaceProps(NOTEBOOK_PATH,
                Map.of("notebookId", id)), null));
        request.setEntity(new StringEntity(requestBody));
        return this;
    }

    public HttpService buildDeleteNotebook(String id) {
        request = getDefaultRequest(Method.DELETE, uri(address() + StringFormat.replaceProps(NOTEBOOK_PATH,
                Map.of("notebookId", id)), null));
        return this;
    }

    @Override
    protected String address() {
        return scenarioVars.getAsString("notebook.manager.address");
    }
}
