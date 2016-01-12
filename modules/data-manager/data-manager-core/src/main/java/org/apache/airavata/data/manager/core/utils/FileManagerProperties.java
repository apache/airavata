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
package org.apache.airavata.data.manager.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class FileManagerProperties {
    private final static Logger logger = LoggerFactory.getLogger(FileManagerProperties.class);

    private static FileManagerProperties instance;

    private Properties properties;

    private FileManagerProperties() throws IOException {
        properties = new Properties();
        properties.load(FileManagerProperties.class.getClassLoader().getResourceAsStream("file-manager.properties"));
    }

    public static FileManagerProperties getInstance() throws IOException {
        if(instance == null){
            instance = new FileManagerProperties();
        }
        return instance;
    }

    public String getProperty(String field, String defaultVal){
        String returnVal = properties.getProperty(field);
        if(returnVal == null)
            return defaultVal;
        else
            return returnVal;
    }
}