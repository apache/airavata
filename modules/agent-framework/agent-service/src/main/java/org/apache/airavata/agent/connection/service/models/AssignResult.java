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
package org.apache.airavata.agent.connection.service.models;

public sealed interface AssignResult permits AssignResult.Assigned, AssignResult.NoWork {

    record Assigned(String jobUnitId, String resolvedCommand) implements AssignResult {}

    record NoWork(String reason) implements AssignResult {
        public static final String EMPTY = "EMPTY";
        public static final String EMPTY_ALL_DONE = "EMPTY_ALL_DONE";
        public static final String NO_ASSIGNMENT = "NO_ASSIGNMENT";
    }

    static Assigned assigned(String id, String cmd) {
        return new Assigned(id, cmd);
    }

    static NoWork empty() {
        return new NoWork(NoWork.EMPTY);
    }

    static NoWork emptyAllDone() {
        return new NoWork(NoWork.EMPTY_ALL_DONE);
    }

    static NoWork noAssignment() {
        return new NoWork(NoWork.NO_ASSIGNMENT);
    }
}
