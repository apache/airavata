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
 package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;

@Entity
@Table(name = "EXPERIMENT_OUTPUT_VALUE")
@IdClass(ExperimentOutputValuePK.class)
public class ExperimentOutputValueEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Id
    @Column(name = "OUTPUT_NAME")
    private String name;

    @Id
    @Lob
    @Column(name = "OUTPUT_VALUE")
    private String value;

    @ManyToOne(targetEntity = ExperimentOutputEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns(
            {@JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID"),
            @JoinColumn(name = "OUTPUT_NAME", referencedColumnName = "OUTPUT_NAME")}
    )
    private ExperimentOutputEntity experimentOutput;

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ExperimentOutputEntity getExperimentOutput() {
        return experimentOutput;
    }

    public void setExperimentOutput(ExperimentOutputEntity experimentOutput) {
        this.experimentOutput = experimentOutput;
    }
}
