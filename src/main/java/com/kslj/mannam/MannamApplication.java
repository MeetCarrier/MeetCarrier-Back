package com.kslj.mannam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MannamApplication {

	public static void main(String[] args) {
		SpringApplication.run(MannamApplication.class, args);
	}

}
