/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.application.service;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.research.application.entity.ApplicationInstallationEntity;
import org.apache.airavata.research.application.model.ApplicationInstallation;
import org.apache.airavata.research.application.repository.ApplicationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultApplicationInstallationService implements ApplicationInstallationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationInstallationService.class);

    private final ApplicationInstallationRepository installationRepository;

    public DefaultApplicationInstallationService(ApplicationInstallationRepository installationRepository) {
        this.installationRepository = installationRepository;
    }

    private ApplicationInstallation toModel(ApplicationInstallationEntity entity) {
        var model = new ApplicationInstallation();
        model.setInstallationId(entity.getInstallationId());
        model.setApplicationId(entity.getApplicationId());
        model.setResourceId(entity.getResourceId());
        model.setLoginUsername(entity.getLoginUsername());
        model.setInstallPath(entity.getInstallPath());
        model.setStatus(entity.getStatus());
        model.setInstalledAt(entity.getInstalledAt());
        model.setErrorMessage(entity.getErrorMessage());
        model.setCreatedAt(entity.getCreatedAt());
        return model;
    }

    private ApplicationInstallationEntity toEntity(ApplicationInstallation model) {
        var entity = new ApplicationInstallationEntity();
        entity.setInstallationId(model.getInstallationId());
        entity.setApplicationId(model.getApplicationId());
        entity.setResourceId(model.getResourceId());
        entity.setLoginUsername(model.getLoginUsername());
        entity.setInstallPath(model.getInstallPath());
        entity.setStatus(model.getStatus());
        entity.setInstalledAt(model.getInstalledAt());
        entity.setErrorMessage(model.getErrorMessage());
        entity.setCreatedAt(model.getCreatedAt());
        return entity;
    }

    @Override
    public ApplicationInstallation getInstallation(String installationId) {
        return installationRepository
                .findById(installationId)
                .map(this::toModel)
                .orElse(null);
    }

    @Override
    public List<ApplicationInstallation> getInstallationsByApplication(String applicationId) {
        return installationRepository.findByApplicationId(applicationId).stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public String createInstallation(ApplicationInstallation installation) {
        if (installation.getInstallationId() == null
                || installation.getInstallationId().isBlank()) {
            installation.setInstallationId(UUID.randomUUID().toString());
        }
        var entity = toEntity(installation);
        var saved = installationRepository.save(entity);
        logger.debug("Created application installation with id={}", saved.getInstallationId());
        return saved.getInstallationId();
    }

    @Override
    @Transactional
    public void updateInstallation(String installationId, ApplicationInstallation installation) {
        if (!installationRepository.existsById(installationId)) {
            throw new IllegalArgumentException("ApplicationInstallation not found: " + installationId);
        }
        installation.setInstallationId(installationId);
        var entity = toEntity(installation);
        installationRepository.save(entity);
        logger.debug("Updated application installation with id={}", installationId);
    }

    @Override
    @Transactional
    public void deleteInstallation(String installationId) {
        installationRepository.deleteById(installationId);
        logger.debug("Deleted application installation with id={}", installationId);
    }
}
