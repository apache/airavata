/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.airavata.xregistry;

public interface XregistryOptions {
    public static final String SSL_HOST_KEY_FILE ="ssl.hostcertsKeyFile";
    public static final String SSL_TRUSTED_CERT_FILE ="ssl.trustedCertsFile";
    
    public static final String MYPROXY_USERNAME ="myproxyUserName";
    public static final String MYPROXY_PASSWD ="myproxyPasswd";
    public static final String MYPROXY_SERVER ="myproxyServer";
    public static final String MYPROXY_LIFETIME ="myproxyLifetime";
    
    public static final String DBURL ="databaseUrl";
    
    public static final String JDBCDRIVERURL ="jdbcDriver";
    
    public static final String PORT = "port";
    
    public static final String SECURITY_ENABLED = "securityEnabled";
    
    
    
    
    
}

