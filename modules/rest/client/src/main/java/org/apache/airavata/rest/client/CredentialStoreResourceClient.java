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

import java.net.URI;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.rest.utils.BasicAuthHeaderUtil;
import org.apache.airavata.rest.utils.ClientConstant;
import org.apache.airavata.rest.utils.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CredentialStoreResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(CredentialStoreResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;
    private String gateway;

    public CredentialStoreResourceClient(String userName,
                                    String gateway,
                                    String serviceURL,
                                    PasswordCallback callback,
                                    Cookie cookie) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = serviceURL;
        this.gateway = gateway;
        this.cookie = cookie;
    }

    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private WebResource getCredentialStoreRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.CredentialResourceConstants.REGISTRY_API_CREDENTIALREGISTRY);
        return webResource;
    }

    public boolean isCredentialExist(String gatewayId, String tokenId) {
    	webResource = getCredentialStoreRegistryBaseResource().path(
                ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL_EXIST);
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("gatewayId", gatewayId);
        queryParams.add("tokenId", tokenId);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);
        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
            String exists = response.getEntity(String.class);
            if (exists.equalsIgnoreCase("true")) {
                return true;
            } else {
                return false;
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                    CookieManager.setCookie(cookie);
                }
            }
            String exists = response.getEntity(String.class);
            if (exists.equalsIgnoreCase("true")) {
                return true;
            } else {
                return false;
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public String getCredentialPublicKey(String gatewayId, String tokenId) {
        webResource = getCredentialStoreRegistryBaseResource().path(
                ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL);
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("gatewayId", gatewayId);
        queryParams.add("tokenId", tokenId);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);
        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return null;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                    CookieManager.setCookie(cookie);
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String publicKey = response.getEntity(String.class);
        return publicKey;
    }
    
    public String createCredential(String gatewayId, String tokenId) {
    	return this.createCredential(gatewayId, tokenId, null);
    }
    
    public String createCredential(String gatewayId, String tokenId, String username) {
    	webResource = getCredentialStoreRegistryBaseResource().path(
                ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL);
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        formParams.add("gatewayId", gatewayId);
        formParams.add("tokenId", tokenId);
        if(username!=null)
        	formParams.add("username", username);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);
        ClientResponse response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            status = response.getStatus();
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                    CookieManager.setCookie(cookie);
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        
        String publicKey = response.getEntity(String.class);
        return publicKey;
    }

}
