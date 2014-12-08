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
@Table(name = "WORKFLOW_OUTPUT")
@IdClass(WorkflowOutput_PK.class)
public class WorkflowOutput implements Serializable {
    @Id
    @Column(name = "WF_TEMPLATE_ID")
    private String wfTemplateId;
    @Id
    @Column(name = "OUTPUT_KEY")
    private String outputKey;
    @Lob
    @Column(name = "OUTPUT_VALUE")
    private char[] outputVal;
    @Column(name = "DATA_TYPE")
    private String dataType;
    @Column(name = "VALIDITY_TYPE")
    private String validityType;
    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;
    @Column(name = "DATA_NAME_LOCATION")
    private String dataNameLocation;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "WF_TEMPLATE_ID")
    private Workflow workflow;

    public String getWfTemplateId() {
        return wfTemplateId;
    }

    public void setWfTemplateId(String wfTemplateId) {
        this.wfTemplateId = wfTemplateId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public char[] getOutputVal() {
        return outputVal;
    }

    public void setOutputVal(char[] outputVal) {
        this.outputVal = outputVal;
    }

    public String getValidityType() {
        return validityType;
    }

    public void setValidityType(String validityType) {
        this.validityType = validityType;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
    }
}
