package org.apache.airavata.agent.connection.service.services;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.airavata.agent.ServerMessage;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.models.DirectoryInfo;
import org.apache.airavata.agent.connection.service.models.ExperimentStorageResponse;
import org.apache.airavata.agent.connection.service.models.FileInfo;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.fuse.DirEntry;
import org.apache.airavata.fuse.ReadDirReq;
import org.apache.airavata.fuse.ReadDirRes;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Timestamp;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

@Service
public class AiravataFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataFileService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final AiravataService airavataService;

    private final Cache<String, ExperimentStorageResponse> storageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public AiravataFileService(AiravataService airavataService) {
        this.airavataService = airavataService;
    }

    public void handleReadDirRequest(ReadDirReq request, StreamObserver<ServerMessage> responseObserver) {
        Airavata.Client airavataClient = airavataService.airavata();
        String fusePath = request.getName();

        ReadDirRes.Builder readDirResBuilder = ReadDirRes.newBuilder();

        try {
            if ("/".equals(fusePath)) {
                List<String> experimentIds = airavataService.getUserExperimentIDs(airavataClient);

                // Handle root directory
                for (String expId : experimentIds) {
                    readDirResBuilder.addResult(DirEntry.newBuilder()
                            .setName(expId)
                            .setIsDir(true)
                            .build());
                }

            } else {
                String experimentId = extractExperimentIdFromPath(fusePath);
                String path = extractPathFromRequest(fusePath);

                ExperimentStorageResponse storageResponse = getExperimentStorage(experimentId, path);

                if (storageResponse == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("File path not found: " + path).asRuntimeException());
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
        } catch (TException | ExecutionException e) {
            LOGGER.error("Failed to fetch experiments when trying to read the directory");
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to fetch experiments  when trying to read the directory").asRuntimeException());
        }

        // responseObserver.onNext(ServerMessage.newBuilder().setReadDirRes(readDirResBuilder.build()).build());
        responseObserver.onCompleted();
    }


    public ExperimentStorageResponse getExperimentStorage(String experimentId, String path) throws ExecutionException {
        String fullPath = experimentId + (path.equals("/") ? "" : "/" + path);
        return storageCache.get(fullPath, () -> fetchExperimentStorageFromAPI(experimentId, path));
    }


    private ExperimentStorageResponse fetchExperimentStorageFromAPI(String experimentId, String path) {
        String url = "https://" + UserContext.gatewayId() + ".cybershuttle.org/api/experiment-storage/" + experimentId + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(UserContext.authzToken().getAccessToken());
        headers.setAll(UserContext.authzToken().getClaimsMap());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ExperimentStorageResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ExperimentStorageResponse.class);

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

}
