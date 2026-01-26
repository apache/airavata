/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.entities.appcatalog;

import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.common.model.DeploymentCommandType;

/**
 * Composite primary key class for the APPLICATION_DEPLOYMENT_COMMAND table.
 *
 * <p>The key consists of:
 * <ul>
 *   <li>deploymentId - the application deployment this command belongs to</li>
 *   <li>commandType - the type of command (PREJOB, POSTJOB, MODULE_LOAD)</li>
 *   <li>command - the actual command string</li>
 * </ul>
 *
 * <p>Note: The combination of deploymentId, commandType, and command must be unique.
 * The commandOrder field is not part of the primary key but is used for ordering
 * commands within each type.
 */
public class ApplicationDeploymentCommandEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String deploymentId;
    private DeploymentCommandType commandType;
    private String command;

    public ApplicationDeploymentCommandEntityPK() {}

    public ApplicationDeploymentCommandEntityPK(
            String deploymentId, DeploymentCommandType commandType, String command) {
        this.deploymentId = deploymentId;
        this.commandType = commandType;
        this.command = command;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public DeploymentCommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(DeploymentCommandType commandType) {
        this.commandType = commandType;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationDeploymentCommandEntityPK that = (ApplicationDeploymentCommandEntityPK) o;
        return Objects.equals(deploymentId, that.deploymentId)
                && commandType == that.commandType
                && Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, commandType, command);
    }

    @Override
    public String toString() {
        return "ApplicationDeploymentCommandEntityPK{"
                + "deploymentId='" + deploymentId + '\''
                + ", commandType=" + commandType
                + ", command='" + command + '\''
                + '}';
    }
}
