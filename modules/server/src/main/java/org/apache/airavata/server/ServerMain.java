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
import org.apache.airavata.common.utils.ApplicationSettings.ShutdownStrategy;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.IServer.ServerStatus;
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
	private static final String stopFileNamePrefixForced = "airavata-server-stop-forced";
	private static int serverIndex=-1;
	private static final String serverStartedFileNamePrefix = "airavata-server-start";
	private static boolean forcedStop=false; 
	private static boolean systemShutDown=false;
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
				setSystemShutDown(true);
				stopAllServers();
			}
		});
	}

	public static void main(String args[]) throws ParseException, IOException {
		CommandLineParameters commandLineParameters = StringUtil.getCommandLineParser(args);
		if (commandLineParameters.getArguments().contains("stop")){
			performServerStopRequest(commandLineParameters);
		}else{
			performServerStart(args);
		}
	}

	private static void performServerStart(String[] args) {
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
		deleteOldStartRecords();
		String serverIndexOption = "serverIndex";
		if (commandLineParameters.getParameters().containsKey(serverIndexOption)){
			serverIndex=Integer.parseInt(commandLineParameters.getParameters().get(serverIndexOption));
		}
		String serverForcedStop = "force";
		if (commandLineParameters.getParameters().containsKey(serverForcedStop)){
			forcedStop=true;
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
	}

	@SuppressWarnings("resource")
	private static void requestStop() throws IOException {
		File file = new File(getServerStopFileName());
		file.createNewFile();
		new RandomAccessFile(file, "rw").getChannel().lock();
		file.deleteOnExit();
		if (forcedStop){ 
			// incase a previous attempt of stopping without forcing is present, best to delete that file
			File f=new File((serverIndex==-1)? stopFileNamePrefix:stopFileNamePrefix+serverIndex);
			if (f.exists()){
				f.deleteOnExit();
			}
		}
	}
	
	private static boolean hasStopRequested(){
		forcedStop=new File(getServerStopForceFileName()).exists() || new File(stopFileNamePrefixForced).exists();
		return  isSystemShutDown() || forcedStop || new File(getServerStopFileName()).exists() || new File(stopFileNamePrefix).exists(); 
	}

	private static String getServerStopFileName() {
		String filePrefix = forcedStop? stopFileNamePrefixForced : stopFileNamePrefix;
		return (serverIndex==-1)? filePrefix:filePrefix+serverIndex;
	}
	
	private static String getServerStopForceFileName() {
		return (serverIndex==-1)?stopFileNamePrefixForced:stopFileNamePrefixForced+serverIndex;
	}

	private static void deleteOldStopRequests(){
		File[] files = new File(".").listFiles();
		for (File file : files) {
			if (file.getName().contains(stopFileNamePrefix) || file.getName().contains(stopFileNamePrefixForced)){
				try {
					file.delete();
				} catch (Exception e) {
					//file is locked which means there's an active process using it
				}
			}
		}
	}
	
	private static void deleteOldStartRecords(){
		File[] files = new File(".").listFiles();
		for (File file : files) {
			if (file.getName().contains(serverStartedFileNamePrefix)){
				try {
					file.delete();
				} catch (Exception e) {
					//file is locked which means there's an active process using it
				}
			}
		}
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
			deleteOldStopRequests();
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
	
	private static int DEFAULT_FORCE_STOP_WAIT_INTERVAL=3000;
	public static void stopAllServers() {
		//stopping should be done in reverse order of starting the servers
		for(int i=servers.size()-1;i>=0;i--){
			try {
				servers.get(i).stop();
				if (forcedStop) {
					waitForServerToStop(servers.get(i),DEFAULT_FORCE_STOP_WAIT_INTERVAL);
				}else{
					waitForServerToStop(servers.get(i),null);
				}
			} catch (Exception e) {
				logger.error("Server Stop Error:",e);
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
				waitForServerToStart(server,null);
			} catch (Exception e) {
				logger.error("Server Start Error:",e);
			}
		}
	}
	private static final int SERVER_STATUS_CHANGE_WAIT_INTERVAL=100;

	private static void waitForServerToStart(IServer server,Integer maxWait) throws Exception {
		int count=0;
//		if (server.getStatus()==ServerStatus.STARTING) {
//			logger.info("Waiting for " + server.getName() + " to start...");
//		}
		while(server.getStatus()==ServerStatus.STARTING && (maxWait==null || count<maxWait)){
			Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
			count+=SERVER_STATUS_CHANGE_WAIT_INTERVAL;
		}
		if (server.getStatus()!=ServerStatus.STARTED){
			throw new Exception("The "+server.getName()+" did not start!!!");
		}
	}

	private static void waitForServerToStop(IServer server,Integer maxWait) throws Exception {
		int count=0;
		if (server.getStatus()==ServerStatus.STOPING) {
			logger.info("Waiting for " + server.getName() + " to stop...");
		}
		//we are doing hasStopRequested() check because while we are stuck in the loop to stop there could be a forceStop request 
		while(server.getStatus()==ServerStatus.STOPING && (maxWait==null || count<maxWait) && hasStopRequested() && (maxWait!=null || !forcedStop)){
			Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
			count+=SERVER_STATUS_CHANGE_WAIT_INTERVAL;
		}
		if (server.getStatus()!=ServerStatus.STOPPED){
			throw new Exception("Error stopping the "+server.getName()+"!!!");
		}
	}

	private static boolean isSystemShutDown() {
		return systemShutDown;
	}

	private static void setSystemShutDown(boolean systemShutDown) {
		ServerMain.systemShutDown = systemShutDown;
	}
}