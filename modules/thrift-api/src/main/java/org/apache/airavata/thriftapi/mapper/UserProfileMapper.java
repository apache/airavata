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

import org.apache.airavata.common.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between domain UserProfile and thrift UserProfile.
 */
@Mapper(config = ModelMapper.class)
public interface UserProfileMapper extends ModelMapper {

    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

    /**
     * Convert domain model to thrift model.
     */
    org.apache.airavata.thriftapi.model.UserProfile toThrift(UserProfile domain);

    /**
     * Convert thrift model to domain model.
     */
    UserProfile toDomain(org.apache.airavata.thriftapi.model.UserProfile thrift);

    default org.apache.airavata.common.model.ethnicity map(org.apache.airavata.thriftapi.model.ethnicity value) {
        if (value == null) return null;
        return org.apache.airavata.common.model.ethnicity.valueOf(value.name());
    }

    default org.apache.airavata.thriftapi.model.ethnicity map(org.apache.airavata.common.model.ethnicity value) {
        if (value == null) return null;
        return org.apache.airavata.thriftapi.model.ethnicity.valueOf(value.name());
    }

    default org.apache.airavata.common.model.race map(org.apache.airavata.thriftapi.model.race value) {
        if (value == null) return null;
        return org.apache.airavata.common.model.race.valueOf(value.name());
    }

    default org.apache.airavata.thriftapi.model.race map(org.apache.airavata.common.model.race value) {
        if (value == null) return null;
        return org.apache.airavata.thriftapi.model.race.valueOf(value.name());
    }

    default org.apache.airavata.common.model.disability map(org.apache.airavata.thriftapi.model.disability value) {
        if (value == null) return null;
        return org.apache.airavata.common.model.disability.valueOf(value.name());
    }

    default org.apache.airavata.thriftapi.model.disability map(org.apache.airavata.common.model.disability value) {
        if (value == null) return null;
        return org.apache.airavata.thriftapi.model.disability.valueOf(value.name());
    }
}
