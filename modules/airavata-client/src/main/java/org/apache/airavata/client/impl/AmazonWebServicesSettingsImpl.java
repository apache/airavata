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

    @Override
    public String getAccessKeyId() {
        return awsAccessKey;
    }

    @Override
    public String getAMIId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getInstanceId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getInstanceType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSecretAccessKey() {
        return awsSecretKey;
    }

    @Override
    public String getUsername() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAccessKeyId(String accessKeyId) {
        this.awsAccessKey = accessKeyId;
    }

    @Override
    public void setAMIId(String amiId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setInstanceId(String instanceId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setInstanceType(String instanceType) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSecretAccessKey(String secretAccessKey) {
        this.awsSecretKey = secretAccessKey;
    }

    @Override
    public void setUsername(String username) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
