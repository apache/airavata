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
package org.apache.airavata.iam.service;

import java.util.List;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;

public interface CredentialStoreService {

    String addSSHCredential(SSHCredential sshCredential) throws CredentialStoreException;

    String addCertificateCredential(CertificateCredential certificateCredential) throws CredentialStoreException;

    String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException;

    boolean credentialExists(String tokenId, String gatewayId);

    SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException;

    List<CredentialSummary> getCredentialSummariesForUser(String gatewayId, String userId)
            throws CredentialStoreException;

    CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException;

    List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws CredentialStoreException;

    List<CredentialSummary> getAllCredentialSummariesCombined(List<String> accessibleTokenIds, String gatewayId)
            throws CredentialStoreException;

    CertificateCredential getCertificateCredential(String tokenId, String gatewayId) throws CredentialStoreException;

    PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException;

    boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException;

    boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException;
}
