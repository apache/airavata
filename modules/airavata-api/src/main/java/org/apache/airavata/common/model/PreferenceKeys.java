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

/**
 * Defines all preference keys for the unified RESOURCE_PREFERENCE key-value store.
 *
 * <p>This class centralizes all preference key definitions used across all resource types
 * at GATEWAY, GROUP, and USER levels. Keys are organized by resource type.
 *
 * <h3>Resource Types:</h3>
 * <ul>
 *   <li>COMPUTE - Compute resource preferences (HPC clusters, cloud instances)</li>
 *   <li>STORAGE - Storage resource preferences (file systems, object stores)</li>
 *   <li>BATCH_QUEUE - Queue policies and limits</li>
 *   <li>APPLICATION - Application defaults and settings</li>
 *   <li>GATEWAY - Gateway-level configuration</li>
 *   <li>SYSTEM - System-wide settings</li>
 * </ul>
 *
 * @see PreferenceResourceType
 * @see PreferenceLevel
 */
public final class PreferenceKeys {

    private PreferenceKeys() {
        // Utility class - prevent instantiation
    }

    // ============================================================================
    // COMPUTE RESOURCE PREFERENCE KEYS
    // Resource ID: computeResourceId
    // ============================================================================

    /** SSH login username for the compute resource */
    public static final String LOGIN_USERNAME = "loginUsername";

    /** Scratch location/directory on the compute resource */
    public static final String SCRATCH_LOCATION = "scratchLocation";

    /** Allocation/project number for job accounting */
    public static final String ALLOCATION_PROJECT_NUMBER = "allocationProjectNumber";

    /** Preferred batch queue name */
    public static final String PREFERRED_BATCH_QUEUE = "preferredBatchQueue";

    /** Preferred job submission protocol (SSH, LOCAL, etc.) */
    public static final String PREFERRED_JOB_SUBMISSION_PROTOCOL = "preferredJobSubmissionProtocol";

    /** Preferred data movement protocol (SCP, SFTP, etc.) */
    public static final String PREFERRED_DATA_MOVEMENT_PROTOCOL = "preferredDataMovementProtocol";

    /** Quality of service setting */
    public static final String QUALITY_OF_SERVICE = "qualityOfService";

    /** Resource reservation name */
    public static final String RESERVATION = "reservation";

    /** Reservation start time (as epoch millis) */
    public static final String RESERVATION_START_TIME = "reservationStartTime";

    /** Reservation end time (as epoch millis) */
    public static final String RESERVATION_END_TIME = "reservationEndTime";

    /** Token for resource-specific credential in the credential store */
    public static final String RESOURCE_CREDENTIAL_TOKEN = "resourceSpecificCredentialStoreToken";

    /** Whether Airavata can override user settings */
    public static final String OVERRIDE_BY_AIRAVATA = "overrideByAiravata";

    /** Whether the user preference has been validated */
    public static final String VALIDATED = "validated";

    /** Gateway ID for usage reporting */
    public static final String USAGE_REPORTING_GATEWAY_ID = "usageReportingGatewayId";

    /** SSH account provisioner class name */
    public static final String SSH_ACCOUNT_PROVISIONER = "sshAccountProvisioner";

    /** Additional info for SSH account provisioner */
    public static final String SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO = "sshAccountProvisionerAdditionalInfo";

    /** Prefix for SSH account provisioner configuration keys */
    public static final String SSH_PROVISIONER_CONFIG_PREFIX = "ssh.provisioner.config.";

    // ============================================================================
    // STORAGE RESOURCE PREFERENCE KEYS
    // Resource ID: storageResourceId
    // ============================================================================

    /** Root location on the file system */
    public static final String FILE_SYSTEM_ROOT_LOCATION = "fileSystemRootLocation";

    // Note: LOGIN_USERNAME and RESOURCE_CREDENTIAL_TOKEN are shared with compute preferences

    // ============================================================================
    // BATCH QUEUE PREFERENCE KEYS
    // Resource ID: computeResourceId:queueName
    // ============================================================================

    /**
     * Inner class for batch queue preference keys.
     * These control queue policies and limits at different levels.
     */
    public static final class BatchQueue {
        private BatchQueue() {}

        /** Maximum number of nodes allowed (INTEGER) */
        public static final String MAX_NODES = "maxNodes";

        /** Maximum number of CPUs/cores allowed (INTEGER) */
        public static final String MAX_CPUS = "maxCpus";

        /** Maximum walltime in minutes (INTEGER) */
        public static final String MAX_WALLTIME = "maxWalltime";

        /** Maximum number of jobs in queue (INTEGER) */
        public static final String MAX_JOBS_IN_QUEUE = "maxJobsInQueue";

