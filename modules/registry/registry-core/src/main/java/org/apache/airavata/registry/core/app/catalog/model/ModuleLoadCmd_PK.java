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
package org.apache.airavata.registry.core.app.catalog.model;

import java.io.Serializable;

public class ModuleLoadCmd_PK implements Serializable {
    private String cmd;
    private String appDeploymentId;

    public ModuleLoadCmd_PK(){
    }

    public ModuleLoadCmd_PK(String cmd, String appDeploymentId){
        this.cmd = cmd;
        this.appDeploymentId = appDeploymentId;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getCmd() {
        return cmd;
    }

    public String getAppDeploymentId() {
        return appDeploymentId;
    }

    public void setCmd(String cmd) {
        this.cmd=cmd;
    }

    public void setAppDeploymentId(String appDeploymentId) {
        this.appDeploymentId=appDeploymentId;
    }
}
