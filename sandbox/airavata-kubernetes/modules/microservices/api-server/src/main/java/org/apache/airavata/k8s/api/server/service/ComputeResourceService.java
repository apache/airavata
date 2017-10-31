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
package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.model.compute.ComputeResourceModel;
import org.apache.airavata.k8s.api.server.repository.ComputeRepository;
import org.apache.airavata.k8s.api.resources.compute.ComputeResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ComputeResourceService {
    private ComputeRepository computeRepository;

    @Autowired
    public ComputeResourceService(ComputeRepository computeRepository) {
        this.computeRepository = computeRepository;
    }

    public long create(ComputeResource resource) {
        ComputeResourceModel model = new ComputeResourceModel();
        model.setName(resource.getName());
        model.setHost(resource.getHost());
        model.setUserName(resource.getUserName());
        model.setPassword(resource.getPassword());
        model.setCommunicationType(resource.getCommunicationType());
        ComputeResourceModel saved = computeRepository.save(model);
        return saved.getId();
    }

    public Optional<ComputeResource> findById(long id) {
        return ToResourceUtil.toResource(computeRepository.findById(id).get());
    }

    public List<ComputeResource> getAll() {
        List<ComputeResource> computeList = new ArrayList<>();
        Optional.ofNullable(computeRepository.findAll())
                .ifPresent(computes ->
                        computes.forEach(compute -> computeList.add(ToResourceUtil.toResource(compute).get())));
        return computeList;
    }
}
