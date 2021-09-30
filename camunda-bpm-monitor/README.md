# Introduction
This extensions allows you to easily add application monitoring for your Camunda project on Spring Boot.

The extension creates [Micrometer](https://micrometer.io/) gauge and counter meters for Camunda's Process Instances, Incidents, User Tasks, and External Tasks.

Micrometer is the metrics collection component included in Spring Boot's Actuator which provides a vendor-nuetral facade to expose metrics to many popular monitoring systems (i.e. Elastic, Prometheus). See [here](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics).

# Quick Start Guide
Quick start guide for Maven project
1. Include the `spring-boot-starter-actuator` dependencies and the extensions monitoring dependencies
``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
2. Include the monitoring repository in your project
3. Add the `EnableMonitoring` annotation to your application entrypoint class.
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
 - Sample Camunda Springboot application with the monitoring extension added
 - Example production-like setup with a cluster of application nodes running on Docker swarm and monitored with Prometheus.
# License
MIT License
# Maintainer
 - [bsorahan](https://github.com/bsorahan)
