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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This initializes the AuthzCacheManager implementation to be used as defined by the configuration.
 * Uses Spring ApplicationContext to get the configured bean with proper dependency injection.
 */
@Component
public class AuthzCacheManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(AuthzCacheManagerFactory.class);
    private static ApplicationContext applicationContext;

    private static AiravataServerProperties properties;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        AuthzCacheManagerFactory.applicationContext = applicationContext;
    }

    @Autowired
    public void setProperties(AiravataServerProperties properties) {
        AuthzCacheManagerFactory.properties = properties;
    }

    public static AuthzCacheManager getAuthzCacheManager() throws AiravataSecurityException {
        if (applicationContext != null && properties != null) {
            try {
                String className = properties.security.authzCache.classpath;
                Class<?> authzCacheManagerImpl = Class.forName(className);
                return (AuthzCacheManager) applicationContext.getBean(authzCacheManagerImpl);
            } catch (ClassNotFoundException e) {
                String error = "Authorization Cache Manager class could not be found.";
                logger.error(e.getMessage(), e);
                throw new AiravataSecurityException(error, e);
            }
        }
        // Fallback to old reflection-based instantiation if ApplicationContext not available
        if (properties != null) {
            try {
                String className = properties.security.authzCache.classpath;
                Class<?> authzCacheManagerImpl = Class.forName(className);
                return (AuthzCacheManager)
                        authzCacheManagerImpl.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                String error = "Error in instantiating the Authorization Cache Manager class.";
                logger.error(e.getMessage(), e);
                throw new AiravataSecurityException(error, e);
            }
        }
        throw new AiravataSecurityException("Properties not initialized");
    }
}
