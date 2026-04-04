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
package org.apache.airavata.credential.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.api.credential.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.credential.service.CredentialService;
import org.apache.airavata.credential.service.SSHAccountService;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.credential.store.proto.CredentialSummary;
import org.apache.airavata.model.credential.store.proto.SummaryType;
import org.springframework.stereotype.Component;

@Component
public class CredentialGrpcService extends CredentialServiceGrpc.CredentialServiceImplBase {

    private final CredentialService credentialService;
    private final SSHAccountService sshAccountService;

    public CredentialGrpcService(CredentialService credentialService, SSHAccountService sshAccountService) {
        this.credentialService = credentialService;
        this.sshAccountService = sshAccountService;
    }

    @Override
    public void generateAndRegisterSSHKeys(
            GenerateAndRegisterSSHKeysRequest request, StreamObserver<GenerateAndRegisterSSHKeysResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String token = credentialService.generateAndRegisterSSHKeys(ctx, request.getDescription());
            observer.onNext(GenerateAndRegisterSSHKeysResponse.newBuilder()
                    .setToken(token)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void registerPwdCredential(
            RegisterPwdCredentialRequest request, StreamObserver<RegisterPwdCredentialResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            var pwdCred = request.getPasswordCredential();
            String token = credentialService.registerPwdCredential(
                    ctx, pwdCred.getLoginUserName(), pwdCred.getPassword(), pwdCred.getDescription());
            observer.onNext(
                    RegisterPwdCredentialResponse.newBuilder().setToken(token).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getCredentialSummary(GetCredentialSummaryRequest request, StreamObserver<CredentialSummary> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            var result = credentialService.getCredentialSummary(ctx, request.getTokenId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllCredentialSummaries(
            GetAllCredentialSummariesRequest request, StreamObserver<GetAllCredentialSummariesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            var summaryType = SummaryType.valueOf(request.getType().name());
            var summaries = credentialService.getAllCredentialSummaries(ctx, summaryType);
            observer.onNext(GetAllCredentialSummariesResponse.newBuilder()
                    .addAllCredentialSummaries(summaries)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteSSHPubKey(DeleteSSHPubKeyRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            credentialService.deleteSSHPubKey(ctx, request.getTokenId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deletePWDCredential(DeletePWDCredentialRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            credentialService.deletePWDCredential(ctx, request.getTokenId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void doesUserHaveSSHAccount(
            DoesUserHaveSSHAccountRequest request, StreamObserver<DoesUserHaveSSHAccountResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean hasAccount = sshAccountService.doesUserHaveSSHAccount(
                    ctx, request.getComputeResourceId(), request.getUsername());
            observer.onNext(DoesUserHaveSSHAccountResponse.newBuilder()
                    .setHasAccount(hasAccount)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isSSHSetupComplete(
            IsSSHSetupCompleteRequest request, StreamObserver<IsSSHSetupCompleteResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean isComplete = sshAccountService.isSSHSetupCompleteForUserComputeResourcePreference(
                    ctx, request.getComputeResourceId(), "");
            observer.onNext(IsSSHSetupCompleteResponse.newBuilder()
                    .setIsComplete(isComplete)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void setupSSHAccount(SetupSSHAccountRequest request, StreamObserver<SetupSSHAccountResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sshAccountService.setupUserComputeResourcePreferencesForSSH(
                    ctx, request.getComputeResourceId(), request.getUsername(), "");
            observer.onNext(
                    SetupSSHAccountResponse.newBuilder().setSuccess(true).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
