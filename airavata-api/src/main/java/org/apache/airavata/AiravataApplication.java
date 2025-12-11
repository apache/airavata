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
 * Spring Boot application entry point for Airavata API.
 *
 * <p>This class initializes the Spring application context and enables
 * dependency injection for services, repositories, and entities.
 *
 * <p>Startup sequence:
 * <ol>
 *   <li>Load and merge application settings from properties files and command line args</li>
 *   <li>Initialize Spring Boot application context</li>
 *   <li>Background services are started via {@link org.apache.airavata.config.BackgroundServicesLauncher}
 *       (Order 1-7): Helix Controller, Global Participant, Workflow Managers, Monitors</li>
 *   <li>Thrift servers are started via {@link org.apache.airavata.config.ThriftServerLauncher}
 *       (Order 10): API Server, Registry Server, Credential Store, etc.</li>
 * </ol>
 *
 * <p>All services run in daemon threads and the main thread is kept alive to prevent
 * the application from exiting.
 */
@SpringBootApplication(
        exclude = {org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class})
@EnableTransactionManagement
@EnableConfigurationProperties(AiravataServerProperties.class)
@Import(AiravataPropertiesConfiguration.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.service",
            "org.apache.airavata.registry",
            "org.apache.airavata.profile",
            "org.apache.airavata.sharing",
            "org.apache.airavata.credential",
            "org.apache.airavata.messaging",
            "org.apache.airavata.monitor",
            "org.apache.airavata.orchestrator",
            "org.apache.airavata.helix",
            "org.apache.airavata.config",
            "org.apache.airavata.api.thrift"
        },
        excludeFilters = {
            @org.springframework.context.annotation.ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\$.*"  // Exclude inner classes (Thrift-generated)
            ),
            @org.springframework.context.annotation.ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\.cpi\\..*"  // Exclude Thrift CPI classes
            )
        })
@EntityScan(
        basePackages = {
            "org.apache.airavata.registry.entities",
            "org.apache.airavata.profile.entities",
            "org.apache.airavata.sharing.entities",
            "org.apache.airavata.credential.entities"
        })
public class AiravataApplication {

    private static final Logger logger = LoggerFactory.getLogger(AiravataApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Airavata Spring Boot application...");
        // Spring Boot will automatically load properties via AiravataPropertiesConfiguration
        // Command line arguments are automatically merged by Spring Boot

        // Start Spring Boot application - this will initialize all beans and run CommandLineRunners
        SpringApplication app = new SpringApplication(AiravataApplication.class);
        // Enable bean overriding to handle repository name conflicts
        app.setDefaultProperties(java.util.Map.of("spring.main.allow-bean-definition-overriding", "true"));
        // Don't exit immediately - keep running for background services
        app.setRegisterShutdownHook(true);
        app.run(args);
    }
}
