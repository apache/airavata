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

package org.apache.aiaravata.application.catalog.data.resources;

public abstract class AbstractResource implements Resource{
    // table names
    public static final String COMPUTE_RESOURCE = "ComputeResource";
    public static final String HOST_ALIAS = "HostAlias";
    public static final String HOST_IPADDRESS = "HostIPAddress";
    public static final String GSISSH_SUBMISSION = "GSISSHSubmission";
    public static final String GSISSH_EXPORT = "GSISSHExport";
    public static final String GSISSH_PREJOBCOMMAND = "GSISSHPreJobCommand";
    public static final String GSISSH_POSTJOBCOMMAND = "GSISSHPostJobCommand";
    public static final String GLOBUS_SUBMISSION = "GlobusJobSubmission";
    public static final String SSH_SUBMISSION = "SSHSubmission";
    public static final String SCP_DATAMOVEMENT = "SCPDataMovement";
    public static final String GRID_FTP_DATAMOVEMENT = "GridFTPDataMovement";
    public static final String JOB_SUBMISSION_PROTOCOL = "JobSubmissionProtocol";
    public static final String DATA_MOVEMENT_PROTOCOL = "DataMovementProtocol";
    public static final String APPLICATION_MODULE = "ApplicationModule";
    public static final String APPLICATION_DEPLOYMENT = "ApplicationDeployment";
    public static final String LIBRARY_PREPAND_PATH = "LibraryPrepandPath";
    public static final String LIBRARY_APEND_PATH = "LibraryApendPath";
    public static final String APP_ENVIRONMENT = "AppEnvironment";
    public static final String APPLICATION_INTERFACE = "ApplicationInterface";
    public static final String APP_MODULE_MAPPING = "AppModuleMapping";
    public static final String APPLICATION_INPUT = "ApplicationInput";
    public static final String APPLICATION_OUTPUT = "ApplicationOutput";
    public static final String GATEWAY_PROFILE = "GatewayProfile";

    // Gateway Table
    public final class ComputeResourceConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String HOST_NAME = "hostName";
        public static final String DESCRIPTION = "description";
        public static final String SCRATCH_LOC = "scratchLocation";
        public static final String PREFERED_SUBMISSION_PROTOCOL = "preferredJobSubProtocol";
    }


}
