package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.SSHSubmission;
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

public class SSHSubmissionResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(SSHSubmissionResource.class);

    private String resourceID;
    private String submissionID;
    private String resourceJobManager;
    private int sshPort;
    private ComputeHostResource computeHostResource;


    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_SUBMISSION);
            generator.setParameter(SSHSubmissionConstants.SUBMISSION_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_SUBMISSION);
            generator.setParameter(SSHSubmissionConstants.SUBMISSION_ID, identifier);
            Query q = generator.selectQuery(em);
            SSHSubmission sshSubmission = (SSHSubmission) q.getSingleResult();
            SSHSubmissionResource sshSubmissionResource =
                    (SSHSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SSH_SUBMISSION, sshSubmission);
            em.getTransaction().commit();
            em.close();
            return sshSubmissionResource;
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
        List<Resource> sshSubmissionResourceList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_SUBMISSION);
            List results;
            if (fieldName.equals(SSHSubmissionConstants.RESOURCE_ID)) {
                generator.setParameter(SSHSubmissionConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SSHSubmission sshSubmission = (SSHSubmission) result;
                        SSHSubmissionResource sshSubmissionResource =
                                (SSHSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SSH_SUBMISSION, sshSubmission);
                        sshSubmissionResourceList.add(sshSubmissionResource);
                    }
                }
            } else if (fieldName.equals(SSHSubmissionConstants.RESOURCE_JOB_MANAGER)) {
                generator.setParameter(SSHSubmissionConstants.RESOURCE_JOB_MANAGER, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SSHSubmission sshSubmission = (SSHSubmission) result;
                        SSHSubmissionResource sshSubmissionResource =
                                (SSHSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SSH_SUBMISSION, sshSubmission);
                        sshSubmissionResourceList.add(sshSubmissionResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for SSH submission resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for SSH Submission resource.");
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
        return sshSubmissionResourceList;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> sshSubmissionResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_SUBMISSION);
            List results;
            if (fieldName.equals(SSHSubmissionConstants.SUBMISSION_ID)) {
                generator.setParameter(SSHSubmissionConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SSHSubmission sshSubmission = (SSHSubmission) result;
                        sshSubmissionResourceIDs.add(sshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(SSHSubmissionConstants.RESOURCE_ID)) {
                generator.setParameter(SSHSubmissionConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SSHSubmission sshSubmission = (SSHSubmission) result;
                        sshSubmissionResourceIDs.add(sshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(SSHSubmissionConstants.RESOURCE_JOB_MANAGER)) {
                generator.setParameter(SSHSubmissionConstants.RESOURCE_JOB_MANAGER, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SSHSubmission sshSubmission = (SSHSubmission) result;
                        sshSubmissionResourceIDs.add(sshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(SSHSubmissionConstants.SSH_PORT)) {
                generator.setParameter(SSHSubmissionConstants.SSH_PORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        SSHSubmission sshSubmission = (SSHSubmission) result;
                        sshSubmissionResourceIDs.add(sshSubmission.getSubmissionID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for SSH Submission resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for SSH Submission resource.");
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
        return sshSubmissionResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            SSHSubmission existingSSHSubmission = em.find(SSHSubmission.class, submissionID);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
            if (existingSSHSubmission != null) {
                existingSSHSubmission.setSubmissionID(submissionID);
                existingSSHSubmission.setResourceID(resourceID);
                existingSSHSubmission.setSshPort(sshPort);
                existingSSHSubmission.setResourceJobManager(resourceJobManager);
                existingSSHSubmission.setComputeResource(computeResource);
                em.merge(existingSSHSubmission);
            } else {
                SSHSubmission sshSubmission = new SSHSubmission();
                sshSubmission.setResourceID(resourceID);
                sshSubmission.setSubmissionID(submissionID);
                sshSubmission.setSshPort(sshPort);
                sshSubmission.setResourceJobManager(resourceJobManager);
                sshSubmission.setComputeResource(computeResource);
                em.persist(sshSubmission);
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
            SSHSubmission sshSubmission = em.find(SSHSubmission.class, identifier);
            em.close();
            return sshSubmission != null;
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

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(String resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
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
