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
package org.apache.airavata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Immutable Spring Boot configuration properties for Airavata server.
 * All properties are loaded atomically at startup via constructor binding.
 *
 * <p>This record mirrors the property file structure exactly. Spring Boot's
 * relaxed binding handles kebab-case to camelCase conversion automatically.
 * All properties are prefixed with "airavata." in application.properties.
 *
 * <p>Properties should be set in application.properties. Runtime validation
 * is performed by PropertiesValidationConfig for fail-fast behavior.
 */
@ConfigurationProperties(prefix = "airavata")
public record AiravataServerProperties(
        // Core Airavata settings (formerly nested under airavata.*)
        String home,
        String defaultGateway,
        boolean validationEnabled,
        Sharing sharing,
        int inMemoryCacheSize,
        String localDataLocation,
        long maxArchiveSize,
        StreamingTransfer streamingTransfer,
        Hibernate hibernate,
        // Subsystem configuration
        Database database,
        Security security,
        RabbitMQ rabbitmq,
        Kafka kafka,
        Zookeeper zookeeper,
        Helix helix,
        Flyway flyway,
        Services services) {
    // ==================== Helper Methods ====================
    
    /**
     * Safely check if sharing is enabled.
     * Returns false if sharing configuration is null or not enabled.
     */
    public boolean isSharingEnabled() {
        return sharing != null && sharing.enabled();
    }

    /**
     * Safely check if RabbitMQ is enabled.
     * Returns false if rabbitmq configuration is null or not enabled.
     */
    public boolean isRabbitMQEnabled() {
        return rabbitmq != null && rabbitmq.enabled();
    }

    /**
     * Safely get the RabbitMQ broker URL.
     * Returns null if rabbitmq configuration is null.
     */
    public String getRabbitMQBrokerUrl() {
        return rabbitmq != null ? rabbitmq.brokerUrl() : null;
    }
    
    // ==================== Core Airavata Settings ====================
    public record Sharing(boolean enabled) {}

    public record StreamingTransfer(boolean enabled) {}

    public record Hibernate(String hbm2ddlAuto) {}
    // ==================== Database Configuration ====================
    public record Database(
            Registry registry,
            Catalog catalog,
            Vault vault,
            Profile profile,
            Sharing sharing,
            Replica replica,
            Workflow workflow,
            Research research,
            String validationQuery) {
        public record Registry(String driver, String url, String user, String password, String validationQuery) {}

        public record Catalog(
                String driver, String url, String user, String password, String validationQuery, Hikari hikari) {
            public record Hikari(long leakDetectionThreshold, String poolName) {}
        }

        public record Vault(String driver, String url, String user, String password, String validationQuery) {}

        public record Profile(String driver, String url, String user, String password, String validationQuery) {}

        public record Sharing(String driver, String url, String user, String password, String validationQuery) {}

        public record Replica(String driver, String url, String user, String password, String validationQuery) {}

        public record Workflow(String driver, String url, String user, String password, String validationQuery) {}

        public record Research(String driver, String url, String user, String password, String validationQuery) {}
    }

    // ==================== Security Configuration ====================
    public record Security(Tls tls, AuthzCache authzCache, Authentication authentication, Iam iam, Vault vault) {
        public record Tls(boolean enabled, int clientTimeout, Keystore keystore) {
            public record Keystore(String path, String password) {}
        }

        public record AuthzCache(boolean enabled) {}

        public record Authentication(boolean enabled) {}

        public record Iam(
                boolean enabled, String serverUrl, String oauthClientId, String oauthClientSecret, Super superAdmin) {
            public record Super(String username, String password) {}
        }

        public record Vault(Keystore keystore) {
            public record Keystore(String url, String password, String alias) {}
        }
    }

    // ==================== Messaging Configuration ====================
    public record RabbitMQ(
            boolean enabled,
            String brokerUrl,
            String experimentExchangeName,
            String experimentLaunchQueueName,
            String processExchangeName,
            String statusExchangeName,
            String dbEventExchangeName,
            boolean durableQueue,
            int prefetchCount) {}

    public record Kafka(boolean enabled, String brokerUrl) {}

    // ==================== Infrastructure Configuration ====================
    public record Zookeeper(Server server, boolean embedded) {
        public record Server(String connection) {}
    }

    // ==================== Helix Configuration ====================
    public record Helix(Cluster cluster, Controller controller, Participant participant) {
        public record Cluster(String name) {}

        public record Controller(String name) {}

        public record Participant(String name) {}
    }

    // ==================== Flyway Configuration ====================
    public record Flyway(boolean enabled) {}

    // ==================== Services Configuration ====================
    public record Services(
            Thrift thrift,
            Rest rest,
            Api api,
            Participant participant,
            Controller controller,
            PreWm prewm,
            PostWm postwm,
            Parser parser,
            Scheduler scheduler,
            Monitor monitor,
            Sharing sharing,
            Registry registry,
            Background background,
            Research research,
            Agent agent,
            Fileserver fileserver,
            Telemetry telemetry,
            Dbus dbus) {
        public record Thrift(boolean enabled, Server server) {
            public record Server(int port) {}
        }

        public record Rest(boolean enabled, Server server) {
            public record Server(int port) {}
        }

        public record Api(Vault vault) {
            public record Vault(Keystore keystore) {
                public record Keystore(String url, String password, String alias) {}
            }
        }

        public record Participant(boolean enabled) {}

        public record Controller(boolean enabled) {}

        public record PreWm(boolean enabled, boolean loadBalanceClusters, String name) {}

        public record PostWm(boolean enabled, boolean loadBalanceClusters, String name) {}

        public record Parser(
                boolean enabled,
                boolean loadBalanceClusters,
                String name,
                String brokerConsumerGroup,
                String topic,
                String storageResourceId,
                boolean deleteContainer,
                double scanningInterval,
                int scanningParallelJobs,
                String enabledGateways,
                int timeStepSeconds) {}

        public record Scheduler(
                boolean enabled,
                Interpreter interpreter,
                Rescheduler rescheduler,
                double clusterScanningInterval,
                double jobScanningInterval,
                int clusterScanningParallelJobs,
                int maximumReschedulerThreshold,
                /** Simple class name for conditional matching - e.g. "DefaultComputeResourceSelectionPolicy" */
                String selectionPolicy,
                /** Simple class name for conditional matching - e.g. "ExponentialBackOffReScheduler" */
                String reschedulerPolicy) {
            public record Interpreter(boolean enabled) {}

            public record Rescheduler(boolean enabled) {}
        }

        public record Monitor(Email email, Realtime realtime, Compute compute) {
            public record Email(
                    boolean enabled,
                    String address,
                    String folderName,
                    String host,
                    String password,
                    String storeProtocol,
                    int period,
                    int connectionRetryInterval,
                    int expiryMins) {}

            public record Realtime(boolean enabled, String brokerConsumerGroup, String brokerTopic) {}

            public record Compute(
                    boolean enabled,
                    String brokerPublisherId,
                    String emailPublisherId,
                    String realtimePublisherId,
                    String brokerTopic,
                    String brokerConsumerGroup,
                    Notification notification,
                    String statusPublishEndpoint,
                    String validators,
                    int clusterCheckTimeWindow,
                    int clusterCheckRepeatTime) {
                public record Notification(String emailIds) {}
            }
        }

        public record Sharing(boolean enabled) {}

        public record Registry(boolean enabled) {}

        public record Background(Controller controller) {
            public record Controller(boolean enabled) {}
        }

        public record Research(
                boolean enabled,
                Grpc grpc,
                Hub hub,
                Portal portal,
                Server server,
                Spring spring,
                Springdoc springdoc,
                Openid openid) {
            public record Grpc(
                    int port, String keepaliveTime, String keepaliveTimeout, boolean permitKeepaliveWithoutCalls) {}

            public record Hub(String adminApiKey, int limit, String url) {}

            public record Portal(String devUrl, String url) {}

            public record Server(int port) {}

            public record Spring(Servlet servlet) {
                public record Servlet(Multipart multipart) {
                    public record Multipart(String maxFileSize, String maxRequestSize) {}
                }
            }

            public record Springdoc(ApiDocs apiDocs, SwaggerUi swaggerUi) {
                public record ApiDocs(boolean enabled) {}

                public record SwaggerUi(
                        String docExpansion, String operationsSorter, String path, String tagsSorter, Oauth oauth) {
                    public record Oauth(String clientId, boolean usePkceWithAuthorizationCodeGrant) {}
                }
            }

            public record Openid(String url) {}
        }

        public record Agent(
                boolean enabled,
                Appinterface appinterface,
                Grpc grpc,
                Server server,
                Spring spring,
                Storage storage,
                Tunnelserver tunnelserver) {
            public record Appinterface(String id) {}

            public record Grpc(long maxInboundMessageSize, int port) {}

            public record Server(int port) {}

            public record Spring(Jpa jpa, Servlet servlet) {
                public record Jpa(Hibernate hibernate, boolean openInView) {
                    public record Hibernate(String ddlAuto) {}
                }

                public record Servlet(Multipart multipart) {
                    public record Multipart(String maxFileSize, String maxRequestSize) {}
                }
            }

            public record Storage(String id, String path) {}

            public record Tunnelserver(String url, String host, int port, String token) {}
        }

        public record Fileserver(boolean enabled, Server server, Spring spring) {
            public record Server(int port) {}

            public record Spring(Servlet servlet) {
                public record Servlet(Multipart multipart) {
                    public record Multipart(String maxFileSize, String maxRequestSize) {}
                }
            }
        }

        public record Telemetry(boolean enabled, Server server) {
            public record Server(int port) {}
        }

        public record Dbus(boolean enabled, String classpath) {}
    }
}
