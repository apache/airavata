package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.SCPDataMovement;
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

public class SCPDataMovementResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(SCPDataMovementResource.class);

    private String resourceID;
    private String dataMoveID;
    private String securityProtocol;
    private int sshPort;

    private ComputeHostResource computeHostResource;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SCP_DATAMOVEMENT);
            generator.setParameter(SCPDataMovementConstants.DATA_MOVE_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SCP_DATAMOVEMENT);
            generator.setParameter(SCPDataMovementConstants.DATA_MOVE_ID, identifier);
            Query q = generator.selectQuery(em);
            SCPDataMovement scpDataMovement = (SCPDataMovement) q.getSingleResult();
            SCPDataMovementResource scpDataMovementResource =
                    (SCPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SCP_DATAMOVEMENT, scpDataMovement);
            em.getTransaction().commit();
            em.close();
            return scpDataMovementResource;
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
        List<Resource> scpDataMoveResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SCP_DATAMOVEMENT);
            List results;
            if (fieldName.equals(SCPDataMovementConstants.RESOURCE_ID)) {
                generator.setParameter(SCPDataMovementConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        SCPDataMovementResource scpDataMovementResource =
                                (SCPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SCP_DATAMOVEMENT, scpDataMovement);
                        scpDataMoveResources.add(scpDataMovementResource);
                    }
                }
            } else if (fieldName.equals(SCPDataMovementConstants.SSH_PORT)) {
                generator.setParameter(SCPDataMovementConstants.SSH_PORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        SCPDataMovementResource scpDataMovementResource =
                                (SCPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SCP_DATAMOVEMENT, scpDataMovement);
                        scpDataMoveResources.add(scpDataMovementResource);
                    }
                }
            } else if (fieldName.equals(SCPDataMovementConstants.SECURITY_PROTOCOL)) {
                generator.setParameter(SCPDataMovementConstants.SECURITY_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        SCPDataMovementResource scpDataMovementResource =
                                (SCPDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SCP_DATAMOVEMENT, scpDataMovement);
                        scpDataMoveResources.add(scpDataMovementResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for SCP Data Movement resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for SCP Data Movement resource.");
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
        return scpDataMoveResources;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> scpDataMoveIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SCP_DATAMOVEMENT);
            List results;
            if (fieldName.equals(SCPDataMovementConstants.DATA_MOVE_ID)) {
                generator.setParameter(SCPDataMovementConstants.DATA_MOVE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        scpDataMoveIDs.add(scpDataMovement.getDataMoveID());
                    }
                }
            }else if (fieldName.equals(SCPDataMovementConstants.SSH_PORT)) {
                generator.setParameter(SCPDataMovementConstants.SSH_PORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        scpDataMoveIDs.add(scpDataMovement.getDataMoveID());
                    }
                }
            }else if (fieldName.equals(SCPDataMovementConstants.RESOURCE_ID)) {
                generator.setParameter(SCPDataMovementConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        scpDataMoveIDs.add(scpDataMovement.getDataMoveID());
                    }
                }
            }else if (fieldName.equals(SCPDataMovementConstants.SECURITY_PROTOCOL)) {
                generator.setParameter(SCPDataMovementConstants.SECURITY_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SCPDataMovement scpDataMovement = (SCPDataMovement) result;
                        scpDataMoveIDs.add(scpDataMovement.getDataMoveID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for SCP Data movement resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for SCP Data movement resource.");
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
        return scpDataMoveIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            SCPDataMovement existingSCPDataMovement = em.find(SCPDataMovement.class, dataMoveID);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
            if (existingSCPDataMovement !=  null){
                existingSCPDataMovement.setDataMoveID(dataMoveID);
                existingSCPDataMovement.setResourceID(resourceID);
                existingSCPDataMovement.setSshPort(sshPort);
                existingSCPDataMovement.setSecurityProtocol(securityProtocol);
                existingSCPDataMovement.setComputeResource(computeResource);
                em.merge(existingSCPDataMovement);
            }else {
                SCPDataMovement scpDataMovement = new SCPDataMovement();
                scpDataMovement.setResourceID(resourceID);
                scpDataMovement.setDataMoveID(dataMoveID);
                scpDataMovement.setSshPort(sshPort);
                scpDataMovement.setSecurityProtocol(securityProtocol);
                scpDataMovement.setComputeResource(computeResource);
                em.persist(scpDataMovement);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
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
            SCPDataMovement scpDataMovement = em.find(SCPDataMovement.class, identifier);
            em.close();
            return scpDataMovement != null;
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

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public ComputeHostResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeHostResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }
}
