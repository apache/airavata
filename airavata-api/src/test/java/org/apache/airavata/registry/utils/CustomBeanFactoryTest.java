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
package org.apache.airavata.registry.utils;

import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.thrift.TFieldRequirementType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, CustomBeanFactoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class CustomBeanFactoryTest {

    public CustomBeanFactoryTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @Test
    public void testRequiredFieldWithDefault() {
        Assertions.assertEquals(
                TFieldRequirementType.REQUIRED,
                UserConfigurationDataModel.metaDataMap.get(UserConfigurationDataModel._Fields.AIRAVATA_AUTO_SCHEDULE)
                        .requirementType);
        UserConfigurationDataModel fromConstructor = new UserConfigurationDataModel();
        Assertions.assertFalse(fromConstructor.isSetAiravataAutoSchedule());

        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        UserConfigurationDataModel fromFactory = (UserConfigurationDataModel)
                customBeanFactory.createBean(null, null, UserConfigurationDataModel.class.getName(), null);
        Assertions.assertTrue(fromFactory.isSetAiravataAutoSchedule());
    }

    @Test
    public void testOptionalFieldWithDefault() {
        Assertions.assertEquals(
                TFieldRequirementType.OPTIONAL,
                UserConfigurationDataModel.metaDataMap.get(UserConfigurationDataModel._Fields.SHARE_EXPERIMENT_PUBLICLY)
                        .requirementType);
        UserConfigurationDataModel fromConstructor = new UserConfigurationDataModel();
        Assertions.assertFalse(fromConstructor.isSetShareExperimentPublicly());

        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        UserConfigurationDataModel fromFactory = (UserConfigurationDataModel)
                customBeanFactory.createBean(null, null, UserConfigurationDataModel.class.getName(), null);
        Assertions.assertTrue(fromFactory.isSetShareExperimentPublicly());
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry",
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
