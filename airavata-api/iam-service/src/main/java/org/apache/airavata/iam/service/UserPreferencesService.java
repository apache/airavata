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
package org.apache.airavata.iam.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.airavata.api.iam.userprofile.UserPreferences;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.iam.model.UserPreferencesEntity;
import org.apache.airavata.iam.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;

/** Per-user workspace preferences, stored as the JSON of the UserPreferences proto. */
@Service
public class UserPreferencesService {

    private final UserPreferencesRepository repository;

    public UserPreferencesService(UserPreferencesRepository repository) {
        this.repository = repository;
    }

    public UserPreferences getUserPreferences(RequestContext ctx) {
        return repository
                .findByGatewayIdAndUserId(ctx.getGatewayId(), ctx.getUserId())
                .map(UserPreferencesService::fromJson)
                .orElseGet(UserPreferences::getDefaultInstance);
    }

    public UserPreferences updateUserPreferences(RequestContext ctx, UserPreferences prefs) {
        UserPreferencesEntity entity = repository
                .findByGatewayIdAndUserId(ctx.getGatewayId(), ctx.getUserId())
                .orElseGet(UserPreferencesEntity::new);
        entity.setGatewayId(ctx.getGatewayId());
        entity.setUserId(ctx.getUserId());
        entity.setPreferencesJson(toJson(prefs));
        return fromJson(repository.save(entity));
    }

    private static String toJson(UserPreferences prefs) {
        try {
            return JsonFormat.printer().print(prefs);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static UserPreferences fromJson(UserPreferencesEntity entity) {
        UserPreferences.Builder builder = UserPreferences.newBuilder();
        try {
            if (entity.getPreferencesJson() != null) {
                JsonFormat.parser().ignoringUnknownFields().merge(entity.getPreferencesJson(), builder);
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        return builder.build();
    }
}
