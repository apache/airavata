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
package org.apache.airavata.thriftapi.mapper;

import org.apache.airavata.credential.model.CertificateCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between domain CertificateCredential and thrift CertificateCredential.
 */
@Mapper(config = ModelMapper.class)
public interface CertificateCredentialMapper extends ModelMapper {

    CertificateCredentialMapper INSTANCE = Mappers.getMapper(CertificateCredentialMapper.class);

    /**
     * Convert domain model to thrift model.
     */
    org.apache.airavata.thriftapi.credential.model.CertificateCredential toThrift(CertificateCredential domain);

    /**
     * Convert thrift model to domain model.
     *
     * Note: The following properties are ignored as they are not present in the Thrift IDL definition:
     * gatewayId, description, portalUserName, certificateRequestedTime, certificates, privateKeyObject
     */
    @Mapping(target = "gatewayId", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "portalUserName", ignore = true)
    @Mapping(target = "certificateRequestedTime", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "privateKeyObject", ignore = true)
    CertificateCredential toDomain(org.apache.airavata.thriftapi.credential.model.CertificateCredential thrift);
}
