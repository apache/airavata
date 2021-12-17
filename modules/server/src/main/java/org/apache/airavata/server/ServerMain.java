/**
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
 */
package org.apache.airavata.server;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.*;
import org.apache.airavata.common.utils.ApplicationSettings.ShutdownStrategy;
import org.apache.airavata.common.utils.IServer.ServerStatus;
import org.apache.airavata.common.utils.StringUtil.CommandLineParameters;
import org.apache.airavata.patform.monitoring.MonitoringServer;
import org.apache.commons.cli.ParseException;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerMain {
	private static List<IServer> servers;
	private static final String SERVERS_KEY="servers";
    private final static Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private static boolean serversLoaded=false;
	private static final String stopFileNamePrefix = "server_stop";
	private static long serverPID=-1;
	private static final String serverStartedFileNamePrefix = "server_start";
	private static boolean systemShutDown=false;
	private static String STOP_COMMAND_STR = "stop";

	private static final String ALL_IN_ONE = "all";
	private static final String API_ORCH = "api-orch";
	private static final String EXECUTION = "execution";
	// server names
	private static final String API_SERVER = "apiserver";
	private static final String CREDENTIAL_STORE = "credentialstore";
	private static final String REGISTRY_SERVER = "regserver";
	private static final String SHARING_SERVER = "sharing_server";
	private static final String GFAC_SERVER = "gfac";
	private static final String ORCHESTRATOR = "orchestrator";
	private static final String PROFILE_SERVICE = "profile_service";
	private static final String DB_EVENT_MANAGER = "db_event_manager";

    private static ServerCnxnFactory cnxnFactory;
//	private static boolean shutdownHookCalledBefore=false;
    static{
		servers = new ArrayList<IServer>();
    }
    
	private static void loadServers(String serverNames) {
		try {
			if (serverNames != null) {
				List<String> serversList = handleServerDependencies(serverNames);
				for (String serverString : serversList) {
					serverString = serverString.trim();
					String serverClassName = ServerSettings.getSetting(serverString);
					Class<?> classInstance;
					try {
						classInstance = ServerMain.class
								.getClassLoader().loadClass(
										serverClassName);
						servers.add((IServer) classInstance.newInstance());
					} catch (ClassNotFoundException e) {
						logger.error("Error while locating server implementation \"" + serverString + "\"!!!", e);
					} catch (InstantiationException e) {
						logger.error("Error while initiating server instance \"" + serverString + "\"!!!", e);
					} catch (IllegalAccessException e) {
						logger.error("Error while initiating server instance \"" + serverString + "\"!!!", e);
					} catch (ClassCastException e) {
						logger.error("Invalid server \"" + serverString + "\"!!!", e);
					}
				}
			} else {
				logger.warn("No server name specify to start, use -h command line option to view help menu ...");
			}
		} catch (ApplicationSettingsException e) {
			logger.error("Error while retrieving server list!!!",e);
		}
		serversLoaded=true;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				setSystemShutDown(true);
				stopAllServers();
			}
		});
	}

	private static List<String> handleServerDependencies(String serverNames) {
		List<String> serverList = new ArrayList<>(Arrays.asList(serverNames.split(",")));
		if (serverList.indexOf(ALL_IN_ONE) > -1) {
			serverList.clear();
			serverList.add(DB_EVENT_MANAGER); // DB Event Manager should start before everything
			serverList.add(REGISTRY_SERVER);  // registry server should start before everything else
			serverList.add(CREDENTIAL_STORE); // credential store should start before api server
			serverList.add(SHARING_SERVER);
			serverList.add(API_SERVER);
			serverList.add(ORCHESTRATOR);
			serverList.add(GFAC_SERVER);
			serverList.add(PROFILE_SERVICE);
		} else if (serverList.indexOf(API_ORCH) > -1) {
			serverList.clear();
			serverList.add(DB_EVENT_MANAGER); // DB Event Manager should start before everything
			serverList.add(REGISTRY_SERVER);  // registry server should start before everything else
            serverList.add(CREDENTIAL_STORE); // credential store should start before api server
			serverList.add(SHARING_SERVER);
			serverList.add(API_SERVER);
			serverList.add(ORCHESTRATOR);
			serverList.add(PROFILE_SERVICE);
		} else if (serverList.indexOf(EXECUTION) > -1) {
			serverList.clear();
			serverList.add(GFAC_SERVER);
		} else {
			// registry server should start before everything
			int regPos = serverList.indexOf(REGISTRY_SERVER);
			if (regPos > 0) {
				String temp = serverList.get(0);
				serverList.set(0, serverList.get(regPos));
				serverList.set(regPos, temp);
			}

			// credential store should start before api server
			int credPos = serverList.indexOf(CREDENTIAL_STORE);
			int apiPos = serverList.indexOf(API_SERVER);
			if (credPos >= 0 && apiPos >= 0 && (credPos > apiPos)) {
				String temp = serverList.get(apiPos);
				serverList.set(apiPos, serverList.get(credPos));
				serverList.set(credPos, temp);
			}
		}
		return serverList;
	}

