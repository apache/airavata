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
 * The primary key class for the job_submission_interface database table.
 */
public class JobSubmissionInterfacePK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String computeResourceId;
    private String jobSubmissionInterfaceId;

    public JobSubmissionInterfacePK() {}

    public JobSubmissionInterfacePK(String computeResourceId, String jobSubmissionInterfaceId) {
        this.computeResourceId = computeResourceId;
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSubmissionInterfacePK that = (JobSubmissionInterfacePK) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(jobSubmissionInterfaceId, that.jobSubmissionInterfaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(computeResourceId, jobSubmissionInterfaceId);
    }

    @Override
    public String toString() {
        return "JobSubmissionInterfacePK{"
                + "computeResourceId='" + computeResourceId + '\''
                + ", jobSubmissionInterfaceId='" + jobSubmissionInterfaceId + '\''
                + '}';
    }
}
