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

package org.apache.airavata.core.gfac.utils;

import static org.apache.airavata.core.gfac.utils.GfacUtils.findStrProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.registry.RegistryService;
import org.apache.xmlbeans.XmlException;
import org.ietf.jgss.GSSCredential;
import org.ogce.schemas.gfac.documents.ApplicationDescriptionType;
import org.ogce.schemas.gfac.documents.ConfigurationDocument;
import org.ogce.schemas.gfac.documents.ConfigurationType;
import org.ogce.schemas.gfac.documents.HostDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.indiana.extreme.lead.workflow_tracking.Notifier;

public class GlobalConfiguration implements GFacOptions {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Properties config;

    private String registryURL;

    private String capManURL;

    private String notificationBrokerURL;

    private String executeLocally;

    private List<String> deploymentHosts;

    private String tempDir = "/tmp";

    private String localHost;

    private Notifier notifier;

    private RegistryService registryService;

    private int port;

    private HashMap<String, HostDescriptionType> hosts = new HashMap<String, HostDescriptionType>();

    private HashMap<String, ApplicationDescriptionType> appNameAndHost2App = new HashMap<String, ApplicationDescriptionType>();
    private HashMap<String, ApplicationDescriptionType> appName2App = new HashMap<String, ApplicationDescriptionType>();

    private String hostSchedulerUrl = null;

    // private long wait4FirstReq = AppicationLifeTimeManager.HOUR_IN_MS * 3;
    //
    // private long lifetimeResolution = AppicationLifeTimeManager.HOUR_IN_MS / 12;
    //
    // private ExtensionRepository extensionRepository;
    //
    // private LocalJobManager localJobManager;
    //
    // private MessageBoxService messageBoxService;

    private String topic;

    private boolean tranportSecurity = false;

    // private CompositeMessageIntercepter messageCorrelator;

    private boolean wsGramPrefered = false;

    // private AdminNotifier adminNotifier;

    private String hostcertsKeyFile;

    private String trustedCertsFile;

    private int minimalLifetimePerRequestMinutes = 10;

    private ConfigurationType extensionConfiguration;

    // private CredentialContext credentialContext;

    private Map<String, OutputStream> stdInMapOfApp = new HashMap<String, OutputStream>();

    private final ExecutorService threadScheduler = Executors.newCachedThreadPool();

    private Set<String> retryonErrorCodes = new HashSet<String>();

    public GlobalConfiguration(Properties initialconfig) throws GfacException {
        this(initialconfig, true, false);
    }

