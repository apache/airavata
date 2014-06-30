package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.GSISSHPreJobCommand;
import org.apache.aiaravata.application.catalog.data.model.GSISSHPreJobCommandPK;
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

public class GSISSHPreJobCommandResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GSISSHPreJobCommandResource.class);

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
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(GSISSH_PREJOBCOMMAND);
            generator.setParameter(GSISSHPreJobCommandConstants.SUBMISSION_ID,
                    ids.get(GSISSHPreJobCommandConstants.SUBMISSION_ID));
            generator.setParameter(GSISSHPreJobCommandConstants.COMMAND, ids.get(GSISSHPreJobCommandConstants.COMMAND));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_PREJOBCOMMAND);
            generator.setParameter(GSISSHPreJobCommandConstants.SUBMISSION_ID,
                    ids.get(GSISSHPreJobCommandConstants.SUBMISSION_ID));
            generator.setParameter(GSISSHPreJobCommandConstants.COMMAND, ids.get(GSISSHPreJobCommandConstants.COMMAND));
            Query q = generator.selectQuery(em);
            GSISSHPreJobCommand gsisshPreJobCommand = (GSISSHPreJobCommand) q.getSingleResult();
            GSISSHPreJobCommandResource gsisshPreJobCommandResource =
                    (GSISSHPreJobCommandResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.GSISSH_PREJOBCOMMAND, gsisshPreJobCommand);
            em.getTransaction().commit();
            em.close();
            return gsisshPreJobCommandResource;
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
        List<Resource> gsiSSHPreJobResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_PREJOBCOMMAND);
            List results;
            if (fieldName.equals(GSISSHPreJobCommandConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHPreJobCommandConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPreJobCommand gsisshPreJobCommand = (GSISSHPreJobCommand) result;
                        GSISSHPreJobCommandResource gsisshPreJobCommandResource =
                                (GSISSHPreJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_PREJOBCOMMAND, gsisshPreJobCommand);
                        gsiSSHPreJobResources.add(gsisshPreJobCommandResource);
                    }
                }
            } else if (fieldName.equals(GSISSHPreJobCommandConstants.COMMAND)) {
                generator.setParameter(GSISSHPreJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPreJobCommand gsisshPreJobCommand = (GSISSHPreJobCommand) result;
                        GSISSHPreJobCommandResource gsisshPreJobCommandResource =
                                (GSISSHPreJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_PREJOBCOMMAND, gsisshPreJobCommand);
                        gsiSSHPreJobResources.add(gsisshPreJobCommandResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Pre Job Command Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Pre Job Command Resource.");
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
        return gsiSSHPreJobResources;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> gsiSSHPreJobResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_PREJOBCOMMAND);
            List results;
            if (fieldName.equals(GSISSHPreJobCommandConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHPreJobCommandConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPreJobCommand gsisshPreJobCommand = (GSISSHPreJobCommand) result;
                        gsiSSHPreJobResourceIDs.add(gsisshPreJobCommand.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHPreJobCommandConstants.COMMAND)) {
                generator.setParameter(GSISSHPreJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHPreJobCommand gsisshPreJobCommand = (GSISSHPreJobCommand) result;
                        gsiSSHPreJobResourceIDs.add(gsisshPreJobCommand.getSubmissionID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Pre Job resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Pre JOb Resource.");
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
        return gsiSSHPreJobResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GSISSHPreJobCommand existingGSIsshPreJobCommand = em.find(GSISSHPreJobCommand.class,
                    new GSISSHPreJobCommandPK(submissionID, command));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GSISSHSubmission gsisshSubmission = em.find(GSISSHSubmission.class, submissionID);
            if (existingGSIsshPreJobCommand !=  null){
                existingGSIsshPreJobCommand.setSubmissionID(submissionID);
                existingGSIsshPreJobCommand.setCommand(command);
                existingGSIsshPreJobCommand.setGsisshSubmission(gsisshSubmission);
                em.merge(existingGSIsshPreJobCommand);
            }else {
                GSISSHPreJobCommand gsisshPreJobCommand = new GSISSHPreJobCommand();
                gsisshPreJobCommand.setSubmissionID(submissionID);
                gsisshPreJobCommand.setCommand(command);
                gsisshPreJobCommand.setGsisshSubmission(gsisshSubmission);
                em.persist(gsisshPreJobCommand);
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
            GSISSHPreJobCommand gsisshPreJobCommand = em.find(GSISSHPreJobCommand.class, new GSISSHPreJobCommandPK(
                    ids.get(GSISSHPreJobCommandConstants.SUBMISSION_ID),
                    ids.get(GSISSHPreJobCommandConstants.COMMAND)));

            em.close();
            return gsisshPreJobCommand != null;
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
