/*
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
 *
*/
package org.apache.airavata.userprofile.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.airavata.model.user.UserProfile;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ModelConversionHelper {
    private final static Logger logger = LoggerFactory.getLogger(ModelConversionHelper.class);
    private ObjectMapper objectMapper;

    public ModelConversionHelper(){
        init();
    }

    /**
     * Private method to register the custom serializers and deserializers
     */
    private void init() {
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("UserProfileModule",
                new Version(1, 0, 0, null, null, null));

        module.addSerializer(UserProfile.class, new UserProfileSerializer());
        module.addDeserializer(UserProfile.class, new UserProfileDeserializer());

        objectMapper.registerModule(module);
    }

    /**
     * Method to serialize a thrift object to json
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public String serializeObject(TBase object) throws JsonProcessingException {
        String json = this.objectMapper.writeValueAsString(object);
        return json;
    }

    /**
     * Method to deserialize a json to the thrift object
     * @param clz
     * @param json
     * @return
     * @throws IOException
     */
    public TBase deserializeObject(Class<?> clz, String json) throws IOException {
        return (TBase)this.objectMapper.readValue(json, clz);
    }
}