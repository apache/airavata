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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.airavata.common.model.EntityType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to provide Thrift mapper INSTANCE fields as Spring beans.
 * This eliminates the need for reflection to access mapper instances.
 * Provides mappers as functional converters (Function<Object, Object>) to avoid type dependencies.
 */
@Configuration
public class ThriftMapperConfiguration {

    /**
     * Provide UserProfileMapper INSTANCE as a Spring bean.
     */
    @Bean
    public UserProfileMapper userProfileMapper() {
        return UserProfileMapper.INSTANCE;
    }

    /**
     * Provide GatewayMapper INSTANCE as a Spring bean.
     */
    @Bean
    public GatewayMapper gatewayMapper() {
        return GatewayMapper.INSTANCE;
    }

    /**
     * Provide ProjectMapper INSTANCE as a Spring bean.
     */
    @Bean
    public ProjectMapper projectMapper() {
        return ProjectMapper.INSTANCE;
    }

    /**
     * Provide ExperimentModelMapper INSTANCE as a Spring bean.
     */
    @Bean
    public ExperimentModelMapper experimentModelMapper() {
        return ExperimentModelMapper.INSTANCE;
    }

    /**
     * Provide mapper converters as functional interfaces to avoid reflection.
     * Each converter wraps the mapper's toDomain method.
     */
    @Bean
    public Map<EntityType, Function<Object, Object>> mapperConverters(
            UserProfileMapper userProfileMapper,
            GatewayMapper gatewayMapper,
            ProjectMapper projectMapper,
            ExperimentModelMapper experimentModelMapper) {
        Map<EntityType, Function<Object, Object>> converters = new HashMap<>();

        converters.put(
                EntityType.USER_PROFILE,
                thrift -> userProfileMapper.toDomain((org.apache.airavata.thriftapi.model.UserProfile) thrift));
        converters.put(
                EntityType.TENANT,
                thrift -> gatewayMapper.toDomain((org.apache.airavata.thriftapi.model.Gateway) thrift));
        converters.put(
                EntityType.PROJECT,
                thrift -> projectMapper.toDomain((org.apache.airavata.thriftapi.model.Project) thrift));
        converters.put(
                EntityType.EXPERIMENT,
                thrift -> experimentModelMapper.toDomain((org.apache.airavata.thriftapi.model.ExperimentModel) thrift));

        return converters;
    }
}
