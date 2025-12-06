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

import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot application entry point for Airavata API.
 * This class initializes the Spring application context and enables
 * dependency injection for services, repositories, and entities.
 * All services (Thrift servers, workflow managers, helix controllers, etc.)
 * are started automatically via BackgroundServicesLauncher and ThriftAPI integration.
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "org.apache.airavata.service",
    "org.apache.airavata.registry.core.repositories",
    "org.apache.airavata.profile",
    "org.apache.airavata.sharing.db.repositories",
    "org.apache.airavata.config",
    "org.apache.airavata.api.thrift"
})
@EntityScan(basePackages = {
    "org.apache.airavata.registry.core.entities",
    "org.apache.airavata.profile.commons",
    "org.apache.airavata.sharing.db.entities"
})
public class AiravataApplication {

    private static final Logger logger = LoggerFactory.getLogger(AiravataApplication.class);

    public static void main(String[] args) {
        // Ensure properties are loaded before Spring context starts
        try {
            // Merge command line args into settings
            ServerSettings.mergeSettingsCommandLineArgs(args);
            // Trigger static initialization of ApplicationSettings
            ApplicationSettings.getSetting("servers", "all");
            logger.info("Airavata properties loaded successfully");
        } catch (Exception e) {
            logger.warn("Failed to pre-load properties, will rely on ApplicationSettings static initialization", e);
        }
        
        // Start Spring Boot application - this will initialize all beans and run CommandLineRunners
        SpringApplication app = new SpringApplication(AiravataApplication.class);
        // Don't exit immediately - keep running for background services
        app.setRegisterShutdownHook(true);
        app.run(args);
    }

}

