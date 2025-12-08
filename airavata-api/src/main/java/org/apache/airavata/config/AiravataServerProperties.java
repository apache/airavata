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

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.env.Environment;

/**
 * Spring Boot configuration properties for Airavata server.
 * Maps all properties from airavata.properties to strongly-typed Java objects.
 */
@ConfigurationProperties(prefix = "")
public class AiravataServerProperties {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServerProperties.class);

    @Autowired
    private Environment environment;

    // ==================== Core Configuration ====================
    public String airavataConfigDir = ".";

    @PostConstruct
    public void bindProperties() {
        logger.info("Binding properties to AiravataServerProperties");

        // Manually bind database properties from environment
        if (database != null && environment != null) {
            // Bind registry
            if (database.registry != null) {
                database.registry.url = getProperty("database.registry.url", database.registry.url);
                database.registry.user = getProperty("database.registry.user", database.registry.user);
                database.registry.password = getProperty("database.registry.password", database.registry.password);
                database.registry.driver = getProperty("database.registry.driver", database.registry.driver);
                database.registry.validationQuery =
                        getProperty("database.registry.validation-query", database.registry.validationQuery);
            }

            // Bind catalog
            if (database.catalog != null) {
                database.catalog.url = getProperty("database.catalog.url", database.catalog.url);
                database.catalog.user = getProperty("database.catalog.user", database.catalog.user);
                database.catalog.password = getProperty("database.catalog.password", database.catalog.password);
                database.catalog.driver = getProperty("database.catalog.driver", database.catalog.driver);
                database.catalog.validationQuery =
                        getProperty("database.catalog.validation-query", database.catalog.validationQuery);
            }

            // Bind vault
            if (database.vault != null) {
                database.vault.url = getProperty("database.vault.url", database.vault.url);
                database.vault.user = getProperty("database.vault.user", database.vault.user);
                database.vault.password = getProperty("database.vault.password", database.vault.password);
                database.vault.driver = getProperty("database.vault.driver", database.vault.driver);
                database.vault.validationQuery =
                        getProperty("database.vault.validation-query", database.vault.validationQuery);
            }

            // Bind profile
            if (database.profile != null) {
                database.profile.url = getProperty("database.profile.url", database.profile.url);
                database.profile.user = getProperty("database.profile.user", database.profile.user);
                database.profile.password = getProperty("database.profile.password", database.profile.password);
                database.profile.driver = getProperty("database.profile.driver", database.profile.driver);
                database.profile.validationQuery =
                        getProperty("database.profile.validation-query", database.profile.validationQuery);
            }

            // Bind sharing
            if (database.sharing != null) {
                database.sharing.url = getProperty("database.sharing.url", database.sharing.url);
                database.sharing.user = getProperty("database.sharing.user", database.sharing.user);
                database.sharing.password = getProperty("database.sharing.password", database.sharing.password);
                database.sharing.driver = getProperty("database.sharing.driver", database.sharing.driver);
                database.sharing.validationQuery =
                        getProperty("database.sharing.validation-query", database.sharing.validationQuery);
            }

            // Bind replica
            if (database.replica != null) {
                database.replica.url = getProperty("database.replica.url", database.replica.url);
                database.replica.user = getProperty("database.replica.user", database.replica.user);
                database.replica.password = getProperty("database.replica.password", database.replica.password);
                database.replica.driver = getProperty("database.replica.driver", database.replica.driver);
                database.replica.validationQuery =
                        getProperty("database.replica.validation-query", database.replica.validationQuery);
            }

            // Bind workflow
            if (database.workflow != null) {
                database.workflow.url = getProperty("database.workflow.url", database.workflow.url);
                database.workflow.user = getProperty("database.workflow.user", database.workflow.user);
                database.workflow.password = getProperty("database.workflow.password", database.workflow.password);
                database.workflow.driver = getProperty("database.workflow.driver", database.workflow.driver);
                database.workflow.validationQuery =
                        getProperty("database.workflow.validation-query", database.workflow.validationQuery);
            }

            // Bind database-level validation query
            database.validationQuery = getProperty("database.validation-query", database.validationQuery);
        }

        // Log configuration
        if (database != null && database.registry != null) {
            logger.info(
                    "Registry URL after binding: {}", database.registry.url != null ? database.registry.url : "NULL");
        }
    }

    private String getProperty(String key, String defaultValue) {
        String value = environment.getProperty(key);
        return value != null ? value : defaultValue;
    }

    // ==================== Database Configuration ====================
    @NestedConfigurationProperty
    public Database database = new Database();

    public static class Database {
        @NestedConfigurationProperty
        public Registry registry = new Registry();

        @NestedConfigurationProperty
        public Catalog catalog = new Catalog();

        @NestedConfigurationProperty
        public Vault vault = new Vault();

        @NestedConfigurationProperty
        public Profile profile = new Profile();

        @NestedConfigurationProperty
        public Sharing sharing = new Sharing();

        @NestedConfigurationProperty
        public Replica replica = new Replica();

        @NestedConfigurationProperty
        public Workflow workflow = new Workflow();

        public String validationQuery = "SELECT 1";

        public static class Registry {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }

        public static class Catalog {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }

        public static class Vault {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }

        public static class Profile {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }

        public static class Sharing {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }

        public static class Replica {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }

        public static class Workflow {
            public String driver = "org.mariadb.jdbc.Driver";
            public String url;
            public String user;
            public String password;
            public String validationQuery = "SELECT 1";
        }
    }

    // ==================== Security Configuration ====================
    public Security security = new Security();

    public static class Security {
        public Tls tls = new Tls();
        public Keystore keystore = new Keystore();
        public AuthzCache authzCache = new AuthzCache();
        public Iam iam = new Iam();

        public static class Tls {
            public boolean enabled = false;
            public int clientTimeout = 10000;
        }

        public static class Keystore {
            public String path = "keystores/airavata.p12";
            public String password = "airavata";
        }

        public static class AuthzCache {
            public boolean enabled = true;
            public String classpath = "org.apache.airavata.security.authzcache.DefaultAuthzCacheManager";
        }

        public static class Iam {
            public String classpath = "org.apache.airavata.security.KeyCloakSecurityManager";
            public String serverUrl;
            public String superAdminUsername = "admin";
            public String superAdminPassword = "admin";
            public String oauthClientId;
            public String oauthClientSecret;
        }
    }

    // ==================== Messaging Configuration ====================
    public RabbitMQ rabbitmq = new RabbitMQ();

    public static class RabbitMQ {
        public String brokerUrl = "amqp://guest:guest@airavata.host:5672/develop";
        public String experimentExchangeName = "experiment_exchange";
        public String experimentLaunchQueueName = "experiment.launch.queue";
        public String processExchangeName = "process_exchange";
        public String statusExchangeName = "status_exchange";
        public boolean durableQueue = false;
        public int prefetchCount = 200;
    }

    public Kafka kafka = new Kafka();

    public static class Kafka {
        public String brokerUrl = "airavata.host:9092";
    }

    // ==================== Infrastructure Configuration ====================
    public Zookeeper zookeeper = new Zookeeper();

    public static class Zookeeper {
        public String serverConnection = "airavata.host:2181";
        public boolean embedded = false;
    }

    public Helix helix = new Helix();

    public static class Helix {
        public String clusterName = "AiravataCluster";
        public String controllerName = "AiravataController";
        public String participantName = "AiravataParticipant";
        public Controller controller = new Controller();
        public Participant participant = new Participant();

        public static class Controller {
            public boolean enabled = true;
        }

        public static class Participant {
            public boolean enabled = true;
        }
    }

    // ==================== Services Configuration ====================
    public Services services = new Services();

    public static class Services {
        public Api api = new Api();
        public Orchestrator orchestrator = new Orchestrator();
        public Participant participant = new Participant();
        public Controller controller = new Controller();
        public PreWm prewm = new PreWm();
        public PostWm postwm = new PostWm();
        public Parser parser = new Parser();
        public Scheduler scheduler = new Scheduler();
        public Monitor monitor = new Monitor();
        public Sharing sharing = new Sharing();
        public Registry registry = new Registry();
        public Default default_ = new Default();
        public DbEvent dbevent = new DbEvent();
        public Vault vault = new Vault();

        public static class Api {
            public int port = 8930;
            public int minThreads = 50;
            public String classpath = "org.apache.airavata.api.thrift.server.AiravataServiceServer";
            public Profile profile = new Profile();

            public static class Profile {
                public Server server = new Server();
                public String classpath = "org.apache.airavata.api.thrift.server.ProfileServiceServer";

                public static class Server {
                    public int port = 8962;
                }
            }
        }

        public static class Orchestrator {
            public int serverMinThreads = 50;
            public int serverPort = 8940;
            public String classpath = "org.apache.airavata.api.thrift.server.OrchestratorServiceServer";
        }

        public static class Participant {
            // Participant-specific configuration
        }

        public static class Controller {
            // Controller-specific configuration
        }

        public static class PreWm {
            public boolean enabled = true;
            public boolean loadBalanceClusters = false;
            public Monitoring monitoring = new Monitoring();
            public String name = "AiravataPreWM";

            public static class Monitoring {
                public boolean enabled = true;
                public int port = 9093;
            }
        }

        public static class PostWm {
            public boolean enabled = true;
            public boolean loadBalanceClusters = false;
            public Monitoring monitoring = new Monitoring();
            public String name = "AiravataPostWM";

            public static class Monitoring {
                public boolean enabled = true;
                public int port = 9094;
            }
        }

        public static class Parser {
            public boolean enabled = false;
            public boolean loadBalanceClusters = false;
            public String name = "AiravataParserWM";
            public String brokerConsumerGroup = "ParsingConsumer";
            public String topic = "parsing-data";
            public String storageResourceId = "CHANGE_ME";
            public boolean deleteContainer = true;
            public double scanningInterval = 3600;
            public int scanningParallelJobs = 1;
            public String enabledGateways = "";
            public int timeStepSeconds = 5;
        }

        public static class Scheduler {
            public String classpath = "org.apache.airavata.orchestrator.core.schedule.DefaultHostScheduler";
            public boolean enabled = false;
            public String gateway = "";
            public String groupResourceProfile = "";
            public String username = "";
            public double clusterScanningInterval = 1800000;
            public double jobScanningInterval = 1800000;
            public int clusterScanningParallelJobs = 1;
            public int maximumReschedulerThreshold = 5;
            public String computeResourceSelectionPolicyClass =
                    "org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection.MultipleComputeResourcePolicy";
            public String computeResourceReschedulerPolicyClass =
                    "org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ExponentialBackOffReScheduler";
        }

        public static class Monitor {
            public Email email = new Email();
            public Realtime realtime = new Realtime();
            public Cluster cluster = new Cluster();
            public Job job = new Job();

            public static class Email {
                public String address;
                public String folderName = "INBOX";
                public String host;
                public String password;
                public String storeProtocol = "imaps";
                public int period = 10000;
                public boolean monitorEnabled = true;
                public int expiryMins = 60;
            }

            public static class Realtime {
                public boolean enabled = false;
                public boolean monitorEnabled = true;
                public String brokerConsumerGroup = "monitor";
                public String brokerTopic = "helix-airavata-mq";
            }

            public static class Cluster {
                public boolean enable = false;
                public int repeatTime = 18000;
            }

            public static class Job {
                public String brokerPublisherId = "AiravataMonitorPublisher";
                public String emailPublisherId = "EmailBasedProducer";
                public String realtimePublisherId = "RealtimeProducer";
                public String brokerTopic = "monitoring-data";
                public String brokerConsumerGroup = "MonitoringConsumer";
                public Notification notification = new Notification();
                public String statusPublishEndpoint;
                public String validators;

                public static class Notification {
                    public String emailIds = "";
                    public boolean enable = true;
                }
            }
        }

        public static class Sharing {
            public int serverPort = 7878;
            public String classpath = "org.apache.airavata.api.thrift.server.SharingRegistryServer";
            public boolean enabled = true;
        }

        public static class Registry {
            public String classpath = "org.apache.airavata.api.thrift.server.RegistryServiceServer";
            public Server server = new Server();

            public static class Server {
                public int minThreads = 50;
                public int port = 8970;
            }
        }

        public static class Default {
            public String gateway = "default";
            public String password;
            public String user;
        }

        public static class DbEvent {
            public String classpath = "org.apache.airavata.main.DBEventManagerRunner";
        }

        public static class Vault {
            public Server server = new Server();
            public String classpath = "org.apache.airavata.api.thrift.server.CredentialServiceServer";
            public Keystore keystore = new Keystore();

            public static class Server {
                public int port = 8960;
            }

            public static class Keystore {
                public String url;
                public String password;
                public String alias;
            }
        }
    }

    // ==================== General Settings Configuration ====================
    public Airavata airavata = new Airavata();

    public static class Airavata {
        public String localDataLocation = "/tmp";
        public int maxArchiveSize = 1000;
        public int inMemoryCacheSize = 1000;
        public boolean enableValidation = true;
        public boolean enableStreamingTransfer = false;
        public String superTenantGatewayId = "default";
        public boolean thriftClientPoolAbandonedRemovalEnabled = true;
        public boolean thriftClientPoolAbandonedRemovalLogged = false;
    }
}
