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
import org.apache.airavata.services.registry.rest.resourcemappings.ConfigurationList;
import org.apache.airavata.services.registry.rest.resourcemappings.URLList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationResourceClient.class);

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private WebResource getConfigurationBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.ConfigResourcePathConstants.CONFIGURATION_REGISTRY_RESOURCE);
        return webResource;
    }


    public Object getConfiguration(String configKey) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.GET_CONFIGURATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("key", configKey);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        String output = response.getEntity(String.class);
        return output;
    }

    public List<Object> getConfigurationList (String configKey) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.GET_CONFIGURATION_LIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("key", configKey);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        ConfigurationList configurationList = response.getEntity(ConfigurationList.class);
        List<Object> configurationValueList = new ArrayList<Object>();
        Object[] configValList = configurationList.getConfigValList();
        for(Object configVal : configValList){
            configurationValueList.add(configVal);
        }

        return configurationValueList;
    }

    public void setConfiguration (String configKey, String configVal, String date){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.SAVE_CONFIGURATION);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("key", configKey);
        formData.add("value", configVal);
        formData.add("date", date);

        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addConfiguration(String configKey, String configVal, String date){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.UPDATE_CONFIGURATION);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("key", configKey);
        formData.add("value", configVal);
        formData.add("date", date);

        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void removeAllConfiguration(String key){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_ALL_CONFIGURATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("key", key);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

    }

    public void removeConfiguration(String key, String value){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_CONFIGURATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("key", key);
        queryParams.add("value", value);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<URI> getGFacURIs(){
        List<URI> uriList = new ArrayList<URI>();
        try{
            webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.GET_GFAC_URI_LIST);
            ClientResponse response = webResource.get(ClientResponse.class);
            int status = response.getStatus();

            if (status != 200) {
                logger.error("Failed : HTTP error code : " + status);
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }

            URLList urlList = response.getEntity(URLList.class);
            String[] uris = urlList.getUris();
            for (String url: uris){
                URI uri = new URI(url);
                uriList.add(uri);
            }
        } catch (URISyntaxException e) {
            logger.error("URI syntax is not correct...");
            return null;
        }
        return uriList;
    }

    public List<URI> getWorkflowInterpreterURIs(){
        List<URI> uriList = new ArrayList<URI>();
        try{
            webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.GET_WFINTERPRETER_URI_LIST);
            ClientResponse response = webResource.get(ClientResponse.class);
            int status = response.getStatus();

            if (status != 200) {
                logger.error("Failed : HTTP error code : " + status);
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }

            URLList urlList = response.getEntity(URLList.class);
            String[] uris = urlList.getUris();
            for (String url: uris){
                URI uri = new URI(url);
                uriList.add(uri);
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return uriList;
    }


    public URI getEventingURI(){
        try{
            webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.GET_EVENTING_URI);
            ClientResponse response = webResource.get(ClientResponse.class);
            int status = response.getStatus();

            if (status != 200) {
                logger.error("Failed : HTTP error code : " + status);
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }

            String uri = response.getEntity(String.class);
            URI eventingURI = new URI(uri);
            return eventingURI;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public URI getMsgBoxURI(){
        try{
            webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.GET_MESSAGE_BOX_URI);
            ClientResponse response = webResource.get(ClientResponse.class);
            int status = response.getStatus();

            if (status != 200) {
                logger.error("Failed : HTTP error code : " + status);
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }

            String uri = response.getEntity(String.class);
            URI msgBoxURI = new URI(uri);
            return msgBoxURI;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void addGFacURI(String uri) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_GFAC_URI);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addWFInterpreterURI(String uri) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_WFINTERPRETER_URI);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void setEventingURI(String uri) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_EVENTING_URI);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void setMessageBoxURI(String uri) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_MESSAGE_BOX_URI);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addGFacURIByDate(String uri, String date) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_GFAC_URI_DATE);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);
        formData.add("date", date);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addWorkflowInterpreterURI(String uri, String date) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_WFINTERPRETER_URI_DATE);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);
        formData.add("date", date);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void setEventingURIByDate(String uri, String date) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_EVENTING_URI_DATE);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);
        formData.add("date", date);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void setMessageBoxURIByDate(String uri, String date) {
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.ADD_MSG_BOX_URI_DATE);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("uri", uri);
        formData.add("date", date);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formData);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void removeGFacURI(String uri){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_GFAC_URI);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("uri", uri);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void removeAllGFacURI(){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_ALL_GFAC_URIS);
        ClientResponse response = webResource.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void removeWorkflowInterpreterURI(String uri){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_WFINTERPRETER_URI);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("uri", uri);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void removeAllWorkflowInterpreterURI(){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_ALL_WFINTERPRETER_URIS);
        ClientResponse response = webResource.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void unsetEventingURI(){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_EVENTING_URI);
        ClientResponse response = webResource.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void unsetMessageBoxURI(){
        webResource = getConfigurationBaseResource().path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_MSG_BOX_URI);
        ClientResponse response = webResource.delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

}
