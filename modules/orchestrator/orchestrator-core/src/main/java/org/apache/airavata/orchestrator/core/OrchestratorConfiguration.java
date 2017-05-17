/**
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
 */
package org.apache.airavata.orchestrator.core;

import java.net.URL;
import java.util.List;

/**
 * This keeps configuration of orchestrator, mostly this keep static
 * configuration, this can be accessed through orchestratorContext object
 */
public class OrchestratorConfiguration {

    private String newJobSubmitterClass;

    private String hangedJobSubmitterClass;

    private int submitterInterval = 1000;

    private int threadPoolSize = 10;

    private boolean startSubmitter = false;

    private URL brokerURL;

    private boolean embeddedMode;

    private List<String> validatorClasses;

    private boolean enableValidation;


    public List<String> getValidatorClasses() {
        return validatorClasses;
    }

    public void setValidatorClasses(List<String> validatorClasses) {
        this.validatorClasses = validatorClasses;
    }

    public boolean isEmbeddedMode() {
        return embeddedMode;
    }

    public void setEmbeddedMode(boolean embeddedMode) {
        this.embeddedMode = embeddedMode;
    }

    public URL getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(URL brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getNewJobSubmitterClass() {
        return newJobSubmitterClass;
    }

    public int getSubmitterInterval() {
        return submitterInterval;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setNewJobSubmitterClass(String newJobSubmitterClass) {
        this.newJobSubmitterClass = newJobSubmitterClass;
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

    public String getHangedJobSubmitterClass() {
        return hangedJobSubmitterClass;
    }

    public void setHangedJobSubmitterClass(String hangedJobSubmitterClass) {
        this.hangedJobSubmitterClass = hangedJobSubmitterClass;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }
}
