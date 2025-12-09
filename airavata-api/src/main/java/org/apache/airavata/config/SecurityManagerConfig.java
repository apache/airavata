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

import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.AiravataSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration for SecurityManager.
 * Provides the configured AiravataSecurityManager bean based on application settings.
 */
@Configuration
public class SecurityManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityManagerConfig.class);

    private final ApplicationContext applicationContext;
    private final AiravataServerProperties properties;

    public SecurityManagerConfig(ApplicationContext applicationContext, AiravataServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @Bean
    @Primary
    public AiravataSecurityManager airavataSecurityManager() throws AiravataSecurityException {
        try {
            String securityManagerClassName = properties.security.iam.classpath;
            logger.info("Creating SecurityManager instance: {}", securityManagerClassName);

            // Try to get from Spring context first (if it's a Spring bean)
            try {
                Class<?> secManagerClass = Class.forName(securityManagerClassName);
                if (applicationContext.getBeanNamesForType(secManagerClass).length > 0) {
                    return (AiravataSecurityManager) applicationContext.getBean(secManagerClass);
                }
            } catch (Exception e) {
                logger.debug("SecurityManager not found in Spring context, creating new instance", e);
            }

            // Fallback to reflection-based instantiation
            Class<?> secManagerImpl = Class.forName(securityManagerClassName);
            return (AiravataSecurityManager)
                    secManagerImpl.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            String error = "Security Manager class could not be found.";
            logger.error(error, e);
            throw new AiravataSecurityException(error, e);
        } catch (Exception e) {
            String error = "Error in instantiating the Security Manager class.";
            logger.error(error, e);
            throw new AiravataSecurityException(error, e);
        }
    }
}
