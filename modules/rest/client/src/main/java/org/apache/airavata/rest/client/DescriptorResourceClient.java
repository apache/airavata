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
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.rest.mappings.resourcemappings.*;
import org.apache.airavata.rest.mappings.utils.DescriptorUtil;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.rest.utils.BasicAuthHeaderUtil;
import org.apache.airavata.rest.utils.ClientConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptorResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(DescriptorResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;
    private String gateway;

    public DescriptorResourceClient(String userName, String gateway, String serviceURI, PasswordCallback callback) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = serviceURI;
        this.gateway = gateway;
    }

    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private WebResource getDescriptorRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.DecResourcePathConstants.DESC_RESOURCE_PATH);
        return webResource;
    }

    public boolean isHostDescriptorExists(String hostDescriptorName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.HOST_DESC_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("hostDescriptorName", hostDescriptorName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }

            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
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


    public void addHostDescriptor(HostDescription hostDescription) {
        HostDescriptor hostDescriptor = DescriptorUtil.createHostDescriptor(hostDescription);
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.HOST_DESC_SAVE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, hostDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, hostDescriptor);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateHostDescriptor(HostDescription hostDescription) {
        HostDescriptor hostDescriptor = DescriptorUtil.createHostDescriptor(hostDescription);
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.HOST_DESC_UPDATE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, hostDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, hostDescriptor);

            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public HostDescription getHostDescriptor(String hostName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.HOST_DESC);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("hostName", hostName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).type(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).type(
                    MediaType.APPLICATION_JSON).get(ClientResponse.class);

            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_BAD_REQUEST) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else if (status == ClientConstant.HTTP_BAD_REQUEST) {
                return null;
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_BAD_REQUEST) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        HostDescriptor hostDescriptor = response.getEntity(HostDescriptor.class);
        HostDescription hostDescription = DescriptorUtil.createHostDescription(hostDescriptor);
        return hostDescription;
    }

    public void removeHostDescriptor(String hostName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.HOST_DESC_DELETE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("hostName", hostName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.delete(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<HostDescription> getHostDescriptors() {
        List<HostDescription> hostDescriptions = new ArrayList<HostDescription>();
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.GET_HOST_DESCS);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return hostDescriptions;
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return hostDescriptions;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        HostDescriptionList hostDescriptionList = response.getEntity(HostDescriptionList.class);
        HostDescriptor[] hostDescriptors = hostDescriptionList.getHostDescriptions();

        for (HostDescriptor hostDescriptor : hostDescriptors) {
            HostDescription hostDescription = DescriptorUtil.createHostDescription(hostDescriptor);
            hostDescriptions.add(hostDescription);
        }

        return hostDescriptions;
    }

    public List<String> getHostDescriptorNames() {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.GET_HOST_DESCS_NAMES);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return new ArrayList<String>();
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return new ArrayList<String>();
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        DescriptorNameList descriptorNameList = response.getEntity(DescriptorNameList.class);
        return descriptorNameList.getDescriptorNames();
    }

    public boolean isServiceDescriptorExists(String serviceDescriptorName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceDescriptorName", serviceDescriptorName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
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

    public void addServiceDescriptor(ServiceDescription serviceDescription) {
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_SAVE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, serviceDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, serviceDescriptor);

            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateServiceDescriptor(ServiceDescription serviceDescription) {
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_UPDATE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, serviceDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, serviceDescriptor);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public ServiceDescription getServiceDescriptor(String serviceName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_BAD_REQUEST) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else if (status == ClientConstant.HTTP_BAD_REQUEST) {
                return null;
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_BAD_REQUEST) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ServiceDescriptor serviceDescriptor = response.getEntity(ServiceDescriptor.class);
        ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(serviceDescriptor);
        return serviceDescription;
    }

    public void removeServiceDescriptor(String serviceName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_DELETE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.delete(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<ServiceDescription> getServiceDescriptors() {
        List<ServiceDescription> serviceDescriptions = new ArrayList<ServiceDescription>();
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.GET_SERVICE_DESCS);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return serviceDescriptions;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return serviceDescriptions;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ServiceDescriptionList serviceDescriptionList = response.getEntity(ServiceDescriptionList.class);
        ServiceDescriptor[] serviceDescriptors = serviceDescriptionList.getServiceDescriptions();

        for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
            ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(serviceDescriptor);
            serviceDescriptions.add(serviceDescription);
        }
        return serviceDescriptions;
    }

    public boolean isApplicationDescriptorExists(String serviceName,
                                                 String hostName,
                                                 String appDescriptorName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APPL_DESC_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostName);
        queryParams.add("appDescName", appDescriptorName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
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

    public void addApplicationDescriptor(ServiceDescription serviceDescription,
                                         HostDescription hostDescriptor,
                                         ApplicationDeploymentDescription descriptor) {
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostDescriptor.getType().getHostName());
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_BUILD_SAVE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addApplicationDescriptor(String serviceName,
                                         String hostName,
                                         ApplicationDeploymentDescription descriptor) {
        ServiceDescription serviceDescription = getServiceDescriptor(serviceName);
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostName);
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_BUILD_SAVE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription,
                                            HostDescription hostDescriptor,
                                            ApplicationDeploymentDescription descriptor) {
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostDescriptor.getType().getHostName());
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_UPDATE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateApplicationDescriptor(String serviceName,
                                            String hostName,
                                            ApplicationDeploymentDescription descriptor) {
        ServiceDescription serviceDescription = getServiceDescriptor(serviceName);
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostName);
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_UPDATE);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public ApplicationDeploymentDescription getApplicationDescriptor(String serviceName,
                                                                     String hostname,
                                                                     String applicationName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_DESCRIPTION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostname);
        queryParams.add("applicationName", applicationName);

        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_BAD_REQUEST) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else if (status == ClientConstant.HTTP_BAD_REQUEST) {
                return null;
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_BAD_REQUEST) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptor applicationDescriptor = response.getEntity(ApplicationDescriptor.class);
        ApplicationDeploymentDescription applicationDeploymentDescription =
                DescriptorUtil.createApplicationDescription(applicationDescriptor);
        return applicationDeploymentDescription;
    }

    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName,
                                                                      String hostname) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_PER_HOST_SERVICE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostname);

        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);
        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptor applicationDescriptor = response.getEntity(ApplicationDescriptor.class);
        ApplicationDeploymentDescription applicationDeploymentDescription =
                DescriptorUtil.createApplicationDescription(applicationDescriptor);
        return applicationDeploymentDescription;
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_ALL_DESCS_SERVICE);
        Map<String, ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = new HashMap<String, ApplicationDeploymentDescription>();
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return applicationDeploymentDescriptionMap;
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return applicationDeploymentDescriptionMap;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptorList applicationDescriptorList =
                response.getEntity(ApplicationDescriptorList.class);
        ApplicationDescriptor[] applicationDescriptors =
                applicationDescriptorList.getApplicationDescriptors();

        for (ApplicationDescriptor applicationDescriptor : applicationDescriptors) {
            ApplicationDeploymentDescription applicationDeploymentDescription =
                    DescriptorUtil.createApplicationDescription(applicationDescriptor);
            applicationDeploymentDescriptionMap.put(
                    applicationDescriptor.getHostdescName(), applicationDeploymentDescription);
        }
        return applicationDeploymentDescriptionMap;
    }

    public Map<String[], ApplicationDeploymentDescription> getApplicationDescriptors() {
        Map<String[], ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = new HashMap<String[], ApplicationDeploymentDescription>();
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_ALL_DESCRIPTORS);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptorList applicationDescriptorList =
                response.getEntity(ApplicationDescriptorList.class);
        ApplicationDescriptor[] applicationDescriptors =
                applicationDescriptorList.getApplicationDescriptors();
        for (ApplicationDescriptor applicationDescriptor : applicationDescriptors) {
            ApplicationDeploymentDescription applicationDeploymentDescription =
                    DescriptorUtil.createApplicationDescription(applicationDescriptor);
            String[] descriptors =
                    {applicationDescriptor.getServiceDescriptor().getServiceName(),
                            applicationDescriptor.getHostdescName()};
            applicationDeploymentDescriptionMap.put(descriptors, applicationDeploymentDescription);
        }
        return applicationDeploymentDescriptionMap;
    }

    public List<String> getApplicationDescriptorNames() {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_NAMES);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return new ArrayList<String>();
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return new ArrayList<String>();
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        DescriptorNameList descriptorNameList = response.getEntity(DescriptorNameList.class);
        return descriptorNameList.getDescriptorNames();
    }

    public void removeApplicationDescriptor(String serviceName,
                                            String hostName,
                                            String applicationName) {
        webResource = getDescriptorRegistryBaseResource().path(
                ResourcePathConstants.DecResourcePathConstants.APP_DESC_DELETE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostName);
        queryParams.add("appName", applicationName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).delete(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }
}
