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
package org.apache.airavata.research.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.research.*;
import org.apache.airavata.research.model.DatasetResourceEntity;
import org.apache.airavata.research.model.ModelResourceEntity;
import org.apache.airavata.research.model.NotebookResourceEntity;
import org.apache.airavata.research.model.PrivacyEnum;
import org.apache.airavata.research.model.RepositoryResourceEntity;
import org.apache.airavata.research.model.ResearchProjectEntity;
import org.apache.airavata.research.model.ResourceEntity;
import org.apache.airavata.research.model.ResourceTypeEnum;
import org.apache.airavata.research.service.ResearchProjectService;
import org.apache.airavata.research.service.ResearchResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ResearchResourceGrpcHandler extends ResearchResourceServiceGrpc.ResearchResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResearchResourceGrpcHandler.class);
    private final ResearchResourceService resourceService;
    private final ResearchProjectService researchProjectService;
    private final ObjectMapper objectMapper;

    public ResearchResourceGrpcHandler(
            ResearchResourceService resourceService,
            ResearchProjectService researchProjectService,
            ObjectMapper objectMapper) {
        this.resourceService = resourceService;
        this.researchProjectService = researchProjectService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createDataset(Struct request, StreamObserver<JsonResponse> responseObserver) {
        try {
            DatasetResourceEntity datasetResource = convertStruct(request, DatasetResourceEntity.class);
            ResourceEntity saved = resourceService.createResource(datasetResource, ResourceTypeEnum.DATASET);
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(saved)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating dataset resource", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void createNotebook(Struct request, StreamObserver<JsonResponse> responseObserver) {
        try {
            NotebookResourceEntity notebookResource = convertStruct(request, NotebookResourceEntity.class);
            ResourceEntity saved = resourceService.createResource(notebookResource, ResourceTypeEnum.NOTEBOOK);
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(saved)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating notebook resource", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void createRepository(
            CreateRepositoryResourceRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            CreateResourceRequest req = request.getResource();
            ResourceEntity saved = resourceService.createRepositoryResourceEntity(
                    req.getName(),
                    req.getDescription(),
                    req.getHeaderImage(),
                    new HashSet<>(req.getTagsList()),
                    new HashSet<>(req.getAuthorsList()),
                    PrivacyEnum.valueOf(req.getPrivacy()),
                    request.getGithubUrl());
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(saved)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating repository resource", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void modifyRepository(ModifyResourceRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            ResourceEntity modified = resourceService.modifyResource(
                    request.getId(),
                    request.getName(),
                    request.getDescription(),
                    request.getHeaderImage(),
                    new HashSet<>(request.getTagsList()),
                    new HashSet<>(request.getAuthorsList()),
                    PrivacyEnum.valueOf(request.getPrivacy()));
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(modified)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error modifying resource", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void createModel(Struct request, StreamObserver<JsonResponse> responseObserver) {
        try {
            ModelResourceEntity modelResource = convertStruct(request, ModelResourceEntity.class);
            ResourceEntity saved = resourceService.createResource(modelResource, ResourceTypeEnum.MODEL);
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(saved)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating model resource", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getTags(GetAllResourcesRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            List<org.apache.airavata.research.model.TagEntity> tags = resourceService.getAllTagsByAlphabeticalOrder();
            List<String> jsonList = tags.stream().map(this::writeJson).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting tags", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getResource(ResourceIdRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            ResourceEntity resource = resourceService.getResourceById(request.getId());
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(resource)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting resource {}", request.getId(), e);
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteResource(ResourceIdRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            boolean result = resourceService.deleteResourceById(request.getId());
            responseObserver.onNext(BoolResponse.newBuilder().setValue(result).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error deleting resource {}", request.getId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAllResources(GetAllResourcesRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            List<Class<? extends ResourceEntity>> typeList = new ArrayList<>();
            for (String type : request.getTypesList()) {
                ResourceTypeEnum rt = ResourceTypeEnum.valueOf(type);
                switch (rt) {
                    case REPOSITORY -> typeList.add(RepositoryResourceEntity.class);
                    case NOTEBOOK -> typeList.add(NotebookResourceEntity.class);
                    case MODEL -> typeList.add(ModelResourceEntity.class);
                    case DATASET -> typeList.add(DatasetResourceEntity.class);
                }
            }
            String[] tags = request.getTagsList().isEmpty()
                    ? null
                    : request.getTagsList().toArray(new String[0]);
            Page<ResourceEntity> page = resourceService.getAllResources(
                    request.getPageNumber(), request.getPageSize(), typeList, tags, request.getNameSearch());
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(page)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting all resources", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void searchResources(SearchResourceRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            ResourceTypeEnum type = ResourceTypeEnum.valueOf(request.getType());
            Class<? extends ResourceEntity> resourceClass = getResourceType(type);
            List<ResourceEntity> resources =
                    resourceService.getAllResourcesByTypeAndName(resourceClass, request.getName());
            List<String> jsonList = resources.stream().map(this::writeJson).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error searching resources", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getProjectsForResource(ResourceIdRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            ResourceEntity resource = resourceService.getResourceById(request.getId());
            List<ResearchProjectEntity> projects;
            if (resource.getClass() == RepositoryResourceEntity.class) {
                projects = researchProjectService.findProjectsWithRepository((RepositoryResourceEntity) resource);
            } else if (resource.getClass() == DatasetResourceEntity.class) {
                projects = researchProjectService.findProjectsContainingDataset((DatasetResourceEntity) resource);
            } else {
                throw new RuntimeException("Projects are only associated with repositories and datasets, and id: "
                        + request.getId() + " is not either.");
            }
            List<String> jsonList = projects.stream().map(this::writeJson).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting projects for resource {}", request.getId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void starResource(StarResourceRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            boolean result = resourceService.starOrUnstarResource(request.getId());
            responseObserver.onNext(BoolResponse.newBuilder().setValue(result).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error starring resource {}", request.getId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void checkUserStarredResource(StarResourceRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            boolean result = resourceService.checkWhetherUserStarredResource(request.getId());
            responseObserver.onNext(BoolResponse.newBuilder().setValue(result).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error checking star for resource {}", request.getId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getResourceStarCount(
            GetResourceStarCountRequest request, StreamObserver<StarCountResponse> responseObserver) {
        try {
            long count = resourceService.getResourceStarCount(request.getId());
            responseObserver.onNext(
                    StarCountResponse.newBuilder().setCount(count).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting star count for resource {}", request.getId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getStarredResources(
            GetStarredResourcesRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            List<ResourceEntity> resources = resourceService.getAllStarredResources(request.getUserId());
            List<String> jsonList = resources.stream().map(this::writeJson).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting starred resources for user {}", request.getUserId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private Class<? extends ResourceEntity> getResourceType(ResourceTypeEnum resourceTypeEnum) {
        return switch (resourceTypeEnum) {
            case REPOSITORY -> RepositoryResourceEntity.class;
            case NOTEBOOK -> NotebookResourceEntity.class;
            case MODEL -> ModelResourceEntity.class;
            case DATASET -> DatasetResourceEntity.class;
        };
    }

    private <T> T convertStruct(Struct struct, Class<T> clazz) {
        try {
            String json = com.google.protobuf.util.JsonFormat.printer().print(struct);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Struct to " + clazz.getSimpleName(), e);
        }
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
