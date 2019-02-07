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

import java.io.Serializable;

/**
 * The primary key class for the data_replica_metadata database table.
 */
public class DataReplicaMetadataPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String replicaId;
    private String metadataKey;

    public DataReplicaMetadataPK() {
    }

    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DataReplicaMetadataPK)) {
            return false;
        }
        DataReplicaMetadataPK castOther = (DataReplicaMetadataPK) other;
        return
                this.replicaId.equals(castOther.replicaId)
                        && this.metadataKey.equals(castOther.metadataKey);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.replicaId.hashCode();
        hash = hash * prime + this.metadataKey.hashCode();

        return hash;
    }
}
