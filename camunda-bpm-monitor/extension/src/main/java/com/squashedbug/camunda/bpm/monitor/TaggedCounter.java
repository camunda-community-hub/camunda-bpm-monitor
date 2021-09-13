package com.squashedbug.camunda.bpm.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import java.util.HashMap;
import java.util.Map;

public class TaggedCounter {
    private MeterRegistry registry;
    String name;
    private Map<Tags, Counter> counters = new HashMap<>();

    public TaggedCounter(String name, MeterRegistry registry) {
        this.name = name;
        this.registry = registry;
    }

    public void increment(Tags tags) {
        Counter counter = counters.get(tags);
        if (counter == null) {
            counter = Counter.builder(name).tags(tags).register(registry);
            counters.put(tags, counter);
        }
        counter.increment();
    }
}
