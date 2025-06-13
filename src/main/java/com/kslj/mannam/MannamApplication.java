package com.kslj.mannam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@EnableAsync
@SpringBootApplication
public class MannamApplication {

	public static void main(String[] args) {
		SpringApplication.run(MannamApplication.class, args);
	}

}
