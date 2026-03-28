package org.apache.airavata.agent.connection.service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "org.apache.airavata.agent.connection.service")
@EntityScan(basePackages = "org.apache.airavata.agent.connection.service")
@EnableJpaRepositories(basePackages = "org.apache.airavata.agent.connection.service")
public class AgentServiceConfig {
}
