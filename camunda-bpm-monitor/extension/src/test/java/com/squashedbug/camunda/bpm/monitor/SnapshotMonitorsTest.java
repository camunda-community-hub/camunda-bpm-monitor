package com.squashedbug.camunda.bpm.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
public class SnapshotMonitorsTest {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    MeterRegistry meterRegistry;

    @Autowired
    SnapshotMonitors snapshotMonitors;

    static final int EXPECTED_COUNT = 3;

    @BeforeEach
    void reset() {

        // Reset meters
        meterRegistry.clear();

        // Delete all process definitions and process instances
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.createProcessDefinitionQuery().list().stream().forEach(
                processDefinition -> repositoryService.deleteProcessDefinition(processDefinition.getId(), true));

    }

    @Test
    void incidentsOpenGauge() {
        // GIVEN process instance with open incident
        ProcessDefinition processDefinition = MonitorTestUtils.createIncidentGeneratingProcessDefinition(processEngine);

        for (int i = 0; i < EXPECTED_COUNT; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        }
        MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

        // WHEN gauges updated
        snapshotMonitors.update();

        // THEN gauge has value as 1
        Gauge gauge = meterRegistry.find(Meters.INCIDENTS_OPEN.getMeterName())
                .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId()).gauge();
        assertEquals(EXPECTED_COUNT, gauge.value());

    }

    @Test
    void processInstancesRunningGauge() {
        // GIVEN process instance running (process waiting for user taks to complete)
        ProcessDefinition processDefinition = MonitorTestUtils.createUserTaskProcessDefinition(processEngine);

        for (int i = 0; i < EXPECTED_COUNT; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        }

        // WHEN gauges updated
        snapshotMonitors.update();

        // THEN gauge has value of expected count
        Gauge gauge = meterRegistry.find(Meters.PROCESS_INSTANCES_RUNNING.getMeterName())
                .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId()).gauge();
        assertEquals(EXPECTED_COUNT, gauge.value());

    }

    @Test
    void processInstancesSuspendedGauge() {
        // GIVEN process instance running (process waiting for user taks to complete)
        ProcessDefinition processDefinition = MonitorTestUtils.createUserTaskProcessDefinition(processEngine);

        for (int i = 0; i < EXPECTED_COUNT; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        }

        processEngine.getRuntimeService().suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

        // WHEN gauges updated
        snapshotMonitors.update();

        // THEN gauge has value of expected count
        Gauge gauge = meterRegistry.find(Meters.PROCESS_INSTANCES_SUSPENDED.getMeterName())
                .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId()).gauge();
        assertEquals(EXPECTED_COUNT, gauge.value());

    }

    @Test
    void externalTaskOpenGauge() {
        // GIVEN process instances running with external task open
        ProcessDefinition processDefinition = MonitorTestUtils.createExternalTaskProcessDefinition(processEngine);

        for (int i = 0; i < EXPECTED_COUNT; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        }

        // WHEN gauges updated
        snapshotMonitors.update();

        // THEN gauge has value of expected count
        Gauge gauge = meterRegistry.find(Meters.EXTERNAL_TASKS_OPEN.getMeterName())
                .tag(ExternalTaskMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId()).gauge();
        assertEquals(EXPECTED_COUNT, gauge.value());

    }

    @Test
    void externalTaskErrorGauge() {
        // GIVEN process instances running with external task in error
        ProcessDefinition processDefinition = MonitorTestUtils.createExternalTaskProcessDefinition(processEngine);

        for (int i = 0; i < EXPECTED_COUNT; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        }

        final String WORKER_ID = "test";
        ExternalTaskService externalTaskService = processEngine.getExternalTaskService();
        externalTaskService.fetchAndLock(EXPECTED_COUNT, WORKER_ID).topic("test-topic", 10).execute().stream()
                .forEach(task -> externalTaskService.handleFailure(task.getId(), WORKER_ID, "error message", 1, 1));

        // WHEN gauges updated
        snapshotMonitors.update();

        // THEN gauge has value of expected count
        Gauge gauge = meterRegistry.find(Meters.EXTERNAL_TASKS_OPEN_ERROR.getMeterName())
                .tag(ExternalTaskMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId()).gauge();
        assertEquals(EXPECTED_COUNT, gauge.value());

    }

    @Test
    void taskOpenGaugeFromProcessInstance() {
        // GIVEN process instances with user tasks open
        ProcessDefinition processDefinition = MonitorTestUtils.createUserTaskProcessDefinition(processEngine);

        for (int i = 0; i < EXPECTED_COUNT; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        }

        // WHEN gauges updated
        snapshotMonitors.update();

        // THEN gauge has value of expected count
        Gauge gauge = meterRegistry.find(Meters.TASKS_OPEN.getMeterName())
                .tag(TaskProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
                .gauge();
        assertEquals(EXPECTED_COUNT, gauge.value());

    }

}
