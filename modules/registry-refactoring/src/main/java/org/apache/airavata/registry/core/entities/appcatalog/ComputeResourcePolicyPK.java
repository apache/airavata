package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by skariyat on 2/8/18.
 */
public class ComputeResourcePolicyPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_POLICY_ID")
    private String resourcePolicyId;

    public ComputeResourcePolicyPK() {
    }

    public String getResourcePolicyId() {
        return resourcePolicyId;
    }

    public void setResourcePolicyId(String resourcePolicyId) {
        this.resourcePolicyId = resourcePolicyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComputeResourcePolicyPK that = (ComputeResourcePolicyPK) o;

        return resourcePolicyId != null ? resourcePolicyId.equals(that.resourcePolicyId) : that.resourcePolicyId == null;
    }

    @Override
    public int hashCode() {
        return resourcePolicyId != null ? resourcePolicyId.hashCode() : 0;
    }
}
