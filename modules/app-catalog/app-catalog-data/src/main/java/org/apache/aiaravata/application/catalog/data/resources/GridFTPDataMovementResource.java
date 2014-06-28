package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.GridFTPDataMovement;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class GridFTPDataMovementResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GridFTPDataMovementResource.class);

    private String dataMoveID;
    private String resourceID;
    private String securityProtocol;
    private String gridFTPEP;

    private ComputeHostResource computeHostResource;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRID_FTP_DATAMOVEMENT);
            generator.setParameter(GridFTPDataMovementConstants.DATA_MOVE_ID, identifier);
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
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRID_FTP_DATAMOVEMENT);
            generator.setParameter(GridFTPDataMovementConstants.DATA_MOVE_ID, identifier);
            Query q = generator.selectQuery(em);
            GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) q.getSingleResult();
            GridFTPDataMovementResource gridFTPDataMovementResource =
                    (GridFTPDataMovementResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.GRID_FTP_DATAMOVEMENT, gridFTPDataMovement);
            em.getTransaction().commit();
            em.close();
            return gridFTPDataMovementResource;
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
        List<Resource> gridFTPDataMoveResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRID_FTP_DATAMOVEMENT);
            List results;
            if (fieldName.equals(GridFTPDataMovementConstants.DATA_MOVE_ID)) {
                generator.setParameter(GridFTPDataMovementConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        GridFTPDataMovementResource gridFTPDataMovementResource =
                                (GridFTPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DATAMOVEMENT, gridFTPDataMovement);
                        gridFTPDataMoveResources.add(gridFTPDataMovementResource);
                    }
                }
            } else if (fieldName.equals(GridFTPDataMovementConstants.RESOURCE_ID)) {
                generator.setParameter(GridFTPDataMovementConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        GridFTPDataMovementResource gridFTPDataMovementResource =
                                (GridFTPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DATAMOVEMENT, gridFTPDataMovement);
                        gridFTPDataMoveResources.add(gridFTPDataMovementResource);
                    }
                }
            } else if (fieldName.equals(GridFTPDataMovementConstants.SECURITY_PROTOCOL)) {
                generator.setParameter(GridFTPDataMovementConstants.SECURITY_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        GridFTPDataMovementResource gridFTPDataMovementResource =
                                (GridFTPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DATAMOVEMENT, gridFTPDataMovement);
                        gridFTPDataMoveResources.add(gridFTPDataMovementResource);
                    }
                }
            } else if (fieldName.equals(GridFTPDataMovementConstants.GRID_FTP_EP)) {
                generator.setParameter(GridFTPDataMovementConstants.GRID_FTP_EP, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        GridFTPDataMovementResource gridFTPDataMovementResource =
                                (GridFTPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DATAMOVEMENT, gridFTPDataMovement);
                        gridFTPDataMoveResources.add(gridFTPDataMovementResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Grid FTP Data Movement resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Grid FTP Data Movement resource.");
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
        return gridFTPDataMoveResources;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> gridFTPDataMoveIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRID_FTP_DATAMOVEMENT);
            List results;
            if (fieldName.equals(GridFTPDataMovementConstants.DATA_MOVE_ID)) {
                generator.setParameter(GridFTPDataMovementConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        gridFTPDataMoveIDs.add(gridFTPDataMovement.getDataMoveID());
                    }
                }
            } else if (fieldName.equals(GridFTPDataMovementConstants.RESOURCE_ID)) {
                generator.setParameter(GridFTPDataMovementConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        gridFTPDataMoveIDs.add(gridFTPDataMovement.getDataMoveID());
                    }
                }
            } else if (fieldName.equals(GridFTPDataMovementConstants.SECURITY_PROTOCOL)) {
                generator.setParameter(GridFTPDataMovementConstants.SECURITY_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        gridFTPDataMoveIDs.add(gridFTPDataMovement.getDataMoveID());
                    }
                }
            } else if (fieldName.equals(GridFTPDataMovementConstants.GRID_FTP_EP)) {
                generator.setParameter(GridFTPDataMovementConstants.GRID_FTP_EP, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDataMovement gridFTPDataMovement = (GridFTPDataMovement) result;
                        gridFTPDataMoveIDs.add(gridFTPDataMovement.getDataMoveID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Grid FTP Data movement resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Grid FTP Data movement resource.");
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
        return gridFTPDataMoveIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GridFTPDataMovement existingGridFTPDataMovement = em.find(GridFTPDataMovement.class, dataMoveID);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingGridFTPDataMovement != null) {
                existingGridFTPDataMovement.setDataMoveID(dataMoveID);
                existingGridFTPDataMovement.setResourceID(resourceID);
                existingGridFTPDataMovement.setGridFTPEP(gridFTPEP);
                existingGridFTPDataMovement.setSecurityProtocol(securityProtocol);
                ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
                existingGridFTPDataMovement.setComputeResource(computeResource);

                em.merge(existingGridFTPDataMovement);
            } else {
                GridFTPDataMovement gridFTPDataMovement = new GridFTPDataMovement();
                gridFTPDataMovement.setResourceID(resourceID);
                gridFTPDataMovement.setDataMoveID(dataMoveID);
                gridFTPDataMovement.setGridFTPEP(gridFTPEP);
                gridFTPDataMovement.setSecurityProtocol(securityProtocol);
                ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
                gridFTPDataMovement.setComputeResource(computeResource);
                em.persist(gridFTPDataMovement);
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
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GridFTPDataMovement gridFTPDataMovement = em.find(GridFTPDataMovement.class, identifier);
            em.close();
            return gridFTPDataMovement != null;
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


    public String getDataMoveID() {
        return dataMoveID;
    }

    public void setDataMoveID(String dataMoveID) {
        this.dataMoveID = dataMoveID;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getGridFTPEP() {
        return gridFTPEP;
    }

    public void setGridFTPEP(String gridFTPEP) {
        this.gridFTPEP = gridFTPEP;
    }

    public ComputeHostResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeHostResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }
}
