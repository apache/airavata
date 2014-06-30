package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.GridFTPDMEndPointPK;
import org.apache.aiaravata.application.catalog.data.model.GridFTPDMEndpoint;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridFTPDMEndpointResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GridFTPDMEndpointResource.class);

    private String dataMoveId;
    private String endpoint;

    private GridFTPDataMovementResource gridFTPDataMovementResource;


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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DM_ENDPOINT);
            generator.setParameter(GridFTPDMEPConstants.ENDPOINT, ids.get(GridFTPDMEPConstants.ENDPOINT));
            generator.setParameter(GridFTPDMEPConstants.DATA_MOVE_ID, ids.get(GridFTPDMEPConstants.DATA_MOVE_ID));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DM_ENDPOINT);
            generator.setParameter(GridFTPDMEPConstants.DATA_MOVE_ID, ids.get(GridFTPDMEPConstants.DATA_MOVE_ID));
            generator.setParameter(GridFTPDMEPConstants.ENDPOINT, ids.get(GridFTPDMEPConstants.ENDPOINT));
            Query q = generator.selectQuery(em);
            GridFTPDMEndpoint result = (GridFTPDMEndpoint) q.getSingleResult();
            GridFTPDMEndpointResource gkEndpointResource =
                    (GridFTPDMEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DM_ENDPOINT, result);
            em.getTransaction().commit();
            em.close();
            return gkEndpointResource;
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
        List<Resource> resources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DM_ENDPOINT);
            List results;
            if (fieldName.equals(GridFTPDMEPConstants.ENDPOINT)) {
                generator.setParameter(GridFTPDMEPConstants.ENDPOINT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDMEndpoint gridFTPDMEndpoint = (GridFTPDMEndpoint) result;
                        GridFTPDMEndpointResource gkEndpointResource =
                                (GridFTPDMEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DM_ENDPOINT, gridFTPDMEndpoint);
                        resources.add(gkEndpointResource);
                    }
                }
            } else if (fieldName.equals(GridFTPDMEPConstants.DATA_MOVE_ID)) {
                generator.setParameter(GridFTPDMEPConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDMEndpoint gridFTPDMEndpoint = (GridFTPDMEndpoint) result;
                        GridFTPDMEndpointResource gkEndpointResource =
                                (GridFTPDMEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRID_FTP_DM_ENDPOINT, gridFTPDMEndpoint);
                        resources.add(gkEndpointResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GridFTPDMEndpoint Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GridFTPDMEndpoint Resource.");
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
        return resources;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> list = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DM_ENDPOINT);
            List results;
            if (fieldName.equals(GridFTPDMEPConstants.DATA_MOVE_ID)) {
                generator.setParameter(GridFTPDMEPConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDMEndpoint gridFTPDMEndpoint = (GridFTPDMEndpoint) result;
                        list.add(gridFTPDMEndpoint.getDataMoveId());
                    }
                }
            } else if (fieldName.equals(GridFTPDMEPConstants.ENDPOINT)) {
                generator.setParameter(GridFTPDMEPConstants.ENDPOINT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GridFTPDMEndpoint gridFTPDMEndpoint = (GridFTPDMEndpoint) result;
                        list.add(gridFTPDMEndpoint.getDataMoveId());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GridFTPDMEndpoint resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GridFTPDMEndpoint Resource.");
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
        return list;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GridFTPDMEndpoint ftpdmEndpoint = em.find(GridFTPDMEndpoint.class, new GridFTPDMEndPointPK(dataMoveId, endpoint));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GridFTPDataMovement dataMovement = em.find(GridFTPDataMovement.class, dataMoveId);
            if (ftpdmEndpoint != null) {
                ftpdmEndpoint.setDataMoveId(dataMoveId);
                ftpdmEndpoint.setEndpoint(endpoint);
                ftpdmEndpoint.setGridFTPDataMovement(dataMovement);
                em.merge(ftpdmEndpoint);
            } else {
                GridFTPDMEndpoint gridFTPDMEndpoint = new GridFTPDMEndpoint();
                gridFTPDMEndpoint.setDataMoveId(dataMoveId);
                gridFTPDMEndpoint.setEndpoint(endpoint);
                gridFTPDMEndpoint.setGridFTPDataMovement(dataMovement);
                em.persist(gridFTPDMEndpoint);
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
        if (identifier instanceof Map){
            ids = (HashMap)identifier;
        }else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GridFTPDMEndpoint gridFTPDMEndpoint = em.find(GridFTPDMEndpoint.class, new GridFTPDMEndPointPK(ids.get(GridFTPDMEPConstants.DATA_MOVE_ID),
                    ids.get(GridFTPDMEPConstants.ENDPOINT)));

            em.close();
            return gridFTPDMEndpoint != null;
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
        }    }

    public String getDataMoveId() {
        return dataMoveId;
    }

    public void setDataMoveId(String dataMoveId) {
        this.dataMoveId = dataMoveId;
    }

    public GridFTPDataMovementResource getGridFTPDataMovementResource() {
        return gridFTPDataMovementResource;
    }

    public void setGridFTPDataMovementResource(GridFTPDataMovementResource gridFTPDataMovementResource) {
        this.gridFTPDataMovementResource = gridFTPDataMovementResource;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

}
