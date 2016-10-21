package org.apache.airavata.registry.core.entities.replicacatalog;

import javax.persistence.*;

/**
 * Created by abhij on 10/13/2016.
 */
@Entity
@Table(name = "data_product_metadata", schema = "airavata_catalog", catalog = "")
@IdClass(DataProductMetadataEntityPK.class)
public class DataProductMetadataEntity {
    private String productUri;
    private String metadataKey;
    private String metadataValue;

    @Id
    @Column(name = "PRODUCT_URI")
    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
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

        DataProductMetadataEntity that = (DataProductMetadataEntity) o;

        if (productUri != null ? !productUri.equals(that.productUri) : that.productUri != null) return false;
        if (metadataKey != null ? !metadataKey.equals(that.metadataKey) : that.metadataKey != null) return false;
        if (metadataValue != null ? !metadataValue.equals(that.metadataValue) : that.metadataValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = productUri != null ? productUri.hashCode() : 0;
        result = 31 * result + (metadataKey != null ? metadataKey.hashCode() : 0);
        result = 31 * result + (metadataValue != null ? metadataValue.hashCode() : 0);
        return result;
    }
}
