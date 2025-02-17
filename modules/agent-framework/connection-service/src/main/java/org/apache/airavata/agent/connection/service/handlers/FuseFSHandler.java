package org.apache.airavata.agent.connection.service.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultJwtParser;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.models.ExperimentStorageResponse;
import org.apache.airavata.agent.connection.service.services.AiravataService;
import org.apache.airavata.fuse.*;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@GrpcService
public class FuseFSHandler extends FuseServiceGrpc.FuseServiceImplBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(FuseFSHandler.class);

    private final AiravataService airavataService;

    private final Cache<String, ExperimentStorageResponse> storageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final RestTemplate restTemplate = new RestTemplate();


    public FuseFSHandler(AiravataService airavataService) {
        this.airavataService = airavataService;
    }

    @Override
    public void statFs(StatFsReq request, StreamObserver<StatFsRes> responseObserver) {
        responseObserver.onNext(StatFsRes.newBuilder().setResult(StatFs.newBuilder()
                .setBlocks(242837545)
                .setBlocksAvailable(139701313)
                .setBlocksFree(139701313)
                .setInodes(5590118156l)
                .setIoSize(4096)
                .setBlockSize(4096)
                .setInodesFree(5588052520l)
                .build()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void fileInfo(FileInfoReq request, StreamObserver<FileInfoRes> responseObserver) {
        LOGGER.info("Calling fileInfo {}", request.getName());
        String fusePath = request.getName();
        try {
            if (fusePath.equals(baseDir)) {
                responseObserver.onNext(FileInfoRes.newBuilder()
                        .setResult(FileInfo.newBuilder()
                                .setName("data")
                                .setSize(128)
                                .setIno(18944809)
                                .setIsDir(true)
                                .setMode(0)
                                .setModTime(Timestamp.newBuilder().setSeconds(2147484141l).setNanos(875873564).build())
                                .build()).build());
            } else {

                String experimentId = extractExperimentIdFromPath(fusePath);
                String path = extractPathFromRequest(fusePath);
                ExperimentStorageResponse storageResponse = getExperimentStorage(experimentId, path);

                if (storageResponse != null) {
                    if(storageResponse.isDir()) {
                        responseObserver.onNext(FileInfoRes.newBuilder()
                                .setResult(FileInfo.newBuilder()
                                        .setName(new File(request.getName()).getName())
                                        .setSize(12)
                                        .setIno(18944812)
                                        .setIsDir(true)
                                        .setMode(0)
                                        .setModTime(Timestamp.newBuilder().setSeconds(1721479248).setNanos(876127687).build())
                                        .build()).build());
                    } else {
                        responseObserver.onNext(FileInfoRes.newBuilder()
                                .setResult(FileInfo.newBuilder()
                                        .setName(new File(request.getName()).getName())
                                        .setSize(12)
                                        .setIno(18944812)
                                        .setIsDir(false)
                                        .setMode(0)
                                        .setModTime(Timestamp.newBuilder().setSeconds(1721479248).setNanos(876127687).build())
                                        .build()).build());
                    }
                }

            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void openDir(OpenDirReq request, StreamObserver<OpenDirRes> responseObserver) {
        LOGGER.info("Calling openDir {}", request.getName());
        responseObserver.onNext(OpenDirRes.newBuilder().setResult(OpenedDir.newBuilder().build()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void openFile(OpenFileReq request, StreamObserver<OpenFileRes> responseObserver) {
        LOGGER.info("Calling openFile {}", request.getName());
        responseObserver.onNext(OpenFileRes.newBuilder().setResult(OpenedFile.newBuilder().build()).build());
        responseObserver.onCompleted();
    }

    public Claims decodeTokenClaims(String token) {
        String[] splitToken = token.split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        DefaultJwtParser parser = new DefaultJwtParser();
        Jwt<?, ?> jwt = parser.parse(unsignedToken);
        Claims claims = (Claims) jwt.getBody();
        return claims;
    }

    private AuthzToken getAuthzToken(RPCContext rpcContext) {
        String accessToken = rpcContext.getAccessToken(); // Remove "Bearer " prefix
        Claims claims = decodeTokenClaims(accessToken);

        // Extract the preferred_username claim
        String userName = claims.get("preferred_username", String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put("userName", userName);
        claimsMap.put("gatewayID", rpcContext.getGatewayId());

        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(accessToken);
        authzToken.setClaimsMap(claimsMap);

        return authzToken;
    }

    private final Map<String, List<String>> expIdCache = new ConcurrentHashMap<>();
    private final Map<String, Long> userLastFetchedExpIdCache = new ConcurrentHashMap<>();

    private List<String> getUserExperimentIds(RPCContext rpcContext) throws TException {
        AuthzToken authzToken = getAuthzToken(rpcContext);

        String key = authzToken.getClaimsMap().get("userName") + "." + authzToken.getClaimsMap().get("gatewayId");
        if (expIdCache.containsKey(key)) {
            if ((System.currentTimeMillis() - userLastFetchedExpIdCache.get(key)) < 20 * 1000) {
                return expIdCache.get(key);
            }
        }
        UserContext.setAuthzToken(authzToken);
        List<String> userExperimentIDs = airavataService.getUserExperimentIDs(airavataService.airavata());
        userLastFetchedExpIdCache.put(key, System.currentTimeMillis());
        expIdCache.put(key, userExperimentIDs);
        return userExperimentIDs;
    }

    private String baseDir = "/";
    @Override
    public void readDir(ReadDirReq request, StreamObserver<ReadDirRes> responseObserver) {
        LOGGER.info("Calling readDir {}", request.getName());

        String fusePath = request.getName();
        try {
            if (fusePath.equals(baseDir)) {
                List<String> userExperimentIds = getUserExperimentIds(request.getContext());
                ReadDirRes.Builder dirBuilder = ReadDirRes.newBuilder();
                for (String experimentId : userExperimentIds) {
                    dirBuilder.addResult(DirEntry.newBuilder()
                            .setIsDir(true)
                            .setName(experimentId)
                            .setFileMode(0)
                            .setInfo(FileInfo.newBuilder()
                                    .setModTime(Timestamp.newBuilder().setSeconds(1721479248l).build())
                                    .setName(experimentId)
                                    .setIno(18944812)
                                    .setSize(12)
                                    .setIsDir(true)
                                    .setMode(0).build()).build());
                }
                responseObserver.onNext(dirBuilder.build());
            } else {
                String experimentId = extractExperimentIdFromPath(fusePath);
                String path = extractPathFromRequest(fusePath);
                ExperimentStorageResponse storageResponse = getExperimentStorage(experimentId, path);

                if (storageResponse == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("File path not found: " + path).asRuntimeException());
                    return;
                }

                ReadDirRes.Builder dirBuilder = ReadDirRes.newBuilder();
                for (org.apache.airavata.agent.connection.service.models.FileInfo fileInfo : storageResponse.getFiles()) {
                    dirBuilder.addResult(DirEntry.newBuilder()
                            .setName(fileInfo.getName())
                            .setIsDir(false)
                            .setInfo(convertFileInfoModel(fileInfo))
                            .build());
                }
                responseObserver.onNext(dirBuilder.build());
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private org.apache.airavata.fuse.FileInfo convertFileInfoModel(org.apache.airavata.agent.connection.service.models.FileInfo model) {
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
        if (fusePath.equals(baseDir)) {
            return "";
        }
        return fusePath.split(baseDir)[1];
    }

    private String extractPathFromRequest(String fusePath) {
        if (fusePath.equals(baseDir)) {
            return baseDir;
        }
        String[] segments = fusePath.split("/", 3); // "/", expId, and path
        return (segments.length > 2) ? segments[2] : "/"; // If there's a path after expId, return it, otherwise "/"
    }


    @Override
    public void readFile(ReadFileReq request, StreamObserver<ReadFileRes> responseObserver) {
        LOGGER.info("Calling readFile {}", request.getName());
        responseObserver.onNext(ReadFileRes.newBuilder()
                .setResult(FileEntry.newBuilder()
                        .setDst(ByteString.copyFrom("Hellllo", Charset.defaultCharset())).build()).build());

        responseObserver.onCompleted();
    }

    @Override
    public void writeFile(WriteFileReq request, StreamObserver<WriteFileRes> responseObserver) {
        LOGGER.info("Calling writeFile {}", request.getName());
        responseObserver.onNext(WriteFileRes.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void setInodeAtt(SetInodeAttReq request, StreamObserver<SetInodeAttRes> responseObserver) {
        LOGGER.info("Calling setInodeAtt {}", request.getName());

        responseObserver.onNext(SetInodeAttRes.newBuilder().setResult(InodeAtt.newBuilder()
                .setAtime(Timestamp.newBuilder().setSeconds(System.currentTimeMillis()/ 1000).build())
                .setCtime(Timestamp.newBuilder().setSeconds(System.currentTimeMillis()/ 1000).build())
                .setMtime(Timestamp.newBuilder().setSeconds(System.currentTimeMillis()/ 1000).build())
                        .setFileMode(777)
                        .setSize(10800)
                .build()).build());

        responseObserver.onCompleted();
    }
}
