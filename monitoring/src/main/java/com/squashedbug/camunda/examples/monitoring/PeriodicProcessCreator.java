package com.squashedbug.camunda.examples.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PeriodicProcessCreator {

    @Autowired
    private ProcessEngine processEngine;

    @Scheduled(fixedRate = 5000)
    public void createProcess() {

        UUID id = UUID.randomUUID();

        Map<String, Object> variables = new HashMap<String, Object>();

        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.startProcessInstanceByKey("process", id.toString(), variables);

    }

}
