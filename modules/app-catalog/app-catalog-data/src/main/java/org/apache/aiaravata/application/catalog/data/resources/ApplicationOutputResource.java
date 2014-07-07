package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.AppOutput_PK;
import org.apache.aiaravata.application.catalog.data.model.ApplicationInterface;
import org.apache.aiaravata.application.catalog.data.model.ApplicationOutput;
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

public class ApplicationOutputResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationOutputResource.class);

    private String interfaceID;
    private String outputKey;
    private String outputVal;
    private String dataType;

    private AppInterfaceResource appInterfaceResource;

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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            generator.setParameter(AppOutputConstants.INTERFACE_ID, ids.get(AppOutputConstants.INTERFACE_ID));
            generator.setParameter(AppOutputConstants.OUTPUT_KEY, ids.get(AppOutputConstants.OUTPUT_KEY));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            generator.setParameter(AppOutputConstants.INTERFACE_ID, ids.get(AppOutputConstants.INTERFACE_ID));
            generator.setParameter(AppOutputConstants.OUTPUT_KEY, ids.get(AppOutputConstants.OUTPUT_KEY));
            Query q = generator.selectQuery(em);
            ApplicationOutput applicationOutput = (ApplicationOutput) q.getSingleResult();
            ApplicationOutputResource applicationOutputResource =
                    (ApplicationOutputResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_OUTPUT
                            , applicationOutput);
            em.getTransaction().commit();
            em.close();
            return applicationOutputResource;
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
        List<Resource> appInputResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            List results;
            if (fieldName.equals(AppOutputConstants.INTERFACE_ID)) {
                generator.setParameter(AppOutputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationOutput applicationOutput = (ApplicationOutput) result;
                        ApplicationOutputResource applicationOutputResource =
                                (ApplicationOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_OUTPUT, applicationOutput);
                        appInputResources.add(applicationOutputResource);
                    }
                }
            } else if (fieldName.equals(AppOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(AppOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationOutput applicationOutput = (ApplicationOutput) result;
                        ApplicationOutputResource applicationOutputResource =
                                (ApplicationOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_OUTPUT, applicationOutput);
                        appInputResources.add(applicationOutputResource);
                    }
                }
            } else if (fieldName.equals(AppOutputConstants.DATA_TYPE)) {
                generator.setParameter(AppOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationOutput applicationOutput = (ApplicationOutput) result;
                        ApplicationOutputResource applicationOutputResource =
                                (ApplicationOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_OUTPUT, applicationOutput);
                        appInputResources.add(applicationOutputResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for App Output Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for App Output Resource.");
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
        return appInputResources;
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
        List<String> appOutputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            List results;
            if (fieldName.equals(AppOutputConstants.INTERFACE_ID)) {
                generator.setParameter(AppOutputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationOutput applicationOutput = (ApplicationOutput) result;
                        appOutputResourceIDs.add(applicationOutput.getInterfaceID());
                    }
                }
            }
            if (fieldName.equals(AppOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(AppOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationOutput applicationOutput = (ApplicationOutput) result;
                        appOutputResourceIDs.add(applicationOutput.getInterfaceID());
                    }
                }
            } else if (fieldName.equals(AppOutputConstants.DATA_TYPE)) {
                generator.setParameter(AppOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationOutput applicationOutput = (ApplicationOutput) result;
                        appOutputResourceIDs.add(applicationOutput.getInterfaceID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for App Output resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for App Output Resource.");
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
        return appOutputResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationOutput existingApplicationOutput = em.find(ApplicationOutput.class,
                    new AppOutput_PK(interfaceID, outputKey));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingApplicationOutput != null) {
                existingApplicationOutput.setInterfaceID(interfaceID);
                ApplicationInterface applicationInterface = em.find(ApplicationInterface.class, interfaceID);
                existingApplicationOutput.setApplicationInterface(applicationInterface);
                existingApplicationOutput.setDataType(dataType);
                existingApplicationOutput.setOutputKey(outputKey);
                existingApplicationOutput.setOutputVal(outputVal);
                em.merge(existingApplicationOutput);
            } else {
                ApplicationOutput applicationOutput = new ApplicationOutput();
                applicationOutput.setInterfaceID(interfaceID);
                ApplicationInterface applicationInterface = em.find(ApplicationInterface.class, interfaceID);
                applicationOutput.setApplicationInterface(applicationInterface);
                applicationOutput.setDataType(dataType);
                applicationOutput.setOutputKey(outputKey);
                applicationOutput.setOutputVal(outputVal);
                em.persist(applicationOutput);
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
            ApplicationOutput applicationOutput = em.find(ApplicationOutput.class, new AppOutput_PK(
                    ids.get(AppOutputConstants.INTERFACE_ID),
                    ids.get(AppOutputConstants.OUTPUT_KEY)));

            em.close();
            return applicationOutput != null;
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

    public String getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(String interfaceID) {
        this.interfaceID = interfaceID;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getOutputVal() {
        return outputVal;
    }

    public void setOutputVal(String outputVal) {
        this.outputVal = outputVal;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public AppInterfaceResource getAppInterfaceResource() {
        return appInterfaceResource;
    }

    public void setAppInterfaceResource(AppInterfaceResource appInterfaceResource) {
        this.appInterfaceResource = appInterfaceResource;
    }
}
