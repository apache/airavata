package org.apache.airavata.service.dataproduct;

import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataProductService {

    private static final Logger logger = LoggerFactory.getLogger(DataProductService.class);

    private final RegistryServerHandler registryHandler;

    public DataProductService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public String registerDataProduct(RequestContext ctx, DataProductModel dataProductModel) throws ServiceException {
        try {
            String result = registryHandler.registerDataProduct(dataProductModel);
            logger.debug("Registered data product {} for gateway {}", result, ctx.getGatewayId());
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error registering the data product " + dataProductModel.getProductName() + ": " + e.getMessage(), e);
        }
    }

    public DataProductModel getDataProduct(RequestContext ctx, String productUri) throws ServiceException {
        try {
            DataProductModel result = registryHandler.getDataProduct(productUri);
            logger.debug("Retrieved data product {}", productUri);
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving the data product " + productUri + ": " + e.getMessage(), e);
        }
    }

    public String registerReplicaLocation(RequestContext ctx, DataReplicaLocationModel replicaLocationModel) throws ServiceException {
        try {
            String result = registryHandler.registerReplicaLocation(replicaLocationModel);
            logger.debug("Registered replica location {} for gateway {}", result, ctx.getGatewayId());
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error registering replica " + replicaLocationModel.getReplicaName() + ": " + e.getMessage(), e);
        }
    }

    public DataProductModel getParentDataProduct(RequestContext ctx, String productUri) throws ServiceException {
        try {
            DataProductModel result = registryHandler.getParentDataProduct(productUri);
            logger.debug("Retrieved parent data product for {}", productUri);
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving parent data product for " + productUri + ": " + e.getMessage(), e);
        }
    }

    public List<DataProductModel> getChildDataProducts(RequestContext ctx, String productUri) throws ServiceException {
        try {
            List<DataProductModel> result = registryHandler.getChildDataProducts(productUri);
            logger.debug("Retrieved child data products for {}", productUri);
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving child data products for " + productUri + ": " + e.getMessage(), e);
        }
    }
}
