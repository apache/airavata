/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public class JSONUtil {


    public static void saveJSON(JsonElement jsonElement, File file) throws IOException {
        IOUtil.writeToFile(jsonElementToString(jsonElement), file);
    }

    public static JsonObject stringToJSONObject(String workflowString) {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(workflowString);
    }

    public static JsonObject loadJSON(File file) throws IOException {
        return loadJSON(new FileReader(file));
    }

    public static JsonObject loadJSON(Reader reader) throws IOException {
       JsonParser parser = new JsonParser();
       JsonElement jsonElement = parser.parse(reader);
        if (jsonElement instanceof JsonObject) {
            return (JsonObject) jsonElement;
        } else {
            throw new RuntimeException("Error while loading Json from file");
        }

    }

    public static String jsonElementToString(JsonElement jsonElement) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }

    public static boolean isEqual(JsonObject originalJsonObject, JsonObject newJsonObject) {
        // TODO - Implement this method
        if (originalJsonObject == null && newJsonObject == null) {
            return true;
        }else if (originalJsonObject == null || newJsonObject == null) {
            return false;
        } else {
            // check the number of childs
            Set<Map.Entry<String , JsonElement>> entrySetOfOriginalJson =  originalJsonObject.entrySet();
            Set<Map.Entry<String , JsonElement>> entrySetOfNewJson =  newJsonObject.entrySet();
            if (entrySetOfOriginalJson.size() != entrySetOfNewJson.size()) {
                return false;
            }

            for (Map.Entry<String, JsonElement> keyString : entrySetOfOriginalJson) {
                JsonElement valueOrig = keyString.getValue();
                JsonElement valueNew = newJsonObject.get(keyString.getKey());
                if (valueOrig instanceof JsonObject && valueNew instanceof JsonObject &&
                        !isEqual((JsonObject) valueOrig, (JsonObject) valueNew)) {
                    return false;
                }else if (valueOrig instanceof JsonArray && valueNew instanceof JsonArray &&
                        !isEqual((JsonArray) valueOrig, (JsonArray) valueNew)) {
                    return false;
                }else if (valueOrig instanceof JsonPrimitive && valueNew instanceof JsonPrimitive &&
                        !isEqual((JsonPrimitive) valueOrig, (JsonPrimitive) valueNew)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isEqual(JsonArray arrayOriginal, JsonArray arrayNew) {
        if (arrayOriginal == null && arrayNew == null) {
            return true;
        }else if (arrayOriginal == null || arrayNew == null) {
            return false;
        }else {
            // check the number of element
            if (arrayOriginal.size() != arrayNew.size()) {
                return false;
            }else if (arrayOriginal.size() == 0) {
                return true;
            } else {
                for (int i = 0; i < arrayOriginal.size(); i++) {
                    JsonElement valueOrig = arrayOriginal.get(i);
                    JsonElement valueNew = arrayNew.get(i);
                    if (valueOrig instanceof JsonObject && valueNew instanceof JsonObject) {
                        if (!isEqual((JsonObject) valueOrig, (JsonObject) valueNew)) {
                            return false;
                        }
                    }else if (valueOrig instanceof JsonPrimitive && valueNew instanceof JsonPrimitive) {
                        if (!isEqual((JsonPrimitive) valueOrig, (JsonPrimitive) valueNew)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isEqual(JsonPrimitive primitiveOrig, JsonPrimitive primitiveNew) {
        if (primitiveOrig == null && primitiveNew == null) {
            return true;
        }else if (primitiveOrig == null || primitiveNew == null) {
            return false;
        } else {
            if (primitiveOrig.isString() && primitiveNew.isString()){
                if(!primitiveOrig.getAsString().equals(primitiveNew.getAsString())) {
                    return false;
                }
            }else if (primitiveOrig.isBoolean() && primitiveNew.isBoolean()) {
                if ((Boolean.valueOf(primitiveOrig.getAsBoolean()).compareTo(primitiveNew.getAsBoolean()) != 0)) {
                    return false;
                }
            }else if (primitiveOrig.isNumber() && primitiveNew.isNumber() ) {
                if (new Double(primitiveOrig.getAsDouble()).compareTo(primitiveNew.getAsDouble()) != 0) {
                    return false;
                }
            }else {
                return primitiveOrig.isJsonNull() && primitiveNew.isJsonNull();
            }
        }
        return true;
    }
}
