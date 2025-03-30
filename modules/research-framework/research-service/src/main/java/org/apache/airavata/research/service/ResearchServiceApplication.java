package org.apache.airavata.research.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories()
public class ResearchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchServiceApplication.class, args);
    }
}
