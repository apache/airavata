/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.bootstrap;

import java.util.HashMap;
import org.apache.airavata.config.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot application entry point for Airavata Server.
 */
@SpringBootApplication(
        scanBasePackages = "org.apache.airavata",
        exclude = {
            org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
            org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties(ServerProperties.class)
@EntityScan(
        basePackages = {
            "org.apache.airavata.research.experiment.entity",
            "org.apache.airavata.execution.entity",
            "org.apache.airavata.compute.resource.entity",
            "org.apache.airavata.accounting.entity",
            "org.apache.airavata.compute.usage.job.entity",
            "org.apache.airavata.research.application.entity",
            "org.apache.airavata.gateway.entity",
            "org.apache.airavata.iam.entity",
            "org.apache.airavata.iam.entity",
            "org.apache.airavata.workflow.entity",
            "org.apache.airavata.status.entity",
            "org.apache.airavata.credential.model",
            "org.apache.airavata.research.artifact.entity",
            "org.apache.airavata.agent.entity",
            "org.apache.airavata.research.service.model.entity"
        })
public class AiravataServer {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServer.class);

    public static void main(String[] args) {
        logger.info("Starting Airavata Server...");
        var app = new SpringApplication(AiravataServer.class);

        var defaultProps = new HashMap<String, Object>();
        defaultProps.put("spring.main.allow-bean-definition-overriding", "true");
        defaultProps.put("spring.classformat.ignore", "true");
        defaultProps.put("spring.config.name", "airavata");

        // Build exclude list for spring.autoconfigure.exclude
        var excludeList = new StringBuilder();
        excludeList.append("org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,");
        excludeList.append("org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,");
        excludeList.append("org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration");

        logger.info("gRPC configurations will load last after all core services");

        defaultProps.put("spring.autoconfigure.exclude", excludeList.toString());
        app.setDefaultProperties(defaultProps);
        app.setRegisterShutdownHook(true);
        app.run(args);
    }
}
