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
package org.apache.airavata.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
	private static List<IServer> servers;
	private static final String SERVERS_KEY="servers";
    private final static Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private static boolean serversLoaded=false;
    
    static{
		servers = new ArrayList<IServer>();
    }
    
	private static void loadServers() {
		try {
			String serversString = ServerSettings.getSetting(SERVERS_KEY);
			if (serversString!=null){
				String[] serversList = serversString.split(",");
				for (String serverString : serversList) {
					String serverClassName = ServerSettings.getSetting(serverString);
					Class<?> classInstance;
					try {
						classInstance = ServerMain.class
						        .getClassLoader().loadClass(
						        		serverClassName);
						servers.add((IServer)classInstance.newInstance());
					} catch (ClassNotFoundException e) {
						logger.error("Error while locating server implementation \""+serverString+"\"!!!",e);
					} catch (InstantiationException e) {
						logger.error("Error while initiating server instance \""+serverString+"\"!!!",e);
					} catch (IllegalAccessException e) {
						logger.error("Error while initiating server instance \""+serverString+"\"!!!",e);
					} catch (ClassCastException e){
						logger.error("Invalid server \""+serverString+"\"!!!",e);
					}
				}
			}
		} catch (ApplicationSettingsException e) {
			logger.error("Error while retrieving server list!!!",e);
		}
		serversLoaded=true;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				stopAllServers();
			}
		});
	}

	public static void main(String args[]) {
		ServerSettings.mergeSettingsCommandLineArgs(args);
		startAllServers();
	}

	public static void stopAllServers() {
		//stopping should be done in reverse order of starting the servers
		for(int i=servers.size()-1;i>=0;i--){
			try {
				servers.get(i).stop();
				servers.get(i).waitForServerToStop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void startAllServers() {
		if (!serversLoaded){
			loadServers();
		}
		for (IServer server : servers) {
			try {
				server.start();
				server.waitForServerToStart();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}