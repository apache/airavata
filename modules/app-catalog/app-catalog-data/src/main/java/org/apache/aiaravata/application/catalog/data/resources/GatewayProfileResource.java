package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.GatewayProfile;
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

public class GatewayProfileResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GatewayProfileResource.class);

    private String gatewayID;
    private String gatewayName;
    private String gatewayDesc;
    private String preferedResource;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_PROFILE);
            generator.setParameter(GatewayProfileConstants.GATEWAY_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_PROFILE);
            generator.setParameter(GatewayProfileConstants.GATEWAY_ID, identifier);
            Query q = generator.selectQuery(em);
            GatewayProfile gatewayProfile = (GatewayProfile) q.getSingleResult();
            GatewayProfileResource gatewayProfileResource =
                    (GatewayProfileResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.GATEWAY_PROFILE, gatewayProfile);
            em.getTransaction().commit();
            em.close();
            return gatewayProfileResource;
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
        List<Resource> gatewayProfileResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_PROFILE);
            List results;
            if (fieldName.equals(GatewayProfileConstants.GATEWAY_ID)) {
                generator.setParameter(GatewayProfileConstants.GATEWAY_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GatewayProfile gatewayProfile = (GatewayProfile) result;
                        GatewayProfileResource gatewayProfileResource =
                                (GatewayProfileResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GATEWAY_PROFILE, gatewayProfile);
                        gatewayProfileResources.add(gatewayProfileResource);
                    }
                }
            } else if (fieldName.equals(GatewayProfileConstants.GATEWAY_NAME)) {
                generator.setParameter(GatewayProfileConstants.GATEWAY_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GatewayProfile gatewayProfile = (GatewayProfile) result;
                        GatewayProfileResource gatewayProfileResource =
                                (GatewayProfileResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GATEWAY_PROFILE, gatewayProfile);
                        gatewayProfileResources.add(gatewayProfileResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Gateway Profile resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Gateway Profile resource.");
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
        return gatewayProfileResources;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> gatewayProfileResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_PROFILE);
            List results;
            if (fieldName.equals(GatewayProfileConstants.GATEWAY_ID)) {
                generator.setParameter(GatewayProfileConstants.GATEWAY_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GatewayProfile gatewayProfile = (GatewayProfile) result;
                        gatewayProfileResourceIDs.add(gatewayProfile.getGatewayID());
                    }
                }
            } else if (fieldName.equals(GatewayProfileConstants.GATEWAY_NAME)) {
                generator.setParameter(GatewayProfileConstants.GATEWAY_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GatewayProfile gatewayProfile = (GatewayProfile) result;
                        gatewayProfileResourceIDs.add(gatewayProfile.getGatewayID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Gateway Profile resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Gateway Profile resource.");
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
        return gatewayProfileResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GatewayProfile existingGatewayProfile = em.find(GatewayProfile.class, gatewayID);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingGatewayProfile != null) {
                existingGatewayProfile.setGatewayDesc(gatewayDesc);
                existingGatewayProfile.setGatewayName(gatewayName);
                existingGatewayProfile.setPreferedResource(preferedResource);
                em.merge(existingGatewayProfile);
            } else {
                GatewayProfile gatewayProfile = new GatewayProfile();
                gatewayProfile.setGatewayID(gatewayID);
                gatewayProfile.setGatewayName(gatewayName);
                gatewayProfile.setGatewayDesc(gatewayDesc);
                gatewayProfile.setPreferedResource(preferedResource);
                em.persist(gatewayProfile);
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
            GatewayProfile gatewayProfile = em.find(GatewayProfile.class, identifier);
            em.close();
            return gatewayProfile != null;
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

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getGatewayDesc() {
        return gatewayDesc;
    }

    public void setGatewayDesc(String gatewayDesc) {
        this.gatewayDesc = gatewayDesc;
    }

    public String getPreferedResource() {
        return preferedResource;
    }

    public void setPreferedResource(String preferedResource) {
        this.preferedResource = preferedResource;
    }
}
