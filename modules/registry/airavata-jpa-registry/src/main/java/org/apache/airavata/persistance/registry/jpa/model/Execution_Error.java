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

package org.apache.airavata.persistance.registry.jpa.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class Execution_Error {
    @Id @GeneratedValue
    private int error_id;
    private String experiment_ID;
    private String workflow_instanceID;
    private String node_id;
    private String gfacJobID;
    private String source_type;
    private Timestamp error_date;
    private String error_reporter;
    private String error_location;
    private String action_taken;
    private int error_reference;

    @ManyToOne()
    @JoinColumn(name = "experiment_ID")
    private Experiment_Data experiment_data;

    @ManyToOne()
    @JoinColumn(name = "workflow_instanceID")
    private Workflow_Data workflow_Data;

    @Lob
    private String error_msg;
    @Lob
    private String error_des;
    private String error_code;

    public String getWorkflow_instanceID() {
        return workflow_instanceID;
    }

    public String getNode_id() {
        return node_id;
    }

    public Workflow_Data getWorkflow_Data() {
        return workflow_Data;
    }

    public String getError_msg() {
        return error_msg;
    }

    public String getError_des() {
        return error_des;
    }

    public String getError_code() {
        return error_code;
    }

    public void setWorkflow_instanceID(String workflow_instanceID) {
        this.workflow_instanceID = workflow_instanceID;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public void setWorkflow_Data(Workflow_Data workflow_Data) {
        this.workflow_Data = workflow_Data;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public void setError_des(String error_des) {
        this.error_des = error_des;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public int getError_id() {
        return error_id;
    }

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public String getGfacJobID() {
        return gfacJobID;
    }

    public String getSource_type() {
        return source_type;
    }

    public Timestamp getError_date() {
        return error_date;
    }

    public Experiment_Data getExperiment_Data() {
        return experiment_data;
    }

    public void setError_id(int error_id) {
        this.error_id = error_id;
    }

    public void setExperiment_ID(String experiment_ID) {
        this.experiment_ID = experiment_ID;
    }

    public void setGfacJobID(String gfacJobID) {
        this.gfacJobID = gfacJobID;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public void setError_date(Timestamp error_date) {
        this.error_date = error_date;
    }

    public void setExperiment_data(Experiment_Data experiment_data) {
        this.experiment_data = experiment_data;
    }

    public String getError_reporter() {
        return error_reporter;
    }

    public String getError_location() {
        return error_location;
    }

    public String getAction_taken() {
        return action_taken;
    }

    public Experiment_Data getExperiment_data() {
        return experiment_data;
    }

    public void setError_reporter(String error_reporter) {
        this.error_reporter = error_reporter;
    }

    public void setError_location(String error_location) {
        this.error_location = error_location;
    }

    public void setAction_taken(String action_taken) {
        this.action_taken = action_taken;
    }

    public int getError_reference() {
        return error_reference;
    }

    public void setError_reference(int error_reference) {
        this.error_reference = error_reference;
    }
}
