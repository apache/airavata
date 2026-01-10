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
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Spring Boot configuration properties for Airavata server.
 * Maps all properties from airavata.properties to strongly-typed Java objects.
 *
 * <p>This is a pure data structure that mirrors the property file structure exactly.
 * Property binding is handled automatically by Spring Boot's {@code @ConfigurationProperties}.
 *
 * <p>For utility methods (config directory resolution, file loading, etc.), see
 * {@link AiravataConfigUtils}.
 */
@ConfigurationProperties(prefix = "")
public class AiravataServerProperties {

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

        @NestedConfigurationProperty
        public Research research = new Research();

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

            @NestedConfigurationProperty
            public Hikari hikari = new Hikari();

            public static class Hikari {
                public long leakDetectionThreshold = 20000;
                public String poolName = "AppCatalogPool";
            }
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

        public static class Research {
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
        public AuthzCache authzCache = new AuthzCache();
        public Iam iam = new Iam();
        public Vault vault = new Vault();

        public static class Tls {
            public boolean enabled = false;
            public int clientTimeout = 10000;
            public Keystore keystore = new Keystore();
        }

        public static class Keystore {
            public String path = "keystores/airavata.p12";
            public String password = "airavata";
        }

        public static class AuthzCache {
            public boolean enabled = true;
        }

        public static class Iam {
            public String serverUrl;
            public String oauthClientId;
            public String oauthClientSecret;

            @NestedConfigurationProperty
            public Super superAdmin = new Super();

            public static class Super {
                public String username = "admin";
                public String password = "admin";
            }
        }

        public static class Vault {
            public Keystore keystore = new Keystore();

            public static class Keystore {
                public String url;
                public String password;
                public String alias;
            }
        }
    }

    // ==================== Messaging Configuration ====================
    public RabbitMQ rabbitmq = new RabbitMQ();

    public static class RabbitMQ {
        public String brokerUrl = "amqp://guest:guest@localhost:5672/develop";
        public String experimentExchangeName = "experiment_exchange";
        public String experimentLaunchQueueName = "experiment.launch.queue";
        public String processExchangeName = "process_exchange";
        public String statusExchangeName = "status_exchange";
        public String dbEventExchangeName = "dbevent_exchange";
        public boolean durableQueue = false;
        public int prefetchCount = 200;
        public boolean enabled = false;
    }

    public Kafka kafka = new Kafka();

    public static class Kafka {
        public String brokerUrl = "localhost:9092";
    }

    // ==================== Infrastructure Configuration ====================
    public Zookeeper zookeeper = new Zookeeper();

    public static class Zookeeper {
        public Server server = new Server();
        public boolean embedded = false;

        public static class Server {
            public String connection = "localhost:2181";
        }
    }

    // ==================== Helix Configuration ====================
    @NestedConfigurationProperty
    public Helix helix = new Helix();

    public static class Helix {
        public Cluster cluster = new Cluster();
        public Controller controller = new Controller();
        public Participant participant = new Participant();

        public static class Cluster {
            public String name = "AiravataCluster";
        }

        public static class Controller {
            public String name = "AiravataController";
        }

        public static class Participant {
            public String name = "AiravataParticipant";
        }
    }

    // ==================== Flyway Configuration ====================
    @NestedConfigurationProperty
    public Flyway flyway = new Flyway();

    public static class Flyway {
        public boolean enabled = false;
    }

    // ==================== Services Configuration ====================
    public Services services = new Services();

    public static class Services {
        // Service enablement flags - both can be true to run in parallel
        public Thrift thrift = new Thrift(); // Default: enabled
        public Rest rest = new Rest(); // Default: disabled

        public Api api = new Api();
        public Participant participant = new Participant();
        public Controller controller = new Controller();
        public PreWm prewm = new PreWm();
        public PostWm postwm = new PostWm();
        public Parser parser = new Parser();
        public Scheduler scheduler = new Scheduler();
        public Monitor monitor = new Monitor();
        public Sharing sharing = new Sharing();
        public Registry registry = new Registry();
        public Background background = new Background();
        public Research research = new Research();
        public Agent agent = new Agent();
        public Fileserver fileserver = new Fileserver();
        public Dbus dbus = new Dbus();

        public static class Thrift {
            public boolean enabled = true;
            public Server server = new Server();

            public static class Server {
                public int port = 8930;
            }
        }

        public static class Rest {
            public boolean enabled = false;
            public Server server = new Server();

            public static class Server {
                public int port = 8082;
            }
        }

