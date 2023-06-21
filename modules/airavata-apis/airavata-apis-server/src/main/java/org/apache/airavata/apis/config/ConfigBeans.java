package org.apache.airavata.apis.config;

import org.apache.airavata.api.execution.ExperimentService;
import org.apache.airavata.apis.scheduling.ExperimentLauncher;
import org.apache.airavata.apis.scheduling.MetaScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigBeans {
    @Bean
    public MetaScheduler metaScheduler() {
        return new MetaScheduler();
    }

    @Bean
    ExperimentLauncher experimentLauncher() {
        return new ExperimentLauncher();
    }
}