    public GlobalConfiguration(Properties initialconfig, boolean loadFromEnviorment, boolean isPersistant)
            throws GfacException {
        try {
            // build effective properties by merging the properties with the
            // enviorment
            this.config = loadEffectiveProperties(initialconfig, loadFromEnviorment);
            extensionConfiguration = loadExtensionConfiguration(isPersistant);
            registryURL = findStrProperty(config, REGISTRY_URL_NAME, registryURL);
            notificationBrokerURL = findStrProperty(config, NOTIFICATION_BROKER_URL_NAME, GFacConstants.BROKER_URL);
            topic = findStrProperty(config, "topic", "gfac-default-topic");
            // this.myLEADAgentURL = findStrProperty(config, MYLEAD_AGENT_URL_NAME, myLEADAgentURL);
            executeLocally = findStrProperty(config, "executeLocally", executeLocally);

            this.tempDir = findStrProperty(config, "GFAC_TMP_DIR", tempDir);

            String tmpDirSuffix = String.valueOf(System.currentTimeMillis()) + "_" + UUID.randomUUID();
            File tmpDirF = new File(this.tempDir, tmpDirSuffix);
            tmpDirF.mkdirs();
            this.tempDir = tmpDirF.getAbsolutePath();
            this.localHost = InetAddress.getLocalHost().getCanonicalHostName();

            // These are properties about transport security
            this.tranportSecurity = GfacUtils.findBooleanProperty(config, "transportSecurity", false);

            this.hostcertsKeyFile = findStrProperty(config, SSL_HOSTCERTS_KEY_FILE, null);
            this.trustedCertsFile = findStrProperty(config, SSL_TRUSTED_CERTS_FILE, null);

            config.getProperty("wait4FirstReqMinutes");
            config.getProperty("lifetimeResolutionMinutes");
            // if (lifetimeResolutionStr != null) {
            // lifetimeResolution = Long.valueOf(lifetimeResolutionStr) * 60 * 1000;
            // }
            // credentialContext = new CredentialContext(this);
            //
            deploymentHosts = new ArrayList<String>();
            String deploymentHostsAsStr = config.getProperty(DEPLOYMENT_HOSTS);
            if (deploymentHostsAsStr != null) {
                // remove whiteapces, and split
                String[] tempdeploymentHosts = deploymentHostsAsStr.replaceAll("\\s", "").split(",");
                for (String hostName : tempdeploymentHosts) {
                    deploymentHosts.add(hostName);
                }
            } else {
                deploymentHosts.add(getLocalHost());
            }

            wsGramPrefered = GfacUtils.findBooleanProperty(config, WS_GRAM_PREFERED, wsGramPrefered);
            // extensionRepository = new ExtensionRepository(this);
            //
            // // Initialize Message interceptors
            // messageCorrelator = new CompositeMessageIntercepter();
            //
            // adminNotifier = new AdminNotifier(notificationBrokerURL, GFacConstants.ADMIN_TOPIC,
            // getLocalHost());

            String minimalLifetimePerRequestMinutesStr = config
                    .getProperty(GFacOptions.MIN_PROXY_LIFETIME_PER_REQUEST_MINUTES);
            if (minimalLifetimePerRequestMinutesStr != null && minimalLifetimePerRequestMinutesStr.trim().length() > 0) {
                minimalLifetimePerRequestMinutes = Integer.valueOf(minimalLifetimePerRequestMinutesStr);
            }

            hostSchedulerUrl = findStrProperty(config, HOST_SCHEDULER_URL, hostSchedulerUrl);
            String[] retryonErrors = config.getProperty(GFacOptions.GFAC_RETRYONJOBERRORCODES, "").split(",");
            for (String retryonError : retryonErrors) {
                getRetryonErrorCodes().add(retryonError);
            }
        } catch (UnknownHostException e) {
            throw new GfacException(e, FaultCode.InitalizationError);
        } catch (IOException e) {
            throw new GfacException(e, FaultCode.InitalizationError);
        }

    }

    /**
     * Gfac load properties from following locations
     * <ol>
     * <li>Explicit Properties</li>
     * <li>$HOME/login.properties</li>
     * <li>conf/gfac.properties</li>
     * <ol>
     * 
     * They take precedence in that order.
     * 
     * @param initialProperties
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */

    private Properties loadEffectiveProperties(Properties initialProperties, boolean loadFromEnviorment)
            throws FileNotFoundException, IOException {
        Properties newProperties = new Properties();

        if (loadFromEnviorment) {
            String loginDataFile = System.getProperty("login.file");
            if (loginDataFile == null) {
                loginDataFile = System.getProperty("user.home") + "/login.properties";
            }
            File loginPropertiesFile = new File(loginDataFile);
            if (loginPropertiesFile.exists()) {
                InputStream loginPropertiesIn = new FileInputStream(loginPropertiesFile);
                newProperties.load(loginPropertiesIn);
                loginPropertiesIn.close();
            }

            File localConfFile = new File("conf/gfac.properties");
            if (localConfFile.exists()) {
                InputStream localPropertiesIn = new FileInputStream(loginPropertiesFile);
                newProperties.load(localPropertiesIn);
                localPropertiesIn.close();
            }
        }
        // This will make sure expliceit properties take precedance over
        // properties defined in enviorment
        newProperties.putAll(initialProperties);
        return newProperties;
    }

