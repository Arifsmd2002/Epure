package com.project2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectEpureApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ProjectEpureApplication.class);
		app.setWebApplicationType(WebApplicationType.SERVLET);
		app.run(args);
	}

}
