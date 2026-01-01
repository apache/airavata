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
package org.apache.airavata.server.applications;

import org.apache.airavata.restproxy.RestProxyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * REST Proxy Application entry point.
 *
 * <p>Note: This class is kept in the distribution module for reference.
 * The unified server uses {@link org.apache.airavata.server.UnifiedApplication}
 * which includes all services.
 */
@SpringBootApplication(
        exclude = {org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class})
@EnableConfigurationProperties(RestProxyConfiguration.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.registry.services",
            "org.apache.airavata.registry.repositories",
            "org.apache.airavata.registry.mappers",
            "org.apache.airavata.registry.utils",
            "org.apache.airavata.service",
            "org.apache.airavata.restproxy"
        })
@EntityScan(basePackages = {"org.apache.airavata.registry.entities"})
@EnableJpaRepositories(basePackages = {"org.apache.airavata.registry.repositories"})
public class RestProxyApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RestProxyApplication.class);
        app.setDefaultProperties(java.util.Map.of("spring.main.allow-bean-definition-overriding", "true"));
        app.run(args);
    }
}
