/*
 * Copyright (c) 2004-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLead.java,v 1.28 2009/01/30 22:23:26 smarru Exp $
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
import java.net.URISyntaxException;
import java.util.List;

import org.apache.airavata.xbaya.mylead.gui.MyleadWorkflowMetadata;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.xmlbeans.XmlException;
import org.ietf.jgss.GSSCredential;

import xsul5.MLogger;
import edu.indiana.extreme.lead.metadata.DataType;
import edu.indiana.extreme.lead.metadata.IdinfoBase;
import edu.indiana.extreme.lead.metadata.KeywordsType;
import edu.indiana.extreme.lead.metadata.LEADResourceType;
import edu.indiana.extreme.lead.metadata.LEADresourceDocument;
import edu.indiana.extreme.lead.metadata.ThemeType;
import edu.indiana.extreme.lead.metadata.util.MinimalLEADMetadata;

/**
 * @author Satoshi Shirasuna
 */
public class MyLead {

    /**
     * WORKFLOW_TEMPLATES_COLLECTION
     */
    public static final String WORKFLOW_TEMPLATES_COLLECTION = "Workflow Templates";

    /**
     * workflowTemplateID
     */
    public static final String WORKFLOW_TEMPLATE_ID_THEMEKT = "workflowTemplateID";

    private static final MLogger logger = MLogger.getLogger();

    private MyLeadConfiguration configuration;

    private MyLeadAgentStub stub;

    private GSSCredential proxy;

    /**
     * Creates a MyLeadConnection
     */
    public MyLead() {
        this.configuration = new MyLeadConfiguration();
    }

    /**
     * Creates a MyLeadConnection
     * 
     * @param configuration
     * @param proxy
     */
    public MyLead(MyLeadConfiguration configuration, GSSCredential proxy) {
        this.configuration = configuration;
        this.proxy = proxy;
    }

    /**
     * Returns the configuration.
     * 
     * @return the configuration
     */
    public MyLeadConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @param proxy
     */
    public void setProxy(GSSCredential proxy) {
        this.proxy = proxy;
    }

