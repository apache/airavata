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
package org.apache.airavata.registry.entities.expcatalog;

import java.io.Serializable;
import java.util.Objects;

/**
 * The primary key class for the job database table.
 */
public class JobPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String jobId;
    private String taskId;

    public JobPK() {}

    public JobPK(String jobId, String taskId) {
        this.jobId = jobId;
        this.taskId = taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobPK that = (JobPK) o;
        return Objects.equals(jobId, that.jobId)
                && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, taskId);
    }

    @Override
    public String toString() {
        return "JobPK{"
                + "jobId='" + jobId + '\''
                + ", taskId='" + taskId + '\''
                + '}';
    }
}
