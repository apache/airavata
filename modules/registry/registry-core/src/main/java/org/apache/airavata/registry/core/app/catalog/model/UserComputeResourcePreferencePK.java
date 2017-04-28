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
package org.apache.airavata.registry.core.app.catalog.model;

import java.io.Serializable;

public class UserComputeResourcePreferencePK implements Serializable {
    private String userId;
    private String gatewayID;
    private String resourceId;

    public UserComputeResourcePreferencePK(String userId, String gatewayID, String resourceId) {
        this.userId = userId;
        this.gatewayID = gatewayID;
        this.resourceId = resourceId;
    }

    public UserComputeResourcePreferencePK() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserComputeResourcePreferencePK)) return false;

        UserComputeResourcePreferencePK that = (UserComputeResourcePreferencePK) o;

        if (!userId.equals(that.userId)) return false;
        if (!gatewayID.equals(that.gatewayID)) return false;
        return resourceId.equals(that.resourceId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + gatewayID.hashCode();
        result = 31 * result + resourceId.hashCode();
        return result;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getGatewayID() { return gatewayID; }

    public void setGatewayID(String gatewayID) { this.gatewayID = gatewayID; }
}
