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
package org.apache.airavata.registry.core.replica.catalog.impl;

import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.registry.core.replica.catalog.model.DataProduct;
import org.apache.airavata.registry.core.replica.catalog.model.DataReplicaLocation;
import org.apache.airavata.registry.core.replica.catalog.utils.ReplicaCatalogJPAUtils;
import org.apache.airavata.registry.core.replica.catalog.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.ReplicaCatalog;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ReplicaCatalogImpl implements ReplicaCatalog {

    private final static Logger logger = LoggerFactory.getLogger(ReplicaCatalogImpl.class);

    @Override
    public String registerDataProduct(DataProductModel productModel) throws ReplicaCatalogException {
        if(productModel.getOwnerName() == null || productModel.getGatewayId() == null){
            throw new ReplicaCatalogException("owner name and gateway id should be non empty");
        }

        if(productModel.getParentProductUri() != null && (!isDataProductExists(productModel.getParentProductUri())
                || !getDataProduct(productModel.getParentProductUri()).getDataProductType().equals(DataProductType.COLLECTION))){
            throw new ReplicaCatalogException("Parent Product does not exists or parent type is not Collection");
        }

        final long currentTime = System.currentTimeMillis();
        String productUri = ReplicaCatalog.schema + "://" + UUID.randomUUID().toString();
        productModel.setProductUri(productUri);
        productModel.setCreationTime(currentTime);
        productModel.setLastModifiedTime(currentTime);
        if(productModel.getReplicaLocations() != null){
            productModel.getReplicaLocations().stream().forEach(r-> {
                r.setProductUri(productUri);
                r.setReplicaId(UUID.randomUUID().toString());
                r.setCreationTime(currentTime);
                r.setLastModifiedTime(currentTime);
            });
        }
        productModel.setCreationTime(System.currentTimeMillis());
        productModel.setLastModifiedTime(System.currentTimeMillis());
        return createDataProduct(productModel);
    }

    private String createDataProduct(DataProductModel productModel) throws ReplicaCatalogException {
        DataProduct dataProduct = ThriftDataModelConversion.getDataProduct(productModel);
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            em.persist(dataProduct);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return dataProduct.getProductUri();
    }

    @Override
    public boolean removeDataProduct(String productUri) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productUri);
            if(dataProduct == null)
                return false;
            em.getTransaction().begin();
            em.remove(dataProduct);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public boolean updateDataProduct(DataProductModel productModel) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productModel.getProductUri());
            if(dataProduct == null)
                return false;
            em.getTransaction().begin();
            productModel.setCreationTime(dataProduct.getCreationTime().getTime());
            productModel.setLastModifiedTime(System.currentTimeMillis());
            em.merge(ThriftDataModelConversion.getUpdatedDataProduct(productModel, dataProduct));
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productUri);
            return ThriftDataModelConversion.getDataProductModel(dataProduct);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public boolean isDataProductExists(String productUri) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productUri);
            return dataProduct != null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
        String replicaId = UUID.randomUUID().toString();
        dataReplicaLocationModel.setReplicaId(replicaId);
        long currentTime = System.currentTimeMillis();
        dataReplicaLocationModel.setCreationTime(currentTime);
        dataReplicaLocationModel.setLastModifiedTime(currentTime);
        dataReplicaLocationModel.setCreationTime(System.currentTimeMillis());
        dataReplicaLocationModel.setLastModifiedTime(System.currentTimeMillis());
        DataReplicaLocation replicaLocation = ThriftDataModelConversion.getDataReplicaLocation(dataReplicaLocationModel);
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            em.persist(replicaLocation);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return replicaId;
    }

    @Override
    public boolean removeReplicaLocation(String replicaId) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataReplicaLocation replicaLocation = em.find(DataReplicaLocation.class, replicaId);
            if(replicaLocation == null)
                return false;
            em.getTransaction().begin();
            em.remove(replicaLocation);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataReplicaLocation dataReplicaLocation = em.find(DataReplicaLocation.class, dataReplicaLocationModel.getReplicaId());
            if(dataReplicaLocation == null)
                return false;
            em.getTransaction().begin();
            dataReplicaLocationModel.setCreationTime(dataReplicaLocation.getCreationTime().getTime());
            dataReplicaLocationModel.setLastModifiedTime(System.currentTimeMillis());
            em.merge(ThriftDataModelConversion.getUpdatedDataReplicaLocation(dataReplicaLocationModel, dataReplicaLocation));
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataReplicaLocation replicaLocation = em.find(DataReplicaLocation.class, replicaId);
            return ThriftDataModelConversion.getDataReplicaLocationModel(replicaLocation);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productUri);
            if(dataProduct == null)
                return null;
            ArrayList<DataReplicaLocationModel> dataReplicaLocationModels = new ArrayList<>();
            dataProduct.getDataReplicaLocations().stream().forEach(rl->dataReplicaLocationModels
                    .add(ThriftDataModelConversion.getDataReplicaLocationModel(rl)));
            return dataReplicaLocationModels;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productUri);
            return ThriftDataModelConversion.getDataProductModel(dataProduct.getParentDataProduct());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            em = ReplicaCatalogJPAUtils.getEntityManager();
            DataProduct dataProduct = em.find(DataProduct.class, productUri);
            Collection<DataProduct> childProducts = dataProduct.getChildDataProducts();
            ArrayList<DataProductModel> returnModels = new ArrayList<>();
            childProducts.stream().forEach(cp->{
                returnModels.add(ThriftDataModelConversion.getDataProductModel(cp));
            });
            return returnModels;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(String gatewayId, String userId, String productName, int limit, int offset) throws ReplicaCatalogException {
        EntityManager em = null;
        try {
            String query = "SELECT dp FROM DataProduct dp " +
                    "WHERE dp.gatewayId = '" + gatewayId + "' AND dp.ownerName='" + userId + "' AND dp.productName LIKE '%"
                    + productName + "%' ORDER BY dp.creationTime DESC";


            em = ReplicaCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;

            //pagination
            if (offset >= 0 && limit >= 0) {
                q = em.createQuery(query).setFirstResult(offset).setMaxResults(limit);
            } else {
                q = em.createQuery(query);
            }

            ArrayList<DataProductModel> returnModels = new ArrayList<>();
            List resultList = q.getResultList();
            for (Object o : resultList) {
                DataProduct dataProduct = (DataProduct) o;
                returnModels.add(ThriftDataModelConversion.getDataProductModel(dataProduct));
            }
            em.getTransaction().commit();
            em.close();
            return returnModels;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
