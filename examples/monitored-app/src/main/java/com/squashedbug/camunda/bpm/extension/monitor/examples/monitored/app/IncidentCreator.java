package com.squashedbug.camunda.bpm.extension.monitor.examples.monitored.app;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class IncidentCreator implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        execution.createIncident("incident", "incident", "incident");
    }

}
