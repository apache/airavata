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
package org.apache.airavata.credential.impl.notifier;

import org.apache.airavata.credential.impl.notifier.EmailNotificationMessage;
import org.apache.airavata.credential.impl.notifier.EmailNotifier;
import org.apache.airavata.credential.impl.notifier.EmailNotifierConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/27/13
 * Time: 1:54 PM
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, EmailNotifierTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class EmailNotifierTest {

    public EmailNotifierTest() {
        // Spring Boot test - dependencies injected via constructor if needed
    }

    @BeforeEach
    public void setUp() throws Exception {}

    // Test is disabled. Need to fill in parameters to send mails
    @Test
    public void xtestNotifyMessage() throws Exception {

        EmailNotifierConfiguration emailNotifierConfiguration =
                new EmailNotifierConfiguration("smtp.googlemail.com", 465, "yyy", "xxx", true, "yyy@gmail.com");

        EmailNotifier notifier = new EmailNotifier(emailNotifierConfiguration);
        EmailNotificationMessage emailNotificationMessage =
                new EmailNotificationMessage("Test", "ggg@gmail.com", "Testing credential store");
        notifier.notifyMessage(emailNotificationMessage);
    }

    // Just to ignore test failures.
    @Test
    public void testIgnore() {}

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.credential",
                "org.apache.airavata.config"
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
