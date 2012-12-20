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
package org.apache.airavata.core.gfac.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.exception.ServerSettingsException;
import org.apache.airavata.common.exception.UnspecifiedServerSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.core.gfac.external.GridConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacConfiguration {
    public static final Logger log = LoggerFactory.getLogger(GFacConfiguration.class);

    private String myProxyServer;

    private String myProxyUser;

    private String myProxyPassphrase;

    private int myProxyLifeCycle;


    private AiravataAPI airavataAPI;

    private String trustedCertLocation;
    
    private static List<GridConfigurationHandler> gridConfigurationHandlers;
    private static String GRID_HANDLERS="airavata.grid.handlers";
    
    static{
    	gridConfigurationHandlers=new ArrayList<GridConfigurationHandler>();
    	try {
			String handlerString = ServerSettings.getSetting(GRID_HANDLERS);
			String[] handlers = handlerString.split(",");
			for (String handlerClass : handlers) {
				try {
					@SuppressWarnings("unchecked")
					Class<GridConfigurationHandler> classInstance = (Class<GridConfigurationHandler>) GFacConfiguration.class
							.getClassLoader().loadClass(handlerClass);
					gridConfigurationHandlers.add(classInstance.newInstance());
				} catch (Exception e) {
					log.error("Error while loading Grid Configuration Handler class "+handlerClass, e);
				}
			}
		} catch (UnspecifiedServerSettingsException e) {
			//no handlers defined
		} catch (ServerSettingsException e1) {
			log.error("Error in reading Configuration handler data!!!",e1);
		}
    }
    
    public static GridConfigurationHandler[] getGridConfigurationHandlers(){
    	return gridConfigurationHandlers.toArray(new GridConfigurationHandler[]{});
    }

    public GFacConfiguration(String myProxyServer,
                             String myProxyUser,
                             String myProxyPassphrase,
                             int myProxyLifeCycle,
                             AiravataAPI airavataAPI,
                             String trustedCertLocation) {
        this.myProxyServer = myProxyServer;
        this.myProxyUser = myProxyUser;
        this.myProxyPassphrase = myProxyPassphrase;
        this.myProxyLifeCycle = myProxyLifeCycle;
        this.airavataAPI = airavataAPI;
        this.trustedCertLocation = trustedCertLocation;
    }

    public String getMyProxyServer() {
        return myProxyServer;
    }

    public String getMyProxyUser() {
        return myProxyUser;
    }

    public String getMyProxyPassphrase() {
        return myProxyPassphrase;
    }

    public int getMyProxyLifeCycle() {
        return myProxyLifeCycle;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public String getTrustedCertLocation() {
        return trustedCertLocation;
    }
}
