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

/**
 * Created by marcus on 11/23/16.
 */
@Deprecated
public enum CredentialOwnerType {
    GATEWAY(org.apache.airavata.model.credential.store.CredentialOwnerType.GATEWAY),
    USER(org.apache.airavata.model.credential.store.CredentialOwnerType.USER);

    private org.apache.airavata.model.credential.store.CredentialOwnerType datamodelType;
    private CredentialOwnerType(org.apache.airavata.model.credential.store.CredentialOwnerType datamodelType) {
       this.datamodelType = datamodelType;
    }

    public org.apache.airavata.model.credential.store.CredentialOwnerType getDatamodelType() {
        return datamodelType;
    }

    public static CredentialOwnerType findByDataModelType(org.apache.airavata.model.credential.store.CredentialOwnerType datamodelType) {
        for( CredentialOwnerType credentialOwnerType : CredentialOwnerType.values() ) {
            if (credentialOwnerType.datamodelType == datamodelType) {
                return credentialOwnerType;
            }
        }

        throw new RuntimeException("No CredentialOwnerType found for data model CredentialOwnerType " + datamodelType);
    }
}
