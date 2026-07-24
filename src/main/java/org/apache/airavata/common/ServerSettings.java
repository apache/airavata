package org.apache.airavata.common;

public class ServerSettings extends ApplicationSettings {

    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_USER_PASSWORD = "default.registry.password";
    private static final String DEFAULT_USER_GATEWAY = "default.registry.gateway";
    private static final String ENABLE_SHARING = "enable.sharing";

    private static final String CREDENTIAL_STORE_DB_URL = "airavata.jdbc.url";
    private static final String CREDENTIAL_STORE_DB_USER = "airavata.jdbc.user";
    private static final String CREDENTIAL_STORE_DB_PASSWORD = "airavata.jdbc.password";
    private static final String CREDENTIAL_STORE_DB_DRIVER = "airavata.jdbc.driver";
    private static final java.lang.String SHARING_REGISTRY_PORT = "sharing.registry.server.port";
    private static final java.lang.String SHARING_REGISTRY_HOST = "sharing.registry.server.host";

    private static final String REGISTRY_DB_URL = "airavata.jdbc.url";
    private static final String REGISTRY_DB_USER = "airavata.jdbc.user";
    private static final String REGISTRY_DB_PASSWORD = "airavata.jdbc.password";
    private static final String REGISTRY_DB_DRIVER = "airavata.jdbc.driver";
    private static final String HOST_SCHEDULER = "host.scheduler";
    public static final String JOB_NOTIFICATION_ENABLE = "job.notification.enable";
    public static final String JOB_NOTIFICATION_EMAILIDS = "job.notification.emailids";

    // email-based monitoring configurations
    private static final String EMAIL_BASED_MONITORING_PERIOD = "email.based.monitoring.period";
    private static final String EMAIL_BASED_MONITOR_HOST = "email.based.monitor.host";
    private static final String EMAIL_BASED_MONITOR_ADDRESS = "email.based.monitor.address";
    private static final String EMAIL_BASED_MONITOR_PASSWORD = "email.based.monitor.password";
    private static final String EMAIL_BASED_MONITOR_FOLDER_NAME = "email.based.monitor.folder.name";
    private static final String EMAIL_BASED_MONITOR_STORE_PROTOCOL = "email.based.monitor.store.protocol";

    // Profile Service Constants
    public static final String PROFILE_SERVICE_SERVER_HOST = "profile.service.server.host";
    public static final String PROFILE_SERVICE_SERVER_PORT = "profile.service.server.port";

    // Iam Server Constants
    public static final String IAM_SERVER_URL = "iam.server.url";
    public static final String IAM_SERVER_SUPER_ADMIN_USERNAME = "iam.server.super.admin.username";
    public static final String IAM_SERVER_SUPER_ADMIN_PASSWORD = "iam.server.super.admin.password";

    private static boolean stopAllThreads = false;

    // Airavata Metascheduler
    public static final String COMPUTE_RESOURCE_SELECTION_POLICY_CLASS = "compute.resource.selection.policy.class";
    public static final String METASCHEDULER_GATEWAY = "metascheduler.gateway";
    public static final String METASCHEDULER_GRP_ID = "metascheduler.group.resource.profile";
    public static final String METASCHEDULER_USERNAME = "metascheduler.username";
    public static final String METASCHEDULER_CLUSTER_SCANNING_INTERVAL = "cluster.scanning.interval";
    public static final String METASCHEDULER_JOB_SCANNING_INTERVAL = "job.scanning.interval";
    public static final String METASCHEDULER_NO_OF_SCANNING_PARALLEL_JOBS = "cluster.scanning.parallel.jobs";

    public static String getDefaultUser() throws Exception {
        return getSetting(DEFAULT_USER);
    }

    public static String getDefaultUserPassword() throws Exception {
        return getSetting(DEFAULT_USER_PASSWORD);
    }

    public static String getDefaultUserGateway() throws Exception {
        return getSetting(DEFAULT_USER_GATEWAY);
    }

    public static String getCredentialStoreDBUser() throws Exception {
        try {
            return getSetting(CREDENTIAL_STORE_DB_USER);
        } catch (Exception e) {
            return getSetting(REGISTRY_DB_USER);
        }
    }

    public static String getCredentialStoreDBPassword() throws Exception {
        try {
            return getSetting(CREDENTIAL_STORE_DB_PASSWORD);
        } catch (Exception e) {
            return getSetting(REGISTRY_DB_PASSWORD);
        }
    }

    public static String getCredentialStoreDBDriver() throws Exception {
        try {
            return getSetting(CREDENTIAL_STORE_DB_DRIVER);
        } catch (Exception e) {
            return getSetting(REGISTRY_DB_DRIVER);
        }
    }

    public static String getCredentialStoreDBURL() throws Exception {
        try {
            return getSetting(CREDENTIAL_STORE_DB_URL);
        } catch (Exception e) {
            return getSetting(REGISTRY_DB_URL);
        }
    }

