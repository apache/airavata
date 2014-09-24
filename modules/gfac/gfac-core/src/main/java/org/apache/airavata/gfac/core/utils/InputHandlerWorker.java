/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.gfac.core.utils;

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class InputHandlerWorker implements Callable {
    private static Logger log = LoggerFactory.getLogger(InputHandlerWorker.class);

    String experimentId;

    String taskId;

    String gatewayId;

    GFac gfac;
    public InputHandlerWorker(GFac gfac, String experimentId,String taskId,String gatewayId) {
        this.gfac = gfac;
        this.experimentId = experimentId;
        this.taskId = taskId;
        this.gatewayId = gatewayId;
    }

    @Override
    public Object call() throws Exception {
        return gfac.submitJob(experimentId,taskId,gatewayId);
    }
}
