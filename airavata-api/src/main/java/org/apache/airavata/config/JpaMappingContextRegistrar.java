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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class JpaMappingContextRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(JpaMappingContextRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        logger.info("Registering custom jpaMappingContext bean definition via JpaMappingContextRegistrar");
        System.out.println("DEBUG: JpaMappingContextRegistrar running");

        // Overwrite 'jpaMappingContext' with our bean
        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(OpenJpaMetamodelMappingContextFactoryBean.class);
        builder.setPrimary(true);
        // Autowiring will handle the EMF injection since we added @Autowired to the setter
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        registry.registerBeanDefinition("jpaMappingContext", builder.getBeanDefinition());
    }
}
