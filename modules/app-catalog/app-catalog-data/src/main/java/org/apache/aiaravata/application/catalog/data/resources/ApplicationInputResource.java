package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.AppInput_PK;
import org.apache.aiaravata.application.catalog.data.model.ApplicationInput;
import org.apache.aiaravata.application.catalog.data.model.ApplicationInterface;
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

public class ApplicationInputResource extends AbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationInputResource.class);

    private String interfaceID;
    private String inputKey;
    private String dataType;
    private String inputVal;
    private String metadata;
    private String appParameter;
    private String appUIDesc;

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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            generator.setParameter(AppInputConstants.INTERFACE_ID, ids.get(AppInputConstants.INTERFACE_ID));
            generator.setParameter(AppInputConstants.INPUT_KEY, ids.get(AppInputConstants.INPUT_KEY));
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
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            generator.setParameter(AppInputConstants.INTERFACE_ID, ids.get(AppInputConstants.INTERFACE_ID));
            generator.setParameter(AppInputConstants.INPUT_KEY, ids.get(AppInputConstants.INPUT_KEY));
            Query q = generator.selectQuery(em);
            ApplicationInput applicationInput = (ApplicationInput) q.getSingleResult();
            ApplicationInputResource applicationInputResource =
                    (ApplicationInputResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_INPUT
                            , applicationInput);
            em.getTransaction().commit();
            em.close();
            return applicationInputResource;
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            List results;
            if (fieldName.equals(AppInputConstants.INTERFACE_ID)) {
                generator.setParameter(AppInputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInput applicationInput = (ApplicationInput) result;
                        ApplicationInputResource applicationInputResource =
                                (ApplicationInputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_INPUT, applicationInput);
                        appInputResources.add(applicationInputResource);
                    }
                }
            } else if (fieldName.equals(AppInputConstants.INPUT_KEY)) {
                generator.setParameter(AppInputConstants.INPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInput applicationInput = (ApplicationInput) result;
                        ApplicationInputResource applicationInputResource =
                                (ApplicationInputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_INPUT, applicationInput);
                        appInputResources.add(applicationInputResource);
                    }
                }
            } else if (fieldName.equals(AppInputConstants.DATA_TYPE)) {
                generator.setParameter(AppInputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInput applicationInput = (ApplicationInput) result;
                        ApplicationInputResource applicationInputResource =
                                (ApplicationInputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_INPUT, applicationInput);
                        appInputResources.add(applicationInputResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for AppInput Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for AppInput Resource.");
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

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> appInputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            List results;
            if (fieldName.equals(AppInputConstants.INTERFACE_ID)) {
                generator.setParameter(AppInputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInput applicationInput = (ApplicationInput) result;
                        appInputResourceIDs.add(applicationInput.getInterfaceID());
                    }
                }
            } else if (fieldName.equals(AppInputConstants.INPUT_KEY)) {
                generator.setParameter(AppInputConstants.INPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInput applicationInput = (ApplicationInput) result;
                        appInputResourceIDs.add(applicationInput.getInterfaceID());
                    }
                }
            } else if (fieldName.equals(AppInputConstants.DATA_TYPE)) {
                generator.setParameter(AppInputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInput applicationInput = (ApplicationInput) result;
                        appInputResourceIDs.add(applicationInput.getInterfaceID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for AppInput resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for AppInput Resource.");
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
        return appInputResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationInput existingApplicationInput = em.find(ApplicationInput.class, new AppInput_PK(interfaceID, inputKey));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingApplicationInput != null) {
                existingApplicationInput.setInterfaceID(interfaceID);
                ApplicationInterface applicationInterface = em.find(ApplicationInterface.class, interfaceID);
                existingApplicationInput.setApplicationInterface(applicationInterface);
                existingApplicationInput.setDataType(dataType);
                existingApplicationInput.setInputKey(inputKey);
                existingApplicationInput.setInputVal(inputVal);
                existingApplicationInput.setMetadata(metadata);
                existingApplicationInput.setAppParameter(appParameter);
                existingApplicationInput.setAppUIDesc(appUIDesc);

                em.merge(existingApplicationInput);
            } else {
                ApplicationInput applicationInput = new ApplicationInput();
                applicationInput.setInterfaceID(interfaceID);
                ApplicationInterface applicationInterface = em.find(ApplicationInterface.class, interfaceID);
                applicationInput.setApplicationInterface(applicationInterface);
                applicationInput.setDataType(dataType);
                applicationInput.setInputKey(inputKey);
                applicationInput.setInputVal(inputVal);
                applicationInput.setMetadata(metadata);
                applicationInput.setAppParameter(appParameter);
                applicationInput.setAppUIDesc(appUIDesc);

                em.persist(applicationInput);
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
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationInput applicationInput = em.find(ApplicationInput.class, new AppInput_PK(
                    ids.get(AppInputConstants.INTERFACE_ID),
                    ids.get(AppInputConstants.INPUT_KEY)));

            em.close();
            return applicationInput != null;
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

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getInputVal() {
        return inputVal;
    }

    public void setInputVal(String inputVal) {
        this.inputVal = inputVal;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getAppParameter() {
        return appParameter;
    }

    public void setAppParameter(String appParameter) {
        this.appParameter = appParameter;
    }

    public String getAppUIDesc() {
        return appUIDesc;
    }

    public void setAppUIDesc(String appUIDesc) {
        this.appUIDesc = appUIDesc;
    }

    public AppInterfaceResource getAppInterfaceResource() {
        return appInterfaceResource;
    }

    public void setAppInterfaceResource(AppInterfaceResource appInterfaceResource) {
        this.appInterfaceResource = appInterfaceResource;
    }
}
