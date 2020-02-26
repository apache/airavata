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

include "../data-models/credential-store-models/credential_store_data_models.thrift"
include "credential_store_errors.thrift"
include "../base-api/base_api.thrift"

namespace java org.apache.airavata.credential.store.cpi
namespace py airavata.api.credential.store

const string CS_CPI_VERSION = "0.18.0"

service CredentialStoreService extends base_api.BaseAPI {

  credential_store_data_models.CredentialSummary getCredentialSummary (
        1: required string tokenId,
        2: required string gatewayId)
    throws (1:credential_store_errors.CredentialStoreException csException);

  list<credential_store_data_models.CredentialSummary> getAllCredentialSummaries(
      1: required credential_store_data_models.SummaryType type,
      2: required list<string> accessibleTokenIds,
      3: required string gatewayId)
    throws (1: credential_store_errors.CredentialStoreException csException);

  /**
  * This method is to add SSHCredential which will return the token Id in success
  **/
  string addSSHCredential (1: required credential_store_data_models.SSHCredential sshCredential)
                        throws (1:credential_store_errors.CredentialStoreException csException);

  string addCertificateCredential (1: required credential_store_data_models.CertificateCredential certificateCredential)
                        throws (1:credential_store_errors.CredentialStoreException csException);

  string addPasswordCredential (1: required credential_store_data_models.PasswordCredential passwordCredential)
                        throws (1:credential_store_errors.CredentialStoreException csException);

  credential_store_data_models.SSHCredential getSSHCredential (1: required string tokenId, 2: required string gatewayId)
                        throws (1:credential_store_errors.CredentialStoreException csException);

  credential_store_data_models.CertificateCredential getCertificateCredential (1: required string tokenId, 2: required string gatewayId)
                        throws (1:credential_store_errors.CredentialStoreException csException);

  credential_store_data_models.PasswordCredential getPasswordCredential (1: required string tokenId, 2: required string gatewayId)
                        throws (1:credential_store_errors.CredentialStoreException csException);

  // Deprecated
  list<credential_store_data_models.CredentialSummary> getAllCredentialSummaryForGateway (1: required credential_store_data_models.SummaryType type,
                              2: required string gatewayId)
                              throws (1:credential_store_errors.CredentialStoreException csException);

    // Deprecated
    list<credential_store_data_models.CredentialSummary> getAllCredentialSummaryForUserInGateway (1: required credential_store_data_models.SummaryType type,
                                                2: required string gatewayId,
                                                3: required string userId)
                                                throws (1:credential_store_errors.CredentialStoreException csException);

  // Deprecated
  map<string,string> getAllPWDCredentialsForGateway (1: required string gatewayId) throws (1:credential_store_errors.CredentialStoreException csException);

  bool deleteSSHCredential(1: required string tokenId, 2: required string gatewayId) throws (1:credential_store_errors.CredentialStoreException csException);

  bool deletePWDCredential(1: required string tokenId, 2: required string gatewayId) throws (1:credential_store_errors.CredentialStoreException csException);
}
