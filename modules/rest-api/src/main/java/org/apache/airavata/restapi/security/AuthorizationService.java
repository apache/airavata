/**
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
package org.apache.airavata.restapi.security;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.security.IamAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Service for checking user authorization and gateway access.
 * Uses in-memory caching to improve performance of repeated access checks.
 */
@Service("restApiAuthorizationService")
public class AuthorizationService {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthorizationService.class);
    private static final String ROOT_USER_ID = "default-admin";
    
    // Cache TTL in milliseconds (5 minutes)
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;
    
    // Simple cache entry with timestamp
    private record CacheEntry<T>(T value, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
    
    // Cache for gateway access checks: key = "userId:gatewayId", value = CacheEntry<Boolean>
    private final Map<String, CacheEntry<Boolean>> gatewayAccessCache = new ConcurrentHashMap<>();
    
    // Cache for accessible gateways list: key = userId, value = CacheEntry<List<String>>
    private final Map<String, CacheEntry<List<String>>> accessibleGatewaysCache = new ConcurrentHashMap<>();
    
    // Scheduler for cache cleanup
    private ScheduledExecutorService cacheCleanupScheduler;
    
    private final UserProfileService userProfileService;
    private final IamAdminService iamAdminService;
    private final GatewayService gatewayService;

    public AuthorizationService(UserProfileService userProfileService, IamAdminService iamAdminService, GatewayService gatewayService) {
        this.userProfileService = userProfileService;
        this.iamAdminService = iamAdminService;
        this.gatewayService = gatewayService;
    }
    
    @PostConstruct
    public void init() {
        // Schedule periodic cache cleanup every 5 minutes
        cacheCleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AuthzCacheCleanup");
            t.setDaemon(true);
            return t;
        });
        cacheCleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
        logger.info("Authorization cache initialized with {} ms TTL", CACHE_TTL_MS);
    }
    
    @PreDestroy
    public void shutdown() {
        if (cacheCleanupScheduler != null) {
            cacheCleanupScheduler.shutdown();
        }
    }
    
    private void cleanupExpiredEntries() {
        int accessRemoved = 0;
        int gatewaysRemoved = 0;
        
        var accessIterator = gatewayAccessCache.entrySet().iterator();
        while (accessIterator.hasNext()) {
            if (accessIterator.next().getValue().isExpired()) {
                accessIterator.remove();
                accessRemoved++;
            }
        }
        
        var gatewaysIterator = accessibleGatewaysCache.entrySet().iterator();
        while (gatewaysIterator.hasNext()) {
            if (gatewaysIterator.next().getValue().isExpired()) {
                gatewaysIterator.remove();
                gatewaysRemoved++;
            }
        }
        
        if (accessRemoved > 0 || gatewaysRemoved > 0) {
            logger.debug("Cache cleanup: removed {} access entries, {} gateway entries", accessRemoved, gatewaysRemoved);
        }
    }

    /**
     * Check if user is the root/admin user.
     */
    public boolean isRootUser(AuthzToken authzToken) {
        String userId = authzToken.getClaimsMap().get("userId");
        String userName = authzToken.getClaimsMap().get("userName");
        // Check both userId (which could be UUID) and userName (which is the username)
        return ROOT_USER_ID.equals(userId) || ROOT_USER_ID.equals(userName);
    }

    /**
     * Check if user has access to a specific gateway.
     * Results are cached for 5 minutes to improve performance.
     */
    public boolean hasGatewayAccess(AuthzToken authzToken, String gatewayId) {
        // Root user has access to all gateways
        if (isRootUser(authzToken)) {
            return true;
        }

        String userId = authzToken.getClaimsMap().get("userId");
        if (userId == null) {
            return false;
        }

        // Check cache first
        String cacheKey = userId + ":" + gatewayId;
        CacheEntry<Boolean> cachedEntry = gatewayAccessCache.get(cacheKey);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            logger.debug("Gateway access cache hit for user {} gateway {}: {}", userId, gatewayId, cachedEntry.value());
            return cachedEntry.value();
        }

        try {
            // Check if user has a profile for this gateway
            var userProfile = userProfileService.getUserProfileById(authzToken, userId, gatewayId);
            boolean hasAccess = userProfile != null;
            
            // Cache the result
            gatewayAccessCache.put(cacheKey, new CacheEntry<>(hasAccess, System.currentTimeMillis()));
            logger.debug("Gateway access cache miss for user {} gateway {}: {}", userId, gatewayId, hasAccess);
            
            return hasAccess;
        } catch (Exception e) {
            // If profile doesn't exist, user doesn't have access
            // Cache the negative result too
            gatewayAccessCache.put(cacheKey, new CacheEntry<>(false, System.currentTimeMillis()));
            return false;
        }
    }

    /**
     * Get list of gateway IDs the user has access to.
     * Results are cached for 5 minutes to improve performance.
     */
    public List<String> getAccessibleGateways(AuthzToken authzToken) {
        String userId = authzToken.getClaimsMap().get("userId");
        
        if (userId == null) {
            return List.of();
        }

        // Root user can access all gateways - return all
        if (isRootUser(authzToken)) {
            try {
                // Get all gateways from registry
                var allGateways = gatewayService.getAllGateways();
                return allGateways.stream()
                    .map(g -> g.getGatewayId())
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // Fallback to default gateway
                return List.of("default");
            }
        }

        // Check cache first
        CacheEntry<List<String>> cachedEntry = accessibleGatewaysCache.get(userId);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            logger.debug("Accessible gateways cache hit for user {}: {} gateways", userId, cachedEntry.value().size());
            return cachedEntry.value();
        }

        // For regular users, get gateways from their profiles
        try {
            // Return gateway from token; can be extended to query user profiles
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            List<String> gateways = gatewayId != null ? List.of(gatewayId) : List.of();
            
            // Cache the result
            accessibleGatewaysCache.put(userId, new CacheEntry<>(gateways, System.currentTimeMillis()));
            logger.debug("Accessible gateways cache miss for user {}: {} gateways", userId, gateways.size());
            
            return gateways;
        } catch (Exception e) {
            return List.of();
        }
    }
    
    /**
     * Invalidate the cache for a specific user. Call this when user permissions change.
     */
    public void invalidateCacheForUser(String userId) {
        accessibleGatewaysCache.remove(userId);
        // Also invalidate all gateway access entries for this user
        gatewayAccessCache.keySet().removeIf(key -> key.startsWith(userId + ":"));
        logger.debug("Invalidated authorization cache for user {}", userId);
    }

    /**
     * Require that user is root user, throw exception if not.
     */
    public void requireRootUser(AuthzToken authzToken) {
        if (!isRootUser(authzToken)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "This operation requires root/admin privileges"
            );
        }
    }

    /**
     * Require that user has access to the specified gateway, throw exception if not.
     */
    public void requireGatewayAccess(AuthzToken authzToken, String gatewayId) {
        if (!hasGatewayAccess(authzToken, gatewayId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User does not have access to gateway: " + gatewayId
            );
        }
    }

    /**
     * Validate and scope gateway ID - ensures user has access and returns the scoped gateway ID.
     */
    public String validateAndScopeGateway(AuthzToken authzToken, String requestedGatewayId) {
        // Clean up the gateway ID - remove any whitespace or duplicates
        if (requestedGatewayId != null) {
            requestedGatewayId = requestedGatewayId.trim();
            // If it contains commas, take the first part (in case of accidental duplication)
            if (requestedGatewayId.contains(",")) {
                logger.warn("Gateway ID contains comma, using first part: {}", requestedGatewayId);
                requestedGatewayId = requestedGatewayId.split(",")[0].trim();
            }
        }
        
        if (requestedGatewayId == null || requestedGatewayId.isEmpty()) {
            // Use gateway from token if not specified
            requestedGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (requestedGatewayId != null) {
                requestedGatewayId = requestedGatewayId.trim();
                // Handle comma-separated values from token as well
                if (requestedGatewayId.contains(",")) {
                    logger.warn("Gateway ID from token contains comma, using first part: {}", requestedGatewayId);
                    requestedGatewayId = requestedGatewayId.split(",")[0].trim();
                }
            }
        }

        if (requestedGatewayId == null || requestedGatewayId.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Gateway ID is required"
            );
        }

        requireGatewayAccess(authzToken, requestedGatewayId);
        return requestedGatewayId;
    }
}
