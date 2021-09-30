package com.squashedbug.camunda.bpm.extension.monitor.examples.monitored.app;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpressionFunctions {

    @Autowired
    ProcessEngine processEngine;

    public String getRandomDateWithinSeconds(int seconds) {
        ZonedDateTime d = ZonedDateTime.now().plusSeconds(new Random().nextInt(seconds) + 1);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(d);
    }

    public void throwException() throws Exception {
        // throw new Exception("an exception");
    }

    public void createTask(String tenantId, String taskName) {

        Task task = processEngine.getTaskService().newTask();
        task.setName(taskName);
        task.setTenantId(tenantId);
        processEngine.getTaskService().saveTask(task);

    }
}