        public static class Research {
            public boolean enabled = true;
            public Grpc grpc = new Grpc();
            public Hub hub = new Hub();
            public Portal portal = new Portal();
            public Server server = new Server();
            public Spring spring = new Spring();
            public Springdoc springdoc = new Springdoc();

            @NestedConfigurationProperty
            public Openid openid = new Openid();

            public static class Openid {
                public String url = "http://localhost:18080/realms/default";
            }

            public static class Grpc {
                public int port = 19908;
                public String keepaliveTime = "30s";
                public String keepaliveTimeout = "5s";
                public boolean permitKeepaliveWithoutCalls = true;
            }

            public static class Hub {
                public String adminApiKey = "JUPYTER_ADMIN_API_KEY";
                public int limit = 10;
                public String url = "http://localhost:20000";
            }

            public static class Portal {
                public String devUrl = "http://localhost:5173";
                public String url = "http://localhost:5173";
            }

            public static class Server {
                public int port = 18889;
            }

            public static class Spring {
                public Servlet servlet = new Servlet();

                public static class Servlet {
                    public Multipart multipart = new Multipart();

                    public static class Multipart {
                        public String maxFileSize = "200MB";
                        public String maxRequestSize = "200MB";
                    }
                }
            }

            public static class Springdoc {
                public ApiDocs apiDocs = new ApiDocs();
                public SwaggerUi swaggerUi = new SwaggerUi();

                public static class ApiDocs {
                    public boolean enabled = true;
                }

                public static class SwaggerUi {
                    public String docExpansion = "none";
                    public String operationsSorter = "alpha";
                    public String path = "/swagger-ui.html";
                    public String tagsSorter = "alpha";
                    public Oauth oauth = new Oauth();

                    public static class Oauth {
                        public String clientId = "data-catalog-portal";
                        public boolean usePkceWithAuthorizationCodeGrant = true;
                    }
                }
            }
        }

        public static class Agent {
            public boolean enabled = true;

            @NestedConfigurationProperty
            public Appinterface appinterface = new Appinterface();

            public Grpc grpc = new Grpc();
            public Server server = new Server();
            public Spring spring = new Spring();
            public Storage storage = new Storage();
            public Tunnelserver tunnelserver = new Tunnelserver();

            public static class Appinterface {
                public String id = "AiravataAgent_f4313e4d-20c2-4bf6-bff1-8aa0f0b0c1d6";
            }

            public static class Grpc {
                public long maxInboundMessageSize = 20971520;
                public int port = 19900;
            }

            public static class Server {
                public int port = 18880;
            }

            public static class Spring {
                public Jpa jpa = new Jpa();
                public Servlet servlet = new Servlet();

                public static class Jpa {
                    public Hibernate hibernate = new Hibernate();
                    public boolean openInView = false;

                    public static class Hibernate {
                        public String ddlAuto = "validate";
                    }
                }

                public static class Servlet {
                    public Multipart multipart = new Multipart();

                    public static class Multipart {
                        public String maxFileSize = "200MB";
                        public String maxRequestSize = "200MB";
                    }
                }
            }

            public static class Storage {
                public String id;
                public String path = "/tmp";
            }

            public static class Tunnelserver {
                public String url = "http://localhost:8000";
                public String host = "localhost";
                public int port = 17000;
                public String token = "airavata";
            }
        }

        public static class Fileserver {
            public boolean enabled = true;
            public Server server = new Server();
            public Spring spring = new Spring();

            public static class Server {
                public int port = 8050;
            }

            public static class Spring {
                public Servlet servlet = new Servlet();

                public static class Servlet {
                    public Multipart multipart = new Multipart();

                    public static class Multipart {
                        public String maxFileSize = "10MB";
                        public String maxRequestSize = "10MB";
                    }
                }
            }
        }

        public Telemetry telemetry = new Telemetry();

        public static class Telemetry {
            public boolean enabled = true;
            public Server server = new Server();

            public static class Server {
                public int port = 9090;
            }
        }

        public static class Api {
            // Note: Api service is controlled by services.thrift.enabled, not a separate services.api.enabled
            // All Thrift services (Profile, Orchestrator, Registry, Vault, Sharing Registry) are now
            // multiplexed on the unified port (services.thrift.server.port). Individual service toggles and ports
            // removed.
            public Vault vault = new Vault();

            public static class Vault {
                public Keystore keystore = new Keystore();
                // Note: enabled and port removed - service is multiplexed on unified port

