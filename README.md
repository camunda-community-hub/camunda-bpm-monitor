[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)
# Introduction
This extensions allows you to easily add application monitoring for your Camunda project on Spring Boot.

The extension creates [Micrometer](https://micrometer.io/) gauge and counter meters for Camunda's Process Instances, Incidents, User Tasks, and External Tasks. These metrics are exposed using Spring Boot's Actuator which provides a vendor-nuetral facade to expose metrics to many popular monitoring systems (i.e. Elastic, Prometheus). See [here](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics).

Your chosen monitoring system can then use these metrics for monitoring, health checks and alarming.

# Quick Start Guide
Quick start guide for Maven project
1. Include the monitor extension dependency
``` xml
<dependency>
    <groupId>com.squashedbug.camunda.bpm.extension.monitor</groupId>
    <artifactId>camunda-bpm-spring-boot-monitor</artifactId>
    <version>0.1.7</version>
</dependency>
```
2. Add the `@EnableMonitoring` annotation to your application entrypoint class.
``` java
@SpringBootApplication
@com.squashedbug.camunda.bpm.monitor.EnableMonitoring
public class MyCamundaApp {

    public static void main(String[] args) {

        SpringApplication.run(MyCamundaApp.class, args);
    }

}

```
# Examples
## Simple Example
Sample Camunda Springboot application with the monitoring extension added. The sample application runs a process definition (`examples/monitored-app/src/main/resources/process.bpmn`) that will generate process instances, tasks, stand-alone tasks, external tasks, and incidents to produce the metrics for monitoring.

1. Run the application:
``` sh
cd examples/monitored-app
mvn spring-boot:run
```
2. Camunda metrics are available with Spring Actuator: http://localhost:8080/actuator/prometheus
```
...
# HELP camunda_process_instances_running_suspended_total
# TYPE camunda_process_instances_running_suspended_total gauge
camunda_process_instances_running_suspended_total{process_definition_id="process:1:6e37439c-231a-11ec-b12a-00155dc41a84",process_definition_key="process",} 0.0
# HELP camunda_tasks_open_age_newest_seconds
# TYPE camunda_tasks_open_age_newest_seconds gauge
camunda_tasks_open_age_newest_seconds{task_name="Task A",} 3.0
...
```

## Prometheus Cluster Example
Example setup with a cluster of application nodes running on Docker swarm and monitored with Prometheus.

1. Run the application in docker stack (deploys docker stack called `my-monitored-app`):
    ``` sh
    cd examples/docker-stack-prometheus
    ./run.sh
    ```

2. Wait for the application stack to start-up (may take a couple of minutes) and the 4 Camunda instances in the cluster to be scraped by Prometheus. Status of the target instances can be checked here: http://localhost:9090/targets

3. Run Prometheus queries on camunda metrics: http://localhost:9090

    For example, the process definition called `process` in the application is expected to start every 5 seconds (~60 times in 5 minutes) across all instances, this can be checked with the query:

    ```
    sum(increase(camunda_process_instances_started_total{process_definition_key="process"}[5m]))
    ```
4. Remove the docker stack and its database volume with:
    ``` sh
    docker stack rm my-monitored-app
    docker volume rm my-monitored-app_db-data
    ```

# Guide

## Metrics
The extension provides metrics on Process Instances, Incidents, Tasks, and External Tasks:
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

## Tags
Metrics include the following tags:
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
When running an applcaition in a cluster using a shared database, it is only necassary for **one** of the instances to be retreiving the guage metrics which provide the current snapshot from the database (for example, `camunda.process.instances.running.total` is a guage metric and would have the same value for all instances). However it is important that counter metrics are being monitored on all instances by the monitoring system. For example, `camunda.process.instances.started` metric will provide the count of process instances started on the instance since the instance started, so to alert if a particular process start count drops below a threshold then all instances in the cluster need to be monitored.

The `camunda.monitoring.snapshot.enabled` Spring Boot property can be set to true on a single instance in the cluster.

In the below docker-compose fragment using Docker Stack, one service is set to monitor the snapshots which has only one replica, while the other service which can have many replicas to scale with load has snapshot monitoring disabled. See the `docker-stack-prometheus` example to see this in action.
```
  my-monitored-app:
    image: my-monitored-app
    deploy:
      replicas: 3
    ports:
      - "8080:8080"
    networks:
      - camunda-overlay
    environment:
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/process-engine
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin
      CAMUNDA_MONITORING_SNAPSHOT_ENABLED: "false"

  my-monitored-app-snapshot-enabled:
    image: my-monitored-app
    deploy:
      replicas: 1
    networks:
      - camunda-overlay
    ports:
      - "8081:8080"
    environment:
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/process-engine
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin
      CAMUNDA_MONITORING_SNAPSHOT_ENABLED: "true"
```

## Spring Boot properties
The extension provides the following Spring Boot properties which can be configured in the application (i.e. `resources/application.properties` file or similar).
- `camunda.monitoring.snapshot.enabled` - Whether the gauge snapshot monitoring is enabled. For clusters with multiple instances using the same database only one running instance needs to have this enabled. *Default `true`*
- `camunda.monitoring.snapshot.updateRate` - Rate in milliseconds to refresh the snapshot of metrics. *Default `10000` (10 seconds)*.

## Reporting limitations
The extension uses counter metrics and does not do any lookups on Camunda's history database which keeps the monitoring extension light-weight and scalable. When an instance is stopped or crashes the monitoring system may not have been synced with the latest metrics and when the instance starts again the counts will reset to 0. So the counter metrics in the monitoring system may not be *EXACT*, which is likely important for reporting use cases but not so relevant for application monitoring.

Note: A suitable reporting soltuion for Camunda would either do lookups on the history database or hooked in with a history event handler, which is not the purpose of this extension. See [here](https://docs.camunda.org/manual/latest/user-guide/process-engine/history/).


# License
MIT License
# Maintainer
 - [bsorahan](https://github.com/bsorahan)

 # Development
  The project uses the maven-release-plugin to deploy artifacts to [OSSRH](https://central.sonatype.org/publish/publish-guide/) and release deployments are promoted to [Maven Central](https://search.maven.org/)
 ## Snapshot Deployment
 1. Ensure that the project version is a SNAPSHOT (update POM version as necassary with `mvn versions:set -DnewVersion={{target release}}-SNAPSHOT`)
 2. Deply the snapshot
  ``` sh
mvn clean deploy -P release
 ```
3. Newly deployed snapshot artifacts deployed here: https://s01.oss.sonatype.org/content/repositories/snapshots/com/squashedbug/camunda/bpm/extension/monitor/

 ## Release Deployment (into Maven Central)
 ``` sh
mvn release:clean release:prepare
mvn release:perform
 ```