        /** Maximum memory in MB (INTEGER) */
        public static final String MAX_MEMORY = "maxMemory";

        /** Default number of nodes (INTEGER) */
        public static final String DEFAULT_NODES = "defaultNodes";

        /** Default number of CPUs (INTEGER) */
        public static final String DEFAULT_CPUS = "defaultCpus";

        /** Default walltime in minutes (INTEGER) */
        public static final String DEFAULT_WALLTIME = "defaultWalltime";

        /** Default memory in MB (INTEGER) */
        public static final String DEFAULT_MEMORY = "defaultMemory";

        /** Whether the queue is enabled (BOOLEAN) */
        public static final String QUEUE_ENABLED = "queueEnabled";

        /** JSON array of allowed user IDs (JSON) */
        public static final String ALLOWED_USERS = "allowedUsers";

        /** JSON array of blocked user IDs (JSON) */
        public static final String BLOCKED_USERS = "blockedUsers";

        /** JSON array of allowed group IDs (JSON) */
        public static final String ALLOWED_GROUPS = "allowedGroups";

        /** JSON array of blocked group IDs (JSON) */
        public static final String BLOCKED_GROUPS = "blockedGroups";

        /** Priority multiplier for scheduling (INTEGER) */
        public static final String PRIORITY = "priority";

        /** Queue-specific macros for job scripts (STRING) */
        public static final String QUEUE_MACROS = "queueMacros";
    }

    // ============================================================================
    // APPLICATION PREFERENCE KEYS
    // Resource ID: applicationInterfaceId
    // ============================================================================

    /**
     * Inner class for application preference keys.
     * These control application defaults and behavior.
     */
    public static final class Application {
        private Application() {}

        /** Default compute resource ID for this application (STRING) */
        public static final String DEFAULT_COMPUTE_RESOURCE = "defaultComputeResource";

        /** Default queue name (STRING) */
        public static final String DEFAULT_QUEUE = "defaultQueue";

        /** Default walltime in minutes (INTEGER) */
        public static final String DEFAULT_WALLTIME = "defaultWalltime";

        /** Default number of nodes (INTEGER) */
        public static final String DEFAULT_NODE_COUNT = "defaultNodeCount";

        /** Default number of CPUs (INTEGER) */
        public static final String DEFAULT_CPU_COUNT = "defaultCpuCount";

        /** Default memory in MB (INTEGER) */
        public static final String DEFAULT_MEMORY = "defaultMemory";

        /** Whether the application is enabled (BOOLEAN) */
        public static final String ENABLED = "enabled";

        /** JSON array of allowed user IDs (JSON) */
        public static final String ALLOWED_USERS = "allowedUsers";

        /** JSON array of blocked user IDs (JSON) */
        public static final String BLOCKED_USERS = "blockedUsers";

        /** JSON array of allowed group IDs (JSON) */
        public static final String ALLOWED_GROUPS = "allowedGroups";

        /** JSON array of required input names (JSON) */
        public static final String REQUIRED_INPUTS = "requiredInputs";

        /** JSON object of default input values (JSON) */
        public static final String DEFAULT_INPUT_VALUES = "defaultInputValues";

        /** Output parsing template ID (STRING) */
        public static final String OUTPUT_PARSING_TEMPLATE = "outputParsingTemplate";

        /** Maximum concurrent instances per user (INTEGER) */
        public static final String MAX_CONCURRENT_PER_USER = "maxConcurrentPerUser";

        /** Application-specific credential token (STRING) */
        public static final String CREDENTIAL_TOKEN = "credentialToken";

        /** Default storage resource for output (STRING) */
        public static final String DEFAULT_STORAGE_RESOURCE = "defaultStorageResource";

        /** Whether to archive working directory (BOOLEAN) */
        public static final String ARCHIVE_WORKING_DIR = "archiveWorkingDirectory";
    }

    // ============================================================================
    // GATEWAY PREFERENCE KEYS
    // Resource ID: gatewayId
    // ============================================================================

    /**
     * Inner class for gateway configuration keys.
     * These control gateway-level features and settings.
     */
    public static final class Gateway {
        private Gateway() {}

        /** Whether experiment launching is enabled (BOOLEAN) */
        public static final String ENABLE_EXPERIMENT_LAUNCH = "enableExperimentLaunch";

        /** Whether data transfer features are enabled (BOOLEAN) */
        public static final String ENABLE_DATA_TRANSFER = "enableDataTransfer";

        /** Whether workflow features are enabled (BOOLEAN) */
        public static final String ENABLE_WORKFLOWS = "enableWorkflows";

