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

import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the module_load_cmd database table.
 */
@Entity
@Table(name = "MODULE_LOAD_CMD")
@IdClass(ModuleLoadCmdPK.class)
public class ModuleLoadCmdEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "APP_DEPLOYMENT_ID")
    private String appdeploymentId;

    @Id
    @Column(name = "CMD")
    private String command;

    @Column(name = "COMMAND_ORDER")
    private int commandOrder;

    @ManyToOne(targetEntity = ApplicationDeploymentEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "APP_DEPLOYMENT_ID")
    @ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
    private ApplicationDeploymentEntity applicationDeployment;

    public ModuleLoadCmdEntity() {
    }

    public String getAppdeploymentId() {
        return appdeploymentId;
    }

    public void setAppdeploymentId(String appdeploymentId) {
        this.appdeploymentId = appdeploymentId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getCommandOrder() {
        return commandOrder;
    }

    public void setCommandOrder(int commandOrder) {
        this.commandOrder = commandOrder;
    }

    public ApplicationDeploymentEntity getApplicationDeployment() {
        return applicationDeployment;
    }

    public void setApplicationDeployment(ApplicationDeploymentEntity applicationDeployment) {
        this.applicationDeployment = applicationDeployment;
    }

}
