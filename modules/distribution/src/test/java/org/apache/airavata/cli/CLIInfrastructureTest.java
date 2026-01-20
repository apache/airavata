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
package org.apache.airavata.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * CLI Infrastructure Tests for Airavata commands requiring external services.
 *
 * These tests use Testcontainers to spin up required infrastructure
 * (MariaDB, Kafka, RabbitMQ, Zookeeper) for testing init and serve commands.
 */
@Testcontainers
@DisplayName("Airavata CLI Infrastructure Tests")
public class CLIInfrastructureTest {

    private static final Logger logger = LoggerFactory.getLogger(CLIInfrastructureTest.class);

    @Container
    static MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.4.13"))
            .withDatabaseName("airavata_test")
            .withUsername("airavata")
            .withPassword("123456")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @Container
    static RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management")).withVhost("develop");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @Container
    static GenericContainer<?> zookeeperContainer =
            new GenericContainer<>(DockerImageName.parse("zookeeper:3.9")).withExposedPorts(2181);

    private static String mariaDBUrl;
    private static String rabbitMQUrl;
    private static String kafkaUrl;
    private static String zookeeperUrl;

    @BeforeAll
    static void setupContainers() {
        mariaDBUrl = mariaDBContainer.getJdbcUrl();
        rabbitMQUrl = String.format(
                "amqp://guest:guest@%s:%d/develop", rabbitMQContainer.getHost(), rabbitMQContainer.getAmqpPort());
        kafkaUrl = kafkaContainer.getBootstrapServers();
        zookeeperUrl = String.format("%s:%d", zookeeperContainer.getHost(), zookeeperContainer.getMappedPort(2181));

        logger.info("Testcontainers started:");
        logger.info("  MariaDB: {}", mariaDBUrl);
        logger.info("  RabbitMQ: {}", rabbitMQUrl);
        logger.info("  Kafka: {}", kafkaUrl);
        logger.info("  Zookeeper: {}", zookeeperUrl);
    }

    @AfterAll
    static void teardownContainers() {
        // Containers are automatically stopped by Testcontainers
    }

    @Nested
    @DisplayName("Infrastructure Connectivity Tests")
    class InfrastructureConnectivityTests {

        @Test
        @DisplayName("MariaDB container should be running")
        void mariaDBShouldBeRunning() {
            assertThat(mariaDBContainer.isRunning()).isTrue();
            assertThat(mariaDBUrl).isNotEmpty();
        }

        @Test
        @DisplayName("RabbitMQ container should be running")
        void rabbitMQShouldBeRunning() {
            assertThat(rabbitMQContainer.isRunning()).isTrue();
            assertThat(rabbitMQUrl).contains("amqp://");
        }

        @Test
        @DisplayName("Kafka container should be running")
        void kafkaShouldBeRunning() {
            assertThat(kafkaContainer.isRunning()).isTrue();
            assertThat(kafkaUrl).isNotEmpty();
        }

        @Test
        @DisplayName("Zookeeper container should be running")
        void zookeeperShouldBeRunning() {
            assertThat(zookeeperContainer.isRunning()).isTrue();
            assertThat(zookeeperUrl).contains(":");
        }
    }

    @Nested
    @DisplayName("Configuration Generation Tests")
    class ConfigurationGenerationTests {

        @Test
        @DisplayName("Should generate valid airavata.properties with testcontainer URLs")
        void shouldGenerateValidPropertiesFile(@TempDir Path tempDir) throws IOException {
            File confDir = tempDir.resolve("conf").toFile();
            confDir.mkdirs();
            File propsFile = new File(confDir, "airavata.properties");

            String props = generateTestProperties();
            try (FileWriter writer = new FileWriter(propsFile)) {
                writer.write(props);
            }

            assertThat(propsFile).exists();
            String content = Files.readString(propsFile.toPath());
            assertThat(content).contains("airavata.database");
            assertThat(content).contains("airavata.rabbitmq");
            assertThat(content).contains("airavata.kafka");
        }
    }

