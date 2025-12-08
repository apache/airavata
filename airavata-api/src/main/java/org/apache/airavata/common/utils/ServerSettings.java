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
package org.apache.airavata.common.utils;

import java.io.File;
import org.apache.airavata.common.exception.ApplicationSettingsException;

public class ServerSettings extends ApplicationSettings {

    private static final String DEFAULT_USER = "services.default.user";
    private static final String DEFAULT_USER_PASSWORD = "services.default.password";
    private static final String DEFAULT_USER_GATEWAY = "services.default.gateway";
    private static final String ENABLE_SHARING = "services.sharing.enabled";

    // Zookeeper + curator constants
    public static final String EMBEDDED_ZK = "zookeeper.embedded";
    public static final String ZOOKEEPER_SERVER_CONNECTION = "zookeeper.server-connection";
    private static final String CREDENTIAL_STORE_DB_URL = "database.vault.url";
    private static final String CREDENTIAL_STORE_DB_USER = "database.vault.user";
    private static final String CREDENTIAL_STORE_DB_PASSWORD = "database.vault.password";
    private static final String CREDENTIAL_STORE_DB_DRIVER = "database.vault.driver";
    private static final java.lang.String SHARING_REGISTRY_PORT = "services.sharing.server-port";
    private static final java.lang.String SHARING_REGISTRY_HOST = "services.sharing.server-host";

    private static final String REGISTRY_DB_URL = "database.registry.url";
    private static final String REGISTRY_DB_USER = "database.registry.user";
    private static final String REGISTRY_DB_PASSWORD = "database.registry.password";
    private static final String REGISTRY_DB_DRIVER = "database.registry.driver";
    private static final String HOST_SCHEDULER = "services.scheduler.classpath";
    public static final String JOB_NOTIFICATION_ENABLE = "services.monitor.job.notification.enable";
    public static final String JOB_NOTIFICATION_EMAILIDS = "services.monitor.job.notification.emailids";

    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker-url";
    public static final String RABBITMQ_STATUS_EXCHANGE_NAME = "rabbitmq.status-exchange-name";
    public static final String RABBITMQ_PROCESS_EXCHANGE_NAME = "rabbitmq.process-exchange-name";
    public static final String RABBITMQ_EXPERIMENT_EXCHANGE_NAME = "rabbitmq.experiment-exchange-name";
    public static final String RABBITMQ_DURABLE_QUEUE = "rabbitmq.durable-queue";
    public static final String RABBITMQ_PREFETCH_COUNT = "rabbitmq.prefetch-count";

    // email-based monitoring configurations
    private static final String EMAIL_BASED_MONITORING_PERIOD = "services.monitor.email.period";
    private static final String EMAIL_BASED_MONITOR_HOST = "services.monitor.email.host";
    private static final String EMAIL_BASED_MONITOR_ADDRESS = "services.monitor.email.address";
    private static final String EMAIL_BASED_MONITOR_PASSWORD = "services.monitor.email.password";
    private static final String EMAIL_BASED_MONITOR_FOLDER_NAME = "services.monitor.email.folder-name";
    private static final String EMAIL_BASED_MONITOR_STORE_PROTOCOL = "services.monitor.email.store-protocol";

    // Profile Service Constants
    public static final String PROFILE_SERVICE_SERVER_HOST = "services.api.profile.server.host";
    public static final String PROFILE_SERVICE_SERVER_PORT = "services.api.profile.server.port";

    // Iam Server Constants
    public static final String IAM_SERVER_URL = "security.iam.server-url";
    public static final String IAM_SERVER_SUPER_ADMIN_USERNAME = "security.iam.super-admin-username";
    public static final String IAM_SERVER_SUPER_ADMIN_PASSWORD = "security.iam.super-admin-password";

    private static boolean stopAllThreads = false;

