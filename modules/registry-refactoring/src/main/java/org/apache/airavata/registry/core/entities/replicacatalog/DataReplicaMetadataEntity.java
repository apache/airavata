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
package org.apache.airavata.registry.core.entities.replicacatalog;

import javax.persistence.*;

@Entity
@Table(name = "data_replica_metadata", schema = "airavata_catalog", catalog = "")
@IdClass(DataReplicaMetadataEntityPK.class)
public class DataReplicaMetadataEntity {
    private String replicaId;
    private String metadataKey;
    private String metadataValue;

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
    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    @Basic
    @Column(name = "METADATA_VALUE")
    public String getMetadataValue() {
        return metadataValue;
    }

    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataReplicaMetadataEntity that = (DataReplicaMetadataEntity) o;

        if (replicaId != null ? !replicaId.equals(that.replicaId) : that.replicaId != null) return false;
        if (metadataKey != null ? !metadataKey.equals(that.metadataKey) : that.metadataKey != null) return false;
        if (metadataValue != null ? !metadataValue.equals(that.metadataValue) : that.metadataValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = replicaId != null ? replicaId.hashCode() : 0;
        result = 31 * result + (metadataKey != null ? metadataKey.hashCode() : 0);
        result = 31 * result + (metadataValue != null ? metadataValue.hashCode() : 0);
        return result;
    }
}
