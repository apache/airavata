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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.common.model.CommandCategory;

/**
 * The persistent class for the job_manager_command database table.
 * 
 * This entity is unified to store both job manager commands and parallelism commands.
 * The COMMAND_CATEGORY column distinguishes between JOB_MANAGER and PARALLELISM commands.
 * The COMMAND_TYPE stores the enum name as a string (e.g., "SUBMISSION", "MPI").
 */
@Entity
@Table(name = "JOB_MANAGER_COMMAND")
@IdClass(JobManagerCommandPK.class)
public class JobManagerCommandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_JOB_MANAGER_ID", nullable = false)
    private String resourceJobManagerId;

    @Id
    @Column(name = "COMMAND_CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    private CommandCategory commandCategory;

    @Id
    @Column(name = "COMMAND_TYPE", nullable = false)
    private String commandType;

    @Column(name = "COMMAND")
    private String command;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "RESOURCE_JOB_MANAGER_ID", insertable = false, updatable = false)
    private ResourceJobManagerEntity resourceJobManager;

    public JobManagerCommandEntity() {}

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

    public ResourceJobManagerEntity getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(ResourceJobManagerEntity resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
