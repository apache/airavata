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
package org.apache.airavata.gfac.impl;

import org.apache.airavata.gfac.core.GFac;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputHandlerWorker implements Runnable {
    private static Logger log = LoggerFactory.getLogger(InputHandlerWorker.class);

    String experimentId;
    String taskId;
    String gatewayId;
    String tokenId;

    GFac gfac;
    public InputHandlerWorker(GFac gfac, String experimentId,String taskId,String gatewayId, String tokenId) {
        this.gfac = gfac;
        this.experimentId = experimentId;
        this.taskId = taskId;
        this.gatewayId = gatewayId;
        this.tokenId = tokenId;
    }

    @Override
    public void run() {
        try {
            gfac.submitJob(experimentId, taskId, gatewayId, tokenId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
