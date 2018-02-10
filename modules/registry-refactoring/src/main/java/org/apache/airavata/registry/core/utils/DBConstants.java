package org.apache.airavata.registry.core.utils;

public class DBConstants {

    public static int SELECT_MAX_ROWS = 1000;

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

    public static class GroupResourceProfile {
        public static final String GATEWAY_ID = "gatewayID";
        public static final String GROUP_RESOURCE_PROFILE_ID = "groupResourceProfileId";
    }

}
