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

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name ="GFAC_JOB_DATA")
public class GFac_Job_Data {
    @Column(name = "EXPERIMENT_ID")
    private String experiment_ID;
    @Column(name = "WORKFLOW_INSTANCE_ID")
    private String workflow_instanceID;
    @Column(name = "NODE_ID")
    private String  node_id;
    @Column(name = "APPLICATION_DESC_ID")
    private String application_descriptor_ID;
    @Column(name = "HOST_DESC_ID")
    private String host_descriptor_ID;
    @Column(name = "SERVICE_DESC_ID")
    private String service_descriptor_ID;
    @Lob
    @Column(name = "JOB_DATA")
    private String job_data;
    @Id
    @Column(name = "LOCAL_JOB_ID")
    private String local_Job_ID;
    @Column(name = "SUBMITTED_TIME")
    private Timestamp  submitted_time;
    @Column(name = "STATUS_UPDATE_TIME")
    private Timestamp  status_update_time;
    @Column(name = "STATUS")
    private String status;
    @Lob
    @Column(name = "METADATA")
    private String metadata;

    @ManyToOne()
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment_Metadata experiment_metadata;

    @ManyToOne()
    @JoinColumn(name = "WORKFLOW_INSTANCE_ID")
    private Workflow_Data workflow_Data;

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public String getWorkflow_instanceID() {
        return workflow_instanceID;
    }

    public String getNode_id() {
        return node_id;
    }

    public String getApplication_descriptor_ID() {
        return application_descriptor_ID;
    }

    public String getHost_descriptor_ID() {
        return host_descriptor_ID;
    }

    public String getService_descriptor_ID() {
        return service_descriptor_ID;
    }

    public String getJob_data() {
        return job_data;
    }

    public String getLocal_Job_ID() {
        return local_Job_ID;
    }

    public Timestamp getSubmitted_time() {
        return submitted_time;
    }

    public Timestamp getStatus_update_time() {
        return status_update_time;
    }

    public String getStatus() {
        return status;
    }

    public String getMetadata() {
        return metadata;
    }

    public Experiment_Metadata getExperiment_metadata() {
        return experiment_metadata;
    }

    public Workflow_Data getWorkflow_Data() {
        return workflow_Data;
    }

    public void setExperiment_ID(String experiment_ID) {
        this.experiment_ID = experiment_ID;
    }

    public void setWorkflow_instanceID(String workflow_instanceID) {
        this.workflow_instanceID = workflow_instanceID;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public void setApplication_descriptor_ID(String application_descriptor_ID) {
        this.application_descriptor_ID = application_descriptor_ID;
    }

    public void setHost_descriptor_ID(String host_descriptor_ID) {
        this.host_descriptor_ID = host_descriptor_ID;
    }

    public void setService_descriptor_ID(String service_descriptor_ID) {
        this.service_descriptor_ID = service_descriptor_ID;
    }

    public void setJob_data(String job_data) {
        this.job_data = job_data;
    }

    public void setLocal_Job_ID(String local_Job_ID) {
        this.local_Job_ID = local_Job_ID;
    }

    public void setSubmitted_time(Timestamp submitted_time) {
        this.submitted_time = submitted_time;
    }

    public void setStatus_update_time(Timestamp status_update_time) {
        this.status_update_time = status_update_time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setExperiment_metadata(Experiment_Metadata experiment_metadata) {
        this.experiment_metadata = experiment_metadata;
    }

    public void setWorkflow_Data(Workflow_Data workflow_Data) {
        this.workflow_Data = workflow_Data;
    }
}
