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
import org.springframework.stereotype.Component;

/**
 * Spring Boot configuration properties for Airavata server.
 * Maps all properties from airavata.properties to strongly-typed Java objects.
 */
@Component
@ConfigurationProperties(prefix = "")
public class AiravataServerProperties {

    private String airavataConfigDir = ".";

    private ApiServer apiServer = new ApiServer();
    private Database database = new Database();
    private Security security = new Security();
    private RabbitMQ rabbitMQ = new RabbitMQ();
    private Kafka kafka = new Kafka();
    private Zookeeper zookeeper = new Zookeeper();
    private Helix helix = new Helix();
    private Workflow workflow = new Workflow();
    private Monitoring monitoring = new Monitoring();
    private Orchestrator orchestrator = new Orchestrator();
    private Profile profile = new Profile();
    private Sharing sharing = new Sharing();
    private Iam iam = new Iam();
    private DefaultRegistry defaultRegistry = new DefaultRegistry();
    private Job job = new Job();
    private DataParser dataParser = new DataParser();
    private Metascheduler metascheduler = new Metascheduler();
    private DataAnalyzer dataAnalyzer = new DataAnalyzer();
    private Thrift thrift = new Thrift();
    private Other other = new Other();

    public String getAiravataConfigDir() {
        return airavataConfigDir;
    }

    public void setAiravataConfigDir(String airavataConfigDir) {
        this.airavataConfigDir = airavataConfigDir;
    }

    public ApiServer getApiServer() {
        return apiServer;
    }