                public static class Keystore {
                    public String url;
                    public String password;
                    public String alias;
                }
            }
        }

        public static class Dbus {
            public boolean enabled = true;
            public String classpath = "org.apache.airavata.main.DBEventManagerRunner";
        }

        public static class Participant {
            public boolean enabled = true; // services.participant.enabled
        }

        public static class Controller {
            public boolean enabled = true;
        }

        public static class PreWm {
            public boolean enabled = true;
            public boolean loadBalanceClusters = false;
            public String name = "AiravataPreWM";
        }

        public static class PostWm {
            public boolean enabled = true;
            public boolean loadBalanceClusters = false;
            public String name = "AiravataPostWM";
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
            public boolean enabled = false;
            public Interpreter interpreter = new Interpreter();
            public Rescheduler rescheduler = new Rescheduler();
            public double clusterScanningInterval = 1800000;
            public double jobScanningInterval = 1800000;
            public int clusterScanningParallelJobs = 1;
            public int maximumReschedulerThreshold = 5;
            public String computeResourceSelectionPolicyClass =
                    "org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection.MultipleComputeResourcePolicy";
            public String computeResourceReschedulerPolicyClass =
                    "org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ExponentialBackOffReScheduler";

            public static class Interpreter {
                public boolean enabled = true;
            }

            public static class Rescheduler {
                public boolean enabled = true;
            }
        }

        public static class Monitor {
            public Email email = new Email();
            public Realtime realtime = new Realtime();
            public Compute compute = new Compute();

            public static class Email {
                public String address;
                public String folderName = "INBOX";
                public String host;
                public String password;
                public String storeProtocol = "imaps";
                public int period = 10000;
                public int connectionRetryInterval = 30000; // 30 seconds
                public boolean enabled = true;
                public int expiryMins = 60;
            }

            public static class Realtime {
                public boolean enabled = true;
                public String brokerConsumerGroup = "monitor";
                public String brokerTopic = "helix-airavata-mq";
            }

            public static class Compute {
                public String brokerPublisherId = "AiravataMonitorPublisher";
                public String emailPublisherId = "EmailBasedProducer";
                public String realtimePublisherId = "RealtimeProducer";
                public String brokerTopic = "monitoring-data";
                public String brokerConsumerGroup = "MonitoringConsumer";
                public Notification notification = new Notification();
                public String statusPublishEndpoint;
                public String validators;
                public boolean enabled = true; // services.monitor.compute.enabled
                public int clusterCheckTimeWindow =
                        300; // time window to skip cluster checks after submission (seconds)
                public int clusterCheckRepeatTime = 18000; // how often to run cluster status checks (seconds)

                public static class Notification {
                    public String emailIds = "";
                }
            }
        }

        public static class Sharing {
            // Note: enabled and serverPort removed - service is multiplexed on unified port
            // (services.thrift.server.port)
        }

        public static class Registry {
            // Note: enabled and Server.port removed - service is multiplexed on unified port
            // (services.thrift.server.port)
        }

        public static class Background {
            public Controller controller = new Controller();

            public static class Controller {
                public boolean enabled = true;
            }
        }
    }

    // ==================== General Settings Configuration ====================
    @NestedConfigurationProperty
    public Airavata airavata = new Airavata();

    public static class Airavata {
        /**
         * Airavata home directory. Config directory is always {home}/conf.
         * Binds from property: airavata.home
         */
        public String home;

        /**
         * Default gateway ID.
         * Binds from property: airavata.default-gateway
         */
        public String defaultGateway = "default";

        /**
         * Enable validation.
         * Binds from property: airavata.validation-enabled
         */
        public boolean validationEnabled = true;

        public Sharing sharing = new Sharing();
        // NOTE: super-tenant gateway id has been merged into airavata.default-gateway.
        /**
         * Size for in-memory authz cache.
         * Binds from property: airavata.in-memory-cache-size
         */
        public int inMemoryCacheSize = 1000;

        /**
         * Local data location for staging.
         * Binds from property: airavata.local-data-location
         */
        public String localDataLocation = "";

        /**
         * Max archive size (bytes).
         * Binds from property: airavata.max-archive-size
         */
        public long maxArchiveSize = 0L;

        /**
         * Streaming transfer settings.
         * Binds from property: airavata.streaming-transfer.*
         */
        public StreamingTransfer streamingTransfer = new StreamingTransfer();

        public static class Sharing {
            public boolean enabled = true;
        }

        public static class StreamingTransfer {
            public boolean enabled = false;
        }
    }
}
