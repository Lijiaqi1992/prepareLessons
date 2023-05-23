package com.ljq.prepareLessons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PrepareLessonsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrepareLessonsApplication.class, args);
    }

}
