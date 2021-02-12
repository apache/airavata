package org.apache.airavata.resource.profile.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.resource.profile.service.s3.*;
import org.apache.airavata.resource.profile.stubs.common.AuthzToken;
import org.apache.airavata.resource.profile.stubs.s3.S3Storage;

public class SampleClient {
    public static void main(String args[]) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 17002).usePlaintext().build();



        S3StorageServiceGrpc.S3StorageServiceBlockingStub s3StorageServiceBlockingStub = S3StorageServiceGrpc.newBlockingStub(channel);
        S3StorageCreateResponse s3Storage = s3StorageServiceBlockingStub.createS3Storage(S3StorageCreateRequest.newBuilder()
                .setAuthzToken(AuthzToken.newBuilder().setAuthorizationToken("token").build())
                .setS3Storage(S3Storage.newBuilder().setBucketName("bucket-1").setRegion("us-east").build()).build());

        System.out.println(s3Storage.getS3Storage());

        S3StorageFetchResponse s3StorageFetchResponse = s3StorageServiceBlockingStub.fetchS3Storage(S3StorageFetchRequest.newBuilder().setS3StorageId(s3Storage.getS3Storage().getS3StorageId()).setAuthzToken(AuthzToken.newBuilder().setAuthorizationToken("token").build()).build());
        System.out.println(s3StorageFetchResponse.getS3Storage());
    }
}
