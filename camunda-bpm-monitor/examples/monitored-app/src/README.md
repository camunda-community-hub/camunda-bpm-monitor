# Overview
Camunda monitoring adds metric collection on process instances, incidents, tasks, and external tasks for Camunda BPM running in a Spring Boot application.

Metrics are exposed using [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) which can then be used as part of your application monitoring solution (for example, with [Prometheus](https://prometheus.io/))

# Usage


## Metrics
- `camunda.process.instances.started`
- `camunda.process.instances.ended`
- `camunda.process.instances.running.total`
- `camunda.process.instances.running.suspended.total`
- `camunda.incidents.created`
- `camunda.incidents.deleted`
- `camunda.incidents.resolved`
- `camunda.incidents.open.total`
- `camunda.incidents.open.age.newest.seconds`
- `camunda.incidents.open.age.oldest.seconds`
- `camunda.tasks.created`
- `camunda.tasks.completed`
- `camunda.tasks.deleted`
- `camunda.tasks.open.total`
- `camunda.tasks.open.age.newest.seconds`
- `camunda.tasks.open.age.oldest.seconds`
- `camunda.external.tasks.started`
- `camunda.external.tasks.ended`
- `camunda.external.tasks.open.total`
- `camunda.external.tasks.open.error.total`




Micrometer adds Micrometer meters spring boot
Micrometer

History event listener for counter meters

Gauge meters for open process instance, incidents, tasks, and external tasks

## Implementations

## Example monitoring processes using Prometheus
TODO


## Tags
- Process instance tags:
    - `tenant.id`
    - `process.definition.id`
    - `process.definition.key`
- Incident tags:
    - `tenant.id`
    - `process.definition.id`
    - `process.definition.key`
    - `activity.id`
    - `failed.activity.id`
    - `incident.type`
- User task (related to a process instance) tags:
    - `tenant.id`
    - `process.definition.id`
    - `process.definition.key`
    - `task.definition.id`
- User task (related to a case instance) tags:
    - `tenant.id`
    - `case.definition.id`
    - `task.definition.id`
- User task (created stand-alone) tags:
    - `tenant.id`
    - `task.name`
- External task tags:
    - `tenant.id`
    - `topic.name`
    - `activity.id`
    - `process.definition.id`
    - `process.definition.key`

## Cluster considerations


## Properties
- `camunda.monitoring.snapshot.enabled` - Whether the gauge snapshot monitoring is enabled. The snapshot monitoringFor clusters with multiple instances using the same database only one running instance needs to have this enabled. *Default `true`*
- `camunda.monitoring.snapshot.updateRate` - Rate in milliseconds to refresh the snapshot of metrics. *Default `10000` (10 seconds)*.
