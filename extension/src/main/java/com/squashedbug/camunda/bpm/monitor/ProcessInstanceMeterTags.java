package com.squashedbug.camunda.bpm.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ProcessInstanceMeterTags {

    TENANT_ID("tenant.id"), PROCESS_DEFINITION_ID("process.definition.id"),
    PROCESS_DEFINITION_KEY("process.definition.key");

    @Getter
    private String tagName;

    public static Tags createTags(String tenantId, String processDefinitionId, String processDefinitionKey) {
        Tags tags = Tags.empty();
        if (tenantId != null) {
            tags.and("tenant.id", tenantId);
        }
        return tags.and("process.definition.id", processDefinitionId).and("process.definition.key",
                processDefinitionKey);

    }

}
