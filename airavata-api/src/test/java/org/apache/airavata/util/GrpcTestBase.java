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
package org.apache.airavata.util;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Tag;

/**
 * Base class for gRPC integration tests using Armeria's ServerExtension.
 * <p>
 * Subclasses create a lightweight Armeria server with one or more gRPC services,
 * then obtain a ManagedChannel to issue requests against it.
 */
@Tag("integration")
public abstract class GrpcTestBase {

    /**
     * Creates an Armeria ServerExtension hosting the given gRPC services
     * with HTTP/JSON transcoding enabled.
     */
    protected static ServerExtension createServer(BindableService... services) {
        return new ServerExtension() {
            @Override
            protected void configure(ServerBuilder sb) {
                var builder = GrpcService.builder().enableHttpJsonTranscoding(true);
                for (var svc : services) {
                    builder.addService(svc);
                }
                sb.service(builder.build());
            }
        };
    }

    /**
     * Creates a plaintext gRPC channel pointing at the given server extension.
     */
    protected static ManagedChannel createChannel(ServerExtension server) {
        return ManagedChannelBuilder.forAddress("127.0.0.1", server.httpPort())
                .usePlaintext()
                .build();
    }
}
