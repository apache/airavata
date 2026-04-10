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

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.apache.airavata.api.parser.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.research.service.ParserService;
import org.springframework.stereotype.Component;

@Component
public class ParserGrpcService extends ParserServiceGrpc.ParserServiceImplBase {

    private final ParserService parserService;

    public ParserGrpcService(ParserService parserService) {
        this.parserService = parserService;
    }

    @Override
    public void saveParser(SaveParserRequest request, StreamObserver<SaveParserResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = parserService.saveParser(ctx, request.getParser());
            observer.onNext(SaveParserResponse.newBuilder().setParserId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getParser(GetParserRequest request, StreamObserver<Parser> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Parser result = parserService.getParser(ctx, request.getParserId(), request.getGatewayId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void listAllParsers(ListAllParsersRequest request, StreamObserver<ListAllParsersResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<Parser> parsers = parserService.listAllParsers(ctx, request.getGatewayId());
            observer.onNext(
                    ListAllParsersResponse.newBuilder().addAllParsers(parsers).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeParser(RemoveParserRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            parserService.removeParser(ctx, request.getParserId(), request.getGatewayId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void saveParsingTemplate(
            SaveParsingTemplateRequest request, StreamObserver<SaveParsingTemplateResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = parserService.saveParsingTemplate(ctx, request.getParsingTemplate());
            observer.onNext(
                    SaveParsingTemplateResponse.newBuilder().setTemplateId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getParsingTemplate(GetParsingTemplateRequest request, StreamObserver<ParsingTemplate> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ParsingTemplate result =
                    parserService.getParsingTemplate(ctx, request.getTemplateId(), request.getGatewayId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getParsingTemplatesForExperiment(
            GetParsingTemplatesForExperimentRequest request,
            StreamObserver<GetParsingTemplatesForExperimentResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ParsingTemplate> templates = parserService.getParsingTemplatesForExperiment(
                    ctx, request.getExperimentId(), request.getGatewayId());
            observer.onNext(GetParsingTemplatesForExperimentResponse.newBuilder()
                    .addAllParsingTemplates(templates)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void listAllParsingTemplates(
            ListAllParsingTemplatesRequest request, StreamObserver<ListAllParsingTemplatesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ParsingTemplate> templates = parserService.listAllParsingTemplates(ctx, request.getGatewayId());
            observer.onNext(ListAllParsingTemplatesResponse.newBuilder()
                    .addAllParsingTemplates(templates)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeParsingTemplate(RemoveParsingTemplateRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            parserService.removeParsingTemplate(ctx, request.getTemplateId(), request.getGatewayId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
