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
package org.apache.airavata.research.session.mapper;

import java.util.List;
import org.apache.airavata.research.session.entity.SessionEntity;
import org.apache.airavata.research.session.model.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public Session toModel(SessionEntity entity) {
        if (entity == null) return null;

        var model = new Session();
        model.setId(entity.getId());
        model.setSessionName(entity.getSessionName());
        model.setUserId(entity.getUserId());
        model.setProjectId(entity.getProject() != null ? entity.getProject().getId() : null);
        model.setStatus(entity.getStatus());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }

    public List<Session> toModelList(List<SessionEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toModel).toList();
    }
}
