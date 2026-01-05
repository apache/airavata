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
package org.apache.airavata;

import org.apache.airavata.config.AiravataPropertiesConfiguration;
import org.apache.airavata.config.AiravataServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot application entry point for Airavata Server.
 */
@SpringBootApplication(
        exclude = {
            org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
            org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
        })
@EnableTransactionManagement
@EnableConfigurationProperties({AiravataServerProperties.class})
@Import({AiravataPropertiesConfiguration.class})
@ComponentScan(
        basePackages = {
            "org.apache.airavata.service",
            "org.apache.airavata.registry.services",
            "org.apache.airavata.registry.repositories",
            "org.apache.airavata.registry.mappers",
            "org.apache.airavata.registry.utils",
            "org.apache.airavata.profile.repositories",
            "org.apache.airavata.profile.mappers",
            "org.apache.airavata.profile.utils",
            "org.apache.airavata.sharing.services",
            "org.apache.airavata.sharing.repositories",
            "org.apache.airavata.sharing.mappers",
            "org.apache.airavata.sharing.utils",
            "org.apache.airavata.credential.repositories",
            "org.apache.airavata.credential.services",
            "org.apache.airavata.credential.utils",
            "org.apache.airavata.common.context",
            "org.apache.airavata.common.exception",
            "org.apache.airavata.common.logging",
            "org.apache.airavata.common.repositories",
            "org.apache.airavata.common.utils",
            "org.apache.airavata.common.validation",
            "org.apache.airavata.accountprovisioning",
            "org.apache.airavata.security",
            "org.apache.airavata.messaging",
            "org.apache.airavata.monitor",
            "org.apache.airavata.orchestrator",
            "org.apache.airavata.helix",
            "org.apache.airavata.config",
            "org.apache.airavata.thriftapi",
            "org.apache.airavata.manager.dbevent",
            "org.apache.airavata.metascheduler",
            "org.apache.airavata.file.server",
            "org.apache.airavata.restapi",
            "org.apache.airavata.agent.connection.service",
            "org.apache.airavata.research.service",
            "org.apache.airavata.bootstrap",
            "org.apache.airavata.cli"
        })
@EntityScan(
        basePackages = {
            "org.apache.airavata.registry.entities",
            "org.apache.airavata.profile.entities",
            "org.apache.airavata.sharing.entities",
            "org.apache.airavata.credential.entities"
        })
public class AiravataServer {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServer.class);

    public static void main(String[] args) {
        logger.info("Starting Airavata Server...");
        SpringApplication app = new SpringApplication(AiravataServer.class);
        app.setDefaultProperties(java.util.Map.of(
                "spring.main.allow-bean-definition-overriding", "true",
                "spring.classformat.ignore", "true",
                "spring.config.name", "airavata"));
        app.setRegisterShutdownHook(true);
        app.run(args);
    }
}
