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
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthzCache extends LinkedHashMap<AuthzCacheIndex, AuthzCacheEntry> {

    private static int MAX_SIZE;
    private final static Logger logger = LoggerFactory.getLogger(AuthzCache.class);

    private static AuthzCache authzCache = null;

    public static AuthzCache getInstance() throws ApplicationSettingsException {
        if (authzCache == null) {
            synchronized (AuthzCache.class) {
                if (authzCache == null) {
                    authzCache = new AuthzCache(ServerSettings.getCacheSize());
                }
            }
        }
        return authzCache;
    }

    private AuthzCache(int initialCapacity) {
        super(initialCapacity);
        MAX_SIZE = initialCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<AuthzCacheIndex, AuthzCacheEntry> eldest) {
        if (size() > MAX_SIZE) {
            logger.info("Authz cache max size exceeded. Removing the old entries.");
        }
        return size() > MAX_SIZE;
    }
}
