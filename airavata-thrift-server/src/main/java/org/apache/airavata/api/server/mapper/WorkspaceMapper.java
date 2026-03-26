/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.api.server.mapper;

import java.util.List;
import org.apache.airavata.model.workspace.proto.Project;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper between Thrift-generated and Protobuf-generated workspace types.
 *
 * <p>Thrift classes live in {@code org.apache.airavata.model.workspace}; Proto classes live in
 * {@code org.apache.airavata.model.workspace.proto}. The packages are distinct — proto files use
 * {@code java_package} with a {@code .proto} suffix to avoid duplicate class errors with the
 * identically-named Thrift-generated classes.
 *
 * <p>Field name differences:
 * <ul>
 *   <li>Thrift {@code projectID} ↔ Proto {@code projectId}
 *   <li>Thrift {@code sharedUsers} / {@code sharedGroups} → handled via {@code @AfterMapping}
 *       because Protobuf builder repeated fields use {@code addAllXxx()} rather than setters
 * </ul>
 *
 * <p>Protobuf messages are immutable; MapStruct populates the {@link Project.Builder} provided by
 * {@link #createProjectBuilder()}, then the generated implementation calls {@code .build()}.
 */
@Mapper
public interface WorkspaceMapper {

    WorkspaceMapper INSTANCE = Mappers.getMapper(WorkspaceMapper.class);

    /**
     * Supplies the Protobuf builder instance. MapStruct uses this via {@code @ObjectFactory}
     * instead of trying to invoke a (non-existent) public no-arg constructor on
     * {@link Project.Builder}.
     */
    @ObjectFactory
    default Project.Builder createProjectBuilder() {
        return Project.newBuilder();
    }

    /**
     * Maps a Thrift {@link org.apache.airavata.model.workspace.Project} to a Protobuf
     * {@link Project.Builder}. Repeated fields are populated by
     * {@link #fillRepeatedFields(org.apache.airavata.model.workspace.Project, Project.Builder)}.
     * Call {@code .build()} on the result to obtain the immutable {@link Project}.
     */
    @Mapping(source = "projectID", target = "projectId")
    @Mapping(target = "sharedUsersList", ignore = true)
    @Mapping(target = "sharedGroupsList", ignore = true)
    Project.Builder thriftToProtoBuilder(org.apache.airavata.model.workspace.Project thrift);

    /**
     * Populates repeated Protobuf fields after the main scalar mapping, using the builder's
     * {@code addAllXxx()} API which avoids attempts to instantiate the abstract
     * {@code ProtocolStringList}.
     */
    @AfterMapping
    default void fillRepeatedFields(
            org.apache.airavata.model.workspace.Project thrift,
            @MappingTarget Project.Builder builder) {
        List<String> users = thrift.getSharedUsers();
        if (users != null) {
            builder.addAllSharedUsers(users);
        }
        List<String> groups = thrift.getSharedGroups();
        if (groups != null) {
            builder.addAllSharedGroups(groups);
        }
    }

    /**
     * Maps a Protobuf {@link Project} to its Thrift counterpart.
     *
     * <p>Protobuf repeated fields are accessed via {@code getSharedUsersList()} / {@code
     * getSharedGroupsList()}, which MapStruct resolves from the property names.
     */
    @Mapping(source = "projectId", target = "projectID")
    @Mapping(source = "sharedUsersList", target = "sharedUsers")
    @Mapping(source = "sharedGroupsList", target = "sharedGroups")
    org.apache.airavata.model.workspace.Project protoToThrift(Project proto);
}
