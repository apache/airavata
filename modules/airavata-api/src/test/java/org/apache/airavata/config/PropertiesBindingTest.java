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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Comprehensive test to verify all properties in AiravataServerProperties
 * are correctly bound from application.properties.
 *
 * Each assertion verifies the property value against the ground truth
 * defined in src/test/resources/application.properties.
 */
@SpringBootTest(classes = PropertiesBindingTest.MinimalConfig.class)
@ActiveProfiles("test")
public class PropertiesBindingTest {

    @Configuration
    // application.properties is auto-loaded by Spring Boot
    @EnableConfigurationProperties(AiravataServerProperties.class)
    static class MinimalConfig {}

    @Autowired
    private AiravataServerProperties properties;

    // ==================== Core Airavata Properties ====================

    @Nested
    @DisplayName("Core Airavata Properties")
    class CoreAiravataProperties {

        @Test
        @DisplayName("airavata.default-gateway = default")
        void testDefaultGateway() {
            assertEquals("default", properties.defaultGateway());
        }

        @Test
        @DisplayName("airavata.in-memory-cache-size = 1000")
        void testInMemoryCacheSize() {
            assertEquals(1000, properties.inMemoryCacheSize());
        }

        @Test
        @DisplayName("airavata.local-data-location = /tmp/airavata")
        void testLocalDataLocation() {
            assertEquals("/tmp/airavata", properties.localDataLocation());
        }

        @Test
        @DisplayName("airavata.max-archive-size = 1073741824")
        void testMaxArchiveSize() {
            assertEquals(1073741824L, properties.maxArchiveSize());
        }

        @Test
        @DisplayName("airavata.sharing.enabled = true")
        void testSharingEnabled() {
            assertTrue(properties.sharing().enabled());
        }

        @Test
        @DisplayName("airavata.streaming-transfer.enabled = false")
        void testStreamingTransferEnabled() {
            assertFalse(properties.streamingTransfer().enabled());
        }

        @Test
        @DisplayName("airavata.validation-enabled = true")
        void testValidationEnabled() {
            assertTrue(properties.validationEnabled());
        }
    }

    // ==================== Database Properties ====================
    // Note: Database properties have been consolidated to standard Spring Boot
    // properties (spring.datasource.*) instead of per-service databases.
    // The old airavata.database.* properties are no longer used.

    // ==================== Security Properties ====================

    @Nested
    @DisplayName("Security Properties")
    class SecurityProperties {

        @Test
        @DisplayName("security.authentication.enabled = true")
        void testAuthenticationEnabled() {
            assertTrue(properties.security().authentication().enabled());
        }

        @Test
        @DisplayName("security.authz-cache.enabled = true")
        void testAuthzCacheEnabled() {
            assertTrue(properties.security().authzCache().enabled());
        }

        @Test
        @DisplayName("security.iam properties")
        void testIamProperties() {
            var iam = properties.security().iam();
            // IAM is enabled for tests with Keycloak testcontainer
            assertTrue(iam.enabled());
            assertEquals("http://localhost:18080", iam.serverUrl());
            assertEquals("pga", iam.oauthClientId());
            assertEquals("m36BXQIxX3j3VILadeHMK5IvbOeRlCCc", iam.oauthClientSecret());
            assertEquals("admin", iam.superAdmin().username());
            assertEquals("admin", iam.superAdmin().password());
        }

        @Test
        @DisplayName("security.tls properties")
        void testTlsProperties() {
            var tls = properties.security().tls();
            assertFalse(tls.enabled());
            assertEquals(10000, tls.clientTimeout());
            assertEquals("conf/keystores/airavata.p12", tls.keystore().path());
            assertEquals("airavata", tls.keystore().password());
        }

        @Test
        @DisplayName("security.vault.keystore properties")
        void testSecurityVaultKeystore() {
            var keystore = properties.security().vault().keystore();
            assertEquals("conf/keystores/airavata.sym.p12", keystore.url());
            assertEquals("airavata", keystore.password());
            assertEquals("airavata", keystore.alias());
        }
    }

    // ==================== Messaging Properties ====================

    @Nested
    @DisplayName("Messaging Properties")
    class MessagingProperties {

        @Test
        @DisplayName("flyway.enabled = false")
        void testFlywayEnabled() {
            assertFalse(properties.flyway().enabled());
        }
    }

