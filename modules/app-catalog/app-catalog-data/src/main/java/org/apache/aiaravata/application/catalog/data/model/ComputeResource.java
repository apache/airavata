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

package org.apache.aiaravata.application.catalog.data.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "COMPUTE_RESOURCE")
public class ComputeResource implements Serializable {
    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceID;
    @Column(name = "HOST_NAME")
    private String hostName;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "PREFERRED_JOB_SUBMISSION_PROTOCOL")
    private String preferredJobSubProtocol;

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreferredJobSubProtocol() {
        return preferredJobSubProtocol;
    }

    public void setPreferredJobSubProtocol(String preferredJobSubProtocol) {
        this.preferredJobSubProtocol = preferredJobSubProtocol;
    }
}
