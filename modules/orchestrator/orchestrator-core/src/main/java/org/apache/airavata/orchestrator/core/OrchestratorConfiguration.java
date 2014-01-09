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
package org.apache.airavata.orchestrator.core;

public class OrchestratorConfiguration {

    private String submitterClass;

    private int submitterInterval = 1000;

    private int threadPoolSize = 10;

    private boolean startSubmitter = false;
    /* setter methods */
    public String getSubmitterClass() {
        return submitterClass;
    }

    public int getSubmitterInterval() {
        return submitterInterval;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /* setter methods */
    public void setSubmitterClass(String submitterClass) {
        this.submitterClass = submitterClass;
    }

    public void setSubmitterInterval(int submitterInterval) {
        this.submitterInterval = submitterInterval;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public boolean isStartSubmitter() {
        return startSubmitter;
    }

    public void setStartSubmitter(boolean startSubmitter) {
        this.startSubmitter = startSubmitter;
    }
}
