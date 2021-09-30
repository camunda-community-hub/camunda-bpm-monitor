package com.squashedbug.camunda.bpm.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum IncidentMeterTags {

    TENANT_ID("tenant.id"), PROCESS_DEFINITION_ID("process.definition.id"),
    PROCESS_DEFINITION_KEY("process.definition.key"), ACTIVITY_ID("activity.id"),
    FAILED_ACTIVITY_ID("failed.activity.id"), INCIDENT_TYPE("incident.type");

    @Getter
    private String tagName;

    public static Tags createTags(String tenantId, String processDefinitionId, String processDefinitionKey,
            String activityId, String failedActivityId, String incidentType) {
        Tags tags = Tags.empty();

        if (tenantId != null) {
            tags = tags.and(IncidentMeterTags.TENANT_ID.getTagName(), tenantId);
        }

        tags = tags.and(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinitionId);
        tags = tags.and(IncidentMeterTags.PROCESS_DEFINITION_KEY.getTagName(), processDefinitionKey);
        tags = tags.and(IncidentMeterTags.ACTIVITY_ID.getTagName(), activityId);
        if (tenantId != failedActivityId) {
            tags = tags.and(IncidentMeterTags.FAILED_ACTIVITY_ID.getTagName(), failedActivityId);
        }
        tags = tags.and(IncidentMeterTags.INCIDENT_TYPE.getTagName(), incidentType);

        return tags;

    }

}
