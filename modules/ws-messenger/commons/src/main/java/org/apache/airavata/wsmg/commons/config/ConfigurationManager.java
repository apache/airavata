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

package org.apache.airavata.wsmg.commons.config;

import java.util.Properties;

public class ConfigurationManager {

    protected Properties configurations = new Properties();

    public ConfigurationManager(String configFileName) {
        try {
            configurations.load(this.getClass().getClassLoader().getResourceAsStream(configFileName));
        } catch (Exception e) {
            throw new RuntimeException("unable to load configurations:::" + configFileName, e);
        }
    }

    public String getConfig(String configName) {
        return configurations.getProperty(configName);
    }

    public String getConfig(String configName, String defaultVal) {
        return configurations.getProperty(configName, defaultVal);
    }

    public int getConfig(String configName, int defaultVal) {
        return Integer.parseInt(configurations.getProperty(configName, Integer.toString(defaultVal)));
    }
    
    public long getConfig(String configName, long defaultVal) {
        return Long.parseLong(configurations.getProperty(configName, Long.toString(defaultVal)));
    }
}
