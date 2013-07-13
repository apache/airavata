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

package org.apache.airavata.gfac.context.security;

import org.apache.airavata.gfac.SecurityContext;

public class AmazonSecurityContext implements SecurityContext {

	public static final String AMAZON_SECURITY_CONTEXT = "amazon";
    private String userName;
    private String accessKey;
    private String secretKey;
    private String amiId;
    private String instanceType;
    private String instanceId;
    private boolean isRunningInstance = false;

    public AmazonSecurityContext(String userName, String accessKey, String secretKey, String amiId, String instanceType) {
        this.userName = userName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.amiId = amiId;
        this.instanceType = instanceType;
    }

    public AmazonSecurityContext(String userName, String accessKey, String secretKey, String instanceId) {
        this.userName = userName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.instanceId = instanceId;
        this.isRunningInstance = true;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getAmiId() {
        return amiId;
    }

    public boolean isRunningInstance() {
        return isRunningInstance;
    }

    public String getUserName() {
        return userName;
    }
}
