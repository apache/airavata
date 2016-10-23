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

package org.apache.airavata.testsuite.multitenantedairavata.utils;

import org.apache.airavata.common.utils.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class PropertyReader {
    private final static Logger logger = LoggerFactory.getLogger(PropertyReader.class);
    private static Properties airavataClientProperties = new Properties();

    public PropertyReader() {
        try {
            loadProperties();
        } catch (IOException e) {
            logger.error("Unable to read properties files", e);
        }
    }

    protected void loadProperties() throws IOException {
        URL airavataURL = ApplicationSettings.loadFile(TestFrameworkConstants.AIRAVATA_CLIENT_PROPERTIES);
        if (airavataURL != null){
            airavataClientProperties.load(airavataURL.openStream());
        }
    }

    public String readProperty (String propertyName, PropertyFileType type){
        switch (type){
            case AIRAVATA_CLIENT:
                return airavataClientProperties.getProperty(propertyName);
        }
        return null;
    }
}
