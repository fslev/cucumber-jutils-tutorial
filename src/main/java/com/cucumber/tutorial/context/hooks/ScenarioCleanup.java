package com.cucumber.tutorial.context.hooks;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.services.api.notebook.NotebookService;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.After;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ScenarioScoped
public class ScenarioCleanup extends BaseScenario {

    private static final boolean cleanup = Boolean.parseBoolean(System.getProperty("cleanup", "true"));

    private final Set<Notebook> notebooksToCleanSet = new HashSet<>();

    @Inject
    private NotebookService notebookService;

    @After(order = 0)
    public void cleanNotebooks() {
        if (cleanup) {
            scenarioUtils.log("Cleaning notebooks...");
            long affected = notebooksToCleanSet.stream().filter(Notebook::remove).count();
            scenarioUtils.log("Cleaned {} notebooks.", affected);
        }
    }

    public void tagNotebook(String id) {
        this.notebooksToCleanSet.add(new Notebook(id));
    }

    private class Notebook {
        private final String id;

        private Notebook(String id) {
            this.id = id;
        }

        public boolean remove() {
            try {
                notebookService.buildDeleteNotebook(id).executeAndMatch("{\"status\":202}");
                return true;
            } catch (Throwable t) {
                scenarioUtils.log(t);
                return false;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Notebook notebook = (Notebook) o;
            return Objects.equals(id, notebook.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}