    // public RegistryService getRegistryService() throws GfacException {
    // if (registryURL != null) {
    // if (registryService == null) {
    // registryService = ExternalServiceFactory.createRegistryService(registryURL, this,
    // null);
    // }
    // return registryService;
    // } else {
    // throw new GfacException("Registry URL not spcified",FaultCode.InitalizationError);
    // }
    // }
    //
    // public ServiceConfiguration createServiceContext(QName serviceName, ServiceMapType serviceDesc,
    // String serviceLocation) throws GfacException {
    // ServiceConfiguration serviceContext = new ServiceConfiguration(this, serviceName,
    // serviceDesc, serviceLocation);
    // return serviceContext;
    // }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public GSSCredential getGssCredentails() throws GfacException {
        try {
            return null; // credentialContext.getProxyCredentails();
        } catch (Exception e) {
            throw new GfacException(e, FaultCode.InitalizationError);
        }
    }

    public ApplicationDescriptionType getApplicationDesc(QName name, String hostName) {
        if (hostName == null) {
            return appName2App.get(name.toString());
        }
        return appNameAndHost2App.get(name.toString() + hostName);
    }

    public void addApplicationDesc(ApplicationDescriptionType appDesc) {
        QName appName = new QName(appDesc.getApplicationName().getTargetNamespace(), appDesc.getApplicationName()
                .getStringValue());
        appNameAndHost2App.put(appName.toString() + appDesc.getDeploymentDescription().getHostName(), appDesc);
        appName2App.put(appName.toString(), appDesc);
    }

    public HostDescriptionType getHost(String name) {
        return hosts.get(name);
    }

    public void addHost(HostDescriptionType host) {
        hosts.put(host.getHostName(), host);
    }

    public String getCapManURL() {
        return capManURL;
    }

    public String getExecuteLocally() {
        return executeLocally;
    }

    public HashMap<String, HostDescriptionType> getHosts() {
        return hosts;
    }

    // public String getMyLEADAgentURL() {
    // return myLEADAgentURL;
    // }

    // public String getMyproxyLifetime() {
    // return myproxyLifetime;
    // }
    //
    // public String getMyproxyPasswd() {
    // return myproxyPasswd;
    // }
    //
    // public String getMyproxyServer() {
    // return myproxyServer;
    // }
    //
    // public String getMyproxyUserName() {
    // return myproxyUserName;
    // }

    public String getRegistryURL() {
        return registryURL;
    }

    public boolean isTranportSecurityEnabled() {
        return tranportSecurity;
    }

    public Notifier getNotifier() {
        // if (notifier == null && notificationBrokerURL != null) {
        // notifier = GfacUtils.createNotifier(notificationBrokerURL, topic);
        // }
        // return notifier;
        return null;
    }

    public String getTempDir() {
        return tempDir;
    }

    public String getLocalHost() {
        return localHost;
    }

    public Properties getConfigurations() {
        Properties copy = new Properties();
        copy.putAll(config);
        return copy;
    }

    public String getProperty(String name) {
        return config.getProperty(name);
    }

    // public long getLifetimeResolution() {
    // return lifetimeResolution;
    // }
    //
    // public long getWait4FirstReq() {
    // return wait4FirstReq;
    // }
    //
    // public ExtensionRepository getExtensionRepository() {
    // return extensionRepository;
    // }
    //
    // public LocalJobManager getLocalJobManager() {
    // if (localJobManager == null) {
    // localJobManager = new LocalJobManager();
    // }
    // return localJobManager;
    // }
    //
    // public MessageBoxService getMessageBoxService() throws GfacException {
    // if (messageBoxService == null) {
    // messageBoxService = ExternalServiceFactory.createMessageBoxService(null);
    // }
    // return messageBoxService;
    // }
    //
    // public CompositeMessageIntercepter getMessageCorrelator() {
    // return messageCorrelator;
    // }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    boolean isWsGramPrefered() {
        return wsGramPrefered;
    }

    void setWsGramPrefered(boolean wsGramPrefered) {
        this.wsGramPrefered = wsGramPrefered;
    }

    // public AdminNotifier getAdminNotifier() {
    // return adminNotifier;
    // }

    public String getHostcertsKeyFile() {
        return hostcertsKeyFile;
    }

    public String getTrustedCertsFile() {
        return trustedCertsFile;
    }

    public int getMinimalLifetimePerRequestMinutes() {
        return minimalLifetimePerRequestMinutes;
    }

    public String getHostSchedulerUrl() {
        return hostSchedulerUrl;
    }

    public ConfigurationType getExtensionConfiguration() {
        return extensionConfiguration;
    }

    /**
     * We load the configuration from local direcotry or from gfac classpath in that order
     * 
     * @return
     * @throws GfacException
     */
    private ConfigurationType loadExtensionConfiguration(boolean isPersistant) throws GfacException {
        try {
            String gfacProfile;
            if (isPersistant) {
                gfacProfile = config.getProperty(GFacOptions.GFAC_PERSISTANT_SERVICE_PROFILE);
            } else {
                gfacProfile = config.getProperty(GFacOptions.GFAC_TRANSIENT_SERVICE_PROFILE);
            }

            if (gfacProfile == null) {
                return null;
                // throw new GfacException("Properties " + GFacOptions.GFAC_PERSISTANT_SERVICE_PROFILE
                // +" and "+ GFacOptions.GFAC_TRANSIENT_SERVICE_PROFILE
                // +" must be specified ",FaultCode.InitalizationError);
            }

            File xmlConfigurationFile = new File(gfacProfile);
            InputStream input;
            if (xmlConfigurationFile.exists()) {
                input = new FileInputStream(xmlConfigurationFile);
            } else {
                input = Thread.currentThread().getContextClassLoader().getResourceAsStream(gfacProfile);
            }
            ConfigurationDocument configurationDoc = ConfigurationDocument.Factory.parse(input);
            return configurationDoc.getConfiguration();
        } catch (FileNotFoundException e) {
            throw new GfacException(e, FaultCode.InitalizationError);
        } catch (XmlException e) {
            throw new GfacException(e, FaultCode.InitalizationError);
        } catch (IOException e) {
            throw new GfacException(e, FaultCode.InitalizationError);
        }
    }

    // public void setRuntimeProperty(String name, Object value) throws GfacException {
    // if (REGISTRY_URL_NAME.equals(name)) {
    // registryService = ExternalServiceFactory.createRegistryService((String) value, this,
    // null);
    // } else if (NOTIFICATION_BROKER_URL_NAME.equals(name)) {
    // notifier = GfacUtils.createNotifier((XmlElement) value, null);
    // } else if (MYLEAD_AGENT_URL_NAME.equals(name)) {
    // myLEADAgentURL = (String) value;
    // } else {
    // if (value instanceof LangStringImpl) {
    // config.setProperty(name, ((LangStringImpl) value).getLang());
    // } else {
    // config.setProperty(name, (String) value);
    // }
    //
    // }
    // }

    public Map<String, OutputStream> getStdInMapOfApp() {
        return stdInMapOfApp;
    }

    public ExecutorService getThreadScheduler() {
        return threadScheduler;
    }

    public List<String> getDeploymentHosts() {
        return deploymentHosts;
    }

    public boolean isLocal() {
        return OPTION_INSTALLATION_LOCAL.equals(config.getProperty(OPTION_INSTALLATION));
    }

    public void setRetryonErrorCodes(Set<String> retryonErrorCodes) {
        this.retryonErrorCodes = retryonErrorCodes;
    }

    public Set<String> getRetryonErrorCodes() {
        return retryonErrorCodes;
    }

}
