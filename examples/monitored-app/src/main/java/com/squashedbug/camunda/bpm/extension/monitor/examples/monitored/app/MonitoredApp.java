package com.squashedbug.camunda.bpm.extension.monitor.examples.monitored.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@com.squashedbug.camunda.bpm.monitor.EnableMonitoring
public class MonitoredApp {

	public static void main(String[] args) {

		SpringApplication.run(MonitoredApp.class, args);
	}

}
