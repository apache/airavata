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

package org.apache.airavata.client.tools;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PeriodicExecutorThread extends Thread {

    private AiravataAPI airavataAPI;
    public static final int URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;
    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;

    private static final Logger log = LoggerFactory.getLogger(PeriodicExecutorThread.class);

    public PeriodicExecutorThread(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public void run() {
        while (true) {
            try {
                updateRegistry(airavataAPI);
                Thread.sleep(URL_UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    return;
                }
                log.error(e.getMessage());
                log.error("Workflow Interpreter Service URL update thread is interrupted");
            }
        }
    }

    protected abstract void updateRegistry(AiravataAPI airavataAPI) throws Exception;
}
