package org.airavata.appcatalog.cpi;

public interface AppCatalog {
    /**
     * Get ComputeResource interface
     * @return ComputeResource interface
     */
    ComputeResource getComputeResource() throws AppCatalogException;

    /**
     * Get application interface
     * @return application interface
     */
    ApplicationInterface getApplicationInterface() throws AppCatalogException;

    /**
     * Get application deployment interface
     * @return application deployment interface
     */
    ApplicationDeployment getApplicationDeployment() throws AppCatalogException;

    /**
     * Get Gateway profile interface
     * @return Gateway profile interface
     * @throws AppCatalogException
     */
    GwyResourceProfile getGatewayProfile() throws AppCatalogException;
}
