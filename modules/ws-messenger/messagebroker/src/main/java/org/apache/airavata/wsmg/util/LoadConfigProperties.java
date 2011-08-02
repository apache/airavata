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

package org.apache.airavata.wsmg.util;

import java.io.File;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.Logger;

public class LoadConfigProperties {

    private static Logger logger = Logger.getLogger(LoadConfigProperties.class);

    public static LoadConfigProperties confProp = null;
    private String axisRepo = null;
    private ConfigurationContext configContext = null;

    private LoadConfigProperties() {

        String axis2RepoDir = System.getProperty("axis2.repository");

        if (axis2RepoDir == null) {
            String axis2Home = System.getenv("AXIS2_HOME");

            if (axis2Home != null) {
                axis2Home = axis2Home.trim();
                axis2RepoDir = axis2Home.endsWith("/") ? axis2Home + "repository" : axis2Home + "/" + "repository";

            }
        }
        File repoDir = null;
        if (axis2RepoDir != null)
            repoDir = new File(axis2RepoDir);

        if (repoDir != null && repoDir.isDirectory()) {

            try {
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoDir
                        .getAbsolutePath());

                axisRepo = repoDir.getAbsolutePath();

            } catch (AxisFault e) {
                logger.error("unable to load the repository", e);
            }

        }

    }

    public static LoadConfigProperties getInstance() {
        if (confProp == null)
            confProp = new LoadConfigProperties();
        return confProp;
    }

    public String getAxisRepo() {
        return axisRepo;
    }

    public boolean hasAxisRepo() {

        logger.info("repo is : " + axisRepo);

        return axisRepo != null;
    }

    public ConfigurationContext getConfCtxFromFileSystem() {
        return configContext;
    }
}
