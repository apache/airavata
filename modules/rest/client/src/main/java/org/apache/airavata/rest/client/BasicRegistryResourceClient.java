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
import org.apache.airavata.rest.utils.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This is the client class for all the basic registry operations. This class is being
 * instantiated by <code>AiravataClient</code>. Users of Airavata can also call this
 * client class if he wants to use REST service
 */
public class BasicRegistryResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(BasicRegistryResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;
    private String gatewayName;
//    private CookieManager cookieManager = new CookieManager();

    /**
     * Creates a BasicRegistryResourceClient
     *
     * @param userName  registry user
     * @param seriveURI REST service URL
     * @param callback  implementation of the <code>PasswordCallback</code>
     */
    public BasicRegistryResourceClient(String userName,
                                       String gateway,
                                       String seriveURI,
                                       PasswordCallback callback,
                                       Cookie cookie) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = seriveURI;
        this.gatewayName = gateway;
        this.cookie = cookie;
    }

    /**
     * Get base URL of the REST service
     *
     * @return REST url
     */
    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    /**
     * Creating the web resource for base url
     *
     * @return web resource related to the base url
     */
    private WebResource getBasicRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.BasicRegistryConstants.REGISTRY_API_BASICREGISTRY);
        return webResource;
    }

    /**
     * Get gateway related to the instance
     *
     * @return gateway
     */
    public Gateway getGateway() {
        webResource = getBasicRegistryBaseResource().path(
                ResourcePathConstants.BasicRegistryConstants.GET_GATEWAY);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gatewayName);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gatewayName);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

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
        return response.getEntity(Gateway.class);
    }

    /**
     * Get executing user for the instance
     *
     * @return airavata user
     */
    public AiravataUser getUser() {
        webResource = getBasicRegistryBaseResource().path(
                ResourcePathConstants.BasicRegistryConstants.GET_USER);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gatewayName);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gatewayName);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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
        return response.getEntity(AiravataUser.class);
    }

    /**
     * setting the gateway
     *
     * @param gateway gateway
     */
    public void setGateway(Gateway gateway) {
        webResource = getBasicRegistryBaseResource().path(
                ResourcePathConstants.BasicRegistryConstants.SET_GATEWAY);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gatewayName);

        ClientResponse response = builder.accept(MediaType.TEXT_PLAIN).type(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, gateway);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gatewayName);
            response = builder.accept(MediaType.TEXT_PLAIN).type(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, gateway);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else if (status == ClientConstant.HTTP_OK) {
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
    }

    /**
     * Setting the airavata user
     *
     * @param user airavata user
     */
    public void setUser(AiravataUser user) {
        webResource = getBasicRegistryBaseResource().path(
                ResourcePathConstants.BasicRegistryConstants.SET_USER);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gatewayName);

        ClientResponse response = builder.accept(MediaType.TEXT_PLAIN).type(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gatewayName);
            response = builder.accept(MediaType.TEXT_PLAIN).type(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
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
    }

    /**
     * Get service API version
     *
     * @return API version
     */
    public Version getVersion() {
        webResource = getBasicRegistryBaseResource().path(
                ResourcePathConstants.BasicRegistryConstants.VERSION);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gatewayName);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gatewayName);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

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
        return response.getEntity(Version.class);
    }

    /**
     * Get registry REST service connection URL
     *
     * @return service URL
     */
    public URI getConnectionURI() {
        try {
            webResource = getBasicRegistryBaseResource().path(
                    ResourcePathConstants.BasicRegistryConstants.GET_SERVICE_URL);
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, null, cookie, gatewayName);

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
                        webResource, null, userName, callback.getPassword(userName), null, gatewayName);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

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

            String uri = response.getEntity(String.class);
            return new URI(uri);
        } catch (URISyntaxException e) {
            logger.error("URI syntax is not correct...");
        }
        return null;
    }

    /**
     * set service connection URL
     *
     * @param connectionURI service connection URL
     */
    public void setConnectionURI(URI connectionURI) {
        webResource = getBasicRegistryBaseResource().path(
                ResourcePathConstants.BasicRegistryConstants.SET_SERVICE_URL);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("connectionurl", connectionURI.toString());
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gatewayName);

        ClientResponse response = builder.type(
                MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
                CookieManager.setCookie(cookie);
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gatewayName);
            response = builder.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);

            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
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
    }
}
