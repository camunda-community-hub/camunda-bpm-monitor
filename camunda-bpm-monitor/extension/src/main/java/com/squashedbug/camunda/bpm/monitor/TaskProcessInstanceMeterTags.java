package com.squashedbug.camunda.bpm.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TaskProcessInstanceMeterTags {

    TENANT_ID("tenant.id"), PROCESS_DEFINITION_ID("process.definition.id"),
    PROCESS_DEFINITION_KEY("process.definition.key"), TASK_DEFINITION_KEY("task.definition.key");

    @Getter
    private String tagName;

    public static Tags createTags(String tenantId, String processDefinitionId, String processDefinitionKey,
            String taskDefinitionKey) {
        Tags tags = Tags.empty();

        if (tenantId != null) {
            tags = tags.and(TaskProcessInstanceMeterTags.TENANT_ID.getTagName(), tenantId);
        }

        tags = tags.and(TaskProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinitionId);
        tags = tags.and(TaskProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(), processDefinitionKey);
        tags = tags.and(TaskProcessInstanceMeterTags.TASK_DEFINITION_KEY.getTagName(), taskDefinitionKey);

        return tags;

    }

}
