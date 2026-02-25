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
package org.apache.airavata.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Base test configuration that provides standard component scanning for tests.
 *
 * <p>This configuration scans all standard packages without any excludeFilters.
 * Components marked with @Profile("!test") will automatically be excluded when
 * the "test" profile is active.
 *
 * <p>Tests can import this configuration or extend it as needed.
 */
@Configuration
@EnableConfigurationProperties(ServerProperties.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.registry",
            "org.apache.airavata.iam",
            "org.apache.airavata.util",
            "org.apache.airavata.exception",
            "org.apache.airavata.status.model",
            "org.apache.airavata.status.entity",
            "org.apache.airavata.experiment",
            "org.apache.airavata.compute",
            "org.apache.airavata.accounting",
            "org.apache.airavata.workflow",
            "org.apache.airavata.execution",
            "org.apache.airavata.research",
            "org.apache.airavata.sharing",
            "org.apache.airavata.gateway",
            "org.apache.airavata.messaging",
            "org.apache.airavata.config",
            "org.apache.airavata.accountprovisioning",
            "org.apache.airavata.job",
            "org.apache.airavata.process",
            "org.apache.airavata.user"
        })
public class BaseTestConfiguration {}
