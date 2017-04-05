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
package org.apache.airavata.service.security.authzcache;

/**
 * This enum defines the status of the authorization cache returned by the authorization cache manager
 * when an authorization status is checked against an authorization request.
 */
public enum AuthzCachedStatus {
    /*Authorization decision is cached for the given authrization request and the decision authorizes the request.*/
    AUTHORIZED,
    /*Authorization decision is cached for the given authorization request and the decision denies authorization.*/
    NOT_AUTHORIZED,
    /*Authorization decision is not either cached or the cached entry is invalid such that re-authorization is needed.*/
    NOT_CACHED
}
