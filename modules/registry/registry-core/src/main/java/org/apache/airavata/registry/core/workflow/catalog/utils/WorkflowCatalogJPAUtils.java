/**
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
 */
package org.apache.airavata.registry.core.workflow.catalog.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.workflow.catalog.model.*;
import org.apache.airavata.registry.core.workflow.catalog.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class WorkflowCatalogJPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowCatalogJPAUtils.class);
    private static final String PERSISTENCE_UNIT_NAME = "workflowcatalog_data";
    private static final String WFCATALOG_JDBC_DRIVER = "wfcatalog.jdbc.driver";
    private static final String WFCATALOG_JDBC_URL = "wfcatalog.jdbc.url";
    private static final String WFCATALOG_JDBC_USER = "wfcatalog.jdbc.user";
    private static final String WFCATALOG_JDBC_PASSWORD = "wfcatalog.jdbc.password";
    private static final String WFCATALOG_VALIDATION_QUERY = "wfcatalog.validationQuery";
    private static final String JPA_CACHE_SIZE = "jpa.cache.size";
    private static final String JPA_CACHE_ENABLED = "cache.enable";
    @PersistenceUnit(unitName="workflowcatalog_data")
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName="worlkflowcatalog_data")
    private static EntityManager wfCatEntityManager;

    public static EntityManager getEntityManager() throws ApplicationSettingsException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(WFCATALOG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(WFCATALOG_JDBC_URL) + "?autoReconnect=true," +
                    "Username=" + readServerProperties(WFCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(WFCATALOG_JDBC_PASSWORD) +
                    ",validationQuery=" + readServerProperties(WFCATALOG_VALIDATION_QUERY);
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            // For app catalog, we don't need caching
//            properties.put("openjpa.DataCache","" + readServerProperties(JPA_CACHE_ENABLED) + "(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE)) + ", SoftReferenceSize=0)");
//            properties.put("openjpa.QueryCache","" + readServerProperties(JPA_CACHE_ENABLED) + "(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE)) + ", SoftReferenceSize=0)");
            properties.put("openjpa.RemoteCommitProvider","sjvm");
            properties.put("openjpa.Log","DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        wfCatEntityManager = factory.createEntityManager();
        return wfCatEntityManager;
    }

    private static String readServerProperties (String propertyName) throws ApplicationSettingsException {
        try {
            return ServerSettings.getSetting(propertyName);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server.properties...", e);
            throw new ApplicationSettingsException("Unable to read airavata-server.properties...");
        }
    }

    /**
     *
     * @param type model type
     * @param o model type instance
     * @return corresponding resource object
     */
    public static WorkflowCatalogResource getResource(WorkflowCatalogResourceType type, Object o) {
        switch (type){
            case WORKFLOW:
                if (o instanceof Workflow) {
                    return createWorkflow((Workflow) o);
                } else {
                    logger.error("Object should be a Workflow.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow.");
                }
            case WORKFLOW_INPUT:
                if (o instanceof WorkflowInput){
                    return createWorflowInput((WorkflowInput) o);
                }else {
                    logger.error("Object should be a Workflow Input.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow Input.");
                }
            case WORKFLOW_OUTPUT:
                if (o instanceof WorkflowOutput){
                    return createWorkflowOutput((WorkflowOutput) o);
                }else {
                    logger.error("Object should be a Workflow Output.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow Output.");
                }
            case COMPONENT_STATUS:
                if (o instanceof ComponentStatus){
                    return createComponentStatus((ComponentStatus) o);
                }else {
                    logger.error("Object should be a Workflow Output.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow Output.");
                }
            case NODE:
                if (o instanceof Node){
                    return createNode((Node) o);
                }else {
                    logger.error("Object should be a Node.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Node.");
                }
            case PORT:
                if (o instanceof Port){
                    return createPort((Port) o);
                }else {
                    logger.error("Object should be a Port.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Port.");
                }
            case EDGE:
                if (o instanceof Edge){
                    return createEdge((Edge) o);
                }else {
                    logger.error("Object should be a Edge.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Edge.");
                }
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }
	
    private static WorkflowCatalogResource createWorflowInput(WorkflowInput o) {
        WorkflowInputResource resource = new WorkflowInputResource();
        if (o != null){
            resource.setWfTemplateId(o.getTemplateID());
            resource.setInputKey(o.getInputKey());
            if (o.getInputVal() != null){
                resource.setInputVal(new String(o.getInputVal()));
            }
            resource.setDataType(o.getDataType());
            resource.setMetadata(o.getMetadata());
            resource.setAppArgument(o.getAppArgument());
            resource.setInputOrder(o.getInputOrder());
            resource.setUserFriendlyDesc(o.getUserFriendlyDesc());
            resource.setStandardInput(o.isStandardInput());
            resource.setRequired(o.isRequired());
            resource.setRequiredToCMD(o.isRequiredToCMD());
            resource.setDataStaged(o.isDataStaged());
            resource.setWorkflowResource((WorkflowResource)createWorkflow(o.getWorkflow()));
        }
        return resource;
    }

    private static WorkflowCatalogResource createWorkflowOutput(WorkflowOutput o) {
        WorkflowOutputResource resource = new WorkflowOutputResource();
        if (o != null){
            resource.setWfTemplateId(o.getTemplateId());
            resource.setOutputKey(o.getOutputKey());
            if (o.getOutputVal() != null){
                resource.setOutputVal(new String(o.getOutputVal()));
            }
            resource.setDataType(o.getDataType());
            resource.setDataMovement(o.isDataMovement());
            resource.setDataNameLocation(o.getDataNameLocation());
            resource.setWorkflowResource((WorkflowResource)createWorkflow(o.getWorkflow()));
        }
        return resource;
    }

    private static ComponentStatusResource createComponentStatus(ComponentStatus o) {
        ComponentStatusResource resource = new ComponentStatusResource();
        if (o != null){
            resource.setStatusId(o.getStatusId());
            resource.setTemplateId(o.getTemplateId());
            resource.setUpdatedTime(o.getUpdateTime());
            resource.setReason(o.getReason());
            resource.setState(o.getState());
        }
        return resource;
    }

    private static WorkflowStatusResource createWorkflowStatus(WorkflowStatus o) {
        WorkflowStatusResource resource = new WorkflowStatusResource();
        if (o != null){
            resource.setStatusId(o.getStatusId());
            resource.setTemplateId(o.getTemplateId());
            resource.setReason(o.getReason());
            resource.setState(o.getState());
            resource.setUpdatedTime(o.getUpdateTime());
        }
        return resource;
    }

    private static EdgeResource createEdge(Edge o) {
        EdgeResource resource = new EdgeResource();
        if (o != null){
            resource.setStatusId(o.getComponentStatusId());
            resource.setTemplateId(o.getTemplateId());
            resource.setEdgeId(o.getEdgeId());
            resource.setDescription(o.getDescription());
            resource.setName(o.getName());
            resource.setCreatedTime(o.getCreatedTime());
        }
        return resource;
    }

    private static PortResource createPort(Port o) {
        PortResource resource = new PortResource();
        if (o != null){
            resource.setStatusId(o.getComponentStatusId());
            resource.setTemplateId(o.getTemplateId());
            resource.setPortId(o.getPortId());
            resource.setDescription(o.getDescription());
            resource.setName(o.getName());
            resource.setCreatedTime(o.getCreatedTime());
        }
        return resource;
    }

    private static NodeResource createNode(Node o) {
        NodeResource resource = new NodeResource();
        if (o != null){
            resource.setStatusId(o.getComponentStatusId());
            resource.setTemplateId(o.getTemplateId());
            resource.setNodeId(o.getNodeId());
            resource.setDescription(o.getDescription());
            resource.setName(o.getName());
            resource.setCreatedTime(o.getCreatedTime());
            resource.setApplicationId(o.getApplicationId());
            resource.setApplicationName(o.getApplicationName());
        }
        return resource;
    }

    private static WorkflowCatalogResource createWorkflow(Workflow o) {
        WorkflowResource workflowResource = new WorkflowResource();
        workflowResource.setWfName(o.getWorkflowName());
        workflowResource.setCreatedUser(o.getCreatedUser());
        if (o.getGraph() != null){
            workflowResource.setGraph(new String(o.getGraph()));
        }
        if (o.getImage() != null){
            workflowResource.setImage(new String(o.getImage()));
        }
        workflowResource.setCreatedTime(o.getCreationTime());
        if (o.getUpdateTime() != null){
            workflowResource.setUpdatedTime(o.getUpdateTime());
        }
        workflowResource.setWfTemplateId(o.getTemplateId());
        workflowResource.setGatewayId(o.getGatewayId());
        return workflowResource;
    }
}
