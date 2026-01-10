/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.profile.utils.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO representing a Keycloak OAuth client.
 * Replaces org.keycloak.representations.idm.ClientRepresentation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientRepresentation {
    private String id;
    private String name;
    private String clientId;
    private String protocol;
    private Boolean standardFlowEnabled;
    private Boolean enabled;
    private Boolean authorizationServicesEnabled;
    private Boolean directAccessGrantsEnabled;
    private Boolean serviceAccountsEnabled;
    private Boolean fullScopeAllowed;
    private String clientAuthenticatorType;
    private List<String> redirectUris;
    private Boolean publicClient;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @JsonProperty("standardFlowEnabled")
    public Boolean getStandardFlowEnabled() {
        return standardFlowEnabled;
    }

    public void setStandardFlowEnabled(Boolean standardFlowEnabled) {
        this.standardFlowEnabled = standardFlowEnabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("authorizationServicesEnabled")
    public Boolean getAuthorizationServicesEnabled() {
        return authorizationServicesEnabled;
    }

    public void setAuthorizationServicesEnabled(Boolean authorizationServicesEnabled) {
        this.authorizationServicesEnabled = authorizationServicesEnabled;
    }

    @JsonProperty("directAccessGrantsEnabled")
    public Boolean getDirectAccessGrantsEnabled() {
        return directAccessGrantsEnabled;
    }

    public void setDirectAccessGrantsEnabled(Boolean directAccessGrantsEnabled) {
        this.directAccessGrantsEnabled = directAccessGrantsEnabled;
    }

    @JsonProperty("serviceAccountsEnabled")
    public Boolean getServiceAccountsEnabled() {
        return serviceAccountsEnabled;
    }

    public void setServiceAccountsEnabled(Boolean serviceAccountsEnabled) {
        this.serviceAccountsEnabled = serviceAccountsEnabled;
    }

    @JsonProperty("fullScopeAllowed")
    public Boolean getFullScopeAllowed() {
        return fullScopeAllowed;
    }

    public void setFullScopeAllowed(Boolean fullScopeAllowed) {
        this.fullScopeAllowed = fullScopeAllowed;
    }

    @JsonProperty("clientAuthenticatorType")
    public String getClientAuthenticatorType() {
        return clientAuthenticatorType;
    }

    public void setClientAuthenticatorType(String clientAuthenticatorType) {
        this.clientAuthenticatorType = clientAuthenticatorType;
    }

    @JsonProperty("redirectUris")
    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    @JsonProperty("publicClient")
    public Boolean getPublicClient() {
        return publicClient;
    }

    public void setPublicClient(Boolean publicClient) {
        this.publicClient = publicClient;
    }
}
