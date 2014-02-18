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

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;

import java.sql.Timestamp;
import java.util.List;

public class ErrorDetailResource extends AbstractResource {
    private int errorId;
    private String expId;
    private String taskId;
    private String nodeInstanceId;
    private Timestamp creationTime;
    private String actualErrorMsg;
    private String userFriendlyErrorMsg;
    private boolean transientPersistent;
    private String errorCategory;
    private String correctiveAction;
    private String actionableGroup;

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getNodeInstanceId() {
        return nodeInstanceId;
    }

    public void setNodeInstanceId(String nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getActualErrorMsg() {
        return actualErrorMsg;
    }

    public void setActualErrorMsg(String actualErrorMsg) {
        this.actualErrorMsg = actualErrorMsg;
    }

    public String getUserFriendlyErrorMsg() {
        return userFriendlyErrorMsg;
    }

    public void setUserFriendlyErrorMsg(String userFriendlyErrorMsg) {
        this.userFriendlyErrorMsg = userFriendlyErrorMsg;
    }

    public boolean isTransientPersistent() {
        return transientPersistent;
    }

    public void setTransientPersistent(boolean transientPersistent) {
        this.transientPersistent = transientPersistent;
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public String getActionableGroup() {
        return actionableGroup;
    }

    public void setActionableGroup(String actionableGroup) {
        this.actionableGroup = actionableGroup;
    }

    @Override
    public Resource create(ResourceType type) {
        return null;
    }

    @Override
    public void remove(ResourceType type, Object name) {

    }

    @Override
    public Resource get(ResourceType type, Object name) {
        return null;
    }

    @Override
    public List<Resource> get(ResourceType type) {
        return null;
    }

    @Override
    public void save() {

    }
}
