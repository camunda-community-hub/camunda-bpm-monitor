package com.squashedbug.camunda.bpm.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExternalTaskMeterTags {

    TENANT_ID("tenant.id"), PROCESS_DEFINITION_ID("process.definition.id"),
    PROCESS_DEFINITION_KEY("process.definition.key"), ACTIVITY_ID("activity.id"), TOPIC_NAME("topic.name");

    @Getter
    private String tagName;

    public static Tags createTags(String tenantId, String processDefinitionId, String processDefinitionKey,
            String activityId, String topicName) {
        Tags tags = Tags.empty();

        if (tenantId != null) {
            tags = tags.and(ExternalTaskMeterTags.TENANT_ID.getTagName(), tenantId);
        }

        tags = tags.and(ExternalTaskMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinitionId);
        tags = tags.and(ExternalTaskMeterTags.PROCESS_DEFINITION_KEY.getTagName(), processDefinitionKey);
        tags = tags.and(ExternalTaskMeterTags.ACTIVITY_ID.getTagName(), activityId);
        tags = tags.and(ExternalTaskMeterTags.TOPIC_NAME.getTagName(), topicName);

        return tags;

    }

}
