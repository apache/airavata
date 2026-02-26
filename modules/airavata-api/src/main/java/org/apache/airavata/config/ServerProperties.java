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
 * is performed by PropertiesValidationConfiguration for fail-fast behavior.
 */
@ConfigurationProperties(prefix = "airavata")
public record ServerProperties(
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
        Cors cors,
        // Subsystem configuration
        Security security,
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

    // ==================== Core Airavata Settings ====================
    public record Sharing(boolean enabled) {}

    public record StreamingTransfer(boolean enabled) {}

    public record Hibernate(String hbm2ddlAuto) {}

    public record Cors(java.util.List<String> allowedOrigins) {}

    // ==================== Security Configuration ====================
    public record Security(Tls tls, Authentication authentication, Iam iam, Vault vault) {
        public record Tls(boolean enabled, int clientTimeout, Keystore keystore) {
            public record Keystore(String path, String password) {}
        }

        public record Authentication(boolean enabled) {}

        public record Iam(
                boolean enabled,
                String serverUrl,
                String realm,
                String oauthClientId,
                String oauthClientSecret,
                Super superAdmin) {
            public record Super(String username, String password) {}
        }

        public record Vault(Keystore keystore) {
            public record Keystore(String url, String password, String alias) {}
        }
    }

    // ==================== Flyway Configuration ====================
    public record Flyway(boolean enabled) {}

    // ==================== Services Configuration ====================
    // Note: HTTP port uses standard server.port, gRPC port uses spring.grpc.server.port.
    // Vault keystore uses airavata.security.vault.keystore.* (no per-service duplication).
    public record Services(
            Rest rest,
            Participant participant,
            Controller controller,
            Scheduler scheduler,
            Monitor monitor,
            Sharing sharing,
            Registry registry,
            Research research,
            Agent agent,
            Fileserver fileserver,
            Telemetry telemetry,
            Dbus dbus) {

        public record Rest(boolean enabled) {}

        public record Participant(boolean enabled) {}

        public record Controller(boolean enabled) {}

        public record Scheduler(
                Interpreter interpreter,
                Rescheduler rescheduler,
                double clusterScanningInterval,
                double jobScanningInterval,
                int clusterScanningParallelJobs,
                int maximumReschedulerThreshold,
                /** Semantic key for @ConditionalOnProperty matching - e.g. "default", "multiple" */
                String selectionPolicy,
                /** Semantic key for @ConditionalOnProperty matching - e.g. "exponential-backoff" */
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

            /** Realtime: Dapr delivers status-change-topic to /api/v1/dapr/pubsub/status-change-topic. */
            public record Realtime(boolean enabled) {}

            /** Compute: Dapr for status; job-status-callback-url for job script curl when set. */
            public record Compute(
                    boolean enabled,
                    String emailPublisherId,
                    String realtimePublisherId,
                    Notification notification,
                    String jobStatusCallbackUrl,
                    int clusterCheckTimeWindow,
                    int clusterCheckRepeatTime) {
                public record Notification(String emailIds) {}
            }
        }

        public record Sharing(boolean enabled) {}

        public record Registry(boolean enabled) {}

        public record Research(
                boolean enabled, Grpc grpc, Hub hub, Portal portal, Spring spring, Springdoc springdoc, Openid openid) {
            public record Grpc(
                    String keepaliveTime,
                    String keepaliveTimeout,
                    boolean permitKeepaliveWithoutCalls,
                    long maxInboundMessageSize) {}

            public record Hub(String adminApiKey, int limit, String url) {}

            public record Portal(String devUrl, String url) {}

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
                Spring spring,
                Storage storage,
                Tunnelserver tunnelserver) {
            public record Appinterface(String id) {}

            public record Grpc(long maxInboundMessageSize) {}

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

        public record Fileserver(boolean enabled, Spring spring) {

            public record Spring(Servlet servlet) {
                public record Servlet(Multipart multipart) {
                    public record Multipart(String maxFileSize, String maxRequestSize) {}
                }
            }
        }

        public record Telemetry(boolean enabled) {}

        public record Dbus(boolean enabled) {}
    }
}
