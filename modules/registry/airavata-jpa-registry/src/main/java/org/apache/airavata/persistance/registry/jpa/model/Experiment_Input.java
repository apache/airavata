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

package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.*;

@Entity
@Table(name ="EXPERIMENT_INPUT")
@IdClass(Experiment_Input_PK.class)
public class Experiment_Input {
    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experiment_id;

    @Id
    @Column(name = "EX_KEY")
    private String ex_key;

    @Column(name = "VALUE")
    private String value;

    @ManyToOne
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment_Metadata experiment_metadata;

    public String getExperiment_id() {
        return experiment_id;
    }

    public void setExperiment_id(String experiment_id) {
        this.experiment_id = experiment_id;
    }

    public Experiment_Metadata getExperiment_metadata() {
        return experiment_metadata;
    }

    public void setExperiment_metadata(Experiment_Metadata experiment_metadata) {
        this.experiment_metadata = experiment_metadata;
    }

    public String getEx_key() {
        return ex_key;
    }

    public void setEx_key(String ex_key) {
        this.ex_key = ex_key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
