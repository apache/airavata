package org.apache.airavata.registry.core.utils;

public class DBConstants {

    public static int SELECT_MAX_ROWS = 1000;

    public static class ApplicationDeployment {
        public static final String APPLICATION_DEPLOYMENT_ID = "appDeploymentId";
        public static final String APPLICATION_MODULE_ID = "appModuleId";
        public static final String COMPUTE_HOST_ID = "computeHostId";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ApplicationModule {
        public static final String APPLICATION_MODULE_ID = "appModuleId";
        public static final String APPLICATION_MODULE_NAME = "appModuleName";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ApplicationInterface {
        public static final String APPLICATION_INTERFACE_ID = "applicationInterfaceId";
        public static final String APPLICATION_NAME = "applicationName";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ApplicationInputs {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
        public static final String INPUT_KEY = "inputKey";
    }

    public static class ApplicationOutputs {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
        public static final String OUTPUT_KEY = "outputKey";
    }

    public static class AppModuleMapping {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
        public static final String APPLICATION_MODULE_ID = "moduleId";
    }

    public static class ComputeResourcePreference {
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class StorageResourcePreference {
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ComputeResource {
        public static final String HOST_NAME = "hostName";
        public static final String COMPUTE_RESOURCE_ID = "computeResourceId";
    }

    public static class ResourceJobManager {
        public static final String RESOURCE_JOB_MANAGER_ID = "resourceJobManagerId";
    }

}