        /** Maximum concurrent experiments per user (INTEGER) */
        public static final String MAX_CONCURRENT_EXPERIMENTS = "maxConcurrentExperiments";

        /** Default storage resource ID (STRING) */
        public static final String DEFAULT_STORAGE_RESOURCE = "defaultStorageResource";

        /** Default compute resource ID (STRING) */
        public static final String DEFAULT_COMPUTE_RESOURCE = "defaultComputeResource";

        /** UI theme name (STRING) */
        public static final String UI_THEME = "uiTheme";

        /** Dashboard layout configuration (JSON) */
        public static final String DASHBOARD_LAYOUT = "dashboardLayout";

        /** Feature flags (JSON object of feature:enabled pairs) */
        public static final String FEATURE_FLAGS = "featureFlags";

        /** Notification settings (JSON) */
        public static final String NOTIFICATION_SETTINGS = "notificationSettings";

        /** Whether gateway is in maintenance mode (BOOLEAN) */
        public static final String MAINTENANCE_MODE = "maintenanceMode";

        /** Maintenance message to display (STRING) */
        public static final String MAINTENANCE_MESSAGE = "maintenanceMessage";

        /** Gateway announcement/banner message (STRING) */
        public static final String ANNOUNCEMENT = "announcement";

        /** Whether email notifications are enabled (BOOLEAN) */
        public static final String ENABLE_EMAIL_NOTIFICATIONS = "enableEmailNotifications";

        /** Default project ID for new experiments (STRING) */
        public static final String DEFAULT_PROJECT = "defaultProject";

        /** JSON array of enabled application interface IDs (JSON) */
        public static final String ENABLED_APPLICATIONS = "enabledApplications";

        /** JSON array of enabled compute resource IDs (JSON) */
        public static final String ENABLED_COMPUTE_RESOURCES = "enabledComputeResources";

        /** JSON array of enabled storage resource IDs (JSON) */
        public static final String ENABLED_STORAGE_RESOURCES = "enabledStorageResources";

        /** Maximum storage quota per user in bytes (INTEGER) */
        public static final String MAX_STORAGE_PER_USER = "maxStoragePerUser";
    }

    // ============================================================================
    // SYSTEM PREFERENCE KEYS
    // Resource ID: "GLOBAL" or gatewayId for gateway-specific overrides
    // ============================================================================

    /**
     * Inner class for system-wide configuration keys.
     * These control system-level settings with optional gateway overrides.
     */
    public static final class System {
        private System() {}

        /** Maximum experiments per user across all gateways (INTEGER) */
        public static final String MAX_EXPERIMENTS_PER_USER = "maxExperimentsPerUser";

        /** Maximum storage per user in bytes (INTEGER) */
        public static final String MAX_STORAGE_PER_USER = "maxStoragePerUser";

        /** Default credential lifetime in seconds (INTEGER) */
        public static final String DEFAULT_CREDENTIAL_LIFETIME = "defaultCredentialLifetime";

        /** Session timeout in seconds (INTEGER) */
        public static final String SESSION_TIMEOUT = "sessionTimeout";

        /** JSON array of enabled authentication providers (JSON) */
        public static final String ENABLED_AUTH_PROVIDERS = "enabledAuthProviders";

        /** Audit log retention period in days (INTEGER) */
        public static final String AUDIT_LOG_RETENTION = "auditLogRetention";

        /** Whether new gateway registration is allowed (BOOLEAN) */
        public static final String ALLOW_GATEWAY_REGISTRATION = "allowGatewayRegistration";

        /** Whether user self-registration is allowed (BOOLEAN) */
        public static final String ALLOW_USER_REGISTRATION = "allowUserRegistration";

        /** Maximum file upload size in bytes (INTEGER) */
        public static final String MAX_UPLOAD_SIZE = "maxUploadSize";

        /** Default experiment data retention in days (INTEGER) */
        public static final String EXPERIMENT_DATA_RETENTION = "experimentDataRetention";

        /** System-wide maintenance mode (BOOLEAN) */
        public static final String SYSTEM_MAINTENANCE_MODE = "systemMaintenanceMode";

        /** System maintenance message (STRING) */
        public static final String SYSTEM_MAINTENANCE_MESSAGE = "systemMaintenanceMessage";

        /** Email sender address for notifications (STRING) */
        public static final String EMAIL_SENDER = "emailSender";

        /** SMTP server host (STRING) */
        public static final String SMTP_HOST = "smtpHost";

        /** SMTP server port (INTEGER) */
        public static final String SMTP_PORT = "smtpPort";

        /** Rate limit: max API requests per minute per user (INTEGER) */
        public static final String API_RATE_LIMIT = "apiRateLimit";
    }
}
