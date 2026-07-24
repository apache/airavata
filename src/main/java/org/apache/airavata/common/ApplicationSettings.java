package org.apache.airavata.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSettings {
    public static final String SERVER_PROPERTIES = "application.properties";
    public static final String AIRAVATA_CONFIG_DIR = "airavata.config.dir";

    public static String ADDITIONAL_SETTINGS_FILES = "external.settings";

    private static final Map<String, String> overrides = new ConcurrentHashMap<>();

    protected Properties properties = new Properties();

    private Exception propertyLoadException;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationSettings.class);

    private static final String SHUTDOWN_STATEGY_STRING = "shutdown.strategy";

    protected static ApplicationSettings INSTANCE;

    public static enum ShutdownStrategy {
        NONE,
        SELF_TERMINATE
    }

    {
        loadProperties();
    }

    private void loadProperties() {
        URL url = getPropertyFileURL();
        try {
            properties.load(url.openStream());
            logger.info("Settings loaded from " + url.toString());
            URL[] externalSettingsFileURLs = getExternalSettingsFileURLs();
            for (URL externalSettings : externalSettingsFileURLs) {
                mergeSettingsImpl(externalSettings.openStream());
                logger.info("External settings merged from " + url.toString());
            }
        } catch (Exception e) {
            propertyLoadException = e;
        }
    }

    protected URL getPropertyFileURL() {
        return ApplicationSettings.loadFile(SERVER_PROPERTIES);
    }

    protected URL[] getExternalSettingsFileURLs() {
        try {
            List<URL> externalSettingsFileURLs = new ArrayList<URL>();
            String externalSettingsFileNames = getSettingImpl(ADDITIONAL_SETTINGS_FILES);
            String[] externalSettingFiles = externalSettingsFileNames.split(",");
            for (String externalSettingFile : externalSettingFiles) {
                URL externalSettingFileURL = ApplicationSettings.loadFile(externalSettingFile);
                if (externalSettingFileURL == null) {
                    logger.warn("Could not file external settings file " + externalSettingFile);
                } else {
                    externalSettingsFileURLs.add(externalSettingFileURL);
                }
            }
            return externalSettingsFileURLs.toArray(new URL[] {});
        } catch (Exception e) {
            return new URL[] {};
        }
    }

    protected static ApplicationSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApplicationSettings();
        }
        return INSTANCE;
    }

    private void saveProperties() throws Exception {
        URL url = getPropertyFileURL();
        if (url.getProtocol().equalsIgnoreCase("file")) {
            try {
                properties.store(
                        new FileOutputStream(url.getPath()),
                        Calendar.getInstance().toString());
            } catch (Exception e) {
                throw new Exception(url.getPath(), e);
            }
        } else {
            logger.warn("Properties cannot be updated to location " + url.toString());
        }
    }

    private void validateSuccessfulPropertyFileLoad() throws Exception {
        if (propertyLoadException != null) {
            throw new Exception(propertyLoadException.getMessage(), propertyLoadException);
        }
    }

    public String getSettingImpl(String key) throws Exception {
        String rawValue;
        if (overrides.containsKey(key)) {
            rawValue = overrides.get(key);

        } else if (System.getProperties().containsKey(key)) {
            rawValue = System.getProperties().getProperty(key);

        } else if (System.getenv().containsKey(key)) {
            rawValue = System.getenv().get(key);

        } else {
            validateSuccessfulPropertyFileLoad();
            if (properties.containsKey(key)) {
                rawValue = properties.getProperty(key);
            } else {
                throw new Exception(key);
            }
        }
        return deriveAbsoluteValueImpl(rawValue);
    }

    public String getSettingImpl(String key, String defaultValue) {
        try {
            return getSettingImpl(key);
        } catch (Exception e) {
            // we'll ignore this error since a default value is provided
        }
        return defaultValue;
    }

    private String deriveAbsoluteValueImpl(String property) {
        if (property != null) {
            Map<Integer, String> containedParameters = StringUtil.getContainedParameters(property);
            List<String> parametersAlreadyProcessed = new ArrayList<String>();
            for (String parameter : containedParameters.values()) {
                if (!parametersAlreadyProcessed.contains(parameter)) {
                    String parameterName = parameter.substring(2, parameter.length() - 1);
                    String parameterValue = getSetting(parameterName, parameter);
                    property = property.replaceAll(Pattern.quote(parameter), parameterValue);
                    parametersAlreadyProcessed.add(parameter);
                }
            }
        }
        return property;
    }

    public void mergeSettingsImpl(InputStream stream) throws IOException {
        Properties tmpProp = new Properties();
        tmpProp.load(stream);
        properties.putAll(tmpProp);
    }

    public void mergeSettingsCommandLineArgsImpl(String[] args) {
        properties.putAll(StringUtil.parseCommandLineOptions(args));
    }

    public ShutdownStrategy getShutdownStrategyImpl() throws Exception {
        String strategy = null;
        try {
            strategy = getSetting(SHUTDOWN_STATEGY_STRING, ShutdownStrategy.SELF_TERMINATE.toString());
            return ShutdownStrategy.valueOf(strategy);
        } catch (Exception e) {
            // if the string mentioned in config is invalid
            throw new Exception("Invalid shutdown strategy configured : " + strategy);
        }
    }

    /*
     * Static methods which will be used by the users
     */

    public static String getSetting(String key) throws Exception {
        return getInstance().getSettingImpl(key);
    }

    public static String getSetting(String key, String defaultValue) {
        return getInstance().getSettingImpl(key, defaultValue);
    }

    public static void setOverride(String key, String value) {
        overrides.put(key, value);
    }

    public static void setSetting(String key, String value) throws Exception {
        getInstance().properties.setProperty(key, value);
        getInstance().saveProperties();
    }

    public static int getIntSetting(String key) throws Exception {
        String val = getInstance().getSettingImpl(key);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new Exception("Value can not be parsed to int", e);
        }
    }

    public static boolean getBooleanSetting(String key) throws Exception {
        String val = getInstance().getSettingImpl(key);
        if (val == null) {
            throw new Exception("Value can not be parsed to Boolean");
        } 
        if (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false")) {
            throw new Exception("Value can not be parsed to Boolean");
        }
        return Boolean.parseBoolean(val);
    }

    public static boolean isSettingDefined(String key) throws Exception {
        return getInstance().properties.containsKey(key);
    }

    public static String getCredentialStoreKeyStorePath() throws Exception {
        String airavataConfigDir = getSetting(AIRAVATA_CONFIG_DIR);
        String credentialStoreKeyStorePath = getSetting("credential.store.keystore.url");
        return new File(airavataConfigDir, credentialStoreKeyStorePath).getAbsolutePath();
    }

    public static String getCredentialStoreKeyAlias() throws Exception {
        return getSetting("credential.store.keystore.alias");
    }

    public static String getCredentialStoreKeyStorePassword() throws Exception {
        return getSetting("credential.store.keystore.password");
    }

    public static String getCredentialStoreServerHost() throws Exception {
        return getSetting("credential.store.server.host");
    }

    public static String getCredentialStoreServerPort() throws Exception {
        return getSetting("credential.store.server.port");
    }

    public static String getRegistryServerPort() throws Exception {
        return getSetting("regserver.server.port");
    }

    public static String getRegistryServerHost() throws Exception {
        return getSetting("regserver.server.host");
    }

    public static String getSuperTenantGatewayId() throws Exception {
        return getSetting("super.tenant.gatewayId");
    }

    public static String getClusterStatusMonitoringRepeatTime() throws Exception {
        return getSetting("cluster.status.monitoring.repeat.time");
    }

    public static Boolean enableClusterStatusMonitoring() throws Exception {
        return getSetting("cluster.status.monitoring.enable").equalsIgnoreCase("true");
    }

    public static Boolean enableMetaschedulerJobScanning() throws Exception {
        return getSetting("metaschedluer.job.scanning.enable").equalsIgnoreCase("true");
    }

    public static Boolean enableDataAnalyzerJobScanning() throws Exception {
        return getSetting("data.analyzer.job.scanning.enable").equalsIgnoreCase("true");
    }

    public static String getProfileServiceServerHost() throws Exception {
        return getSetting(ServerSettings.PROFILE_SERVICE_SERVER_HOST);
    }

    public static String getProfileServiceServerPort() throws Exception {
        return getSetting(ServerSettings.PROFILE_SERVICE_SERVER_PORT);
    }

    public static String getIamServerUrl() throws Exception {
        return getSetting(ServerSettings.IAM_SERVER_URL);
    }

    public static void mergeSettingsCommandLineArgs(String[] args) {
        getInstance().mergeSettingsCommandLineArgsImpl(args);
    }

    public static ShutdownStrategy getShutdownStrategy() throws Exception {
        return getInstance().getShutdownStrategyImpl();
    }

    public static URL loadFile(String fileName) {

        if (System.getProperty(AIRAVATA_CONFIG_DIR) != null) {
            String airavataConfigDir = System.getProperty(AIRAVATA_CONFIG_DIR);
            try {
                airavataConfigDir = airavataConfigDir.endsWith(File.separator)
                        ? airavataConfigDir
                        : airavataConfigDir + File.separator;
                String filePath = airavataConfigDir + fileName;

                File asfile = new File(filePath);
                if (asfile.exists()) {

                    return asfile.toURI().toURL();
                }
            } catch (MalformedURLException e) {
                logger.error("Error parsing the file from airavata.config.dir: {}", airavataConfigDir);
            }
        }

        return ApplicationSettings.class.getClassLoader().getResource(fileName);
    }
}
