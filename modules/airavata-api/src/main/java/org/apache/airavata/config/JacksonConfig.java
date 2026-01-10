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
package org.apache.airavata.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson ObjectMapper configuration.
 * Provides a single, properly configured ObjectMapper bean for the entire application.
 * 
 * All JSON serialization/deserialization should use this bean instead of creating
 * new ObjectMapper instances.
 */
@Configuration
public class JacksonConfig {

    /**
     * Primary ObjectMapper bean with standard configuration.
     * Features:
     * - Java 8 date/time support
     * - ISO date format (not timestamps)
     * - Lenient deserialization (unknown properties ignored)
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .build();
    }

    /**
     * Holder for static access to ObjectMapper in legacy code.
     * Use @Autowired ObjectMapper instead where possible.
     */
    private static ObjectMapper globalMapper;

    /**
     * Get the global ObjectMapper for static contexts.
     * Prefer dependency injection where possible.
     */
    public static ObjectMapper getGlobalMapper() {
        if (globalMapper == null) {
            // Fallback if Spring context not available
            globalMapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .build();
        }
        return globalMapper;
    }

    /**
     * Set the global mapper (called by Spring during initialization).
     */
    @jakarta.annotation.PostConstruct
    public void initGlobalMapper() {
        globalMapper = objectMapper();
    }
}
