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
import org.camunda.bpm.engine.runtime.Incident;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class IncidentSnapshotMonitor extends Monitor {

    public IncidentSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
        super(processEngine, meterRegistry);
    }

    @Override
    protected List<String> getGaugeNames() {
        return Arrays.asList(Meters.INCIDENTS_OPEN, Meters.INCIDENTS_OPEN_NEWEST, Meters.INCIDENTS_OPEN_OLDEST).stream()
                .map(Meters::getMeterName).collect(Collectors.toList());
    }

    @Override
    protected Collection<MultiGaugeData> retrieveGaugesData() {
        Map<String, MultiGaugeData> map = new HashMap<>();
        List<Incident> incs = getProcessEngine().getRuntimeService().createIncidentQuery().unlimitedList();

        Date now = new Date();

        for (Incident inc : incs) {
            String groupByKey = inc.getProcessDefinitionId();
            MultiGaugeData data = map.get(groupByKey);

            if (data == null) {
                Map<String, Long> gaugeValues = new HashMap<>();
                gaugeValues.put(Meters.INCIDENTS_OPEN_NEWEST.getMeterName(), Long.MAX_VALUE);
                gaugeValues.put(Meters.INCIDENTS_OPEN_OLDEST.getMeterName(), Long.MIN_VALUE);
                gaugeValues.put(Meters.INCIDENTS_OPEN.getMeterName(), Long.valueOf(0));

                ProcessDefinition processDefinition = getProcessDefinition(inc.getProcessDefinitionId());
                Tags tags = IncidentMeterTags.createTags(processDefinition.getTenantId(), processDefinition.getId(),
                        processDefinition.getKey(), inc.getActivityId(), inc.getFailedActivityId(),
                        inc.getIncidentType());

                data = new MultiGaugeData(gaugeValues, tags);
            }

            long ageSeconds = (now.getTime() - inc.getIncidentTimestamp().getTime()) / 1000;

            data.gaugesValues.merge(Meters.INCIDENTS_OPEN_NEWEST.getMeterName(), ageSeconds, Long::min);
            data.gaugesValues.merge(Meters.INCIDENTS_OPEN_OLDEST.getMeterName(), ageSeconds, Long::max);
            data.gaugesValues.merge(Meters.INCIDENTS_OPEN.getMeterName(), Long.valueOf(1), Long::sum);

            map.put(groupByKey, data);

        }

        return map.values();
    }

}
