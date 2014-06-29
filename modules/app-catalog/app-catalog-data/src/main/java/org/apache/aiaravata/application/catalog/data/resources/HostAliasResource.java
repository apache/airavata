package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.HostAlias;
import org.apache.aiaravata.application.catalog.data.model.HostAliasPK;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class HostAliasResource extends AbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(HostAliasResource.class);

    private String resourceID;
    private String alias;
    private ComputeHostResource computeHostResource;

    public void remove(Object identifier) throws AppCatalogException {
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
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(HOST_ALIAS);
            generator.setParameter(HostAliasConstants.RESOURCE_ID, ids.get(HostAliasConstants.RESOURCE_ID));
            generator.setParameter(HostAliasConstants.ALIAS, ids.get(HostAliasConstants.ALIAS));
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
        } catch (ApplicationSettingsException e) {
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

    public Resource get(Object identifier) throws AppCatalogException {
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
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_ALIAS);
            generator.setParameter(HostAliasConstants.RESOURCE_ID, ids.get(HostAliasConstants.RESOURCE_ID));
            generator.setParameter(HostAliasConstants.ALIAS, ids.get(HostAliasConstants.ALIAS));
            Query q = generator.selectQuery(em);
            HostAlias hostAlias = (HostAlias) q.getSingleResult();
            HostAliasResource hostAliasResource =
                    (HostAliasResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_ALIAS, hostAlias);
            em.getTransaction().commit();
            em.close();
            return hostAliasResource;
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

        List<Resource> hostAliasResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_ALIAS);
            List results;
            if (fieldName.equals(HostAliasConstants.ALIAS)) {
                generator.setParameter(HostAliasConstants.ALIAS, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        HostAliasResource hostAliasResource =
                                (HostAliasResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_ALIAS, hostAlias);
                        hostAliasResources.add(hostAliasResource);
                    }
                }
            } else if (fieldName.equals(HostAliasConstants.RESOURCE_ID)) {
                generator.setParameter(HostAliasConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        HostAliasResource hostAliasResource =
                                (HostAliasResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_ALIAS, hostAlias);
                        hostAliasResources.add(hostAliasResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Host Alias Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Host Alias Resource.");
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
        return hostAliasResources;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {

        List<String> hostAliasResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_ALIAS);
            List results;
            if (fieldName.equals(HostAliasConstants.ALIAS)) {
                generator.setParameter(HostAliasConstants.ALIAS, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        hostAliasResourceIDs.add(hostAlias.getResourceID());
                    }
                }
            } else if (fieldName.equals(HostAliasConstants.RESOURCE_ID)) {
                generator.setParameter(HostAliasConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        hostAliasResourceIDs.add(hostAlias.getResourceID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Host Alias resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Host Alias Resource.");
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
        return hostAliasResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            HostAlias existingHostAlias = em.find(HostAlias.class, new HostAliasPK(resourceID, alias));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingHostAlias !=  null){
                existingHostAlias.setAlias(alias);
                ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
                existingHostAlias.setComputeResource(computeResource);
                existingHostAlias.setResourceID(resourceID);

                em.merge(existingHostAlias);
            }else {
                HostAlias hostAlias = new HostAlias();
                hostAlias.setAlias(alias);
                hostAlias.setResourceID(resourceID);
                ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
                hostAlias.setComputeResource(computeResource);

                em.persist(hostAlias);
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
            HostAlias hostAlias = em.find(HostAlias.class, new HostAliasPK(ids.get(HostAliasConstants.RESOURCE_ID),
                    ids.get(HostAliasConstants.ALIAS)));

            em.close();
            return hostAlias != null;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ComputeHostResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeHostResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }
}
