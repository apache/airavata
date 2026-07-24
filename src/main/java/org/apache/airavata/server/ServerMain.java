package org.apache.airavata.server;

import org.apache.airavata.server.config.AiravataServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "org.apache.airavata" })
@EntityScan("org.apache.airavata")
@EnableJpaRepositories("org.apache.airavata")
@EnableConfigurationProperties(AiravataServerProperties.class)
public class ServerMain {
    public static void main(String[] args) {
        System.out.println("Starting Airavata Server...");
        SpringApplication.run(ServerMain.class, args);
        // Initialize and start the server components here
    }
}