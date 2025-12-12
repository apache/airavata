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
package org.apache.airavata.registry.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized utility for backward compatibility mappings and transformations.
 * 
 * <p>This class consolidates all backward compatibility logic that was previously
 * scattered across services. It provides methods for:
 * <ul>
 *   <li>Field name mappings (old → new column names)</li>
 *   <li>Type conversions between legacy and current formats</li>
 *   <li>Default value handling for nullable fields</li>
 *   <li>Legacy data migration helpers</li>
 * </ul>
 * 
 * <p>All compatibility mappings should be documented here and eventually removed
 * once all legacy data has been migrated.
 */
public class EntityCompatibilityMapper {
    private static final Logger logger = LoggerFactory.getLogger(EntityCompatibilityMapper.class);

    /**
     * Maps legacy field names to current DDL column names.
     * 
     * @param legacyFieldName The old field/column name
     * @return The current column name, or null if no mapping exists
     */
    public static String mapLegacyFieldName(String legacyFieldName) {
        // Add mappings as needed when legacy field names are encountered
        // Example: return "NEW_COLUMN_NAME".equals(legacyFieldName) ? "CURRENT_COLUMN_NAME" : null;
        return null;
    }

    /**
     * Converts a legacy boolean value stored as smallint to Boolean.
     * 
     * @param value The smallint value (0 or 1, or null)
     * @return Boolean value, or null if input is null
     */
    public static Boolean convertSmallintToBoolean(Short value) {
        if (value == null) {
            return null;
        }
        return value != 0;
    }

    /**
     * Converts a Boolean to smallint for legacy compatibility.
     * 
     * @param value The Boolean value
     * @return Short value (0 or 1), or null if input is null
     */
    public static Short convertBooleanToSmallint(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? (short) 1 : (short) 0;
    }

    /**
     * Converts a legacy boolean value stored as tinyint to Boolean.
     * 
     * @param value The tinyint value (0 or 1, or null)
     * @return Boolean value, or null if input is null
     */
    public static Boolean convertTinyintToBoolean(Byte value) {
        if (value == null) {
            return null;
        }
        return value != 0;
    }

    /**
     * Converts a Boolean to tinyint for legacy compatibility.
     * 
     * @param value The Boolean value
     * @return Byte value (0 or 1), or null if input is null
     */
    public static Byte convertBooleanToTinyint(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? (byte) 1 : (byte) 0;
    }

    /**
     * Provides default value for nullable fields that may be null in legacy data.
     * 
     * @param value The current value
     * @param defaultValue The default to use if value is null
     * @param <T> The value type
     * @return The value if not null, otherwise the default
     */
    public static <T> T withDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Logs a compatibility mapping usage for tracking and eventual removal.
     * 
     * @param entityType The entity class name
     * @param fieldName The field being mapped
     * @param mappingType The type of mapping (e.g., "field_name", "type_conversion")
     */
    public static void logCompatibilityMapping(String entityType, String fieldName, String mappingType) {
        logger.debug("Compatibility mapping applied: {} -> {} ({})", entityType, fieldName, mappingType);
    }
}

