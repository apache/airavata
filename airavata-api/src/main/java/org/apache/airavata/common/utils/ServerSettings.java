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

    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_USER_PASSWORD = "default.registry.password";
    private static final String DEFAULT_USER_GATEWAY = "default.gateway";
    private static final String ENABLE_SHARING = "sharing.enabled";

    // Zookeeper + curator constants
    public static final String ZOOKEEPER_SERVER_CONNECTION = "zookeeper.server.connection";
    private static final String CREDENTIAL_STORE_DB_URL = "airavata.jdbc.url.credential.store";
    private static final String CREDENTIAL_STORE_DB_USER = "airavata.jdbc.user";
    private static final String CREDENTIAL_STORE_DB_PASSWORD = "airavata.jdbc.password";
    private static final String CREDENTIAL_STORE_DB_DRIVER = "airavata.jdbc.driver";

    private static final java.lang.String API_SERVER_HOST = "api.server.host";
    private static final java.lang.String API_SERVER_PORT = "api.server.port";

    private static final String REGISTRY_DB_URL = "airavata.jdbc.url.registry";
    private static final String REGISTRY_DB_USER = "airavata.jdbc.user";
    private static final String REGISTRY_DB_PASSWORD = "airavata.jdbc.password";
    private static final String REGISTRY_DB_DRIVER = "airavata.jdbc.driver";
    private static final String HOST_SCHEDULER = "host.scheduler";
    public static final String JOB_NOTIFICATION_ENABLE = "job.notification.enabled";
    public static final String JOB_NOTIFICATION_EMAILIDS = "job.notification.emailids";

    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_STATUS_EXCHANGE_NAME = "rabbitmq.status.exchange.name";
    public static final String RABBITMQ_PROCESS_EXCHANGE_NAME = "rabbitmq.process.exchange.name";
    public static final String RABBITMQ_EXPERIMENT_EXCHANGE_NAME = "rabbitmq.experiment.exchange.name";
    public static final String RABBITMQ_DURABLE_QUEUE = "rabbitmq.durable.queue";
    public static final String RABBITMQ_PREFETCH_COUNT = "rabbitmq.prefetch.count";

    // email-based monitoring configurations
    private static final String EMAIL_BASED_MONITORING_PERIOD = "monitor.email.period.millis";
    private static final String EMAIL_BASED_MONITOR_HOST = "monitor.email.host";
    private static final String EMAIL_BASED_MONITOR_ADDRESS = "monitor.email.address";
    private static final String EMAIL_BASED_MONITOR_PASSWORD = "monitor.email.password";
    private static final String EMAIL_BASED_MONITOR_FOLDER_NAME = "monitor.email.folder.name";
    private static final String EMAIL_BASED_MONITOR_STORE_PROTOCOL = "monitor.email.store.protocol";

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
    public static final String METASCHEDULER_JOB_SCANNING_INTERVAL = "metascheduler.job.scanning.interval";
    public static final String METASCHEDULER_CLUSTER_SCANNING_INTERVAL = "metascheduler.cluster.scanning.interval";
    public static final String METASCHEDULER_NO_OF_SCANNING_PARALLEL_JOBS = "metascheduler.cluster.scanning.parallel.jobs";
    public static final String COMPUTE_RESOURCE_RESCHEDULER_CLASS = "compute.resource.rescheduler.policy.class";
    public static final String METASCHEDULER_MAXIMUM_RESCHEDULED_THRESHOLD =
            "metascheduler.maximum.rescheduler.threshold";
    public static final String DATA_ANALYZER_SCANNING_INTERVAL = "data.analyzer.scanning.interval";
    public static final String DATA_ANALYZER_NO_OF_SCANNING_PARALLEL_JOBS = "data.analyzer.scanning.parallel.jobs";
    public static final String DATA_ANALYZER_ENABLED = "data.analyzer.enabled";
    public static final String DATA_ANALYZER_ENABLED_GATEWAYS = "data.analyzer.gateways";
    public static final String DATA_ANALYZER_TIME_STEP_IN_SECONDS = "data.analyzer.period.seconds";

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
        return Integer.parseInt(getSetting(Constants.AUTHZ_CACHE_SIZE));
    }

    public static String getLocalDataLocation() {
        return getSetting(Constants.LOCAL_DATA_LOCATION, System.getProperty("java.io.tmpdir"));
    }

    public static Boolean isEnableSharing() throws ApplicationSettingsException {
        return Boolean.parseBoolean(getSetting(ENABLE_SHARING));
    }

    public static String getApiServerPort() {
        return getSetting(API_SERVER_PORT, "8930");
    }

    public static String getApiServerHost() {
        return getSetting(API_SERVER_HOST, "localhost");
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

    public static Boolean getDataAnalyzerEnabled() throws ApplicationSettingsException {
        return getSetting(DATA_ANALYZER_ENABLED).equalsIgnoreCase("true");
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
