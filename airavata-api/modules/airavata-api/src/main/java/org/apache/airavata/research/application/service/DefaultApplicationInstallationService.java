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
import org.apache.airavata.research.application.mapper.ApplicationInstallationMapper;
import org.apache.airavata.research.application.model.ApplicationInstallation;
import org.apache.airavata.research.application.repository.ApplicationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultApplicationInstallationService implements ApplicationInstallationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationInstallationService.class);

    private final ApplicationInstallationRepository installationRepository;
    private final ApplicationInstallationMapper mapper;

    public DefaultApplicationInstallationService(
            ApplicationInstallationRepository installationRepository, ApplicationInstallationMapper mapper) {
        this.installationRepository = installationRepository;
        this.mapper = mapper;
    }

    @Override
    public ApplicationInstallation getInstallation(String installationId) {
        return installationRepository
                .findById(installationId)
                .map(mapper::toModel)
                .orElse(null);
    }

    @Override
    public List<ApplicationInstallation> getInstallationsByApplication(String applicationId) {
        return mapper.toModelList(installationRepository.findByApplicationId(applicationId));
    }

    @Override
    public String createInstallation(ApplicationInstallation installation) {
        if (installation.getInstallationId() == null
                || installation.getInstallationId().isBlank()) {
            installation.setInstallationId(UUID.randomUUID().toString());
        }
        var entity = mapper.toEntity(installation);
        var saved = installationRepository.save(entity);
        logger.debug("Created application installation with id={}", saved.getInstallationId());
        return saved.getInstallationId();
    }

    @Override
    public void updateInstallation(String installationId, ApplicationInstallation installation) {
        if (!installationRepository.existsById(installationId)) {
            throw new IllegalArgumentException("ApplicationInstallation not found: " + installationId);
        }
        installation.setInstallationId(installationId);
        var entity = mapper.toEntity(installation);
        installationRepository.save(entity);
        logger.debug("Updated application installation with id={}", installationId);
    }

    @Override
    public void deleteInstallation(String installationId) {
        installationRepository.deleteById(installationId);
        logger.debug("Deleted application installation with id={}", installationId);
    }
}
