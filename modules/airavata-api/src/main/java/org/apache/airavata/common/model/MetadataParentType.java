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
 * Enum representing the type of parent entity for a unified metadata record.
 * This allows a single METADATA table to store key-value metadata for various
 * entity types including data products, data replicas, experiments, and processes.
 *
 * <p>Extensible for future entity types that require metadata storage.
 */
public enum MetadataParentType {
    /**
     * Metadata associated with a data product.
     */
    DATA_PRODUCT(0),

    /**
     * Metadata associated with a data replica location.
     */
    DATA_REPLICA(1),

    /**
     * Metadata associated with an experiment.
     */
    EXPERIMENT(2),

    /**
     * Metadata associated with a process.
     */
    PROCESS(3),

    /**
     * Metadata associated with a task.
     */
    TASK(4),

    /**
     * Metadata associated with a job.
     */
    JOB(5),

    /**
     * Metadata associated with an application deployment.
     */
    APPLICATION_DEPLOYMENT(6),

    /**
     * Metadata associated with a compute resource.
     */
    COMPUTE_RESOURCE(7),

    /**
     * Metadata associated with a storage resource.
     */
    STORAGE_RESOURCE(8);

    private final int value;

    MetadataParentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MetadataParentType findByValue(int value) {
        switch (value) {
            case 0:
                return DATA_PRODUCT;
            case 1:
                return DATA_REPLICA;
            case 2:
                return EXPERIMENT;
            case 3:
                return PROCESS;
            case 4:
                return TASK;
            case 5:
                return JOB;
            case 6:
                return APPLICATION_DEPLOYMENT;
            case 7:
                return COMPUTE_RESOURCE;
            case 8:
                return STORAGE_RESOURCE;
            default:
                return null;
        }
    }
}
