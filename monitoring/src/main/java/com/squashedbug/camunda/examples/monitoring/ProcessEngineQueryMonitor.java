package com.squashedbug.camunda.examples.monitoring;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;

@Component
@Profile("monitoring")
public class ProcessEngineQueryMonitor {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    MeterRegistry meterRegistry;

    MultiGauge processInstanceRunningCountGauge;
    MultiGauge processInstanceRunningSuspendedCountGauge;

    MultiGauge incidentOpenCountGauge;
    MultiGauge incidentOpenOldestAgeSecondsGauge;
    MultiGauge incidentOpenNewestAgeSecondsGauge;

    @PostConstruct

    public void init() {
        // Process Instance monitors
        processInstanceRunningCountGauge = MultiGauge.builder("camunda.process.instances.running.total")
                .register(meterRegistry);
        processInstanceRunningSuspendedCountGauge = MultiGauge
                .builder("camunda.process.instances.running.suspended.total").register(meterRegistry);
        updateProcessInstanceGauges();

        // Incident monitors
        incidentOpenCountGauge = MultiGauge.builder("camunda.incidents.open.total").register(meterRegistry);
        incidentOpenOldestAgeSecondsGauge = MultiGauge.builder("camunda.incidents.oldest.age.seconds")
                .register(meterRegistry);
        incidentOpenNewestAgeSecondsGauge = MultiGauge.builder("camunda.incidents.newest.age.seconds")
                .register(meterRegistry);
        updateIncidentGauges();
    }

    @Scheduled(fixedDelay = 10000)
    public void updateProcessInstanceGauges() {
        List<ProcessDefinitionInstanceData> data = retrieveProcessDefinitionInstanceData();
        processInstanceRunningCountGauge.register(
                data.stream().map(ProcessDefinitionInstanceData::toCountRow).collect(Collectors.toList()), true);
        processInstanceRunningSuspendedCountGauge.register(
                data.stream().map(ProcessDefinitionInstanceData::toSuspendedCountRow).collect(Collectors.toList()),
                true);
    }

    @Scheduled(fixedDelay = 10000)
    public void updateIncidentGauges() {
        List<ProcessDefinitionIncidentData> data = retrieveProcessDefinitionIncidentData();
        incidentOpenCountGauge.register(
                data.stream().map(ProcessDefinitionIncidentData::toCountRow).collect(Collectors.toList()), true);
        incidentOpenOldestAgeSecondsGauge.register(
                data.stream().map(ProcessDefinitionIncidentData::toOldestAgeRow).collect(Collectors.toList()), true);
        incidentOpenNewestAgeSecondsGauge.register(
                data.stream().map(ProcessDefinitionIncidentData::toNewestAgeRow).collect(Collectors.toList()), true);

    }

    List<ProcessDefinitionInstanceData> retrieveProcessDefinitionInstanceData() {
        HashMap<String, ProcessDefinitionInstanceData> map = new HashMap<String, ProcessDefinitionInstanceData>();
        List<ProcessInstance> pis = processEngine.getRuntimeService().createProcessInstanceQuery().unlimitedList();

        for (ProcessInstance pi : pis) {
            ProcessDefinitionInstanceData data = map.getOrDefault(pi.getProcessDefinitionId(),
                    new ProcessDefinitionInstanceData(pi.getProcessDefinitionId()));

            data.incrementCount();
            if (pi.isSuspended()) {
                data.incrementSuspendedCount();
            }

            map.put(pi.getProcessDefinitionId(), data);

        }

        return new ArrayList<ProcessDefinitionInstanceData>(map.values());
    }

    List<ProcessDefinitionIncidentData> retrieveProcessDefinitionIncidentData() {
        HashMap<String, ProcessDefinitionIncidentData> map = new HashMap<String, ProcessDefinitionIncidentData>();
        List<Incident> incs = processEngine.getRuntimeService().createIncidentQuery().unlimitedList();

        Date now = new Date();
        for (Incident inc : incs) {
            ProcessDefinitionIncidentData data = map.getOrDefault(inc.getProcessDefinitionId(),
                    new ProcessDefinitionIncidentData(inc.getProcessDefinitionId()));

            long ageSeconds = (now.getTime() - inc.getIncidentTimestamp().getTime()) / 1000;
            data.incrementCount();
            data.updateOldestAndNewestAgeWith(ageSeconds);

            map.put(inc.getProcessDefinitionId(), data);

        }

        return new ArrayList<ProcessDefinitionIncidentData>(map.values());
    }

    class ProcessDefinitionInstanceData {
        String processDefinitionId;
        long count = 0;
        long suspendedCount = 0;

        public ProcessDefinitionInstanceData(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }

        public void incrementCount() {
            count++;
        }

        public void incrementSuspendedCount() {
            suspendedCount++;
        }

        public MultiGauge.Row<ProcessDefinitionInstanceData> toCountRow() {
            return MultiGauge.Row.of(Tags.of("process.defintion.id", processDefinitionId), this, c -> this.count);
        }

        public MultiGauge.Row<ProcessDefinitionInstanceData> toSuspendedCountRow() {
            return MultiGauge.Row.of(Tags.of("process.defintion.id", processDefinitionId), this,
                    c -> this.suspendedCount);
        }

    }

    class ProcessDefinitionIncidentData {
        String processDefinitionId;
        long count = 0;
        Long oldestAge = null;
        Long newestAge = null;

        public ProcessDefinitionIncidentData(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }

        public void incrementCount() {
            count++;
        }

        public void updateOldestAndNewestAgeWith(long ageSeconds) {
            oldestAge = (oldestAge == null) ? ageSeconds : Math.max(oldestAge, ageSeconds);
            newestAge = (newestAge == null) ? ageSeconds : Math.min(newestAge, ageSeconds);
        }

        public MultiGauge.Row<ProcessDefinitionIncidentData> toCountRow() {
            return MultiGauge.Row.of(Tags.of("process.defintion.id", processDefinitionId), this, c -> this.count);
        }

        public MultiGauge.Row<ProcessDefinitionIncidentData> toNewestAgeRow() {
            return MultiGauge.Row.of(Tags.of("process.defintion.id", processDefinitionId), this, c -> this.newestAge);
        }

        public MultiGauge.Row<ProcessDefinitionIncidentData> toOldestAgeRow() {
            return MultiGauge.Row.of(Tags.of("process.defintion.id", processDefinitionId), this, c -> this.oldestAge);
        }

    }

}
