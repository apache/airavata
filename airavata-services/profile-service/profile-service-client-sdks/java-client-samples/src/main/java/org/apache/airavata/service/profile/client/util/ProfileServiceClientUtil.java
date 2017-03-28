package org.apache.airavata.service.profile.client.util;

import java.net.URL;
import java.util.Properties;

/**
 * Created by goshenoy on 3/23/17.
 */
public class ProfileServiceClientUtil {

    private static final String PROFILE_SERVICE_SERVER_HOST = "profile.service.server.host";
    private static final String PROFILE_SERVICE_SERVER_PORT = "profile.service.server.port";
    private static final String PROFILE_CLIENT_SAMPLE_PROPERTIES = "profile-client-sample.properties";

    private static Properties properties;
    private static Exception propertyLoadException;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        URL url = ProfileServiceClientUtil.class.getClassLoader().getResource(PROFILE_CLIENT_SAMPLE_PROPERTIES);
        try {
            properties = new Properties();
            properties.load(url.openStream());
        } catch (Exception ex) {
            propertyLoadException = ex;
        }
    }

    public static String  getProfileServiceServerHost() throws Exception {
        validateSuccessfullPropertyLoad();
        return properties.getProperty(PROFILE_SERVICE_SERVER_HOST);
    }

    public static int getProfileServiceServerPort() throws Exception {
        validateSuccessfullPropertyLoad();
        return Integer.parseInt(properties.getProperty(PROFILE_SERVICE_SERVER_PORT));
    }

    private static void validateSuccessfullPropertyLoad() throws Exception {
        if (propertyLoadException != null) {
            throw new Exception(propertyLoadException.getMessage(), propertyLoadException);
        }
    }
}
