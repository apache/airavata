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
package org.apache.airavata.common.model;

/**
 * Enum defining entity types that can be shared via the Sharing Registry.
 *
 * <p>The sharing registry provides visibility control: determining which users/groups
 * can see and access specific entities. Each entity type listed here can be registered
 * as a shareable entity with ownership and permission grants.
 *
 * <h3>Visibility vs Access Model:</h3>
 * <ul>
 *   <li><b>Visibility</b> (Sharing Registry): Controls who can see an entity exists</li>
 *   <li><b>Access</b> (RESOURCE_ACCESS table): Controls who can use a resource with which credential</li>
 * </ul>
 *
 * <p>For compute/storage resources, both visibility AND access must be granted:
 * <ol>
 *   <li>User must have visibility (sharing registry permission) to see the resource</li>
 *   <li>User must have access grant (RESOURCE_ACCESS) with a credential to use the resource</li>
 * </ol>
 */
public enum SharingResourceType {
    /** Projects containing experiments */
    PROJECT(0),

    /** Individual experiments */
    EXPERIMENT(1),

    /** Data products and replicas */
    DATA(2),

    /** Application deployments (specific deployments on compute resources) */
    APPLICATION_DEPLOYMENT(3),

    /** Group resource profiles (shared credential/preference configurations) */
    GROUP_RESOURCE_PROFILE(4),

    /** Credential tokens (SSH keys, certificates, etc.) */
    CREDENTIAL_TOKEN(5),

    /** Generic/fallback type for other entities */
    OTHER(6),

    /** Application interfaces (application definitions with inputs/outputs) */
    APPLICATION_INTERFACE(7),

    /** Compute resources (HPC clusters, cloud instances, etc.) */
    COMPUTE_RESOURCE(8),

    /** Storage resources (file systems, object stores, etc.) */
    STORAGE_RESOURCE(9),

    /** Allocation pools (merged groups: same project across runtimes, pool of credentials/runtimes) */
    ALLOCATION_POOL(10);

    private final int value;

    SharingResourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SharingResourceType findByValue(int value) {
        switch (value) {
            case 0:
                return PROJECT;
            case 1:
                return EXPERIMENT;
            case 2:
                return DATA;
            case 3:
                return APPLICATION_DEPLOYMENT;
            case 4:
                return GROUP_RESOURCE_PROFILE;
            case 5:
                return CREDENTIAL_TOKEN;
            case 6:
                return OTHER;
            case 7:
                return APPLICATION_INTERFACE;
            case 8:
                return COMPUTE_RESOURCE;
            case 9:
                return STORAGE_RESOURCE;
            case 10:
                return ALLOCATION_POOL;
            default:
                return null;
        }
    }
}
