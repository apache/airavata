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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.api.AmazonWebServicesSettings;

public class AmazonWebServicesSettingsImpl implements AmazonWebServicesSettings {
    private String awsAccessKey;
    private String awsSecretKey;
    private String amiId;
    private String instanceId;
    private String instanceType;
    private String username;

    @Override
    public String getAccessKeyId() {
        return awsAccessKey;
    }

    @Override
    public String getAMIId() {
        return amiId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getInstanceType() {
        return instanceType;
    }

    @Override
    public String getSecretAccessKey() {
        return awsSecretKey;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setAccessKeyId(String accessKeyId) {
        this.awsAccessKey = accessKeyId;
    }

    @Override
    public void setAMIId(String amiId) {
        this.amiId = amiId;
    }

    @Override
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public void setSecretAccessKey(String secretAccessKey) {
        this.awsSecretKey = secretAccessKey;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }
}
