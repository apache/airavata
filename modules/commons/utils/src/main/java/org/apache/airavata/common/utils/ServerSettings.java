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

import org.apache.airavata.common.exception.ApplicationSettingsException;


public class ServerSettings extends ApplicationSettings{
    private static final String DEFAULT_GATEWAY_ID="gateway.id";
    private static final String SYSTEM_USER="system.user";
    private static final String SYSTEM_USER_PASSWORD="system.password";
    private static final String SYSTEM_USER_GATEWAY="system.gateway";
    private static final String TOMCAT_PORT = "port";
    
	public static String getDefaultGatewayId()throws ApplicationSettingsException{
    	return getSetting(DEFAULT_GATEWAY_ID);
    }
    
    public static String getSystemUser() throws ApplicationSettingsException{
    	return getSetting(SYSTEM_USER);
    }
    
    public static String getSystemUserPassword() throws ApplicationSettingsException{
    	return getSetting(SYSTEM_USER_PASSWORD);
    }
    
    public static String getSystemUserGateway() throws ApplicationSettingsException{
    	return getSetting(SYSTEM_USER_GATEWAY);
    }

    public static String getTomcatPort() throws ApplicationSettingsException {
        return getSetting(TOMCAT_PORT);
    }
}
