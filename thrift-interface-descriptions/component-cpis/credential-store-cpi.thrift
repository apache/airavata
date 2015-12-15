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

/*
 * Component Programming Interface definition for Apache Airavata GFac Service.
 *
*/

include "credential-store-data-models.thrift"
include "credential-store-errors.thrift"

namespace java org.apache.airavata.credential.store.cpi

const string CS_CPI_VERSION = "0.16.0"

service CredentialStoreService {

  /** Query CS server to fetch the CPI version */
  string getCSServiceVersion(),

  /**
  * This method is to add SSHCredential which will return the token Id in success
  **/
  string addSSHCredential (1: required credential-store-data-models.SSHCredential sshCredential)
                        throws (1:credential-store-errors.CredentialStoreException csException);

  string addCertificateCredential (1: required credential-store-data-models.CertificateCredential certificateCredential)
                        throws (1:credential-store-errors.CredentialStoreException csException);

  string addPasswordCredential (1: required credential-store-data-models.PasswordCredential passwordCredential)
                        throws (1:credential-store-errors.CredentialStoreException csException);

  credential-store-data-models.SSHCredential getSSHCredential (1: required string tokenId, 2: required string gatewayId)
                        throws (1:credentialStoreErrors.CredentialStoreException csException);

  credential-store-data-models.CertificateCredential getCertificateCredential (1: required string tokenId, 2: required string gatewayId)
                        throws (1:credentialStoreErrors.CredentialStoreException csException);

  credential-store-data-models.PasswordCredential getPasswordCredential (1: required string tokenId, 2: required string gatewayId)
                        throws (1:credentialStoreErrors.CredentialStoreException csException);

  map<string,string> getAllSSHKeysForUser (1: required string username) throws (1:credentialStoreErrors.CredentialStoreException csException);

  map<string,string> getAllSSHKeysForGateway (1: required string gatewayId) throws (1:credentialStoreErrors.CredentialStoreException csException);

  bool deleteSSHCredential(1: required string tokenId, 2: required string gatewayId) throws (1:credentialStoreErrors.CredentialStoreException csException);


}