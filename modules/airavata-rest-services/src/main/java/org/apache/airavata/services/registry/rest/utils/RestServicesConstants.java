package org.apache.airavata.services.registry.rest.utils;

import org.apache.commons.codec.binary.Base64;

public class RestServicesConstants {
    public static final String AIRAVATA_SERVER_PROPERTIES = "airavata-server.properties";
    public static final String GATEWAY_ID = "gateway.id";
    public static final String GATEWAY = "gateway";
    public static final String REGISTRY_USERNAME = "registry.user";
    public static final String REGISTRY_USER = "airavata.user";
    public static final String AIRAVATA_REGISTRY = "airavataRegistry";
    public static final String AIRAVATA_API = "airavataAPI";


    /**
     * A method to use by clients in the case of Basic Access authentication.
     * Creates Basic Auth header structure.
     * Reference - http://en.wikipedia.org/wiki/Basic_access_authentication
     * @param userName The user name.
     * @param password Password as credentials.
     * @return  Base64 encoded authorisation header.
     */
    public static String getBasicAuthHeader(String userName, String password) {

        String credentials = userName + ":" + password;
        String encodedString = new String(Base64.encodeBase64(credentials.getBytes()));
        return "Basic " + encodedString;
    }
}
