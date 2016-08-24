
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XbayaConfig  {
   private final static Logger logger = LoggerFactory.getLogger(XbayaConfig.class);

   public static final String USER_NAME = "user.name";
   public static final String AUTHENTICATED = "authenticated";
   public static final String OAUTH_TOKEN = "oauth.token";
   public static final String OAUTH_REFRESH_TOKEN = "oauth.refresh.token";
   public static final String OAUTH_TOKEN_EXPIRATION_TIME = "oauth.expiration.time";
   public static final String AIRAVATA_HOST = "airavata.host";
   public static final String AIRAVATA_PORT = "airavata.port";
   public static final String AIRAVATA_GATEWAY_ID = "airavata.gateway-id";
   public static final String SFTP_HOST = "sftp.host";
   public static final String SFTP_PORT = "sftp.port";
   public static final String IDP_URL = "idp.url";
   public static final String IDP_TENANT_ID = "idp.tenant.id";
   public static final String IDP_AUTHORISED_ROLES = "idp.authorised.roles";
   public static final String IDP_OAUTH_CLIENT_ID = "idp.oauth.client.id";
   public static final String IDP_OAUTH_CLIENT_SECRET = "idp.oauth.client.secret";
   public static final String DEFAULT_FILE_DOWNLOAD_PATH = "default.file.download.path";
   public static final java.lang.String GATEWAY_STORAGE_ID = "gateway.storage.resource.id";
   public static final java.lang.String REMOTE_DATA_DIR_ROOT = "remote.data.dir.root";
   public static final java.lang.String REMOTE_DATA_DIR_PREFIX = "remote.data.dir.prefix";
}

