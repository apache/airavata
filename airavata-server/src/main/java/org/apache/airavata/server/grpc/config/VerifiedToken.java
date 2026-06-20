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
package org.apache.airavata.server.grpc.config;

import java.util.List;

/**
 * Identity and authorization derived from a signature-verified Keycloak access token: the user
 * ({@code preferred_username}), the gateway (the realm parsed from the token issuer), and the realm roles.
 * This is the sole source of caller identity — client-asserted headers are never trusted.
 */
public record VerifiedToken(String userName, String gatewayId, List<String> roles) {}
