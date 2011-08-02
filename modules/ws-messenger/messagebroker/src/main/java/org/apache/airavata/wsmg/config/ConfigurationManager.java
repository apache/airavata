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

package org.apache.airavata.wsmg.config;

import java.io.File;
import java.util.Properties;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axis2.Constants;
import org.apache.log4j.Logger;

public class ConfigurationManager {

    private Logger logger = Logger.getLogger(ConfigurationManager.class);

    protected Properties configurations = new Properties();

    public ConfigurationManager(String configFileName) {

        try {

            configurations.load(this.getClass().getClassLoader().getResourceAsStream(configFileName));

        } catch (Exception e) {
            throw new RuntimeException("unable to load configurations", e);
        }

    }

    private void configureSystemProperties(Properties p) {
        String axis2Repo = getConfig(WsmgCommonConstants.CONFIG_AXIS2_REPO, null);

        if (!dirExists(axis2Repo)) {

            String axis2Home = System.getenv("AXIS2_HOME");

            axis2Repo = axis2Home.endsWith(File.separator) ? axis2Home + axis2Repo : axis2Home + File.separator
                    + axis2Repo;

        }

        if (axis2Repo != null) {

            logger.info("axis2 repository :" + axis2Repo);
            System.setProperty(Constants.AXIS2_REPO, axis2Repo);
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

    private boolean dirExists(String path) {
        File f = new File(path);

        return f.isDirectory();
    }

}
