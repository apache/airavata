package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.DataMovementProtocol;
import org.apache.aiaravata.application.catalog.data.model.DataMovementProtocolPK;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataMovementProtocolResource extends AbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(DataMovementProtocolResource.class);

    private String resourceID;
    private String dataMoveID;
    private String dataMoveType;
    private ComputeResourceResource computeHostResource;

    public void remove(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_PROTOCOL);
            generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_TYPE, ids.get(DataMoveProtocolConstants.DATA_MOVE_TYPE));
            generator.setParameter(DataMoveProtocolConstants.RESOURCE_ID, ids.get(DataMoveProtocolConstants.RESOURCE_ID));
            generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_ID, ids.get(DataMoveProtocolConstants.DATA_MOVE_ID));
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public Resource get(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_PROTOCOL);
            generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_TYPE, ids.get(DataMoveProtocolConstants.DATA_MOVE_TYPE));
            generator.setParameter(DataMoveProtocolConstants.RESOURCE_ID, ids.get(DataMoveProtocolConstants.RESOURCE_ID));
            generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_ID, ids.get(DataMoveProtocolConstants.DATA_MOVE_ID));
            Query q = generator.selectQuery(em);
            DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) q.getSingleResult();
            DataMovementProtocolResource dataMovementProtocolResource =
                    (DataMovementProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_PROTOCOL, dataMovementProtocol);
            em.getTransaction().commit();
            em.close();
            return dataMovementProtocolResource;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
        List<Resource> dataMoveProtocolResourcesList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_PROTOCOL);
            List results;
            if (fieldName.equals(DataMoveProtocolConstants.RESOURCE_ID)) {
                generator.setParameter(DataMoveProtocolConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) result;
                        DataMovementProtocolResource dataMovementProtocolResource =
                                (DataMovementProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_PROTOCOL, dataMovementProtocol);
                        dataMoveProtocolResourcesList.add(dataMovementProtocolResource);
                    }
                }
            } else if (fieldName.equals(DataMoveProtocolConstants.DATA_MOVE_TYPE)) {
                generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) result;
                        DataMovementProtocolResource dataMovementProtocolResource =
                                (DataMovementProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_PROTOCOL, dataMovementProtocol);
                        dataMoveProtocolResourcesList.add(dataMovementProtocolResource);
                    }
                }
            } else if (fieldName.equals(DataMoveProtocolConstants.DATA_MOVE_ID)) {
                generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) result;
                        DataMovementProtocolResource dataMovementProtocolResource =
                                (DataMovementProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_PROTOCOL, dataMovementProtocol);
                        dataMoveProtocolResourcesList.add(dataMovementProtocolResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Data Movement Protocol Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Data Movement Protocol Resource.");
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return dataMoveProtocolResourcesList;
    }

    @Override
    public List<Resource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> dataMovementProtocolIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_PROTOCOL);
            List results;
            if (fieldName.equals(DataMoveProtocolConstants.DATA_MOVE_ID)) {
                generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) result;
                        dataMovementProtocolIDs.add(dataMovementProtocol.getDataMoveID());
                    }
                }
            } else if (fieldName.equals(DataMoveProtocolConstants.RESOURCE_ID)) {
                generator.setParameter(DataMoveProtocolConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) result;
                        dataMovementProtocolIDs.add(dataMovementProtocol.getDataMoveID());
                    }
                }
            } else if (fieldName.equals(DataMoveProtocolConstants.DATA_MOVE_TYPE)) {
                generator.setParameter(DataMoveProtocolConstants.DATA_MOVE_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        DataMovementProtocol dataMovementProtocol = (DataMovementProtocol) result;
                        dataMovementProtocolIDs.add(dataMovementProtocol.getDataMoveID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Data Move Protocol resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Data Move Protocol Resource.");
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return dataMovementProtocolIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            DataMovementProtocol existingDataMovementProtocol = em.find(DataMovementProtocol.class, new DataMovementProtocolPK(resourceID, dataMoveID, dataMoveType));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingDataMovementProtocol != null) {
                existingDataMovementProtocol.setDataMoveID(dataMoveType);
                existingDataMovementProtocol.setDataMoveID(dataMoveID);
                ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
                existingDataMovementProtocol.setComputeResource(computeResource);
                existingDataMovementProtocol.setResourceID(resourceID);
                em.merge(existingDataMovementProtocol);
            } else {
                DataMovementProtocol dataMovementProtocol = new DataMovementProtocol();
                dataMovementProtocol.setDataMoveType(dataMoveType);
                dataMovementProtocol.setDataMoveID(dataMoveID);
                dataMovementProtocol.setResourceID(resourceID);
                ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
                dataMovementProtocol.setComputeResource(computeResource);
                em.persist(dataMovementProtocol);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public boolean isExists(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            DataMovementProtocol dataMovementProtocol = em.find(DataMovementProtocol.class, new DataMovementProtocolPK(
                    ids.get(DataMoveProtocolConstants.RESOURCE_ID),
                    ids.get(DataMoveProtocolConstants.DATA_MOVE_ID), ids.get(DataMoveProtocolConstants.DATA_MOVE_TYPE)));

            em.close();
            return dataMovementProtocol != null;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getDataMoveID() {
        return dataMoveID;
    }

    public void setDataMoveID(String dataMoveID) {
        this.dataMoveID = dataMoveID;
    }

    public String getDataMoveType() {
        return dataMoveType;
    }

    public void setDataMoveType(String dataMoveType) {
        this.dataMoveType = dataMoveType;
    }

    public ComputeResourceResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeResourceResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }
}