    // ==================== Services Properties ====================

    @Nested
    @DisplayName("Services Properties")
    class ServicesProperties {

        @Test
        @DisplayName("services.agent properties")
        void testAgentService() {
            var agent = properties.services().agent();
            assertTrue(agent.enabled());
            assertEquals(
                    "AiravataAgent_f4313e4d-20c2-4bf6-bff1-8aa0f0b0c1d6",
                    agent.appinterface().id());
            assertEquals(20971520L, agent.grpc().maxInboundMessageSize());
            assertEquals(19900, agent.grpc().port());
            assertEquals(18880, agent.server().port());
            assertEquals("validate", agent.spring().jpa().hibernate().ddlAuto());
            assertFalse(agent.spring().jpa().openInView());
            assertEquals("200MB", agent.spring().servlet().multipart().maxFileSize());
            assertEquals("200MB", agent.spring().servlet().multipart().maxRequestSize());
            assertEquals(
                    "localhost_77116e91-f042-4d3a-ab9c-3e7b4ebcd5bd",
                    agent.storage().id());
            assertEquals("/tmp", agent.storage().path());
            assertEquals("http://localhost:8000", agent.tunnelserver().url());
            assertEquals("localhost", agent.tunnelserver().host());
            assertEquals(17000, agent.tunnelserver().port());
            assertEquals("airavata", agent.tunnelserver().token());
        }

        @Test
        @DisplayName("services.api.vault.keystore properties")
        void testApiVaultKeystore() {
            var keystore = properties.services().api().vault().keystore();
            assertEquals("conf/keystores/airavata.sym.p12", keystore.url());
            assertEquals("airavata", keystore.password());
            assertEquals("airavata", keystore.alias());
        }

        @Test
        @DisplayName("services.background.controller.enabled = false")
        void testBackgroundController() {
            assertFalse(properties.services().background().controller().enabled());
        }

        @Test
        @DisplayName("services.controller.enabled = true")
        void testControllerEnabled() {
            assertTrue(properties.services().controller().enabled());
        }

        @Test
        @DisplayName("services.dbus properties")
        void testDbusService() {
            var dbus = properties.services().dbus();
            assertFalse(dbus.enabled());
            assertEquals("", dbus.classpath());
        }

        @Test
        @DisplayName("services.fileserver properties")
        void testFileserverService() {
            var fileserver = properties.services().fileserver();
            assertTrue(fileserver.enabled());
            assertEquals(8050, fileserver.server().port());
            assertEquals("10MB", fileserver.spring().servlet().multipart().maxFileSize());
            assertEquals("10MB", fileserver.spring().servlet().multipart().maxRequestSize());
        }

        @Test
        @DisplayName("services.monitor.compute properties")
        void testComputeMonitor() {
            var compute = properties.services().monitor().compute();
            assertTrue(compute.enabled());
            assertEquals(18000, compute.clusterCheckRepeatTime());
            assertEquals(300, compute.clusterCheckTimeWindow());
            assertEquals("EmailBasedProducer", compute.emailPublisherId());
            assertEquals("", compute.jobStatusCallbackUrl());
            assertEquals("", compute.notification().emailIds());
            assertEquals("RealtimeProducer", compute.realtimePublisherId());
            assertEquals("BatchQueueValidator,ExperimentStatusValidator", compute.validators());
        }

        @Test
        @DisplayName("services.monitor.email properties")
        void testEmailMonitor() {
            var email = properties.services().monitor().email();
            assertFalse(email.enabled());
            assertEquals("monitoring.airavata@gmail.com", email.address());
            assertEquals(30000, email.connectionRetryInterval());
            assertEquals(60, email.expiryMins());
            assertEquals("INBOX", email.folderName());
            assertEquals("imap.gmail.com", email.host());
            assertEquals("123456", email.password());
            assertEquals(10000, email.period());
            assertEquals("imaps", email.storeProtocol());
        }

        @Test
        @DisplayName("services.monitor.realtime properties")
        void testRealtimeMonitor() {
            var realtime = properties.services().monitor().realtime();
            assertTrue(realtime.enabled());
        }

