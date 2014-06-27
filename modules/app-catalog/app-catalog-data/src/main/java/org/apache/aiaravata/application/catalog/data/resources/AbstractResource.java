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

    // Compute Resource Table
    public final class ComputeResourceConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String HOST_NAME = "hostName";
        public static final String DESCRIPTION = "description";
        public static final String SCRATCH_LOC = "scratchLocation";
        public static final String PREFERED_SUBMISSION_PROTOCOL = "preferredJobSubProtocol";
    }

    // Host Alias Table
    public final class HostAliasConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String ALIAS = "alias";
    }

    // Host IPAddress Table
    public final class HostIPAddressConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String IP_ADDRESS = "ipaddress";
    }

    // GSSISSH Submission Table
    public final class GSISSHSubmissionConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String SUBMISSION_ID = "submissionID";
        public static final String RESOURCE_JOB_MANAGER = "resourceJobManager";
        public static final String SSH_PORT = "sshPort";
        public static final String INSTALLED_PATH = "installedPath";
        public static final String MONITOR_MODE = "monitorMode";
    }

    // GSSISSH Export Table
    public final class GSISSHExportConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String EXPORT = "export";
    }

    // GSSISSH Pre Job Command Table
    public final class GSISSHPreJobCommandConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String COMMAND = "command";
    }

    // GSSISSH Post Job Command Table
    public final class GSISSHPostJobCommandConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String COMMAND = "command";
    }

    // GSSISSH Post Job Command Table
    public final class GlobusJobSubmissionConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String SUBMISSION_ID = "submissionID";
        public static final String resourceJobManager = "RESOURCE_JOB_MANAGER";
        public static final String securityProtocol = "SECURITY_PROTOCAL";
        public static final String globusEP = "GLOBUS_GATEKEEPER_EP";
    }

    // GSSISSH Post Job Command Table
    public final class SSHSubmissionConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String SUBMISSION_ID = "submissionID";
        public static final String RESOURCE_JOB_MANAGER = "resourceJobManager";
        public static final String SSH_PORT = "sshPort";
    }

    public final class SCPDataMovementConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String DATA_MOVE_ID = "dataMoveID";
        public static final String SECURITY_PROTOCOL = "securityProtocol";
        public static final String SSH_PORT = "sshPort";
    }

    public final class GridFTPDataMovementConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String DATA_MOVE_ID = "dataMoveID";
        public static final String SECURITY_PROTOCOL = "securityProtocol";
        public static final String GRID_FTP_EP = "gridFTPEP";
    }

    public final class JobSubmissionProtocolConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String SUBMISSION_ID = "submissionID";
        public static final String JOB_TYPE = "jobType";
    }

    public final class DataMoveProtocolConstants {
        public static final String RESOURCE_ID = "resourceID";
        public static final String DATA_MOVE_ID = "submissionID";
        public static final String JOB_TYPE = "jobType";
    }

    public final class ApplicationModuleConstants {
        public static final String MODULE_ID = "moduleID";
        public static final String MODULE_NAME = "moduleName";
        public static final String MODULE_VERSION = "moduleVersion";
        public static final String MODULE_DESC = "moduleDesc";
    }

    public final class ApplicationDeploymentConstants {
        public static final String APP_MODULE_ID = "appModuleID";
        public static final String DEPLOYMENT_ID = "deployementID";
        public static final String COMPUTE_HOST_ID = "hostID";
        public static final String EXECUTABLE_PATH = "executablePath";
        public static final String APPLICATION_DESC = "applicationDesc";
        public static final String ENV_MODULE_LOAD_CMD = "envModuleLoaString";
    }

    public final class LibraryPrepandPathConstants {
        public static final String DEPLOYMENT_ID = "deployementID";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }

    public final class LibraryApendPathConstants {
        public static final String DEPLOYMENT_ID = "deployementID";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }

    public final class AppEnvironmentConstants {
        public static final String DEPLOYMENT_ID = "deployementID";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }

    public final class ApplicationInterfaceConstants {
        public static final String INTERFACE_ID = "interfaceID";
        public static final String APPLICATION_NAME = "appName";
    }

    public final class AppModuleMappingConstants {
        public static final String INTERFACE_ID = "interfaceID";
        public static final String MODULE_ID = "moduleID";
    }

    public final class AppInputConstants {
        public static final String INTERFACE_ID = "interfaceID";
        public static final String INPUT_KEY = "inputKey";
        public static final String INPUT_VALUE = "inputVal";
        public static final String DATA_TYPE = "dataType";
        public static final String METADATA = "metadata";
        public static final String APP_PARAMETER = "appParameter";
        public static final String APP_UI_DESCRIPTION = "appUIDesc";
    }

    public final class AppOutputConstants {
        public static final String INTERFACE_ID = "interfaceID";
        public static final String OUTPUT_KEY = "outputKey";
        public static final String OUTPUT_VALUE = "outputVal";
        public static final String DATA_TYPE = "dataType";
        public static final String METADATA = "metadata";
    }

    public final class GatewayProfileConstants {
        public static final String GATEWAY_ID = "gatewayID";
        public static final String GATEWAY_NAME = "gatewayName";
        public static final String GATEWAY_DESC = "gatewayDesc";
        public static final String PREFERED_RESOURCE = "preferedResource";
    }




}
