/*
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
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;

/**
 * The primary key class for the compute_resource_preference database table.
 */
public class ComputeResourcePreferencePK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    private String gatewayId;
    private String computeResourceId;

    public ComputeResourcePreferencePK() {
    }

    public ComputeResourcePreferencePK(String gatewayId, String computeResourceId) {
        this.gatewayId = gatewayId;
        this.computeResourceId = computeResourceId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ComputeResourcePreferencePK)) {
            return false;
        }
        ComputeResourcePreferencePK castOther = (ComputeResourcePreferencePK) other;
        return
                this.gatewayId.equals(castOther.gatewayId)
                        && this.computeResourceId.equals(castOther.computeResourceId);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.gatewayId.hashCode();
        hash = hash * prime + this.computeResourceId.hashCode();

        return hash;
    }
}