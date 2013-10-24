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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegistryAccessorInstantiateException;
import org.apache.airavata.registry.api.exception.RegistryAccessorInvalidException;
import org.apache.airavata.registry.api.exception.RegistryAccessorNotFoundException;
import org.apache.airavata.registry.api.exception.RegistryAccessorUndefinedException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryUtils {
    private static final Logger log = LoggerFactory.getLogger(RegistryUtils.class);


    public static String validateAxisService(String urlString)throws RegistryException {
        if(!urlString.endsWith("?wsdl")){
            urlString = urlString + "?wsdl";
        }
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            // the URL is not in a valid form
            throw new RegistryException("Given Axis2 Service URL : " + urlString + " is Invalid",e);
        } catch (IOException e) {
            // the connection couldn't be established
            throw new RegistryException("Given Axis2 Service URL : " + urlString + " is Invalid",e);
        }
        return  urlString;
    }
    public static String validateURL(String urlString)throws RegistryException{
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            // the URL is not in a valid form
            throw new RegistryException("Given URL: " + urlString + " is Invalid",e);
        } catch (IOException e) {
            // the connection couldn't be established
            throw new RegistryException("Given URL: " + urlString + " is Invalid",e);
        }
        return  urlString;
    }
//    public static boolean validateRegistryCredentials(String userName,String password,String url)throws RegistryException{
//        HashMap<String, String> map = new HashMap<String, String>();
//        map.put("org.apache.jackrabbit.repository.uri", url);
//        try {
//            AiravataJCRRegistry airavataJCRRegistry = new AiravataJCRRegistry(new URI(url), "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory", userName, password, map);
//            airavataJCRRegistry.saveGFacDescriptor("dummy");
////            airavataJCRRegistry.deleteGFacDescriptor("dummy");
//        } catch (Exception e) {
//            throw new RegistryException("Check the properties file for JCR Registry Configuration",e);
//        }
//        return true;
//    }

    public static AiravataRegistry2 getRegistryFromServerSettings() {
        String username = "";
//        Properties properties = new Properties();
        AiravataRegistry2 registry = null;
//        try {
////            properties.load(url.openStream());
//        	username=ServerSettings.getSystemUser();
////            if (properties.get(REGISTRY_USER) != null) {
////                username = (String) properties.get(REGISTRY_USER);
////            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
        	username=ServerSettings.getSystemUser();
            registry = AiravataRegistryFactory.getRegistry(new Gateway(ServerSettings.getSystemUserGateway()),
                    new AiravataUser(username));
        } catch (AiravataConfigurationException e) {
            log.error("Error initializing AiravataRegistry2");
        } catch (RegistryAccessorNotFoundException e) {
            log.error("Error initializing AiravataRegistry2");
        } catch (RegistryAccessorInstantiateException e) {
            log.error("Error initializing AiravataRegistry2");
        } catch (RegistryAccessorInvalidException e) {
            log.error("Error initializing AiravataRegistry2");
        } catch (RegistryAccessorUndefinedException e) {
            log.error("Error initializing AiravataRegistry2");
        } catch (ApplicationSettingsException e) {
        	log.error("Error initializing AiravataRegistry2",e);
		} catch (RegistryException e) {
            log.error("Error initializing AiravataRegistry2",e);
        }
        return registry;
    }
}
