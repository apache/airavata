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
@EnableConfigurationProperties(AiravataServerProperties.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.registry.services",
            "org.apache.airavata.registry.repositories",
            "org.apache.airavata.registry.mappers",
            "org.apache.airavata.registry.utils",
            "org.apache.airavata.service",
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
            "org.apache.airavata.messaging",
            "org.apache.airavata.config",
            "org.apache.airavata.common.utils",
            "org.apache.airavata.security",
            "org.apache.airavata.accountprovisioning"
        })
public class BaseTestConfiguration {}
