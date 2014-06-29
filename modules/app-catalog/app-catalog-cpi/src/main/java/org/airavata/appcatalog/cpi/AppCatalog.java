package org.airavata.appcatalog.cpi;

public interface AppCatalog {
    ComputeResource getComputeResource();
    ApplicationInterface getApplicationInterface();
    ApplicationDeployment getApplicationDeployment();
}
