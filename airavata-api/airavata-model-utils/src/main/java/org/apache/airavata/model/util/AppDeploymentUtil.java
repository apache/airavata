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
package org.apache.airavata.model.util;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;

public class AppDeploymentUtil {
    public static ApplicationDeploymentDescription createAppDeployment (String moduleId,
                                                                        String computeHost,
                                                                        String executablePath,
                                                                        String appDepDescription,
                                                                        String moduleLoadCmd){
        ApplicationDeploymentDescription description = new ApplicationDeploymentDescription();
        description.setAppModuleId(moduleId);
        description.setComputeHostId(computeHost);
        description.setExecutablePath(executablePath);
        description.setAppDeploymentDescription(appDepDescription);
        //TODO
//        description.setModuleLoadCmd(moduleLoadCmd);
        return description;
    }

    public static SetEnvPaths createEnvPath (String name,
                                             String val){
        SetEnvPaths setEnvPaths = new SetEnvPaths();
        setEnvPaths.setName(name);
        setEnvPaths.setValue(val);
        return setEnvPaths;
    }

}
