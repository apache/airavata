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
package org.apache.airavata.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Registry component that collects enabled authenticator beans from Spring context.
 * Replaces reflection-based loading from AuthenticatorConfigurationReader.
 * Authenticators are sorted by @Order annotation (higher priority = lower order value).
 */
@Component
@ConditionalOnProperty(prefix = "airavata.security.authentication", name = "enabled", havingValue = "true")
public class AuthenticatorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorRegistry.class);

    private final ApplicationContext applicationContext;
    private List<Authenticator> authenticators;

    public AuthenticatorRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Get all enabled authenticator beans from Spring context, sorted by priority.
     * Authenticators are sorted by @Order annotation (lower order value = higher priority).
     * If no @Order annotation is present, DEFAULT_AUTHENTICATOR_PRIORITY is used.
     *
     * @return List of enabled authenticators, sorted by priority (highest first)
     */
    public List<Authenticator> getAuthenticators() {
        if (authenticators == null) {
            synchronized (this) {
                if (authenticators == null) {
                    authenticators = loadAuthenticators();
                }
            }
        }
        return Collections.unmodifiableList(authenticators);
    }

    private List<Authenticator> loadAuthenticators() {
        List<Authenticator> authenticatorList = new ArrayList<>();

        try {
            Map<String, Authenticator> authenticatorBeans = applicationContext.getBeansOfType(Authenticator.class);

            for (Map.Entry<String, Authenticator> entry : authenticatorBeans.entrySet()) {
                Authenticator authenticator = entry.getValue();

                // Only include enabled authenticators
                if (authenticator.isEnabled()) {
                    logger.debug(
                            "Adding authenticator bean: {} (class: {})",
                            entry.getKey(),
                            authenticator.getClass().getName());
                    authenticatorList.add(authenticator);
                } else {
                    logger.debug("Skipping disabled authenticator bean: {}", entry.getKey());
                }
            }

            // Sort by priority (using @Order annotation or getPriority() method)
            authenticatorList.sort(new AuthenticatorPriorityComparator());

            logger.info("Loaded {} enabled authenticator(s) from Spring context", authenticatorList.size());

        } catch (Exception e) {
            logger.error("Error loading authenticators from Spring context", e);
            throw new RuntimeException("Failed to load authenticators", e);
        }

        return authenticatorList;
    }

    /**
     * Check if authentication is enabled in the system.
     * This checks if any authenticators are available and enabled.
     *
     * @return true if authentication is enabled, false otherwise
     */
    public boolean isAuthenticationEnabled() {
        return !getAuthenticators().isEmpty();
    }

    /**
     * Comparator to sort authenticators by priority.
     * Uses @Order annotation if present, otherwise falls back to getPriority() method.
     * Lower order value = higher priority (Spring convention).
     */
    private static class AuthenticatorPriorityComparator implements Comparator<Authenticator> {
        @Override
        public int compare(Authenticator o1, Authenticator o2) {
            int priority1 = getEffectivePriority(o1);
            int priority2 = getEffectivePriority(o2);

            // Higher priority (lower order value) comes first
            return Integer.compare(priority1, priority2);
        }

        private int getEffectivePriority(Authenticator authenticator) {
            // Check for @Order annotation on the class
            Order orderAnnotation = authenticator.getClass().getAnnotation(Order.class);
            if (orderAnnotation != null) {
                return orderAnnotation.value();
            }

            // Fall back to getPriority() method
            return authenticator.getPriority();
        }
    }
}
