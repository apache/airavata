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
package org.apache.airavata.gfac.ssh.api;

import org.apache.airavata.gfac.ssh.api.job.JobDescriptor;

/**
 * This represents a CPU core of a machine in the cluster
 */
public class Core {
    private JobDescriptor job;
    private String id;

    public Core(String id) {
        this.id = id;
        this.job = null;
    }

    /**
     * @return core's id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return job running on the core
     */
    public JobDescriptor getJob() {
        return job;
    }

    public void setJob(JobDescriptor job) {
        this.job = job;
    }

}
