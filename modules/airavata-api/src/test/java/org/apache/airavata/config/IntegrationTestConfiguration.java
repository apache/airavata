/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.airavata.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * Shared Spring configuration for integration tests.
 * 
 * <p>This configuration:
 * <ul>
 *   <li>Provides a minimal {@link AiravataServerProperties} for testing</li>
 *   <li>Scans only repository-related packages (not security, monitoring, etc.)</li>
 *   <li>Imports JPA and Testcontainers configurations</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * {@code
 * @SpringBootTest(classes = IntegrationTestConfiguration.class)
 * class MyTest { ... }
 * }
 * </pre>
 * 
 * @see JpaConfig
 * @see TestcontainersConfig
 */
@Configuration
// application.properties is auto-loaded by Spring Boot
@Import({JpaConfig.class, TestcontainersConfig.class})
@ComponentScan(
    basePackages = {
        "org.apache.airavata.registry.services",
        "org.apache.airavata.registry.mappers",
        "org.apache.airavata.registry.repositories"
    },
    // Exclude components that require external dependencies
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, 
            pattern = ".*Credential.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, 
            pattern = ".*Security.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, 
            pattern = ".*Monitor.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, 
            pattern = ".*Kafka.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, 
            pattern = ".*Rabbit.*")
    }
)
public class IntegrationTestConfiguration {
    
    /**
     * Provides a minimal AiravataServerProperties for testing.
     * Uses null-safe defaults for optional fields.
     * Only created if no other AiravataServerProperties bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(AiravataServerProperties.class)
    public AiravataServerProperties airavataServerProperties() {
        // Create minimal nested records for required fields
        var database = new AiravataServerProperties.Database(
            null, null, null, null, null, null, null, null, null);
        
        var security = new AiravataServerProperties.Security(
            null, null, null, null, null);
        
        var services = new AiravataServerProperties.Services(
            new AiravataServerProperties.Services.Thrift(true, 
                new AiravataServerProperties.Services.Thrift.Server(8930)),
            new AiravataServerProperties.Services.Rest(false,
                new AiravataServerProperties.Services.Rest.Server(8082)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        return new AiravataServerProperties(
            "",                                              // home
            "default",                                       // defaultGateway
            true,                                            // validationEnabled
            new AiravataServerProperties.Sharing(true),      // sharing
            1000,                                            // inMemoryCacheSize
            "/tmp/airavata",                                 // localDataLocation
            1073741824,                                      // maxArchiveSize
            new AiravataServerProperties.StreamingTransfer(false),  // streamingTransfer
            null,                                            // hibernate
            database,                                        // database
            security,                                        // security
            null,                                            // rabbitmq
            null,                                            // kafka
            null,                                            // zookeeper
            null,                                            // helix
            null,                                            // flyway
            services                                         // services
        );
    }
}
