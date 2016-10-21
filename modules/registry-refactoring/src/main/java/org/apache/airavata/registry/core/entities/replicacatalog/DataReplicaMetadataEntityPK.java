package org.apache.airavata.registry.core.entities.replicacatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by abhij on 10/13/2016.
 */
public class DataReplicaMetadataEntityPK implements Serializable {
    private String replicaId;
    private String metadataKey;

    @Column(name = "REPLICA_ID")
    @Id
    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    @Column(name = "METADATA_KEY")
    @Id
    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataReplicaMetadataEntityPK that = (DataReplicaMetadataEntityPK) o;

        if (replicaId != null ? !replicaId.equals(that.replicaId) : that.replicaId != null) return false;
        if (metadataKey != null ? !metadataKey.equals(that.metadataKey) : that.metadataKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = replicaId != null ? replicaId.hashCode() : 0;
        result = 31 * result + (metadataKey != null ? metadataKey.hashCode() : 0);
        return result;
    }
}
