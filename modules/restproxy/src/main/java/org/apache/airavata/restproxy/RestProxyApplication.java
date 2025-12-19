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
package org.apache.airavata.restproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        exclude = {org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class})
@EnableConfigurationProperties(RestProxyConfiguration.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.registry",
            "org.apache.airavata.service",
            "org.apache.airavata.restproxy"
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\$.*" // Exclude inner classes
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*\\.cpi\\..*" // Exclude Thrift CPI classes
                    ),
            @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = "org\\.apache\\.airavata\\.model\\..*" // Exclude Thrift-generated model classes
                    )
        })
@EntityScan(
        basePackages = {
            "org.apache.airavata.registry.entities"
        })
@EnableJpaRepositories(
        basePackages = {
            "org.apache.airavata.registry.repositories"
        })
public class RestProxyApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RestProxyApplication.class);
        app.setDefaultProperties(java.util.Map.of(
                "spring.main.allow-bean-definition-overriding", "true"
                ));
        app.run(args);
    }
}
