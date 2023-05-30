package org.apache.airavata.apis.db.entity.backend;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "backend_type")
public abstract class ComputeBackendEntity {

    @Id
    @Column(name = "BACKEND_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String backendId;

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((backendId == null) ? 0 : backendId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComputeBackendEntity other = (ComputeBackendEntity) obj;
        if (backendId == null) {
            if (other.backendId != null)
                return false;
        } else if (!backendId.equals(other.backendId))
            return false;
        return true;
    }

}
