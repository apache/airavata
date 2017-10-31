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
package org.apache.airavata.k8s.api.server.service.data;

import org.apache.airavata.k8s.api.resources.data.DataEntryResource;
import org.apache.airavata.k8s.api.server.model.data.DataStoreModel;
import org.apache.airavata.k8s.api.server.repository.DataStoreRepository;
import org.apache.airavata.k8s.api.server.repository.ExperimentOutputDataRepository;
import org.apache.airavata.k8s.api.server.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class DataStoreService {

    private DataStoreRepository dataStoreRepository;
    private TaskRepository taskRepository;
    private ExperimentOutputDataRepository experimentOutputDataRepository;

    public DataStoreService(DataStoreRepository dataStoreRepository,
                            TaskRepository taskRepository,
                            ExperimentOutputDataRepository experimentOutputDataRepository) {
        this.dataStoreRepository = dataStoreRepository;
        this.taskRepository = taskRepository;
        this.experimentOutputDataRepository = experimentOutputDataRepository;
    }

    public long createEntry(long taskId, long expOutId, byte[] content) {
        DataStoreModel model = new DataStoreModel();
        model.setTaskModel(taskRepository.findById(taskId).get())
                .setExperimentOutputData(experimentOutputDataRepository.findById(expOutId).get())
                .setContent(content);
        return dataStoreRepository.save(model).getId();
    }

    public List<DataEntryResource> getEntriesForProcess(long processId) {
        List<DataEntryResource> entries = new ArrayList<>();
        List<DataStoreModel> dataStoreModels = this.dataStoreRepository.findByTaskModel_ParentProcess_Id(processId);
        Optional.ofNullable(dataStoreModels).ifPresent(models -> models.forEach(model -> entries.add(new DataEntryResource()
                .setId(model.getId())
                .setName(model.getExperimentOutputData().getName())
                .setDataType(model.getExperimentOutputData().getType().name()))));
        return entries;
    }
}