    /**
     * Saves a workflow
     * 
     * @param workflow
     * @param redeploy
     * @return The resource ID (myLEAD)
     * @throws MyLeadException
     */
    public String save(Workflow workflow, boolean redeploy) throws MyLeadException {

        URI templateID = workflow.getUniqueWorkflowName();
        if (templateID == null) {
            throw new IllegalStateException("The workflow has not be deployed to the Workflow Engine.");
        }

        try {
            connect();

            String uid = this.configuration.getUser();
            String project = this.configuration.getProject();

            if (redeploy) {
                LEADresourceDocument leadResourceDocument = this.stub.getCollection(uid, templateID.toString());
                if (leadResourceDocument != null) {
                    // No need to create a new collection because it's already
                    // there.

                    LEADResourceType leadResource = leadResourceDocument.getLEADresource();
                    String resourceID = leadResource.getResourceID();
                    return resourceID;
                }

                // It comes here when the workflow template is only in GPEL, not
                // in myLEAD.
            }

            String workflowTemplatesCollectionID = stub.getWorkflowTemplateCollectionId(uid, project);
            if (null == workflowTemplatesCollectionID) {
                throw new MyLeadException("Template collection not found for the project :" + project + " for user :"
                        + uid);
            }
            String name = workflow.getName();
            String description = workflow.getDescription();
            LEADresourceDocument resourceDocument = createResourceDocument(templateID, uid, name, description);

            // TODO change this to true in the future.
            boolean assignNewResourceID = false;
            String parentResourceID = workflowTemplatesCollectionID;
            //
            // LEADResourceType resourceType = LEADResourceType.Factory
            // .newInstance();
            // OtheridinfoType newWorkflow = resourceType.addNewWorkflow();

            String resourceID = this.stub.createCollection(uid, resourceDocument.getLEADresource(), parentResourceID,
                    assignNewResourceID);
            return resourceID;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * Loads a graph from of a specified workflow template
     * 
     * @param resourceID
     *            The resourceID (myLEAD) of the workflow template.
     * @return The workflow template ID (GPEL)
     * @throws MyLeadException
     */
    public URI load(String resourceID) throws MyLeadException {
        try {
            connect();

            String uid = this.configuration.getUser();
            LEADresourceDocument resourceDocument = this.stub.getCollection(uid, resourceID);
            String templateID = getWorkflowTemplateID(resourceDocument);
            return new URI(templateID);
        } catch (URISyntaxException e) {
            String message = "Workflow template ID is in wrong format.";
            throw new MyLeadException(message, e);
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    public URI loadWorkflow(String resourceID) throws MyLeadException {
        try {
            connect();

            String uid = this.configuration.getUser();
            LEADresourceDocument resourceDocument = this.stub.getCollection(uid, resourceID);
            String templateID = getWorkflowTemplateID(resourceDocument);
            return new URI(templateID);
        } catch (URISyntaxException e) {
            String message = "Workflow template ID is in wrong format.";
            throw new MyLeadException(message, e);
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    /**
     * Lists the workflow templates.
     * 
     * @return The list of workflow templates.
     * @throws MyLeadException
     */
    public List<MyleadWorkflowMetadata> list() throws MyLeadException {
        try {
            connect();

            String uid = this.configuration.getUser();
            String project = this.configuration.getProject();

            // String workflowTemplatesCollectionID =
            // getWorkflowTemplatesCollectionID(
            // uid, project);

            List<MyleadWorkflowMetadata> workflowTemplates = this.stub.getWorkflowResources(uid, project);
            return workflowTemplates;
        } catch (RuntimeException e) {
            throw new MyLeadException(e);
        }
    }

    private void connect() throws MyLeadException {
        if (this.configuration.isValid()) {
            // Reconnect only when necessary.
            // Check if the proxy is new too.
            if (this.stub == null || this.proxy != this.stub.getProxy()
                    || !this.configuration.getURL().equals(this.stub.getURL())) {
                this.stub = new MyLeadAgentStub(this.configuration.getURL(), this.proxy);
            }
        }
    }

    /**
     * @param uid
     * @param project
     * @return The ID of the workfow templates collection.
     * @throws MyLeadException
     */
    // private String getWorkflowTemplatesCollectionID(String uid, String
    // project)
    // throws MyLeadException {
    // if (!this.stub.doesUserHaveMyLeadAccount(uid)) {
    // throw new MyLeadException("User, " + uid
    // + " does not have a LEAD account.");
    // }
    //
    // String projectID;
    // if (project.startsWith("urn:uuid:")) {
    // // project is a project ID.
    // projectID = project;
    // } else {
    // // project is a project name.
    // // Get the project ID by the user ID and the project name.
    // projectID = this.stub.projectNameToID(uid, project);
    // if (projectID == null) {
    // throw new MyLeadException("Project, " + project
    // + ", doesn't exist under user, " + uid);
    // }
    // }
    //
    // // Get the ID of the workflow template collection by the user
    // // ID, the project ID as a parent ID, and the collection mame,
    // // "Workflow Templates"
    // String workflowTemplatesCollectionID = this.stub.collectionNameToID(
    // uid, projectID, WORKFLOW_TEMPLATES_COLLECTION);
    // if (workflowTemplatesCollectionID == null) {
    // // "Workflow Templates" collection should be created by myLEAD, but
    // // in case it doesn't exist, we will create it here.
    // MinimalLEADMetadata workflowTemplatesMetadata = new MinimalLEADMetadata(
    // uid, MyLead.WORKFLOW_TEMPLATES_COLLECTION,
    // "Workflow templates collection");
    // // This is neccesary.
    // workflowTemplatesMetadata.setResourceId("Unknown");
    // workflowTemplatesMetadata.construct();
    // workflowTemplatesCollectionID = this.stub.createCollection(uid,
    // workflowTemplatesMetadata.getLeadResourceDoc().getLEADresource(),
    // projectID, true);
    // }
    // return workflowTemplatesCollectionID;
    // }
    /**
     * @param templateID
     * @param uid
     * @param name
     * @param description
     * @return The LEADMetadata
     */
    private LEADresourceDocument createResourceDocument(URI templateID, String uid, String name, String description) {

        LEADresourceDocument leadResourceDoc = null;
        try {
            MinimalLEADMetadata metadata = new MinimalLEADMetadata(uid, name, description);
            metadata.setResourceId(templateID.toString());

            leadResourceDoc = LEADresourceDocument.Factory.parse(metadata.toString());

            LEADResourceType leadResourceType = leadResourceDoc.getLEADresource();

            DataType data = leadResourceType.getData();
            IdinfoBase idInfo = data.getIdinfo();
            KeywordsType keywords = idInfo.getKeywords();
            ThemeType leadProjectThema = keywords.getThemeArray(0);
            // This will get "leadproject.org";
            leadProjectThema.addThemekey("workflow");
            leadProjectThema.addThemekey("workflow-ODE");
            leadProjectThema.addThemekey("workflow-ODE-Process");

            // Set workflow ID.
            ThemeType idThema = keywords.addNewTheme();
            idThema.setThemekt(WORKFLOW_TEMPLATE_ID_THEMEKT);
            idThema.addThemekey(templateID.toString());

            XMLUtil.validate(leadResourceDoc);
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return leadResourceDoc;
    }

    private String getWorkflowTemplateID(LEADresourceDocument resourceDocument) {
        // Check if the templateID is set in the metadata.
        LEADResourceType resource = resourceDocument.getLEADresource();
        DataType data = resource.getData();
        IdinfoBase idInfo = data.getIdinfo();
        KeywordsType keywords = idInfo.getKeywords();
        ThemeType[] themes = keywords.getThemeArray();

        String templateID = null;
        for (ThemeType theme : themes) {
            String themekt = theme.getThemekt();
            if (WORKFLOW_TEMPLATE_ID_THEMEKT.equals(themekt)) {
                // Has only one key.
                templateID = theme.getThemekeyArray(0);
                break;
            }
        }

        if (templateID == null) {
            logger.warning("The LEAD metadata doesn't have workflow ID." + " Use resource ID as workflow ID instead.");
            // This happends with the metadata created by XBaya older than
            // 2.5.5.
            String resourceID = resource.getResourceID();
            templateID = resourceID;
        }
        return templateID;
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
