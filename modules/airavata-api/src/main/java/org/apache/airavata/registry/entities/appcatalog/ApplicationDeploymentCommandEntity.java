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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.common.model.DeploymentCommandType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Unified entity for application deployment commands.
 *
 * <p>This entity consolidates the following legacy entities:
 * <ul>
 *   <li>PrejobCommandEntity - Commands executed before the job</li>
 *   <li>PostjobCommandEntity - Commands executed after the job</li>
 *   <li>ModuleLoadCmdEntity - Module load commands for environment setup</li>
 * </ul>
 *
 * <p>The commandType discriminator identifies the type of command. Commands are
 * ordered within each type using the commandOrder field.
 *
 * @see DeploymentCommandType
 * @see ApplicationDeploymentCommandEntityPK
 */
@Entity(name = "ApplicationDeploymentCommandEntity")
@Table(
        name = "APPLICATION_DEPLOYMENT_COMMAND",
        indexes = {
            @Index(name = "idx_deploy_cmd_deployment", columnList = "DEPLOYMENT_ID"),
            @Index(name = "idx_deploy_cmd_type", columnList = "COMMAND_TYPE"),
            @Index(name = "idx_deploy_cmd_deployment_type", columnList = "DEPLOYMENT_ID, COMMAND_TYPE"),
            @Index(name = "idx_deploy_cmd_order", columnList = "DEPLOYMENT_ID, COMMAND_TYPE, COMMAND_ORDER")
        })
@IdClass(ApplicationDeploymentCommandEntityPK.class)
public class ApplicationDeploymentCommandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // ============================================
    // PRIMARY KEY FIELDS
    // ============================================

    @Id
    @Column(name = "DEPLOYMENT_ID", nullable = false)
    private String deploymentId;

    @Id
    @Column(name = "COMMAND_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeploymentCommandType commandType;

    @Id
    @Column(name = "COMMAND", nullable = false, length = 1024)
    private String command;

    // ============================================
    // DATA FIELDS
    // ============================================

    @Column(name = "COMMAND_ORDER")
    private int commandOrder;

    // ============================================
    // RELATIONSHIPS
    // ============================================

    @ManyToOne(targetEntity = ApplicationDeploymentEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "DEPLOYMENT_ID", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ApplicationDeploymentEntity applicationDeployment;

    public ApplicationDeploymentCommandEntity() {}

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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
