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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Spring Boot configuration properties for Airavata server.
 * Maps all properties from airavata.properties to strongly-typed Java objects.
 *
 * <p>All configuration files are loaded from {airavataHome}/conf:
 * <ul>
 *   <li>airavata.properties - Unified configuration (aggregated from all modules)</li>
 *   <li>logback.xml - Logging configuration</li>
 *   <li>email-config.yml - Email monitoring configuration</li>
 *   <li>templates/ - Job submission templates</li>
 *   <li>keystores/ - Security keystores</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "")
public class AiravataServerProperties {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServerProperties.class);
    private static final String SERVER_PROPERTIES = "airavata.properties";
    private static volatile Properties cachedAiravataProperties;
    private static volatile AiravataServerProperties instance;

    private Environment environment;

    public AiravataServerProperties() {
        // No-arg constructor required for @ConfigurationProperties
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    // ==================== Core Configuration ====================
    /**
     * Airavata home directory. Config directory is always {airavataHome}/conf.
     *
     * <p>Resolution precedence (checked in order):
     * <ol>
     *   <li>System property -Dairavata.home=XXX (highest priority, can override property file)</li>
     *   <li>airavata.home property in airavata.properties (if set and non-empty)</li>
     *   <li>Resources root (IDE mode: modules/distribution/src/main/resources)</li>
     * </ol>
     *
     * <p>In IDE mode, if neither system property nor property file value is set, the resources root
     * is automatically resolved and used as airavataHome.
     * The {airavataHome}/conf directory must exist and be a valid directory.
     *
     * <p>Static utility methods are available on this class:
     * {@link #getConfigDir()}, {@link #loadFile(String)}, {@link #getSetting(String, String)}.
     */
    public String airavataHome;

    @PostConstruct
    public void bindProperties() {
        logger.info("Binding properties to AiravataServerProperties");

        // Resolution precedence:
        // 1. System property -Dairavata.home=XXX (highest priority)
        // 2. Property from airavata.properties file (via Spring binding)
        // 3. Resources root (IDE mode)

        // Check system property first (can override property file)
        String systemPropertyHome = System.getProperty("airavata.home");
        if (systemPropertyHome != null && !systemPropertyHome.isEmpty()) {
            this.airavataHome = systemPropertyHome;
            logger.debug("Using airavata.home from system property: {}", this.airavataHome);
        }

        // IDE mode: If still not set, try to resolve resources root
        if (this.airavataHome == null || this.airavataHome.isEmpty()) {
            String resourcesRoot = resolveResourcesRoot();
            if (resourcesRoot != null) {
                this.airavataHome = resourcesRoot;
                logger.info("IDE mode detected: using resources root as airavata.home: {}", this.airavataHome);
            }
        }

        // Validate that airavataHome is set and {airavataHome}/conf exists
        if (this.airavataHome == null || this.airavataHome.isEmpty()) {
            throw new IllegalStateException(
                    "airavata.home is not set. Please set -Dairavata.home=XXX or set airavata.home in airavata.properties.");
        }

        java.io.File confDir = new java.io.File(this.airavataHome, "conf");
        if (!confDir.exists() || !confDir.isDirectory()) {
            throw new IllegalStateException("Config directory does not exist at " + confDir.getAbsolutePath()
                    + ". Please ensure airavata.home points to the correct Airavata installation directory.");
        }

        logger.info("Airavata home resolved: {}, config directory: {}", this.airavataHome, confDir.getAbsolutePath());

        // Manually bind properties from Environment if they weren't bound automatically
        // This ensures properties from the custom PropertySource are available
        if (this.database == null) {
            this.database = new Database();
        }

        // Manually bind all database configurations from Environment
        // This ensures properties from the custom PropertySource are available
        if (environment != null) {
            bindDatabaseConfig(
                    environment,
                    "profile",
                    () -> this.database.profile == null
                            ? (this.database.profile = new Database.Profile())
                            : this.database.profile);
            bindDatabaseConfig(
                    environment,
                    "catalog",
                    () -> this.database.catalog == null
                            ? (this.database.catalog = new Database.Catalog())
                            : this.database.catalog);
            bindDatabaseConfig(
                    environment,
                    "registry",
                    () -> this.database.registry == null
                            ? (this.database.registry = new Database.Registry())
                            : this.database.registry);
            bindDatabaseConfig(
                    environment,
                    "replica",
                    () -> this.database.replica == null
                            ? (this.database.replica = new Database.Replica())
                            : this.database.replica);
            bindDatabaseConfig(
                    environment,
                    "research",
                    () -> this.database.research == null
                            ? (this.database.research = new Database.Research())
                            : this.database.research);
            bindDatabaseConfig(
                    environment,
                    "sharing",
                    () -> this.database.sharing == null
                            ? (this.database.sharing = new Database.Sharing())
                            : this.database.sharing);
            bindDatabaseConfig(
                    environment,
                    "workflow",
                    () -> this.database.workflow == null
                            ? (this.database.workflow = new Database.Workflow())
                            : this.database.workflow);
            bindDatabaseConfig(
                    environment,
                    "vault",
                    () -> this.database.vault == null
                            ? (this.database.vault = new Database.Vault())
                            : this.database.vault);
        }

        // Manually bind security.vault.keystore properties from Environment
        if (environment != null && this.security != null) {
            if (this.security.vault == null) {
                this.security.vault = new Security.Vault();
            }
            if (this.security.vault.keystore == null) {
                this.security.vault.keystore = new Security.Vault.Keystore();
            }
            if (this.security.vault.keystore.url == null) {
                String keystoreUrl = environment.getProperty("security.vault.keystore.url");
                if (keystoreUrl != null) {
                    this.security.vault.keystore.url = keystoreUrl;
                    this.security.vault.keystore.alias =
                            environment.getProperty("security.vault.keystore.alias", "airavata");
                    this.security.vault.keystore.password =
                            environment.getProperty("security.vault.keystore.password", "airavata");
                    logger.debug("Manually bound security.vault.keystore properties from Environment");
                }
            }
        }

        // Store instance for static method access
        instance = this;

        // Bind services.defaults.user/password from security.iam.super.*
        if (services != null && services.defaults != null && security != null && security.iam != null) {
            services.defaults.user = security.iam.superAdminUsername;
            services.defaults.password = security.iam.superAdminPassword;
        }
    }

    /**
     * Helper method to bind database configuration from Environment.
     * Works with any database config class that has url, driver, user, password, validationQuery fields.
     */
    private void bindDatabaseConfig(Environment env, String dbName, java.util.function.Supplier<Object> dbSupplier) {
        if (env == null) return;
        String url = env.getProperty("database." + dbName + ".url");
        if (url != null && !url.isEmpty()) {
            try {
                Object db = dbSupplier.get();
                if (db != null) {
                    // Use reflection to set fields since database config classes have same structure
                    java.lang.reflect.Field urlField = db.getClass().getField("url");
                    if (urlField.get(db) == null) {
                        urlField.set(db, url);
                        java.lang.reflect.Field driverField = db.getClass().getField("driver");
                        driverField.set(
                                db, env.getProperty("database." + dbName + ".driver", "org.mariadb.jdbc.Driver"));
                        java.lang.reflect.Field userField = db.getClass().getField("user");
                        userField.set(db, env.getProperty("database." + dbName + ".user"));
                        java.lang.reflect.Field passwordField = db.getClass().getField("password");
                        passwordField.set(db, env.getProperty("database." + dbName + ".password"));
                        java.lang.reflect.Field validationQueryField =
                                db.getClass().getField("validationQuery");
                        validationQueryField.set(
                                db, env.getProperty("database." + dbName + ".validation-query", "SELECT 1"));
                        logger.debug("Manually bound database.{} properties from Environment", dbName);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to bind database.{} properties: {}", dbName, e.getMessage());
            }
        }
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
            public String superAdminUsername = "admin";
            public String superAdminPassword = "admin";
            public String oauthClientId;
            public String oauthClientSecret;
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
        public boolean durableQueue = false;
        public int prefetchCount = 200;
    }

    public Kafka kafka = new Kafka();

    public static class Kafka {
        public String brokerUrl = "localhost:9092";
    }

    // ==================== Infrastructure Configuration ====================
    public Zookeeper zookeeper = new Zookeeper();

    public static class Zookeeper {
        public String serverConnection = "localhost:2181";
        public boolean embedded = false;
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
        public Default defaults = new Default();
        public Background background = new Background();
        public Helix helix = new Helix();
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
        }

        public static class Agent {
            public boolean enabled = true;
        }

        public static class Fileserver {
            public boolean enabled = true;
        }

        public Telemetry telemetry = new Telemetry();

        public static class Telemetry {
            public boolean enabled = true;
            public Server server = new Server();

            public static class Server {
                public int port = 9090;
            }
        }

        public static class Helix {
            public String clusterName = "AiravataCluster";
            public String controllerName = "AiravataController";
            public String participantName = "AiravataParticipant";
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

        public static class Default {
            public String gateway = "default";
            public String password;
            public String user;
        }

        public static class Background {
            public Controller controller = new Controller();

            public static class Controller {
                public boolean enabled = true;
            }
        }
    }

    // ==================== General Settings Configuration ====================
    public Airavata airavata = new Airavata();

    public static class Airavata {
        public Validation validation = new Validation();
        public Sharing sharing = new Sharing();
        // NOTE: super-tenant gateway id has been merged into airavata.defaults.gateway.
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

        public static class Validation {
            public boolean enabled = true;
        }

        public static class Sharing {
            public boolean enabled = true;
        }

        public static class StreamingTransfer {
            public boolean enabled = false;
        }
    }

    // ==================== Static utility methods (for tools/Helix tasks) ====================

    /**
     * Resolve the resources root path in IDE mode.
     * Attempts to find modules/distribution/src/main/resources by locating conf/airavata.properties on classpath.
     *
     * @return Absolute path to resources root, or null if not found
     */
    private static String resolveResourcesRoot() {
        try {
            // Try to locate conf/airavata.properties on classpath
            java.net.URL resourceUrl =
                    AiravataServerProperties.class.getClassLoader().getResource("conf/airavata.properties");
            if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
                // Extract filesystem path
                String resourcePath = resourceUrl.getPath();
                // Remove URL encoding if present
                if (resourcePath.contains("%20")) {
                    resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
                }
                // Remove leading slash on Windows
                if (resourcePath.startsWith("/")
                        && System.getProperty("os.name").toLowerCase().contains("win")) {
                    resourcePath = resourcePath.substring(1);
                }
                // Navigate up from conf/airavata.properties to resources root
                java.io.File resourceFile = new java.io.File(resourcePath);
                java.io.File confDir = resourceFile.getParentFile(); // conf/
                if (confDir != null && confDir.getName().equals("conf")) {
                    java.io.File resourcesRoot = confDir.getParentFile(); // resources root
                    if (resourcesRoot != null && resourcesRoot.exists() && resourcesRoot.isDirectory()) {
                        return resourcesRoot.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not resolve resources root", e);
        }
        return null;
    }

    /**
     * Get the config directory path (always {airavataHome}/conf).
     * Resolution precedence:
     * <ol>
     *   <li>Instance field (if Spring bean is initialized)</li>
     *   <li>System property -Dairavata.home=XXX</li>
     *   <li>Resources root (IDE mode)</li>
     * </ol>
     *
     * @return The config directory path ({airavataHome}/conf)
     * @throws IllegalStateException if airavataHome cannot be resolved or {airavataHome}/conf does not exist
     */
    public static String getConfigDir() {
        // Try to use instance field first (if Spring bean is initialized)
        if (instance != null && instance.airavataHome != null && !instance.airavataHome.isEmpty()) {
            File confDir = new File(instance.airavataHome, "conf");
            if (confDir.exists() && confDir.isDirectory()) {
                return confDir.getAbsolutePath();
            }
            throw new IllegalStateException("Config directory does not exist at " + confDir.getAbsolutePath()
                    + ". Please ensure airavata.home points to the correct Airavata installation directory.");
        }

        // Check system property
        String systemPropertyHome = System.getProperty("airavata.home");
        if (systemPropertyHome != null && !systemPropertyHome.isEmpty()) {
            File confDir = new File(systemPropertyHome, "conf");
            if (confDir.exists() && confDir.isDirectory()) {
                return confDir.getAbsolutePath();
            }
            throw new IllegalStateException("Config directory does not exist at " + confDir.getAbsolutePath()
                    + ". Please ensure -Dairavata.home points to the correct Airavata installation directory.");
        }

        // IDE mode: Try to resolve resources root
        String resourcesRoot = resolveResourcesRoot();
        if (resourcesRoot != null) {
            File confDir = new File(resourcesRoot, "conf");
            if (confDir.exists() && confDir.isDirectory()) {
                logger.debug("IDE mode: using resources root configDir: {}", confDir.getAbsolutePath());
                return confDir.getAbsolutePath();
            }
        }

        throw new IllegalStateException(
                "airavata.home is not set. Please set -Dairavata.home=XXX or set airavata.home in airavata.properties.");
    }

    /**
     * Load a config file from configDir.
     * The fileName should NOT include "conf/" prefix - it will be resolved relative to configDir.
     *
     * @param fileName The filename (e.g., "email-config.yml", "logback.xml", "templates/PBS_Groovy.template")
     * @return URL to the file
     * @throws IllegalStateException if configDir cannot be resolved or file is not found
     */
    public static URL loadFile(String fileName) {
        String configDir = getConfigDir(); // Will throw if not found
        try {
            // Load from filesystem: {configDir}/{fileName}
            String configDirPath = configDir.endsWith(File.separator) ? configDir : configDir + File.separator;
            String filePath = configDirPath + fileName;
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                logger.debug("Loading file from configDir: {}", file.getAbsolutePath());
                return file.toURI().toURL();
            }
            throw new IllegalStateException("Config file not found: " + fileName + " in configDir: " + configDir);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error parsing the file path from configDir: " + configDir, e);
        }
    }

    /**
     * Load and cache {@code airavata.properties} from {airavataHome}/conf.
     */
    public static Properties getAiravataProperties() {
        Properties props = cachedAiravataProperties;
        if (props != null) {
            return props;
        }
        synchronized (AiravataServerProperties.class) {
            if (cachedAiravataProperties != null) {
                return cachedAiravataProperties;
            }
            Properties loaded = new Properties();
            URL url = loadFile(SERVER_PROPERTIES); // Will throw if configDir not found or file missing
            try (InputStream is = url.openStream()) {
                loaded.load(is);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load airavata.properties from " + url, e);
            }
            cachedAiravataProperties = loaded;
            return loaded;
        }
    }

    /**
     * Lightweight, non-Spring property access for tools/Helix tasks.
     * Order: system props -> env vars -> loaded airavata.properties.
     */
    public static String getSetting(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            value = getAiravataProperties().getProperty(key);
        }
        return value != null ? value : defaultValue;
    }

    public static String getSetting(String key) {
        return getSetting(key, null);
    }

    /**
     * Custom PropertySourceFactory that loads airavata.properties
     * from configDir. Fails immediately if configDir is not set.
     */
    public static class AiravataPropertySourceFactory implements PropertySourceFactory {

        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(
                String name, EncodedResource resource) throws IOException {

            // Load from configDir (will throw if not found)
            String configDir = getConfigDir();
            try {
                String configDirPath = configDir.endsWith(File.separator) ? configDir : configDir + File.separator;
                String filePath = configDirPath + SERVER_PROPERTIES;
                File configFile = new File(filePath);

                if (!configFile.exists() || !configFile.isFile()) {
                    throw new IllegalStateException("airavata.properties not found in configDir: " + filePath);
                }

                logger.info("Loading airavata.properties from: {}", configFile.getAbsolutePath());
                Properties props = new Properties();
                try (InputStream is = new FileInputStream(configFile)) {
                    props.load(is);
                }
                // Log a sample property to verify loading
                String registryUrl = props.getProperty("database.registry.url");
                logger.debug("Loaded database.registry.url: {}", registryUrl != null ? "found" : "not found");
                return new org.springframework.core.env.PropertiesPropertySource("airavata.properties", props);
            } catch (Exception e) {
                if (e instanceof IllegalStateException) {
                    throw e;
                }
                throw new IllegalStateException("Failed to load airavata.properties from configDir: " + configDir, e);
            }
        }
    }
}
