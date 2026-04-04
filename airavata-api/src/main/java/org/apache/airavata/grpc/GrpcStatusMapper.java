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
package org.apache.airavata.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class GrpcStatusMapper {

    private GrpcStatusMapper() {}

    public static StatusRuntimeException toStatusException(Exception e) {
        String className = e.getClass().getSimpleName();
        String message = e.getMessage() != null ? e.getMessage() : className;

        Status status =
                switch (className) {
                    case "EntityNotFoundException" -> Status.NOT_FOUND;
                    case "AuthorizationException" -> Status.PERMISSION_DENIED;
                    case "AuthenticationException" -> Status.UNAUTHENTICATED;
                    case "ValidationException" -> Status.INVALID_ARGUMENT;
                    case "DuplicateEntityException" -> Status.ALREADY_EXISTS;
                    default -> Status.INTERNAL;
                };

        return status.withDescription(message).withCause(e).asRuntimeException();
    }
}
