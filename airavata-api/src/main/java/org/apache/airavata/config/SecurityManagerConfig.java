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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration for SecurityManager.
 * Provides the configured AiravataSecurityManager bean based on application settings.
 * Uses ObjectProvider for optional bean resolution with fallback to reflection-based instantiation
 * for plugin-style implementations that may not be Spring beans.
 */
@Configuration
@ConditionalOnProperty(name = "security.manager.enabled", havingValue = "true", matchIfMissing = false)
public class SecurityManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityManagerConfig.class);

    private final ObjectProvider<AiravataSecurityManager> securityManagerProvider;
    private final AiravataServerProperties properties;

    public SecurityManagerConfig(
            ObjectProvider<AiravataSecurityManager> securityManagerProvider, AiravataServerProperties properties) {
        this.securityManagerProvider = securityManagerProvider;
        this.properties = properties;
    }

    @Bean
    @Primary
    public AiravataSecurityManager airavataSecurityManager() throws AiravataSecurityException {
        try {
            String securityManagerClassName = properties.security.iam.classpath;
            logger.info("Creating SecurityManager instance: {}", securityManagerClassName);

            // Try to get from Spring context first (if it's a Spring bean and not currently being created)
            // Use stream() to avoid circular dependency during bean creation
            AiravataSecurityManager bean = null;
            try {
                bean = securityManagerProvider.getIfUnique();
            } catch (Exception e) {
                // Ignore if bean is currently being created (circular dependency)
                logger.debug("SecurityManager bean not available yet, will create via reflection: {}", e.getMessage());
            }

            if (bean != null) {
                Class<?> secManagerClass = Class.forName(securityManagerClassName);
                if (secManagerClass.isInstance(bean)) {
                    return bean;
                }
            }

            // Fallback to reflection-based instantiation for plugin-style implementations
            // Only if the class has a no-arg constructor (for plugin-style implementations)
            logger.debug("SecurityManager not found in Spring context, attempting to create via reflection");
            Class<?> secManagerImpl = Class.forName(securityManagerClassName);
            try {
                // Try no-arg constructor first (for plugin-style implementations)
                java.lang.reflect.Constructor<?> noArgConstructor = secManagerImpl.getDeclaredConstructor();
                return (AiravataSecurityManager) noArgConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                // If no no-arg constructor, the class must be a Spring bean with dependencies
                // In this case, we should wait for Spring to create it, or it's not available
                String error = String.format(
                        "Security Manager class %s requires constructor arguments and is not available as a Spring bean. "
                                + "Ensure the required dependencies (e.g., RegistryService) are enabled.",
                        securityManagerClassName);
                logger.error(error);
                throw new AiravataSecurityException(error, e);
            }
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
