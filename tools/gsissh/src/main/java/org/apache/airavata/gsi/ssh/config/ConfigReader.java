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

package org.apache.airavata.gsi.ssh.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads basic configurations.
 */
public class ConfigReader {

    private static final String CONFIGURATION_FILE = "gsissh.properties";


    private Properties properties;

    /**
     * Reads configurations from the class path configuration file.
     * @throws IOException If an error occurred while reading configurations.
     */
    public ConfigReader() throws IOException {
        this.properties = getPropertiesFromClasspath(CONFIGURATION_FILE);
    }

    private Properties getPropertiesFromClasspath(String propFileName) throws IOException {
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(propFileName);

        if (inputStream == null) {
            throw new FileNotFoundException("System configuration file '" + propFileName
                    + "' not found in the classpath");
        }

        props.load(inputStream);

        return props;
    }

    public String getConfiguration(String key) {
        return this.properties.getProperty(key);
    }

    /**
     * Gets all the SSH related properties used by JSch.
     * @return All properties.
     */
    public Properties getProperties() {
        return this.properties;
    }


}
