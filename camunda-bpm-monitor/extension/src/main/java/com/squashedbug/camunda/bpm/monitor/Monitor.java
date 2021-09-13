package com.squashedbug.camunda.bpm.monitor;

import java.util.Collection;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.MultiGauge.Row;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public abstract class Monitor {

    private ProcessEngine processEngine;
    private MeterRegistry meterRegistry;

    Map<String, MultiGauge> multiGaugeMap = new HashMap<>();

    public Monitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
        this.processEngine = processEngine;
        this.meterRegistry = meterRegistry;
        multiGaugeMap = getGaugeNames().stream().distinct().collect(Collectors.toMap(Function.identity(),
                gaugeName -> MultiGauge.builder(gaugeName).register(meterRegistry)));

        update();
    }

    protected abstract List<String> getGaugeNames();

    protected abstract Collection<MultiGaugeData> retrieveGaugesData();

    public void update() {
        Collection<MultiGaugeData> gaugesData = retrieveGaugesData();

        multiGaugeMap.entrySet().stream()
                .forEach(gaugeEntry -> gaugeEntry.getValue().register(
                        gaugesData.stream().map(d -> getRow(gaugeEntry.getKey(), d)).collect(Collectors.toList()),
                        true));
    }

    private Row<MultiGaugeData> getRow(String gaugeName, MultiGaugeData d) {
        return Row.of(d.getTags(), d, gaugeData -> gaugeData.gaugesValues.get(gaugeName));
    }

    protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
        return processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
    }

    protected Tags getTagsForProcessDefinition(String processDefinitionId) {
        return addTagsForProcessDefinition(Tags.empty(), processDefinitionId);
    }

    protected Tags addTagsForProcessDefinition(Tags tags, String processDefinitionId) {
        ProcessDefinition pd = getProcessDefinition(processDefinitionId);
        if (pd.getTenantId() != null) {
            tags.and("tenant.id", pd.getTenantId());
        }
        return tags.and("process.definition.id", pd.getId()).and("process.definition.key", pd.getKey());

    }

    @AllArgsConstructor
    @Getter
    protected class MultiGaugeData {
        Map<String, Long> gaugesValues;
        Tags tags;
    }
}
