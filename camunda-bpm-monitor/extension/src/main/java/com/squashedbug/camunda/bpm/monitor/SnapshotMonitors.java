package com.squashedbug.camunda.bpm.monitor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
@PropertySource("classpath:library.properties")
@ConditionalOnProperty(value = "camunda.monitoring.snapshot.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class SnapshotMonitors {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    MeterRegistry meterRegistry;

    List<Monitor> monitors = new ArrayList<>();

    @PostConstruct
    void init() {
        monitors = List.of(new ProcessInstanceSnapshotMonitor(processEngine, meterRegistry),
                new IncidentSnapshotMonitor(processEngine, meterRegistry),
                new TaskSnapshotMonitor(processEngine, meterRegistry),
                new ExternalTaskSnapshotMonitor(processEngine, meterRegistry));
    }

    @Scheduled(fixedDelayString = "${camunda.monitoring.snapshot.updateRate}")
    public void update() {
        monitors.stream().forEach(monitor -> monitor.update());
    }

}
