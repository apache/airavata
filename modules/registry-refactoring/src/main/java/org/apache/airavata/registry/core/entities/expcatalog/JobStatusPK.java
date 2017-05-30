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
package org.apache.airavata.registry.core.entities.expcatalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class JobStatusPK implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(JobStatusPK.class);
    private String state;
    private String jobId;

    @Id
    @Column(name = "STATUS_ID")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Id
    @Column(name = "JOB_ID")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobStatusPK that = (JobStatusPK) o;

        if (getState() != null ? !getState().equals(that.getState()) : that.getState() != null) return false;
        if (getJobId() != null ? !getJobId().equals(that.getJobId()) : that.getJobId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getState() != null ? getState().hashCode() : 0;
        result = 31 * result + (getJobId() != null ? getJobId().hashCode() : 0);
        return result;
    }
}