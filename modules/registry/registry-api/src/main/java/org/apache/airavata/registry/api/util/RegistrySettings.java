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

package org.apache.airavata.registry.api.util;

import java.net.URL;
import java.util.Properties;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.api.exception.RegistrySettingsException;
import org.apache.airavata.registry.api.exception.RegistrySettingsLoadException;
import org.apache.airavata.registry.api.exception.UnspecifiedRegistrySettingsException;

public class RegistrySettings {
    private static final String REPOSITORY_PROPERTIES = "registry.properties";
    private static final String SERVER_REPOSITORY_PROPERTIES = ServerSettings.REPOSITORY_PROPERTIES;
    private static final String CLIENT_REPOSITORY_PROPERTIES = "airavata-client.properties";
    private static Properties properties = new Properties();
    private static Exception propertyLoadException;
    private static final String REGISTRY_ACCESSOR_CLASS = "class.registry.accessor";
    
    static{
    	URL url = RegistrySettings.class.getClassLoader()
				.getResource(REPOSITORY_PROPERTIES);
		if (url == null) {
    		if (AiravataUtils.isServer()){
    			 url=RegistrySettings.class.getClassLoader().getResource(SERVER_REPOSITORY_PROPERTIES);
        	}else{
        		url=RegistrySettings.class.getClassLoader().getResource(CLIENT_REPOSITORY_PROPERTIES);
        	}
		}
        try {
            properties.load(url.openStream());
        } catch (Exception e) {
        	propertyLoadException=e;
        }
    }
    
    private static void validateSuccessfulPropertyFileLoad() throws RegistrySettingsException{
    	if (propertyLoadException!=null){
    		throw new RegistrySettingsLoadException(propertyLoadException);
    	}
    }
    
    public static String getSetting(String key) throws RegistrySettingsException{
    	validateSuccessfulPropertyFileLoad();
    	if (properties.containsKey(key)){
    		return properties.getProperty(key);
    	}
        return null;
//    	throw new UnspecifiedRegistrySettingsException(key);
    }
    
    public static String getSetting(String key, String defaultValue){
    	try {
			validateSuccessfulPropertyFileLoad();
			if (properties.containsKey(key)){
				return properties.getProperty(key);
			}
		} catch (RegistrySettingsException e) {
			//we'll ignore this error since a default value is provided
		}
		return defaultValue;
    }
    
    public static String getRegistryAccessorClass() throws RegistrySettingsException{
    	return getSetting(REGISTRY_ACCESSOR_CLASS);
    }
}
