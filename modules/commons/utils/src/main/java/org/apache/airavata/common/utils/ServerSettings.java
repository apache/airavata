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

package org.apache.airavata.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.airavata.common.exception.ApplicationSettingsException;

public class ServerSettings extends ApplicationSettings {

    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_USER_PASSWORD = "default.registry.password";
    private static final String DEFAULT_USER_GATEWAY = "default.registry.gateway";

    private static final String SERVER_CONTEXT_ROOT = "server.context-root";
    public static final String EMBEDDED_ZK = "embedded.zk";
    public static final String IP = "ip";

    private static final String CREDENTIAL_STORE_DB_URL = "credential.store.jdbc.url";
    private static final String CREDENTIAL_STORE_DB_USER = "credential.store.jdbc.user";
    private static final String CREDENTIAL_STORE_DB_PASSWORD = "credential.store.jdbc.password";
    private static final String CREDENTIAL_STORE_DB_DRIVER = "credential.store.jdbc.driver";

    private static final String REGISTRY_DB_URL = "registry.jdbc.url";
    private static final String REGISTRY_DB_USER = "registry.jdbc.user";
    private static final String REGISTRY_DB_PASSWORD = "registry.jdbc.password";
    private static final String REGISTRY_DB_DRIVER = "registry.jdbc.driver";
    private static final String ENABLE_HTTPS = "enable.https";
    private static final String HOST_SCHEDULER = "host.scheduler";
    private static final String MY_PROXY_SERVER = "myproxy.server";
    private static final String MY_PROXY_USER = "myproxy.user";
    private static final String MY_PROXY_PASSWORD = "myproxy.password";
    private static final String MY_PROXY_LIFETIME = "myproxy.life";
    private static final String STATUS_PUBLISHER = "status.publisher";
    private static final String TASK_LAUNCH_PUBLISHER = "task.launch.publisher";
    private static final String ACTIVITY_LISTENERS = "activity.listeners";
    public static final String PUBLISH_RABBITMQ = "publish.rabbitmq";
    public static final String JOB_NOTIFICATION_ENABLE = "job.notification.enable";
    public static final String JOB_NOTIFICATION_EMAILIDS = "job.notification.emailids";
    public static final String JOB_NOTIFICATION_FLAGS = "job.notification.flags";
    public static final String GFAC_PASSIVE = "gfac.passive"; // by default this is desabled
    public static final String LAUNCH_QUEUE_NAME = "launch.queue.name";
    public static final String CANCEL_QUEUE_NAME = "cancel.queue.name";


    //    Workflow Enactment Service component configuration.
    private static final String ENACTMENT_THREAD_POOL_SIZE = "enactment.thread.pool.size";
    private static final int DEFAULT_ENACTMENT_THREAD_POOL_SIZE = 10;
    private static final String WORKFLOW_PARSER = "workflow.parser";


    private static boolean stopAllThreads = false;

    public static String getDefaultUser() throws ApplicationSettingsException {
        return getSetting(DEFAULT_USER);
    }

    public static String getLaunchQueueName() {
        return getSetting(LAUNCH_QUEUE_NAME, "launch.queue");
    }


    public static String getCancelQueueName() {
        return getSetting(CANCEL_QUEUE_NAME, "cancel.queue");
    }

    public static String getDefaultUserPassword() throws ApplicationSettingsException {
        return getSetting(DEFAULT_USER_PASSWORD);
    }

    public static String getDefaultUserGateway() throws ApplicationSettingsException {
        return getSetting(DEFAULT_USER_GATEWAY);
    }

    public static String getServerContextRoot() {
        return getSetting(SERVER_CONTEXT_ROOT, "axis2");
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

    public static boolean isEnableHttps() {
        try {
            return Boolean.parseBoolean(getSetting(ENABLE_HTTPS));
        } catch (ApplicationSettingsException e) {
            return false;
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

    public static String getMyProxyServer() throws ApplicationSettingsException {
        return getSetting(MY_PROXY_SERVER);
    }

    public static String getMyProxyUser() throws ApplicationSettingsException {
        return getSetting(MY_PROXY_USER);
    }

    public static String getMyProxyPassword() throws ApplicationSettingsException {
        return getSetting(MY_PROXY_PASSWORD);
    }

    public static int getMyProxyLifetime() throws ApplicationSettingsException {
        return Integer.parseInt(getSetting(MY_PROXY_LIFETIME));
    }

    public static String[] getActivityListeners() throws ApplicationSettingsException {
        return getSetting(ACTIVITY_LISTENERS).split(",");
    }

    public static String getStatusPublisher() throws ApplicationSettingsException {
        return getSetting(STATUS_PUBLISHER);
    }

    public static String getTaskLaunchPublisher() throws ApplicationSettingsException {
        return getSetting(TASK_LAUNCH_PUBLISHER);
    }

    public static boolean isRabbitMqPublishEnabled() throws ApplicationSettingsException {
        String setting = getSetting(PUBLISH_RABBITMQ);
        return Boolean.parseBoolean(setting);
    }

    public static boolean isGFacPassiveMode()throws ApplicationSettingsException {
        String setting = getSetting(GFAC_PASSIVE);
        return Boolean.parseBoolean(setting);
    }

    public static boolean isEmbeddedZK() {
        return Boolean.parseBoolean(getSetting(EMBEDDED_ZK, "true"));
    }

    public static String getIp() {
        try {
            return getSetting(IP);
        } catch (ApplicationSettingsException e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public static int getEnactmentThreadPoolSize() {
        String threadPoolSize = null;
        try {
            threadPoolSize = getSetting(ENACTMENT_THREAD_POOL_SIZE);
        } catch (ApplicationSettingsException e) {
            return DEFAULT_ENACTMENT_THREAD_POOL_SIZE;
        }
        return Integer.valueOf(threadPoolSize);
    }

    public static String getWorkflowParser() throws ApplicationSettingsException {
        return getSetting(WORKFLOW_PARSER);
    }
}
