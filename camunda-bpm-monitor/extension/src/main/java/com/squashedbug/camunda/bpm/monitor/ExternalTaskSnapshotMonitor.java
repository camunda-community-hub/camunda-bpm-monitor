package com.squashedbug.camunda.bpm.monitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class ExternalTaskSnapshotMonitor extends Monitor {

    public ExternalTaskSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
        super(processEngine, meterRegistry);
    }

    @Override
    protected List<String> getGaugeNames() {
        return Arrays.asList(Meters.EXTERNAL_TASKS_OPEN, Meters.EXTERNAL_TASKS_OPEN_ERROR).stream()
                .map(Meters::getMeterName).collect(Collectors.toList());
    }

    @Override
    protected Collection<MultiGaugeData> retrieveGaugesData() {
        Map<Tags, MultiGaugeData> map = new HashMap<>();
        List<ExternalTask> tasks = getProcessEngine().getExternalTaskService().createExternalTaskQuery()
                .unlimitedList();

        for (ExternalTask task : tasks) {
            Tags tags = ExternalTaskMeterTags.createTags(task.getTenantId(), task.getProcessDefinitionId(),
                    task.getProcessDefinitionKey(), task.getActivityId(), task.getTopicName());

            MultiGaugeData data = map.get(tags);

            if (data == null) {
                Map<String, Long> gaugeValues = new HashMap<>();
                gaugeValues.put(Meters.EXTERNAL_TASKS_OPEN.getMeterName(), Long.valueOf(0));
                gaugeValues.put(Meters.EXTERNAL_TASKS_OPEN_ERROR.getMeterName(), Long.valueOf(0));

                data = new MultiGaugeData(gaugeValues, getTagsForProcessDefinition(task.getProcessDefinitionId()));
            }

            data.gaugesValues.merge(Meters.EXTERNAL_TASKS_OPEN.getMeterName(), Long.valueOf(1), Long::sum);
            if (task.getErrorMessage() != null) {
                data.gaugesValues.merge(Meters.EXTERNAL_TASKS_OPEN_ERROR.getMeterName(), Long.valueOf(1), Long::sum);
            }

            map.put(tags, data);

        }

        return map.values();
    }
}
