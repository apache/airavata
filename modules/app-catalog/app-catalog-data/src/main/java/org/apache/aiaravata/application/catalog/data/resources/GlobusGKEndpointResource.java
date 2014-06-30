package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.*;
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

public class GlobusGKEndpointResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GlobusGKEndpointResource.class);

    private String submissionID;
    private String endpoint;

    private GlobusJobSubmissionResource globusJobSubmissionResource;


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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            generator.setParameter(GlobusEPConstants.ENDPOINT, ids.get(GlobusEPConstants.ENDPOINT));
            generator.setParameter(GlobusEPConstants.SUBMISSION_ID, ids.get(GlobusEPConstants.SUBMISSION_ID));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            generator.setParameter(GlobusEPConstants.SUBMISSION_ID, ids.get(GlobusEPConstants.SUBMISSION_ID));
            generator.setParameter(GlobusEPConstants.ENDPOINT, ids.get(GlobusEPConstants.ENDPOINT));
            Query q = generator.selectQuery(em);
            GlobusGKEndpoint gkEndpoint = (GlobusGKEndpoint) q.getSingleResult();
            GlobusGKEndpointResource gkEndpointResource =
                    (GlobusGKEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GLOBUS_GK_ENDPOINT, gkEndpoint);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            List results;
            if (fieldName.equals(GlobusEPConstants.ENDPOINT)) {
                generator.setParameter(GlobusEPConstants.ENDPOINT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint gkEndpoint = (GlobusGKEndpoint) result;
                        GlobusGKEndpointResource gkEndpointResource =
                                (GlobusGKEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GLOBUS_GK_ENDPOINT, gkEndpoint);
                        resources.add(gkEndpointResource);
                    }
                }
            } else if (fieldName.equals(GlobusEPConstants.SUBMISSION_ID)) {
                generator.setParameter(GlobusEPConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint globusGKEndpoint = (GlobusGKEndpoint) result;
                        GlobusGKEndpointResource gkEndpointResource =
                                (GlobusGKEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GLOBUS_GK_ENDPOINT, globusGKEndpoint);
                        resources.add(gkEndpointResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Globus Endpoint Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Globus Endpoint Resource.");
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            List results;
            if (fieldName.equals(GlobusEPConstants.SUBMISSION_ID)) {
                generator.setParameter(GlobusEPConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint globusGKEndpoint = (GlobusGKEndpoint) result;
                        list.add(globusGKEndpoint.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GlobusEPConstants.ENDPOINT)) {
                generator.setParameter(GlobusEPConstants.ENDPOINT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint globusGKEndpoint = (GlobusGKEndpoint) result;
                        list.add(globusGKEndpoint.getSubmissionID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Globus EP resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Globus EP Resource.");
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
            GlobusGKEndpoint existingGlobusEP = em.find(GlobusGKEndpoint.class, new GlobusGKEndPointPK(submissionID, endpoint));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GlobusJobSubmission globusJobSubmission = em.find(GlobusJobSubmission.class, submissionID);
            if (existingGlobusEP != null) {
                existingGlobusEP.setSubmissionID(submissionID);
                existingGlobusEP.setEndpoint(endpoint);
                existingGlobusEP.setGlobusSubmission(globusJobSubmission);
                em.merge(existingGlobusEP);
            } else {
                GlobusGKEndpoint globusGKEndpoint = new GlobusGKEndpoint();
                globusGKEndpoint.setSubmissionID(submissionID);
                globusGKEndpoint.setEndpoint(endpoint);
                globusGKEndpoint.setGlobusSubmission(globusJobSubmission);
                em.persist(globusGKEndpoint);
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
            GlobusGKEndpoint gkEndpoint = em.find(GlobusGKEndpoint.class, new GlobusGKEndPointPK(ids.get(GlobusEPConstants.SUBMISSION_ID),
                    ids.get(GlobusEPConstants.ENDPOINT)));

            em.close();
            return gkEndpoint != null;
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

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public GlobusJobSubmissionResource getGlobusJobSubmissionResource() {
        return globusJobSubmissionResource;
    }

    public void setGlobusJobSubmissionResource(GlobusJobSubmissionResource globusJobSubmissionResource) {
        this.globusJobSubmissionResource = globusJobSubmissionResource;
    }
}
