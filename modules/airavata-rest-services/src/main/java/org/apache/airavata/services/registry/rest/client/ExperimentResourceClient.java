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

package org.apache.airavata.services.registry.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.services.registry.rest.resourcemappings.ExperimentList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentResourceClient.class);

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private WebResource getExperimentRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.ExperimentResourcePathConstants.EXP_RESOURCE_PATH);
        return webResource;
    }

    public void addExperiment(String projectName, AiravataExperiment experiment){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.ADD_EXP);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", projectName);
        formParams.add("experimentID", experiment.getExperimentId());
        formParams.add("submittedDate", experiment.getSubmittedDate().toString());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void removeExperiment(String experimentId){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.DELETE_EXP);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<AiravataExperiment> getExperiments(){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.GET_ALL_EXPS);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();
        List<AiravataExperiment>  airavataExperiments = new ArrayList<AiravataExperiment>();
        for (AiravataExperiment airavataExperiment : experiments){
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public List<AiravataExperiment> getExperiments(String projectName){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_BY_PROJECT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);

        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();

        List<AiravataExperiment>  airavataExperiments = new ArrayList<AiravataExperiment>();
        for (AiravataExperiment airavataExperiment : experiments){
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_BY_DATE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("fromDate", from.toString());
        queryParams.add("toDate", to.toString());

        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();

        List<AiravataExperiment>  airavataExperiments = new ArrayList<AiravataExperiment>();
        for (AiravataExperiment airavataExperiment : experiments){
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_PER_PROJECT_BY_DATE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        queryParams.add("fromDate", from.toString());
        queryParams.add("toDate", to.toString());

        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentList experimentList = response.getEntity(ExperimentList.class);
        AiravataExperiment[] experiments = experimentList.getExperiments();

        List<AiravataExperiment>  airavataExperiments = new ArrayList<AiravataExperiment>();
        for (AiravataExperiment airavataExperiment : experiments){
            airavataExperiments.add(airavataExperiment);
        }
        return airavataExperiments;
    }

    public boolean isExperimentExists(String experimentId){
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.EXP_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }else {
            return true;
        }
    }

    public boolean isExperimentExists(String experimentId, boolean createIfNotPresent){
        String createStatus = "false";
        webResource = getExperimentRegistryBaseResource().path(ResourcePathConstants.ExperimentResourcePathConstants.EXP_EXISTS_CREATE);
        if (createIfNotPresent){
            createStatus = "true";
        }
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId );
        formParams.add("createIfNotPresent", createStatus );

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }else {
            return true;
        }
    }

}