//	private static void addSecondaryShutdownHook(){
//		Runtime.getRuntime().addShutdownHook(new Thread(){
//			@Override
//			public void run() {
//				System.out.print("Graceful shutdown attempt is still active. Do you want to exit instead? (y/n)");
//				String command=System.console().readLine().trim().toLowerCase();
//				if (command.equals("yes") || command.equals("y")){
//					System.exit(1);
//				}


//			}
//		});
//	}
	
	public static void main(String args[]) throws IOException, AiravataException, ParseException {
		ServerSettings.mergeSettingsCommandLineArgs(args);
		ServerSettings.setServerRoles(ApplicationSettings.getSetting(SERVERS_KEY, "all").split(","));

		if (ServerSettings.getBooleanSetting("api.server.monitoring.enabled")) {
			MonitoringServer monitoringServer = new MonitoringServer(
					ServerSettings.getSetting("api.server.monitoring.host"),
					ServerSettings.getIntSetting("api.server.monitoring.port"));
			monitoringServer.start();

			Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
		}

		CommandLineParameters commandLineParameters = StringUtil.getCommandLineParser(args);
        if (commandLineParameters.getArguments().contains(STOP_COMMAND_STR)){
            performServerStopRequest(commandLineParameters);
        } else {
			performServerStart(args);
		}
    }



    private static void performServerStart(String[] args) {
		setServerStarted();
		logger.info("Airavata server instance starting...");
		for (String string : args) {
			logger.info("Server Arguments: " + string);
		}
		String serverNames;
		try {
			serverNames = ApplicationSettings.getSetting(SERVERS_KEY);
			startAllServers(serverNames);
		} catch (ApplicationSettingsException e1) {
			logger.error("Error finding servers property");
		}
		while(!hasStopRequested()){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				stopAllServers();
			}
		}
		if (hasStopRequested()){
            ServerSettings.setStopAllThreads(true);
			stopAllServers();
			ShutdownStrategy shutdownStrategy;
			try {
				shutdownStrategy = ServerSettings.getShutdownStrategy();
			} catch (Exception e) {
				String strategies="";
				for(ShutdownStrategy s:ShutdownStrategy.values()){
					strategies+="/"+s.toString();
				}
				logger.warn(e.getMessage());
				logger.warn("Valid shutdown options are : "+strategies.substring(1));
				shutdownStrategy=ShutdownStrategy.SELF_TERMINATE;
			}
			if (shutdownStrategy==ShutdownStrategy.SELF_TERMINATE) {
				System.exit(0);
			}
		}
	}

	private static void performServerStopRequest(
			CommandLineParameters commandLineParameters) throws IOException {
//		deleteOldStartRecords();
		String serverIndexOption = "serverIndex";
		if (commandLineParameters.getParameters().containsKey(serverIndexOption)){
			serverPID=Integer.parseInt(commandLineParameters.getParameters().get(serverIndexOption));
		}
		if (isServerRunning()) {
			logger.info("Requesting airavata server"+(serverPID==-1? "(s)":" instance "+serverPID)+" to stop...");
			requestStop();
			while(isServerRunning()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
			logger.info("Server"+(serverPID==-1? "(s)":" instance "+serverPID)+" stopped!!!");
		}else{
			logger.error("Server"+(serverPID==-1? "":" instance "+serverPID)+" is not running!!!");
		}
        if (ServerSettings.isEmbeddedZK()) {
            cnxnFactory.shutdown();
        }
    }

	@SuppressWarnings("resource")
	private static void requestStop() throws IOException {
		File file = new File(getServerStopFileName());
		file.createNewFile();
		new RandomAccessFile(file, "rw").getChannel().lock();
		file.deleteOnExit();
	}
	
	private static boolean hasStopRequested(){
		return  isSystemShutDown() || new File(getServerStopFileName()).exists() || new File(stopFileNamePrefix).exists(); 
	}

	private static String getServerStopFileName() {
		return stopFileNamePrefix;
	}
	
	private static void deleteOldStopRequests(){
		File[] files = new File(".").listFiles();
		for (File file : files) {
			if (file.getName().contains(stopFileNamePrefix)){
				file.delete();
			}
		}
	}
	
//	private static void deleteOldStartRecords(){
//		File[] files = new File(".").listFiles();
//		for (File file : files) {
//			if (file.getName().contains(serverStartedFileNamePrefix)){
//				try {
//					new FileOutputStream(file);
//					file.delete();
//				} catch (Exception e) {
//					//file is locked which means there's an active process using it
//				}
//			}
//		}
//	}
	
	private static boolean isServerRunning(){
		if (serverPID==-1){
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
			serverPID = getPID();
			deleteOldStopRequests();
			File serverStartedFile = null;
			serverStartedFile = new File(getServerStartedFileName());
			serverStartedFile.createNewFile();
			serverStartedFile.deleteOnExit();
			new RandomAccessFile(serverStartedFile,"rw").getChannel().lock();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
            logger.error(e.getMessage(), e);
		}
	}

	private static String getServerStartedFileName() {
		return new File(new File(System.getenv("AIRAVATA_HOME"),"bin"),serverStartedFileNamePrefix+"_"+Long.toString(serverPID)).toString();
	}

	public static void stopAllServers() {
		//stopping should be done in reverse order of starting the servers
		for(int i=servers.size()-1;i>=0;i--){
			try {
				servers.get(i).stop();
				waitForServerToStop(servers.get(i),null);
			} catch (Exception e) {
				logger.error("Server Stop Error:",e);
			}
		}
	}

	public static void startAllServers(String serversNames) {
		if (!serversLoaded){
			loadServers(serversNames);
		}
		for (IServer server : servers) {
			try {
				server.configure();
				server.start();
				waitForServerToStart(server,null);
			} catch (Exception e) {
				logger.error("Server Start Error:",e);
			}
		}
	}
	private static final int SERVER_STATUS_CHANGE_WAIT_INTERVAL=500;

	private static void waitForServerToStart(IServer server,Integer maxWait) throws Exception {
		int count=0;
//		if (server.getStatus()==ServerStatus.STARTING) {
//			logger.info("Waiting for " + server.getName() + " to start...");
//		}
		while(server.getStatus()==ServerStatus.STARTING && (maxWait==null || count<maxWait)){
			Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
			count+=SERVER_STATUS_CHANGE_WAIT_INTERVAL;
		}
		if (server.getStatus()!= ServerStatus.STARTED){
			logger.error("The "+server.getName()+" did not start!!!");
		}
	}

	private static void waitForServerToStop(IServer server,Integer maxWait) throws Exception {
		int count=0;
		if (server.getStatus()==ServerStatus.STOPING) {
			logger.info("Waiting for " + server.getName() + " to stop...");
		}
		//we are doing hasStopRequested() check because while we are stuck in the loop to stop there could be a forceStop request 
		while(server.getStatus()==ServerStatus.STOPING && (maxWait==null || count<maxWait)){
			Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
			count+=SERVER_STATUS_CHANGE_WAIT_INTERVAL;
		}
		if (server.getStatus()!=ServerStatus.STOPPED){
            logger.error("Error stopping the "+server.getName()+"!!!");
		}
	}

	private static boolean isSystemShutDown() {
		return systemShutDown;
	}

	private static void setSystemShutDown(boolean systemShutDown) {
		ServerMain.systemShutDown = systemShutDown;
	}
	
//	private static int getPID(){
//		try {
//			java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory
//					.getRuntimeMXBean();
//			java.lang.reflect.Field jvm = runtime.getClass()
//					.getDeclaredField("jvm");
//			jvm.setAccessible(true);
//			sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm
//					.get(runtime);
//			java.lang.reflect.Method pid_method = mgmt.getClass()
//					.getDeclaredMethod("getProcessId");
//			pid_method.setAccessible(true);
//
//			int pid = (Integer) pid_method.invoke(mgmt);
//			return pid;
//		} catch (Exception e) {
//			return -1;
//		}
//	}

	//getPID from ProcessHandle JDK 9 and onwards
	private static long getPID () {
		try {
			return ProcessHandle.current().pid();
		} catch (Exception e) {
			return -1;
		}

	}



}