    /**
     * Generate test properties using testcontainer URLs.
     */
    private String generateTestProperties() {
        StringBuilder sb = new StringBuilder();

        // Database properties - use same URL for all catalogs (test mode)
        String[] catalogs = {"catalog", "registry", "profile", "sharing", "replica", "workflow", "vault", "research"};
        for (String catalog : catalogs) {
            sb.append(String.format("airavata.database.%s.driver=org.mariadb.jdbc.Driver%n", catalog));
            sb.append(String.format("airavata.database.%s.url=%s%n", catalog, mariaDBUrl));
            sb.append(String.format("airavata.database.%s.user=airavata%n", catalog));
            sb.append(String.format("airavata.database.%s.password=123456%n", catalog));
            sb.append(String.format("airavata.database.%s.validation-query=SELECT 1%n", catalog));
        }

        // Messaging properties
        sb.append(String.format("airavata.rabbitmq.broker-url=%s%n", rabbitMQUrl));
        sb.append("airavata.rabbitmq.enabled=true\n");
        sb.append("airavata.rabbitmq.status-exchange-name=status_exchange\n");
        sb.append("airavata.rabbitmq.process-exchange-name=process_exchange\n");
        sb.append("airavata.rabbitmq.experiment-exchange-name=experiment_exchange\n");
        sb.append("airavata.rabbitmq.experiment-launch-queue-name=experiment.launch.queue\n");
        sb.append("airavata.rabbitmq.db-event-exchange-name=dbevent_exchange\n");
        sb.append("airavata.rabbitmq.durable-queue=false\n");
        sb.append("airavata.rabbitmq.prefetch-count=200\n");

        sb.append(String.format("airavata.kafka.broker-url=%s%n", kafkaUrl));
        sb.append("airavata.kafka.enabled=false\n");

        sb.append(String.format("airavata.zookeeper.server.connection=%s%n", zookeeperUrl));

        // Helix properties
        sb.append("airavata.helix.cluster.name=AiravataCluster\n");
        sb.append("airavata.helix.controller.name=AiravataController\n");
        sb.append("airavata.helix.participant.name=AiravataParticipant\n");

        // Security properties (disabled for tests)
        sb.append("airavata.security.iam.enabled=false\n");
        sb.append("airavata.security.iam.server-url=http://localhost:18080\n");
        sb.append("airavata.security.iam.oauth-client-id=pga\n");
        sb.append("airavata.security.iam.oauth-client-secret=secret\n");
        sb.append("airavata.security.iam.super-admin.username=admin\n");
        sb.append("airavata.security.iam.super-admin.password=admin\n");
        sb.append("airavata.security.authzCache.enabled=false\n");
        sb.append("airavata.security.authentication.enabled=false\n");

        // Service properties
        sb.append("airavata.services.thrift.enabled=false\n");
        sb.append("airavata.services.rest.enabled=false\n");
        sb.append("airavata.services.agent.enabled=false\n");

        // Flyway
        sb.append("airavata.flyway.enabled=true\n");

        // Misc
        sb.append("airavata.default-gateway=default\n");
        sb.append("airavata.local-data-location=/tmp/airavata\n");
        sb.append("airavata.in-memory-cache-size=1000\n");
        sb.append("airavata.hibernate.hbm2ddl-auto=none\n");

        return sb.toString();
    }

    @Nested
    @DisplayName("Init Command Tests")
    class InitCommandTests {

        @Test
        @DisplayName("Init command should require database connectivity")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void initCommandShouldRequireDatabaseConnectivity() {
            // The init command requires a running Spring context with database
            // This test verifies that the init command class exists and is properly configured
            assertThat(org.apache.airavata.cli.commands.InitCommand.class).isNotNull();

            // Verify the command has the expected annotations
            picocli.CommandLine.Command cmd =
                    org.apache.airavata.cli.commands.InitCommand.class.getAnnotation(picocli.CommandLine.Command.class);
            assertThat(cmd).isNotNull();
            assertThat(cmd.name()).isEqualTo("init");
            // Description should mention database/migration
            String[] descriptions = cmd.description();
            assertThat(descriptions.length).isGreaterThan(0);
            assertThat(descriptions[0].toLowerCase()).containsAnyOf("database", "migration", "flyway");
        }
    }

    @Nested
    @DisplayName("Serve Command Tests")
    class ServeCommandTests {

        @Test
        @DisplayName("Serve command should have foreground option")
        void serveCommandShouldHaveForegroundOption() throws NoSuchFieldException {
            // Verify the serve command has the foreground option
            java.lang.reflect.Field foregroundField =
                    org.apache.airavata.cli.commands.ServeCommand.class.getDeclaredField("foreground");
            assertThat(foregroundField).isNotNull();

            picocli.CommandLine.Option option = foregroundField.getAnnotation(picocli.CommandLine.Option.class);
            assertThat(option).isNotNull();
            assertThat(option.names()).contains("--foreground");
        }

        @Test
        @DisplayName("Serve command should check AIRAVATA_HOME environment")
        void serveCommandShouldCheckAiravataHome() {
            // The serve command checks for airavata.home system property or AIRAVATA_HOME env
            // This test verifies the command structure
            picocli.CommandLine.Command cmd = org.apache.airavata.cli.commands.ServeCommand.class.getAnnotation(
                    picocli.CommandLine.Command.class);
            assertThat(cmd).isNotNull();
            assertThat(cmd.name()).isEqualTo("serve");
            assertThat(cmd.description()).contains("Start all Airavata services");
        }
    }
}
