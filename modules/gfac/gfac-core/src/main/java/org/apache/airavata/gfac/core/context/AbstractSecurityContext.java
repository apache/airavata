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
package org.apache.airavata.gfac.core.context;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 6/26/13
 * Time: 4:33 PM
 */

import java.io.Serializable;

import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.core.RequestData;
import org.apache.airavata.gfac.core.SecurityContext;

/**
 * Abstract implementation of the security context.
 */
public abstract class AbstractSecurityContext implements SecurityContext, Serializable {

    private CredentialReader credentialReader;
    private RequestData requestData;

    public AbstractSecurityContext(CredentialReader credentialReader, RequestData requestData) {
        this.credentialReader = credentialReader;
        this.requestData = requestData;
    }
    public AbstractSecurityContext() {

    }

    public CredentialReader getCredentialReader() {
        return credentialReader;
    }

    public RequestData getRequestData() {
        return requestData;
    }
}
