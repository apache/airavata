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
package org.apache.airavata.compute.resource.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.GroupComputeResourcePreference;
import org.apache.airavata.compute.resource.model.GroupResourceProfile;
import org.apache.airavata.compute.resource.entity.ResourceBindingEntity;
import org.apache.airavata.compute.resource.entity.ResourceEntity;
import org.apache.airavata.compute.resource.repository.ResourceBindingRepository;
import org.apache.airavata.compute.resource.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter service that maps resource binding entities to profile/preference views
 * used by the workflow engine and IAM layer.
 *
 * <p>Provides both clean entity-based methods ({@link #getBinding}, {@link #getUserBinding},
 * {@link #getStorageBinding}, {@link #getGatewayDefaultCredentialToken}) and legacy
 * stub-based methods ({@link #getGroupComputeResourcePreference}, {@link #getGroupResourceProfile})
 * that will be removed once all consumers are migrated.
 *
 * <p>Read methods return {@code null} or empty collections when bindings are not found.
 */
@Service
@Transactional(readOnly = true)
public class ResourceProfileAdapter {

    private static final Logger log = LoggerFactory.getLogger(ResourceProfileAdapter.class);

    private final ResourceRepository resourceRepository;
    private final ResourceBindingRepository resourceBindingRepository;

    public ResourceProfileAdapter(
            ResourceRepository resourceRepository, ResourceBindingRepository resourceBindingRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceBindingRepository = resourceBindingRepository;
    }

    // -------------------------------------------------------------------------
    // Clean binding methods
    // -------------------------------------------------------------------------

    /**
     * Look up a resource binding for the given compute resource within a gateway.
     *
     * <p>Falls back to a gateway-agnostic lookup if no binding matches the gateway filter.
     *
     * @param computeResourceId the compute resource identifier
     * @param gatewayId         the gateway identifier
     * @return the first matching binding, or {@code null} if none found
     */
    public ResourceBindingEntity getBinding(String computeResourceId, String gatewayId) {
        List<ResourceBindingEntity> bindings =
                resourceBindingRepository.findByGatewayIdAndResourceId(gatewayId, computeResourceId);
        if (bindings.isEmpty()) {
            bindings = resourceBindingRepository.findByResourceId(computeResourceId);
        }
        if (bindings.isEmpty()) {
            log.debug("getBinding: no binding found for resourceId={}, gatewayId={}", computeResourceId, gatewayId);
            return null;
        }
        return bindings.get(0);
    }

    /**
     * Look up a resource binding for the given user, gateway, and compute resource.
     *
     * @param userName          the user name (informational; not stored on bindings)
     * @param gatewayId         the gateway identifier
     * @param computeResourceId the compute resource identifier
     * @return the first matching binding, or {@code null} if none found
     */
    public ResourceBindingEntity getUserBinding(String userName, String gatewayId, String computeResourceId) {
        List<ResourceBindingEntity> bindings =
                resourceBindingRepository.findByGatewayIdAndResourceId(gatewayId, computeResourceId);
        if (bindings.isEmpty()) {
            log.debug(
                    "getUserBinding: no binding found for gatewayId={}, resourceId={}",
                    gatewayId,
                    computeResourceId);
            return null;
        }
        return bindings.get(0);
    }

    /**
     * Look up a storage binding for the given gateway and storage resource.
     *
     * @param gatewayId         the gateway identifier
     * @param storageResourceId the storage resource identifier
     * @return the first matching binding, or {@code null} if none found
     */
    public ResourceBindingEntity getStorageBinding(String gatewayId, String storageResourceId) {
        List<ResourceBindingEntity> bindings =
                resourceBindingRepository.findByGatewayIdAndResourceId(gatewayId, storageResourceId);
        if (bindings.isEmpty()) {
            log.debug(
                    "getStorageBinding: no binding found for gatewayId={}, storageResourceId={}",
                    gatewayId,
                    storageResourceId);
            return null;
        }
        return bindings.get(0);
    }

    /**
     * Resolve the file system root location for a storage binding.
     *
     * <p>Looks up the {@code "fileSystemRootLocation"} from the binding metadata first;
     * falls back to {@code basePath} from the resource's storage capability.
     *
     * @param binding           the storage binding
     * @param storageResourceId the storage resource identifier
     * @return the root location, or {@code null} if not configured
     */
    public String resolveStorageRootLocation(ResourceBindingEntity binding, String storageResourceId) {
        String rootLocation = getMetadataString(binding.getMetadata(), "fileSystemRootLocation");
        if (rootLocation == null) {
            Optional<ResourceEntity> resourceOpt = resourceRepository.findById(storageResourceId);
            if (resourceOpt.isPresent()) {
                ResourceEntity resource = resourceOpt.get();
                if (resource.getCapabilities() != null
                        && resource.getCapabilities().getStorage() != null) {
                    rootLocation = resource.getCapabilities().getStorage().getBasePath();
                }
            }
        }
        return rootLocation;
    }

    /**
     * Return the default credential store token for a gateway.
     *
     * <p>Returns the credential ID from the first binding found for the gateway,
     * or {@code null} if no bindings exist.
     *
     * @param gatewayId the gateway identifier
     * @return credential token, or {@code null}
     */
    public String getGatewayDefaultCredentialToken(String gatewayId) {
        List<ResourceBindingEntity> bindings = resourceBindingRepository.findByGatewayId(gatewayId);
        if (bindings.isEmpty()) {
            return null;
        }
        return bindings.get(0).getCredentialId();
    }

    // -------------------------------------------------------------------------
    // Group compute resource preference
    // -------------------------------------------------------------------------

    /**
     * Resolve a {@link GroupComputeResourcePreference} for the given compute resource within
     * the specified group resource profile.
     *
     * <p>The group resource profile ID is treated as a gateway ID. The first binding found for
     * the resource within that gateway is used. When no binding is found a minimal preference
     * object carrying only the {@code computeResourceId} is returned.
     *
     * @param computeResourceId       the compute resource identifier
     * @param groupResourceProfileId  the group resource profile (gateway) identifier
     * @return preference object; never {@code null}
     */
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        List<ResourceBindingEntity> bindings =
                resourceBindingRepository.findByGatewayIdAndResourceId(groupResourceProfileId, computeResourceId);
        if (bindings.isEmpty()) {
            bindings = resourceBindingRepository.findByResourceId(computeResourceId);
        }

        GroupComputeResourcePreference preference = new GroupComputeResourcePreference();
        preference.setComputeResourceId(computeResourceId);
        preference.setGroupResourceProfileId(groupResourceProfileId);

        if (bindings.isEmpty()) {
            log.debug(
                    "getGroupComputeResourcePreference: no binding found for resourceId={}, profileId={}",
                    computeResourceId,
                    groupResourceProfileId);
            preference.setResourceType(ComputeResourceType.PLAIN);
            return preference;
        }

        ResourceBindingEntity binding = bindings.get(0);
        Map<String, Object> metadata = binding.getMetadata();

        preference.setLoginUserName(binding.getLoginUsername());
        preference.setScratchLocation(getMetadataString(metadata, "scratchLocation"));
        preference.setPreferredBatchQueue(getMetadataString(metadata, "preferredBatchQueue"));
        preference.setAllocationProjectNumber(getMetadataString(metadata, "allocationProjectNumber"));
        preference.setResourceSpecificCredentialStoreToken(binding.getCredentialId());

        Optional<ResourceEntity> resourceOpt = resourceRepository.findById(computeResourceId);
        if (resourceOpt.isPresent()) {
            ResourceEntity resource = resourceOpt.get();
            if (resource.getCapabilities() != null && resource.getCapabilities().getCompute() != null) {
                preference.setResourceType(mapComputeResourceType(
                        resource.getCapabilities().getCompute().getJobManagerType()));
            } else {
                preference.setResourceType(ComputeResourceType.PLAIN);
            }
        } else {
            preference.setResourceType(ComputeResourceType.PLAIN);
        }

        return preference;
    }

    // -------------------------------------------------------------------------
    // Group resource profile
    // -------------------------------------------------------------------------

    /**
     * Build a {@link GroupResourceProfile} for the given profile ID.
     *
     * @param groupResourceProfileId the group resource profile (gateway) identifier
     * @return profile object; never {@code null}
     */
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) {
        List<ResourceBindingEntity> bindings = resourceBindingRepository.findByGatewayId(groupResourceProfileId);

        GroupResourceProfile profile = new GroupResourceProfile();
        profile.setGroupResourceProfileId(groupResourceProfileId);
        profile.setGatewayId(groupResourceProfileId);

        if (!bindings.isEmpty()) {
            profile.setDefaultCredentialStoreToken(bindings.get(0).getCredentialId());
        }

        List<GroupComputeResourcePreference> preferences = bindings.stream()
                .map(binding -> getGroupComputeResourcePreference(binding.getResourceId(), groupResourceProfileId))
                .toList();
        profile.setComputePreferences(preferences);

        return profile;
    }

    /**
     * Build a list of {@link GroupResourceProfile} objects for each accessible profile ID.
     *
     * @param gatewayId                    the gateway identifier
     * @param accessibleGroupResProfileIds list of profile IDs to include
     * @return list of group resource profiles; never {@code null}
     */
    public List<GroupResourceProfile> getGroupResourceList(
            String gatewayId, List<String> accessibleGroupResProfileIds) {
        if (accessibleGroupResProfileIds == null) {
            return new ArrayList<>();
        }
        return accessibleGroupResProfileIds.stream()
                .map(this::getGroupResourceProfile)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Existence checks
    // -------------------------------------------------------------------------

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) {
        return !resourceBindingRepository
                .findByGatewayId(groupResourceProfileId)
                .isEmpty();
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId) {
        return !resourceBindingRepository
                .findByGatewayIdAndResourceId(groupResourceProfileId, computeResourceId)
                .isEmpty();
    }

    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayId, String computeResourceId) {
        return !resourceBindingRepository
                .findByGatewayIdAndResourceId(gatewayId, computeResourceId)
                .isEmpty();
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) {
        return !resourceBindingRepository.findByGatewayId(gatewayId).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Safely retrieve a String value from a metadata map.
     *
     * @param metadata the metadata map (may be {@code null})
     * @param key      the map key to look up
     * @return the value as String, or {@code null} if the map is null or the key is absent
     */
    public static String getMetadataString(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return null;
        }
        Object value = metadata.get(key);
        return value instanceof String ? (String) value : (value != null ? String.valueOf(value) : null);
    }

    private ComputeResourceType mapComputeResourceType(String jobManagerType) {
        return ComputeResourceType.fromJobManagerType(jobManagerType);
    }
}