        @Test
        @DisplayName("services.parser properties")
        void testParserService() {
            var parser = properties.services().parser();
            assertFalse(parser.enabled());
            assertTrue(parser.deleteContainer());
            assertEquals("", parser.enabledGateways());
            assertFalse(parser.loadBalanceClusters());
            assertEquals(3600.0, parser.scanningInterval());
            assertEquals(1, parser.scanningParallelJobs());
            assertEquals("CHANGE_ME", parser.storageResourceId());
            assertEquals(5, parser.timeStepSeconds());
        }

        @Test
        @DisplayName("services.participant.enabled = true")
        void testParticipantEnabled() {
            assertTrue(properties.services().participant().enabled());
        }

        @Test
        @DisplayName("services.postwm properties")
        void testPostWmService() {
            var postwm = properties.services().postwm();
            assertTrue(postwm.enabled());
            assertFalse(postwm.loadBalanceClusters());
        }

        @Test
        @DisplayName("services.prewm properties")
        void testPreWmService() {
            var prewm = properties.services().prewm();
            assertTrue(prewm.enabled());
            assertFalse(prewm.loadBalanceClusters());
        }

        @Test
        @DisplayName("services.research properties")
        void testResearchService() {
            var research = properties.services().research();
            assertFalse(research.enabled());
            assertEquals(19908, research.grpc().port());
            assertEquals("30s", research.grpc().keepaliveTime());
            assertEquals("5s", research.grpc().keepaliveTimeout());
            assertTrue(research.grpc().permitKeepaliveWithoutCalls());
            assertEquals("JUPYTER_ADMIN_API_KEY", research.hub().adminApiKey());
            assertEquals(10, research.hub().limit());
            assertEquals("http://localhost:20000", research.hub().url());
            assertEquals(
                    "http://localhost:18080/realms/default", research.openid().url());
            assertEquals("http://localhost:5173", research.portal().devUrl());
            assertEquals("http://localhost:5173", research.portal().url());
            assertEquals(18889, research.server().port());
            assertEquals("200MB", research.spring().servlet().multipart().maxFileSize());
            assertEquals("200MB", research.spring().servlet().multipart().maxRequestSize());
            assertTrue(research.springdoc().apiDocs().enabled());
            assertEquals("none", research.springdoc().swaggerUi().docExpansion());
            assertEquals(
                    "data-catalog-portal",
                    research.springdoc().swaggerUi().oauth().clientId());
            assertTrue(research.springdoc().swaggerUi().oauth().usePkceWithAuthorizationCodeGrant());
            assertEquals("alpha", research.springdoc().swaggerUi().operationsSorter());
            assertEquals("/swagger-ui.html", research.springdoc().swaggerUi().path());
            assertEquals("alpha", research.springdoc().swaggerUi().tagsSorter());
        }

        @Test
        @DisplayName("services.rest properties")
        void testRestService() {
            var rest = properties.services().rest();
            assertFalse(rest.enabled());
            assertEquals(8082, rest.server().port());
        }

        @Test
        @DisplayName("services.scheduler properties")
        void testSchedulerService() {
            var scheduler = properties.services().scheduler();
            assertFalse(scheduler.enabled());
            assertEquals(1800000.0, scheduler.clusterScanningInterval());
            assertEquals(1, scheduler.clusterScanningParallelJobs());
            // Interpreter and rescheduler are disabled in test properties
            assertFalse(scheduler.interpreter().enabled());
            assertEquals(1800000.0, scheduler.jobScanningInterval());
            assertEquals(5, scheduler.maximumReschedulerThreshold());
            assertFalse(scheduler.rescheduler().enabled());
            assertEquals("DefaultComputeResourceSelectionPolicy", scheduler.selectionPolicy());
            assertEquals("ExponentialBackOffReScheduler", scheduler.reschedulerPolicy());
        }

        @Test
        @DisplayName("services.telemetry properties")
        void testTelemetryService() {
            var telemetry = properties.services().telemetry();
            assertTrue(telemetry.enabled());
            assertEquals(9090, telemetry.server().port());
        }

        @Test
        @DisplayName("services.thrift properties")
        void testThriftService() {
            var thrift = properties.services().thrift();
            assertTrue(thrift.enabled());
            assertEquals(8930, thrift.server().port());
        }

        @Test
        @DisplayName("services.sharing properties")
        void testSharingService() {
            assertTrue(properties.services().sharing().enabled());
        }

        @Test
        @DisplayName("services.registry properties")
        void testRegistryService() {
            assertTrue(properties.services().registry().enabled());
        }
    }
}
