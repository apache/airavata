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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.StringUtil.CommandLineParameters;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
	private static List<IServer> servers;
	private static final String SERVERS_KEY="servers";
    private final static Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private static boolean serversLoaded=false;
	private static final String stopFileNamePrefix = "airavata-server-stop";
	private static int serverIndex=-1;
	private static final String serverStartedFileNamePrefix = "airavata-server-start";
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

	public static void main(String args[]) throws ParseException, IOException {
		CommandLineParameters commandLineParameters = StringUtil.getCommandLineParser(args);
		
		if (commandLineParameters.getArguments().contains("stop")){
			String serverIndexOption = "serverIndex";
			if (commandLineParameters.getParameters().containsKey(serverIndexOption)){
				serverIndex=Integer.parseInt(commandLineParameters.getParameters().get(serverIndexOption));
			}
			if (isServerRunning()) {
				logger.info("Requesting airavata server"+(serverIndex==-1? "(s)":" instance "+serverIndex)+" to stop...");
				requestStop();
				while(isServerRunning()){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				logger.info("Server"+(serverIndex==-1? "(s)":" instance "+serverIndex)+" stopped!!!");
			}else{
				logger.error("Server"+(serverIndex==-1? "":" instance "+serverIndex)+" is not running!!!");
			}
		}else{
			setServerStarted();
			logger.info("Airavata server instance "+serverIndex+" starting...");
			ServerSettings.mergeSettingsCommandLineArgs(args);
			startAllServers();
			while(!hasStopRequested()){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					stopAllServers();
				}
			}
			if (hasStopRequested()){
				stopAllServers();
				System.exit(0);
			}
		}
	}

	@SuppressWarnings("resource")
	private static void requestStop() throws IOException {
		//FIXME currently stop requests all the servers to stop
		File file = new File(getServerStopFileName());
		file.createNewFile();
		new RandomAccessFile(file, "rw").getChannel().lock();
		file.deleteOnExit();
	}
	
	private static boolean hasStopRequested(){
		return new File(getServerStopFileName()).exists() || new File(stopFileNamePrefix).exists(); 
	}

	private static String getServerStopFileName() {
		return (serverIndex==-1)?stopFileNamePrefix:stopFileNamePrefix+serverIndex;
	}

	private static boolean isServerRunning(){
		if (serverIndex==-1){
			String[] files = new File(".").list();
			for (String file : files) {
				if (file.contains(serverStartedFileNamePrefix)){
					return true;
				}
			}
			return false;
		}else{
			return new File(getServerStartedFileName()).exists();
		}
	}
	
	@SuppressWarnings({ "resource" })
	private static void setServerStarted(){
		try {
			File serverStartedFile = null;
			while(serverStartedFile==null || serverStartedFile.exists()){
				serverIndex++;
				serverStartedFile = new File(getServerStartedFileName());
			}
			serverStartedFile.createNewFile();
			serverStartedFile.deleteOnExit();
			new RandomAccessFile(serverStartedFile,"rw").getChannel().lock();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getServerStartedFileName() {
		return serverStartedFileNamePrefix+serverIndex;
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