    public static String getHostScheduler() throws Exception {
        return getSetting(HOST_SCHEDULER);
    }

    public static boolean isStopAllThreads() {
        return stopAllThreads;
    }

    public static void setStopAllThreads(boolean stopAllThreads) {
        ServerSettings.stopAllThreads = stopAllThreads;
    }

    public static int getEmailMonitorPeriod() throws Exception {
        return Integer.parseInt(getSetting(EMAIL_BASED_MONITORING_PERIOD, "100000"));
    }

    public static String getEmailBasedMonitorHost() throws Exception {
        return getSetting(EMAIL_BASED_MONITOR_HOST);
    }

    public static String getEmailBasedMonitorAddress() throws Exception {
        return getSetting(EMAIL_BASED_MONITOR_ADDRESS);
    }

    public static String getEmailBasedMonitorPassword() throws Exception {
        return getSetting(EMAIL_BASED_MONITOR_PASSWORD);
    }

    public static String getEmailBasedMonitorFolderName() throws Exception {
        return getSetting(EMAIL_BASED_MONITOR_FOLDER_NAME);
    }

    public static String getEmailBasedMonitorStoreProtocol() throws Exception {
        return getSetting(EMAIL_BASED_MONITOR_STORE_PROTOCOL);
    }

    public static String getRemoteIDPServiceUrl() throws Exception {
        return getSetting(ServerSettings.IAM_SERVER_URL);
    }

    public static String getIamServerSuperAdminUsername() throws Exception {
        return getSetting(ServerSettings.IAM_SERVER_SUPER_ADMIN_USERNAME);
    }

    public static String getIamServerSuperAdminPassword() throws Exception {
        return getSetting(ServerSettings.IAM_SERVER_SUPER_ADMIN_PASSWORD);
    }

    public static boolean isTLSEnabled() throws Exception {
        return Boolean.parseBoolean(getSetting(Constants.IS_TLS_ENABLED, "false"));
    }

    public static String getTlsCertPath() throws Exception {
        return getSetting(Constants.TLS_CERT_PATH);
    }

    public static String getTlsKeyPath() throws Exception {
        return getSetting(Constants.TLS_KEY_PATH);
    }

    public static int getTLSClientTimeout() throws Exception {
        return Integer.parseInt(getSetting(Constants.TLS_CLIENT_TIMEOUT));
    }

    public static String getSecurityManagerClassName() throws Exception {
        return getSetting(Constants.SECURITY_MANAGER_CLASS);
    }

    public static String getAuthzCacheManagerClassName() throws Exception {
        return getSetting(Constants.AUTHZ_CACHE_MANAGER_CLASS);
    }

    public static boolean isAuthzCacheEnabled() throws Exception {
        return Boolean.parseBoolean(getSetting(Constants.AUTHZ_CACHE_ENABLED));
    }

    public static int getCacheSize() throws Exception {
        return Integer.parseInt(getSetting(Constants.IN_MEMORY_CACHE_SIZE));
    }

    public static String getLocalDataLocation() {
        return getSetting(Constants.LOCAL_DATA_LOCATION, System.getProperty("java.io.tmpdir"));
    }

    public static Boolean isEnableSharing() throws Exception {
        return Boolean.parseBoolean(getSetting(ENABLE_SHARING));
    }

    public static String getSharingRegistryPort() {
        return getSetting(SHARING_REGISTRY_PORT, "7878");
    }

    public static String getSharingRegistryHost() {
        return getSetting(SHARING_REGISTRY_HOST, "localhost");
    }

    public static Boolean isSteamingEnabled() {
        return Boolean.valueOf(getSetting(Constants.ENABLE_STREAMING_TRANSFER, "True"));
    }

    public static String getComputeResourceSelectionPolicyClass() throws Exception {
        return getSetting(
                COMPUTE_RESOURCE_SELECTION_POLICY_CLASS,
                "org.apache.airavata.orchestration.task.MultipleComputeResourcePolicy");
    }

    public static String getMetaschedulerGateway() throws Exception {
        return getSetting(METASCHEDULER_GATEWAY, "");
    }

    public static String getMetaschedulerGrpId() throws Exception {
        return getSetting(METASCHEDULER_GRP_ID, "");
    }

    public static String getMetaschedulerUsername() throws Exception {
        return getSetting(METASCHEDULER_USERNAME, "");
    }

    public static double getMetaschedulerClusterScanningInterval() throws Exception {
        return Double.parseDouble(getSetting(METASCHEDULER_CLUSTER_SCANNING_INTERVAL, "1800000"));
    }

    public static double getMetaschedulerJobScanningInterval() throws Exception {
        return Double.parseDouble(getSetting(METASCHEDULER_JOB_SCANNING_INTERVAL, "1800000"));
    }

    public static int getMetaschedulerNoOfScanningParallelJobs() throws Exception {
        return Integer.parseInt(getSetting(METASCHEDULER_NO_OF_SCANNING_PARALLEL_JOBS, "1"));
    }
}
