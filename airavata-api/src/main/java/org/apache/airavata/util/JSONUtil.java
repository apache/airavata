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
package org.apache.airavata.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

public class JSONUtil {

    private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void saveJSON(JsonNode jsonNode, File file) throws IOException {
        IOUtil.writeToFile(jsonNodeToString(jsonNode), file);
    }

    public static ObjectNode stringToJSONObject(String workflowString) throws IOException {
        return (ObjectNode) MAPPER.readTree(workflowString);
    }

    public static ObjectNode loadJSON(File file) throws IOException {
        return (ObjectNode) MAPPER.readTree(file);
    }

    public static ObjectNode loadJSON(Reader reader) throws IOException {
        return (ObjectNode) MAPPER.readTree(reader);
    }

    public static String jsonNodeToString(JsonNode jsonNode) throws IOException {
        return PRETTY_MAPPER.writeValueAsString(jsonNode);
    }

    public static boolean isEqual(ObjectNode originalJsonObject, ObjectNode newJsonObject) {
        if (originalJsonObject == null && newJsonObject == null) {
            return true;
        } else if (originalJsonObject == null || newJsonObject == null) {
            return false;
        } else {
            // check the number of childs
            int origSize = originalJsonObject.size();
            int newSize = newJsonObject.size();
            if (origSize != newSize) {
                return false;
            }

            Iterator<Map.Entry<String, JsonNode>> fields = originalJsonObject.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode valueOrig = entry.getValue();
                JsonNode valueNew = newJsonObject.get(entry.getKey());
                if (valueOrig.isObject()
                        && valueNew != null
                        && valueNew.isObject()
                        && !isEqual((ObjectNode) valueOrig, (ObjectNode) valueNew)) {
                    return false;
                } else if (valueOrig.isArray()
                        && valueNew != null
                        && valueNew.isArray()
                        && !isEqual((ArrayNode) valueOrig, (ArrayNode) valueNew)) {
                    return false;
                } else if (valueOrig.isValueNode()
                        && valueNew != null
                        && valueNew.isValueNode()
                        && !isEqualPrimitive(valueOrig, valueNew)) {
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
                        if (!isEqualPrimitive(valueOrig, valueNew)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isEqualPrimitive(JsonNode primitiveOrig, JsonNode primitiveNew) {
        if (primitiveOrig == null && primitiveNew == null) {
            return true;
        } else if (primitiveOrig == null || primitiveNew == null) {
            return false;
        } else {
            if (primitiveOrig.isTextual() && primitiveNew.isTextual()) {
                return primitiveOrig.asText().equals(primitiveNew.asText());
            } else if (primitiveOrig.isBoolean() && primitiveNew.isBoolean()) {
                return primitiveOrig.asBoolean() == primitiveNew.asBoolean();
            } else if (primitiveOrig.isNumber() && primitiveNew.isNumber()) {
                return Double.compare(primitiveOrig.asDouble(), primitiveNew.asDouble()) == 0;
            } else {
                return primitiveOrig.isNull() && primitiveNew.isNull();
            }
        }
    }
}
