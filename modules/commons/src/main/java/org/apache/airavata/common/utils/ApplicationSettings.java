/**
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
 */
package org.apache.airavata.common.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationSettings {
    public static final String SERVER_PROPERTIES="airavata-server.properties";
    public static final String AIRAVATA_CONFIG_DIR = "airavata.config.dir";

    public static String ADDITIONAL_SETTINGS_FILES = "external.settings";

	protected Properties properties = new Properties();

    private Exception propertyLoadException;


    protected static final String TRUST_STORE_PATH="trust.store";
    protected static final String TRUST_STORE_PASSWORD="trust.store.password";

    private static final String REGULAR_EXPRESSION = "\\$\\{[a-zA-Z.-]*\\}";

    private final static Logger logger = LoggerFactory.getLogger(ApplicationSettings.class);

    private static final String SHUTDOWN_STATEGY_STRING="shutdown.strategy";

    // ThriftClientPool Constants
    private static final String THRIFT_CLIENT_POOL_ABANDONED_REMOVAL_ENABLED = "thrift.client.pool.abandoned.removal.enabled";
    private static final String THRIFT_CLIENT_POOL_ABANDONED_REMOVAL_LOGGED = "thrift.client.pool.abandoned.removal.logged";
    
    protected static ApplicationSettings INSTANCE;
    public static enum ShutdownStrategy{
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
            logger.info("Settings loaded from "+url.toString());
            URL[] externalSettingsFileURLs = getExternalSettingsFileURLs();
            for (URL externalSettings : externalSettingsFileURLs) {
				mergeSettingsImpl(externalSettings.openStream());
				logger.info("External settings merged from "+url.toString());
			}
        } catch (Exception e) {
        	propertyLoadException=e;
        }
	}

	protected URL getPropertyFileURL() {
		return ApplicationSettings.loadFile(SERVER_PROPERTIES);
	}
	
	protected URL[] getExternalSettingsFileURLs(){
		try {
			List<URL> externalSettingsFileURLs=new ArrayList<URL>();
			String externalSettingsFileNames = getSettingImpl(ADDITIONAL_SETTINGS_FILES);
			String[] externalSettingFiles = externalSettingsFileNames.split(",");
			for (String externalSettingFile : externalSettingFiles) {
				URL externalSettingFileURL = ApplicationSettings.loadFile(externalSettingFile);
				if (externalSettingFileURL==null){
					logger.warn("Could not file external settings file "+externalSettingFile);
				}else{
					externalSettingsFileURLs.add(externalSettingFileURL);
				}
			}
			return externalSettingsFileURLs.toArray(new URL[]{});
		} catch (ApplicationSettingsException e) {
			return new URL[]{};
		}
	}
	protected static ApplicationSettings getInstance(){
		if (INSTANCE==null){
			INSTANCE=new ApplicationSettings();
		}
		return INSTANCE;
	}
	
	protected static void setInstance(ApplicationSettings settingsInstance){
		INSTANCE=settingsInstance;
	}
	
	private void saveProperties() throws ApplicationSettingsException{
		URL url = getPropertyFileURL();
		if (url.getProtocol().equalsIgnoreCase("file")){
			try {
				properties.store(new FileOutputStream(url.getPath()), Calendar.getInstance().toString());
			} catch (Exception e) {
				throw new ApplicationSettingsException(url.getPath(), e);
			}
		}else{
			logger.warn("Properties cannot be updated to location "+url.toString());
		}
	}
	
    private void validateSuccessfulPropertyFileLoad() throws ApplicationSettingsException{
    	if (propertyLoadException!=null){
    		throw new ApplicationSettingsException(propertyLoadException.getMessage(), propertyLoadException);
    	}
    }

    /**
     * Returns the configuration value relevant for the given key.
     * If configuration value contains references to other configuration values they will also
     * be replaced. E.g :- If configuration key reads http://${ip}:${port}/axis2/services/RegistryService?wsdl,
     * the variables ip and port will get replaced by their appropriated values in the configuration.
     * @param key The configuration key to read value of
     * @return The configuration value. For above example caller will get a value like
     * http://192.2.33.12:8080/axis2/services/RegistryService?wsdl
     * @throws ApplicationSettingsException If an error occurred while reading configurations.
     * @deprecated use #getSetting(String) instead
     */
    public String getAbsoluteSetting(String key) throws ApplicationSettingsException {

        String configurationValueWithVariables = ApplicationSettings.getSetting(key);

        List<String> variableList
                = getAllMatches(configurationValueWithVariables, REGULAR_EXPRESSION);

        if (variableList == null || variableList.isEmpty()) {
            return configurationValueWithVariables;
        }

        for(String variableIdentifier : variableList) {
            String variableName = getVariableNameOnly(variableIdentifier);
            String value = getAbsoluteSetting(variableName);

            configurationValueWithVariables = configurationValueWithVariables.replace(variableIdentifier, value);
        }

        return configurationValueWithVariables;

    }

    private static String getVariableNameOnly(String variableWithIdentifiers) {
        return variableWithIdentifiers.substring(2, (variableWithIdentifiers.length() - 1));
    }

    private static List<String> getAllMatches(String text, String regex) {
        List<String> matches = new ArrayList<String>();
        Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
        while(m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }
    
    public String getSettingImpl(String key) throws ApplicationSettingsException{
    	String rawValue;
    	if (System.getProperties().containsKey(key)) {
            rawValue = System.getProperties().getProperty(key);

        } else if (System.getenv().containsKey(key)) {
    	    rawValue = System.getenv().get(key);

    	} else {
    		validateSuccessfulPropertyFileLoad();
	    	if (properties.containsKey(key)){
	    		rawValue = properties.getProperty(key);
	    	}else{
	    		throw new ApplicationSettingsException(key);
	    	}
    	}
    	return deriveAbsoluteValueImpl(rawValue);
    }
    
    public String getSettingImpl(String key, String defaultValue){
    	try {
    		return getSettingImpl(key);
		} catch (ApplicationSettingsException e) {
			//we'll ignore this error since a default value is provided
		}
		return defaultValue;
    }

	private String deriveAbsoluteValueImpl(String property){
		if (property!=null){
			Map<Integer, String> containedParameters = StringUtil.getContainedParameters(property);
			List<String> parametersAlreadyProcessed=new ArrayList<String>();
			for (String parameter : containedParameters.values()) {
				if (!parametersAlreadyProcessed.contains(parameter)) {
					String parameterName = parameter.substring(2,parameter.length() - 1);
					String parameterValue = getSetting(parameterName,parameter);
					property = property.replaceAll(Pattern.quote(parameter), parameterValue);
					parametersAlreadyProcessed.add(parameter);
				}
			}
		}
		return property;
	}
    
    public void setSettingImpl(String key, String value) throws ApplicationSettingsException{
    	properties.setProperty(key, value);
    	saveProperties();
    }
    
    public boolean isSettingDefinedImpl(String key) throws ApplicationSettingsException{
    	validateSuccessfulPropertyFileLoad();
    	return properties.containsKey(key);
    }

    public String getTrustStorePathImpl() throws ApplicationSettingsException {
        return getSetting(TRUST_STORE_PATH);
    }

    public String getTrustStorePasswordImpl() throws ApplicationSettingsException {
        return getSetting(TRUST_STORE_PASSWORD);
    }

    public String getCredentialStoreKeyStorePathImpl() throws ApplicationSettingsException {
        return getSetting("credential.store.keystore.url");
    }

    public String getCredentialStoreKeyAliasImpl() throws ApplicationSettingsException {
        return getSetting("credential.store.keystore.alias");
    }

    public String getCredentialStoreKeyStorePasswordImpl() throws ApplicationSettingsException {
        return getSetting("credential.store.keystore.password");
    }

    public String getCredentialStoreNotifierEnabledImpl() throws ApplicationSettingsException {
        return getSetting("notifier.enabled");
    }

    public String getCredentialStoreNotifierDurationImpl() throws ApplicationSettingsException {
        return getSetting("notifier.duration");
    }

    public String getCredentialStoreEmailServerImpl() throws ApplicationSettingsException {
        return getSetting("email.server");
    }

    public String getCredentialStoreEmailServerPortImpl() throws ApplicationSettingsException {
        return getSetting("email.server.port");
    }

    public String getCredentialStoreEmailUserImpl() throws ApplicationSettingsException {
        return getSetting("email.user");
    }

    public String getCredentialStoreEmailPasswordImpl() throws ApplicationSettingsException {
        return getSetting("email.password");
    }

    public String getCredentialStoreEmailSSLConnectImpl() throws ApplicationSettingsException {
        return getSetting("email.ssl");
    }

    public String getCredentialStoreEmailFromEmailImpl() throws ApplicationSettingsException {
        return getSetting("email.from");
    }

    /**
     * @deprecated use {{@link #getSetting(String)}}
     * @return
     */
    public Properties getPropertiesImpl() {
        return properties;
    }
    
    public void mergeSettingsImpl(Map<String,String> props){
    	properties.putAll(props);
    }
    
    public void mergeSettingsImpl(InputStream stream) throws IOException{
    	Properties tmpProp = new Properties();
    	tmpProp.load(stream);
    	properties.putAll(tmpProp);
    }
    
    public void mergeSettingsCommandLineArgsImpl(String[] args){
    	properties.putAll(StringUtil.parseCommandLineOptions(args));
    }
 
    public ShutdownStrategy getShutdownStrategyImpl() throws Exception{
    	String strategy = null;
    	try {
			strategy = getSetting(SHUTDOWN_STATEGY_STRING, ShutdownStrategy.SELF_TERMINATE.toString());
			return ShutdownStrategy.valueOf(strategy);
		} catch (Exception e) {
			//if the string mentioned in config is invalid
			throw new Exception("Invalid shutdown strategy configured : "+strategy);
		}
    }
    
    /*
     * Static methods which will be used by the users
     */
    
    public static String getSetting(String key) throws ApplicationSettingsException {
    	return getInstance().getSettingImpl(key);
    }

    public static String getSetting(String key, String defaultValue) {
    	return getInstance().getSettingImpl(key,defaultValue);
    }
    
    public static void setSetting(String key, String value) throws ApplicationSettingsException{
        getInstance().properties.setProperty(key, value);
        getInstance().saveProperties();
    }


    public static int getIntSetting(String key) throws ApplicationSettingsException {
        String val = getInstance().getSettingImpl(key);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new ApplicationSettingsException("Value can not be parsed to int", e);
        }
    }

    public static boolean getBooleanSetting(String key) throws ApplicationSettingsException {
        String val = getInstance().getSettingImpl(key);
        return Optional.ofNullable(BooleanUtils.toBooleanObject(val))
                .orElseThrow(() -> new ApplicationSettingsException("Value can not be parsed to Boolean"));
    }

    public static long getLongSetting(String key) throws ApplicationSettingsException {
        String val = getInstance().getSettingImpl(key);
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            throw new ApplicationSettingsException("Value can not be parsed to long", e);
        }
    }

    public static double getDoubleSetting(String key) throws ApplicationSettingsException {
        String val = getInstance().getSettingImpl(key);
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new ApplicationSettingsException("Value can not be parsed to double", e);
        }
    }

    public static boolean isSettingDefined(String key) throws ApplicationSettingsException{
    	return getInstance().properties.containsKey(key);
    }

    public static boolean isTrustStorePathDefined() throws ApplicationSettingsException {
        return ApplicationSettings.isSettingDefined(TRUST_STORE_PATH);
    }

    public static String getTrustStorePath() throws ApplicationSettingsException {
        return getSetting(TRUST_STORE_PATH);
    }

    public static String getTrustStorePassword() throws ApplicationSettingsException {
        return getSetting(TRUST_STORE_PASSWORD);
    }

    public static void initializeTrustStore() throws ApplicationSettingsException {
        SecurityUtil.setTrustStoreParameters(getTrustStorePath(), getTrustStorePassword());
    }

    public static String getCredentialStoreKeyStorePath() throws ApplicationSettingsException {
        return getSetting("credential.store.keystore.url");
    }

    public static String getCredentialStoreKeyAlias() throws ApplicationSettingsException {
        return getSetting("credential.store.keystore.alias");
    }

    public static String getCredentialStoreKeyStorePassword() throws ApplicationSettingsException {
        return getSetting("credential.store.keystore.password");
    }

    public static String getCredentialStoreServerHost() throws ApplicationSettingsException {
        return getSetting("credential.store.server.host");
    }

    public static String getCredentialStoreServerPort() throws ApplicationSettingsException {
        return getSetting("credential.store.server.port");
    }
    public static String getCredentialStoreNotifierEnabled() throws ApplicationSettingsException {
        return getSetting("notifier.enabled");
    }

    public static String getCredentialStoreNotifierDuration() throws ApplicationSettingsException {
        return getSetting("notifier.duration");
    }

    public static String getCredentialStoreEmailServer() throws ApplicationSettingsException {
        return getSetting("email.server");
    }

    public static String getCredentialStoreEmailServerPort() throws ApplicationSettingsException {
        return getSetting("email.server.port");
    }

    public static String getCredentialStoreEmailUser() throws ApplicationSettingsException {
        return getSetting("email.user");
    }

    public static String getCredentialStoreEmailPassword() throws ApplicationSettingsException {
        return getSetting("email.password");
    }

    public static String getCredentialStoreEmailSSLConnect() throws ApplicationSettingsException {
        return getSetting("email.ssl");
    }

    public static String getCredentialStoreEmailFromEmail() throws ApplicationSettingsException {
        return getSetting("email.from");
    }

    public static String getRegistryServerPort() throws ApplicationSettingsException {
        return getSetting("regserver.server.port");
    }

    public static String getRegistryServerHost() throws ApplicationSettingsException {
        return getSetting("regserver.server.host");
    }

    public static String getSuperTenantGatewayId() throws ApplicationSettingsException {
        return getSetting("super.tenant.gatewayId");
    }

    public static String getClusterStatusMonitoringRepatTime() throws ApplicationSettingsException {
        return getSetting("cluster.status.monitoring.repeat.time");
    }

    public static Boolean enableClusterStatusMonitoring() throws ApplicationSettingsException {
        return getSetting("cluster.status.monitoring.enable").equalsIgnoreCase("true");
    }

    public static String getUserProfileServerHost() throws ApplicationSettingsException {
        return getSetting(ServerSettings.USER_PROFILE_SERVER_HOST);
    }

    public static String getUserProfileServerPort() throws ApplicationSettingsException {
        return getSetting(ServerSettings.USER_PROFILE_SERVER_PORT);
    }

    public static String getProfileServiceServerHost() throws ApplicationSettingsException {
        return getSetting(ServerSettings.PROFILE_SERVICE_SERVER_HOST);
    }

    public static String getProfileServiceServerPort() throws ApplicationSettingsException {
        return getSetting(ServerSettings.PROFILE_SERVICE_SERVER_PORT);
    }

    public static String getIamServerUrl() throws ApplicationSettingsException {
        return getSetting(ServerSettings.IAM_SERVER_URL);
    }

    public static boolean isThriftClientPoolAbandonedRemovalEnabled() {
        return Boolean.valueOf(getSetting(THRIFT_CLIENT_POOL_ABANDONED_REMOVAL_ENABLED, "false"));
    }

    public static boolean isThriftClientPoolAbandonedRemovalLogged() {
        return Boolean.valueOf(getSetting(THRIFT_CLIENT_POOL_ABANDONED_REMOVAL_LOGGED, "false"));
    }

    /**
     * @deprecated use {{@link #getSetting(String)}}
     * @return
     * @throws ApplicationSettingsException 
     */
    public static Properties getProperties() throws ApplicationSettingsException {
        return getInstance().properties;
    }
    
    public static void mergeSettings(Map<String,String> props) {
    	getInstance().mergeSettingsImpl(props);
    }
    
    public static void mergeSettings(InputStream stream) throws IOException{
    	getInstance().mergeSettingsImpl(stream);
    }
    
    public static void mergeSettingsCommandLineArgs(String[] args){
    	getInstance().mergeSettingsCommandLineArgsImpl(args);
    }
 
    public static ShutdownStrategy getShutdownStrategy() throws Exception{
    	return getInstance().getShutdownStrategyImpl();
    }

    public static URL loadFile(String fileName) {

        if(System.getProperty(AIRAVATA_CONFIG_DIR) != null) {
            String airavataConfigDir = System.getProperty(AIRAVATA_CONFIG_DIR);
            try {
                airavataConfigDir = airavataConfigDir.endsWith(File.separator) ? airavataConfigDir : airavataConfigDir + File.separator;
                String filePath = airavataConfigDir + fileName;

                File asfile  = new File(filePath);
                if (asfile.exists()) {

                    return asfile.toURI().toURL();
                }
            } catch (MalformedURLException e) {
                logger.error("Error parsing the file from airavata.config.dir", airavataConfigDir);
            }
        }

        return ApplicationSettings.class.getClassLoader().getResource(fileName);

    }
}
