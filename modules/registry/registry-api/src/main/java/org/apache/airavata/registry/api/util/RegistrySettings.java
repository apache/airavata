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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.registry.api.exception.RegistrySettingsException;
import org.apache.airavata.registry.api.exception.RegistrySettingsLoadException;

public class RegistrySettings {
    private static final String REPOSITORY_PROPERTIES = "registry.properties";
    private static Properties properties = new Properties();
    private static Exception propertyLoadException;
    private static final String REGISTRY_ACCESSOR_CLASS = "class.registry.accessor";
    private static final String SAVE_APPLICATION_JOB_STATUS_HISTORY="enable.application.job.status.history";
    
    static{
    	URL url = RegistrySettings.class.getClassLoader()
				.getResource(REPOSITORY_PROPERTIES);
		if (url == null) {
    		if (AiravataUtils.isServer()){
    			 url=RegistrySettings.class.getClassLoader().getResource(ApplicationSettings.SERVER_PROPERTIES);
                if(url == null){
                    url=RegistrySettings.class.getClassLoader().getResource(ApplicationSettings.CLIENT_PROPERTIES);
                }
    		}else if (AiravataUtils.isClient()){
        		url=RegistrySettings.class.getClassLoader().getResource(ApplicationSettings.CLIENT_PROPERTIES);
                if(url == null){
                    url=RegistrySettings.class.getClassLoader().getResource(ApplicationSettings.SERVER_PROPERTIES);
                }
        	}else{
        		//unknown execution mode... If so, first assume its client, if not server...
        		url=RegistrySettings.class.getClassLoader().getResource(ApplicationSettings.CLIENT_PROPERTIES);
        		if (url==null){
        			url=RegistrySettings.class.getClassLoader().getResource(ApplicationSettings.SERVER_PROPERTIES);
        		}
        	}
		}
        try {
            properties.load(url.openStream());
        } catch (Exception e) {
        	propertyLoadException=e;
        }
        System.out.println(url);
    }
    
    private static void validateSuccessfulPropertyFileLoad() throws RegistrySettingsException{
    	if (propertyLoadException!=null){
    		throw new RegistrySettingsLoadException(propertyLoadException);
    	}
    }
    
    public static String getSetting(String key) throws RegistrySettingsException{
    	String rawValue=null;
    	if (System.getProperties().containsKey(key)){
    		rawValue=System.getProperties().getProperty(key);
    	}else{
    		validateSuccessfulPropertyFileLoad();
	    	if (properties.containsKey(key)){
	    		rawValue=properties.getProperty(key);
	    	}else{
	    		return null;		
	    	}
    	}
    	return deriveAbsoluteValueImpl(rawValue);
//    	throw new UnspecifiedRegistrySettingsException(key);
    }
    
	private static String deriveAbsoluteValueImpl(String property){
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
    public static String getSetting(String key, String defaultValue){
    	try {
    		return getSetting(key);
		} catch (RegistrySettingsException e) {
			//we'll ignore this error since a default value is provided
		}
		return defaultValue;
    }
    
    public static String getRegistryAccessorClass() throws RegistrySettingsException{
    	return getSetting(REGISTRY_ACCESSOR_CLASS);
    }
    
    public static boolean isApplicationJobStatusHistoryEnabled(){
    	return "true".equalsIgnoreCase(getSetting(SAVE_APPLICATION_JOB_STATUS_HISTORY, "false"));
    }
}
