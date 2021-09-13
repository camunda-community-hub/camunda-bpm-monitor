
package com.squashedbug.camunda.bpm.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
public class MonitorHistoryTest {

        @Autowired
        ProcessEngine processEngine;

        @Autowired
        MeterRegistry meterRegistry;

        @Test
        void contextLoads() {
        }

        @BeforeEach
        void reset() {

                // Reset meters
                meterRegistry.clear();

                // Delete all process definitions and process instances
                RepositoryService repositoryService = processEngine.getRepositoryService();
                repositoryService.createProcessDefinitionQuery().list().stream()
                                .forEach(processDefinition -> repositoryService
                                                .deleteProcessDefinition(processDefinition.getId(), true));

        }

        @Test
        void processInstanceStartedCounter() {
                // GIVEN process definition
                ProcessDefinition processDefinition = MonitorTestUtils.createEmptyProcessDefinition(processEngine);

                // WHEN process instance started
                processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());

                // THEN counter increamented
                Counter counter = meterRegistry.find(Meters.PROCESS_INSTANCES_STARTED.getMeterName())
                                .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(),
                                                processDefinition.getId())
                                .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(),
                                                processDefinition.getKey())
                                .counter();
                assertEquals(1.0d, counter.count());

        }

        @Test
        void processInstanceEndedCounter() {
                // GIVEN process definition
                ProcessDefinition processDefinition = MonitorTestUtils.createEmptyProcessDefinition(processEngine);

                // WHEN process instance started
                processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());

                // THEN counter increamented
                Counter counter = meterRegistry.find(Meters.PROCESS_INSTANCES_ENDED.getMeterName())
                                .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(),
                                                processDefinition.getId())
                                .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(),
                                                processDefinition.getKey())
                                .counter();
                assertEquals(1.0d, counter.count());

        }

        @Test
        void incidentCreatedCounter() {
                // GIVEN process definition that creates incident
                ProcessDefinition processDefinition = MonitorTestUtils
                                .createIncidentGeneratingProcessDefinition(processEngine);

                // WHEN process instance started
                processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
                MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

                // THEN counter incremented
                Counter counter = meterRegistry.find(Meters.INCIDENTS_CREATED.getMeterName())
                                .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
                                .counter();
                assertEquals(1.0d, counter.count());

        }

        @Test
        void incidentResolvedCounter() {
                // GIVEN process definition that creates incident
                ProcessDefinition processDefinition = MonitorTestUtils
                                .createIncidentGeneratingProcessDefinition(processEngine);

                processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
                MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

                Incident incident = processEngine.getRuntimeService().createIncidentQuery()
                                .processDefinitionId(processDefinition.getId()).singleResult();

                // WHEN incident resolved
                processEngine.getRuntimeService().resolveIncident(incident.getId());

                // THEN counter incremented
                Counter counter = meterRegistry.find(Meters.INCIDENTS_RESOLVED.getMeterName())
                                .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
                                .counter();
                assertEquals(1.0d, counter.count());

        }

        @Test
        void incidentDeletedCounter() {
                // GIVEN process definition that creates incident
                ProcessDefinition processDefinition = MonitorTestUtils
                                .createIncidentGeneratingProcessDefinition(processEngine);

                processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
                MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

                // WHEN incident deleted (by removing process instance that has the incident)
                processEngine.getRepositoryService().deleteProcessDefinition(processDefinition.getId(), true);

                // THEN counter incremented
                Counter counter = meterRegistry.find(Meters.INCIDENTS_DELETED.getMeterName())
                                .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
                                .counter();
                assertEquals(1.0d, counter.count());

        }
}
