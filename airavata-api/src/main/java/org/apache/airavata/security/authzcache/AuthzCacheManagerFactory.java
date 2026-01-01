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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * This initializes the AuthzCacheManager implementation to be used as defined by the configuration.
 * Uses Spring ObjectProvider for optional bean resolution with fallback to reflection-based instantiation
 * for plugin-style implementations that may not be Spring beans.
 */
@Component
public class AuthzCacheManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(AuthzCacheManagerFactory.class);
    private final ObjectProvider<AuthzCacheManager> authzCacheManagerProvider;
    private final AiravataServerProperties properties;

    public AuthzCacheManagerFactory(
            ObjectProvider<AuthzCacheManager> authzCacheManagerProvider, AiravataServerProperties properties) {
        this.authzCacheManagerProvider = authzCacheManagerProvider;
        this.properties = properties;
    }

    public AuthzCacheManager getAuthzCacheManager() throws AiravataSecurityException {
        try {
            String className = properties.security.authzCache.classpath;
            Class<?> authzCacheManagerImpl = Class.forName(className);

            // Try to get from Spring context first (if it's a Spring bean)
            AuthzCacheManager bean = authzCacheManagerProvider.getIfAvailable();
            if (bean != null && authzCacheManagerImpl.isInstance(bean)) {
                return bean;
            }

            // Fallback to reflection-based instantiation for plugin-style implementations
            logger.debug("AuthzCacheManager not found in Spring context, creating new instance via reflection");
            return (AuthzCacheManager)
                    authzCacheManagerImpl.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            String error = "Authorization Cache Manager class could not be found.";
            logger.error(error, e);
            throw new AiravataSecurityException(error, e);
        } catch (Exception e) {
            String error = "Error in instantiating the Authorization Cache Manager class.";
            logger.error(error, e);
            throw new AiravataSecurityException(error, e);
        }
    }
}
