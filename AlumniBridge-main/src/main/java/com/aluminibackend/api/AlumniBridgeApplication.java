package com.aluminibackend.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.aluminibackend.api.integration")
public class AlumniBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlumniBridgeApplication.class, args);
	}

}
