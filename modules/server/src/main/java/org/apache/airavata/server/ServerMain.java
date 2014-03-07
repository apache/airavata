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

public class ServerMain {
	private static List<IServer> servers;
	private static final String SERVERS_KEY="servers";
	static {
		servers = new ArrayList<IServer>();
		try {
			String serversString = ServerSettings.getSetting(SERVERS_KEY);
			if (serversString!=null){
				String[] serversList = serversString.split(",");
				for (String serverString : serversList) {
					String serverClassName = ServerSettings.getSetting(serverString);
					Class<?> classInstance = ServerMain.class
                            .getClassLoader().loadClass(
                            		serverClassName);
					servers.add((IServer)classInstance.newInstance());
				}
			}
		} catch (ApplicationSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new Thread() {
			public void run() {
				startAllServers();
			}
		}.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				stopAllServers();
			}
		});
		try {
			while (true) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			stopAllServers();
		}
	}

	private static void stopAllServers() {
		for (IServer server : servers) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void startAllServers() {
		for (IServer server : servers) {
			try {
				server.start();
				server.waitForServerStart();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}