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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.apache.airavata.config.AiravataServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Authorization cache using Caffeine.
 * Provides thread-safe, high-performance caching for authorization decisions.
 */
@Component
public class AuthzCache {

    private static final Logger logger = LoggerFactory.getLogger(AuthzCache.class);
    
    private final Cache<AuthzCacheIndex, AuthzCacheEntry> cache;

    public AuthzCache(AiravataServerProperties serverProperties) {
        int maxSize = serverProperties.airavata.inMemoryCacheSize > 0 ? 
                serverProperties.airavata.inMemoryCacheSize : 1000;
        
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .evictionListener((key, value, cause) -> {
                    if (cause.wasEvicted()) {
                        logger.debug("Authz cache entry evicted: {} due to {}", key, cause);
                    }
                })
                .build();
        
        logger.info("AuthzCache initialized with max size: {}", maxSize);
    }

    /**
     * Get an entry from the cache.
     */
    public AuthzCacheEntry get(AuthzCacheIndex key) {
        return cache.getIfPresent(key);
    }

    /**
     * Put an entry into the cache.
     */
    public void put(AuthzCacheIndex key, AuthzCacheEntry value) {
        cache.put(key, value);
    }

    /**
     * Check if key exists in cache.
     */
    public boolean containsKey(AuthzCacheIndex key) {
        return cache.getIfPresent(key) != null;
    }

    /**
     * Remove an entry from the cache.
     */
    public void remove(AuthzCacheIndex key) {
        cache.invalidate(key);
    }

    /**
     * Clear all entries from the cache.
     */
    public void clear() {
        cache.invalidateAll();
        logger.info("AuthzCache cleared");
    }

    /**
     * Get the current size of the cache.
     */
    public long size() {
        return cache.estimatedSize();
    }

    /**
     * Get cache statistics.
     */
    public String getStats() {
        return cache.stats().toString();
    }
}
