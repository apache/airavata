/**
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
 */
package org.apache.airavata.k8s.api.server.model.data;

import org.apache.airavata.k8s.api.server.model.experiment.ExperimentInputData;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentOutputData;
import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "DATA_STORE")
public class DataStoreModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(length = 1000000, name = "CONTENT")
    @Basic(fetch = FetchType.LAZY)
    private byte[] content;

    @ManyToOne
    private ExperimentOutputData experimentOutputData;

    @ManyToOne
    private TaskModel taskModel;

    public long getId() {
        return id;
    }

    public DataStoreModel setId(long id) {
        this.id = id;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public DataStoreModel setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public ExperimentOutputData getExperimentOutputData() {
        return experimentOutputData;
    }

    public DataStoreModel setExperimentOutputData(ExperimentOutputData experimentOutputData) {
        this.experimentOutputData = experimentOutputData;
        return this;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public DataStoreModel setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }
}
