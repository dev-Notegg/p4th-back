package com.p4th.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P4thBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(P4thBackendApplication.class, args);
    }
}
