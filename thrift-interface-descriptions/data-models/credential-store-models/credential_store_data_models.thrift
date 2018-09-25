/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */


namespace java org.apache.airavata.model.credential.store
namespace php Airavata.Model.Credential.Store
namespace py airavata.model.credential.store

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"

struct SSHCredential {
    1: required string gatewayId,
    2: required string username,
    3: optional string passphrase,
    4: optional string publicKey,
    5: optional string privateKey,
    6: optional i64 persistedTime,
    7: optional string token,
    8: optional string description,
}

/**
 * Data Types supported in Airavata. The primitive data types
 *
*/
enum SummaryType{
	SSH,
	PASSWD,
	CERT
}

struct CredentialSummary {
    1: required SummaryType type,
    2: required string gatewayId,
    /**
     * The username corresponds to the Credential's `portalUserName` which is the username of the user that
     * created the credential.
     */
    3: required string username,
    4: optional string publicKey,
    5: optional i64 persistedTime,
    6: required string token,
    7: optional string description
}

struct CommunityUser {
    1: required string gatewayName,
    2: required string username,
    3: required string userEmail
}

struct CertificateCredential {
    1: required CommunityUser communityUser,
    2: required string x509Cert,
    3: optional string notAfter,
    4: optional string privateKey,
    5: optional i64 lifeTime,
    6: optional string notBefore
    7: optional i64 persistedTime,
    8: optional string token
}

struct PasswordCredential {
    1: required string gatewayId,
    2: required string portalUserName,
    3: required string loginUserName,
    4: required string password,
    5: optional string description,
    6: optional i64 persistedTime,
    7: optional string token
}
