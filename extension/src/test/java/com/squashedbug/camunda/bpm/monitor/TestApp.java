package com.squashedbug.camunda.bpm.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMonitoring
public class TestApp {

	public static void main(String[] args) {

		SpringApplication.run(TestApp.class, args);
	}

}
