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
package org.apache.airavata.gfac.deployment;

import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DeploymentEngine {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentEngine.class);

    public static final String PLUGINS_DIR = "plugins";
    private GFacConfiguration configuration = null;
    private File repoPath = null;
    private File extensionDirectory = null;
    public DeploymentEngine(GFacConfiguration configuration) {
        this.configuration = configuration;
    }

    public void loadRepository(String repoDir) throws DeploymentException {
        repoPath = new File(repoDir);
        if(!repoPath.exists()){
            logger.error("repository path is missing");
        }
        if(extensionDirectory == null) {
            extensionDirectory = new File(repoDir, PLUGINS_DIR);
        }
        if(!extensionDirectory.exists()){
            logger.error("plugins directory is missing");
        }
    }

}