    public void setApiServer(ApiServer apiServer) {
        this.apiServer = apiServer;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public RabbitMQ getRabbitMQ() {
        return rabbitMQ;
    }

    public void setRabbitMQ(RabbitMQ rabbitMQ) {
        this.rabbitMQ = rabbitMQ;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Zookeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(Zookeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    public Helix getHelix() {
        return helix;
    }

    public void setHelix(Helix helix) {
        this.helix = helix;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    public Orchestrator getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Sharing getSharing() {
        return sharing;
    }

    public void setSharing(Sharing sharing) {
        this.sharing = sharing;
    }

    public Iam getIam() {
        return iam;
    }

    public void setIam(Iam iam) {
        this.iam = iam;
    }

    public DefaultRegistry getDefaultRegistry() {
        return defaultRegistry;
    }

    public void setDefaultRegistry(DefaultRegistry defaultRegistry) {
        this.defaultRegistry = defaultRegistry;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public DataParser getDataParser() {
        return dataParser;
    }

    public void setDataParser(DataParser dataParser) {
        this.dataParser = dataParser;
    }

    public Metascheduler getMetascheduler() {
        return metascheduler;
    }

    public void setMetascheduler(Metascheduler metascheduler) {
        this.metascheduler = metascheduler;
    }

    public DataAnalyzer getDataAnalyzer() {
        return dataAnalyzer;
    }

    public void setDataAnalyzer(DataAnalyzer dataAnalyzer) {
        this.dataAnalyzer = dataAnalyzer;
    }

    public Thrift getThrift() {
        return thrift;
    }

    public void setThrift(Thrift thrift) {
        this.thrift = thrift;
    }

    public Other getOther() {
        return other;
    }

    public void setOther(Other other) {
        this.other = other;
    }

    public static class ApiServer {
        private String host = "0.0.0.0";
        private int port = 8930;
        private int minThreads = 50;
        private String clazz = "org.apache.airavata.api.thrift.server.AiravataServiceServer";
        private MonitoringConfig monitoring = new MonitoringConfig();

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getClazz() {
            return clazz;
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public int getMinThreads() {
            return minThreads;
        }

        public void setMinThreads(int minThreads) {
            this.minThreads = minThreads;
        }

        public MonitoringConfig getMonitoring() {
            return monitoring;
        }

        public void setMonitoring(MonitoringConfig monitoring) {
            this.monitoring = monitoring;
        }

        public static class MonitoringConfig {
            private boolean enabled = true;
            private String host = "0.0.0.0";
            private int port = 9097;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }
    }

    public static class Database {
        private Registry registry = new Registry();
        private AppCatalog appCatalog = new AppCatalog();
        private CredentialStore credentialStore = new CredentialStore();
        private ProfileService profileService = new ProfileService();
        private SharingCatalog sharingCatalog = new SharingCatalog();
        private ReplicaCatalog replicaCatalog = new ReplicaCatalog();
        private WorkflowCatalog workflowCatalog = new WorkflowCatalog();
        private String validationQuery = "SELECT 1 from CONFIGURATION";

        public Registry getRegistry() {
            return registry;
        }

        public void setRegistry(Registry registry) {
            this.registry = registry;
        }

        public AppCatalog getAppCatalog() {
            return appCatalog;
        }

        public void setAppCatalog(AppCatalog appCatalog) {
            this.appCatalog = appCatalog;
        }

        public CredentialStore getCredentialStore() {
            return credentialStore;
        }

        public void setCredentialStore(CredentialStore credentialStore) {
            this.credentialStore = credentialStore;
        }

        public ProfileService getProfileService() {
            return profileService;
        }

        public void setProfileService(ProfileService profileService) {
            this.profileService = profileService;
        }

        public SharingCatalog getSharingCatalog() {
            return sharingCatalog;
        }

        public void setSharingCatalog(SharingCatalog sharingCatalog) {
            this.sharingCatalog = sharingCatalog;
        }

        public ReplicaCatalog getReplicaCatalog() {
            return replicaCatalog;
        }

        public void setReplicaCatalog(ReplicaCatalog replicaCatalog) {
            this.replicaCatalog = replicaCatalog;
        }

        public WorkflowCatalog getWorkflowCatalog() {
            return workflowCatalog;
        }

        public void setWorkflowCatalog(WorkflowCatalog workflowCatalog) {
            this.workflowCatalog = workflowCatalog;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public static class Registry {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String validationQuery = "SELECT 1 from CONFIGURATION";

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getValidationQuery() {
                return validationQuery;
            }

            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }
        }

        public static class AppCatalog {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String validationQuery = "SELECT 1 from CONFIGURATION";

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getValidationQuery() {
                return validationQuery;
            }

            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }
        }

        public static class CredentialStore {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String jdbcValidationQuery = "SELECT 1 from CONFIGURATION";
            private String serverHost = "localhost";
            private int serverPort = 8960;
            private String clazz = "org.apache.airavata.api.thrift.server.CredentialServiceServer";
            private Keystore keystore = new Keystore();

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getJdbcValidationQuery() {
                return jdbcValidationQuery;
            }

            public void setJdbcValidationQuery(String jdbcValidationQuery) {
                this.jdbcValidationQuery = jdbcValidationQuery;
            }

            public String getServerHost() {
                return serverHost;
            }

            public void setServerHost(String serverHost) {
                this.serverHost = serverHost;
            }

            public int getServerPort() {
                return serverPort;
            }

            public void setServerPort(int serverPort) {
                this.serverPort = serverPort;
            }

            public String getClazz() {
                return clazz;
            }

            public void setClazz(String clazz) {
                this.clazz = clazz;
            }

            public Keystore getKeystore() {
                return keystore;
            }

            public void setKeystore(Keystore keystore) {
                this.keystore = keystore;
            }

            public static class Keystore {
                private String url;
                private String password;
                private String alias;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(String password) {
                    this.password = password;
                }

                public String getAlias() {
                    return alias;
                }

                public void setAlias(String alias) {
                    this.alias = alias;
                }
            }
        }

        public static class ProfileService {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String validationQuery = "SELECT 1";
            private String serverHost;
            private int serverPort = 8962;
            private String clazz = "org.apache.airavata.api.thrift.server.ProfileServiceServer";

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getValidationQuery() {
                return validationQuery;
            }

            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }

            public String getServerHost() {
                return serverHost;
            }

            public void setServerHost(String serverHost) {
                this.serverHost = serverHost;
            }

            public int getServerPort() {
                return serverPort;
            }

            public void setServerPort(int serverPort) {
                this.serverPort = serverPort;
            }

            public String getClazz() {
                return clazz;
            }

            public void setClazz(String clazz) {
                this.clazz = clazz;
            }
        }

        public static class SharingCatalog {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String validationQuery = "SELECT 1 from CONFIGURATION";

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getValidationQuery() {
                return validationQuery;
            }

            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }
        }

        public static class ReplicaCatalog {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String validationQuery = "SELECT 1 from CONFIGURATION";

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getValidationQuery() {
                return validationQuery;
            }

            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }
        }

        public static class WorkflowCatalog {
            private String jdbcDriver = "org.mariadb.jdbc.Driver";
            private String jdbcUrl;
            private String jdbcUser;
            private String jdbcPassword;
            private String validationQuery = "SELECT 1 from CONFIGURATION";

            public String getJdbcDriver() {
                return jdbcDriver;
            }

            public void setJdbcDriver(String jdbcDriver) {
                this.jdbcDriver = jdbcDriver;
            }

            public String getJdbcUrl() {
                return jdbcUrl;
            }

            public void setJdbcUrl(String jdbcUrl) {
                this.jdbcUrl = jdbcUrl;
            }

            public String getJdbcUser() {
                return jdbcUser;
            }

            public void setJdbcUser(String jdbcUser) {
                this.jdbcUser = jdbcUser;
            }

            public String getJdbcPassword() {
                return jdbcPassword;
            }

            public void setJdbcPassword(String jdbcPassword) {
                this.jdbcPassword = jdbcPassword;
            }

            public String getValidationQuery() {
                return validationQuery;
            }

            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }
        }
    }

    public static class Security {
        private Tls tls = new Tls();
        private Keystore keystore = new Keystore();
        private AuthzCache authzCache = new AuthzCache();
        private String securityManagerClass = "org.apache.airavata.security.KeyCloakSecurityManager";

        public Tls getTls() {
            return tls;
        }

        public void setTls(Tls tls) {
            this.tls = tls;
        }

        public Keystore getKeystore() {
            return keystore;
        }

        public void setKeystore(Keystore keystore) {
            this.keystore = keystore;
        }

        public AuthzCache getAuthzCache() {
            return authzCache;
        }

        public void setAuthzCache(AuthzCache authzCache) {
            this.authzCache = authzCache;
        }

        public String getSecurityManagerClass() {
            return securityManagerClass;
        }

        public void setSecurityManagerClass(String securityManagerClass) {
            this.securityManagerClass = securityManagerClass;
        }

        public static class Tls {
            private boolean enabled = false;
            private int clientTimeout = 10000;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getClientTimeout() {
                return clientTimeout;
            }

            public void setClientTimeout(int clientTimeout) {
                this.clientTimeout = clientTimeout;
            }
        }

        public static class Keystore {
            private String path = "keystores/airavata.p12";
            private String password = "airavata";

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }

        public static class AuthzCache {
            private boolean enabled = true;
            private String managerClass = "org.apache.airavata.security.authzcache.DefaultAuthzCacheManager";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getManagerClass() {
                return managerClass;
            }

            public void setManagerClass(String managerClass) {
                this.managerClass = managerClass;
            }
        }
    }

    public static class RabbitMQ {
        private String brokerUrl = "amqp://guest:guest@airavata.host:5672/develop";
        private String experimentExchangeName = "experiment_exchange";
        private String experimentLaunchQueueName = "experiment.launch.queue";
        private String processExchangeName = "process_exchange";
        private String statusExchangeName = "status_exchange";
        private boolean durableQueue = false;
        private int prefetchCount = 200;

        public String getBrokerUrl() {
            return brokerUrl;
        }

        public void setBrokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
        }

        public String getExperimentExchangeName() {
            return experimentExchangeName;
        }

        public void setExperimentExchangeName(String experimentExchangeName) {
            this.experimentExchangeName = experimentExchangeName;
        }

        public String getExperimentLaunchQueueName() {
            return experimentLaunchQueueName;
        }

        public void setExperimentLaunchQueueName(String experimentLaunchQueueName) {
            this.experimentLaunchQueueName = experimentLaunchQueueName;
        }

        public String getProcessExchangeName() {
            return processExchangeName;
        }

        public void setProcessExchangeName(String processExchangeName) {
            this.processExchangeName = processExchangeName;
        }

        public String getStatusExchangeName() {
            return statusExchangeName;
        }

        public void setStatusExchangeName(String statusExchangeName) {
            this.statusExchangeName = statusExchangeName;
        }

        public boolean isDurableQueue() {
            return durableQueue;
        }

        public void setDurableQueue(boolean durableQueue) {
            this.durableQueue = durableQueue;
        }

        public int getPrefetchCount() {
            return prefetchCount;
        }

        public void setPrefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
        }
    }

    public static class Kafka {
        private String brokerUrl = "airavata.host:9092";

        public String getBrokerUrl() {
            return brokerUrl;
        }

        public void setBrokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
        }
    }

    public static class Zookeeper {
        private String serverConnection = "airavata.host:2181";
        private boolean embedded = false;

        public String getServerConnection() {
            return serverConnection;
        }

        public void setServerConnection(String serverConnection) {
            this.serverConnection = serverConnection;
        }

        public boolean isEmbedded() {
            return embedded;
        }

        public void setEmbedded(boolean embedded) {
            this.embedded = embedded;
        }
    }

    public static class Helix {
        private String clusterName = "AiravataCluster";
        private String controllerName = "AiravataController";
        private String participantName = "AiravataParticipant";
        private boolean controllerEnabled = true;
        private boolean participantEnabled = true;

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getControllerName() {
            return controllerName;
        }

        public void setControllerName(String controllerName) {
            this.controllerName = controllerName;
        }

        public String getParticipantName() {
            return participantName;
        }

        public void setParticipantName(String participantName) {
            this.participantName = participantName;
        }

        public boolean isControllerEnabled() {
            return controllerEnabled;
        }

        public void setControllerEnabled(boolean controllerEnabled) {
            this.controllerEnabled = controllerEnabled;
        }

        public boolean isParticipantEnabled() {
            return participantEnabled;
        }

        public void setParticipantEnabled(boolean participantEnabled) {
            this.participantEnabled = participantEnabled;
        }
    }

    public static class Workflow {
        private PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        private PostWorkflowManager postWorkflowManager = new PostWorkflowManager();
        private boolean preEnabled = true;
        private boolean parserEnabled = true;
        private boolean postEnabled = true;

        public PreWorkflowManager getPreWorkflowManager() {
            return preWorkflowManager;
        }

        public void setPreWorkflowManager(PreWorkflowManager preWorkflowManager) {
            this.preWorkflowManager = preWorkflowManager;
        }

        public PostWorkflowManager getPostWorkflowManager() {
            return postWorkflowManager;
        }

        public void setPostWorkflowManager(PostWorkflowManager postWorkflowManager) {
            this.postWorkflowManager = postWorkflowManager;
        }

        public boolean isPreEnabled() {
            return preEnabled;
        }

        public void setPreEnabled(boolean preEnabled) {
            this.preEnabled = preEnabled;
        }

        public boolean isParserEnabled() {
            return parserEnabled;
        }

        public void setParserEnabled(boolean parserEnabled) {
            this.parserEnabled = parserEnabled;
        }

        public boolean isPostEnabled() {
            return postEnabled;
        }

        public void setPostEnabled(boolean postEnabled) {
            this.postEnabled = postEnabled;
        }

        public static class PreWorkflowManager {
            private boolean loadbalanceClusters = false;
            private boolean monitoringEnabled = true;
            private String monitoringHost;
            private int monitoringPort = 9093;
            private String name = "AiravataPreWM";

            public boolean isLoadbalanceClusters() {
                return loadbalanceClusters;
            }

            public void setLoadbalanceClusters(boolean loadbalanceClusters) {
                this.loadbalanceClusters = loadbalanceClusters;
            }

            public boolean isMonitoringEnabled() {
                return monitoringEnabled;
            }

            public void setMonitoringEnabled(boolean monitoringEnabled) {
                this.monitoringEnabled = monitoringEnabled;
            }

            public String getMonitoringHost() {
                return monitoringHost;
            }

            public void setMonitoringHost(String monitoringHost) {
                this.monitoringHost = monitoringHost;
            }

            public int getMonitoringPort() {
                return monitoringPort;
            }

            public void setMonitoringPort(int monitoringPort) {
                this.monitoringPort = monitoringPort;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class PostWorkflowManager {
            private boolean loadbalanceClusters = false;
            private boolean monitoringEnabled = true;
            private String monitoringHost;
            private int monitoringPort = 9094;
            private String name = "AiravataPostWM";

            public boolean isLoadbalanceClusters() {
                return loadbalanceClusters;
            }

            public void setLoadbalanceClusters(boolean loadbalanceClusters) {
                this.loadbalanceClusters = loadbalanceClusters;
            }

            public boolean isMonitoringEnabled() {
                return monitoringEnabled;
            }

            public void setMonitoringEnabled(boolean monitoringEnabled) {
                this.monitoringEnabled = monitoringEnabled;
            }

            public String getMonitoringHost() {
                return monitoringHost;
            }

            public void setMonitoringHost(String monitoringHost) {
                this.monitoringHost = monitoringHost;
            }

            public int getMonitoringPort() {
                return monitoringPort;
            }

            public void setMonitoringPort(int monitoringPort) {
                this.monitoringPort = monitoringPort;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }

    public static class Monitoring {
        private EmailBasedMonitor emailBasedMonitor = new EmailBasedMonitor();
        private RealtimeMonitor realtimeMonitor = new RealtimeMonitor();
        private ParticipantMonitoring participantMonitoring = new ParticipantMonitoring();
        private ClusterStatusMonitoring clusterStatusMonitoring = new ClusterStatusMonitoring();
        private int emailExpirationMinutes = 60;

        public EmailBasedMonitor getEmailBasedMonitor() {
            return emailBasedMonitor;
        }

        public void setEmailBasedMonitor(EmailBasedMonitor emailBasedMonitor) {
            this.emailBasedMonitor = emailBasedMonitor;
        }

        public RealtimeMonitor getRealtimeMonitor() {
            return realtimeMonitor;
        }

        public void setRealtimeMonitor(RealtimeMonitor realtimeMonitor) {
            this.realtimeMonitor = realtimeMonitor;
        }

        public ParticipantMonitoring getParticipantMonitoring() {
            return participantMonitoring;
        }

        public void setParticipantMonitoring(ParticipantMonitoring participantMonitoring) {
            this.participantMonitoring = participantMonitoring;
        }

        public ClusterStatusMonitoring getClusterStatusMonitoring() {
            return clusterStatusMonitoring;
        }

        public void setClusterStatusMonitoring(ClusterStatusMonitoring clusterStatusMonitoring) {
            this.clusterStatusMonitoring = clusterStatusMonitoring;
        }

        public int getEmailExpirationMinutes() {
            return emailExpirationMinutes;
        }

        public void setEmailExpirationMinutes(int emailExpirationMinutes) {
            this.emailExpirationMinutes = emailExpirationMinutes;
        }

        public static class EmailBasedMonitor {
            private String address;
            private String folderName = "INBOX";
            private String host;
            private String password;
            private String storeProtocol = "imaps";
            private int period = 10000;
            private boolean monitorEnabled = true;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public boolean isMonitorEnabled() {
                return monitorEnabled;
            }

            public void setMonitorEnabled(boolean monitorEnabled) {
                this.monitorEnabled = monitorEnabled;
            }

            public String getFolderName() {
                return folderName;
            }

            public void setFolderName(String folderName) {
                this.folderName = folderName;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getStoreProtocol() {
                return storeProtocol;
            }

            public void setStoreProtocol(String storeProtocol) {
                this.storeProtocol = storeProtocol;
            }

            public int getPeriod() {
                return period;
            }

            public void setPeriod(int period) {
                this.period = period;
            }
        }

        public static class RealtimeMonitor {
            private boolean enabled = false;
            private boolean monitorEnabled = true;
            private String brokerConsumerGroup = "monitor";
            private String brokerTopic = "helix-airavata-mq";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isMonitorEnabled() {
                return monitorEnabled;
            }

            public void setMonitorEnabled(boolean monitorEnabled) {
                this.monitorEnabled = monitorEnabled;
            }

            public String getBrokerConsumerGroup() {
                return brokerConsumerGroup;
            }

            public void setBrokerConsumerGroup(String brokerConsumerGroup) {
                this.brokerConsumerGroup = brokerConsumerGroup;
            }

            public String getBrokerTopic() {
                return brokerTopic;
            }

            public void setBrokerTopic(String brokerTopic) {
                this.brokerTopic = brokerTopic;
            }
        }

        public static class ParticipantMonitoring {
            private boolean enabled = true;
            private String host;
            private int port = 9096;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }

        public static class ClusterStatusMonitoring {
            private boolean enable = false;
            private int repeatTime = 18000;

            public boolean isEnable() {
                return enable;
            }

            public void setEnable(boolean enable) {
                this.enable = enable;
            }

            public int getRepeatTime() {
                return repeatTime;
            }

            public void setRepeatTime(int repeatTime) {
                this.repeatTime = repeatTime;
            }
        }
    }

    public static class Orchestrator {
        private String serverHost;
        private int serverMinThreads = 50;
        private int serverPort = 8940;
        private String clazz = "org.apache.airavata.api.thrift.server.OrchestratorServiceServer";

        public String getServerHost() {
            return serverHost;
        }

        public void setServerHost(String serverHost) {
            this.serverHost = serverHost;
        }

        public int getServerMinThreads() {
            return serverMinThreads;
        }

        public void setServerMinThreads(int serverMinThreads) {
            this.serverMinThreads = serverMinThreads;
        }

        public int getServerPort() {
            return serverPort;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        public String getClazz() {
            return clazz;
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }
    }

    public static class Profile {
        // Profile properties are in Database.ProfileService
    }

    public static class Sharing {
        private String serverHost = "0.0.0.0";
        private int serverPort = 7878;
        private String clazz = "org.apache.airavata.api.thrift.server.SharingRegistryServer";
        private boolean enabled = true;

        public String getServerHost() {
            return serverHost;
        }

        public void setServerHost(String serverHost) {
            this.serverHost = serverHost;
        }

        public int getServerPort() {
            return serverPort;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        public String getClazz() {
            return clazz;
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Iam {
        private String serverUrl;
        private String superAdminUsername = "admin";
        private String superAdminPassword = "admin";

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getSuperAdminUsername() {
            return superAdminUsername;
        }

        public void setSuperAdminUsername(String superAdminUsername) {
            this.superAdminUsername = superAdminUsername;
        }

        public String getSuperAdminPassword() {
            return superAdminPassword;
        }

        public void setSuperAdminPassword(String superAdminPassword) {
            this.superAdminPassword = superAdminPassword;
        }
    }

    public static class DefaultRegistry {
        private String gateway = "default";
        private String oauthClientId;
        private String oauthClientSecret;
        private String password;
        private String user;

        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public String getOauthClientId() {
            return oauthClientId;
        }

        public void setOauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
        }

        public String getOauthClientSecret() {
            return oauthClientSecret;
        }

        public void setOauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }

    public static class Job {
        private Monitor monitor = new Monitor();
        private Notification notification = new Notification();
        private String statusPublishEndpoint;
        private String validators;

        public Monitor getMonitor() {
            return monitor;
        }

        public void setMonitor(Monitor monitor) {
            this.monitor = monitor;
        }

        public Notification getNotification() {
            return notification;
        }

        public void setNotification(Notification notification) {
            this.notification = notification;
        }

        public String getStatusPublishEndpoint() {
            return statusPublishEndpoint;
        }

        public void setStatusPublishEndpoint(String statusPublishEndpoint) {
            this.statusPublishEndpoint = statusPublishEndpoint;
        }

        public String getValidators() {
            return validators;
        }

        public void setValidators(String validators) {
            this.validators = validators;
        }

        public static class Monitor {
            private String brokerPublisherId = "AiravataMonitorPublisher";
            private String emailPublisherId = "EmailBasedProducer";
            private String realtimePublisherId = "RealtimeProducer";
            private String brokerTopic = "monitoring-data";
            private String brokerConsumerGroup = "MonitoringConsumer";

            public String getBrokerPublisherId() {
                return brokerPublisherId;
            }

            public void setBrokerPublisherId(String brokerPublisherId) {
                this.brokerPublisherId = brokerPublisherId;
            }

            public String getEmailPublisherId() {
                return emailPublisherId;
            }

            public void setEmailPublisherId(String emailPublisherId) {
                this.emailPublisherId = emailPublisherId;
            }

            public String getRealtimePublisherId() {
                return realtimePublisherId;
            }

            public void setRealtimePublisherId(String realtimePublisherId) {
                this.realtimePublisherId = realtimePublisherId;
            }

            public String getBrokerTopic() {
                return brokerTopic;
            }

            public void setBrokerTopic(String brokerTopic) {
                this.brokerTopic = brokerTopic;
            }

            public String getBrokerConsumerGroup() {
                return brokerConsumerGroup;
            }

            public void setBrokerConsumerGroup(String brokerConsumerGroup) {
                this.brokerConsumerGroup = brokerConsumerGroup;
            }
        }

        public static class Notification {
            private String emailids = "";
            private boolean enable = true;

            public String getEmailids() {
                return emailids;
            }

            public void setEmailids(String emailids) {
                this.emailids = emailids;
            }

            public boolean isEnable() {
                return enable;
            }

            public void setEnable(boolean enable) {
                this.enable = enable;
            }
        }
    }

    public static class DataParser {
        private String brokerConsumerGroup = "ParsingConsumer";
        private String topic = "parsing-data";
        private String storageResourceId = "CHANGE_ME";
        private boolean deleteContainer = true;
        private boolean analyzerJobScanningEnable = false;

        public String getBrokerConsumerGroup() {
            return brokerConsumerGroup;
        }

        public void setBrokerConsumerGroup(String brokerConsumerGroup) {
            this.brokerConsumerGroup = brokerConsumerGroup;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getStorageResourceId() {
            return storageResourceId;
        }

        public void setStorageResourceId(String storageResourceId) {
            this.storageResourceId = storageResourceId;
        }

        public boolean isDeleteContainer() {
            return deleteContainer;
        }

        public void setDeleteContainer(boolean deleteContainer) {
            this.deleteContainer = deleteContainer;
        }

        public boolean isAnalyzerJobScanningEnable() {
            return analyzerJobScanningEnable;
        }

        public void setAnalyzerJobScanningEnable(boolean analyzerJobScanningEnable) {
            this.analyzerJobScanningEnable = analyzerJobScanningEnable;
        }
    }

    public static class Metascheduler {
        private boolean jobScanningEnable = false;

        public boolean isJobScanningEnable() {
            return jobScanningEnable;
        }

        public void setJobScanningEnable(boolean jobScanningEnable) {
            this.jobScanningEnable = jobScanningEnable;
        }
    }

    public static class DataAnalyzer {
        private boolean jobScanningEnable = false;

        public boolean isJobScanningEnable() {
            return jobScanningEnable;
        }

        public void setJobScanningEnable(boolean jobScanningEnable) {
            this.jobScanningEnable = jobScanningEnable;
        }
    }

    public static class Thrift {
        private ClientPool clientPool = new ClientPool();

        public ClientPool getClientPool() {
            return clientPool;
        }

        public void setClientPool(ClientPool clientPool) {
            this.clientPool = clientPool;
        }

        public static class ClientPool {
            private boolean abandonedRemovalEnabled = true;
            private boolean abandonedRemovalLogged = false;

            public boolean isAbandonedRemovalEnabled() {
                return abandonedRemovalEnabled;
            }

            public void setAbandonedRemovalEnabled(boolean abandonedRemovalEnabled) {
                this.abandonedRemovalEnabled = abandonedRemovalEnabled;
            }

            public boolean isAbandonedRemovalLogged() {
                return abandonedRemovalLogged;
            }

            public void setAbandonedRemovalLogged(boolean abandonedRemovalLogged) {
                this.abandonedRemovalLogged = abandonedRemovalLogged;
            }
        }
    }

    public static class Other {
        private String localDataLocation = "/tmp";
        private int inMemoryCacheSize = 1000;
        private boolean enableValidation = true;
        private boolean enableStreamingTransfer = false;
        private String hostScheduler = "org.apache.airavata.orchestrator.core.schedule.DefaultHostScheduler";
        private String superTenantGatewayId = "default";
        private String dbEventManagerClass = "org.apache.airavata.main.DBEventManagerRunner";
        private String regserver = "org.apache.airavata.api.thrift.server.RegistryServiceServer";
        private RegistryServer registryServer = new RegistryServer();
        private String strictHostKeyChecking = "no";

        public String getLocalDataLocation() {
            return localDataLocation;
        }

        public void setLocalDataLocation(String localDataLocation) {
            this.localDataLocation = localDataLocation;
        }

        public int getInMemoryCacheSize() {
            return inMemoryCacheSize;
        }

        public void setInMemoryCacheSize(int inMemoryCacheSize) {
            this.inMemoryCacheSize = inMemoryCacheSize;
        }

        public boolean isEnableValidation() {
            return enableValidation;
        }

        public void setEnableValidation(boolean enableValidation) {
            this.enableValidation = enableValidation;
        }

        public boolean isEnableStreamingTransfer() {
            return enableStreamingTransfer;
        }

        public void setEnableStreamingTransfer(boolean enableStreamingTransfer) {
            this.enableStreamingTransfer = enableStreamingTransfer;
        }

        public String getHostScheduler() {
            return hostScheduler;
        }

        public void setHostScheduler(String hostScheduler) {
            this.hostScheduler = hostScheduler;
        }

        public String getSuperTenantGatewayId() {
            return superTenantGatewayId;
        }

        public void setSuperTenantGatewayId(String superTenantGatewayId) {
            this.superTenantGatewayId = superTenantGatewayId;
        }

        public String getDbEventManagerClass() {
            return dbEventManagerClass;
        }

        public void setDbEventManagerClass(String dbEventManagerClass) {
            this.dbEventManagerClass = dbEventManagerClass;
        }

        public String getRegserver() {
            return regserver;
        }

        public void setRegserver(String regserver) {
            this.regserver = regserver;
        }

        public RegistryServer getRegistryServer() {
            return registryServer;
        }

        public void setRegistryServer(RegistryServer registryServer) {
            this.registryServer = registryServer;
        }

        public String getStrictHostKeyChecking() {
            return strictHostKeyChecking;
        }

        public void setStrictHostKeyChecking(String strictHostKeyChecking) {
            this.strictHostKeyChecking = strictHostKeyChecking;
        }

        public static class RegistryServer {
            private String host = "localhost";
            private int minThreads = 50;
            private int port = 8970;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getMinThreads() {
                return minThreads;
            }

            public void setMinThreads(int minThreads) {
                this.minThreads = minThreads;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }
    }
}
