package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.GSISSHPostJobCommand;
import org.apache.aiaravata.application.catalog.data.model.GSISSHPostJobCommandPK;
import org.apache.aiaravata.application.catalog.data.model.GSISSHSubmission;
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

public class GSISSHPostJobCommandResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GSISSHPostJobCommandResource.class);

    private String submissionID;
    private String command;

    private GSISSHSubmissionResource gsisshSubmissionResource;


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
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(GSISSH_POSTJOBCOMMAND);
            generator.setParameter(GSISSHPostJobCommandConstants.SUBMISSION_ID,
                    ids.get(GSISSHPostJobCommandConstants.SUBMISSION_ID));
            generator.setParameter(GSISSHPostJobCommandConstants.COMMAND, ids.get(GSISSHPostJobCommandConstants.COMMAND));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_POSTJOBCOMMAND);
            generator.setParameter(GSISSHPostJobCommandConstants.SUBMISSION_ID,
                    ids.get(GSISSHPostJobCommandConstants.SUBMISSION_ID));
            generator.setParameter(GSISSHPostJobCommandConstants.COMMAND, ids.get(GSISSHPostJobCommandConstants.COMMAND));
            Query q = generator.selectQuery(em);
            GSISSHPostJobCommand gsisshPostJobCommand = (GSISSHPostJobCommand) q.getSingleResult();
            GSISSHPostJobCommandResource gsisshPostJobCommandResource =
                    (GSISSHPostJobCommandResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.GSISSH_POSTJOBCOMMAND, gsisshPostJobCommand);
            em.getTransaction().commit();
            em.close();
            return gsisshPostJobCommandResource;
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
        List<Resource> gsiSSHPostJobCommandResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_POSTJOBCOMMAND);
            List results;
            if (fieldName.equals(GSISSHPostJobCommandConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHPostJobCommandConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPostJobCommand gsisshPostJobCommand = (GSISSHPostJobCommand) result;
                        GSISSHPostJobCommandResource gsisshPostJobCommandResource =
                                (GSISSHPostJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_POSTJOBCOMMAND, gsisshPostJobCommand);
                        gsiSSHPostJobCommandResources.add(gsisshPostJobCommandResource);
                    }
                }
            } else if (fieldName.equals(GSISSHPostJobCommandConstants.COMMAND)) {
                generator.setParameter(GSISSHPostJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPostJobCommand gsisshPostJobCommand = (GSISSHPostJobCommand) result;
                        GSISSHPostJobCommandResource gsisshPostJobCommandResource =
                                (GSISSHPostJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_POSTJOBCOMMAND, gsisshPostJobCommand);
                        gsiSSHPostJobCommandResources.add(gsisshPostJobCommandResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Post Job Command Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Post Job Command Resource.");
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
        return gsiSSHPostJobCommandResources;
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
        List<String> gsiSSHPostJobResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_POSTJOBCOMMAND);
            List results;
            if (fieldName.equals(GSISSHPostJobCommandConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHPostJobCommandConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPostJobCommand gsisshPostJobCommand = (GSISSHPostJobCommand) result;
                        gsiSSHPostJobResourceIDs.add(gsisshPostJobCommand.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHPostJobCommandConstants.COMMAND)) {
                generator.setParameter(GSISSHPostJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPostJobCommand gsisshPostJobCommand = (GSISSHPostJobCommand) result;
                        gsiSSHPostJobResourceIDs.add(gsisshPostJobCommand.getSubmissionID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Post Job resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Post JOb Resource.");
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
        return gsiSSHPostJobResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GSISSHPostJobCommand existingPostJobCommand = em.find(GSISSHPostJobCommand.class,
                    new GSISSHPostJobCommandPK(submissionID, command));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GSISSHSubmission gsisshSubmission = em.find(GSISSHSubmission.class, submissionID);
            if (existingPostJobCommand !=  null){
                existingPostJobCommand.setSubmissionID(submissionID);
                existingPostJobCommand.setCommand(command);
                existingPostJobCommand.setGsisshSubmission(gsisshSubmission);
                em.merge(existingPostJobCommand);
            }else {
                GSISSHPostJobCommand gsisshPostJobCommand = new GSISSHPostJobCommand();
                gsisshPostJobCommand.setSubmissionID(submissionID);
                gsisshPostJobCommand.setCommand(command);
                gsisshPostJobCommand.setGsisshSubmission(gsisshSubmission);
                em.persist(gsisshPostJobCommand);
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
            GSISSHPostJobCommand gsisshPostJobCommand = em.find(GSISSHPostJobCommand.class, new GSISSHPostJobCommandPK(
                    ids.get(GSISSHPostJobCommandConstants.SUBMISSION_ID),
                    ids.get(GSISSHPostJobCommandConstants.COMMAND)));

            em.close();
            return gsisshPostJobCommand != null;
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

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public GSISSHSubmissionResource getGsisshSubmissionResource() {
        return gsisshSubmissionResource;
    }

    public void setGsisshSubmissionResource(GSISSHSubmissionResource gsisshSubmissionResource) {
        this.gsisshSubmissionResource = gsisshSubmissionResource;
    }
}
