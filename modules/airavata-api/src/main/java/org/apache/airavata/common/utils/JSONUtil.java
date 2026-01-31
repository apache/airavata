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
package org.apache.airavata.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import org.apache.airavata.config.JacksonConfig;

/**
 * JSON utility class using centralized ObjectMapper.
 */
public class JSONUtil {

    /**
     * Get the ObjectMapper instance.
     * Uses the globally configured ObjectMapper from JacksonConfig.
     */
    private static ObjectMapper getObjectMapper() {
        return JacksonConfig.getGlobalMapper();
    }

    public static void saveJSON(JsonNode jsonNode, File file) throws IOException {
        IOUtil.writeToFile(jsonNodeToString(jsonNode), file);
    }

    public static ObjectNode stringToJSONObject(String workflowString) {
        try {
            JsonNode node = getObjectMapper().readTree(workflowString);
            if (node.isObject()) {
                return (ObjectNode) node;
            }
            throw new IllegalArgumentException("String does not represent a JSON object");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }

    public static ObjectNode loadJSON(File file) throws IOException {
        return loadJSON(new FileReader(file));
    }

    public static ObjectNode loadJSON(Reader reader) throws IOException {
        try {
            JsonNode node = getObjectMapper().readTree(reader);
            if (node.isObject()) {
                return (ObjectNode) node;
            }
            throw new IllegalArgumentException("File does not contain a JSON object");
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse JSON from reader", e);
        }
    }

    public static String jsonNodeToString(JsonNode jsonNode) {
        try {
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JsonNode to string", e);
        }
    }

    public static boolean isEqual(ObjectNode originalJsonObject, ObjectNode newJsonObject) {
        if (originalJsonObject == null && newJsonObject == null) {
            return true;
        } else if (originalJsonObject == null || newJsonObject == null) {
            return false;
        } else {
            // check the number of children
            if (originalJsonObject.size() != newJsonObject.size()) {
                return false;
            }

            Iterator<String> fieldNames = originalJsonObject.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode valueOrig = originalJsonObject.get(key);
                JsonNode valueNew = newJsonObject.get(key);

                if (valueNew == null) {
                    return false;
                }

                if (valueOrig.isObject() && valueNew.isObject()) {
                    if (!isEqual((ObjectNode) valueOrig, (ObjectNode) valueNew)) {
                        return false;
                    }
                } else if (valueOrig.isArray() && valueNew.isArray()) {
                    if (!isEqual((ArrayNode) valueOrig, (ArrayNode) valueNew)) {
                        return false;
                    }
                } else if (valueOrig.isValueNode() && valueNew.isValueNode()) {
                    if (!isEqual(valueOrig, valueNew)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isEqual(ArrayNode arrayOriginal, ArrayNode arrayNew) {
        if (arrayOriginal == null && arrayNew == null) {
            return true;
        } else if (arrayOriginal == null || arrayNew == null) {
            return false;
        } else {
            // check the number of elements
            if (arrayOriginal.size() != arrayNew.size()) {
                return false;
            } else if (arrayOriginal.size() == 0) {
                return true;
            } else {
                for (int i = 0; i < arrayOriginal.size(); i++) {
                    JsonNode valueOrig = arrayOriginal.get(i);
                    JsonNode valueNew = arrayNew.get(i);
                    if (valueOrig.isObject() && valueNew.isObject()) {
                        if (!isEqual((ObjectNode) valueOrig, (ObjectNode) valueNew)) {
                            return false;
                        }
                    } else if (valueOrig.isValueNode() && valueNew.isValueNode()) {
                        if (!isEqual(valueOrig, valueNew)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isEqual(JsonNode nodeOrig, JsonNode nodeNew) {
        if (nodeOrig == null && nodeNew == null) {
            return true;
        } else if (nodeOrig == null || nodeNew == null) {
            return false;
        } else {
            if (nodeOrig.isTextual() && nodeNew.isTextual()) {
                return nodeOrig.asText().equals(nodeNew.asText());
            } else if (nodeOrig.isBoolean() && nodeNew.isBoolean()) {
                return nodeOrig.asBoolean() == nodeNew.asBoolean();
            } else if (nodeOrig.isNumber() && nodeNew.isNumber()) {
                return nodeOrig.asDouble() == nodeNew.asDouble();
            } else {
                return nodeOrig.isNull() && nodeNew.isNull();
            }
        }
    }
}