    // Airavata Metascheduler
    public static final String COMPUTE_RESOURCE_SELECTION_POLICY_CLASS =
            "services.scheduler.compute-resource-selection-policy-class";
    public static final String METASCHEDULER_GATEWAY = "services.scheduler.gateway";
    public static final String METASCHEDULER_GRP_ID = "services.scheduler.group-resource-profile";
    public static final String METASCHEDULER_USERNAME = "services.scheduler.username";
    public static final String METASCHEDULER_CLUSTER_SCANNING_INTERVAL = "services.scheduler.cluster-scanning-interval";
    public static final String METASCHEDULER_JOB_SCANNING_INTERVAL = "services.scheduler.job-scanning-interval";
    public static final String METASCHEDULER_NO_OF_SCANNING_PARALLEL_JOBS =
            "services.scheduler.cluster-scanning-parallel-jobs";
    public static final String COMPUTE_RESOURCE_RESCHEDULER_CLASS =
            "services.scheduler.compute-resource-rescheduler-policy-class";
    public static final String METASCHEDULER_MAXIMUM_RESCHEDULED_THRESHOLD =
            "services.scheduler.maximum-rescheduler-threshold";
    public static final String DATA_ANALYZER_SCANNING_INTERVAL = "services.parser.scanning-interval";
    public static final String DATA_ANALYZER_NO_OF_SCANNING_PARALLEL_JOBS = "services.parser.scanning-parallel-jobs";
    public static final String DATA_ANALYZER_ENABLED_GATEWAYS = "services.parser.enabled-gateways";
    public static final String DATA_ANALYZER_TIME_STEP_IN_SECONDS = "services.parser.time-step-seconds";

    public static String getDefaultUser() throws ApplicationSettingsException {
        return getSetting(DEFAULT_USER);
    }

    public static String getRabbitmqExperimentLaunchQueueName() {
        return getSetting(RABBITMQ_EXPERIMENT_EXCHANGE_NAME, "experiment.launch.queue");
    }

    public static String getRabbitmqBrokerUrl() {
        return getSetting(RABBITMQ_BROKER_URL, "amqp://localhost:5672");
    }

    public static String getRabbitmqStatusExchangeName() {
        return getSetting(RABBITMQ_STATUS_EXCHANGE_NAME, "status_exchange");
    }

    public static String getRabbitmqProcessExchangeName() {
        return getSetting(RABBITMQ_PROCESS_EXCHANGE_NAME, "process_exchange");
    }

    public static String getRabbitmqExperimentExchangeName() {
        return getSetting(RABBITMQ_EXPERIMENT_EXCHANGE_NAME, "experiment_exchange");
    }

    public static boolean getRabbitmqDurableQueue() {
        return Boolean.parseBoolean(getSetting(RABBITMQ_DURABLE_QUEUE, "false"));
    }

    public static int getRabbitmqPrefetchCount() {
        return Integer.parseInt(getSetting(RABBITMQ_PREFETCH_COUNT, "200"));
    }

    public static String getDefaultUserPassword() throws ApplicationSettingsException {
        return getSetting(DEFAULT_USER_PASSWORD);
    }

    public static String getDefaultUserGateway() throws ApplicationSettingsException {
        return getSetting(DEFAULT_USER_GATEWAY);
    }

