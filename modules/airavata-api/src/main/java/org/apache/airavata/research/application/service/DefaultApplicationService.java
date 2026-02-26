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
import org.apache.airavata.core.service.AbstractCrudService;
import org.apache.airavata.research.application.entity.ApplicationEntity;
import org.apache.airavata.research.application.mapper.ApplicationMapper;
import org.apache.airavata.research.application.model.Application;
import org.apache.airavata.research.application.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link ApplicationService}.
 *
 * <p>Standard CRUD operations (create/get/update/delete/listByGateway) are provided by
 * {@link AbstractCrudService}.
 */
@Service
public class DefaultApplicationService extends AbstractCrudService<ApplicationEntity, Application>
        implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    public DefaultApplicationService(ApplicationRepository repository, ApplicationMapper mapper) {
        super(repository, mapper);
        this.applicationRepository = repository;
    }

    // -------------------------------------------------------------------------
    // AbstractCrudService hooks
    // -------------------------------------------------------------------------

    @Override
    protected String getId(Application model) {
        return model.getApplicationId();
    }

    @Override
    protected void setId(Application model, String id) {
        model.setApplicationId(id);
    }

    @Override
    protected List<ApplicationEntity> findByGateway(String gatewayId) {
        return applicationRepository.findByGatewayId(gatewayId);
    }

    @Override
    protected String entityName() {
        return "Application";
    }
}
