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
package org.apache.airavata.security.authzcache;

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.security.AiravataSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This initializes the AuthzCacheManager implementation to be used as defined by the configuration.
 * Uses Spring ApplicationContext to get the configured bean with proper dependency injection.
 */
@Component
public class AuthzCacheManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(AuthzCacheManagerFactory.class);
    private final ApplicationContext applicationContext;
    private final AiravataServerProperties properties;

    public AuthzCacheManagerFactory(ApplicationContext applicationContext, AiravataServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    public AuthzCacheManager getAuthzCacheManager() throws AiravataSecurityException {
        try {
            String className = properties.security.authzCache.classpath;
            Class<?> authzCacheManagerImpl = Class.forName(className);
            // Try to get from Spring context first (if it's a Spring bean)
            try {
                return (AuthzCacheManager) applicationContext.getBean(authzCacheManagerImpl);
            } catch (Exception e) {
                logger.debug("AuthzCacheManager not found in Spring context, creating new instance", e);
                // Fallback to reflection-based instantiation
                return (AuthzCacheManager)
                        authzCacheManagerImpl.getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException e) {
            String error = "Authorization Cache Manager class could not be found.";
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException(error, e);
        } catch (Exception e) {
            String error = "Error in instantiating the Authorization Cache Manager class.";
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException(error, e);
        }
    }
}
