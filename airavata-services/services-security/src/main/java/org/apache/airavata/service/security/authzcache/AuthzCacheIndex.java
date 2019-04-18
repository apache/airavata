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
package org.apache.airavata.service.security.authzcache;

/**
 * Cache index of the default authorization cache.
 */
public class AuthzCacheIndex {

    private String subject;
    private String oauthAccessToken;
    private String action;
    private String gatewayId;

    public AuthzCacheIndex(String userName, String gatewayId, String accessToken, String actionString) {
        this.subject = userName;
        this.oauthAccessToken = accessToken;
        this.action = actionString;
        this.gatewayId = gatewayId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOauthAccessToken() {
        return oauthAccessToken;
    }

    public void setOauthAccessToken(String oauthAccessToken) {
        this.oauthAccessToken = oauthAccessToken;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /*Equals and hash code methods are overridden since this is being used as an index of a map and that containsKey method
        * should return true if the values of two index objects are equal.*/
    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != getClass()) {
            return false;
        }
        return ((this.getSubject().equals(((AuthzCacheIndex) other).getSubject()))
                && (this.getGatewayId().equals(((AuthzCacheIndex) other).getGatewayId()))
                && (this.getOauthAccessToken().equals(((AuthzCacheIndex) other).getOauthAccessToken()))
                && (this.getAction().equals(((AuthzCacheIndex) other).getAction())));
    }

    @Override
    public int hashCode() {
        return this.getSubject().hashCode() + this.getOauthAccessToken().hashCode() + this.getGatewayId().hashCode()
                + this.getAction().hashCode();
    }
}
