package com.cucumber.tutorial.context;

import com.cucumber.utils.context.ScenarioUtils;
import com.cucumber.utils.context.vars.ScenarioVars;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseScenario {
    protected static final Logger LOG = LogManager.getLogger();
    @Inject
    protected ScenarioUtils scenarioUtils;
    @Inject
    protected ScenarioVars scenarioVars;
}
