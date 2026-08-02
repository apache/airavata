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
package org.apache.airavata.server.auth;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Fetches profile claims (email, name, etc.) from CILogon's userinfo endpoint.
 * CILogon's token introspection response only carries a small, fixed claim set
 * (RFC 7662 basics like {@code active}/{@code scope}/{@code sub}); the fuller
 * profile is only available from userinfo.
 */
public class CILogonUserInfoClient {

    private static final Log LOG = LogFactory.getLog(CILogonUserInfoClient.class);

    private final RestClient restClient;
    private final String userInfoUri;

    public CILogonUserInfoClient(RestClient restClient, String userInfoUri) {
        this.restClient = restClient;
        this.userInfoUri = userInfoUri;
    }

    /** Returns the userinfo claims for the given access token, or an empty map if the call fails. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchUserInfo(String accessToken) {
        try {
            Map<String, Object> body = restClient
                    .get()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            return body != null ? body : Map.of();
        } catch (RestClientException ex) {
            LOG.warn("Failed to fetch CILogon userinfo; proceeding without profile claims", ex);
            return Map.of();
        }
    }
}
