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

import java.net.URL;
import java.util.Properties;

import org.apache.airavata.common.exception.ServerSettingsException;
import org.apache.airavata.common.exception.ServerSettingsLoadException;
import org.apache.airavata.common.exception.UnspecifiedServerSettingsException;

public class ServerSettings {
    public static final String REPOSITORY_PROPERTIES = "airavata-server.properties";
    private static Properties properties = new Properties();
    private static Exception propertyLoadException;
    private static final String DEFAULT_GATEWAY_ID="gateway.id";
    private static final String SYSTEM_USER="system.user";
    private static final String SYSTEM_USER_PASSWORD="system.password";
    private static final String SYSTEM_USER_GATEWAY="system.gateway";
    private static final String TOMCAT_PORT = "port";


    static{
    	URL url = ServerSettings.class.getClassLoader()
				.getResource(REPOSITORY_PROPERTIES);
        try {
            properties.load(url.openStream());
        } catch (Exception e) {
        	propertyLoadException=e;
        }
    }
    
    private static void validateSuccessfulPropertyFileLoad() throws ServerSettingsException{
    	if (propertyLoadException!=null){
    		throw new ServerSettingsLoadException(propertyLoadException);
    	}
    }
    
    public static String getSetting(String key) throws ServerSettingsException{
    	validateSuccessfulPropertyFileLoad();
    	if (properties.containsKey(key)){
    		return properties.getProperty(key);
    	}
    	throw new UnspecifiedServerSettingsException(key);
    }
    
    public static String getSetting(String key, String defaultValue){
    	try {
			validateSuccessfulPropertyFileLoad();
			if (properties.containsKey(key)){
				return properties.getProperty(key);
			}
		} catch (ServerSettingsException e) {
			//we'll ignore this error since a default value is provided
		}
		return defaultValue;
    }
    
    public static String getDefaultGatewayId()throws ServerSettingsException{
    	return getSetting(DEFAULT_GATEWAY_ID);
    }
    
    public static String getSystemUser() throws ServerSettingsException{
    	return getSetting(SYSTEM_USER);
    }
    
    public static String getSystemUserPassword() throws ServerSettingsException{
    	return getSetting(SYSTEM_USER_PASSWORD);
    }
    
    public static String getSystemUserGateway() throws ServerSettingsException{
    	return getSetting(SYSTEM_USER_GATEWAY);
    }

    public static String getTomcatPort() {
        return properties.getProperty(TOMCAT_PORT);
    }
}
