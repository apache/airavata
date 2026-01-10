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

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring Cache configuration using Caffeine.
 * Provides caching for authorization decisions and other frequently accessed data.
 * 
 * Configure via airavata.properties:
 *   airavata.in-memory-cache-size=1000
 *   security.authzCache.enabled=true
 * 
 * Cache names:
 *   - authzCache: Authorization decision cache
 *   - userProfileCache: User profile cache
 *   - gatewayCache: Gateway information cache
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String AUTHZ_CACHE = "authzCache";
    public static final String USER_PROFILE_CACHE = "userProfileCache";
    public static final String GATEWAY_CACHE = "gatewayCache";

    private final AiravataServerProperties properties;

    public CacheConfig(AiravataServerProperties properties) {
        this.properties = properties;
    }

    /**
     * Primary cache manager using Caffeine.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCacheBuilder());
        cacheManager.setCacheNames(java.util.List.of(AUTHZ_CACHE, USER_PROFILE_CACHE, GATEWAY_CACHE));
        return cacheManager;
    }

    /**
     * Default Caffeine cache configuration.
     */
    private Caffeine<Object, Object> defaultCacheBuilder() {
        int maxSize = properties.airavata.inMemoryCacheSize > 0 ? 
                properties.airavata.inMemoryCacheSize : 1000;
        
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats();
    }

    /**
     * Authorization cache with shorter TTL.
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<Object, Object> authzCaffeineCache() {
        int maxSize = properties.airavata.inMemoryCacheSize > 0 ? 
                properties.airavata.inMemoryCacheSize : 1000;
        
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .build();
    }
}
