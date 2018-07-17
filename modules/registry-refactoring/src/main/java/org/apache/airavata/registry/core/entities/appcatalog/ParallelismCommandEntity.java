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

import org.apache.airavata.model.parallelism.ApplicationParallelismType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the parallelism_command database table.
 */
@Entity
@Table(name = "PARALLELISM_COMMAND")
@IdClass(ParallelismCommandPK.class)
public class ParallelismCommandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_JOB_MANAGER_ID")
    private String resourceJobManagerId;

    @Id
    @Column(name = "COMMAND_TYPE")
    @Enumerated(EnumType.STRING)
    private ApplicationParallelismType commandType;

    @Column(name = "COMMAND")
    private String command;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "RESOURCE_JOB_MANAGER_ID")
    private ResourceJobManagerEntity resourceJobManager;

    public ParallelismCommandEntity() {
    }

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public ApplicationParallelismType getCommandType() {
        return commandType;
    }

    public void setCommandType(ApplicationParallelismType commandType) {
        this.commandType = commandType;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ResourceJobManagerEntity getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(ResourceJobManagerEntity resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }
}