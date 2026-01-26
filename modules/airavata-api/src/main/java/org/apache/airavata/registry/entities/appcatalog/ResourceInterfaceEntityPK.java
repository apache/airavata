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
import org.apache.airavata.common.model.InterfaceType;

/**
 * Composite primary key class for the unified RESOURCE_INTERFACE table.
 * The key consists of resourceId, interfaceId, and interfaceType.
 */
public class ResourceInterfaceEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String interfaceId;
    private InterfaceType interfaceType;

    public ResourceInterfaceEntityPK() {}

    public ResourceInterfaceEntityPK(String resourceId, String interfaceId, InterfaceType interfaceType) {
        this.resourceId = resourceId;
        this.interfaceId = interfaceId;
        this.interfaceType = interfaceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceInterfaceEntityPK that = (ResourceInterfaceEntityPK) o;
        return Objects.equals(resourceId, that.resourceId)
                && Objects.equals(interfaceId, that.interfaceId)
                && interfaceType == that.interfaceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, interfaceId, interfaceType);
    }
}
