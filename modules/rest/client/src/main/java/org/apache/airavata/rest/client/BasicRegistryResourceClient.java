/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.rest.utils.BasicAuthHeaderUtil;
import org.apache.airavata.rest.utils.ClientConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

public class BasicRegistryResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(BasicRegistryResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;

    public BasicRegistryResourceClient(String userName, String seriveURI, PasswordCallback callback) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = seriveURI;
    }

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private WebResource getBasicRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.BasicRegistryConstants.REGISTRY_API_BASICREGISTRY);
        return webResource;
    }

    public Gateway getGateway (){
        webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.GET_GATEWAY);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }else if (status == ClientConstant.HTTP_UNAUTHORIZED){
            webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
            response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK ) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }


        Gateway gateway = response.getEntity(Gateway.class);
        return gateway;
    }

    public AiravataUser getUser (){
        webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.GET_USER);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED){
            webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
            response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK ) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        AiravataUser airavataUser = response.getEntity(AiravataUser.class);
        return airavataUser;
    }

    public void setGateway (Gateway gateway){
        webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.SET_GATEWAY);
        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, gateway);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED){
            webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
            response = webResource.accept(MediaType.TEXT_PLAIN).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, gateway);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public void setUser (AiravataUser user){
        webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.SET_USER);
        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED){
            webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
            response = webResource.accept(MediaType.TEXT_PLAIN).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public Version getVersion() {
        webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.VERSION);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED){
            webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
            response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK ) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        Version airavataVersion = response.getEntity(Version.class);
        return airavataVersion;
    }

    public URI getConnectionURI(){
        try {
            webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.GET_SERVICE_URL);
            ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            int status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
                webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
                response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
                status = response.getStatus();

                if (status != ClientConstant.HTTP_OK) {
                    logger.error(response.getEntity(String.class));
                    throw new RuntimeException("Failed : HTTP error code : "
                            + status);
                }
            }
            String uri = response.getEntity(String.class);
            return new URI(uri);
        } catch (URISyntaxException e) {
            logger.error("URI syntax is not correct...");
        }
        return null;
    }

    public void setConnectionURI(URI connectionURI){
        webResource = getBasicRegistryBaseResource().path(ResourcePathConstants.BasicRegistryConstants.SET_SERVICE_URL);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("connectionurl", connectionURI.toString());
        ClientResponse response = webResource.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED){
            webResource.header("Authorization", BasicAuthHeaderUtil.getBasicAuthHeader(userName, callback.getPassword(userName)));
            response = webResource.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }
}
