package com.personal.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonalAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalAssistantApplication.class, args);
	}

}
