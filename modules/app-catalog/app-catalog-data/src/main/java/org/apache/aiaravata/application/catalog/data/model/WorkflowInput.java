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

package org.apache.aiaravata.application.catalog.data.model;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "WORKFLOW_INPUT")
@IdClass(WorkflowInput_PK.class)
public class WorkflowInput implements Serializable {
    @Id
    @Column(name = "WF_TEMPLATE_ID")
    private String wfTemplateId;
    @Id
    @Column(name = "INPUT_KEY")
    private String inputKey;
    @Lob
    @Column(name = "INPUT_VALUE")
    private char[] inputVal;
    @Column(name = "DATA_TYPE")
    private String dataType;
    @Column(name = "METADATA")
    private String metadata;
    @Column(name = "APP_ARGUMENT")
    private String appArgument;
    @Column(name = "USER_FRIENDLY_DESC")
    private String userFriendlyDesc;
    @Column(name = "STANDARD_INPUT")
    private boolean standardInput;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "WF_TEMPLATE_ID")
    private Workflow workflow;

    public String getWfTemplateId() {
        return wfTemplateId;
    }

    public void setWfTemplateId(String wfTemplateId) {
        this.wfTemplateId = wfTemplateId;
    }

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public char[] getInputVal() {
        return inputVal;
    }

    public void setInputVal(char[] inputVal) {
        this.inputVal = inputVal;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public String getUserFriendlyDesc() {
        return userFriendlyDesc;
    }

    public void setUserFriendlyDesc(String userFriendlyDesc) {
        this.userFriendlyDesc = userFriendlyDesc;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }
}
