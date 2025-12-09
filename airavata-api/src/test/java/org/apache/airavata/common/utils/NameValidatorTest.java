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
package org.apache.airavata.common.utils;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, NameValidatorTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class NameValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(NameValidatorTest.class);

    public NameValidatorTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @Test
    public void testValidate() {
        logger.info("validate('abc90_90abc'): {}", NameValidator.validate("abc90_90abc")); // true
        logger.info("validate('abc_abc_123'): {}", NameValidator.validate("abc_abc_123")); // true
        logger.info("validate('abc_abc_'): {}", NameValidator.validate("abc_abc_")); // true
        logger.info("validate('abc_abc'): {}", NameValidator.validate("abc_abc")); // true
        logger.info("validate('abc.abc'): {}", NameValidator.validate("abc.abc")); // true
        logger.info("validate('9abc_abc'): {}", NameValidator.validate("9abc_abc")); // false, starts with digit
        logger.info("validate('_abc_abc'): {}", NameValidator.validate("_abc_abc")); // false, starts with underscore
        logger.info("validate('\\abc_abc'): {}", NameValidator.validate("\\abc_abc")); // false, starts with backslash
        logger.info("validate('abc\\_abc'): {}", NameValidator.validate("abc\\_abc")); // false, contains backslash
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.common",
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
