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

import java.util.List;
import java.util.Objects;

/**
 * Domain model: GlobusJobSubmission
 */
public class GlobusJobSubmission {
    private String jobSubmissionInterfaceId;
    private SecurityProtocol securityProtocol;
    private List<String> globusGateKeeperEndPoint;

    public GlobusJobSubmission() {}

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public List<String> getGlobusGateKeeperEndPoint() {
        return globusGateKeeperEndPoint;
    }

    public void setGlobusGateKeeperEndPoint(List<String> globusGateKeeperEndPoint) {
        this.globusGateKeeperEndPoint = globusGateKeeperEndPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobusJobSubmission that = (GlobusJobSubmission) o;
        return Objects.equals(jobSubmissionInterfaceId, that.jobSubmissionInterfaceId)
                && Objects.equals(securityProtocol, that.securityProtocol)
                && Objects.equals(globusGateKeeperEndPoint, that.globusGateKeeperEndPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobSubmissionInterfaceId, securityProtocol, globusGateKeeperEndPoint);
    }

    @Override
    public String toString() {
        return "GlobusJobSubmission{" + "jobSubmissionInterfaceId=" + jobSubmissionInterfaceId + ", securityProtocol="
                + securityProtocol + ", globusGateKeeperEndPoint=" + globusGateKeeperEndPoint + "}";
    }
}
