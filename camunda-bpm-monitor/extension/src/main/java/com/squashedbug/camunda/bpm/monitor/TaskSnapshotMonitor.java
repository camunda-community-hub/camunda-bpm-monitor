package com.squashedbug.camunda.bpm.monitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class TaskSnapshotMonitor extends Monitor {

    public TaskSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
        super(processEngine, meterRegistry);
    }

    @Override
    protected List<String> getGaugeNames() {
        return Arrays.asList(Meters.TASKS_OPEN, Meters.TASKS_OPEN_NEWEST, Meters.TASKS_OPEN_OLDEST).stream()
                .map(Meters::getMeterName).collect(Collectors.toList());
    }

    @Override
    protected Collection<MultiGaugeData> retrieveGaugesData() {
        Map<Tags, MultiGaugeData> map = new HashMap<>();
        List<Task> tasks = getProcessEngine().getTaskService().createTaskQuery().unlimitedList();

        Date now = new Date();

        for (Task task : tasks) {
            Tags tags;
            if (task.getProcessInstanceId() != null) {
                // Task is related to a process instance
                ProcessDefinition processDefinition = getProcessDefinition(task.getProcessDefinitionId());
                tags = TaskProcessInstanceMeterTags.createTags(task.getTenantId(), task.getProcessDefinitionId(),
                        processDefinition.getKey(), task.getTaskDefinitionKey());

            } else if (task.getCaseInstanceId() != null) {
                // Task is related to a case instance
                tags = TaskCaseInstanceMeterTags.createTags(task.getTenantId(), task.getCaseDefinitionId(),
                        task.getTaskDefinitionKey());

            } else {
                // Task is stand-alone
                tags = TaskStandAloneMeterTags.createTags(task.getTenantId(), task.getName());
            }

            MultiGaugeData data = map.get(tags);

            if (data == null) {
                Map<String, Long> gaugeValues = new HashMap<>();
                gaugeValues.put(Meters.TASKS_OPEN_NEWEST.getMeterName(), Long.MAX_VALUE);
                gaugeValues.put(Meters.TASKS_OPEN_OLDEST.getMeterName(), Long.MIN_VALUE);
                gaugeValues.put(Meters.TASKS_OPEN.getMeterName(), Long.valueOf(0));

                data = new MultiGaugeData(gaugeValues, tags);
            }

            long ageSeconds = (now.getTime() - task.getCreateTime().getTime()) / 1000;

            data.gaugesValues.merge(Meters.TASKS_OPEN_NEWEST.getMeterName(), ageSeconds, Long::min);
            data.gaugesValues.merge(Meters.TASKS_OPEN_OLDEST.getMeterName(), ageSeconds, Long::max);
            data.gaugesValues.merge(Meters.TASKS_OPEN.getMeterName(), Long.valueOf(1), Long::sum);

            map.put(tags, data);

        }

        return map.values();
    }

}
