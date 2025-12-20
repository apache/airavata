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

import com.github.dozermapper.core.config.BeanContainer;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.registry.entities.appcatalog.AWSGroupComputeResourcePrefEntity;
import org.apache.airavata.registry.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.entities.appcatalog.SlurmGroupComputeResourcePrefEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, CustomBeanFactoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "security.manager.enabled=false",
            "services.registryService.enabled=false",
            "services.background.enabled=false",
            "services.thrift.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class CustomBeanFactoryTest {

    public CustomBeanFactoryTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @Test
    public void testGroupComputeResourcePreferenceToSlurmEntity() {
        // Create a GroupComputeResourcePreference with SLURM resource type
        GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setResourceType(ComputeResourceType.SLURM);
        pref.setComputeResourceId("test-resource-id");
        pref.setGroupResourceProfileId("test-profile-id");

        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        BeanContainer beanContainer = new BeanContainer();

        GroupComputeResourcePrefEntity entity = (GroupComputeResourcePrefEntity) customBeanFactory.createBean(
                pref,
                GroupComputeResourcePreference.class,
                GroupComputeResourcePrefEntity.class.getName(),
                beanContainer);

        // Should create SlurmGroupComputeResourcePrefEntity, not base class
        Assertions.assertNotNull(entity);
        Assertions.assertTrue(entity instanceof SlurmGroupComputeResourcePrefEntity);
        Assertions.assertFalse(entity instanceof AWSGroupComputeResourcePrefEntity);
    }

    @Test
    public void testGroupComputeResourcePreferenceToAWSEntity() {
        // Create a GroupComputeResourcePreference with AWS resource type
        GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setResourceType(ComputeResourceType.AWS);
        pref.setComputeResourceId("test-resource-id");
        pref.setGroupResourceProfileId("test-profile-id");

        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        BeanContainer beanContainer = new BeanContainer();

        GroupComputeResourcePrefEntity entity = (GroupComputeResourcePrefEntity) customBeanFactory.createBean(
                pref,
                GroupComputeResourcePreference.class,
                GroupComputeResourcePrefEntity.class.getName(),
                beanContainer);

        // Should create AWSGroupComputeResourcePrefEntity, not base class
        Assertions.assertNotNull(entity);
        Assertions.assertTrue(entity instanceof AWSGroupComputeResourcePrefEntity);
        Assertions.assertFalse(entity instanceof SlurmGroupComputeResourcePrefEntity);
    }

    @Test
    public void testDomainModelCreation() {
        // Test that domain models (non-Thrift) are created correctly
        CustomBeanFactory customBeanFactory = new CustomBeanFactory();
        BeanContainer beanContainer = new BeanContainer();

        GroupComputeResourcePreference pref = (GroupComputeResourcePreference)
                customBeanFactory.createBean(null, null, GroupComputeResourcePreference.class.getName(), beanContainer);

        Assertions.assertNotNull(pref);
        Assertions.assertInstanceOf(GroupComputeResourcePreference.class, pref);
    }

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.DozerMapperConfig.class
                        }),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.registry\\.messaging\\..*")
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
