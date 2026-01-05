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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ThriftMapperImplExcludeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot application entry point for Airavata Server.
 */
@SpringBootApplication(
        exclude = {
            org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
            org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@EnableTransactionManagement
@EnableConfigurationProperties({AiravataServerProperties.class})
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
            "org.apache.airavata.agents",
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
            "org.apache.airavata.bootstrap"
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = ThriftMapperImplExcludeFilter.class))
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
        
        Map<String, Object> defaultProps = new HashMap<>();
        defaultProps.put("spring.main.allow-bean-definition-overriding", "true");
        defaultProps.put("spring.classformat.ignore", "true");
        defaultProps.put("spring.config.name", "airavata");
        
        // Build exclude list for spring.autoconfigure.exclude
        StringBuilder excludeList = new StringBuilder();
        excludeList.append("org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,");
        excludeList.append("org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,");
        excludeList.append("org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration");
        
        // Conditionally exclude gRPC auto-configuration if research service is disabled
        boolean researchEnabled = isResearchServiceEnabled();
        if (!researchEnabled) {
            excludeList.append(",org.springframework.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration");
            logger.debug("Research service is disabled, excluding gRPC auto-configuration");
        }
        
        defaultProps.put("spring.autoconfigure.exclude", excludeList.toString());
        app.setDefaultProperties(defaultProps);
        app.setRegisterShutdownHook(true);
        app.run(args);
    }
    
    /**
     * Check if research service is enabled by reading from system properties or airavata.properties.
     */
    private static boolean isResearchServiceEnabled() {
        // Check system property first
        String systemProp = System.getProperty("services.research.enabled");
        if (systemProp != null) {
            return "true".equalsIgnoreCase(systemProp);
        }
        
        // Try to read from airavata.properties
        try {
            String airavataHome = System.getProperty("airavata.home");
            if (airavataHome == null || airavataHome.isEmpty()) {
                airavataHome = System.getenv("AIRAVATA_HOME");
            }
            
            if (airavataHome != null && !airavataHome.isEmpty()) {
                File confDir = new File(airavataHome, "conf");
                File propsFile = new File(confDir, "airavata.properties");
                if (propsFile.exists() && propsFile.isFile()) {
                    Properties props = new Properties();
                    try (InputStream is = new FileInputStream(propsFile)) {
                        props.load(is);
                        String enabled = props.getProperty("services.research.enabled", "true");
                        return "true".equalsIgnoreCase(enabled);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not read services.research.enabled from airavata.properties, defaulting to true", e);
        }
        
        // Default to enabled if we can't determine
        return true;
    }
}
