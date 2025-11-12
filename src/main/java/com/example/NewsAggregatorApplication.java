package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableScheduling
public class NewsAggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsAggregatorApplication.class, args);
        System.out.println("🚀 News API running on http://localhost:8080/api/news");
    }
}