    public static String getCredentialStoreDBUser() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_USER);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_USER);
        }
    }

    public static String getCredentialStoreDBPassword() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_PASSWORD);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_PASSWORD);
        }
    }

    public static String getCredentialStoreDBDriver() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_DRIVER);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_DRIVER);
        }
    }

    public static String getCredentialStoreDBURL() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_URL);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_URL);
        }
    }

    public static String getHostScheduler() throws ApplicationSettingsException {
        return getSetting(HOST_SCHEDULER);
    }

    public static boolean isStopAllThreads() {
        return stopAllThreads;
    }

    public static void setStopAllThreads(boolean stopAllThreads) {
        ServerSettings.stopAllThreads = stopAllThreads;
    }

    public static boolean isEmbeddedZK() {
        return Boolean.parseBoolean(getSetting(EMBEDDED_ZK, "true"));
    }

    public static int getEmailMonitorPeriod() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(EMAIL_BASED_MONITORING_PERIOD, "100000"));
    }

    public static String getEmailBasedMonitorHost() throws ApplicationSettingsException {
        return getSetting(EMAIL_BASED_MONITOR_HOST);
    }

    public static String getEmailBasedMonitorAddress() throws ApplicationSettingsException {
        return getSetting(EMAIL_BASED_MONITOR_ADDRESS);
    }

    public static String getEmailBasedMonitorPassword() throws ApplicationSettingsException {
        return getSetting(EMAIL_BASED_MONITOR_PASSWORD);
    }

    public static String getEmailBasedMonitorFolderName() throws ApplicationSettingsException {
        return getSetting(EMAIL_BASED_MONITOR_FOLDER_NAME);
    }

    public static String getEmailBasedMonitorStoreProtocol() throws ApplicationSettingsException {
        return getSetting(EMAIL_BASED_MONITOR_STORE_PROTOCOL);
    }

    public static String getRemoteIDPServiceUrl() throws ApplicationSettingsException {
        return getSetting(ServerSettings.IAM_SERVER_URL);
    }

    public static String getIamServerSuperAdminUsername() throws ApplicationSettingsException {
        return getSetting(ServerSettings.IAM_SERVER_SUPER_ADMIN_USERNAME);
    }

    public static String getIamServerSuperAdminPassword() throws ApplicationSettingsException {
        return getSetting(ServerSettings.IAM_SERVER_SUPER_ADMIN_PASSWORD);
    }

    public static String getZookeeperConnection() throws ApplicationSettingsException {
        return getSetting(ZOOKEEPER_SERVER_CONNECTION, "localhost:2181");
    }

    public static boolean isTLSEnabled() throws ApplicationSettingsException {
        return Boolean.parseBoolean(getSetting(Constants.IS_TLS_ENABLED, "false"));
    }

    public static String getKeyStorePath() throws ApplicationSettingsException {
        String airavataConfigDir = getSetting(AIRAVATA_CONFIG_DIR);
        String keystorePath = getSetting(Constants.KEYSTORE_PATH);
        return new File(airavataConfigDir, keystorePath).getAbsolutePath();
    }

    public static String getKeyStorePassword() throws ApplicationSettingsException {
        return getSetting(Constants.KEYSTORE_PASSWORD);
    }

    public static int getTLSClientTimeout() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(Constants.TLS_CLIENT_TIMEOUT));
    }

    public static String getSecurityManagerClassName() throws ApplicationSettingsException {
        return getSetting(Constants.SECURITY_MANAGER_CLASS);
    }

    public static String getAuthzCacheManagerClassName() throws ApplicationSettingsException {
        return getSetting(Constants.AUTHZ_CACHE_MANAGER_CLASS);
    }

    public static boolean isAuthzCacheEnabled() throws ApplicationSettingsException {
        return Boolean.parseBoolean(getSetting(Constants.AUTHZ_CACHE_ENABLED));
    }

    public static int getCacheSize() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(Constants.IN_MEMORY_CACHE_SIZE));
    }

    public static String getLocalDataLocation() {
        return getSetting(Constants.LOCAL_DATA_LOCATION, System.getProperty("java.io.tmpdir"));
    }

    public static Boolean isEnableSharing() throws ApplicationSettingsException {
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

    public static String getComputeResourceSelectionPolicyClass() throws ApplicationSettingsException {
        return getSetting(
                COMPUTE_RESOURCE_SELECTION_POLICY_CLASS,
                "org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection.MultipleComputeResourcePolicy");
    }

    public static String getReSchedulerPolicyClass() throws ApplicationSettingsException {
        return getSetting(
                COMPUTE_RESOURCE_RESCHEDULER_CLASS,
                "org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ExponentialBackOffReScheduler");
    }

    public static String getMetaschedulerGateway() throws ApplicationSettingsException {
        return getSetting(METASCHEDULER_GATEWAY, "");
    }

    public static String getMetaschedulerGrpId() throws ApplicationSettingsException {
        return getSetting(METASCHEDULER_GRP_ID, "");
    }

    public static String getMetaschedulerUsername() throws ApplicationSettingsException {
        return getSetting(METASCHEDULER_USERNAME, "");
    }

    public static String getDataAnalyzingEnabledGateways() throws ApplicationSettingsException {
        return getSetting(DATA_ANALYZER_ENABLED_GATEWAYS, "");
    }

    public static int getDataAnalyzerTimeStep() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(DATA_ANALYZER_TIME_STEP_IN_SECONDS, "1"));
    }

    public static double getMetaschedulerClusterScanningInterval() throws ApplicationSettingsException {
        return Double.parseDouble(getSetting(METASCHEDULER_CLUSTER_SCANNING_INTERVAL, "1800000"));
    }

    public static double getMetaschedulerJobScanningInterval() throws ApplicationSettingsException {
        return Double.parseDouble(getSetting(METASCHEDULER_JOB_SCANNING_INTERVAL, "1800000"));
    }

    public static int getMetaschedulerNoOfScanningParallelJobs() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(METASCHEDULER_NO_OF_SCANNING_PARALLEL_JOBS, "1"));
    }

    public static double getDataAnalyzerScanningInterval() throws ApplicationSettingsException {
        return Double.parseDouble(getSetting(DATA_ANALYZER_SCANNING_INTERVAL, "1800000"));
    }

    public static int getDataAnalyzerNoOfScanningParallelJobs() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(DATA_ANALYZER_NO_OF_SCANNING_PARALLEL_JOBS, "1"));
    }

    public static int getMetaschedulerReschedulingThreshold() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(METASCHEDULER_MAXIMUM_RESCHEDULED_THRESHOLD, "5"));
    }
}
