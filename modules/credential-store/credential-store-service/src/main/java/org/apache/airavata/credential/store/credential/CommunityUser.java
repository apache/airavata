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
package org.apache.airavata.credential.store.credential;

import java.io.Serializable;

/**
 * Represents the community user.
 */
public class CommunityUser implements Serializable {

    static final long serialVersionUID = 5783370135149452010L;

    private String gatewayName;
    private String userName;
    private String userEmail;

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public CommunityUser(String gatewayName, String userName, String userEmail) {
        this.gatewayName = gatewayName;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public CommunityUser(String gatewayName, String userName) {
        this.gatewayName = gatewayName;
        this.userName = userName;
    }
}
