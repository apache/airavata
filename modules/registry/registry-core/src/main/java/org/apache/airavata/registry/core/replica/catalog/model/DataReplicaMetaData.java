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
package org.apache.airavata.registry.core.replica.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "DATA_REPLICA_METADATA")
@IdClass(DataReplicaMetaData_PK.class)
public class DataReplicaMetaData {
    private final static Logger logger = LoggerFactory.getLogger(DataReplicaMetaData.class);
    private String replicaId;
    private String key;
    private String value;

    private DataReplicaLocation dataReplicaLocation;

    @Id
    @Column(name = "REPLICA_ID")
    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    @Id
    @Column(name = "METADATA_KEY")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "METADATA_VALUE")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ManyToOne
    @JoinColumn(name = "REPLICA_ID", referencedColumnName = "REPLICA_ID")
    public DataReplicaLocation getDataReplicaLocation() {
        return dataReplicaLocation;
    }

    public void setDataReplicaLocation(DataReplicaLocation dataReplicaLocation) {
        this.dataReplicaLocation = dataReplicaLocation;
    }
}