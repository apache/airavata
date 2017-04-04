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
package org.apache.airavata.client.secure.client;

public class Properties {
    //Airavata server host, port
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 9930;

    //trust store parameters
    public static final String TRUST_STORE_PATH = "../../../../../airavata/modules/configuration/server/src/main/resources/client_truststore.jks";
    public static final String TRUST_STORE_PASSWORD = "airavata";

    public static String oauthAuthzServerURL = "https://localhost:9443/services/";
    public static String oauthTokenEndPointURL = "https://localhost:9443/oauth2/token";
    public static int authzServerPort = 9443;
    public static String adminUserName = "admin";
    public static String adminPassword = "admin";
    public static int grantType = 1;

    //OAuth consumer app properties
    public static String appName = "AiravataGWP1";
    public static String consumerID = "AiravataGW1";
    public static String consumerSecret = "AiravataGW1234";

    //resource owner credential
    public static String userName = "admin";
    public static String password = "admin";
}
