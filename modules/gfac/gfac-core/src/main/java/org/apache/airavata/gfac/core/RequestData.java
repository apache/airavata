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
package org.apache.airavata.gfac.core;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 6/28/13
 * Time: 3:28 PM
 */

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;

/**
 * Encapsulates GFac specific data that are coming in the request.
 */
public class RequestData {

    private static final int DEFAULT_LIFE_TIME = 3600;
    private static final int DEFAULT_MY_PROXY_PORT = 7512;

    private String tokenId;
    private String requestUser;
    private String gatewayId;

    private String myProxyServerUrl = null;
    private int myProxyPort = 0;
    private String myProxyUserName = null;
    private String myProxyPassword = null;
    private int myProxyLifeTime = DEFAULT_LIFE_TIME;




    public RequestData() {
    }

    public RequestData(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public RequestData(String tokenId, String requestUser, String gatewayId) {
        this.tokenId = tokenId;
        this.requestUser = requestUser;
        this.gatewayId = gatewayId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(String requestUser) {
        this.requestUser = requestUser;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getMyProxyServerUrl() throws ApplicationSettingsException {
        if (myProxyServerUrl == null) {
            myProxyServerUrl = ServerSettings.getSetting(GFacConstants.MYPROXY_SERVER);
        }
        return myProxyServerUrl;
    }

    public void setMyProxyServerUrl(String myProxyServerUrl) {
        this.myProxyServerUrl = myProxyServerUrl;
    }

    public int getMyProxyPort() {

        if (myProxyPort == 0) {
            String sPort = ServerSettings.getSetting(GFacConstants.MYPROXY_SERVER_PORT, Integer.toString(DEFAULT_MY_PROXY_PORT));
            myProxyPort = Integer.parseInt(sPort);
        }

        return myProxyPort;
    }

    public void setMyProxyPort(int myProxyPort) {
        this.myProxyPort = myProxyPort;
    }

    public String getMyProxyUserName() throws ApplicationSettingsException {
        if (myProxyUserName == null) {
            myProxyUserName = ServerSettings.getSetting(GFacConstants.MYPROXY_USER);
        }

        return myProxyUserName;
    }

    public void setMyProxyUserName(String myProxyUserName) {
        this.myProxyUserName = myProxyUserName;
    }

    public String getMyProxyPassword() throws ApplicationSettingsException {

        if (myProxyPassword == null) {
            myProxyPassword = ServerSettings.getSetting(GFacConstants.MYPROXY_PASS);
        }

        return myProxyPassword;
    }

    public int getMyProxyLifeTime() {
        String life = ServerSettings.getSetting(GFacConstants.MYPROXY_LIFE,Integer.toString(myProxyLifeTime));
        myProxyLifeTime = Integer.parseInt(life);
        return myProxyLifeTime;
    }

    public void setMyProxyLifeTime(int myProxyLifeTime) {
        this.myProxyLifeTime = myProxyLifeTime;
    }

    public void setMyProxyPassword(String myProxyPassword) {
        this.myProxyPassword = myProxyPassword;
    }
}
