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
package org.apache.airavata.messaging.core.util;

import java.util.Map;
import java.util.function.Function;
import org.apache.airavata.common.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry for Thrift to Domain mappers.
 * Uses Spring-injected mapper converters (functional interfaces) instead of reflection.
 * Maps EntityType to the appropriate converter function.
 */
@Component
public class ThriftToDomainMapperRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ThriftToDomainMapperRegistry.class);
    private final Map<EntityType, Function<Object, Object>> mapperConverters;

    public ThriftToDomainMapperRegistry(Map<EntityType, Function<Object, Object>> mapperConverters) {
        this.mapperConverters = mapperConverters;
        logger.debug("Initialized ThriftToDomainMapperRegistry with {} mappers", mapperConverters.size());
    }

    /**
     * Convert a Thrift model to a Domain model based on EntityType.
     *
     * @param entityType The type of entity
     * @param thriftModel The Thrift model to convert
     * @return The converted Domain model
     * @throws IllegalArgumentException if entity type is not supported
     */
    public Object convertToDomain(EntityType entityType, Object thriftModel) {
        Function<Object, Object> converter = mapperConverters.get(entityType);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        try {
            return converter.apply(thriftModel);
        } catch (Exception e) {
            logger.error("Failed to convert {} to domain model", entityType, e);
            throw new RuntimeException("Failed to convert " + entityType + " to domain model", e);
        }
    }
}
