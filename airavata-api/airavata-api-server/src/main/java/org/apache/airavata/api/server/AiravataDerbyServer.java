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
package org.apache.airavata.api.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataDerbyServer implements IServer{

    private final static Logger logger = LoggerFactory.getLogger(AiravataDerbyServer.class);
	private static final String SERVER_NAME = "Airavata Derby Server";
	private static final String SERVER_VERSION = "1.0";
	
    public static final String CONFIGURATION_TABLE = "CONFIGURATION";
    public static final String REGISTRY_JDBC_DRIVER = "registry.jdbc.driver";
    public static final String REGISTRY_JDBC_URL = "registry.jdbc.url";
    public static final String REGISTRY_JDBC_USER = "registry.jdbc.user";
    public static final String REGISTRY_JDBC_PASSWORD = "registry.jdbc.password";
    public static final String START_DERBY_ENABLE = "start.derby.server.mode";
    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";
    public static final String DEFAULT_PROJECT_NAME = "default";
    private NetworkServerControl server;
    private String jdbcURl;
    private String jdbcUser;
    private String jdbcPassword;
    
    private ServerStatus status;

	public AiravataDerbyServer() {
		setStatus(ServerStatus.STOPPED);
	}
    public static int getPort(String jdbcURL){
        try{
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getPort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }
    
	public void startDerbyInServerMode() {
		try {
            System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "true");
            server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"),
                    getPort(jdbcURl),
                    jdbcUser, jdbcPassword);
            java.io.PrintWriter consoleWriter = new java.io.PrintWriter(System.out, true);
            server.start(consoleWriter);
        } catch (IOException e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether " +
                    "specified port is available");
        } catch (Exception e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether " +
                    "specified port is available");
        }
    }
	
    public void stopDerbyInServerMode() {
        System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "false");
        if (server!=null){
        	try {
				server.shutdown();
			} catch (Exception e) {
	            logger.error("Error when stopping the derby server : "+e.getLocalizedMessage());
			}
        }
    }
	
    public void startDerbyServer() throws AiravataSystemException {
        startDerbyServer();
        setStatus(ServerStatus.STARTED);
    }

    public static void main(String[] args) {
    	try {
			AiravataDerbyServer server = new AiravataDerbyServer();
			server.start();
		} catch (Exception e) {
			logger.error("Error while initializing derby server", e);
		}
    }

	@Override
	public void start() throws Exception {
		setStatus(ServerStatus.STARTING);
    	startDerbyServer();
	}

	@Override
	public void stop() throws Exception {
		if (getStatus()==ServerStatus.STARTED){
			setStatus(ServerStatus.STOPING);
			stopDerbyInServerMode();
			setStatus(ServerStatus.STOPPED);
		}
	}

	@Override
	public void restart() throws Exception {
		stop();
		start();
	}

	@Override
	public void configure() throws Exception {
		try{
            jdbcURl = ServerSettings.getSetting(REGISTRY_JDBC_URL);
            jdbcUser = ServerSettings.getSetting(REGISTRY_JDBC_USER);
            jdbcPassword = ServerSettings.getSetting(REGISTRY_JDBC_PASSWORD);
            jdbcURl = jdbcURl + "?" + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata server properties", e.getMessage());
        }
	}

	@Override
	public ServerStatus getStatus() throws Exception {
		return status;
	}
	
	private void setStatus(ServerStatus stat){
		status=stat;
		status.updateTime();
	}

	@Override
	public String getName() {
		return SERVER_NAME;
	}

	@Override
	public String getVersion() {
		return SERVER_VERSION;
	}

}
