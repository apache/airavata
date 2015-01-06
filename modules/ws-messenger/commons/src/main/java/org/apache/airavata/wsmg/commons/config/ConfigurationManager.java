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


import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {
    protected static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
//    protected Properties configurations = new Properties();
    
//    public ConfigurationManager(String configFileName) {
//        try {
//            configurations.load(this.getClass().getClassLoader().getResourceAsStream(configFileName));
//        } catch (Exception e) {
//            throw new RuntimeException("unable to load configurations:::" + configFileName, e);
//        }
//    }

    public String getConfig(String configName) throws Exception {
        try {
			return ServerSettings.getSetting(configName);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
		}
    }

    public String getConfig(String configName, String defaultVal) {
        return ServerSettings.getSetting(configName, defaultVal);
    }

    public int getConfig(String configName, int defaultVal) {
        return Integer.parseInt(getConfig(configName, Integer.toString(defaultVal)));
    }

    public long getConfig(String configName, long defaultVal) throws ApplicationSettingsException {
        return Long.parseLong(getConfig(configName, Long.toString(defaultVal)));
    }
}
