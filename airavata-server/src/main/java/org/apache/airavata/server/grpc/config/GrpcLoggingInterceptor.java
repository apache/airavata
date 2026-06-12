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
package org.apache.airavata.server.grpc.config;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs every inbound gRPC (and HTTP/JSON-transcoded) call and its outcome so failures are traceable.
 *
 * <p>On a non-OK close this logs the status code, description AND the server-side {@link Status#getCause()
 * cause} — which gRPC attaches (via {@code GrpcStatusMapper#toStatusException}) but never propagates to the
 * client and which is otherwise never logged. That cause is the actual root error (e.g. an SFTP
 * "permission denied" behind a generic "Failed to create directory"); without it failures surface only as
 * an opaque INTERNAL status on the caller.
 */
@Component
public class GrpcLoggingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcLoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        final String method = call.getMethodDescriptor().getFullMethodName();
        final long startNanos = System.nanoTime();
        log.info("gRPC --> {}", method);

        ServerCall<ReqT, RespT> loggingCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
                    @Override
                    public void close(Status status, Metadata trailers) {
                        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
                        if (status.isOk()) {
                            log.info("gRPC <-- {} {} ({} ms)", method, status.getCode(), elapsedMs);
                        } else {
                            Throwable cause = status.getCause();
                            if (cause != null) {
                                // trailing Throwable -> logged with its stack trace (root cause).
                                log.warn(
                                        "gRPC <-- {} {} ({} ms): {}",
                                        method,
                                        status.getCode(),
                                        elapsedMs,
                                        status.getDescription(),
                                        cause);
                            } else {
                                log.warn(
                                        "gRPC <-- {} {} ({} ms): {}",
                                        method,
                                        status.getCode(),
                                        elapsedMs,
                                        status.getDescription());
                            }
                        }
                        super.close(status, trailers);
                    }
                };

        return next.startCall(loggingCall, headers);
    }
}
