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
package org.apache.airavata.k8s.api.resources.compute;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ComputeResource {

    private long id;
    private String name;
    private String host;
    private String userName;
    private String password;
    private String communicationType;

    public long getId() {
        return id;
    }

    public ComputeResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ComputeResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ComputeResource setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ComputeResource setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ComputeResource setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public ComputeResource setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
        return this;
    }
}
