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

package org.apache.airavata.registry.api;

import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public abstract class AbstractRegistryUpdaterThread extends Thread {

    private AiravataRegistry2 airavataRegistry2;
    public static final int URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;
    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;

    private static final Logger log = LoggerFactory.getLogger(AbstractRegistryUpdaterThread.class);

    public AbstractRegistryUpdaterThread(AiravataRegistry2 registry) {
        airavataRegistry2 = registry;
    }

    public void run() {
        while (true) {
            try {
                updateRegistry(airavataRegistry2);
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

    protected abstract void updateRegistry(AiravataRegistry2 registry) throws Exception;
}
