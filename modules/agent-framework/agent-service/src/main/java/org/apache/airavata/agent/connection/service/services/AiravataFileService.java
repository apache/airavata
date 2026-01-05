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
package org.apache.airavata.agent.connection.service.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.airavata.agent.ServerMessage;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.models.DirectoryInfo;
import org.apache.airavata.agent.connection.service.models.ExperimentStorageResponse;
import org.apache.airavata.agent.connection.service.models.FileInfo;
import org.apache.airavata.common.model.ExperimentSearchFields;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.fuse.DirEntry;
import org.apache.airavata.fuse.ReadDirReq;
import org.apache.airavata.fuse.ReadDirRes;
import org.apache.airavata.service.AiravataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiravataFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataFileService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final AiravataService airavataService;

    private final Cache<String, ExperimentStorageResponse> storageCache =
            CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    public AiravataFileService(AiravataService airavataService) {
        this.airavataService = airavataService;
    }

    public void handleReadDirRequest(ReadDirReq request, StreamObserver<ServerMessage> responseObserver) {
        String fusePath = request.getName();

        ReadDirRes.Builder readDirResBuilder = ReadDirRes.newBuilder();

        try {
            if ("/".equals(fusePath)) {
                List<String> experimentIds = getUserExperimentIDs();

                // Handle root directory
                for (String expId : experimentIds) {
                    readDirResBuilder.addResult(
                            DirEntry.newBuilder().setName(expId).setIsDir(true).build());
                }

            } else {
                String experimentId = extractExperimentIdFromPath(fusePath);
                String path = extractPathFromRequest(fusePath);

                ExperimentStorageResponse storageResponse = getExperimentStorage(experimentId, path);

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
        String fullPath = experimentId + (path.equals("/") ? "" : "/" + path);
        return storageCache.get(fullPath, () -> fetchExperimentStorageFromAPI(experimentId, path));
    }

    private ExperimentStorageResponse fetchExperimentStorageFromAPI(String experimentId, String path) {
        String url = "https://" + UserContext.gatewayId() + ".cybershuttle.org/api/experiment-storage/" + experimentId
                + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(UserContext.authzToken().getAccessToken());
        headers.setAll(UserContext.authzToken().getClaimsMap());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ExperimentStorageResponse> responseEntity =
                restTemplate.exchange(url, HttpMethod.GET, entity, ExperimentStorageResponse.class);

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
                .setIno(generateInodeNumber(model.getDataProductURI()))
                .build();
    }

    private long generateInodeNumber(String value) {
        long hash = value.hashCode();
        return Math.abs(hash);
    }

    private String getProjectId(String projectName) {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects;
            try {
                userProjects = airavataService.getUserProjects(
                        UserContext.authzToken(), UserContext.gatewayId(), UserContext.username(), limit, offset);
            } catch (Exception e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            java.util.Optional<Project> defaultProject = userProjects.stream()
                    .filter(project -> projectName.equals(project.getName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectID();
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
        String projectId = getProjectId("Default Project");
        Map<ExperimentSearchFields, String> filters = Map.of(ExperimentSearchFields.PROJECT_ID, projectId);

        return Stream.iterate(0, offset -> offset + limit)
                .<List<ExperimentSummaryModel>>map(offset -> {
                    try {
                        return airavataService.searchExperiments(
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
