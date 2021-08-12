package com.squashedbug.camunda.examples.monitoring;

import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonitorListener {

    private TaggedCounter perProcessDefinitionStart;
    private TaggedCounter perProcessDefinitionEnd;

    @Autowired
    public MonitorListener(MeterRegistry meterRegistry) {
        perProcessDefinitionStart = new TaggedCounter("camunda.process.instances.start", "process.definition.id",
                meterRegistry);
        perProcessDefinitionEnd = new TaggedCounter("camunda.process.instances.end", "process.definition.id",
                meterRegistry);

    }

    @EventListener
    public void onHistoryEvent(HistoryEvent historyEvent) {
        if (historyEvent instanceof HistoricProcessInstanceEventEntity) {
            if (historyEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_START)) {
                onProcessInstanceStart(historyEvent.getProcessDefinitionId());
            } else if (historyEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_END)) {
                onProcessInstanceEnd(historyEvent.getProcessDefinitionId());
            }
        }
    }

    private void onProcessInstanceEnd(String processDefinitionId) {
        perProcessDefinitionEnd.increment(processDefinitionId);
    }

    private void onProcessInstanceStart(String processDefinitionId) {
        perProcessDefinitionStart.increment(processDefinitionId);

    }

}
