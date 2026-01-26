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
import org.apache.airavata.common.model.CommandCategory;

/**
 * The primary key class for the job_manager_command database table.
 * 
 * Composite primary key consisting of:
 * - resourceJobManagerId: The ID of the resource job manager
 * - commandCategory: The category of command (JOB_MANAGER or PARALLELISM)
 * - commandType: The specific command type as a string
 */
public class JobManagerCommandPK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    private String resourceJobManagerId;
    private CommandCategory commandCategory;
    private String commandType;

    public JobManagerCommandPK() {}

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public CommandCategory getCommandCategory() {
        return commandCategory;
    }

    public void setCommandCategory(CommandCategory commandCategory) {
        this.commandCategory = commandCategory;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JobManagerCommandPK)) {
            return false;
        }
        JobManagerCommandPK castOther = (JobManagerCommandPK) other;
        return Objects.equals(this.resourceJobManagerId, castOther.resourceJobManagerId)
                && Objects.equals(this.commandCategory, castOther.commandCategory)
                && Objects.equals(this.commandType, castOther.commandType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceJobManagerId, commandCategory, commandType);
    }
}
