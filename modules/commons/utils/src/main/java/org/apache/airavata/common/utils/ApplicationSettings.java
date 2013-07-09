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

package org.apache.airavata.common.utils;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.exception.ApplicationSettingsLoadException;
import org.apache.airavata.common.exception.ApplicationSettingsStoreException;
import org.apache.airavata.common.exception.UnspecifiedApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ApplicationSettings {
    public static final String SERVER_PROPERTIES="airavata-server.properties";
    public static final String CLIENT_PROPERTIES="airavata-client.properties";
	private static Properties properties = new Properties();
    private static Exception propertyLoadException;


    protected static final String TRUST_STORE_PATH="trust.store";
    protected static final String TRUST_STORE_PASSWORD="trust.store.password";

    private static final String REGULAR_EXPRESSION = "\\$\\{[a-zA-Z.-]*\\}";

    private final static Logger logger = LoggerFactory.getLogger(ApplicationSettings.class);

    static{
    	loadProperties();
    }

	private static void loadProperties() {
		URL url = getPropertyFileURL();
        try {
            properties.load(url.openStream());
        } catch (Exception e) {
        	propertyLoadException=e;
        }
	}

	private static URL getPropertyFileURL() {
		URL url;
		if (AiravataUtils.isServer()){
			 url=ApplicationSettings.class.getClassLoader().getResource(SERVER_PROPERTIES);
    	}else{
    		url=ApplicationSettings.class.getClassLoader().getResource(CLIENT_PROPERTIES);
    	}
		return url;
	}
	
	private static void saveProperties() throws ApplicationSettingsStoreException{
		URL url = getPropertyFileURL();
		if (url.getProtocol().equalsIgnoreCase("file")){
			try {
				properties.store(new FileOutputStream(url.getPath()), Calendar.getInstance().toString());
			} catch (Exception e) {
				throw new ApplicationSettingsStoreException(url.getPath(), e);
			}
		}else{
			logger.warn("Properties cannot be updated to location "+url.toString());
		}
	}
	
    private static void validateSuccessfulPropertyFileLoad() throws ApplicationSettingsException{
    	if (propertyLoadException!=null){
    		throw new ApplicationSettingsLoadException(propertyLoadException);
    	}
    }
    
    public static String getSetting(String key) throws ApplicationSettingsException{
    	validateSuccessfulPropertyFileLoad();
    	if (properties.containsKey(key)){
    		return properties.getProperty(key);
    	}
    	throw new UnspecifiedApplicationSettingsException(key);
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
     */
    public static String getAbsoluteSetting(String key) throws ApplicationSettingsException {

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
    
    public static String getSetting(String key, String defaultValue){
    	try {
			validateSuccessfulPropertyFileLoad();
			if (properties.containsKey(key)){
				return properties.getProperty(key);
			}
		} catch (ApplicationSettingsException e) {
			//we'll ignore this error since a default value is provided
		}
		return defaultValue;
    }
    
    public static void setSetting(String key, String value) throws ApplicationSettingsException{
    	properties.setProperty(key, value);
    	saveProperties();
    }
    
    public static boolean isSettingDefined(String key) throws ApplicationSettingsException{
    	validateSuccessfulPropertyFileLoad();
    	return properties.containsKey(key);
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

    public static Properties getProperties() {
        return properties;
    }
}
