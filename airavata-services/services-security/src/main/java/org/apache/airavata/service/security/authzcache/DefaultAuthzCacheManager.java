/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.security.authzcache;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.security.AiravataSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAuthzCacheManager implements AuthzCacheManager {

    private final static Logger logger = LoggerFactory.getLogger(DefaultAuthzCacheManager.class);

    @Override
    public AuthzCachedStatus getAuthzCachedStatus(AuthzCacheIndex authzCacheIndex) throws AiravataSecurityException {
        if (isAuthzDecisionCached(authzCacheIndex)) {
            AuthzCacheEntry cacheEntry = getAuthzCacheEntry(authzCacheIndex);
            long expiryTime = cacheEntry.getExpiryTime();
            long currentTime = System.currentTimeMillis();
            long timePassed = (currentTime - cacheEntry.getEntryTimestamp()) / 1000;
            if (expiryTime > timePassed) {
                //access token is still valid. Hence, return the cached decision
                if (cacheEntry.getDecision()) {
                    return AuthzCachedStatus.AUTHORIZED;
                } else {
                    return AuthzCachedStatus.NOT_AUTHORIZED;
                }
            } else {
                //access token has been expired. Hence, remove the entry and return.
                removeAuthzCacheEntry(authzCacheIndex);
                return AuthzCachedStatus.NOT_CACHED;
            }
        } else {
            return AuthzCachedStatus.NOT_CACHED;
        }
    }

    @Override
    public void addToAuthzCache(AuthzCacheIndex authzCacheIndex, AuthzCacheEntry authzCacheEntry) throws AiravataSecurityException {
        try {
            AuthzCache.getInstance().put(authzCacheIndex, authzCacheEntry);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in obtaining the authorization cache instance.");
        }
    }

    @Override
    public boolean isAuthzDecisionCached(AuthzCacheIndex authzCacheIndex) throws AiravataSecurityException {
        try {
            return AuthzCache.getInstance().containsKey(authzCacheIndex);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in obtaining the authorization cache instance.");
        }
    }

    @Override
    public AuthzCacheEntry getAuthzCacheEntry(AuthzCacheIndex authzCacheIndex) throws AiravataSecurityException {
        try {
            return AuthzCache.getInstance().get(authzCacheIndex);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in obtaining the authorization cache instance.");
        }
    }

    @Override
    public void removeAuthzCacheEntry(AuthzCacheIndex authzCacheIndex) throws AiravataSecurityException {
        try {
            AuthzCache.getInstance().remove(authzCacheIndex);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in obtaining the authorization cache instance.");
        }
    }

    @Override
    public void clearCache() throws AiravataSecurityException {
        try {
            AuthzCache.getInstance().clear();
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in obtaining the authorization cache instance.");

        }
    }
}