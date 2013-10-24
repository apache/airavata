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

import java.util.ArrayList;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http11.Http11Protocol;

public class ServerSettings extends ApplicationSettings{
    private static final String SYSTEM_USER="system.user";
    private static final String SYSTEM_USER_PASSWORD="system.password";
    private static final String SYSTEM_USER_GATEWAY="system.gateway";
    private static final String TOMCAT_PORT = "port";
    private static final String SERVER_CONTEXT_ROOT="server.context-root";
    private static String tomcatPort=null;

    private static final String CREDENTIAL_STORE_DB_URL ="credential.store.jdbc.url";
    private static final String CREDENTIAL_STORE_DB_USER ="credential.store.jdbc.user";
    private static final String CREDENTIAL_STORE_DB_PASSWORD ="credential.store.jdbc.password";
    private static final String CREDENTIAL_STORE_DB_DRIVER ="credential.store.jdbc.driver";

    private static final String REGISTRY_DB_URL ="registry.jdbc.url";
    private static final String REGISTRY_DB_USER ="registry.jdbc.user";
    private static final String REGISTRY_DB_PASSWORD ="registry.jdbc.password";
    private static final String REGISTRY_DB_DRIVER ="registry.jdbc.driver";
    private static final String ENABLE_HTTPS = "enable.https";
    private static final String HOST_SCHEDULER = "host.scheduler";

    public static String getSystemUser() throws ApplicationSettingsException{
    	return getSetting(SYSTEM_USER);
    }
    
    public static String getSystemUserPassword() throws ApplicationSettingsException{
    	return getSetting(SYSTEM_USER_PASSWORD);
    }
    
    public static String getSystemUserGateway() throws ApplicationSettingsException{
    	return getSetting(SYSTEM_USER_GATEWAY);
    }

    public static String getServerContextRoot(){
    	return getSetting(SERVER_CONTEXT_ROOT,"axis2");
    }

    public static String getCredentialStoreDBUser() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_USER);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_USER);
        }
    }

    public static String getCredentialStoreDBPassword() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_PASSWORD);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_PASSWORD);
        }
    }

    public static String getCredentialStoreDBDriver() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_DRIVER);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_DRIVER);
        }
    }

    public static String getCredentialStoreDBURL() throws ApplicationSettingsException {
        try {
            return getSetting(CREDENTIAL_STORE_DB_URL);
        } catch (ApplicationSettingsException e) {
            return getSetting(REGISTRY_DB_URL);
        }

    }

    public static boolean isEnableHttps() {
        try {
            return Boolean.parseBoolean(getSetting(ENABLE_HTTPS));
        } catch (ApplicationSettingsException e) {
            return false;
        }
    }

    public static String getTomcatPort(String protocol) throws ApplicationSettingsException {
    	if (tomcatPort==null) {
			try {
				//First try to get the port from a tomcat if it is already running
				ArrayList<MBeanServer> mBeanServers = MBeanServerFactory
						.findMBeanServer(null);
				if (mBeanServers.size() > 0) {
					MBeanServer mBeanServer = mBeanServers.get(0);
					Server server = null;
					String[] domains = mBeanServer.getDomains();
					for (String domain : domains) {
						try {
							server = (Server) mBeanServer.getAttribute(
									new ObjectName(domain, "type", "Server"),
									"managedResource");
							break;
						} catch (InstanceNotFoundException e) {
							//ignore
						}
					}
					if (server != null) {
						Service[] findServices = server.findServices();
						for (Service service : findServices) {
							for (Connector connector : service.findConnectors()) {
								ProtocolHandler protocolHandler = connector.getProtocolHandler();
                                if(protocol != null && protocol.equals(connector.getScheme())){
								if (protocolHandler instanceof Http11Protocol
										|| protocolHandler instanceof Http11AprProtocol
										|| protocolHandler instanceof Http11NioProtocol) {
									Http11Protocol p = (Http11Protocol) protocolHandler;
									if (p.getSslImplementationName() == null
											|| p.getSslImplementationName()
													.equals("")) {
										tomcatPort = String.valueOf(connector
												.getPort());
									}
                                }
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//if unable to determine the server port from a running tomcat server, get it from 
			//the server settings file
			if (tomcatPort == null) {
				tomcatPort = getSetting(TOMCAT_PORT);
			}
		}
		return tomcatPort;
    }

    public static String getHostScheduler() throws ApplicationSettingsException {
        return getSetting(HOST_SCHEDULER);
    }
}
