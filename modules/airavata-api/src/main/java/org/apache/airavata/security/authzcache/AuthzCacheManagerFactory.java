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

import org.apache.airavata.security.AiravataSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for AuthzCacheManager.
 * The AuthzCacheManager bean is provided by the implementation class (e.g., DefaultAuthzCacheManager)
 * which is annotated with @Component. Spring will automatically inject it where needed.
 * This factory is kept for backward compatibility but simply returns the injected bean.
 */
@Component
public class AuthzCacheManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(AuthzCacheManagerFactory.class);
    private final AuthzCacheManager authzCacheManager;

    public AuthzCacheManagerFactory(AuthzCacheManager authzCacheManager) {
        this.authzCacheManager = authzCacheManager;
        logger.debug(
                "AuthzCacheManagerFactory initialized with: {}",
                authzCacheManager.getClass().getName());
    }

    public AuthzCacheManager getAuthzCacheManager() throws AiravataSecurityException {
        return authzCacheManager;
    }
}
