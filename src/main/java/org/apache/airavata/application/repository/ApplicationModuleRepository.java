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
package org.apache.airavata.application.repository;

import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.application.mapper.ApplicationMapper;
import org.apache.airavata.application.model.ApplicationModuleEntity;
import org.springframework.stereotype.Component;

@Component
public class ApplicationModuleRepository
        extends AbstractRepository<ApplicationModule, ApplicationModuleEntity, String> {

    public ApplicationModuleRepository() {
        super(ApplicationModule.class, ApplicationModuleEntity.class);
    }

    @Override
    protected ApplicationModule toModel(ApplicationModuleEntity entity) {
        return ApplicationMapper.INSTANCE.appModuleToModel(entity);
    }

    @Override
    protected ApplicationModuleEntity toEntity(ApplicationModule model) {
        return ApplicationMapper.INSTANCE.appModuleToEntity(model);
    }
}
