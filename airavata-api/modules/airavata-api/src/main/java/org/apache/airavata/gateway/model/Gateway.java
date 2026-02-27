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
package org.apache.airavata.gateway.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Domain model: Gateway
 */
public class Gateway {
    private String gatewayId;

    @NotBlank(message = "gatewayName is required")
    private String gatewayName;

    private String domain;
    private String emailAddress;

    public Gateway() {}

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gateway that = (Gateway) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(gatewayName, that.gatewayName)
                && Objects.equals(domain, that.domain)
                && Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, gatewayName, domain, emailAddress);
    }

    @Override
    public String toString() {
        return "Gateway{"
                + "gatewayId=" + gatewayId
                + ", gatewayName=" + gatewayName
                + ", domain=" + domain
                + ", emailAddress=" + emailAddress
                + "}";
    }
}
