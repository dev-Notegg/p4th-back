package com.p4th.backend;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync @SpringBootApplication
@EnableTransactionManagement
public class P4thBackendApplication {
    @PostConstruct
    public void init() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
    }


    public static void main(String[] args) {
        SpringApplication.run(P4thBackendApplication.class, args);
    }

}
