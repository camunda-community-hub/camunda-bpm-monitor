package com.squashedbug.camunda.bpm.extension.monitor.examples.monitored.app;

import com.squashedbug.camunda.bpm.monitor.EnableMonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
@SpringBootApplication(scanBasePackages = { "com.squashedbug.camunda.bpm.extension.monitor",
		"com.squashedbug.camunda.bpm.monitor" })
*/
@SpringBootApplication
@EnableMonitoring
public class MonitoredApp {

	public static void main(String[] args) {

		SpringApplication.run(MonitoredApp.class, args);
	}

}
