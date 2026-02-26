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
package org.apache.airavata.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.airavata.agent.ServerMessage;
import org.apache.airavata.agent.UserContext;
import org.apache.airavata.agent.db.entity.ExperimentStorageCacheEntity;
import org.apache.airavata.agent.db.repository.ExperimentStorageCacheRepository;
import org.apache.airavata.agent.model.DirectoryInfo;
import org.apache.airavata.agent.model.ExperimentStorageResponse;
import org.apache.airavata.agent.model.FileInfo;
import org.apache.airavata.research.experiment.model.ExperimentSearchFields;
import org.apache.airavata.research.experiment.model.ExperimentSummaryModel;
import org.apache.airavata.research.project.model.Project;
import org.apache.airavata.research.experiment.service.ExperimentSearchService;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.fuse.DirEntry;
import org.apache.airavata.fuse.ReadDirReq;
import org.apache.airavata.fuse.ReadDirRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("agentFileService")
public class AiravataFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataFileService.class);
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExperimentService experimentService;
    private final ExperimentSearchService experimentSearchService;
    private final ExperimentStorageCacheRepository storageCacheRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiravataFileService(
            ExperimentService experimentService,
            ExperimentSearchService experimentSearchService,
            ExperimentStorageCacheRepository storageCacheRepository) {
        this.experimentService = experimentService;
        this.experimentSearchService = experimentSearchService;
        this.storageCacheRepository = storageCacheRepository;
    }

    public void handleReadDirRequest(ReadDirReq request, StreamObserver<ServerMessage> responseObserver) {
        var fusePath = request.getName();

        var readDirResBuilder = ReadDirRes.newBuilder();

        try {
            if ("/".equals(fusePath)) {
                var experimentIds = getUserExperimentIDs();

                // Handle root directory
                for (String expId : experimentIds) {
                    readDirResBuilder.addResult(
                            DirEntry.newBuilder().setName(expId).setIsDir(true).build());
                }

            } else {
                var experimentId = extractExperimentIdFromPath(fusePath);
                var path = extractPathFromRequest(fusePath);

                var storageResponse = getExperimentStorage(experimentId, path);

                if (storageResponse == null) {
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("File path not found: " + path)
                            .asRuntimeException());
                    return;
                }

                // List directories
                for (DirectoryInfo dirInfo : storageResponse.getDirectories()) {
                    readDirResBuilder.addResult(DirEntry.newBuilder()
                            .setName(dirInfo.getName())
                            .setIsDir(true)
                            .build());
                }

                // List files
                for (FileInfo fileInfo : storageResponse.getFiles()) {
                    readDirResBuilder.addResult(DirEntry.newBuilder()
                            .setName(fileInfo.getName())
                            .setIsDir(false)
                            .setInfo(convertFileInfoModel(fileInfo))
                            .build());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to fetch experiments when trying to read the directory");
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to fetch experiments  when trying to read the directory")
                    .asRuntimeException());
        }

        // responseObserver.onNext(ServerMessage.newBuilder().setReadDirRes(readDirResBuilder.build()).build());
        responseObserver.onCompleted();
    }

    public ExperimentStorageResponse getExperimentStorage(String experimentId, String path) throws ExecutionException {
        var fullPath = experimentId + (path.equals("/") ? "" : "/" + path);
        long minTimestamp = System.currentTimeMillis() - CACHE_TTL_MS;

        // Check DB cache for fresh entry
        var cached = storageCacheRepository.findFreshEntry(fullPath, minTimestamp);
        if (cached.isPresent()) {
            try {
                return objectMapper.readValue(cached.get().getResponseJson(), ExperimentStorageResponse.class);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Failed to deserialize cached storage response: {}", e.getMessage());
            }
        }

        // Fetch from API and save to DB
        var response = fetchExperimentStorageFromAPI(experimentId, path);
        if (response != null) {
            try {
                var entity = new ExperimentStorageCacheEntity();
                entity.setCacheKey(fullPath);
                entity.setExperimentId(experimentId);
                entity.setPath(path);
                entity.setResponseJson(objectMapper.writeValueAsString(response));
                entity.setCachedAt(System.currentTimeMillis());
                storageCacheRepository.save(entity);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Failed to serialize storage response for caching: {}", e.getMessage());
            }
        }
        return response;
    }

    private ExperimentStorageResponse fetchExperimentStorageFromAPI(String experimentId, String path) {
        var url = "https://" + UserContext.gatewayId() + ".cybershuttle.org/api/experiment-storage/" + experimentId
                + "/" + path;

        var headers = new HttpHeaders();
        headers.setBearerAuth(UserContext.authzToken().getAccessToken());
        headers.setAll(UserContext.authzToken().getClaimsMap());

        var entity = new HttpEntity<String>(headers);

        var responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ExperimentStorageResponse.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
            return null;
        } else {
            throw new RuntimeException("Failed to fetch experiment storage: " + responseEntity.getStatusCode());
        }
    }

    private String extractExperimentIdFromPath(String fusePath) {
        if (fusePath.equals("/")) {
            return "";
        }
        return fusePath.split("/")[1];
    }

    private String extractPathFromRequest(String fusePath) {
        if (fusePath.equals("/")) {
            return "/";
        }
        String[] segments = fusePath.split("/", 3); // "/", expId, and path
        return (segments.length > 2) ? segments[2] : "/"; // If there's a path after expId, return it, otherwise "/"
    }

    private org.apache.airavata.fuse.FileInfo convertFileInfoModel(FileInfo model) {
        return org.apache.airavata.fuse.FileInfo.newBuilder()
                .setName(model.getName())
                .setSize(model.getSize())
                .setModTime(Timestamp.newBuilder()
                        .setSeconds(model.getModifiedTime().getEpochSecond())
                        .setNanos(model.getModifiedTime().getNano())
                        .build())
                .setIsDir(false)
                .setIno(generateInodeNumber(model.getArtifactUri()))
                .build();
    }

    private long generateInodeNumber(String value) {
        var hash = (long) value.hashCode();
        return Math.abs(hash);
    }

    private String getProjectId(String projectName) {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects;
            try {
                userProjects = experimentService.getUserProjects(
                        UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
            } catch (Exception e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            var defaultProject = userProjects.stream()
                    .filter(project -> projectName.equals(project.getProjectName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectId();
            }
            if (userProjects.size() < limit) {
                break;
            }
            offset += limit;
        }

        throw new RuntimeException(
                "Could not find project: " + projectName + " for the user: " + UserContext.username());
    }

    private List<String> getUserExperimentIDs() {
        int limit = 100;
        var projectId = getProjectId("Default Project");
        var filters = Map.of(ExperimentSearchFields.PROJECT_ID, projectId);

        return Stream.iterate(0, offset -> offset + limit)
                .<List<ExperimentSummaryModel>>map(offset -> {
                    try {
                        return experimentSearchService.searchExperiments(
                                UserContext.authzToken(),
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset);
                    } catch (Exception e) {
                        String msg = String.format(
                                "Error searching experiments: gatewayId=%s, username=%s, filters=%s, limit=%d, offset=%d. Reason: %s",
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset,
                                e.getMessage());
                        LOGGER.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                })
                .takeWhile(list -> !list.isEmpty())
                .flatMap(List::stream)
                .map(ExperimentSummaryModel::getExperimentId)
                .collect(Collectors.toList());
    }
}
