
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
package org.airavata.xbaya.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XbayaContext {
   private final static Logger logger = LoggerFactory.getLogger(XbayaContext.class);

   private Map<String,String> dynamicConfigurations = new HashMap<>();

   private Properties properties = new Properties();
   private static final String PROPERTY_FILE_NAME = "/xbaya.properties";

   private static XbayaContext instance;

   private XbayaContext(){
       InputStream inputStream = XbayaContext.class.getResourceAsStream(PROPERTY_FILE_NAME);
       try {
           properties.load(inputStream);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   public static XbayaContext getInstance(){
       if(XbayaContext.instance == null){
           XbayaContext.instance = new XbayaContext();
       }
       return XbayaContext.instance;
   }

   public ZoneOffset getTimeZoneOffset(){
       LocalDateTime dt = LocalDateTime.now();
       return dt.atZone(TimeZone.getDefault().toZoneId()).getOffset();
   }

   public String getFileDownloadLocation(){ return System.getProperty("user.home") + File.separator
           + properties.getProperty(XbayaConfig.DEFAULT_FILE_DOWNLOAD_PATH);}

   public String getAiravataGatewayId(){ return properties.getProperty(XbayaConfig.AIRAVATA_GATEWAY_ID);}

   public void setUserName(String userName){ dynamicConfigurations.put(XbayaConfig.USER_NAME, userName);}

   public String getUserName(){ return dynamicConfigurations.get(XbayaConfig.USER_NAME);}

   public int getMaxRecentExpCount(){ return 20; }

   public String getRecentExperimentsDummyId(){ return "$$$$$$"; }

   public void setAuthenticated(boolean authenticated) {
       if(authenticated)
           dynamicConfigurations.put(XbayaConfig.AUTHENTICATED, "true");
   }

   public boolean getAuthenticated() { return dynamicConfigurations.containsKey(XbayaConfig.AUTHENTICATED); }

   public void setOAuthToken(String oauthToken) {
       dynamicConfigurations.put(XbayaConfig.OAUTH_TOKEN,oauthToken);
   }

   public String getOAuthToken(){
       return dynamicConfigurations.get(XbayaConfig.OAUTH_TOKEN);
   }

   public void setRefreshToken(String refreshToken){
       dynamicConfigurations.put(XbayaConfig.OAUTH_REFRESH_TOKEN, refreshToken);
   }

   public String getRefreshToken(){
       return dynamicConfigurations.get(XbayaConfig.OAUTH_REFRESH_TOKEN);
   }

   public void setTokenExpiaryTime(long tokenExpiarationTime) {
       dynamicConfigurations.put(XbayaConfig.OAUTH_TOKEN_EXPIRATION_TIME, tokenExpiarationTime + "");
   }

   public long getOAuthTokenExpirationTime(){
       return Long.parseLong(dynamicConfigurations.get(XbayaConfig.OAUTH_TOKEN_EXPIRATION_TIME));
   }

   public String getAiravataHost() {
       return properties.getProperty(XbayaConfig.AIRAVATA_HOST);
   }

   public int getAiravataPort() {
       return Integer.parseInt(properties.getProperty(XbayaConfig.AIRAVATA_PORT));
   }

   public String getSFTPHost() {
       return properties.getProperty(XbayaConfig.SFTP_HOST);
   }

   public int getSFTPPort() {
       return Integer.parseInt(properties.getProperty(XbayaConfig.SFTP_PORT));
   }

   public String getIdpUrl() {
       return properties.getProperty(XbayaConfig.IDP_URL);
   }

   public String[] getAuthorisedUserRoles() {
       return properties.getProperty(XbayaConfig.IDP_AUTHORISED_ROLES).split(",");
   }

   public String getOAuthClientId() {
       return properties.getProperty(XbayaConfig.IDP_OAUTH_CLIENT_ID);
   }

   public String getOAuthClientSecret() {
       return properties.getProperty(XbayaConfig.IDP_OAUTH_CLIENT_SECRET);
   }

   public String getIdpTenantId() {
       return properties.getProperty(XbayaConfig.IDP_TENANT_ID);
   }

   public CharSequence getGaussianAppName() {
       return "gaussian";
   }

   public CharSequence getGamessAppName() {
       return "gamess";
   }

   public String getGatewayaStorageId(){
       return properties.getProperty(XbayaConfig.GATEWAY_STORAGE_ID);
   }

   public String getGatewayUserDataRoot(){
       return properties.getProperty(XbayaConfig.REMOTE_DATA_DIR_ROOT) + getUserName() + "/";
   }

   public String getRemoteDataDirPrefix(){
       return properties.getProperty(XbayaConfig.REMOTE_DATA_DIR_PREFIX);
   }
}

