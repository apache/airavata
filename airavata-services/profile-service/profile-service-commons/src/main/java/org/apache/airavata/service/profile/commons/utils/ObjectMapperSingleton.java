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
package org.apache.airavata.service.profile.commons.utils;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectMapperSingleton {
    private final static Logger logger = LoggerFactory.getLogger(ObjectMapperSingleton.class);

    private static ObjectMapperSingleton instance;
    private final Mapper mapper;

    private ObjectMapperSingleton() {
        mapper = DozerBeanMapperBuilder.buildDefault();
    }

    public static ObjectMapperSingleton getInstance() {
        if (instance == null)
            instance = new ObjectMapperSingleton();
        return instance;
    }

    public Mapper getMapper() {
        return mapper;
    }
}