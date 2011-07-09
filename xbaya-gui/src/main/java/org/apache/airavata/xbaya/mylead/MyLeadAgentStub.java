/*
 * Copyright (c) 2004-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadAgentStub.java,v 1.33 2009/01/30 22:49:02 smarru Exp $
 */

package org.apache.airavata.xbaya.mylead;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.xbaya.mylead.gui.MyleadWorkflowMetadata;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.xmlbeans.XmlException;
import org.ietf.jgss.GSSCredential;

import xsul.invoker.gsi.GsiInvoker;
import xsul.invoker.puretls.PuretlsInvoker;
import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif_xsul_soap_gsi.Provider;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;
import xsul5.MLogger;
import edu.indiana.dde.metadata.catalog.domain.CatalogAggregationType;
import edu.indiana.dde.metadata.catalog.domain.ObjectResponseType;
import edu.indiana.dde.metadata.catalog.types.ContentFilterType;
import edu.indiana.dde.metadata.catalog.types.ContextQueryType;
import edu.indiana.dde.metadata.catalog.types.ContextType;
import edu.indiana.dde.metadata.catalog.types.ElementFilterType;
import edu.indiana.dde.metadata.catalog.types.ElementResponseType;
import edu.indiana.dde.metadata.catalog.types.HierarchyFilterType;
import edu.indiana.dde.metadata.catalog.types.MetadataElementDocument;
import edu.indiana.dde.metadata.catalog.types.QueryComponentType;
import edu.indiana.dde.metadata.catalog.types.QueryElementType;
import edu.indiana.dde.metadata.catalog.types.QueryObjectType;
import edu.indiana.dde.metadata.catalog.types.QueryPropertyType;
import edu.indiana.dde.metadata.catalog.types.QueryResponseSetType;
import edu.indiana.dde.metadata.catalog.types.QueryTextElementType;
import edu.indiana.dde.metadata.catalog.types.TextComparisonType;
import edu.indiana.dde.metadata.catalog.types.UserType;
import edu.indiana.dde.mylead.agent.AgentPortType;
import edu.indiana.dde.mylead.agent.xmlbeans.AccountExistenceRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.AddUsersRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.AddUsersRequestType;
import edu.indiana.dde.mylead.agent.xmlbeans.BooleanResponseDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.CollectionType;
import edu.indiana.dde.mylead.agent.xmlbeans.ContextQueryRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.ContextQueryRequestType;
import edu.indiana.dde.mylead.agent.xmlbeans.CreateCollectionsRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.CreateCollectionsRequestType;
import edu.indiana.dde.mylead.agent.xmlbeans.CreateProjectsRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.CreateTopLevelObjectsRequestType;
import edu.indiana.dde.mylead.agent.xmlbeans.OperationResponseDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.OperationResponseType;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryByIDsRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryByIDsRequestType;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryResponseDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryResponseType;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryResultConfigurationType;
import edu.indiana.dde.mylead.agent.xmlbeans.StatusEnumType;
import edu.indiana.dde.mylead.agent.xmlbeans.StatusEnumType.Enum;
import edu.indiana.dde.mylead.agent.xmlbeans.TopLevelObjectType;
import edu.indiana.extreme.lead.metadata.LEADResourceType;
import edu.indiana.extreme.lead.metadata.LEADresourceDocument;
import edu.indiana.extreme.lead.metadata.util.MinimalLEADMetadata;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadAgentStub {

    /**
     * HFILTER_TARGET
     */
    public static final String HFILTER_TARGET = "TARGET";

    /**
     * HFILTER_CHILDREN
     */
    public static final String HFILTER_CHILDREN = "CHILDREN";

    private static final MLogger logger = MLogger.getLogger();

    private static final String WORKFLOW_TEMPLATES_COLLECTION = "Workflow Templates";

    private URI url;

    private String wsdlLoc;

    private GSSCredential proxy;

    // private AgentPortType stub;

    /**
     * Creates a MyLeadAgentStub
     * 
     * @param url
     * 
     * @throws MyLeadException
     */
    public MyLeadAgentStub(URI url) throws MyLeadException {
        this(url, null);
    }

    /**
     * Creates a MyLeadAgentStub
     * 
     * @param url
     * @param proxy
     * 
     * @throws MyLeadException
     */
    public MyLeadAgentStub(URI url, GSSCredential proxy) throws MyLeadException {
        this.url = url;
        this.wsdlLoc = url.toString();
        this.proxy = proxy;
        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(WSDLUtil.appendWSDLQuary(url).toString(), null, null, null, null);

            if (SecurityUtil.isSecureService(url)) {
                PuretlsInvoker secureInvoker = new GsiInvoker(proxy, XBayaSecurity.getTrustedCertificates());
                Provider secureProvider = new Provider(secureInvoker);
                service.addLocalProvider(secureProvider);
            }

            WSIFClient client = XmlBeansWSIFRuntime.getDefault().newClientFor(service, null);

            // to make it compatible even though some unused operation is
            // removed from the WSDL.
            client.allowMethodsUnmappedToWsdlOperations(true);
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * @return The URL.
     */
    public URI getURL() {
        return this.url;
    }

    /**
     * Returns the proxy.
     * 
     * @return The proxy
     */
    public GSSCredential getProxy() {
        return this.proxy;
    }

    /**
     * @param uid
     * @return true if the specified user has an account; false otherwise.
     * @throws MyLeadException
     */
    public boolean doesUserHaveMyLeadAccount(String uid) throws MyLeadException {
        logger.entering(new Object[] { uid });
        try {
            AgentPortType stub = this.createStub(uid);
            AccountExistenceRequestDocument reqDoc = AccountExistenceRequestDocument.Factory.newInstance();
            reqDoc.addNewAccountExistenceRequest();
            BooleanResponseDocument responce = stub.doesAccountExist(reqDoc);
            if (null == responce || null == responce.getBooleanResponse()) {
                throw new MyLeadException("Invalid responce returned");
            }

            boolean result = responce.getBooleanResponse().getValue();
            logger.finest("result: " + result);
            return result;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * @param adminDN
     * @param userDN
     * @return The query result
     * @throws MyLeadException
     */
    public String addNewUser(String adminDN, String userDN) throws MyLeadException {

        try {

            AgentPortType stub = this.createStub(adminDN);
            AddUsersRequestDocument reqDoc = AddUsersRequestDocument.Factory.newInstance();
            AddUsersRequestType req = reqDoc.addNewAddUsersRequest();
            UserType params = req.addNewCatalogUser();
            params.setUserId(adminDN);
            params.setUserName(userDN);

            OperationResponseDocument responce = stub.addUsers(reqDoc);
            if (null == responce || null == responce.getOperationResponse()) {
                throw new MyLeadException("Invalid responce");
            }
            OperationResponseType outputType = responce.getOperationResponse();
            Enum status = outputType.getStatus();
            String queryResult = null;
            if (status.equals(StatusEnumType.SUCCESS)) {
                if (outputType.isSetMessage()) {
                    queryResult = outputType.getMessage();
                }
            } else {
                if (outputType.isSetMessage()) {
                    logger.finest("faultMessage: " + outputType.getMessage());
                    throw new MyLeadException(outputType.getMessage());
                }
            }

            if (status == StatusEnumType.FAILURE) {
                throw new MyLeadException("Failed adding user:" + responce.toString());
            }

            logger.finest("queryResult: " + queryResult);
            return queryResult;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * @param uid
     * @param leadResource
     * @param assignNewResourceID
     * @return The resourceID
     * @throws MyLeadException
     */
    public String createProject(String uid, String leadResource, boolean assignNewResourceID) throws MyLeadException {
        try {
            CreateProjectsRequestDocument inputMsg = CreateProjectsRequestDocument.Factory.newInstance();
            CreateTopLevelObjectsRequestType params = inputMsg.addNewCreateProjectsRequest();
            AgentPortType stub = this.createStub(uid);
            TopLevelObjectType topLevelObjectType = params.addNewObjectInfo();
            topLevelObjectType.setAssignNewResourceID(assignNewResourceID);
            LEADresourceDocument leadResourceDoc = LEADresourceDocument.Factory.parse(leadResource);
            LEADResourceType leadResourceType = leadResourceDoc.getLEADresource();
            topLevelObjectType.setLEADresource(leadResourceType);
            OperationResponseDocument outputMsg = stub.createProjects(inputMsg);
            OperationResponseType outputType = outputMsg.getOperationResponse();
            StatusEnumType.Enum statusType = outputType.getStatus();
            String[] resourceIDs = outputType.getResourceIDArray();
            if (!statusType.equals(StatusEnumType.SUCCESS)) {
                if (outputType.isSetMessage()) {
                    logger.finest("faultMessage: " + outputType.getMessage());
                    throw new MyLeadException(outputType.getMessage());
                }
            }
            String resourceID = null;
            if (resourceIDs != null && resourceIDs.length > 0) {
                resourceID = resourceIDs[0];
            }
            logger.finest("resourceID: " + resourceID);
            return resourceID;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        } catch (XmlException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * @param uid
     * @param leadResource
     * @param parentResourceID
     * @param assignNewResourceID
     * @return The resourceID
     * @throws MyLeadException
     */
    @SuppressWarnings("boxing")
    public String createCollection(String uid, LEADResourceType leadResourceType, String parentResourceID,
            boolean assignNewResourceID) throws MyLeadException {
        logger.entering(new Object[] { uid, leadResourceType, parentResourceID, assignNewResourceID });
        try {

            CreateCollectionsRequestDocument inputMsg = CreateCollectionsRequestDocument.Factory.newInstance();
            CreateCollectionsRequestType params = inputMsg.addNewCreateCollectionsRequest();

            AgentPortType stub = this.createStub(uid);
            CollectionType collectionType = params.addNewCollectionInfo();

            collectionType.setAssignNewResourceID(assignNewResourceID);
            collectionType.setParentID(parentResourceID);

            collectionType.setLEADresource(leadResourceType);

            OperationResponseDocument outputMsg = stub.createCollections(inputMsg);
            OperationResponseType outputType = outputMsg.getOperationResponse();

            StatusEnumType.Enum statusType = outputType.getStatus();

            String[] resourceIDs = outputType.getResourceIDArray();

            if (!statusType.equals(StatusEnumType.SUCCESS)) {
                if (outputType.isSetMessage()) {
                    logger.finest("faultMessage: " + outputType.getMessage());
                    throw new MyLeadException(outputType.getMessage());
                }

            }
            String resourceID = null;
            if (resourceIDs != null && resourceIDs.length > 0) {
                resourceID = resourceIDs[0];
            }

            logger.finest("resourceID: " + resourceID);
            return resourceID;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * @param userDN
     * @param hFilter
     * @param cFilter
     * @param resourceID
     * @return The query result
     * @throws MyLeadException
     */
    public QueryResponseSetType queryById(String uid, QueryByIDsRequestDocument inputMsg) throws MyLeadException {
        logger.entering(new Object[] { uid, inputMsg });
        try {

            // QueryByIDsRequestDocument inputMsg =
            // QueryByIDsRequestDocument.Factory.newInstance();
            // QueryByIDsRequestType params =
            // inputMsg.addNewQueryByIDsRequest();
            AgentPortType stub = this.createStub(uid);
            // params.addResourceID(resourceID);
            // QueryResultConfigurationType queryResultConfigurationType =
            // params.addNewQueryResultConfiguration();
            // queryResultConfigurationType.setContentFilter(MyLEADUtil.getCFilter(cFilter));
            // queryResultConfigurationType.setHierarchyFilter(MyLEADUtil.getHFilter(hFilter));
            QueryResponseDocument outputMsg = stub.queryByIDs(inputMsg);
            QueryResponseType outputType = outputMsg.getQueryResponse();
            QueryResponseSetType returnDoc = outputType.getQueryResponseSet();
            StatusEnumType.Enum statusType = outputType.getStatus();
            if (statusType.equals(StatusEnumType.SUCCESS)) {
                logger.finest("resultElement: " + returnDoc.toString());
                return returnDoc;
            } else {
                logger.finest("faultMessage: " + outputType.getMessage());
                throw new MyLeadException(outputType.getMessage());
            }
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * Lists all projects under a specified user.
     * 
     * @param uid
     * @return The projects
     * @throws MyLeadException
     */
    public ArrayList<MyLeadQueryResultItem> listProjects(String uid) throws MyLeadException {
        try {
            ArrayList<MyLeadQueryResultItem> projects = new ArrayList<MyLeadQueryResultItem>();

            ContextQueryRequestDocument cqrdoc = ContextQueryRequestDocument.Factory.newInstance();
            ContextQueryRequestType cqType = cqrdoc.addNewContextQueryRequest();

            QueryObjectType qTarget = cqType.addNewQueryTarget();
            qTarget.setAggrType(CatalogAggregationType.PROJECT);

            QueryResultConfigurationType qrConfig = cqType.addNewQueryResultConfiguration();
            qrConfig.setCount(0);
            qrConfig.setOffset(0);
            qrConfig.setHierarchyFilter(HierarchyFilterType.TARGET);

            ElementFilterType elementFilterType = qrConfig.addNewElementFilter();
            elementFilterType.setPropertyName("citation");
            elementFilterType.setPropertySource("LEAD");
            elementFilterType.setElementName("title");
            elementFilterType.setElementSource("LEAD");
            elementFilterType.setElementFilter("title");

            QueryResponseDocument outputMsg = this.createStub(uid).queryWithContext(cqrdoc);
            QueryResponseType outputType = outputMsg.getQueryResponse();

            StatusEnumType.Enum statusType = outputType.getStatus();

            if (statusType.equals(StatusEnumType.SUCCESS)) {
                if (outputType.isSetMessage()) {
                    logger.fine(outputType.getMessage());
                }
                if (outputType.isSetQueryResponseSet()) {
                    QueryResponseSetType queryResponseSetType = outputType.getQueryResponseSet();
                    List<ElementResponseType> elemRespList = queryResponseSetType.getElementResponseList();
                    for (ElementResponseType elemResp : elemRespList) {
                        List<MetadataElementDocument.MetadataElement> metadataElementList = elemResp
                                .getMetadataElementList();
                        for (MetadataElementDocument.MetadataElement metadataElement : metadataElementList) {
                            String title = metadataElement.getName();
                            String projectGuid = null;
                            String projectName = null;
                            if ("title".equals(title)) {
                                projectName = metadataElement.getStringValue();
                                projectGuid = elemResp.getObjectId();
                            }
                            if (projectGuid != null && projectName != null) {
                                MyLeadQueryResultItem myLeadItem = new MyLeadQueryResultItem();
                                myLeadItem.setResouceID(projectGuid);
                                myLeadItem.setTitle(projectName);
                                projects.add(myLeadItem);
                            }
                            break;
                        }
                    }
                }
            } else {
                if (outputType.isSetMessage()) {
                    throw new MyLeadException("When trying to get a list of " + "projects for user [" + uid
                            + "] received the " + "following fault message from MyLEAD Agent: "
                            + outputType.getMessage());
                }
                if (outputType.isSetOperationSummary()) {
                    throw new MyLeadException("When trying to get a list of " + "projects for user [" + uid
                            + "] received the " + "following fault message from MyLEAD Agent: "
                            + outputType.getOperationSummary());
                }
                if (outputType.isSetOperationSummaryStreamUrl()) {
                    throw new MyLeadException("When trying to get a list of " + "projects for user [" + uid
                            + "] received the " + "following fault message from MyLEAD Agent: "
                            + outputType.getOperationSummaryStreamUrl());
                }
            }

            return projects;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * @param uid
     * @param name
     * @return The project
     * @throws MyLeadException
     */
    public String projectNameToID(String uid, String name) throws MyLeadException {
        // XXX There must be a direct way to get the project with a specified
        // title from myLEAD.
        ArrayList<MyLeadQueryResultItem> projects = listProjects(uid);
        for (MyLeadQueryResultItem project : projects) {
            String title = project.getTitle();
            if (name.equals(title)) {
                String projectID = project.getResouceID();
                return projectID;
            }
        }
        return null;
    }

    // /**
    // * List all collections under a specified resource, which can be a project
    // * or a collection.
    // *
    // * @param uid
    // * @param resourceID
    // * @return The collections
    // * @throws MyLeadException
    // */
    // public List<MyLeadQueryResultItem> listCollections(String uid,
    // String resourceID) throws MyLeadException {
    // try {
    //
    // QueryByIDsRequestDocument inputMsg =
    // QueryByIDsRequestDocument.Factory.newInstance();
    // QueryByIDsRequestType params = inputMsg.addNewQueryByIDsRequest();
    // params.addResourceID(resourceID);
    // QueryResultConfigurationType queryResultConfigurationType =
    // params.addNewQueryResultConfiguration();
    //
    // queryResultConfigurationType.setCount(0);
    // queryResultConfigurationType.setOffset(0);
    // queryResultConfigurationType.setHierarchyFilter(HierarchyFilterType.TARGET);
    // ElementFilterType elemFilter =
    // queryResultConfigurationType.addNewElementFilter();
    // elemFilter.setElementName("title");
    // elemFilter.setElementSource("LEAD");
    // elemFilter.setPropertyName("citation");
    // elemFilter.setPropertySource("LEAD");
    // elemFilter.setElementFilter("title");
    //
    // MinimalLEADMetadata workflowTemplatesMetadata = new MinimalLEADMetadata(
    // uid, MyLead.WORKFLOW_TEMPLATES_COLLECTION,
    // "Workflow templates collection");
    // // This is neccesary.msg
    // workflowTemplatesMetadata.setResourceId("Unknown");
    // workflowTemplatesMetadata.construct();
    //
    //
    // QueryResponseSetType response = queryById(uid, inputMsg);
    // List<ElementResponseType> responseList =
    // response.getElementResponseList();
    // ArrayList<MyLeadQueryResultItem> collections = new
    // ArrayList<MyLeadQueryResultItem>();
    // for (ElementResponseType type : responseList) {
    // MyLeadQueryResultItem myLeadItem = new MyLeadQueryResultItem();
    // myLeadItem.setResouceID(type.getObjectId());
    // myLeadItem.setTitle(type.getObjectClass().toString());
    // collections.add(myLeadItem);
    // }
    // return c/**
    // * List all collections under a specified resource, which can be a project
    // * or a collection.
    // *
    // * @param uid
    // * @param resourceID
    // * @return The collections
    // * @throws MyLeadException
    // */
    // public List<MyLeadQueryResultItem> listCollections(String uid,
    // String resourceID) throws MyLeadException {
    // try {
    //
    // QueryByIDsRequestDocument inputMsg =
    // QueryByIDsRequestDocument.Factory.newInstance();
    // QueryByIDsRequestType params = inputMsg.addNewQueryByIDsRequest();
    // params.addResourceID(resourceID);
    // QueryResultConfigurationType queryResultConfigurationType =
    // params.addNewQueryResultConfiguration();
    //
    // queryResultConfigurationType.setCount(0);
    // queryResultConfigurationType.setOffset(0);
    // queryResultConfigurationType.setHierarchyFilter(HierarchyFilterType.TARGET);
    // ElementFilterType elemFilter =
    // queryResultConfigurationType.addNewElementFilter();
    // elemFilter.setElementName("title");
    // elemFilter.setElementSource("LEAD");
    // elemFilter.setPropertyName("citation");
    // elemFilter.setPropertySource("LEAD");
    // elemFilter.setElementFilter("title");
    //
    // MinimalLEADMetadata workflowTemplatesMetadata = new MinimalLEADMetadata(
    // uid, MyLead.WORKFLOW_TEMPLATES_COLLECTION,
    // "Workflow templates collection");
    // // This is neccesary.
    // workflowTemplatesMetadata.setResourceId("Unknown");
    // workflowTemplatesMetadata.construct();
    //
    //
    // QueryResponseSetType response = queryById(uid, inputMsg);
    // List<ElementResponseType> responseList =
    // response.getElementResponseList();
    // ArrayList<MyLeadQueryResultItem> collections = new
    // ArrayList<MyLeadQueryResultItem>();
    // for (ElementResponseType type : responseList) {
    // MyLeadQueryResultItem myLeadItem = new MyLeadQueryResultItem();
    // myLeadItem.setResouceID(type.getObjectId());
    // myLeadItem.setTitle(type.getObjectClass().toString());
    // collections.add(myLeadItem);
    // }
    // return collections;
    // } catch (RuntimeException e) {
    // throw new MyLeadException(e);
    // }
    // }ollections;
    // } catch (RuntimeException e) {
    // throw new MyLeadException(e);
    // }
    // }

    public List<MyleadWorkflowMetadata> getWorkflowResources(String userDN, String projectID) {
        if (userDN == null) {
            throw new IllegalArgumentException("User ID must not be null when retrieving workflow templates.");
        }
        List<String[]> userAndProjectIDsList = null;
        if (projectID == null) {
            throw new IllegalArgumentException("Project ID is null");
        } else {
            userAndProjectIDsList = new ArrayList<String[]>();
            userAndProjectIDsList.add(new String[] { userDN, projectID });
        }

        List<MyleadWorkflowMetadata> workflows = null;
        for (String[] userAndProjectId : userAndProjectIDsList) {
            String lUserID = userAndProjectId[0];
            String lProjectID = userAndProjectId[1];
            logger.finest("using userID [" + lUserID + "] and projectID [" + lProjectID + "] for sample workflows");
            workflows = new ArrayList<MyleadWorkflowMetadata>();

            try {
                ContextQueryRequestDocument query = ContextQueryRequestDocument.Factory.newInstance();
                ContextQueryRequestType params = query.addNewContextQueryRequest();
                QueryObjectType target = params.addNewQueryTarget();
                target.setAggrType(CatalogAggregationType.COLLECTION);

                ContextQueryType context = params.addNewContextQuery();
                QueryComponentType parentCollection = context.addNewQueryComponent();
                parentCollection.setAggrType(CatalogAggregationType.COLLECTION);
                parentCollection.setRelation(ContextType.DIRECT_PARENT);

                QueryPropertyType titleProperty = parentCollection.addNewQueryProperty();
                titleProperty.setName("citation");
                titleProperty.setSource("LEAD");
                QueryElementType titleElement = titleProperty.addNewQueryElement();
                titleElement.setName("title");
                titleElement.setSource("LEAD");
                QueryTextElementType textCriteria = titleElement.addNewQueryTextElement();
                textCriteria.setCompare(TextComparisonType.EXACT);
                textCriteria.setValue(WORKFLOW_TEMPLATES_COLLECTION);
                QueryComponentType project = parentCollection.addNewQueryComponent();
                project.setAggrType(CatalogAggregationType.PROJECT);
                project.setRelation(ContextType.PARENT);
                project.setObjectId(projectID);

                QueryResultConfigurationType config = params.addNewQueryResultConfiguration();
                config.setOffset(0);
                config.setCount(0);
                config.setHierarchyFilter(HierarchyFilterType.TARGET);
                ElementFilterType filterTitle = config.addNewElementFilter();
                filterTitle.setPropertyName("citation");
                filterTitle.setPropertySource("LEAD");
                filterTitle.setElementName("title");
                filterTitle.setElementSource("LEAD");
                filterTitle.setElementFilter("title");
                ElementFilterType filterDesc = config.addNewElementFilter();
                filterDesc.setPropertyName("description");
                filterDesc.setPropertySource("LEAD");
                filterDesc.setElementName("abstract");
                filterDesc.setElementSource("LEAD");
                filterDesc.setElementFilter("abstract");
                QueryResponseDocument outputMsg = createStub(userDN).queryWithContext(query);

                QueryResponseType outputType = outputMsg.getQueryResponse();

                StatusEnumType.Enum statusType = outputType.getStatus();

                if (statusType.equals(StatusEnumType.SUCCESS)) {
                    if (outputType.isSetMessage()) {
                        logger.finest(outputType.getMessage());
                    }
                    if (outputType.isSetQueryResponseSet()) {
                        QueryResponseSetType queryResponseSetType = outputType.getQueryResponseSet();
                        List<ElementResponseType> elemRespList = queryResponseSetType.getElementResponseList();
                        for (ElementResponseType elemResp : elemRespList) {
                            String wrkflwTemplateGUID = null;
                            String wrkflwName = null;
                            String wrkflwDescription = null;
                            List<MetadataElementDocument.MetadataElement> metadataElementList = elemResp
                                    .getMetadataElementList();
                            for (MetadataElementDocument.MetadataElement metadataElement : metadataElementList) {
                                String elementName = metadataElement.getName();
                                if ("title".equals(elementName)) {
                                    wrkflwName = metadataElement.getStringValue();
                                    wrkflwTemplateGUID = elemResp.getObjectId();
                                } else if ("abstract".equals(elementName)) {
                                    wrkflwDescription = metadataElement.getStringValue();
                                }
                            }
                            if (wrkflwTemplateGUID != null) {

                                workflows.add(new MyleadWorkflowMetadata(wrkflwTemplateGUID, wrkflwName,
                                        wrkflwDescription, userDN, projectID));

                            }
                        }
                    }
                } else {
                    if (outputType.isSetMessage()) {
                        throw new MyLeadException(outputType.getMessage());
                    }
                    if (outputType.isSetOperationSummary()) {
                        throw new MyLeadException(outputType.getOperationSummary().xmlText());
                    }
                    if (outputType.isSetOperationSummaryStreamUrl()) {
                        throw new MyLeadException(outputType.getOperationSummaryStreamUrl());
                    }
                }
            } catch (Exception e) {
                logger.caught(e);
            }

        } // client expects an empty array, not null
        if (null == workflows) {
            workflows = new ArrayList<MyleadWorkflowMetadata>();
        }
        return workflows;

    }

    public String getWorkflowTemplateCollectionId(String userID, String projectID) throws MyLeadException {
        String wfTempCollectionId = null;
        try {
            ContextQueryRequestDocument query = ContextQueryRequestDocument.Factory.newInstance();
            ContextQueryRequestType params = query.addNewContextQueryRequest();
            QueryObjectType target = params.addNewQueryTarget();
            target.setAggrType(CatalogAggregationType.COLLECTION);

            ContextQueryType context = params.addNewContextQuery();
            QueryComponentType parentCollection = context.addNewQueryComponent();
            parentCollection.setAggrType(CatalogAggregationType.PROJECT);
            parentCollection.setRelation(ContextType.PARENT);
            parentCollection.setObjectId(projectID);

            QueryPropertyType titleProperty = target.addNewQueryProperty();

            titleProperty.setName("citation");
            titleProperty.setSource("LEAD");
            QueryElementType titleElement = titleProperty.addNewQueryElement();
            titleElement.setName("title");
            titleElement.setSource("LEAD");
            QueryTextElementType textCriteria = titleElement.addNewQueryTextElement();
            textCriteria.setCompare(TextComparisonType.EXACT);
            textCriteria.setValue(WORKFLOW_TEMPLATES_COLLECTION);

            QueryResultConfigurationType config = params.addNewQueryResultConfiguration();
            config.setOffset(0);
            config.setCount(0);
            config.setHierarchyFilter(HierarchyFilterType.TARGET);
            ContentFilterType.Enum cFilter = ContentFilterType.ID_ONLY;
            config.setContentFilter(cFilter);
            QueryResponseDocument outputMsg = createStub(userID).queryWithContext(query);
            QueryResponseType outputType = outputMsg.getQueryResponse();

            StatusEnumType.Enum statusType = outputType.getStatus();

            if (statusType.equals(StatusEnumType.SUCCESS)) {
                if (outputType.isSetMessage()) {
                    logger.finest(outputType.getMessage());
                }
                if (outputType.isSetQueryResponseSet()) {
                    QueryResponseSetType queryResponseSetType = outputType.getQueryResponseSet();
                    if (queryResponseSetType.getGuidResponseList().size() > 0) {
                        wfTempCollectionId = queryResponseSetType.getGuidResponseList().get(0).getObjectId();
                    } else {
                        logger.finest("Workflow Templates collection not found!  Will attempt to create now.");
                        try {
                            wfTempCollectionId = createWorkflowTemplatesCollection(userID, projectID);
                        } catch (Exception e) {
                            logger.caught(e);
                            throw new MyLeadException(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.caught(e);
            throw new MyLeadException(e);
        }
        return wfTempCollectionId;

    }

    private String createWorkflowTemplatesCollection(String userID, String projectId) throws MyLeadException {
        String collectionResourceId = null; // return value

        try {

            // Create a "Workflow Templates" collection for this project
            MinimalLEADMetadata collMetadata = new MinimalLEADMetadata(userID, WORKFLOW_TEMPLATES_COLLECTION,
                    "Workflow templates for project " + projectId);
            collMetadata.setResourceId("Unknown");

            CreateCollectionsRequestDocument createColl = CreateCollectionsRequestDocument.Factory.newInstance();
            CreateCollectionsRequestType collParams = createColl.addNewCreateCollectionsRequest();

            LEADresourceDocument workflowCollResourceDoc = LEADresourceDocument.Factory.parse(collMetadata.toString());
            LEADResourceType workflowCollResourceType = workflowCollResourceDoc.getLEADresource();

            CollectionType collectionType = collParams.addNewCollectionInfo();
            collectionType.setAssignNewResourceID(true);
            collectionType.setParentID(projectId);

            collectionType.setLEADresource(workflowCollResourceType);

            OperationResponseDocument outputMsg = createStub(userID).createCollections(createColl);
            OperationResponseType outputType = outputMsg.getOperationResponse();

            StatusEnumType.Enum statusType = outputType.getStatus();

            if (statusType.equals(StatusEnumType.SUCCESS)) {
                String[] resourceIDs = outputType.getResourceIDArray();
                for (String collectionId : resourceIDs) {
                    logger.finest("Workflow Collection Id is " + collectionId);
                    collectionResourceId = collectionId;
                }
                if (outputType.isSetMessage()) {
                    logger.finest("Add collection message is = " + outputType.getMessage());
                }
            } else {
                if (outputType.isSetMessage()) {
                    logger.severe("An error occurred when trying to create the "
                            + "workflow templates collection for the Sample: " + outputType.getMessage());
                }
                if (outputType.isSetOperationSummary()) {
                    logger.severe("An error occurred when trying to create the "
                            + "workflow templates collection for the Sample: " + outputType.getOperationSummary());
                }
                if (outputType.isSetOperationSummaryStreamUrl()) {
                    logger.severe("An error occurred when trying to create the "
                            + "workflow templates collection for the Sample: "
                            + outputType.getOperationSummaryStreamUrl());
                }
            }
        } catch (Exception e) {
            logger.caught(e);
            throw new MyLeadException(e);
        }

        return collectionResourceId;
    }

    /**
     * @param uid
     * @param parentID
     *            This can be either project ID or collection ID.
     * @param collectionName
     * @return The collection ID
     * @throws MyLeadException
     */
    // public String collectionNameToID(String uid, String parentID,
    // String collectionName) throws MyLeadException {
    // // XXX There must be a direct way to get the project with a specified
    // // title from myLEAD.
    // List<MyLeadQueryResultItem> collections = listCollections(uid, parentID);
    // for (MyLeadQueryResultItem collection : collections) {
    // String title = collection.getTitle();
    // if (collectionName.equals(title)) {
    // String resouceID = collection.getResouceID();
    // logger.finest("resourceID: " + resouceID);
    // return resouceID;
    // }
    // }
    // return null;
    // }
    /**
     * @param uid
     * @param collectionID
     * @return The projects
     * @throws MyLeadException
     */
    public LEADresourceDocument getCollection(String uid, String collectionID) throws MyLeadException {
        try {

            QueryByIDsRequestDocument inputMsg = QueryByIDsRequestDocument.Factory.newInstance();
            QueryByIDsRequestType params = inputMsg.addNewQueryByIDsRequest();
            params.addResourceID(collectionID);
            QueryResultConfigurationType queryResultConfigurationType = params.addNewQueryResultConfiguration();

            ContentFilterType.Enum cFilter = ContentFilterType.FULL_SCHEMA;
            queryResultConfigurationType.setContentFilter(cFilter);
            queryResultConfigurationType.setCount(0);
            queryResultConfigurationType.setOffset(0);
            queryResultConfigurationType.setHierarchyFilter(HierarchyFilterType.TARGET);
            ElementFilterType elemFilter = queryResultConfigurationType.addNewElementFilter();
            elemFilter.setElementName("title");
            elemFilter.setElementSource("LEAD");
            elemFilter.setPropertyName("citation");
            elemFilter.setPropertySource("LEAD");
            elemFilter.setElementFilter("title");

            QueryResponseSetType response = queryById(uid, inputMsg);

            List<ObjectResponseType> objectResponseList = response.getObjectResponseList();
            if (objectResponseList.size() > 0) {
                ObjectResponseType collection = objectResponseList.get(0);
                LEADresourceDocument leadDoc = LEADresourceDocument.Factory.newInstance();
                LEADResourceType leadResource = collection.getLEADresource();
                leadDoc.setLEADresource(leadResource);
                return leadDoc;
            } else {
                throw new MyLeadException("Collection not found for id :" + collectionID);
            }

        } catch (MyLeadException e) {
            // No collection with the specified ID found.
            return null;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    public AgentPortType createStub(String dn) throws MyLeadException {
        LeadContextHeader leadContextHeader = null;
        leadContextHeader = new LeadContextHeader("NEI", dn);
        leadContextHeader.setUserDn(dn);

        StickySoapHeaderHandler soapHeaderHandler = new StickySoapHeaderHandler("use-lead-header", leadContextHeader);
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(WSDLUtil.appendWSDLQuary(wsdlLoc), null, null, null, null);
        if (this.proxy != null) {
            PuretlsInvoker secureInvoker = new GsiInvoker(proxy, XBayaSecurity.getTrustedCertificates());
            Provider secureProvider = new Provider(secureInvoker);
            service.addLocalProvider(secureProvider);
        }

        WSIFClient wcl = XmlBeansWSIFRuntime.getDefault().newClientFor(service, null);
        wcl.addHandler(soapHeaderHandler);

        AgentPortType stub = (AgentPortType) wcl.generateDynamicStub(AgentPortType.class);

        return stub;

    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2004-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
