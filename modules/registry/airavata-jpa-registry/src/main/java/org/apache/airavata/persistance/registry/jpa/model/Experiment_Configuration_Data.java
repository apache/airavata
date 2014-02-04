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
@Table(name ="EXPERIMENT_CONFIGURATION_DATA")
public class Experiment_Configuration_Data {
    @Id
    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment_Metadata experiment_metadata;
    @Column(name = "RESOURCE_HOST_ID")
    private String resource_host_id;
    @Column(name = "TOTAL_CPU_COUNT")
    private int total_cpu_count;
    @Column(name = "NODE_COUNT")
    private int node_count;

    @Column(name = "NUMBER_OF_THREADS")
    private int number_of_threads;
    @Column(name = "QUEUE_NAME")
    private String queue_name;
    @Column(name = "WALLTIME_LIMIT")
    private int walltime_limit;
    @Column(name = "JOB_START_TIME")
    private Timestamp job_start_time;
    @Column(name = "TOTAL_PHYSICAL_MEMORY")
    private int total_physical_memory;
    @Column(name = "COMPUTATIONAL_PROJECT_ACCOUNT")
    private String computational_project_account;

    @Column(name = "AIRAVATA_AUTO_SCHEDULE")
    private boolean airavata_auto_schedule;
    @Column(name = "OVERRIDE_MANUAL_SCHEDULE_PARAMS")
    private boolean override_manual_schedule;
    @Column(name = "WORKING_DIR")
    private String working_dir;
    @Column(name = "STAGE_INPUT_FILES_TO_WORKING_DIR")
    private boolean stage_input_files_to_working_dir;
    @Column(name = "OUTPUT_DATA_DIR")
    private String output_data_dir;
    @Column(name = "DATA_REG_URL")
    private String data_reg_url;
    @Column(name = "PERSIST_OUTPUT_DATA")
    private boolean persist_output_data;
    @Column(name = "CLEAN_AFTER_JOB")
    private boolean clean_after_job;
    @Lob
    @Column(name = "EXPERIMENT_CONFIG_DATA")
    private byte[] experiment_config_data;

    public Experiment_Metadata getExperiment_metadata() {
        return experiment_metadata;
    }

    public void setExperiment_metadata(Experiment_Metadata experiment_metadata) {
        this.experiment_metadata = experiment_metadata;
    }

    public String getResource_host_id() {
        return resource_host_id;
    }

    public void setResource_host_id(String resource_host_id) {
        this.resource_host_id = resource_host_id;
    }

    public int getTotal_cpu_count() {
        return total_cpu_count;
    }

    public void setTotal_cpu_count(int total_cpu_count) {
        this.total_cpu_count = total_cpu_count;
    }

    public int getNode_count() {
        return node_count;
    }

    public void setNode_count(int node_count) {
        this.node_count = node_count;
    }

    public int getNumber_of_threads() {
        return number_of_threads;
    }

    public void setNumber_of_threads(int number_of_threads) {
        this.number_of_threads = number_of_threads;
    }

    public String getQueue_name() {
        return queue_name;
    }

    public void setQueue_name(String queue_name) {
        this.queue_name = queue_name;
    }

    public int getWalltime_limit() {
        return walltime_limit;
    }

    public void setWalltime_limit(int walltime_limit) {
        this.walltime_limit = walltime_limit;
    }

    public Timestamp getJob_start_time() {
        return job_start_time;
    }

    public void setJob_start_time(Timestamp job_start_time) {
        this.job_start_time = job_start_time;
    }

    public int getTotal_physical_memory() {
        return total_physical_memory;
    }

    public void setTotal_physical_memory(int total_physical_memory) {
        this.total_physical_memory = total_physical_memory;
    }

    public String getComputational_project_account() {
        return computational_project_account;
    }

    public void setComputational_project_account(String computational_project_account) {
        this.computational_project_account = computational_project_account;
    }

    public boolean isAiravata_auto_schedule() {
        return airavata_auto_schedule;
    }

    public void setAiravata_auto_schedule(boolean airavata_auto_schedule) {
        this.airavata_auto_schedule = airavata_auto_schedule;
    }

    public boolean isOverride_manual_schedule() {
        return override_manual_schedule;
    }

    public void setOverride_manual_schedule(boolean override_manual_schedule) {
        this.override_manual_schedule = override_manual_schedule;
    }

    public String getWorking_dir() {
        return working_dir;
    }

    public void setWorking_dir(String working_dir) {
        this.working_dir = working_dir;
    }

    public boolean isStage_input_files_to_working_dir() {
        return stage_input_files_to_working_dir;
    }

    public void setStage_input_files_to_working_dir(boolean stage_input_files_to_working_dir) {
        this.stage_input_files_to_working_dir = stage_input_files_to_working_dir;
    }

    public String getOutput_data_dir() {
        return output_data_dir;
    }

    public void setOutput_data_dir(String output_data_dir) {
        this.output_data_dir = output_data_dir;
    }

    public String getData_reg_url() {
        return data_reg_url;
    }

    public void setData_reg_url(String data_reg_url) {
        this.data_reg_url = data_reg_url;
    }

    public boolean isPersist_output_data() {
        return persist_output_data;
    }

    public void setPersist_output_data(boolean persist_output_data) {
        this.persist_output_data = persist_output_data;
    }

    public boolean isClean_after_job() {
        return clean_after_job;
    }

    public void setClean_after_job(boolean clean_after_job) {
        this.clean_after_job = clean_after_job;
    }

    public byte[] getExperiment_config_data() {
        return experiment_config_data;
    }

    public void setExperiment_config_data(byte[] experiment_config_data) {
        this.experiment_config_data = experiment_config_data;
    }
}
