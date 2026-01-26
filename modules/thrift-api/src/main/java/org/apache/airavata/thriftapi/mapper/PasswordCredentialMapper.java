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

import java.util.Date;
import org.apache.airavata.credential.model.PasswordCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between domain PasswordCredential and thrift PasswordCredential.
 */
@Mapper(config = ModelMapper.class)
public interface PasswordCredentialMapper extends ModelMapper {

    PasswordCredentialMapper INSTANCE = Mappers.getMapper(PasswordCredentialMapper.class);

    /**
     * Convert domain model to thrift model.
     */
    @Mapping(target = "persistedTime", source = "persistedTime", qualifiedByName = "dateToLong")
    org.apache.airavata.thriftapi.credential.model.PasswordCredential toThrift(PasswordCredential domain);

    /**
     * Convert thrift model to domain model.
     */
    @Mapping(target = "persistedTime", source = "persistedTime", qualifiedByName = "longToDate")
    PasswordCredential toDomain(org.apache.airavata.thriftapi.credential.model.PasswordCredential thrift);

    /**
     * Convert Date to long (milliseconds since epoch).
     */
    @Named("dateToLong")
    default long dateToLong(Date date) {
        return date != null ? date.getTime() : 0L;
    }

    /**
     * Convert long to Date.
     */
    @Named("longToDate")
    default Date longToDate(long millis) {
        return millis > 0 ? new Date(millis) : null;
    }
}
