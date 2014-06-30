package org.airavata.appcatalog.cpi;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;

import java.util.List;
import java.util.Map;

public interface ApplicationDeployment {
    /**
     * Add application deployment
     * @param deploymentDescription application deployment
     * @return unique id for application deployment
     */
    String addApplicationDeployment (ApplicationDeploymentDescription deploymentDescription) throws AppCatalogException;

    /**
     * This method will retrive application deployement
     * @param deploymentId unique deployment id
     * @return application deployment
     */
    String getApplicationDeployement (String deploymentId) throws AppCatalogException;

    /**
     * This method will return a list of application deployments according to given search criteria
     * @param filters map should be provided as the field name and it's value
     * @return list of application deployments
     */
    List<ApplicationDeploymentDescription> getApplicationDeployements (Map<String, String> filters) throws AppCatalogException;

    /**
     * Check whether application deployment exists
     * @param deploymentId unique deployment id
     * @return true or false
     */
    boolean isAppDeploymentExists (String deploymentId) throws AppCatalogException;

    /**
     * Remove application deployment
     * @param deploymentId unique deployment id
     */
    void removeAppDeployment (String deploymentId) throws AppCatalogException;
}
