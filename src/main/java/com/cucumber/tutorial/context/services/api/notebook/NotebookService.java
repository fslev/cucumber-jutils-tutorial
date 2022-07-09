package com.cucumber.tutorial.context.services.api.notebook;

import com.cucumber.tutorial.context.services.api.HttpService;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.clients.http.Method;
import io.jtest.utils.common.StringFormat;

import java.util.Map;

@ScenarioScoped
public class NotebookService extends HttpService {

    public static final String NOTEBOOKS_PATH = "/api/notebooks";
    public static final String NOTEBOOK_PATH = "/api/notebooks/#[notebookId]";


    public HttpService buildGetNotebooks(Map<String, String> queryParams) {
        this.client = getBuilder().path(NOTEBOOKS_PATH).queryParams(queryParams).method(Method.GET).build();
        return this;
    }

    public HttpService buildGetNotebook(String id) {
        this.client = getBuilder().path(StringFormat.replaceProps(NOTEBOOK_PATH, Map.of("notebookId", id)))
                .method(Method.GET).build();
        return this;
    }

    public HttpService buildCreateNotebook(String requestBody) {
        this.client = getBuilder().path(NOTEBOOKS_PATH).method(Method.POST).entity(requestBody).build();
        return this;
    }

    public HttpService buildUpdateNotebook(String id, String requestBody) {
        this.client = getBuilder().path(StringFormat.replaceProps(NOTEBOOK_PATH, Map.of("notebookId", id)))
                .method(Method.PATCH).entity(requestBody).build();
        return this;
    }

    public HttpService buildDeleteNotebook(String id) {
        this.client = getBuilder().path(StringFormat.replaceProps(NOTEBOOK_PATH, Map.of("notebookId", id)))
                .method(Method.DELETE).build();
        return this;
    }

    @Override
    protected String address() {
        return scenarioVars.getAsString("notebook.manager.address");
    }
}
