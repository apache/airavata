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
package org.apache.airavata.server;

import org.apache.airavata.config.AiravataPropertiesConfiguration;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.restproxy.RestProxyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Unified Spring Boot application entry point for Airavata Server.
 *
 * <p>This class initializes the Spring application context and enables
 * dependency injection for all Airavata services: API, Agent Service,
 * Research Service, File Server, and optionally REST Proxy or Thrift API.
 *
 * <p>Startup modes:
 * <ul>
 *   <li><b>thrift</b> (default): Starts Thrift API servers, Agent Service,
 *       Research Service, File Server, and all background services.</li>
 *   <li><b>rest</b>: Starts REST Proxy instead of Thrift API, along with
 *       Agent Service, Research Service, File Server, and all background services.</li>
 * </ul>
 *
 * <p>Configuration:
 * <ul>
 *   <li>Set <code>services.thrift.enabled=true</code> for thrift mode (default)</li>
 *   <li>Set <code>services.thrift.enabled=false</code> and <code>services.rest.enabled=true</code> for rest mode</li>
 * </ul>
 */
@SpringBootApplication(
        exclude = {org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class})
@EnableTransactionManagement
@EnableConfigurationProperties({AiravataServerProperties.class, RestProxyConfiguration.class})
@Import({AiravataPropertiesConfiguration.class})
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
            "org.apache.airavata.api.thrift",
            "org.apache.airavata.thriftapi",
            "org.apache.airavata.manager",
            "org.apache.airavata.metascheduler",
            "org.apache.airavata.file.server",
            "org.apache.airavata.restproxy",
            "org.apache.airavata.server.bootstrap"
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\$.*" // Exclude inner classes (Thrift-generated)
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\.cpi\\..*" // Exclude Thrift CPI classes
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = "org\\.apache\\.airavata\\.model\\..*" // Exclude Thrift-generated model classes
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\.agent\\.connection\\.service\\.services\\..*" // Exclude agent service's services
                    // subpackage to avoid AiravataService
                    // conflict
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = "org\\.apache\\.airavata\\.research\\.service\\.AiravataService" // Exclude research
                    // service's
                    // AiravataService to avoid
                    // conflict
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = "org\\.apache\\.airavata\\.agent\\.connection\\.service\\.config\\..*" // Exclude agent
                    // service config to
                    // avoid filter
                    // conflicts
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = "org\\.apache\\.airavata\\.research\\.service\\.config\\..*" // Exclude research service
                    // config to avoid filter
                    // conflicts
                    )
        })
@EntityScan(
        basePackages = {
            "org.apache.airavata.registry.entities",
            "org.apache.airavata.profile.entities",
            "org.apache.airavata.sharing.entities",
            "org.apache.airavata.credential.entities"
        })
@EnableJpaRepositories(basePackages = {"org.apache.airavata.registry.repositories"})
public class UnifiedApplication {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Airavata Unified Server...");

        // Spring Boot will automatically load properties via AiravataPropertiesConfiguration
        // Command line arguments are automatically merged by Spring Boot

        // Start Spring Boot application - this will initialize all beans and run CommandLineRunners
        SpringApplication app = new SpringApplication(UnifiedApplication.class);
        // Enable bean overriding to handle repository name conflicts
        app.setDefaultProperties(java.util.Map.of(
                "spring.main.allow-bean-definition-overriding", "true",
                "spring.classformat.ignore", "true" // Ignore class format issues in Thrift-generated classes
                ));
        // Don't exit immediately - keep running for background services
        app.setRegisterShutdownHook(true);
        app.run(args);
    }
}
