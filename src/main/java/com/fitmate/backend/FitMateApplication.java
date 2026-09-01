package com.fitmate.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // jpa 감시
public class FitMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitMateApplication.class, args);
	}

}
