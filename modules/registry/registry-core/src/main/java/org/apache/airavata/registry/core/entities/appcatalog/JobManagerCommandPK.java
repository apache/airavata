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
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;

import java.io.Serializable;

/**
 * The primary key class for the job_manager_command database table.
 */
public class JobManagerCommandPK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    private String resourceJobManagerId;
    private JobManagerCommand commandType;

    public JobManagerCommandPK() {
    }

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public JobManagerCommand getCommandType() {
        return commandType;
    }

    public void setCommandType(JobManagerCommand commandType) {
        this.commandType = commandType;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JobManagerCommandPK)) {
            return false;
        }
        JobManagerCommandPK castOther = (JobManagerCommandPK) other;
        return
                this.resourceJobManagerId.equals(castOther.resourceJobManagerId)
                        && this.commandType.equals(castOther.commandType);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.resourceJobManagerId.hashCode();
        hash = hash * prime + this.commandType.hashCode();

        return hash;
    }
}