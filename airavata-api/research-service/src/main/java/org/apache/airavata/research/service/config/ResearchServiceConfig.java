package org.apache.airavata.research.service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "org.apache.airavata.research.service")
@EntityScan(basePackages = "org.apache.airavata.research.service")
@EnableJpaRepositories(basePackages = "org.apache.airavata.research.service")
@EnableJpaAuditing
public class ResearchServiceConfig {
}
