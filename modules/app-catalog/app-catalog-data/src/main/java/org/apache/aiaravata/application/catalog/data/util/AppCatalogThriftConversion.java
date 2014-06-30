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

package org.apache.aiaravata.application.catalog.data.util;

import org.apache.aiaravata.application.catalog.data.resources.ComputeHostResource;
import org.apache.aiaravata.application.catalog.data.resources.GSISSHSubmissionResource;
import org.apache.aiaravata.application.catalog.data.resources.GlobusJobSubmissionResource;
import org.apache.aiaravata.application.catalog.data.resources.SSHSubmissionResource;
import org.apache.airavata.model.computehost.ComputeResourceDescription;
import org.apache.airavata.model.computehost.GSISSHJobSubmission;
import org.apache.airavata.model.computehost.GlobusJobSubmission;
import org.apache.airavata.model.computehost.SSHJobSubmission;

public class AppCatalogThriftConversion {
    public static ComputeHostResource getComputeHostResource (ComputeResourceDescription description){
        ComputeHostResource resource = new ComputeHostResource();
        resource.setHostName(description.getHostName());
        resource.setDescription(description.getResourceDescription());
        resource.setPreferredJobSubmissionProtocol(description.getPreferredJobSubmissionProtocol());
        resource.setPreferredJobSubmissionProtocol(description.getResourceId());
        return resource;
    }

    public static GSISSHSubmissionResource getGSISSHSubmission (ComputeHostResource hostResource, GSISSHJobSubmission submission){
        GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
        resource.setComputeHostResource(hostResource);
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setMonitorMode(submission.getMonitorMode());
        resource.setInstalledPath(submission.getInstalledPath());
        resource.setResourceID(hostResource.getResoureId());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        resource.setSshPort(submission.getSshPort());
        return resource;
    }

    public static GlobusJobSubmissionResource getGlobusJobSubmission (ComputeHostResource hostResource, GlobusJobSubmission submission){
        GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
        resource.setComputeHostResource(hostResource);
        resource.setResourceID(hostResource.getResoureId());
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        return resource;
    }

    public static SSHSubmissionResource getSSHJobSubmission (ComputeHostResource hostResource, SSHJobSubmission submission){
        SSHSubmissionResource resource = new SSHSubmissionResource();
        resource.setComputeHostResource(hostResource);
        resource.setResourceID(hostResource.getResoureId());
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        return resource;
    }



}
