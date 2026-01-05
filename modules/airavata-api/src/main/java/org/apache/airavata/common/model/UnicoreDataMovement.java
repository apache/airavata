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

import java.util.Objects;

/**
 * Domain model: UnicoreDataMovement
 */
public class UnicoreDataMovement {
    private String dataMovementInterfaceId;
    private SecurityProtocol securityProtocol;
    private String unicoreEndPointURL;

    public UnicoreDataMovement() {}

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getUnicoreEndPointURL() {
        return unicoreEndPointURL;
    }

    public void setUnicoreEndPointURL(String unicoreEndPointURL) {
        this.unicoreEndPointURL = unicoreEndPointURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnicoreDataMovement that = (UnicoreDataMovement) o;
        return Objects.equals(dataMovementInterfaceId, that.dataMovementInterfaceId)
                && Objects.equals(securityProtocol, that.securityProtocol)
                && Objects.equals(unicoreEndPointURL, that.unicoreEndPointURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataMovementInterfaceId, securityProtocol, unicoreEndPointURL);
    }

    @Override
    public String toString() {
        return "UnicoreDataMovement{" + "dataMovementInterfaceId=" + dataMovementInterfaceId + ", securityProtocol="
                + securityProtocol + ", unicoreEndPointURL=" + unicoreEndPointURL + "}";
    }
}
