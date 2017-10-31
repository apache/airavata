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

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.application.ApplicationDeployment;
import org.apache.airavata.k8s.api.server.repository.ApplicationDeploymentRepository;
import org.apache.airavata.k8s.api.server.repository.ApplicationModuleRepository;
import org.apache.airavata.k8s.api.server.repository.ComputeRepository;
import org.apache.airavata.k8s.api.resources.application.ApplicationDeploymentResource;
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
public class ApplicationDeploymentService {

    private ApplicationDeploymentRepository applicationDeploymentRepository;
    private ComputeRepository computeRepository;
    private ApplicationModuleRepository applicationModuleRepository;

    @Autowired
    public ApplicationDeploymentService(ApplicationDeploymentRepository applicationDeploymentRepository,
                                        ComputeRepository computeRepository,
                                        ApplicationModuleRepository applicationModuleRepository) {
        this.applicationDeploymentRepository = applicationDeploymentRepository;
        this.computeRepository = computeRepository;
        this.applicationModuleRepository = applicationModuleRepository;
    }

    public long create(ApplicationDeploymentResource resource) {
        ApplicationDeployment deployment = new ApplicationDeployment();
        deployment.setPreJobCommand(resource.getPreJobCommand());
        deployment.setPostJobCommand(resource.getPostJobCommand());
        deployment.setExecutablePath(resource.getExecutablePath());
        deployment.setName(resource.getName());
        deployment.setComputeResource(computeRepository
                .findById(resource.getComputeResourceId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find a compute resource with id " +
                        resource.getComputeResourceId())));
        deployment.setApplicationModule(applicationModuleRepository
                .findById(resource.getApplicationModuleId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find an app module with id "
                        + resource.getApplicationModuleId())));
        ApplicationDeployment saved = applicationDeploymentRepository.save(deployment);
        return saved.getId();
    }

    public Optional<ApplicationDeploymentResource> findById(long id) {
        return ToResourceUtil.toResource(applicationDeploymentRepository.findById(id).get());
    }

    public List<ApplicationDeploymentResource> getAll() {
        List<ApplicationDeploymentResource> deploymentList = new ArrayList<>();
        Optional.ofNullable(applicationDeploymentRepository.findAll())
                .ifPresent(deployments ->
                        deployments.forEach(dep -> deploymentList.add(ToResourceUtil.toResource(dep).get())));
        return deploymentList;
    }
}
