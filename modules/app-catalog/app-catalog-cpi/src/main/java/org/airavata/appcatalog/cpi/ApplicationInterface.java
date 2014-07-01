package org.airavata.appcatalog.cpi;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;

import java.util.List;
import java.util.Map;

public interface ApplicationInterface {
    /**
     * This method will add an application module
     * @param applicationModule application module
     * @return unique module id
     */
    String addApplicationModule (ApplicationModule applicationModule) throws AppCatalogException;

    /**
     * This method will add application interface description
     * @param applicationInterfaceDescription application interface
     * @return unique app interface id
     */
    String addApplicationInterface(ApplicationInterfaceDescription applicationInterfaceDescription) throws AppCatalogException;

    /**
     * This method will add an application module mapping
     * @param moduleId unique module Id
     * @param interfaceId unique interface id
     */
    void addApplicationModuleMapping (String moduleId, String interfaceId) throws AppCatalogException;

    /**
     * This method will update application module
     * @param moduleId unique module Id
     * @param updatedModule updated module
     * @throws AppCatalogException
     */
    void updateApplicationModule (String moduleId, ApplicationModule updatedModule) throws AppCatalogException;

    /**
     * This method will update application interface
     * @param interfaceId unique interface id
     * @param updatedInterface updated app interface
     * @throws AppCatalogException
     */
    void updateApplicationInterface (String interfaceId, ApplicationInterfaceDescription updatedInterface) throws AppCatalogException;

    /**
     * This method will retrieve application module by given module id
     * @param moduleId unique module Id
     * @return application module object
     */
    ApplicationModule getApplicationModule (String moduleId) throws AppCatalogException;

    /**
     * This method will retrieve application interface by given interface id
     * @param interfaceId unique interface id
     * @return application interface desc
     */
    ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException;

    /**
     * This method will return a list of application modules according to given search criteria
     * @param filters map should be provided as the field name and it's value
     * @return list of application modules
     */
    List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException;

    /**
     * This method will return a list of application interfaces according to given search criteria
     * @param filters map should be provided as the field name and it's value
     * @return list of application interfaces
     */
    List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters) throws AppCatalogException;

    /**
     * Remove application interface
     * @param interfaceId unique interface id
     */
    boolean removeApplicationInterface (String interfaceId) throws AppCatalogException;

    /**
     * Remove application module
     * @param moduleId unique module Id
     */
    boolean removeApplicationModule (String moduleId) throws AppCatalogException;

    /**
     * Check whether application interface exists
     * @param interfaceId unique interface id
     * @return true or false
     */
    boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException;

    /**
     * Check whether application module exists
     * @param moduleId unique module Id
     * @return true or false
     */
    boolean isApplicationModuleExists(String moduleId) throws AppCatalogException;

    /**
     * This method will retrieve application inputs for given application interface
     * @param interfaceId application interface id
     * @return list of inputs
     * @throws AppCatalogException
     */
    List<InputDataObjectType> getApplicationInputs(String interfaceId) throws AppCatalogException;

    /**
     * This method will retrieve application outputs for given application interface
     * @param interfaceId application interface id
     * @return list of output
     * @throws AppCatalogException
     */
    List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException;

}
