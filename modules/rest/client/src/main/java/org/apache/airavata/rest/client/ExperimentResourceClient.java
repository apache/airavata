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
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.rest.mappings.resourcemappings.ExperimentList;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;
    private String gateway;

    public ExperimentResourceClient(String userName,
                                    String gateway,
                                    String serviceURI,
                                    PasswordCallback callback) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = serviceURI;
        this.gateway = gateway;
    }

    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private WebResource getExperimentRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.ExperimentResourcePathConstants.EXP_RESOURCE_PATH);
        return webResource;
    }

    public void addExperiment(String projectName, AiravataExperiment experiment) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(experiment.getSubmittedDate());
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.ADD_EXP);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", projectName);
        formParams.add("experimentID", experiment.getExperimentId());
        formParams.add("submittedDate", date);

        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null,cookie, gateway);
        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if(response.getCookies().size() > 0){
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
        }
    }

    public void removeExperiment(String experimentId) {
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.DELETE_EXP);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null,cookie, gateway);
        ClientResponse response = builder.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
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
                if(response.getCookies().size() > 0){
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
        }
    }

    public List<AiravataExperiment> getExperiments() {
        List<AiravataExperiment> airavataExperiments = new ArrayList<AiravataExperiment>();
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.GET_ALL_EXPS);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null,cookie, gateway);
        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK &&
                status != ClientConstant.HTTP_UNAUTHORIZED &&
                status != ClientConstant.HTTP_NO_CONTENT) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, null,cookie, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return airavataExperiments;
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if(response.getCookies().size() > 0){
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return airavataExperiments;
        }else {
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();
        for (AiravataExperiment airavataExperiment : experiments) {
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public List<AiravataExperiment> getExperiments(String projectName) {
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_BY_PROJECT);
        List<AiravataExperiment> airavataExperiments = new ArrayList<AiravataExperiment>();
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);

        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null,cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK &&
                status != ClientConstant.HTTP_UNAUTHORIZED &&
                status != ClientConstant.HTTP_NO_CONTENT) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return airavataExperiments;
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if(response.getCookies().size() > 0){
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return airavataExperiments;
        }else {
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();

        for (AiravataExperiment airavataExperiment : experiments) {
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to) {
        List<AiravataExperiment> airavataExperiments = new ArrayList<AiravataExperiment>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fromDate = dateFormat.format(from);
        String toDate = dateFormat.format(to);
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_BY_DATE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("fromDate", fromDate);
        queryParams.add("toDate", toDate);

        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null,cookie, gateway);
        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK &&
                status != ClientConstant.HTTP_UNAUTHORIZED &&
                status != ClientConstant.HTTP_NO_CONTENT) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return airavataExperiments;
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else  if (status == ClientConstant.HTTP_NO_CONTENT) {
                return airavataExperiments;
            }else {
                if(response.getCookies().size() > 0){
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();


        for (AiravataExperiment airavataExperiment : experiments) {
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) {
        List<AiravataExperiment> airavataExperiments = new ArrayList<AiravataExperiment>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fromDate = dateFormat.format(from);
        String toDate = dateFormat.format(to);
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_PER_PROJECT_BY_DATE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        queryParams.add("fromDate", fromDate);
        queryParams.add("toDate", toDate);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null,cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK &&
                status != ClientConstant.HTTP_UNAUTHORIZED &&
                status != ClientConstant.HTTP_NO_CONTENT) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();

            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return airavataExperiments;
            }

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if(response.getCookies().size() > 0){
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return airavataExperiments;
        }else {
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();


        for (AiravataExperiment airavataExperiment : experiments) {
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public boolean isExperimentExists(String experimentId) {
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.EXP_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null,cookie, gateway);
        ClientResponse response = builder.get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK &&
                status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if(response.getCookies().size() > 0){
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
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        }

    }

    public boolean isExperimentExists(String experimentId, boolean createIfNotPresent) {
        String createStatus = "false";
        webResource = getExperimentRegistryBaseResource().path(
                ResourcePathConstants.ExperimentResourcePathConstants.EXP_EXISTS_CREATE);
        if (createIfNotPresent) {
            createStatus = "true";
        }
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("createIfNotPresent", createStatus);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, callback.getPassword(userName), null, gateway);
        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, null,cookie, gateway);
            response = builder.accept(
                    MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if(response.getCookies().size() > 0){
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
            if(response.getCookies().size() > 0){
                cookie = response.getCookies().get(0).toCookie();
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        }
    }

}
