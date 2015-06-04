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

package org.apache.airavata.registry.core.experiment.catalog.model;

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;

@DataCache
@Entity
@Table(name = "CONFIG_DATA")
public class ExperimentConfigData implements Serializable {
    @Id
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "AIRAVATA_AUTO_SCHEDULE")
    private boolean airavataAutoSchedule;
    @Column(name = "OVERRIDE_MANUAL_SCHEDULE_PARAMS")
    private boolean overrideManualParams;
    @Column(name = "SHARE_EXPERIMENT")
    private boolean shareExp;
    @Column(name = "USER_DN")
    private String userDn;
    @Column(name = "GENERATE_CERT")
    private boolean generateCert;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private Computational_Resource_Scheduling resourceScheduling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private AdvancedInputDataHandling inputDataHandling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private AdvancedOutputDataHandling outputDataHandling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private QosParam qosParam;

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public boolean isAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean isOverrideManualParams() {
        return overrideManualParams;
    }

    public void setOverrideManualParams(boolean overrideManualParams) {
        this.overrideManualParams = overrideManualParams;
    }

    public boolean isShareExp() {
        return shareExp;
    }

    public void setShareExp(boolean shareExp) {
        this.shareExp = shareExp;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public AdvancedInputDataHandling getInputDataHandling() {
        return inputDataHandling;
    }

    public void setInputDataHandling(AdvancedInputDataHandling inputDataHandling) {
        this.inputDataHandling = inputDataHandling;
    }

    public AdvancedOutputDataHandling getOutputDataHandling() {
        return outputDataHandling;
    }

    public void setOutputDataHandling(AdvancedOutputDataHandling outputDataHandling) {
        this.outputDataHandling = outputDataHandling;
    }

    public QosParam getQosParam() {
        return qosParam;
    }

    public void setQosParam(QosParam qosParam) {
        this.qosParam = qosParam;
    }

    public Computational_Resource_Scheduling getResourceScheduling() {
        return resourceScheduling;
    }

    public void setResourceScheduling(Computational_Resource_Scheduling resourceScheduling) {
        this.resourceScheduling = resourceScheduling;
    }
}
