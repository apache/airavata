package org.apache.airavata.registry.cpi;

import org.apache.airavata.model.data.replica.DataProductModel;

import java.util.List;

public interface DataProductInterface {

    String schema = "airavata-dp";

    String registerDataProduct(DataProductModel product) throws ReplicaCatalogException;

    boolean updateDataProduct(DataProductModel product) throws ReplicaCatalogException;

    DataProductModel getDataProduct(String productUri) throws ReplicaCatalogException;

    DataProductModel getParentDataProduct(String productUri) throws ReplicaCatalogException;

    List<DataProductModel> getChildDataProducts(String productUri) throws ReplicaCatalogException;

    List<DataProductModel> searchDataProductsByName(String gatewayId, String userId, String productName,
                                                    int limit, int offset) throws ReplicaCatalogException;

    boolean isDataProductExists(String productUri) throws ReplicaCatalogException;

    boolean removeDataProduct(String productUri) throws ReplicaCatalogException;

}
