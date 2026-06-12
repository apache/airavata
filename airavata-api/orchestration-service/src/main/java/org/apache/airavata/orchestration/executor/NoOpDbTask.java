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
package org.apache.airavata.orchestration.executor;

import org.apache.airavata.task.DbTask;
import org.apache.airavata.task.DbTaskResult;
import org.apache.airavata.task.TaskContext;

/**
 * No-op task used to exercise the executor's claim/advance machinery without performing real
 * work (Phase B). Real task implementations are wired in later phases; this one always succeeds.
 */
public class NoOpDbTask implements DbTask {

    @Override
    public DbTaskResult run(TaskContext ctx) {
        return DbTaskResult.completed("no-op");
    }
}
