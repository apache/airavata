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
package org.apache.airavata.registry.entities.appcatalog;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for GridftpEndpointEntity.
 */
public class GridftpEndpointPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String endpoint;
    private String dataMovementInterfaceId;

    public GridftpEndpointPK() {}

    public GridftpEndpointPK(String endpoint, String dataMovementInterfaceId) {
        this.endpoint = endpoint;
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridftpEndpointPK that = (GridftpEndpointPK) o;
        return Objects.equals(endpoint, that.endpoint)
                && Objects.equals(dataMovementInterfaceId, that.dataMovementInterfaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, dataMovementInterfaceId);
    }

    @Override
    public String toString() {
        return "GridftpEndpointPK{"
                + "endpoint='" + endpoint + '\''
                + ", dataMovementInterfaceId='" + dataMovementInterfaceId + '\''
                + '}';
    }
}
