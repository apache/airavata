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
package org.apache.airavata.gfac.core.cluster;

/**
 * Encapsulate server information.
 */
public class ServerInfo {
    private static int DEFAULT_PORT = 22;
    private String host;
    private String userName;
    private int port;
    private String credentialToken;

    public ServerInfo(String userName, String host, String credentialToken) {
        this(userName, host, credentialToken, DEFAULT_PORT);
    }

    public ServerInfo(String userName, String host, String credentialToken, int port) {
        this.host = host;
        this.userName = userName;
        this.port = port;
        this.credentialToken = credentialToken;
    }

    public String getHost() {
        return host;
    }

    public String getUserName() {
        return userName;
    }

    public int getPort() {
        return port;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

}
