package org.apache.airavata.registry.core.entities.replicacatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by abhij on 10/13/2016.
 */
public class DataProductMetadataEntityPK implements Serializable {
    private String productUri;
    private String metadataKey;

    @Column(name = "PRODUCT_URI")
    @Id
    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
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

        DataProductMetadataEntityPK that = (DataProductMetadataEntityPK) o;

        if (productUri != null ? !productUri.equals(that.productUri) : that.productUri != null) return false;
        if (metadataKey != null ? !metadataKey.equals(that.metadataKey) : that.metadataKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = productUri != null ? productUri.hashCode() : 0;
        result = 31 * result + (metadataKey != null ? metadataKey.hashCode() : 0);
        return result;
    }
}
